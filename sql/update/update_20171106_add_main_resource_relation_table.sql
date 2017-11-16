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

-- DROP VIEW riha.main_resource_relation_view;
CREATE OR REPLACE VIEW riha.main_resource_relation_view AS
  SELECT
    mrr.*,
    infosystem.json_content ->> 'short_name'         AS infosystem_short_name,
    infosystem.json_content ->> 'name'               AS infosystem_name,
    related_infosystem.json_content ->> 'short_name' AS related_infosystem_short_name,
    related_infosystem.json_content ->> 'name'       AS related_infosystem_name
  FROM riha.main_resource_relation mrr
    LEFT JOIN riha.main_resource_view infosystem ON (infosystem.json_content ->> 'uuid') = mrr.infosystem_uuid :: TEXT
    LEFT JOIN riha.main_resource_view related_infosystem
ON (related_infosystem.json_content ->> 'uuid') = mrr.related_infosystem_uuid :: TEXT;