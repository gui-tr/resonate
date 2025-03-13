create table subscriptions (
    id uuid primary key default gen_random_uuid(),
    user_id uuid references profiles(id) on delete cascade,
    status text check (status in ('active', 'canceled', 'expired')) not null,
    plan text check (plan in ('monthly', 'yearly')) not null,
    start_date timestamp default now(),
    end_date timestamp
);
