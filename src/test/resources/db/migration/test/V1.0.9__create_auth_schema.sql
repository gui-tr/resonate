-- Create auth schema and necessary tables for tests
CREATE SCHEMA IF NOT EXISTS auth;

-- Create a simple users table in auth schema to satisfy RLS policies
CREATE TABLE IF NOT EXISTS auth.users (
    id uuid PRIMARY KEY,
    email text,
    created_at timestamp with time zone DEFAULT now()
);

-- Create the auth.uid() function that Supabase uses in RLS policies
CREATE OR REPLACE FUNCTION auth.uid()
RETURNS uuid
LANGUAGE sql
STABLE
AS $$
  SELECT (current_setting('request.jwt.claims', true)::json->>'sub')::uuid;
$$;

  SELECT current_setting('request.jwt.claims', true)::json->>'sub'::uuid;
$$;

-- Create test artist profile to satisfy foreign key constraints
INSERT INTO artist_profiles (user_id, biography, social_links, created_at)
VALUES (
    '00000000-0000-0000-0000-000000000000',
    'Test Artist Biography',
    '{"twitter":"@testartist"}',
    NOW()
)
ON CONFLICT (user_id) DO NOTHING;

-- Create test fan profile to satisfy foreign key constraints
INSERT INTO fan_profiles (user_id, subscription_active, subscription_start_date, created_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    true,
    NOW(),
    NOW()
)
ON CONFLICT (user_id) DO NOTHING; 