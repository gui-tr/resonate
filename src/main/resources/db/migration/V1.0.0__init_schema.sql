-- V1.0.0__init_schema.sql

-- Create table for artist profiles with an auto-generated primary key "id"
CREATE TABLE IF NOT EXISTS artist_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    biography TEXT
);

-- Create table for fan profiles with an auto-generated primary key "id"
CREATE TABLE IF NOT EXISTS fan_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    subscription_active BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create table for audio files with an auto-generated primary key "id"
CREATE TABLE IF NOT EXISTS audio_files (
    id BIGSERIAL PRIMARY KEY,
    file_identifier VARCHAR(255),
    file_url TEXT,
    file_size BIGINT,
    checksum VARCHAR(255),
    file_path TEXT
);

-- Create table for releases with an auto-generated primary key "id"
CREATE TABLE IF NOT EXISTS releases (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255),
    release_date DATE,
    upc VARCHAR(255),
    artist_id UUID NOT NULL
);

-- Create table for tracks with an auto-generated primary key "id"
CREATE TABLE IF NOT EXISTS tracks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255),
    duration INTEGER,
    release_id BIGINT REFERENCES releases(id)
);
