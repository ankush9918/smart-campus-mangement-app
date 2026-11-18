import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { S3Client, PutObjectCommand, GetObjectCommand, HeadObjectCommand, DeleteObjectCommand } from "npm:@aws-sdk/client-s3@3.682.0";
import { getSignedUrl } from "npm:@aws-sdk/s3-request-presigner@3.682.0";
import { createClient } from "npm:@supabase/supabase-js@2.39.8";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, GET, OPTIONS",
};

serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const authHeader = req.headers.get("Authorization");
    if (!authHeader) {
      return new Response(
        JSON.stringify({ error: "Missing Authorization header" }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseAnonKey = Deno.env.get("SUPABASE_ANON_KEY") ?? "";
    const supabaseServiceRole = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";

    const userClient = createClient(supabaseUrl, supabaseAnonKey, {
      global: { headers: { Authorization: authHeader } },
    });

    const { data: { user }, error: authError } = await userClient.auth.getUser();
    if (authError || !user) {
      return new Response(
        JSON.stringify({ error: "Unauthorized: Invalid or expired session", details: authError?.message }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // Determine user role
    let userRole = (user.user_metadata?.role || "").toLowerCase().trim();
    if (!userRole && supabaseServiceRole) {
      const adminClient = createClient(supabaseUrl, supabaseServiceRole);
      const { data: faculty } = await adminClient
        .from("faculty")
        .select("id")
        .or(`auth_user_id.eq.${user.id},email.ilike.${user.email}`)
        .maybeSingle();

      if (faculty) {
        userRole = "professor";
      } else {
        const { data: student } = await adminClient
          .from("students")
          .select("id")
          .or(`auth_user_id.eq.${user.id},email.ilike.${user.email}`)
          .maybeSingle();
        if (student) userRole = "student";
      }
    }

    // Configure S3 Client for Cloudflare R2
    const R2_ACCOUNT_ID = (Deno.env.get("R2_ACCOUNT_ID") || "").trim();
    const R2_ACCESS_KEY_ID = (Deno.env.get("R2_ACCESS_KEY_ID") || "").trim();
    const R2_SECRET_ACCESS_KEY = (Deno.env.get("R2_SECRET_ACCESS_KEY") || "").trim();
    const R2_BUCKET_NAME = (Deno.env.get("R2_BUCKET_NAME") || "college-erp-files").trim();
    let R2_ENDPOINT = (Deno.env.get("R2_ENDPOINT") || "").trim();

    if (!R2_ENDPOINT && R2_ACCOUNT_ID) {
      R2_ENDPOINT = `https://${R2_ACCOUNT_ID}.r2.cloudflarestorage.com`;
    } else if (R2_ENDPOINT && !R2_ENDPOINT.startsWith("http://") && !R2_ENDPOINT.startsWith("https://")) {
      R2_ENDPOINT = `https://${R2_ENDPOINT}`;
    }

    const s3Client = new S3Client({
      region: "auto",
      endpoint: R2_ENDPOINT,
      credentials: {
        accessKeyId: R2_ACCESS_KEY_ID,
        secretAccessKey: R2_SECRET_ACCESS_KEY,
      },
      forcePathStyle: true,
    });

    const url = new URL(req.url);
    const pathname = url.pathname;
    let body: any = {};
    if (req.method === "POST") {
      body = await req.json().catch(() => ({}));
    }

    const action = (body.action || (pathname.endsWith("/download") ? "download" : "upload")).toLowerCase();

    // ACTION: UPLOAD
    if (action === "upload") {
      const isFaculty = ["professor", "teacher", "faculty", "admin"].includes(userRole);
      if (!isFaculty) {
        return new Response(
          JSON.stringify({ error: "Forbidden: Only faculty members are authorized to upload PDFs." }),
          { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      const fileName = (body.fileName || "").trim();
      const fileType = (body.fileType || "assignment").toLowerCase().trim();
      const contentType = (body.contentType || "application/pdf").toLowerCase().trim();
      const fileSize = Number(body.fileSize || 0);

      if (!fileName) {
        return new Response(
          JSON.stringify({ error: "Invalid request: fileName is required." }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      // 1. Enforce file size validation (Max 25 MB)
      const MAX_FILE_SIZE = 25 * 1024 * 1024;
      if (fileSize > MAX_FILE_SIZE) {
        return new Response(
          JSON.stringify({ error: "File exceeds maximum permitted size of 25 MB." }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      // 2. Strict extension whitelist
      const lowerName = fileName.toLowerCase();
      const allowedExtensions = [".pdf", ".doc", ".docx", ".ppt", ".pptx", ".xls", ".xlsx", ".txt", ".csv", ".png", ".jpg", ".jpeg", ".webp", ".zip"];
      if (!allowedExtensions.some((ext) => lowerName.endsWith(ext))) {
        return new Response(
          JSON.stringify({ error: "Invalid file type: Only academic documents, spreadsheets, slides, images, and archives are permitted." }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      const disallowed = [".exe", ".bat", ".cmd", ".sh", ".php", ".js", ".vbs", ".apk"];
      if (disallowed.some((ext) => lowerName.endsWith(ext))) {
        return new Response(
          JSON.stringify({ error: "Invalid file type: Executable files are not allowed." }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      const prefix = fileType.includes("material") ? "study-materials" : "assignments";
      const cleanFileName = fileName.replace(/[^a-zA-Z0-9._-]/g, "_");
      const objectKey = `${prefix}/${Date.now()}_${cleanFileName}`;

      const command = new PutObjectCommand({
        Bucket: R2_BUCKET_NAME,
        Key: objectKey,
        ContentType: contentType,
      });

      const uploadUrl = await getSignedUrl(s3Client, command, { expiresIn: 900 });

      return new Response(
        JSON.stringify({
          success: true,
          uploadUrl,
          objectKey,
          bucket: R2_BUCKET_NAME,
          expiresIn: 900,
        }),
        { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // ACTION: DOWNLOAD
    if (action === "download") {
      const isAuthorized = ["student", "professor", "teacher", "faculty", "admin"].includes(userRole);
      if (!isAuthorized) {
        return new Response(
          JSON.stringify({ error: "Forbidden: User is not authorized to access files." }),
          { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      let rawKey = body.objectKey || body.key || url.searchParams.get("objectKey") || url.searchParams.get("key") || "";
      if (!rawKey) {
        return new Response(
          JSON.stringify({ error: "Invalid request: objectKey is required." }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      let cleanKey = rawKey.trim();
      if (cleanKey.startsWith("r2://")) {
        cleanKey = cleanKey.replace(/^r2:\/\/[^/]+\//, "").replace(/^r2:\/\//, "");
      }
      if (cleanKey.includes("..") || cleanKey.includes("//") || cleanKey.includes("\\")) {
        return new Response(
          JSON.stringify({ error: "Invalid object key: Path traversal is not permitted." }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      if (!cleanKey.startsWith("assignments/") && !cleanKey.startsWith("study-materials/")) {
        return new Response(
          JSON.stringify({ error: "Forbidden: Storage access is restricted to assignments and study-materials." }),
          { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      if (cleanKey.startsWith("http://") || cleanKey.startsWith("https://")) {
        if (cleanKey.includes(".supabase.co/storage/")) {
          return new Response(
            JSON.stringify({
              success: true,
              downloadUrl: cleanKey,
              isLegacy: true,
              expiresIn: 3600,
            }),
            { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
          );
        }
      }

      const command = new GetObjectCommand({
        Bucket: R2_BUCKET_NAME,
        Key: cleanKey,
      });

      const downloadUrl = await getSignedUrl(s3Client, command, { expiresIn: 900 });

      return new Response(
        JSON.stringify({
          success: true,
          downloadUrl,
          objectKey: cleanKey,
          expiresIn: 900,
        }),
        { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // ACTION: COMPLETE (Verify uploaded file in Cloudflare R2 bucket)
    if (action === "complete") {
      const isFaculty = ["professor", "teacher", "faculty", "admin"].includes(userRole);
      if (!isFaculty) {
        return new Response(
          JSON.stringify({ error: "Forbidden: Only faculty members are authorized to complete uploads." }),
          { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      let rawKey = body.objectKey || body.key || "";
      if (!rawKey) {
        return new Response(
          JSON.stringify({ error: "Invalid request: objectKey is required." }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      let cleanKey = rawKey.trim();
      if (cleanKey.startsWith("r2://")) {
        cleanKey = cleanKey.replace(/^r2:\/\/[^/]+\//, "").replace(/^r2:\/\//, "");
      }
      if (cleanKey.includes("..") || cleanKey.includes("//") || cleanKey.includes("\\")) {
        return new Response(
          JSON.stringify({ error: "Invalid object key: Path traversal is not permitted." }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      if (!cleanKey.startsWith("assignments/") && !cleanKey.startsWith("study-materials/")) {
        return new Response(
          JSON.stringify({ error: "Forbidden: Storage access is restricted to assignments and study-materials." }),
          { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      try {
        const headCommand = new HeadObjectCommand({
          Bucket: R2_BUCKET_NAME,
          Key: cleanKey,
        });
        const headResult = await s3Client.send(headCommand);

        return new Response(
          JSON.stringify({
            success: true,
            verified: true,
            objectKey: cleanKey,
            contentLength: headResult.ContentLength,
            etag: headResult.ETag,
            contentType: headResult.ContentType,
            lastModified: headResult.LastModified,
          }),
          { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      } catch (headErr: any) {
        const statusCode = headErr?.$metadata?.httpStatusCode || 404;
        return new Response(
          JSON.stringify({
            success: false,
            verified: false,
            error: "Cloudflare R2 verification failed: Object does not exist in bucket.",
            details: headErr.message,
          }),
          { status: statusCode === 404 ? 404 : 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }
    }

    // ACTION: DELETE
    if (action === "delete") {
      const isFaculty = ["professor", "teacher", "faculty", "admin"].includes(userRole);
      if (!isFaculty) {
        return new Response(
          JSON.stringify({ error: "Forbidden: Only faculty members are authorized to delete files." }),
          { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      let rawKey = body.objectKey || body.key || "";
      if (!rawKey) {
        return new Response(
          JSON.stringify({ error: "Invalid request: objectKey is required." }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      let cleanKey = rawKey.trim();
      if (cleanKey.startsWith("r2://")) {
        cleanKey = cleanKey.replace(/^r2:\/\/[^/]+\//, "").replace(/^r2:\/\//, "");
      }
      if (cleanKey.includes("..") || cleanKey.includes("//") || cleanKey.includes("\\")) {
        return new Response(
          JSON.stringify({ error: "Invalid object key: Path traversal is not permitted." }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      if (!cleanKey.startsWith("assignments/") && !cleanKey.startsWith("study-materials/")) {
        return new Response(
          JSON.stringify({ error: "Forbidden: Storage access is restricted to assignments and study-materials." }),
          { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      const delCommand = new DeleteObjectCommand({
        Bucket: R2_BUCKET_NAME,
        Key: cleanKey,
      });
      await s3Client.send(delCommand);

      return new Response(
        JSON.stringify({
          success: true,
          deletedKey: cleanKey,
          message: "File deleted successfully from Cloudflare R2.",
        }),
        { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    return new Response(
      JSON.stringify({ error: `Unknown action '${action}'. Valid actions are 'upload', 'download', 'complete', or 'delete'.` }),
      { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (err: any) {
    return new Response(
      JSON.stringify({ error: err.message || "Internal server error" }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
