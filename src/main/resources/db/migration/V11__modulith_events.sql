create table event_publication
(
    id                     uuid                        not null
        primary key,
    completion_attempts    integer                     not null,
    completion_date        timestamp(6) with time zone,
    event_type             varchar(255)                not null,
    last_resubmission_date timestamp(6) with time zone,
    listener_id            varchar(255)                not null,
    publication_date       timestamp(6) with time zone not null,
    serialized_event       varchar(255)                not null,
    status                 varchar(255)
        constraint event_publication_status_check
            check ((status)::text = ANY
                   ((ARRAY ['PUBLISHED'::character varying, 'PROCESSING'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying, 'RESUBMITTED'::character varying])::text[]))
);

CREATE TABLE event_publication_archive
(
    id                     UUID                           NOT NULL,
    publication_date       TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    listener_id            VARCHAR                        NOT NULL,
    serialized_event       VARCHAR                        NOT NULL,
    event_type             VARCHAR                        NOT NULL,
    completion_date        TIMESTAMP(6) WITHOUT TIME ZONE,
    last_resubmission_date TIMESTAMP(6) WITHOUT TIME ZONE,
    completion_attempts    INTEGER                        NOT NULL,
    status                 VARCHAR(255),
    CONSTRAINT pk_event_publication_archive PRIMARY KEY (id)
);
