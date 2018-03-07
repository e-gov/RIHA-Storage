-- Normalize existing trigger--trigger function--function naming convention across schema

-- trigger function name is tightly coupled with trigger
DROP FUNCTION IF EXISTS riha.update_main_resource_search_content() CASCADE;
CREATE OR REPLACE FUNCTION riha.on_before_main_resource_insert_or_update()
  RETURNS TRIGGER AS $$
BEGIN
  new.search_content = riha.main_resource_search_content(new.json_content);
  RETURN new;
END $$
LANGUAGE plpgsql
IMMUTABLE;

-- trigger name reflects event and resource names
DROP TRIGGER IF EXISTS update_search_content
ON riha.main_resource;
CREATE TRIGGER before_main_resource_insert_or_update
  BEFORE INSERT OR UPDATE
  ON riha.main_resource
  FOR EACH ROW EXECUTE PROCEDURE riha.on_before_main_resource_insert_or_update();