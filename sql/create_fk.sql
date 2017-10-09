alter table riha.main_resource add CONSTRAINT fk_main_resource_main_resource_02 FOREIGN KEY (main_resource_template_id)
REFERENCES riha.main_resource (main_resource_id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION;

alter table riha.data_object add CONSTRAINT fk_data_object_data_object FOREIGN KEY (data_object_parent_id)
REFERENCES riha.data_object (data_object_id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE CASCADE;

alter table riha.data_object add CONSTRAINT fk_data_object_kind FOREIGN KEY (kind_id)
REFERENCES riha.kind (kind_id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION;

alter table riha.data_object add CONSTRAINT fk_data_object_main_resource FOREIGN KEY (main_resource_id)
REFERENCES riha.main_resource (main_resource_id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE CASCADE;

alter table riha.document add CONSTRAINT fk_data_object FOREIGN KEY (data_object_id)
REFERENCES riha.data_object (data_object_id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE CASCADE;

alter table riha.document add CONSTRAINT fk_main_resource FOREIGN KEY (main_resource_id)
REFERENCES riha.main_resource (main_resource_id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE CASCADE;

alter table riha.comment add CONSTRAINT fk_comment_comment FOREIGN KEY (comment_parent_id)
REFERENCES riha.comment (comment_id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE riha.file_resource ADD CONSTRAINT fk_file_resource_large_object FOREIGN KEY (large_object_id)
REFERENCES riha.large_object (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION;

CREATE TRIGGER tr_infosystem_update AFTER UPDATE ON main_resource FOR EACH ROW EXECUTE PROCEDURE infosystem_trg();
