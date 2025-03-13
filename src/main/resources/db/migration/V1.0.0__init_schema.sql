-- Initial schema

CREATE TABLE artist_profiles (
    user_id UUID PRIMARY KEY,  -- References Supabase auth user ID
    biography TEXT,
    social_links JSONB,         -- Stores social links as JSON (can be expanded as needed)
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE fan_profiles (
    user_id UUID PRIMARY KEY,  -- References Supabase auth user ID
    subscription_active BOOLEAN NOT NULL DEFAULT FALSE,
    subscription_start_date TIMESTAMPTZ,  -- Start date of the rolling monthly subscription
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE releases (
    id SERIAL PRIMARY KEY,
    artist_id UUID NOT NULL,         -- Foreign key to artist_profiles.user_id
    title TEXT NOT NULL,
    release_date DATE NOT NULL,
    upc VARCHAR(50),                 -- Nullable official UPC
    created_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT fk_artist
        FOREIGN KEY (artist_id)
            REFERENCES artist_profiles(user_id)
            ON DELETE CASCADE
);

CREATE TABLE tracks (
    id SERIAL PRIMARY KEY,
    release_id INTEGER NOT NULL,     -- Foreign key to releases.id
    title TEXT NOT NULL,
    duration INTEGER NOT NULL,       -- Duration in seconds
    isrc VARCHAR(50),                -- Nullable official ISRC
    file_path TEXT NOT NULL,         -- Reference to the audio file in Backblaze B2
    file_size INTEGER,               -- Optional file size (e.g., in bytes)
    created_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT fk_release
        FOREIGN KEY (release_id)
            REFERENCES releases(id)
            ON DELETE CASCADE
);