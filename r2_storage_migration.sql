-- ======================================================================
-- MIGRATION: Cloudflare R2 Storage Integration for Assignments & Study Materials
-- Description: Adds storage_provider and r2_key metadata columns while preserving
--              all existing Supabase Storage records and public URLs.
-- ======================================================================

-- 1. Update public.assignments table
ALTER TABLE public.assignments 
    ADD COLUMN IF NOT EXISTS storage_provider TEXT DEFAULT 'supabase',
    ADD COLUMN IF NOT EXISTS r2_key TEXT;

-- 2. Update public.study_materials table
ALTER TABLE public.study_materials 
    ADD COLUMN IF NOT EXISTS storage_provider TEXT DEFAULT 'supabase',
    ADD COLUMN IF NOT EXISTS r2_key TEXT;

-- 3. Mark all existing records with Supabase Storage provider for backward compatibility
UPDATE public.assignments 
SET storage_provider = 'supabase' 
WHERE storage_provider IS NULL OR storage_provider = '';

UPDATE public.study_materials 
SET storage_provider = 'supabase' 
WHERE storage_provider IS NULL OR storage_provider = '';

-- 4. Create indices for fast provider lookups
CREATE INDEX IF NOT EXISTS idx_assignments_storage_provider ON public.assignments(storage_provider);
CREATE INDEX IF NOT EXISTS idx_assignments_r2_key ON public.assignments(r2_key);
CREATE INDEX IF NOT EXISTS idx_study_materials_storage_provider ON public.study_materials(storage_provider);
CREATE INDEX IF NOT EXISTS idx_study_materials_r2_key ON public.study_materials(r2_key);

-- 5. Force PostgREST schema cache reload so new columns are immediately queryable via REST
NOTIFY pgrst, 'reload schema';
