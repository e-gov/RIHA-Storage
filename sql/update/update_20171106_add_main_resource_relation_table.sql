CREATE SEQUENCE riha.main_resource_relation_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
GRANT SELECT, USAGE ON SEQUENCE riha.main_resource_relation_seq TO riha;

CREATE TABLE riha.main_resource_relation
(
  main_resource_relation_id INTEGER NOT NULL
    CONSTRAINT main_resource_relation_pkey
    PRIMARY KEY,
  creation_date             TIMESTAMP,
  infosystem_uuid           UUID,
  modified_date             TIMESTAMP,
  related_infosystem_uuid   UUID,
  type                      VARCHAR(255)
);
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE riha.main_resource_relation TO riha;

ALTER TABLE riha.main_resource_relation
  ADD CONSTRAINT main_resource_relation_unique UNIQUE (infosystem_uuid, related_infosystem_uuid, type);

-- DROP INDEX riha.idx_main_resource_relation_infosystem_uuid;
CREATE INDEX idx_main_resource_relation_infosystem_uuid
  ON riha.main_resource_relation (infosystem_uuid);

-- DROP INDEX riha.idx_main_resource_relation_related_infosystem_uuid;
CREATE INDEX idx_main_resource_relation_related_infosystem_uuid
  ON riha.main_resource_relation (related_infosystem_uuid);