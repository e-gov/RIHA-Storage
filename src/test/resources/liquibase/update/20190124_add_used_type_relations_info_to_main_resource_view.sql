-- drop view riha.main_resource_view cascade

CREATE OR REPLACE VIEW riha.main_resource_view AS

 SELECT DISTINCT ON (json_content ->> 'uuid')
    main_resource.*,
    ((main_resource.json_content #>>
      '{meta,creation_timestamp}' :: TEXT [])) :: TIMESTAMP WITH TIME ZONE AS j_creation_timestamp,
    ((main_resource.json_content #>>
      '{meta,update_timestamp}' :: TEXT [])) :: TIMESTAMP WITH TIME ZONE   AS j_update_timestamp,
    last_positive_approval_request.sub_type                                AS last_positive_approval_request_type,
    last_positive_approval_request.modified_date                           AS last_positive_approval_request_date,
    last_positive_establishment_request.modified_date                      AS last_positive_establishment_request_date,
    last_positive_take_into_use_request.modified_date                      AS last_positive_take_into_use_request_date,
    last_positive_finalization_request.modified_date                       AS last_positive_finalization_request_date
    ,COALESCE (has_used_system_types_relations.has_used_system_type_relations, false)                      AS has_used_system_type_relations
  FROM riha.main_resource AS main_resource
    LEFT JOIN (SELECT DISTINCT ON (infosystem_uuid)
                 infosystem_uuid,
                 sub_type,
                 modified_date
               FROM riha.comment
               WHERE
                 type = 'ISSUE'
                 AND sub_type IN ('ESTABLISHMENT_REQUEST',
                                  'TAKE_INTO_USE_REQUEST',
                                  'FINALIZATION_REQUEST')
                 AND status = 'CLOSED'
                 AND resolution_type = 'POSITIVE'
               ORDER BY infosystem_uuid, modified_date DESC) AS last_positive_approval_request
      ON (json_content ->> 'uuid') :: UUID = last_positive_approval_request.infosystem_uuid
    LEFT JOIN (SELECT DISTINCT ON (infosystem_uuid)
                infosystem_uuid,
                modified_date
              FROM riha.comment
              WHERE
                type = 'ISSUE'
                AND sub_type = 'ESTABLISHMENT_REQUEST'
                AND status = 'CLOSED'
                AND resolution_type = 'POSITIVE'
              ORDER BY infosystem_uuid, modified_date DESC) AS last_positive_establishment_request
      ON (json_content ->> 'uuid') :: UUID = last_positive_establishment_request.infosystem_uuid
      LEFT JOIN (SELECT DISTINCT ON (infosystem_uuid)
                infosystem_uuid,
                modified_date
              FROM riha.comment
              WHERE
                type = 'ISSUE'
                AND sub_type = 'TAKE_INTO_USE_REQUEST'
                AND status = 'CLOSED'
                AND resolution_type = 'POSITIVE'
              ORDER BY infosystem_uuid, modified_date DESC) AS last_positive_take_into_use_request
      ON (json_content ->> 'uuid') :: UUID = last_positive_take_into_use_request.infosystem_uuid
      LEFT JOIN (SELECT DISTINCT ON (infosystem_uuid)
                infosystem_uuid,
                modified_date
              FROM riha.comment
              WHERE
                type = 'ISSUE'
                AND sub_type = 'FINALIZATION_REQUEST'
                AND status = 'CLOSED'
                AND resolution_type = 'POSITIVE'
              ORDER BY infosystem_uuid, modified_date DESC) AS last_positive_finalization_request
      ON (json_content ->> 'uuid') :: UUID = last_positive_finalization_request.infosystem_uuid

      LEFT JOIN
        (select count(*) > 0 as has_used_system_type_relations, infosystem_uuid from riha.main_resource_relation mrr where mrr.type ='USED_SYSTEM' group by infosystem_uuid)
         as has_used_system_types_relations
      ON  (json_content ->> 'uuid') :: UUID = has_used_system_types_relations.infosystem_uuid

  ORDER BY json_content ->> 'uuid',
    j_update_timestamp DESC NULLS LAST,
    main_resource_id DESC

