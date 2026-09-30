create table incomes (
    id          bigserial     primary key,
    property_id bigint        not null references properties (id) on delete cascade,
    date        date          not null,
    amount      numeric(10,2) not null check (amount >= 0),
    source      varchar(200)  not null,
    notes       varchar(1000),
    created_at  timestamptz   not null default now()
);

create index ix_incomes_property_date on incomes (property_id, date);

create table expenses (
    id          bigserial     primary key,
    property_id bigint        not null references properties (id) on delete cascade,
    date        date          not null,
    amount      numeric(10,2) not null check (amount >= 0),
    category    varchar(20)   not null check (category in ('cleaning', 'maintenance', 'commission', 'other')),
    notes       varchar(1000),
    created_at  timestamptz   not null default now()
);

create index ix_expenses_property_date on expenses (property_id, date);
