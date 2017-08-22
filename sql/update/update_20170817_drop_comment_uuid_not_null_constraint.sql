-- some of comment records may not have info system uuid defined

ALTER TABLE riha.comment ALTER COLUMN infosystem_uuid DROP DEFAULT;
ALTER TABLE riha.comment ALTER COLUMN infosystem_uuid DROP NOT NULL;