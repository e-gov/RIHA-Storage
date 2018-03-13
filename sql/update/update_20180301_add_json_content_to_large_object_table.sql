-- Add json_content column to large_object table in order to persist CSV file content as JSON

ALTER TABLE riha.large_object
  ADD indexed boolean DEFAULT false NOT NULL;

ALTER TABLE riha.large_object
  ADD csv_search_content jsonb NULL;