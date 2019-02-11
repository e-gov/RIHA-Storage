CREATE OR REPLACE FUNCTION riha.is_set(val jsonb)
  RETURNS BOOLEAN AS $$
BEGIN
  RETURN NOT (val ISNULL OR val :: text = 'null');
END $$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION riha.has_section(infosystem jsonb, section text)
  RETURNS BOOLEAN AS $$
BEGIN
  RETURN (infosystem ? section) AND riha.is_set(infosystem -> section)
         AND (jsonb_array_length(infosystem -> section) > 0);
END $$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION riha.is_broken(infosystem jsonb, section text)
  RETURNS BOOLEAN AS $$
DECLARE
  j_file jsonb;
BEGIN
  IF (riha.has_section(infosystem, section))
  THEN
    FOR j_file IN SELECT * FROM jsonb_array_elements(infosystem -> section)
    LOOP
      IF (j_file = 'null')
      THEN
        RETURN TRUE;
      ELSE
        RETURN FALSE;
      END IF;
    END LOOP;
  ELSE
    RETURN FALSE;
  END IF;
END $$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION riha.set_creation_timestamp(file_list jsonb, update_timestamp jsonb)
  RETURNS JSONB [] AS $$
DECLARE
  j_file        jsonb;
  updated_files jsonb [] := '{}';
BEGIN
  FOR j_file IN SELECT * FROM jsonb_array_elements(file_list)
  LOOP
    j_file := jsonb_set(j_file, '{creation_timestamp}', update_timestamp);
    updated_files := array_append(updated_files, j_file);
  END LOOP;

  RETURN updated_files;
END $$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION riha.copy_files(prev_file_list jsonb, current_file_list jsonb)
  RETURNS JSONB [] AS $$
DECLARE
  j_file        jsonb;
  updated_files jsonb [] := '{}';
BEGIN
  --take all docs from prev history entry
  FOR j_file IN SELECT * FROM jsonb_array_elements(prev_file_list)
  LOOP
    updated_files := array_append(updated_files, j_file);
  END LOOP;

  --add non null docs from current history entry
  FOR j_file IN SELECT * FROM jsonb_array_elements(current_file_list)
  LOOP
    IF (j_file != 'null')
    THEN
      updated_files := array_append(updated_files, j_file);
    END IF;
  END LOOP;

  RETURN updated_files;
END $$
LANGUAGE plpgsql;

DO $$
DECLARE
  infosystem_uuid       UUID;
  current_row           riha.main_resource%rowtype;
  prev_row              riha.main_resource%rowtype;
  updated_files         jsonb [] := '{}';
  update_timestamp      jsonb;
  updated_systems_count INTEGER := 0;
BEGIN
  FOR infosystem_uuid IN (SELECT DISTINCT json_content ->> 'uuid' as uuid
                          FROM riha.main_resource
                                WHERE (riha.has_section(json_content, 'documents') OR
                                       riha.has_section(json_content, 'data_files') OR
                                       riha.has_section(json_content, 'legislations')))
  LOOP
    FOR current_row IN (SELECT *
                        FROM riha.main_resource
                        WHERE json_content ->> 'uuid' = infosystem_uuid :: TEXT
                        ORDER BY main_resource_id ASC)
    LOOP
--       RAISE NOTICE 'Before update %', current_row.json_content;

      IF (prev_row ISNULL)
      --first info system history entry, for all files set creation_timestamp = info system creation timestamp
      THEN
        update_timestamp := current_row.json_content #> '{meta, update_timestamp}';

        IF (riha.has_section(current_row.json_content, 'documents'))
        THEN
          updated_files := riha.set_creation_timestamp(current_row.json_content -> 'documents', update_timestamp);
          current_row.json_content := jsonb_set(current_row.json_content, '{documents}',
                                                array_to_json(updated_files) :: jsonb);
        END IF;

        IF (riha.has_section(current_row.json_content, 'data_files'))
        THEN
          updated_files := riha.set_creation_timestamp(current_row.json_content -> 'data_files', update_timestamp);
          current_row.json_content := jsonb_set(current_row.json_content, '{data_files}',
                                                array_to_json(updated_files) :: jsonb);
        END IF;

        IF (riha.has_section(current_row.json_content, 'legislations'))
        THEN
          updated_files := riha.set_creation_timestamp(current_row.json_content -> 'legislations', update_timestamp);
          current_row.json_content := jsonb_set(current_row.json_content, '{legislations}',
                                                array_to_json(updated_files) :: jsonb);
        END IF;
      ELSE
        IF (riha.is_broken(current_row.json_content, 'documents'))
        THEN
          updated_files := riha.copy_files(prev_row.json_content -> 'documents',
                                           current_row.json_content -> 'documents');
          current_row.json_content := jsonb_set(current_row.json_content, '{documents}',
                                                array_to_json(updated_files) :: jsonb);
        END IF;
        IF (riha.is_broken(current_row.json_content, 'data_files'))
        THEN
          updated_files := riha.copy_files(prev_row.json_content -> 'data_files',
                                           current_row.json_content -> 'data_files');
          current_row.json_content := jsonb_set(current_row.json_content, '{data_files}',
                                                array_to_json(updated_files) :: jsonb);
        END IF;
        IF (riha.is_broken(current_row.json_content, 'legislations'))
        THEN
          updated_files := riha.copy_files(prev_row.json_content -> 'legislations',
                                           current_row.json_content -> 'legislations');
          current_row.json_content := jsonb_set(current_row.json_content, '{legislations}',
                                                array_to_json(updated_files) :: jsonb);
        END IF;
      END IF;

--       RAISE NOTICE 'After update %', current_row.json_content;
      prev_row := current_row;

      UPDATE riha.main_resource
      SET json_content = current_row.json_content
      WHERE main_resource_id = current_row.main_resource_id;

    END LOOP;

    prev_row := null;
    updated_systems_count := updated_systems_count + 1;
    RAISE NOTICE 'Processed %', infosystem_uuid;
  END LOOP;
  RAISE NOTICE 'Updated: % infosystems', updated_systems_count;
END $$;

DROP FUNCTION IF EXISTS riha.is_set(jsonb) CASCADE;
DROP FUNCTION IF EXISTS riha.has_section(jsonb, text) CASCADE;
DROP FUNCTION IF EXISTS riha.is_broken(jsonb, text) CASCADE;
DROP FUNCTION IF EXISTS riha.set_creation_timestamp(jsonb, jsonb) CASCADE;
DROP FUNCTION IF EXISTS riha.copy_files(jsonb, jsonb) CASCADE;