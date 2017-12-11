-- optional session setting, can be skipped (though highly recommended to use to avoid any potential
-- time zone issues)
SET TIME ZONE 'Europe/Tallinn';

-- views must be dropped first as they fields types depend on parental tables
DROP VIEW IF EXISTS riha.main_resource_view CASCADE;
DROP VIEW IF EXISTS riha.main_resource_relation_view;
DROP VIEW IF EXISTS riha.comment_type_issue_view;

-- alter following fields types from 'TIMESTAMP WITHOUT TIME ZONE' to 'TIMESTAMP WITH TIME ZONE'
-- and keep current date values according to local time zone (Europe/Tallinn)
ALTER TABLE riha.comment ALTER COLUMN creation_date TYPE TIMESTAMP WITH TIME ZONE
  USING creation_date AT TIME ZONE 'Europe/Tallinn';
ALTER TABLE riha.comment ALTER COLUMN modified_date TYPE TIMESTAMP WITH TIME ZONE
  USING modified_date AT TIME ZONE 'Europe/Tallinn';

ALTER TABLE riha.data_object ALTER COLUMN creation_date TYPE TIMESTAMP WITH TIME ZONE
  USING creation_date AT TIME ZONE 'Europe/Tallinn';
ALTER TABLE riha.data_object ALTER COLUMN modified_date TYPE TIMESTAMP WITH TIME ZONE
  USING modified_date AT TIME ZONE 'Europe/Tallinn';
ALTER TABLE riha.data_object ALTER COLUMN start_date TYPE TIMESTAMP WITH TIME ZONE
  USING start_date AT TIME ZONE 'Europe/Tallinn';
ALTER TABLE riha.data_object ALTER COLUMN end_date TYPE TIMESTAMP WITH TIME ZONE
  USING end_date AT TIME ZONE 'Europe/Tallinn';

ALTER TABLE riha.document ALTER COLUMN creation_date TYPE TIMESTAMP WITH TIME ZONE
  USING creation_date AT TIME ZONE 'Europe/Tallinn';
ALTER TABLE riha.document ALTER COLUMN modified_date TYPE TIMESTAMP WITH TIME ZONE
  USING modified_date AT TIME ZONE 'Europe/Tallinn';
ALTER TABLE riha.document ALTER COLUMN start_date TYPE TIMESTAMP WITH TIME ZONE
  USING start_date AT TIME ZONE 'Europe/Tallinn';
ALTER TABLE riha.document ALTER COLUMN end_date TYPE TIMESTAMP WITH TIME ZONE
  USING end_date AT TIME ZONE 'Europe/Tallinn';

ALTER TABLE riha.file_resource ALTER COLUMN creation_date TYPE TIMESTAMP WITH TIME ZONE
  USING creation_date AT TIME ZONE 'Europe/Tallinn';

ALTER TABLE riha.kind ALTER COLUMN creation_date TYPE TIMESTAMP WITH TIME ZONE
  USING creation_date AT TIME ZONE 'Europe/Tallinn';
ALTER TABLE riha.kind ALTER COLUMN modified_date TYPE TIMESTAMP WITH TIME ZONE
  USING modified_date AT TIME ZONE 'Europe/Tallinn';
ALTER TABLE riha.kind ALTER COLUMN start_date TYPE TIMESTAMP WITH TIME ZONE
  USING start_date AT TIME ZONE 'Europe/Tallinn';
ALTER TABLE riha.kind ALTER COLUMN end_date TYPE TIMESTAMP WITH TIME ZONE
  USING end_date AT TIME ZONE 'Europe/Tallinn';

ALTER TABLE riha.large_object ALTER COLUMN creation_date TYPE TIMESTAMP WITH TIME ZONE
  USING creation_date AT TIME ZONE 'Europe/Tallinn';

ALTER TABLE riha.main_resource ALTER COLUMN creation_date TYPE TIMESTAMP WITH TIME ZONE
  USING creation_date AT TIME ZONE 'Europe/Tallinn';
ALTER TABLE riha.main_resource ALTER COLUMN modified_date TYPE TIMESTAMP WITH TIME ZONE
  USING modified_date AT TIME ZONE 'Europe/Tallinn';
ALTER TABLE riha.main_resource ALTER COLUMN start_date TYPE TIMESTAMP WITH TIME ZONE
  USING start_date AT TIME ZONE 'Europe/Tallinn';
ALTER TABLE riha.main_resource ALTER COLUMN end_date TYPE TIMESTAMP WITH TIME ZONE
  USING end_date AT TIME ZONE 'Europe/Tallinn';

ALTER TABLE riha.main_resource_relation ALTER COLUMN creation_date TYPE TIMESTAMP WITH TIME ZONE
  USING creation_date AT TIME ZONE 'Europe/Tallinn';
ALTER TABLE riha.main_resource_relation ALTER COLUMN modified_date TYPE TIMESTAMP WITH TIME ZONE
  USING modified_date AT TIME ZONE 'Europe/Tallinn';

-- Recreate all related views
CREATE OR REPLACE VIEW riha.main_resource_view AS
  SELECT DISTINCT ON (json_content ->> 'uuid')
    *,
    ((main_resource.json_content #>> '{meta,creation_timestamp}' :: TEXT [])) :: TIMESTAMP WITH TIME ZONE AS j_creation_timestamp,
    ((main_resource.json_content #>> '{meta,update_timestamp}' :: TEXT [])) :: TIMESTAMP   WITH TIME ZONE AS j_update_timestamp
  FROM riha.main_resource AS main_resource
  ORDER BY json_content ->> 'uuid',
    j_update_timestamp DESC NULLS LAST,
    main_resource_id DESC;

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

CREATE OR REPLACE VIEW riha.comment_type_issue_view AS
  SELECT
    issue.*,
    infosystem.json_content ->> 'short_name' AS infosystem_short_name
  FROM riha.comment issue
    INNER JOIN riha.main_resource_view infosystem
      ON (infosystem.json_content ->> 'uuid') = issue.infosystem_uuid :: TEXT
  WHERE issue.type = 'ISSUE'
  ORDER BY issue.comment_id;
