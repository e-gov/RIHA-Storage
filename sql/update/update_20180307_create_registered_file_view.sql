-- DROP VIEW riha.registered_file_view;
CREATE OR REPLACE VIEW riha.registered_file_view AS
  SELECT
    f.uuid            AS file_resource_uuid,
    f.name            AS file_resource_name,
    f.large_object_id AS file_resource_large_object_id,
    i.uuid            AS infosystem_uuid,
    i.short_name      AS infosystem_short_name,
    i.name            AS infosystem_name,
    i.owner_name      AS infosystem_owner_name,
    i.owner_code      AS infosystem_owner_code
  FROM riha.registered_file r
    LEFT JOIN riha.file_resource f
      ON f.uuid = r.file_resource_uuid
    LEFT JOIN riha.large_object lo
      ON f.large_object_id = lo.id
    LEFT JOIN (
                SELECT
                  (json_content #>> '{uuid}') :: UUID AS uuid,
                  json_content #>> '{short_name}'     AS short_name,
                  json_content #>> '{name}'           AS name,
                  json_content #>> '{owner,name}'     AS owner_name,
                  json_content #>> '{owner,code}'     AS owner_code
                FROM riha.main_resource_view) i
      ON i.uuid = r.main_resource_uuid;