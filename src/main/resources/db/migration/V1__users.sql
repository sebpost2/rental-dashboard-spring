create table users (
    id              bigserial    primary key,
    email           varchar(320) not null unique,
    hashed_password varchar(100) not null,
    created_at      timestamptz  not null default now()
);
