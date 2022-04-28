DROP FUNCTION IF EXISTS main_resource_search_content();
CREATE OR REPLACE FUNCTION main_resource_search_content(json_content JSONB)
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
DROP FUNCTION IF EXISTS on_before_main_resource_insert_or_update() CASCADE;
CREATE OR REPLACE FUNCTION on_before_main_resource_insert_or_update()
  RETURNS TRIGGER AS $$
BEGIN
  new.search_content = main_resource_search_content(new.json_content);
  RETURN new;
END
$$ LANGUAGE plpgsql
IMMUTABLE;

DROP TRIGGER IF EXISTS before_main_resource_insert_or_update
ON main_resource;
CREATE TRIGGER before_main_resource_insert_or_update
  BEFORE INSERT OR UPDATE
  ON main_resource
  FOR EACH ROW EXECUTE PROCEDURE on_before_main_resource_insert_or_update();


-- Updates main_resource.uuid and file_resource.uuid association table.
-- Created associations are checked against both latest version of main_resource and existence of file_resource. Resulting table should contain actual data only.
DROP FUNCTION IF EXISTS recreate_main_resource_registered_files() CASCADE;
CREATE OR REPLACE FUNCTION recreate_main_resource_registered_files(updated_infosystem_uuid UUID)
  RETURNS VOID AS $$
DECLARE
  file_resource_uuid UUID;
BEGIN
  IF updated_infosystem_uuid IS NOT NULL
  THEN

    DELETE FROM registered_file
    WHERE main_resource_uuid = updated_infosystem_uuid;

    FOR file_resource_uuid IN (
      SELECT DISTINCT (substr(data_file ->> 'url', 8) :: UUID) AS document_uuid
      FROM jsonb_array_elements(
               (SELECT json_content -> 'data_files'
                FROM main_resource_view
                WHERE
                  json_content ->> 'uuid' = updated_infosystem_uuid :: TEXT AND json_content #> '{data_files,0}' IS NOT NULL)) AS data_file
      WHERE data_file ->> 'url' ~* 'file://[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}')
    LOOP
      IF exists(SELECT 1
                FROM file_resource fr
                WHERE fr.uuid = file_resource_uuid
                      AND fr.infosystem_uuid = updated_infosystem_uuid)
      THEN
        INSERT INTO registered_file (file_resource_uuid, main_resource_uuid, section)
        VALUES (file_resource_uuid, updated_infosystem_uuid, 'DATA_FILES');
      END IF;
    END LOOP;

  END IF;
END $$
LANGUAGE plpgsql
VOLATILE;

-- Creates trigger function for updating registering files
DROP FUNCTION IF EXISTS on_after_main_resource_insert_or_update() CASCADE;
CREATE OR REPLACE FUNCTION on_after_main_resource_insert_or_update()
  RETURNS TRIGGER AS $$
BEGIN
  PERFORM recreate_main_resource_registered_files((new.json_content ->> 'uuid') :: UUID);
  RETURN new;
END $$
LANGUAGE plpgsql
VOLATILE;

-- Creates trigger that updates registered_file table AFTER main_resource insert or update.
DROP TRIGGER IF EXISTS after_main_resource_insert_or_update
ON main_resource;

CREATE TRIGGER after_main_resource_insert_or_update
  AFTER INSERT OR UPDATE
  ON main_resource
  FOR EACH ROW EXECUTE PROCEDURE on_after_main_resource_insert_or_update();
