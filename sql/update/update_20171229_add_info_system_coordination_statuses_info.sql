-- Drop main_resource_view and views that depend on it
DROP VIEW IF EXISTS riha.main_resource_view CASCADE;

-- Create main_resource_view with last positive coordination statuses data columns
CREATE OR REPLACE VIEW riha.main_resource_view AS
  SELECT DISTINCT ON (json_content ->> 'uuid')
    main_resource.*,
    ((main_resource.json_content #>>
      '{meta,creation_timestamp}' :: TEXT [])) :: TIMESTAMP WITH TIME ZONE AS j_creation_timestamp,
    ((main_resource.json_content #>>
      '{meta,update_timestamp}' :: TEXT [])) :: TIMESTAMP WITH TIME ZONE   AS j_update_timestamp,
    last_positive_approval_request.sub_type                                AS last_positive_approval_request_type,
    last_positive_approval_request.modified_date                           AS last_positive_approval_request_date,
    last_positive_establishment_request.modified_date                      AS last_positive_establishment_request_date,
    last_positive_take_into_use_request.modified_date                      AS last_positive_take_into_use_request_date,
    last_positive_finalization_request.modified_date                       AS last_positive_finalization_request_date
  FROM riha.main_resource AS main_resource
    LEFT JOIN (SELECT DISTINCT ON (infosystem_uuid)
                 infosystem_uuid,
                 sub_type,
                 modified_date
               FROM riha.comment
               WHERE
                 type = 'ISSUE'
                 AND sub_type IN ('ESTABLISHMENT_REQUEST',
                                  'TAKE_INTO_USE_REQUEST',
                                  'FINALIZATION_REQUEST')
                 AND status = 'CLOSED'
                 AND resolution_type = 'POSITIVE'
               ORDER BY infosystem_uuid, modified_date DESC) AS last_positive_approval_request
      ON (json_content ->> 'uuid') :: UUID = last_positive_approval_request.infosystem_uuid
    LEFT JOIN (SELECT DISTINCT ON (infosystem_uuid)
                infosystem_uuid,
                modified_date
              FROM riha.comment
              WHERE
                type = 'ISSUE'
                AND sub_type = 'ESTABLISHMENT_REQUEST'
                AND status = 'CLOSED'
                AND resolution_type = 'POSITIVE'
              ORDER BY infosystem_uuid, modified_date DESC) AS last_positive_establishment_request
      ON (json_content ->> 'uuid') :: UUID = last_positive_establishment_request.infosystem_uuid
      LEFT JOIN (SELECT DISTINCT ON (infosystem_uuid)
                infosystem_uuid,
                modified_date
              FROM riha.comment
              WHERE
                type = 'ISSUE'
                AND sub_type = 'TAKE_INTO_USE_REQUEST'
                AND status = 'CLOSED'
                AND resolution_type = 'POSITIVE'
              ORDER BY infosystem_uuid, modified_date DESC) AS last_positive_take_into_use_request
      ON (json_content ->> 'uuid') :: UUID = last_positive_take_into_use_request.infosystem_uuid
      LEFT JOIN (SELECT DISTINCT ON (infosystem_uuid)
                infosystem_uuid,
                modified_date
              FROM riha.comment
              WHERE
                type = 'ISSUE'
                AND sub_type = 'FINALIZATION_REQUEST'
                AND status = 'CLOSED'
                AND resolution_type = 'POSITIVE'
              ORDER BY infosystem_uuid, modified_date DESC) AS last_positive_finalization_request
      ON (json_content ->> 'uuid') :: UUID = last_positive_finalization_request.infosystem_uuid
  ORDER BY json_content ->> 'uuid',
    j_update_timestamp DESC NULLS LAST,
    main_resource_id DESC;

-- Recreate views
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

DROP VIEW IF EXISTS riha.comment_type_issue_view;
CREATE OR REPLACE VIEW riha.comment_type_issue_view AS
  SELECT
    issue.*,
    infosystem.json_content ->> 'short_name' AS infosystem_short_name
  FROM riha.comment issue
    INNER JOIN riha.main_resource_view infosystem
      ON (infosystem.json_content ->> 'uuid') = issue.infosystem_uuid :: TEXT
  WHERE issue.type = 'ISSUE'
  ORDER BY issue.comment_id;