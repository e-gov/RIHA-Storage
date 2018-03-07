-- registered_file table holds latest main_resource version and existing file_resource association
-- DROP TABLE IF EXISTS riha.registered_file;
CREATE TABLE riha.registered_file
(
  file_resource_uuid UUID,
  main_resource_uuid UUID,
  section            VARCHAR(150),
  CONSTRAINT registered_file_file_resource_uuid_fk FOREIGN KEY (file_resource_uuid) REFERENCES riha.file_resource (uuid) ON DELETE CASCADE
);
COMMENT ON COLUMN riha.registered_file.section
IS 'Main resource section that contains this file';
COMMENT ON TABLE riha.registered_file
IS 'File resources that appear in main_resource json description';

-- Updates main_resource.uuid and file_resource.uuid association table.
-- Created associations are checked against both latest version of main_resource and existence of file_resource. Resulting table should contain actual data only.
DROP FUNCTION IF EXISTS riha.recreate_main_resource_registered_files() CASCADE;
CREATE OR REPLACE FUNCTION riha.recreate_main_resource_registered_files(updated_infosystem_uuid UUID)
  RETURNS VOID AS $$
DECLARE
  file_resource_uuid UUID;
BEGIN
  IF updated_infosystem_uuid IS NOT NULL
  THEN

    DELETE FROM riha.registered_file
    WHERE main_resource_uuid = updated_infosystem_uuid;

    FOR file_resource_uuid IN (
      SELECT DISTINCT (substr(data_file ->> 'url', 8) :: UUID) AS document_uuid
      FROM jsonb_array_elements(
               (SELECT json_content -> 'data_files'
                FROM riha.main_resource_view
                WHERE
                  json_content ->> 'uuid' = updated_infosystem_uuid :: TEXT)) AS data_file
      WHERE data_file ->> 'url' ~* 'file://[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}')
    LOOP
      IF exists(SELECT 1
                FROM riha.file_resource fr
                WHERE fr.uuid = file_resource_uuid
                      AND fr.infosystem_uuid = updated_infosystem_uuid)
      THEN
        INSERT INTO riha.registered_file (file_resource_uuid, main_resource_uuid, section)
        VALUES (file_resource_uuid, updated_infosystem_uuid, 'DATA_FILES');
      END IF;
    END LOOP;

  END IF;
END $$
LANGUAGE plpgsql
VOLATILE;

-- Creates trigger function for updating registering files
DROP FUNCTION IF EXISTS riha.on_after_main_resource_insert_or_update() CASCADE;
CREATE OR REPLACE FUNCTION riha.on_after_main_resource_insert_or_update()
  RETURNS TRIGGER AS $$
BEGIN
  PERFORM riha.recreate_main_resource_registered_files((new.json_content ->> 'uuid') :: UUID);
  RETURN new;
END $$
LANGUAGE plpgsql
VOLATILE;

-- Creates trigger that updates registered_file table AFTER main_resource insert or update.
DROP TRIGGER IF EXISTS after_main_resource_insert_or_update
ON riha.main_resource;

CREATE TRIGGER after_main_resource_insert_or_update
  AFTER INSERT OR UPDATE
  ON riha.main_resource
  FOR EACH ROW EXECUTE PROCEDURE riha.on_after_main_resource_insert_or_update();

-- Populate registered_file table
SELECT riha.recreate_main_resource_registered_files((json_content ->> 'uuid') :: UUID)
FROM riha.main_resource_view;