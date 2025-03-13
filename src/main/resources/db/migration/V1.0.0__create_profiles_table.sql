create table profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    username text unique not null,
    email text unique not null,
    user_type text check (user_type in ('artist', 'listener')) not null,
    created_at timestamp default now()
);
