-- ==========================================================
-- SMART COLLEGE - SUPABASE AUTHENTICATION FIX & USER CREATION
-- Project ID: easrebumarwkloibtemf
-- ==========================================================

-- 1. Enable pgcrypto extension for password encryption
CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA extensions;

-- 2. Create RPC Function to create confirmed Auth Users directly
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
BEGIN
    -- Check if user already exists in auth.users
    SELECT id INTO existing_user_id FROM auth.users WHERE LOWER(email) = clean_email;
    
    IF existing_user_id IS NOT NULL THEN
        -- User exists: update password & ensure email is confirmed
        encrypted_pw := extensions.crypt(new_password, extensions.gen_salt('bf'));
        
        UPDATE auth.users
        SET 
            encrypted_password = encrypted_pw,
            email_confirmed_at = COALESCE(email_confirmed_at, NOW()),
            raw_user_meta_data = jsonb_build_object('role', user_role, 'name', full_name),
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
        IF user_role = 'student' THEN
            UPDATE public.students SET auth_user_id = existing_user_id::text WHERE LOWER(email) = clean_email;
        ELSIF user_role = 'professor' THEN
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
        jsonb_build_object('role', user_role, 'name', full_name),
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
    IF user_role = 'student' THEN
        UPDATE public.students SET auth_user_id = new_user_id::text WHERE LOWER(email) = clean_email;
    ELSIF user_role = 'professor' THEN
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

-- 3. Grant execute permissions to anon and authenticated roles
GRANT EXECUTE ON FUNCTION public.create_user_account(TEXT, TEXT, TEXT, TEXT) TO anon;
GRANT EXECUTE ON FUNCTION public.create_user_account(TEXT, TEXT, TEXT, TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION public.create_user_account(TEXT, TEXT, TEXT, TEXT) TO service_role;

-- 4. CREATE / ACTIVATE ANKUSH'S ACCOUNT IMMEDIATELY
-- Password is set to '123456'. (You can change it to whatever password you want)
SELECT public.create_user_account(
    'ankush251312@recmainpuri.in',
    '123456',
    'student',
    'ankush'
);
