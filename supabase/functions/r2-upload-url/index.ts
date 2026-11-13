import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { S3Client, PutObjectCommand } from "npm:@aws-sdk/client-s3@3.682.0";
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

    // Role-based Access Control: Verify caller is Faculty / Professor / Admin
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
      }
    }

    const isAuthorized = ["professor", "teacher", "faculty", "admin"].includes(userRole);
    if (!isAuthorized) {
      return new Response(
        JSON.stringify({ error: "Forbidden: Only faculty members are authorized to upload files." }),
        { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const body = await req.json().catch(() => ({}));
    const fileName = (body.fileName || "").trim();
    const fileType = (body.fileType || "assignment").toLowerCase().trim();
    const contentType = (body.contentType || "application/pdf").toLowerCase().trim();

    if (!fileName) {
      return new Response(
        JSON.stringify({ error: "Invalid request: fileName is required." }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // Disallow dangerous executable extensions
    const lowerName = fileName.toLowerCase();
    const disallowed = [".exe", ".bat", ".cmd", ".sh", ".php", ".js", ".vbs", ".apk", ".bin"];
    if (disallowed.some((ext) => lowerName.endsWith(ext))) {
      return new Response(
        JSON.stringify({ error: "Invalid file type: Executable/script files are strictly disallowed." }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

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

    const prefix = fileType.includes("material") ? "study-materials" : "assignments";
    // Preserve original filename while safely sanitizing special filesystem characters
    const cleanFileName = fileName.replace(/[^a-zA-Z0-9._-]/g, "_");
    const objectKey = `${prefix}/${Date.now()}_${cleanFileName}`;

    const command = new PutObjectCommand({
      Bucket: R2_BUCKET_NAME,
      Key: objectKey,
      ContentType: contentType,
    });

    // Generate short-lived presigned PUT URL (valid for 3600 seconds)
    const uploadUrl = await getSignedUrl(s3Client, command, { expiresIn: 3600 });

    return new Response(
      JSON.stringify({
        success: true,
        uploadUrl,
        objectKey,
        bucket: R2_BUCKET_NAME,
        expiresIn: 3600,
      }),
      { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (err: any) {
    return new Response(
      JSON.stringify({ error: err.message || "Internal server error" }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
