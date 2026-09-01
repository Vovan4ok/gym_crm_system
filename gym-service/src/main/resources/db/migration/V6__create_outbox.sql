create table outbox_messages (
    id             uuid primary key,
    destination    varchar(255) not null,
    payload        text         not null,
    correlation_id varchar(64),
    group_id       varchar(255),
    status         varchar(20)  not null default 'PENDING',
    attempts       int          not null default 0,
    created_at     timestamp    not null default now(),
    sent_at        timestamp
);
create index idx_outbox_pending on outbox_messages (status, created_at);