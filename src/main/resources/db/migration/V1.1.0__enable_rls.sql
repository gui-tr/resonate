-- Enable Row-Level Security (RLS) and define policies for our tables

-- For artist_profiles table
ALTER TABLE artist_profiles ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Artist can manage their own profiles" ON artist_profiles
    FOR ALL
    USING (user_id::text = auth.uid()::text);

-- For fan_profiles table
ALTER TABLE fan_profiles ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Fan can manage their own profiles" ON fan_profiles
    FOR ALL
    USING (user_id::text = auth.uid()::text);

-- For releases table
ALTER TABLE releases ENABLE ROW LEVEL SECURITY;
-- Allow public reading
CREATE POLICY "Public read releases" ON releases
    FOR SELECT
    USING (true);
-- Allow modifications only for the owning artist
CREATE POLICY "Artist can modify their own release" ON releases
    FOR ALL
    USING (artist_id::text = auth.uid()::text);

-- For tracks table
ALTER TABLE tracks ENABLE ROW LEVEL SECURITY;
-- Allow public reading
CREATE POLICY "Public read tracks" ON tracks
    FOR SELECT
    USING (true);
-- Allow modifications only if the authenticated artist owns the related release
CREATE POLICY "Artist can modify tracks in their own release" ON tracks
    FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM releases r
            WHERE r.id = tracks.release_id
              AND r.artist_id::text = auth.uid()::text
        )
    );
