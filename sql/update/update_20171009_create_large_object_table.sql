CREATE SEQUENCE riha.large_object_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;
GRANT SELECT, USAGE ON SEQUENCE riha.large_object_seq TO riha;

-- Table: riha.large_object

-- DROP TABLE riha.large_object;

CREATE TABLE riha.large_object
(
    id integer NOT NULL,
    creation_date timestamp without time zone,
    data oid,
    hash character varying(255),
    CONSTRAINT large_object_pkey PRIMARY KEY (id)
)
WITH (
OIDS = FALSE
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE riha.large_object TO riha;

-- Table: riha.file_resource

-- DROP TABLE riha.file_resource;

CREATE TABLE riha.file_resource
(
    uuid uuid NOT NULL,
    content_type character varying(255),
    creation_date timestamp without time zone,
    name character varying(255),
    large_object_id integer NOT NULL,
    CONSTRAINT file_resource_pkey PRIMARY KEY (uuid),
    CONSTRAINT fk_file_resource_large_object FOREIGN KEY (large_object_id)
    REFERENCES riha.large_object (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
)
WITH (
OIDS = FALSE
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE riha.file_resource TO riha;
