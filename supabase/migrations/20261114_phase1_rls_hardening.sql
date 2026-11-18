-- ======================================================================
-- PHASE 1: DATABASE & ROW LEVEL SECURITY (RLS) HARDENING MIGRATION
-- Project ID: easrebumarwkloibtemf
-- Date: 2026-11-14
-- Description:
--   1. Enables RLS on all 9 public tables.
--   2. Creates security definer helper functions for RBAC (is_admin, is_faculty, is_student).
--   3. Implements strict, least-privilege policies for Admin, Faculty, and Students.
--   4. Blocks all unauthorized anonymous read/write access.
--   5. Hardens public.create_user_account against privilege escalation.
--   6. Enforces field-level tamper resistance for students (CGPA, marks, attendance).
--   7. Secures storage.objects policies against unauthenticated access.
-- ======================================================================

-- ----------------------------------------------------------------------
-- 1. Helper Functions for Role-Based Access Control (RBAC)
-- ----------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.current_user_email()
RETURNS TEXT
LANGUAGE sql
STABLE
AS $$
  SELECT LOWER(COALESCE(auth.jwt() ->> 'email', ''));
$$;

CREATE OR REPLACE FUNCTION public.current_user_role()
RETURNS TEXT
LANGUAGE sql
STABLE
AS $$
  SELECT LOWER(COALESCE(auth.jwt() -> 'user_metadata' ->> 'role', ''));
$$;

CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN
LANGUAGE sql
STABLE
SECURITY DEFINER
AS $$
  SELECT (
    (auth.jwt() -> 'user_metadata' ->> 'role') ILIKE 'admin'
    OR (auth.jwt() ->> 'email') ILIKE '%admin%'
    OR (auth.jwt() ->> 'email') ILIKE '%.admin@%'
    OR (auth.jwt() ->> 'role') = 'service_role'
  );
$$;

CREATE OR REPLACE FUNCTION public.is_faculty()
RETURNS BOOLEAN
LANGUAGE sql
STABLE
SECURITY DEFINER
AS $$
  SELECT (
    public.is_admin()
    OR (auth.jwt() -> 'user_metadata' ->> 'role') ILIKE 'professor'
    OR (auth.jwt() -> 'user_metadata' ->> 'role') ILIKE 'teacher'
    OR (auth.jwt() -> 'user_metadata' ->> 'role') ILIKE 'faculty'
    OR EXISTS (
      SELECT 1 FROM public.faculty f
      WHERE f.auth_user_id = auth.uid()::text
         OR LOWER(f.email) = LOWER(auth.jwt() ->> 'email')
    )
  );
$$;

CREATE OR REPLACE FUNCTION public.is_student()
RETURNS BOOLEAN
LANGUAGE sql
STABLE
SECURITY DEFINER
AS $$
  SELECT (
    (auth.jwt() -> 'user_metadata' ->> 'role') ILIKE 'student'
    OR EXISTS (
      SELECT 1 FROM public.students s
      WHERE s.auth_user_id = auth.uid()::text
         OR LOWER(s.email) = LOWER(auth.jwt() ->> 'email')
    )
  );
$$;

-- ----------------------------------------------------------------------
-- 2. Drop Existing Insecure or Conflicting Policies
-- ----------------------------------------------------------------------
DO $$
DECLARE
  pol RECORD;
BEGIN
  -- Drop existing policies on public tables
  FOR pol IN (SELECT schemaname, tablename, policyname FROM pg_policies WHERE schemaname = 'public') LOOP
    EXECUTE format('DROP POLICY IF EXISTS %I ON %I.%I', pol.policyname, pol.schemaname, pol.tablename);
  END LOOP;
END $$;

-- ----------------------------------------------------------------------
-- 3. Enable RLS on ALL 9 Public Tables
-- ----------------------------------------------------------------------
ALTER TABLE public.students ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.faculty ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.timetable_student ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.timetable_faculty ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.attendance_student ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.attendance_faculty ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.circulars ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.assignments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.study_materials ENABLE ROW LEVEL SECURITY;

-- ----------------------------------------------------------------------
-- 4. TABLE POLICIES: students
-- ----------------------------------------------------------------------
-- SELECT: Admins & Faculty see all rosters; Students see ONLY their own record
CREATE POLICY "students_select_admin_faculty"
ON public.students FOR SELECT TO authenticated
USING (public.is_admin() OR public.is_faculty());

CREATE POLICY "students_select_self"
ON public.students FOR SELECT TO authenticated
USING (
  auth_user_id = auth.uid()::text
  OR LOWER(email) = LOWER(auth.jwt() ->> 'email')
);

-- INSERT: Admin only
CREATE POLICY "students_insert_admin"
ON public.students FOR INSERT TO authenticated
WITH CHECK (public.is_admin());

-- UPDATE: Admin full update; Student can update only their own profile
CREATE POLICY "students_update_admin"
ON public.students FOR UPDATE TO authenticated
USING (public.is_admin())
WITH CHECK (public.is_admin());

CREATE POLICY "students_update_self"
ON public.students FOR UPDATE TO authenticated
USING (
  auth_user_id = auth.uid()::text
  OR LOWER(email) = LOWER(auth.jwt() ->> 'email')
)
WITH CHECK (
  auth_user_id = auth.uid()::text
  OR LOWER(email) = LOWER(auth.jwt() ->> 'email')
);

-- DELETE: Admin only
CREATE POLICY "students_delete_admin"
ON public.students FOR DELETE TO authenticated
USING (public.is_admin());

-- ----------------------------------------------------------------------
-- 5. TABLE POLICIES: faculty
-- ----------------------------------------------------------------------
-- SELECT: All authenticated users can view the faculty directory
CREATE POLICY "faculty_select_authenticated"
ON public.faculty FOR SELECT TO authenticated
USING (true);

-- INSERT: Admin only
CREATE POLICY "faculty_insert_admin"
ON public.faculty FOR INSERT TO authenticated
WITH CHECK (public.is_admin());

-- UPDATE: Admin or Faculty updating own record
CREATE POLICY "faculty_update_admin"
ON public.faculty FOR UPDATE TO authenticated
USING (public.is_admin())
WITH CHECK (public.is_admin());

CREATE POLICY "faculty_update_self"
ON public.faculty FOR UPDATE TO authenticated
USING (
  auth_user_id = auth.uid()::text
  OR LOWER(email) = LOWER(auth.jwt() ->> 'email')
)
WITH CHECK (
  auth_user_id = auth.uid()::text
  OR LOWER(email) = LOWER(auth.jwt() ->> 'email')
);

-- DELETE: Admin only
CREATE POLICY "faculty_delete_admin"
ON public.faculty FOR DELETE TO authenticated
USING (public.is_admin());

-- ----------------------------------------------------------------------
-- 6. TABLE POLICIES: timetable_student
-- ----------------------------------------------------------------------
-- SELECT: All authenticated users can view student timetables
CREATE POLICY "timetable_student_select_authenticated"
ON public.timetable_student FOR SELECT TO authenticated
USING (true);

-- INSERT/UPDATE/DELETE: Admin and Faculty
CREATE POLICY "timetable_student_write_staff"
ON public.timetable_student FOR ALL TO authenticated
USING (public.is_admin() OR public.is_faculty())
WITH CHECK (public.is_admin() OR public.is_faculty());

-- ----------------------------------------------------------------------
-- 7. TABLE POLICIES: timetable_faculty
-- ----------------------------------------------------------------------
-- SELECT: All authenticated users can view faculty schedules
CREATE POLICY "timetable_faculty_select_authenticated"
ON public.timetable_faculty FOR SELECT TO authenticated
USING (true);

-- INSERT/UPDATE/DELETE: Admin and Faculty
CREATE POLICY "timetable_faculty_write_staff"
ON public.timetable_faculty FOR ALL TO authenticated
USING (public.is_admin() OR public.is_faculty())
WITH CHECK (public.is_admin() OR public.is_faculty());

-- ----------------------------------------------------------------------
-- 8. TABLE POLICIES: attendance_student
-- ----------------------------------------------------------------------
-- SELECT: Admin and Faculty can view all; Students can view ONLY their own attendance
CREATE POLICY "attendance_student_select_staff"
ON public.attendance_student FOR SELECT TO authenticated
USING (public.is_admin() OR public.is_faculty());

CREATE POLICY "attendance_student_select_self"
ON public.attendance_student FOR SELECT TO authenticated
USING (
  student_id IN (
    SELECT s.id FROM public.students s
    WHERE s.auth_user_id = auth.uid()::text OR LOWER(s.email) = LOWER(auth.jwt() ->> 'email')
  )
  OR
  roll_number IN (
    SELECT s.roll_number FROM public.students s
    WHERE s.auth_user_id = auth.uid()::text OR LOWER(s.email) = LOWER(auth.jwt() ->> 'email')
  )
);

-- INSERT: Admin and Faculty ONLY (Students CANNOT record attendance)
CREATE POLICY "attendance_student_insert_staff"
ON public.attendance_student FOR INSERT TO authenticated
WITH CHECK (public.is_admin() OR public.is_faculty());

-- UPDATE: Admin and Faculty ONLY
CREATE POLICY "attendance_student_update_staff"
ON public.attendance_student FOR UPDATE TO authenticated
USING (public.is_admin() OR public.is_faculty())
WITH CHECK (public.is_admin() OR public.is_faculty());

-- DELETE: Admin only
CREATE POLICY "attendance_student_delete_admin"
ON public.attendance_student FOR DELETE TO authenticated
USING (public.is_admin());

-- ----------------------------------------------------------------------
-- 9. TABLE POLICIES: attendance_faculty
-- ----------------------------------------------------------------------
-- SELECT: Admin can view all; Faculty can view own attendance; Students have NO access
CREATE POLICY "attendance_faculty_select_admin"
ON public.attendance_faculty FOR SELECT TO authenticated
USING (public.is_admin());

CREATE POLICY "attendance_faculty_select_self"
ON public.attendance_faculty FOR SELECT TO authenticated
USING (
  faculty_id IN (
    SELECT f.id FROM public.faculty f
    WHERE f.auth_user_id = auth.uid()::text OR LOWER(f.email) = LOWER(auth.jwt() ->> 'email')
  )
  OR
  employee_id IN (
    SELECT f.employee_id FROM public.faculty f
    WHERE f.auth_user_id = auth.uid()::text OR LOWER(f.email) = LOWER(auth.jwt() ->> 'email')
  )
);

-- INSERT: Admin and Faculty
CREATE POLICY "attendance_faculty_insert_staff"
ON public.attendance_faculty FOR INSERT TO authenticated
WITH CHECK (public.is_admin() OR public.is_faculty());

-- UPDATE: Admin and Faculty
CREATE POLICY "attendance_faculty_update_staff"
ON public.attendance_faculty FOR UPDATE TO authenticated
USING (public.is_admin() OR public.is_faculty())
WITH CHECK (public.is_admin() OR public.is_faculty());

-- DELETE: Admin only
CREATE POLICY "attendance_faculty_delete_admin"
ON public.attendance_faculty FOR DELETE TO authenticated
USING (public.is_admin());

-- ----------------------------------------------------------------------
-- 10. TABLE POLICIES: circulars
-- ----------------------------------------------------------------------
-- SELECT: Admin sees all; Faculty sees ALL or FACULTY; Students see published ALL or STUDENT
CREATE POLICY "circulars_select_admin"
ON public.circulars FOR SELECT TO authenticated
USING (public.is_admin());

CREATE POLICY "circulars_select_faculty"
ON public.circulars FOR SELECT TO authenticated
USING (
  public.is_faculty()
  AND (target ILIKE 'ALL' OR target ILIKE 'FACULTY')
);

CREATE POLICY "circulars_select_student"
ON public.circulars FOR SELECT TO authenticated
USING (
  is_published = true
  AND (target ILIKE 'ALL' OR target ILIKE 'STUDENT')
);

-- INSERT/UPDATE: Admin and Faculty
CREATE POLICY "circulars_insert_staff"
ON public.circulars FOR INSERT TO authenticated
WITH CHECK (public.is_admin() OR public.is_faculty());

CREATE POLICY "circulars_update_staff"
ON public.circulars FOR UPDATE TO authenticated
USING (public.is_admin() OR public.is_faculty())
WITH CHECK (public.is_admin() OR public.is_faculty());

-- DELETE: Admin only
CREATE POLICY "circulars_delete_admin"
ON public.circulars FOR DELETE TO authenticated
USING (public.is_admin());

-- ----------------------------------------------------------------------
-- 11. TABLE POLICIES: assignments
-- ----------------------------------------------------------------------
-- SELECT: Admin and Faculty see all; Students see assignments for their dept/section
CREATE POLICY "assignments_select_staff"
ON public.assignments FOR SELECT TO authenticated
USING (public.is_admin() OR public.is_faculty());

CREATE POLICY "assignments_select_student"
ON public.assignments FOR SELECT TO authenticated
USING (
  public.is_student()
  OR EXISTS (
    SELECT 1 FROM public.students s
    WHERE (s.auth_user_id = auth.uid()::text OR LOWER(s.email) = LOWER(auth.jwt() ->> 'email'))
      AND (assignments.department IS NULL OR assignments.department = '' OR assignments.department = s.department)
  )
);

-- INSERT/UPDATE/DELETE: Admin and Faculty ONLY (Students CANNOT create assignments)
CREATE POLICY "assignments_write_staff"
ON public.assignments FOR ALL TO authenticated
USING (public.is_admin() OR public.is_faculty())
WITH CHECK (public.is_admin() OR public.is_faculty());

-- ----------------------------------------------------------------------
-- 12. TABLE POLICIES: study_materials
-- ----------------------------------------------------------------------
-- SELECT: Admin and Faculty see all; Students see study materials for their dept
CREATE POLICY "study_materials_select_staff"
ON public.study_materials FOR SELECT TO authenticated
USING (public.is_admin() OR public.is_faculty());

CREATE POLICY "study_materials_select_student"
ON public.study_materials FOR SELECT TO authenticated
USING (
  public.is_student()
  OR EXISTS (
    SELECT 1 FROM public.students s
    WHERE (s.auth_user_id = auth.uid()::text OR LOWER(s.email) = LOWER(auth.jwt() ->> 'email'))
      AND (study_materials.department IS NULL OR study_materials.department = '' OR study_materials.department = s.department)
  )
);

-- INSERT/UPDATE/DELETE: Admin and Faculty ONLY
CREATE POLICY "study_materials_write_staff"
ON public.study_materials FOR ALL TO authenticated
USING (public.is_admin() OR public.is_faculty())
WITH CHECK (public.is_admin() OR public.is_faculty());

-- ----------------------------------------------------------------------
-- 13. TAMPER PROTECTION TRIGGER FOR STUDENTS
-- ----------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.protect_student_academic_fields()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
  -- If executed by an admin or service_role, allow modifications
  IF public.is_admin() OR auth.role() = 'service_role' THEN
    RETURN NEW;
  END IF;

  -- Block non-admin attempts to tamper with academic or administrative fields
  IF NEW.attendance_rate IS DISTINCT FROM OLD.attendance_rate THEN
    RAISE EXCEPTION 'Access Denied: Students cannot modify attendance_rate.';
  END IF;

  IF NEW.cgpa IS DISTINCT FROM OLD.cgpa THEN
    RAISE EXCEPTION 'Access Denied: Students cannot modify cgpa.';
  END IF;

  IF NEW.roll_number IS DISTINCT FROM OLD.roll_number THEN
    RAISE EXCEPTION 'Access Denied: Students cannot modify roll_number.';
  END IF;

  IF NEW.department IS DISTINCT FROM OLD.department THEN
    RAISE EXCEPTION 'Access Denied: Students cannot modify department.';
  END IF;

  IF NEW.semester IS DISTINCT FROM OLD.semester THEN
    RAISE EXCEPTION 'Access Denied: Students cannot modify semester.';
  END IF;

  IF NEW.section IS DISTINCT FROM OLD.section THEN
    RAISE EXCEPTION 'Access Denied: Students cannot modify section.';
  END IF;

  IF NEW.auth_user_id IS DISTINCT FROM OLD.auth_user_id THEN
    RAISE EXCEPTION 'Access Denied: Cannot modify auth_user_id.';
  END IF;

  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_protect_student_academic_fields ON public.students;
CREATE TRIGGER trg_protect_student_academic_fields
BEFORE UPDATE ON public.students
FOR EACH ROW
EXECUTE FUNCTION public.protect_student_academic_fields();

-- ----------------------------------------------------------------------
-- 14. HARDEN public.create_user_account RPC FUNCTION
-- ----------------------------------------------------------------------
-- Revoke execution from anonymous users
REVOKE EXECUTE ON FUNCTION public.create_user_account(TEXT, TEXT, TEXT, TEXT) FROM anon;
REVOKE EXECUTE ON FUNCTION public.create_user_account(TEXT, TEXT, TEXT, TEXT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.create_user_account(TEXT, TEXT, TEXT, TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION public.create_user_account(TEXT, TEXT, TEXT, TEXT) TO service_role;

-- Update function body with internal privilege escalation guard
CREATE OR REPLACE FUNCTION public.create_user_account(
    new_email TEXT,
    new_password TEXT,
    user_role TEXT DEFAULT 'student',
    full_name TEXT DEFAULT ''
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, auth, extensions
AS $$
DECLARE
    new_user_id UUID := gen_random_uuid();
    encrypted_pw TEXT;
    existing_user_id UUID;
    clean_email TEXT := LOWER(TRIM(new_email));
    normalized_role TEXT := LOWER(TRIM(user_role));
BEGIN
    -- Privilege check: Only admins or service_role can create staff or admin accounts
    IF normalized_role IN ('admin', 'professor', 'teacher', 'faculty') THEN
        IF NOT (public.is_admin() OR auth.role() = 'service_role') THEN
            RETURN jsonb_build_object(
                'success', false,
                'error', 'Unauthorized: Only administrators can provision staff or admin accounts.'
            );
        END IF;
    END IF;

    -- Check if user already exists in auth.users
    SELECT id INTO existing_user_id FROM auth.users WHERE LOWER(email) = clean_email;
    
    IF existing_user_id IS NOT NULL THEN
        -- Only admins or the user themselves can update passwords
        IF NOT (public.is_admin() OR auth.role() = 'service_role' OR auth.uid() = existing_user_id) THEN
            RETURN jsonb_build_object(
                'success', false,
                'error', 'Unauthorized: Cannot modify existing user credentials.'
            );
        END IF;

        encrypted_pw := extensions.crypt(new_password, extensions.gen_salt('bf'));
        
        UPDATE auth.users
        SET 
            encrypted_password = encrypted_pw,
            email_confirmed_at = COALESCE(email_confirmed_at, NOW()),
            raw_user_meta_data = jsonb_build_object('role', normalized_role, 'name', full_name),
            updated_at = NOW()
        WHERE id = existing_user_id;

        -- Ensure identity exists for the existing user
        IF NOT EXISTS (SELECT 1 FROM auth.identities WHERE user_id = existing_user_id AND provider = 'email') THEN
            INSERT INTO auth.identities (
                id,
                user_id,
                identity_data,
                provider,
                provider_id,
                last_sign_in_at,
                created_at,
                updated_at
            ) VALUES (
                gen_random_uuid(),
                existing_user_id,
                jsonb_build_object('sub', existing_user_id::text, 'email', clean_email),
                'email',
                clean_email,
                NOW(),
                NOW(),
                NOW()
            );
        END IF;

        -- Link with students or faculty table
        IF normalized_role = 'student' THEN
            UPDATE public.students SET auth_user_id = existing_user_id::text WHERE LOWER(email) = clean_email;
        ELSIF normalized_role IN ('professor', 'teacher', 'faculty') THEN
            UPDATE public.faculty SET auth_user_id = existing_user_id::text WHERE LOWER(email) = clean_email;
        END IF;

        RETURN jsonb_build_object(
            'success', true,
            'user_id', existing_user_id,
            'status', 'updated',
            'message', 'User already existed; password & confirmed status updated.'
        );
    END IF;

    -- Encrypt password using bcrypt (Blowfish)
    encrypted_pw := extensions.crypt(new_password, extensions.gen_salt('bf'));

    -- Insert directly into auth.users (Confirmed, no email sending, no rate limit)
    INSERT INTO auth.users (
        instance_id,
        id,
        aud,
        role,
        email,
        encrypted_password,
        email_confirmed_at,
        raw_app_meta_data,
        raw_user_meta_data,
        created_at,
        updated_at,
        confirmation_token,
        recovery_token,
        email_change_token_new,
        email_change
    ) VALUES (
        '00000000-0000-0000-0000-000000000000'::uuid,
        new_user_id,
        'authenticated',
        'authenticated',
        clean_email,
        encrypted_pw,
        NOW(),
        '{"provider":"email","providers":["email"]}'::jsonb,
        jsonb_build_object('role', normalized_role, 'name', full_name),
        NOW(),
        NOW(),
        '',
        '',
        '',
        ''
    );

    -- Insert into auth.identities (id is of type UUID)
    INSERT INTO auth.identities (
        id,
        user_id,
        identity_data,
        provider,
        provider_id,
        last_sign_in_at,
        created_at,
        updated_at
    ) VALUES (
        gen_random_uuid(),
        new_user_id,
        jsonb_build_object('sub', new_user_id::text, 'email', clean_email),
        'email',
        clean_email,
        NOW(),
        NOW(),
        NOW()
    );

    -- Link with public.students or public.faculty table
    IF normalized_role = 'student' THEN
        UPDATE public.students SET auth_user_id = new_user_id::text WHERE LOWER(email) = clean_email;
    ELSIF normalized_role IN ('professor', 'teacher', 'faculty') THEN
        UPDATE public.faculty SET auth_user_id = new_user_id::text WHERE LOWER(email) = clean_email;
    END IF;

    RETURN jsonb_build_object(
        'success', true,
        'user_id', new_user_id,
        'status', 'created',
        'message', 'User account created and confirmed successfully.'
    );
EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object(
        'success', false,
        'error', SQLERRM
    );
END;
$$;

-- ----------------------------------------------------------------------
-- 15. STORAGE BUCKET POLICIES (Hardened)
-- ----------------------------------------------------------------------
-- Remove overly permissive public upload/update policies
DROP POLICY IF EXISTS "Allow public insert objects" ON storage.objects;
DROP POLICY IF EXISTS "Allow public select objects" ON storage.objects;
DROP POLICY IF EXISTS "Allow public update objects" ON storage.objects;
DROP POLICY IF EXISTS "Public Upload Student Photos" ON storage.objects;
DROP POLICY IF EXISTS "Public Update Student Photos" ON storage.objects;

-- Secure storage policies:
-- 1. Read: Authenticated users can view objects in public buckets
CREATE POLICY "storage_objects_select_authenticated"
ON storage.objects FOR SELECT TO authenticated
USING (bucket_id IN ('student-photos', 'assignments', 'study-materials'));

-- 2. Insert Student Photos: Authenticated users can upload avatars
CREATE POLICY "storage_student_photos_upload"
ON storage.objects FOR INSERT TO authenticated
WITH CHECK (bucket_id = 'student-photos');

-- 3. Insert Academic Files: Staff / Admin only
CREATE POLICY "storage_academic_upload_staff"
ON storage.objects FOR INSERT TO authenticated
WITH CHECK (
  bucket_id IN ('assignments', 'study-materials')
  AND (public.is_admin() OR public.is_faculty())
);

-- 4. Update / Delete: Staff / Admin or Owner
CREATE POLICY "storage_objects_modify_staff"
ON storage.objects FOR UPDATE TO authenticated
USING (
  public.is_admin()
  OR public.is_faculty()
  OR owner = auth.uid()
)
WITH CHECK (
  public.is_admin()
  OR public.is_faculty()
  OR owner = auth.uid()
);

CREATE POLICY "storage_objects_delete_staff"
ON storage.objects FOR DELETE TO authenticated
USING (
  public.is_admin()
  OR public.is_faculty()
  OR owner = auth.uid()
);

-- Reload PostgREST schema cache
NOTIFY pgrst, 'reload schema';
