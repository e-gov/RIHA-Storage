create or replace view riha.main_resource_view as
  SELECT DISTINCT ON ((main_resource.json_content ->> 'uuid'::text)) main_resource.main_resource_id,
                     main_resource.uri,
                     main_resource.name,
                     main_resource.owner,
                     main_resource.short_name,
                     main_resource.version,
                     main_resource.json_content,
                     main_resource.parent_uri,
                     main_resource.main_resource_parent_id,
                     main_resource.kind,
                     main_resource.state,
                     main_resource.start_date,
                     main_resource.end_date,
                     main_resource.creator,
                     main_resource.modifier,
                     main_resource.creation_date,
                     main_resource.modified_date,
                     main_resource.old_id,
                     main_resource.field_name,
                     main_resource.kind_id,
                     main_resource.main_resource_template_id,
                     main_resource.search_content,



                     ((main_resource.json_content #>> '{meta,creation_timestamp}'::text[]))::timestamp with time zone AS j_creation_timestamp,
                     ((main_resource.json_content #>> '{meta,update_timestamp}'::text[]))::timestamp with time zone AS j_update_timestamp,
case when
                     COALESCE( jsonb_exists_any(main_resource.json_content -> 'topics', array[ 'dokumendihaldussüsteem', 'x-tee alamsüsteem', 'asutusesiseseks kasutamiseks']), false)
                        or (select count(*) from riha.main_resource_relation where type = 'USED_SYSTEM' and main_resource_relation_id = main_resource_id) > 0
                        then 'AUTOMATICALLY_REGISTERED'::varchar (150)
                        else last_positive_approval_request.sub_type
                    end as last_positive_approval_request_type,

                     last_positive_approval_request.modified_date AS last_positive_approval_request_date,
                     last_positive_establishment_request.modified_date AS last_positive_establishment_request_date,
                     last_positive_take_into_use_request.modified_date AS last_positive_take_into_use_request_date,
                     last_positive_finalization_request.modified_date AS last_positive_finalization_request_date,
                     COALESCE (has_used_system_types_relations.has_used_system_type_relations, false) AS has_used_system_type_relations
  FROM (((((riha.main_resource main_resource
      LEFT JOIN (SELECT DISTINCT ON (comment.infosystem_uuid) comment.infosystem_uuid,
                                    comment.sub_type,
                                    comment.modified_date
                 FROM riha.comment
                 WHERE (((comment.type) :: text = 'ISSUE' :: text) AND ((comment.sub_type) :: text = ANY
                                                                        ((ARRAY['ESTABLISHMENT_REQUEST'::character varying, 'TAKE_INTO_USE_REQUEST'::character varying, 'FINALIZATION_REQUEST'::character varying]) :: text [])) AND
                        ((comment.status) :: text = 'CLOSED' :: text) AND
                        ((comment.resolution_type) :: text = 'POSITIVE' :: text))
                 ORDER BY comment.infosystem_uuid, comment.modified_date DESC) last_positive_approval_request ON ((
    ((main_resource.json_content ->> 'uuid' :: text)) :: uuid = last_positive_approval_request.infosystem_uuid)))
      LEFT JOIN (SELECT DISTINCT ON (comment.infosystem_uuid) comment.infosystem_uuid, comment.modified_date
                 FROM riha.comment
                 WHERE (((comment.type) :: text = 'ISSUE' :: text) AND
                        ((comment.sub_type) :: text = 'ESTABLISHMENT_REQUEST' :: text) AND
                        ((comment.status) :: text = 'CLOSED' :: text) AND
                        ((comment.resolution_type) :: text = 'POSITIVE' :: text))
                 ORDER BY comment.infosystem_uuid, comment.modified_date DESC) last_positive_establishment_request ON ((
    ((main_resource.json_content ->> 'uuid' :: text)) :: uuid = last_positive_establishment_request.infosystem_uuid)))
      LEFT JOIN (SELECT DISTINCT ON (comment.infosystem_uuid) comment.infosystem_uuid, comment.modified_date
                 FROM riha.comment
                 WHERE (((comment.type) :: text = 'ISSUE' :: text) AND
                        ((comment.sub_type) :: text = 'TAKE_INTO_USE_REQUEST' :: text) AND
                        ((comment.status) :: text = 'CLOSED' :: text) AND
                        ((comment.resolution_type) :: text = 'POSITIVE' :: text))
                 ORDER BY comment.infosystem_uuid, comment.modified_date DESC) last_positive_take_into_use_request ON ((
    ((main_resource.json_content ->> 'uuid' :: text)) :: uuid = last_positive_take_into_use_request.infosystem_uuid)))
      LEFT JOIN (SELECT DISTINCT ON (comment.infosystem_uuid) comment.infosystem_uuid, comment.modified_date
                 FROM riha.comment
                 WHERE (((comment.type) :: text = 'ISSUE' :: text) AND
                        ((comment.sub_type) :: text = 'FINALIZATION_REQUEST' :: text) AND
                        ((comment.status) :: text = 'CLOSED' :: text) AND
                        ((comment.resolution_type) :: text = 'POSITIVE' :: text))
                 ORDER BY comment.infosystem_uuid, comment.modified_date DESC) last_positive_finalization_request ON ((
    ((main_resource.json_content ->> 'uuid' :: text)) :: uuid = last_positive_finalization_request.infosystem_uuid)))
      LEFT JOIN (SELECT (count(*) > 0) AS has_used_system_type_relations, mrr.infosystem_uuid
                 FROM riha.main_resource_relation mrr
                 WHERE ((mrr.type) :: text = 'USED_SYSTEM' :: text)
                 GROUP BY mrr.infosystem_uuid) has_used_system_types_relations ON ((
    ((main_resource.json_content ->> 'uuid' :: text)) :: uuid = has_used_system_types_relations.infosystem_uuid))
      )
  ORDER BY (main_resource.json_content ->> 'uuid'::text), ((main_resource.json_content #>> '{meta,update_timestamp}'::text[]))::timestamp with time zone DESC NULLS LAST, main_resource.main_resource_id DESC;



