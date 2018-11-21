alter table riha.main_resource drop constraint fk_main_resource_main_resource_02;
alter table riha.data_object drop constraint fk_data_object_data_object;
alter table riha.data_object drop constraint fk_data_object_kind;
alter table riha.data_object drop constraint fk_data_object_main_resource;
alter table riha.document drop CONSTRAINT fk_data_object;
alter TABLE riha.document DROP CONSTRAINT fk_main_resource;
alter table riha.comment drop CONSTRAINT fk_comment_comment;
alter table riha.file_resource drop CONSTRAINT fk_file_resource_large_object;
DROP TRIGGER IF EXISTS tr_infosystem_update ON main_resource;