-- Index: riha.ixfk_comment_comment

DROP INDEX riha.ixfk_comment_comment;

-- Table: riha.comment

DROP TABLE riha.comment;

-- Table: riha.document

DROP TABLE riha.document;

-- Index: riha.ixfk_data_object_main_resource

DROP INDEX riha.ixfk_data_object_main_resource;

-- Index: riha.ixfk_data_object_data_object

DROP INDEX riha.ixfk_data_object_data_object;

-- Table: riha.data_object

DROP TABLE riha.data_object;

-- Table: riha.main_resource_relation
DROP INDEX riha.idx_main_resource_relation_infosystem_uuid;
DROP INDEX riha.idx_main_resource_relation_related_infosystem_uuid;
DROP TABLE riha.main_resource_relation;

-- Index: riha.ixfk_main_resource_kind_template

DROP INDEX riha.ixfk_main_resource_kind_template;

-- Index: riha.ixfk_main_resource_kind

DROP INDEX riha.ixfk_main_resource_kind;

-- Index: riha.ixfk_main_resource_main_resource

DROP INDEX riha.ixfk_main_resource_main_resource;

-- Table: riha.main_resource

DROP TABLE riha.main_resource;

-- Table: riha.kind

DROP TABLE riha.kind;

-- Table: riha.file_resource

DROP TABLE riha.file_resource;

-- Table: riha.large_object

DROP TABLE riha.large_object;

-- Sequence: riha.comment_seq

DROP SEQUENCE riha.comment_seq;

-- Sequence: riha.data_object_seq

DROP SEQUENCE riha.data_object_seq;

-- Sequence: riha.document_seq

DROP SEQUENCE riha.document_seq;

-- Sequence: riha.kind_seq

DROP SEQUENCE riha.kind_seq;

-- Sequence: riha.main_resource_seq

DROP SEQUENCE riha.main_resource_seq;

-- Sequence: riha.main_resource_relation_seq

DROP SEQUENCE riha.main_resource_relation_seq;