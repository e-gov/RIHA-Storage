-- Remove access restriction
ALTER TABLE riha.main_resource DROP access_restriction;
ALTER TABLE riha.data_object DROP access_restriction;
ALTER TABLE riha.document DROP access_restriction;
ALTER TABLE riha.comment DROP access_restriction;