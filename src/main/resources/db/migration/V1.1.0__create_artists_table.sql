create table artists (
    id uuid primary key references profiles(id) on delete cascade,
    display_name text not null,
    bio text,
    profile_image_url text
);
