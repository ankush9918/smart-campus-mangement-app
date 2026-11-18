import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { S3Client, DeleteObjectCommand } from "npm:@aws-sdk/client-s3@3.682.0";
import { createClient } from "npm:@supabase/supabase-js@2.39.8";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, GET, OPTIONS, DELETE",
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

    // Role-based Access Control: Only Faculty / Admin can delete files
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
        JSON.stringify({ error: "Forbidden: Only faculty members are authorized to delete files." }),
        { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const body = await req.json().catch(() => ({}));
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
        JSON.stringify({ error: "Forbidden: Deletion is restricted to assignments and study-materials." }),
        { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } }
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

    const command = new DeleteObjectCommand({
      Bucket: R2_BUCKET_NAME,
      Key: cleanKey,
    });

    await s3Client.send(command);

    return new Response(
      JSON.stringify({
        success: true,
        deletedKey: cleanKey,
        message: "File deleted successfully from Cloudflare R2.",
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
