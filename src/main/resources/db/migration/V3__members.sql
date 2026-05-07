create table member
(
    id       UUID primary key,
    name     varchar not null,
    email    varchar not null,
    password varchar not null,
    enabled  boolean default true
);