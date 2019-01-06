CREATE OR REPLACE FUNCTION riha.set_creation_and_update_time_for_infosystem(infosystem_uuid UUID)
  RETURNS VOID AS $$
DECLARE
  prev_row     riha.main_resource%rowtype;
  current_row  riha.main_resource%rowtype;

  updated_docs jsonb;
  is_row_updated boolean;
BEGIN
  FOR current_row IN (SELECT *
                      FROM riha.main_resource as main_resource
                      WHERE json_content ->> 'uuid' = infosystem_uuid :: TEXT
                      ORDER BY main_resource_id ASC)
  LOOP
    is_row_updated := false;

    IF NOT prev_row ISNULL
    THEN
      IF (current_row.json_content ? 'documents') AND (jsonb_array_length(current_row.json_content -> 'documents') > 0)
      THEN
        updated_docs := riha.set_creation_and_update_time_for_docs(prev_row.json_content -> 'documents',
                                                                   current_row.json_content -> 'documents',
                                                                   current_row.json_content #>
                                                                   '{meta, update_timestamp}');
        current_row.json_content := jsonb_set(current_row.json_content, '{documents}', updated_docs);
        is_row_updated := true;
      END IF;

      IF (current_row.json_content ? 'data_files') AND (jsonb_array_length(current_row.json_content -> 'data_files') > 0)
      THEN
        updated_docs := riha.set_creation_and_update_time_for_docs(prev_row.json_content -> 'data_files',
                                                                   current_row.json_content -> 'data_files',
                                                                   current_row.json_content #>
                                                                   '{meta, update_timestamp}');
        current_row.json_content := jsonb_set(current_row.json_content, '{data_files}', updated_docs);
        is_row_updated := true;
      END IF;

      IF (current_row.json_content ? 'legislations') AND (jsonb_array_length(current_row.json_content -> 'legislations') > 0)
      THEN
        updated_docs := riha.set_creation_and_update_time_for_docs(prev_row.json_content -> 'legislations',
                                                                   current_row.json_content -> 'legislations',
                                                                   current_row.json_content #>
                                                                   '{meta, update_timestamp}');
        current_row.json_content := jsonb_set(current_row.json_content, '{legislations}', updated_docs);
        is_row_updated := true;
      END IF;
    END IF;

    IF is_row_updated
      THEN
        UPDATE riha.main_resource
        SET json_content = current_row.json_content
        WHERE main_resource_id = current_row.main_resource_id;
    END IF;

    prev_row := current_row;
  END LOOP;
END $$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION riha.set_creation_and_update_time_for_docs(old_list         jsonb, new_list jsonb,
                                                                      update_timestamp jsonb)
  RETURNS JSONB AS $$
DECLARE
  j_new_doc    jsonb;
  j_old_doc    jsonb;
  is_new_doc   boolean;

  updated_docs jsonb [] := '{}';
BEGIN
--   RAISE NOTICE 'Before  update %', new_list;
  FOR j_new_doc IN SELECT * FROM jsonb_array_elements(new_list)
  LOOP
    is_new_doc := true;
    FOR j_old_doc IN SELECT * FROM jsonb_array_elements(old_list)
    LOOP
      IF (
        ((j_new_doc ->> 'url') = (j_old_doc ->> 'url')) OR
        ((j_new_doc ->> 'name') = (j_old_doc ->> 'name'))
      )
      THEN
        -- prev ver of same doc
        is_new_doc := false;
        IF (
          ((j_new_doc ->> 'name') <> (j_old_doc ->> 'name')) OR
          ((j_new_doc ->> 'url') <> (j_old_doc ->> 'url')) OR
          is_access_restriction_changed(j_new_doc -> 'accessRestriction', j_old_doc -> 'accessRestriction')
        )
        THEN
          -- was updated
          j_new_doc := jsonb_set(j_new_doc, '{update_timestamp}', update_timestamp);
        ELSE
          -- was not updated
          IF (j_old_doc ? 'update_timestamp')
          THEN
            j_new_doc := jsonb_set(j_new_doc, '{update_timestamp}', j_old_doc -> 'update_timestamp');
          END IF;
        END IF;
        EXIT;
      END IF;
    END LOOP;

    IF (is_new_doc)
    THEN
      j_new_doc := jsonb_set(j_new_doc, '{creation_timestamp}', update_timestamp);
    ELSE
      j_new_doc := jsonb_set(j_new_doc, '{creation_timestamp}', j_old_doc -> 'creation_timestamp');
    END IF;

    updated_docs := array_append(updated_docs, j_new_doc);
  END LOOP;

--   RAISE NOTICE 'After update %', array_to_json(updated_docs);
  RETURN array_to_json(updated_docs);
END $$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION riha.is_set(val jsonb)
  RETURNS BOOLEAN AS $$
BEGIN
  RETURN NOT (val ISNULL OR val :: text = 'null');
END $$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION riha.is_access_restriction_changed(old_v jsonb, new_v jsonb)
  RETURNS BOOLEAN AS $$
BEGIN
  IF NOT is_set(old_v) AND NOT is_set(new_v)
  THEN
    RETURN FALSE;
  ELSEIF NOT is_set(old_v) AND is_set(new_v)
    THEN
      RETURN TRUE;
  ELSEIF is_set(old_v) AND NOT is_set(new_v)
    THEN
      RETURN TRUE;
  ELSE
    RETURN (old_v :: text <> new_v :: text);
  END IF;

END $$
LANGUAGE plpgsql;

DO $$
DECLARE
  infosystem_uuid UUID;
BEGIN
  FOR infosystem_uuid IN (SELECT DISTINCT json_content ->> 'uuid' as uuid FROM riha.main_resource)
  LOOP
    PERFORM riha.set_creation_and_update_time_for_infosystem(infosystem_uuid);
  END LOOP;

  RAISE NOTICE 'DONE!';
END $$;

DROP FUNCTION IF EXISTS riha.is_set(jsonb) CASCADE;
DROP FUNCTION IF EXISTS riha.is_access_restriction_changed(jsonb, jsonb) CASCADE;
DROP FUNCTION IF EXISTS riha.set_creation_and_update_time_for_docs(jsonb, jsonb, jsonb) CASCADE;
DROP FUNCTION IF EXISTS riha.set_creation_and_update_time_for_infosystem(UUID) CASCADE;