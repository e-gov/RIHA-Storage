--DROP VIEW riha.comment_type_issue_view;
CREATE OR REPLACE VIEW riha.comment_type_issue_view AS
  SELECT
    issue.*,
    infosystem.json_content ->> 'short_name' infosystem_short_name,
    infosystem.json_content ->> 'name' infosystem_full_name,
    array_to_json(array_agg(event ORDER BY event.comment_id) FILTER (WHERE event.type = 'ISSUE_EVENT')) events,
    row_to_json(last_comment) last_comment
  FROM riha.comment issue
    INNER JOIN riha.main_resource_view infosystem
      ON (infosystem.json_content ->> 'uuid') = issue.infosystem_uuid :: TEXT
    LEFT JOIN riha.comment event
      ON issue.comment_id = event.comment_parent_id
    LEFT JOIN (SELECT DISTINCT ON (comment_parent_id)
        comment_id,
        comment_parent_id,
        creation_date,
        author_name,
        author_personal_code,
        organization_name,
        organization_code
      FROM riha.comment
      WHERE type = 'ISSUE_COMMENT'
      ORDER BY comment_parent_id, creation_date DESC) last_comment
    ON issue.comment_id = last_comment.comment_parent_id
  WHERE issue.type = 'ISSUE'
  GROUP BY issue.comment_id, infosystem_short_name, infosystem_full_name, last_comment.*
  ORDER BY issue.comment_id;