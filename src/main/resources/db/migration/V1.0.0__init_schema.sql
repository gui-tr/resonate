-- Initial schema

CREATE TABLE artist_profiles (
    user_id UUID PRIMARY KEY,
    biography TEXT,
    social_links TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE fan_profiles (
    user_id UUID PRIMARY KEY,
    subscription_active BOOLEAN NOT NULL DEFAULT FALSE,
    subscription_start_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE releases (
    id SERIAL PRIMARY KEY,
    artist_id UUID NOT NULL,
    title TEXT NOT NULL,
    release_date DATE NOT NULL,
    upc VARCHAR(50),
    created_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT fk_artist
        FOREIGN KEY (artist_id)
            REFERENCES artist_profiles(user_id)
            ON DELETE CASCADE
);

CREATE TABLE tracks (
    id SERIAL PRIMARY KEY,
    release_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    duration INTEGER NOT NULL,
    isrc VARCHAR(50),
    file_path TEXT NOT NULL,
    file_size INTEGER,
    created_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT fk_release
        FOREIGN KEY (release_id)
            REFERENCES releases(id)
            ON DELETE CASCADE
);