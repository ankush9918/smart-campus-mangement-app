import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { S3Client, GetObjectCommand, HeadObjectCommand } from "npm:@aws-sdk/client-s3@3.682.0";
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

    // Role-based Access Control: Both Students & Faculty are authorized to download
    let userRole = (user.user_metadata?.role || "").toLowerCase().trim();
    if (!userRole && supabaseServiceRole) {
      const adminClient = createClient(supabaseUrl, supabaseServiceRole);
      const { data: student } = await adminClient
        .from("students")
        .select("id")
        .or(`auth_user_id.eq.${user.id},email.ilike.${user.email}`)
        .maybeSingle();

      if (student) {
        userRole = "student";
      } else {
        const { data: faculty } = await adminClient
          .from("faculty")
          .select("id")
          .or(`auth_user_id.eq.${user.id},email.ilike.${user.email}`)
          .maybeSingle();
        if (faculty) userRole = "professor";
      }
    }

    const isAuthorized = ["student", "professor", "teacher", "faculty", "admin"].includes(userRole);
    if (!isAuthorized) {
      return new Response(
        JSON.stringify({ error: "Forbidden: User is not authorized to access files." }),
        { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // Support both POST with JSON body or GET with query parameter ?key=...
    let rawKey = "";
    if (req.method === "POST") {
      const body = await req.json().catch(() => ({}));
      rawKey = body.objectKey || body.key || "";
    } else {
      const url = new URL(req.url);
      rawKey = url.searchParams.get("objectKey") || url.searchParams.get("key") || "";
    }

    if (!rawKey) {
      return new Response(
        JSON.stringify({ error: "Invalid request: objectKey is required." }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // Sanitize object key
    let cleanKey = rawKey.trim();
    if (cleanKey.startsWith("r2://")) {
      cleanKey = cleanKey.replace(/^r2:\/\/[^/]+\//, "").replace(/^r2:\/\//, "");
    }
    if (cleanKey.includes("..")) {
      return new Response(
        JSON.stringify({ error: "Invalid object key: Path traversal is not permitted." }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // Check if legacy Supabase storage URL passed accidentally
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

    // Check if object exists in R2
    try {
      await s3Client.send(new HeadObjectCommand({
        Bucket: R2_BUCKET_NAME,
        Key: cleanKey,
      }));
    } catch (headErr: any) {
      return new Response(
        JSON.stringify({
          success: false,
          error: "Requested file not found in storage bucket.",
          details: headErr.message,
        }),
        { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const command = new GetObjectCommand({
      Bucket: R2_BUCKET_NAME,
      Key: cleanKey,
    });

    const downloadUrl = await getSignedUrl(s3Client, command, { expiresIn: 3600 });

    return new Response(
      JSON.stringify({
        success: true,
        downloadUrl,
        objectKey: cleanKey,
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
