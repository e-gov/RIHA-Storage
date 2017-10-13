CREATE OR REPLACE VIEW riha.main_resource_view AS
  SELECT DISTINCT ON (json_content ->> 'uuid') *,
    ((main_resource.json_content #>> '{meta,creation_timestamp}'::text[]))::timestamp AS j_creation_timestamp
  FROM riha.main_resource as main_resource
  ORDER BY json_content ->> 'uuid',
    creation_date DESC,
    main_resource_id DESC;