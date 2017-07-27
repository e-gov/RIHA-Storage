-- v_main_resource provides a view on latest info system versions from main_resource
-- provided rows are with unique uuid, latest creation_date and, in case of ties, greatest entity id.

CREATE INDEX main_resource_uuid
  ON riha.main_resource (((json_content ->> 'uuid')));

CREATE VIEW riha.main_resource_view AS
  SELECT DISTINCT ON (json_content ->> 'uuid') *
  FROM riha.main_resource
  ORDER BY json_content ->> 'uuid',
    creation_date DESC,
    main_resource_id DESC;