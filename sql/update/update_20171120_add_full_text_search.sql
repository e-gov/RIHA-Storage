-- enable trigram extension
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- add full text search column
ALTER TABLE riha.main_resource
  ADD COLUMN search_content TEXT;

-- recreate views
DROP VIEW IF EXISTS riha.main_resource_view CASCADE;
CREATE OR REPLACE VIEW riha.main_resource_view AS
  SELECT DISTINCT ON (json_content ->> 'uuid')
    *,
    ((main_resource.json_content #>> '{meta,creation_timestamp}' :: TEXT [])) :: TIMESTAMP AS j_creation_timestamp,
    ((main_resource.json_content #>> '{meta,update_timestamp}' :: TEXT [])) :: TIMESTAMP   AS j_update_timestamp
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

-- create function that picks data from json
DROP FUNCTION IF EXISTS riha.main_resource_search_content();
CREATE OR REPLACE FUNCTION riha.main_resource_search_content(json_content JSONB)
  RETURNS TEXT AS $$
SELECT concat_ws(' ',
                 json_content #>> '{name}',
                 json_content #>> '{short_name}',
                 json_content #>> '{owner,code}',
                 json_content #>> '{owner,name}',
                 json_content #>> '{purpose}',
                 json_content #>> '{homepage}',

                 (SELECT string_agg(value, ' ')
                  FROM jsonb_array_elements_text(nullif(jsonb_extract_path(json_content, 'topics'), 'null'))),

                 (SELECT string_agg(value, ' ')
                  FROM jsonb_array_elements_text(nullif(jsonb_extract_path(json_content, 'stored_data'), 'null'))),

                 (SELECT string_agg(value ->> 'name', ' ')
                  FROM jsonb_array_elements(nullif(jsonb_extract_path(json_content, 'data_files'), 'null'))),

                 (SELECT string_agg(value ->> 'url', ' ')
                  FROM jsonb_array_elements(nullif(jsonb_extract_path(json_content, 'data_files'), 'null'))
                  WHERE value ->> 'url' NOT ILIKE 'file://%'),

                 (SELECT string_agg(value ->> 'name', ' ')
                  FROM jsonb_array_elements(nullif(jsonb_extract_path(json_content, 'documents'), 'null'))),

                 (SELECT string_agg(value ->> 'url', ' ')
                  FROM jsonb_array_elements(nullif(jsonb_extract_path(json_content, 'documents'), 'null'))
                  WHERE value ->> 'url' NOT ILIKE 'file://%'),

                 (SELECT string_agg(value ->> 'name', ' ')
                  FROM jsonb_array_elements(nullif(jsonb_extract_path(json_content, 'legislations'), 'null'))),

                 (SELECT string_agg(value ->> 'url', ' ')
                  FROM jsonb_array_elements(nullif(jsonb_extract_path(json_content, 'legislations'), 'null')))
);
$$ LANGUAGE SQL
IMMUTABLE;

-- add trigger to update full text search column data
DROP FUNCTION IF EXISTS riha.update_main_resource_search_content() CASCADE;
CREATE OR REPLACE FUNCTION riha.update_main_resource_search_content()
  RETURNS TRIGGER AS $$
BEGIN
  new.search_content = riha.main_resource_search_content(new.json_content);
  RETURN new;
END
$$ LANGUAGE plpgsql
IMMUTABLE;

DROP TRIGGER IF EXISTS update_search_content
ON riha.main_resource;
CREATE TRIGGER update_search_content
  BEFORE INSERT OR UPDATE
  ON riha.main_resource
  FOR EACH ROW EXECUTE PROCEDURE riha.update_main_resource_search_content();

-- update table
UPDATE riha.main_resource
SET search_content = riha.main_resource_search_content(json_content);

-- add index on full text search column
DROP INDEX IF EXISTS idx_main_resource_search;
CREATE INDEX idx_main_resource_search
  ON riha.main_resource
  USING GIN (search_content gin_trgm_ops);