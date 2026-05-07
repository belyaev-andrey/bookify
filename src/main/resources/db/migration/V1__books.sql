create table book
(
    id        UUID primary key,
    name      varchar not null,
    isbn      varchar not null,
    available BOOLEAN DEFAULT TRUE
);