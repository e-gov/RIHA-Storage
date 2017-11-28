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
