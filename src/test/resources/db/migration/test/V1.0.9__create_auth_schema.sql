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
