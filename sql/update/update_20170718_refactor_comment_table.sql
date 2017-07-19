-- Comment table refactoring for approval support

-- Remove unused columns
ALTER TABLE riha.comment DROP uri;
ALTER TABLE riha.comment DROP organization;
ALTER TABLE riha.comment DROP state;
ALTER TABLE riha.comment DROP main_resource_uri;
ALTER TABLE riha.comment DROP data_object_uri;
ALTER TABLE riha.comment DROP document_uri;
ALTER TABLE riha.comment DROP comment_uri;
ALTER TABLE riha.comment DROP creator;
ALTER TABLE riha.comment DROP modifier;
ALTER TABLE riha.comment DROP kind;
ALTER TABLE riha.comment DROP json_content;

-- Add new approval columns
ALTER TABLE riha.comment ADD author_name VARCHAR(255) NULL;
ALTER TABLE riha.comment ADD author_personal_code VARCHAR(11) NULL;
ALTER TABLE riha.comment ADD organization_name VARCHAR(255) NULL;
ALTER TABLE riha.comment ADD organization_code VARCHAR(50) NULL;
ALTER TABLE riha.comment ADD status VARCHAR(150) NULL;
ALTER TABLE riha.comment ADD type VARCHAR(150) NULL;
ALTER TABLE riha.comment ADD title VARCHAR(255) NULL;

-- Remove limit from comment
ALTER TABLE riha.comment ALTER COLUMN comment TYPE VARCHAR USING comment::VARCHAR;