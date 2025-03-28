-- Creates the audio_files table to store metadata for audio files

CREATE TABLE audio_files (
    id SERIAL PRIMARY KEY,
    file_identifier TEXT NOT NULL,
    file_url TEXT NOT NULL,
    file_size BIGINT,
    checksum TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
