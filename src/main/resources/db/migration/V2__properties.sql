create table properties (
    id            bigserial    primary key,
    owner_id      bigint       not null references users (id) on delete cascade,
    name          varchar(200) not null,
    address       varchar(500) not null,
    property_type varchar(50)  not null default 'apartment',
    currency      varchar(3)   not null default 'USD',
    created_at    timestamptz  not null default now()
);

create index ix_properties_owner_id on properties (owner_id);
