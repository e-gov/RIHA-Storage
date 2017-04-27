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

-- Index: riha.ixfk_main_resource_kind_template

DROP INDEX riha.ixfk_main_resource_kind_template;

-- Index: riha.ixfk_main_resource_kind

DROP INDEX riha.ixfk_main_resource_kind;

-- Index: riha.ixfk_main_resource_main_resource

DROP INDEX riha.ixfk_main_resource_main_resource;

-- Table: riha.main_resource

DROP TABLE riha.main_resource;

-- Index: riha.ixfk_user_rights_kind

DROP INDEX riha.ixfk_user_rights_kind;

-- Table: riha.role_right

DROP TABLE riha.role_right;

-- Table: riha.kind

DROP TABLE riha.kind;



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

-- Sequence: riha.role_right_seq

DROP SEQUENCE riha.role_right_seq;
