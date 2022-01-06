create or replace view riha.main_resource_relation_view as
    SELECT
        mrr.*,
        infosystem.json_content ->> 'short_name'         AS infosystem_short_name,
        infosystem.json_content ->> 'name'               AS infosystem_name,
        related_infosystem.json_content ->> 'short_name' AS related_infosystem_short_name,
        related_infosystem.json_content ->> 'name'       AS related_infosystem_name,
        infosystem.json_content #>> '{meta,system_status,status}' AS infosystem_status,
        related_infosystem.json_content #>> '{meta,system_status,status}' AS related_infosystem_status
    FROM riha.main_resource_relation mrr
             LEFT JOIN riha.main_resource_view infosystem ON (infosystem.json_content ->> 'uuid') = mrr.infosystem_uuid :: TEXT
             LEFT JOIN riha.main_resource_view related_infosystem
                       ON (related_infosystem.json_content ->> 'uuid') = mrr.related_infosystem_uuid :: TEXT;
