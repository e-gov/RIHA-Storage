ALTER TABLE riha.comment ADD resolution_type VARCHAR(255) NULL;

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