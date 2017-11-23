DROP INDEX IF EXISTS riha.ixfk_comment_comment;
DROP TABLE IF EXISTS riha.comment;

DROP TABLE IF EXISTS riha.document;

DROP INDEX IF EXISTS riha.ixfk_data_object_main_resource;
DROP INDEX IF EXISTS riha.ixfk_data_object_data_object;
DROP TABLE IF EXISTS riha.data_object;

DROP VIEW IF EXISTS riha.main_resource_relation_view;
DROP FUNCTION IF EXISTS riha.update_main_resource_search_content();
DROP FUNCTION IF EXISTS riha.main_resource_search_content( JSONB );
DROP INDEX IF EXISTS riha.idx_main_resource_relation_infosystem_uuid;
DROP INDEX IF EXISTS riha.idx_main_resource_relation_related_infosystem_uuid;
DROP TABLE IF EXISTS riha.main_resource_relation;

DROP VIEW IF EXISTS riha.main_resource_view;
DROP INDEX IF EXISTS riha.ixfk_main_resource_kind_template;
DROP INDEX IF EXISTS riha.ixfk_main_resource_kind;
DROP INDEX IF EXISTS riha.ixfk_main_resource_main_resource;
DROP TABLE IF EXISTS riha.main_resource;

DROP TABLE IF EXISTS riha.kind;

DROP TABLE IF EXISTS riha.file_resource;

DROP TABLE IF EXISTS riha.large_object;

DROP SEQUENCE IF EXISTS riha.comment_seq;
DROP SEQUENCE IF EXISTS riha.data_object_seq;
DROP SEQUENCE IF EXISTS riha.document_seq;
DROP SEQUENCE IF EXISTS riha.kind_seq;
DROP SEQUENCE IF EXISTS riha.main_resource_seq;
DROP SEQUENCE IF EXISTS riha.main_resource_relation_seq;
DROP SEQUENCE IF EXISTS riha.large_object_seq;