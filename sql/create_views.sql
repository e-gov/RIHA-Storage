CREATE VIEW riha.main_resource_view AS
  SELECT DISTINCT ON (json_content ->> 'uuid') *
  FROM riha.main_resource
  ORDER BY json_content ->> 'uuid',
    creation_date DESC,
    main_resource_id DESC;