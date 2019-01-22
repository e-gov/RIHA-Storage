WITH imported_info_system AS (
    SELECT
        DISTINCT ON (inf.lyhinimi)
                    inf.lyhinimi,
                    uuid_in(md5(inf.lyhinimi) :: CSTRING) AS inf_uuid,
                    json_build_object(
                        'class', substring(inf.tk_kaideldavus_kood, '[^_]*$')
                                   || substring(inf.tk_terviklus_kood, '[^_]*$')
                                   || substring(inf.tk_konfidentsiaalsus_kood, '[^_]*$'),
                        'level', CASE
                                   WHEN (inf.tk_kaideldavus_kood || inf.tk_terviklus_kood || inf.tk_konfidentsiaalsus_kood) ~ '3' THEN 'H'
                                   WHEN (inf.tk_kaideldavus_kood || inf.tk_terviklus_kood || inf.tk_konfidentsiaalsus_kood) ~ '2' THEN 'M'
                                   ELSE 'L'
                            END,
                        'standard', 'ISKE',
                        'latest_audit_date', 'null',
                        'latest_audit_resolution', 'null') AS security
    FROM infosysteem inf
    WHERE inf.kuupaev_kuni IS NULL
      AND inf.staatus_kood IS DISTINCT FROM 'STAATUS_EI_ASUTATA'
      AND inf.staatus_kood IS DISTINCT FROM 'INFOSYS_STAATUS_LOPETATUD'
      AND kategooria IS DISTINCT FROM 'INFOSYSTEEM_KATEGOORIA_ALAMSYSTEEM'
      AND inf.tk_kaideldavus_kood IS NOT NULL
      AND inf.tk_konfidentsiaalsus_kood IS NOT NULL
      AND inf.tk_terviklus_kood IS NOT NULL
    ORDER BY inf.lyhinimi, inf.created DESC
    )
INSERT INTO riha.main_resource(main_resource_id, uri, name, owner, short_name, version, json_content, parent_uri, main_resource_parent_id,
                                     kind, state, start_date, end_date, creator, modifier, creation_date, modified_date, old_id, field_name, kind_id, main_resource_template_id)
SELECT
       nextval('riha.main_resource_seq'),
       uri,
       name,
       owner,
       short_name,
       version,
       jsonb_set(json_content, '{security}', imported_info_system.security :: jsonb),
       parent_uri,
       main_resource_parent_id,
       kind,
       state,
       start_date,
       end_date,
       creator,
       modifier,
       creation_date,
       modified_date,
       old_id,
       field_name,
       kind_id,
       main_resource_template_id
FROM imported_info_system INNER JOIN riha.main_resource_view mrv
         ON imported_info_system.inf_uuid = (mrv.json_content ->> 'uuid') :: UUID
WHERE NOT mrv.json_content ? 'security';