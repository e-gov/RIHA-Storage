DROP VIEW IF EXISTS riha.main_resource_view CASCADE;
CREATE OR REPLACE VIEW riha.main_resource_view AS
  SELECT DISTINCT ON (json_content ->> 'uuid')
    *,
    ((main_resource.json_content #>> '{meta,creation_timestamp}' :: TEXT [])) :: TIMESTAMP WITH TIME ZONE AS j_creation_timestamp,
    ((main_resource.json_content #>> '{meta,update_timestamp}' :: TEXT [])) :: TIMESTAMP   WITH TIME ZONE AS j_update_timestamp
  FROM riha.main_resource AS main_resource
  ORDER BY json_content ->> 'uuid',
    j_update_timestamp DESC NULLS LAST,
    main_resource_id DESC;

DROP VIEW IF EXISTS riha.main_resource_relation_view;
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


-- DROP VIEW riha.comment_type_issue_view;
CREATE OR REPLACE VIEW riha.comment_type_issue_view AS
  SELECT
    issue.*,
    infosystem.json_content ->> 'short_name' AS infosystem_short_name
  FROM riha.comment issue
    INNER JOIN riha.main_resource_view infosystem
      ON (infosystem.json_content ->> 'uuid') = issue.infosystem_uuid :: TEXT
  WHERE issue.type = 'ISSUE'
  ORDER BY issue.comment_id;