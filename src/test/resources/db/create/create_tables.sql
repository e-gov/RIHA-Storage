-- Extensions
-- CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Sequence: comment_seq

-- DROP SEQUENCE comment_seq;

CREATE SEQUENCE IF NOT EXISTS comment_seq
INCREMENT 1
MINVALUE 1
MAXVALUE 9223372036854775807
START 10803
CACHE 1;
-- GRANT SELECT, USAGE ON SEQUENCE comment_seq TO riha;

-- Sequence: data_object_seq

-- DROP SEQUENCE data_object_seq;

CREATE SEQUENCE IF NOT EXISTS data_object_seq
INCREMENT 1
MINVALUE 1
MAXVALUE 9223372036854775807
START 5604527
CACHE 1;
-- GRANT SELECT, USAGE ON SEQUENCE data_object_seq TO riha;

-- Sequence: document_seq

-- DROP SEQUENCE document_seq;

CREATE SEQUENCE IF NOT EXISTS document_seq
INCREMENT 1
MINVALUE 1
MAXVALUE 9223372036854775807
START 183791
CACHE 1;
-- GRANT SELECT, USAGE ON SEQUENCE document_seq TO riha;

-- Sequence: kind_seq

-- DROP SEQUENCE kind_seq;

CREATE SEQUENCE IF NOT EXISTS kind_seq
INCREMENT 1
MINVALUE 1
MAXVALUE 9223372036854775807
START 398
CACHE 1;
-- GRANT SELECT, USAGE ON SEQUENCE kind_seq TO riha;

-- Sequence: main_resource_seq

-- DROP SEQUENCE main_resource_seq;

CREATE SEQUENCE IF NOT EXISTS main_resource_seq
INCREMENT 1
MINVALUE 1
MAXVALUE 9223372036854775807
START 436069
CACHE 1;
-- GRANT SELECT, USAGE ON SEQUENCE main_resource_seq TO riha;

CREATE SEQUENCE IF NOT EXISTS large_object_seq
  INCREMENT 1
  START 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  CACHE 1;
-- GRANT SELECT, USAGE ON SEQUENCE large_object_seq TO riha;

CREATE SEQUENCE IF NOT EXISTS main_resource_relation_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
-- GRANT SELECT, USAGE ON SEQUENCE main_resource_relation_seq TO riha;

-- Table: kind

-- DROP TABLE kind;

CREATE TABLE IF NOT EXISTS kind
(
  kind_id integer NOT NULL,
  name character varying(150) NOT NULL, -- Ressursi liigi nimetus
  json_content json,
  state character varying(150) DEFAULT 'C', -- Olek
  start_date timestamp with time zone, -- Ressursi tüübi kehtivuse alguse ajamoment
  end_date timestamp with time zone, -- Ressursi tüübi kehtivuse lõpu ajamoment
  creator character varying(150), -- Kirje tekitaja isikukood
  modifier character varying(150), -- Kirje muutja isikukood
  creation_date timestamp with time zone, -- Kirje loomise ajamoment
  modified_date timestamp with time zone, -- Kirje muutmise ajamoment
  CONSTRAINT pk_kind PRIMARY KEY (kind_id)
);

COMMENT ON TABLE kind
IS 'Sisaldab metainformatsiooni erinevate main_resource tabelis hoitavate ressursside tüüpide kohta.';
COMMENT ON COLUMN kind.name IS 'Ressursi liigi nimetus';
COMMENT ON COLUMN kind.state IS 'Olek';
COMMENT ON COLUMN kind.start_date IS 'Ressursi tüübi kehtivuse alguse ajamoment';
COMMENT ON COLUMN kind.end_date IS 'Ressursi tüübi kehtivuse lõpu ajamoment';
COMMENT ON COLUMN kind.creator IS 'Kirje tekitaja isikukood';
COMMENT ON COLUMN kind.modifier IS 'Kirje muutja isikukood';
COMMENT ON COLUMN kind.creation_date IS 'Kirje loomise ajamoment';
COMMENT ON COLUMN kind.modified_date IS 'Kirje muutmise ajamoment';

-- GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE kind TO riha;

-- Table: main_resource

-- DROP TABLE main_resource;

CREATE TABLE IF NOT EXISTS main_resource
(
  main_resource_id integer NOT NULL, -- Ressursi unikaalne ID. Iga uus versioon saab uue ID. Kõige väiksema ID-ga ressurss on hetkel aktuaalne
  uri character varying(150), -- Ressursi unikaalne URI. Sellega määratakse millised on samad aga erineva versiooniga ressursid ressursside tabelis
  name character varying(190), -- Ressursi nimetus
  owner character varying(150), -- Ressursi omanik. Tavapäraselt ettevõtte registrikood. Infosüsteemi mõttes vastutav isik.
  short_name character varying(50), -- Ressursi lühinimetus
  version character varying(10), -- Inimloetav versiooni nimi. See ei ühti infosüsteemi versiooni nimetusega
  json_content json, -- Ressursi kirjelduse täisinfo esitatuna json struktuurina (sisaldab ka eraldi väljadena toodud andmed).
  parent_uri character varying(150), -- Hierarhilise ressursi puhul on siin näidatud vanema URI
  main_resource_parent_id integer, -- Hierarhilise ressursi puhul on siin näidatud vanema ID
  kind character varying(150), -- Ressursi liik (infosystem, classifier, service, dictionary, xmlresource vms.)
  state character(1), -- Ressursi olek (C-current, O-old, T-temporary, D-deleted jms.) Vaikimisi 'C'.
  start_date timestamp with time zone, -- Käesoleva versiooni kehtivuse algus
  end_date timestamp with time zone, -- Käesoleva versiooni kehtivuse lõpp
  creator character varying(150), -- Kirje loonud isiku isikukood või muu identifikaator
  modifier character varying(150), -- Viimati kirjet muutnud isiku isikukood või muu identifikaator
  creation_date timestamp with time zone, -- Kirje loomise ajahetk.
  modified_date timestamp with time zone, -- Kirje viimati muutmise ajahetk.
  old_id integer,
  field_name character varying(150),
  kind_id integer, -- Ressursi liik (infosystem, classifier, service, dictionary, xmlresource vms.).
  main_resource_template_id integer,
  search_content text, -- Indexed full text search column.
  CONSTRAINT pk_main_resource PRIMARY KEY (main_resource_id),
  CONSTRAINT fk_main_resource_main_resource_02 FOREIGN KEY (main_resource_template_id)
  REFERENCES main_resource (main_resource_id)
  ON UPDATE NO ACTION ON DELETE NO ACTION
);

COMMENT ON TABLE main_resource
IS 'Selles tabelis hoitakse andmebaasi põhiobjektide (ressursside) põhiinfot. Ressurssideks võivad olla näiteks infosüsteem, klassifikaator, valdkonna sõnastik vms. Ressursse hoitakse tabelis versioonidena, iga ressursi versiooni jaoks hoitakse eraldi kirjet. Sealjuures lähtutakse põhimõttest, et kõige esimene tekitatud kirje kajastab alati jooksvat hetkeseisu ning hiljem tekitatud kirjed on vastava ressursi kirjelduse vanemad versioonid. ';
COMMENT ON COLUMN main_resource.main_resource_id IS 'Ressursi unikaalne ID. Iga uus versioon saab uue ID. Kõige väiksema ID-ga ressurss on hetkel aktuaalne';
COMMENT ON COLUMN main_resource.uri IS 'Ressursi unikaalne URI. Sellega määratakse millised on samad aga erineva versiooniga ressursid ressursside tabelis';
COMMENT ON COLUMN main_resource.name IS 'Ressursi nimetus';
COMMENT ON COLUMN main_resource.owner IS 'Ressursi omanik. Tavapäraselt ettevõtte registrikood. Infosüsteemi mõttes vastutav isik.';
COMMENT ON COLUMN main_resource.short_name IS 'Ressursi lühinimetus';
COMMENT ON COLUMN main_resource.version IS 'Inimloetav versiooni nimi. See ei ühti infosüsteemi versiooni nimetusega';
COMMENT ON COLUMN main_resource.json_content IS 'Ressursi kirjelduse täisinfo esitatuna json struktuurina (sisaldab ka eraldi väljadena toodud andmed).';
COMMENT ON COLUMN main_resource.parent_uri IS 'Hierarhilise ressursi puhul on siin näidatud vanema URI';
COMMENT ON COLUMN main_resource.main_resource_parent_id IS 'Hierarhilise ressursi puhul on siin näidatud vanema ID';
COMMENT ON COLUMN main_resource.kind IS 'Ressursi liik (infosystem, classifier, service, dictionary, xmlresource vms.)';
COMMENT ON COLUMN main_resource.state IS 'Ressursi olek (C-current, O-old, T-temporary, D-deleted jms.) Vaikimisi ''C''.';
COMMENT ON COLUMN main_resource.start_date IS 'Käesoleva versiooni kehtivuse algus';
COMMENT ON COLUMN main_resource.end_date IS 'Käesoleva versiooni kehtivuse lõpp';
COMMENT ON COLUMN main_resource.creator IS 'Kirje loonud isiku isikukood või muu identifikaator';
COMMENT ON COLUMN main_resource.modifier IS 'Viimati kirjet muutnud isiku isikukood või muu identifikaator';
COMMENT ON COLUMN main_resource.creation_date IS 'Kirje loomise ajahetk.';
COMMENT ON COLUMN main_resource.modified_date IS 'Kirje viimati muutmise ajahetk.';
COMMENT ON COLUMN main_resource.kind_id IS 'Ressursi liik (infosystem, classifier, service, dictionary, xmlresource vms.).';
COMMENT ON COLUMN main_resource.search_content IS 'Indexed full text search column.';

-- GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE main_resource TO riha;

-- CREATE INDEX main_resource_uuid
--   ON main_resource (((json_content ->> 'uuid')));

-- Index: ixfk_main_resource_main_resource

-- DROP INDEX ixfk_main_resource_main_resource;

CREATE INDEX IF NOT EXISTS ixfk_main_resource_main_resource
  ON main_resource
  (main_resource_parent_id);

-- Index: ixfk_main_resource_kind

-- DROP INDEX ixfk_main_resource_kind;

CREATE INDEX IF NOT EXISTS ixfk_main_resource_kind
  ON main_resource
  (kind_id);

-- Index: ixfk_main_resource_kind_template

-- DROP INDEX ixfk_main_resource_kind_template;

CREATE INDEX IF NOT EXISTS ixfk_main_resource_kind_template
  ON main_resource
  (main_resource_template_id);

-- DROP INDEX IF EXISTS idx_main_resource_search;

-- CREATE INDEX IF NOT EXISTS idx_main_resource_search
--   ON main_resource
--   USING GIN (search_content gin_trgm_ops);



-- Table: data_object

-- DROP TABLE data_object;

CREATE TABLE IF NOT EXISTS data_object
(
  data_object_id integer NOT NULL, -- Unikaalne andmeobjekti ID (kõigil erinevatel kirjetel on see unikaalne)
  uri character varying(150) NOT NULL, -- Andmeobjekti unikaalne URI (kõigil sama andmeobjekti erinevatel versioonidel on sama URI)
  name character varying(150) NOT NULL, -- Andmeobjekti nimi
  main_resource_id integer NOT NULL, -- Ressursi ID, mille alla antud andmeobjekt kuulub
  json_content json, -- Andmeobjekti sisu kirjeldus esitatuna json struktuurina.
  data_object_parent_id integer, -- Hierarhilise andmeobjekti puhul on siin vanema ID
  kind character varying(150), -- Andmeobjekti tüüp (databse, table, field, json, input, output jne)
  state character(1), -- Andmeobjekti staatus (C-current, O-old, T-temporary, D-deleted jne)
  start_date timestamp with time zone, -- Andmeobjekti versiooni kehtivuse algus
  end_date timestamp with time zone, -- Andmeobjekti versiooni kehtivuse lõpp
  creator character varying(150) NOT NULL, -- Kirje loonud isiku isikukood või muu identifikaator
  modifier character varying(150), -- Kirjet viimati muutnud isiku isikukood või muu identifikaator
  creation_date timestamp with time zone NOT NULL, -- Kirje loomise ajamoment.
  modified_date timestamp with time zone, -- Kirje viimase muutmise ajamoment.
  field_name character varying(150),
  old_id integer,
  kind_id integer,
  CONSTRAINT pk_data_object PRIMARY KEY (data_object_id),
  CONSTRAINT fk_data_object_data_object FOREIGN KEY (data_object_parent_id)
  REFERENCES data_object (data_object_id)
  ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT fk_data_object_kind FOREIGN KEY (kind_id)
  REFERENCES kind (kind_id)
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_data_object_main_resource FOREIGN KEY (main_resource_id)
  REFERENCES main_resource (main_resource_id)
  ON UPDATE NO ACTION ON DELETE CASCADE
);

COMMENT ON TABLE data_object
IS 'Tabel kajastab põhiressursside juurde kuuluvate andmeobjektide nfot. Iga andmeobjekti kohta on tabelis üks kirje, iga kirje viitab põhiressursile, mille alla see andmeobjekt kuulub. Andmeobjekte saab olla erineva tasemega ning nad võivad olla omavahel hierarhiliselt seotud  Näiteks infosüsteemi andmeobjektideks on infosüsteemi andmeväljad ning kirjeteks on andmekoosseisu element, andmebaas, andmebaasi tabel, andmebaasi tabeli atribuut. Lisaks saab andmeobjekte esitada hierarhiliselt, näidates ära, millise andmeobjekti alla antud andmeobjekt kuulub - näiteks andmebaasi tabeli atribuudi korral näidata ära andmebaasi tabel, mille alla see kuulub.';
COMMENT ON COLUMN data_object.data_object_id IS 'Unikaalne andmeobjekti ID (kõigil erinevatel kirjetel on see unikaalne)';
COMMENT ON COLUMN data_object.uri IS 'Andmeobjekti unikaalne URI (kõigil sama andmeobjekti erinevatel versioonidel on sama URI)';
COMMENT ON COLUMN data_object.name IS 'Andmeobjekti nimi';
COMMENT ON COLUMN data_object.main_resource_id IS 'Ressursi ID, mille alla antud andmeobjekt kuulub';
COMMENT ON COLUMN data_object.json_content IS 'Andmeobjekti sisu kirjeldus esitatuna json struktuurina.';
COMMENT ON COLUMN data_object.data_object_parent_id IS 'Hierarhilise andmeobjekti puhul on siin vanema ID';
COMMENT ON COLUMN data_object.kind IS 'Andmeobjekti tüüp (databse, table, field, json, input, output jne)';
COMMENT ON COLUMN data_object.state IS 'Andmeobjekti staatus (C-current, O-old, T-temporary, D-deleted jne)';
COMMENT ON COLUMN data_object.start_date IS 'Andmeobjekti versiooni kehtivuse algus';
COMMENT ON COLUMN data_object.end_date IS 'Andmeobjekti versiooni kehtivuse lõpp';
COMMENT ON COLUMN data_object.creator IS 'Kirje loonud isiku isikukood või muu identifikaator';
COMMENT ON COLUMN data_object.modifier IS 'Kirjet viimati muutnud isiku isikukood või muu identifikaator';
COMMENT ON COLUMN data_object.creation_date IS 'Kirje loomise ajamoment.';
COMMENT ON COLUMN data_object.modified_date IS 'Kirje viimase muutmise ajamoment.';

-- GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE data_object TO riha;

-- Index: ixfk_data_object_data_object

-- DROP INDEX ixfk_data_object_data_object;

CREATE INDEX IF NOT EXISTS ixfk_data_object_data_object
  ON data_object
  (data_object_parent_id);

-- Index: ixfk_data_object_main_resource

-- DROP INDEX ixfk_data_object_main_resource;

CREATE INDEX IF NOT EXISTS ixfk_data_object_main_resource
  ON data_object
  (main_resource_id);

-- Table: document

-- DROP TABLE document;

CREATE TABLE IF NOT EXISTS document
(
  document_id integer NOT NULL, -- Unikaalne dokumendi ID
  uri character varying(150) NOT NULL, -- Dokumendi URI. Versioonist sõltumatu dokumendi identifikaator
  url character varying(240), -- Dokumendi URL, kui faili asemel on antud link dokumendile
  name character varying(150), -- Dokumendi nimi
  filename character varying(150), -- Dokumendifaili nimi serveri failisüsteemis dokumentidele ette nähtud kausta suhtes. Võib olla ka tühi, kui dokument on antud URL abil.
  mime character varying(150), -- MIME tüüp, kui see on teada
  json_content json, -- Dokumendi info esitatuna json struktuurina.
  state character(1), -- Dokumendi staatus (C-current, D-deleted, O-old, T-temporary jne)
  start_date timestamp with time zone, -- Dokumendi kehtivuse algus
  end_date timestamp with time zone, -- Dokumendi kehtivuse lõpp
  creator character varying(150), -- Kirje loonud isiku isikukood või muu identifikaator
  modifier character varying(150), -- Kirjet viimati muutnud isiku isikukood või muu identifikaator
  creation_date timestamp with time zone, -- Kirje loomise ajamoment.
  modified_date timestamp with time zone, -- Kirje viimase muutmise ajamoment.
  main_resource_id integer,
  data_object_id integer,
  field_name character varying(150),
  old_id integer,
  kind character varying(150),
  kind_id integer,
  CONSTRAINT pk_document PRIMARY KEY (document_id),
  CONSTRAINT fk_data_object FOREIGN KEY (data_object_id)
  REFERENCES data_object (data_object_id)
  ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT fk_main_resource FOREIGN KEY (main_resource_id)
  REFERENCES main_resource (main_resource_id)
  ON UPDATE NO ACTION ON DELETE CASCADE
);

COMMENT ON TABLE document
IS 'Dokumentide tabel. Iga dokument on esitatud eraldi kirjes kas vahetult dokumendifailina (kirjes tuuakse failinimi, dokument hoitakse failisüsteemis või andmebaasis) või siis URLina.';
COMMENT ON COLUMN document.document_id IS 'Unikaalne dokumendi ID';
COMMENT ON COLUMN document.uri IS 'Dokumendi URI. Versioonist sõltumatu dokumendi identifikaator';
COMMENT ON COLUMN document.url IS 'Dokumendi URL, kui faili asemel on antud link dokumendile';
COMMENT ON COLUMN document.name IS 'Dokumendi nimi';
COMMENT ON COLUMN document.filename IS 'Dokumendifaili nimi serveri failisüsteemis dokumentidele ette nähtud kausta suhtes. Võib olla ka tühi, kui dokument on antud URL abil.';
COMMENT ON COLUMN document.mime IS 'MIME tüüp, kui see on teada';
COMMENT ON COLUMN document.json_content IS 'Dokumendi info esitatuna json struktuurina.';
COMMENT ON COLUMN document.state IS 'Dokumendi staatus (C-current, D-deleted, O-old, T-temporary jne)';
COMMENT ON COLUMN document.start_date IS 'Dokumendi kehtivuse algus';
COMMENT ON COLUMN document.end_date IS 'Dokumendi kehtivuse lõpp';
COMMENT ON COLUMN document.creator IS 'Kirje loonud isiku isikukood või muu identifikaator';
COMMENT ON COLUMN document.modifier IS 'Kirjet viimati muutnud isiku isikukood või muu identifikaator';
COMMENT ON COLUMN document.creation_date IS 'Kirje loomise ajamoment.';
COMMENT ON COLUMN document.modified_date IS 'Kirje viimase muutmise ajamoment.';

-- GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE document TO riha;

-- Table: comment

-- DROP TABLE comment;

CREATE TABLE IF NOT EXISTS comment
(
  comment_id integer NOT NULL, -- Kommentaari unikaalne ID.
  comment_parent_id integer, -- Kui on tegemist hierarhilise kommentaariumiga, siis viitab vanemale
  creation_date timestamp with time zone, -- Kirje tekitamise ajamoment.
  modified_date timestamp with time zone, -- Kirje viimase muutmise ajamoment.
  infosystem_uuid UUID,
  comment VARCHAR,
  author_name VARCHAR(255) NULL,
  author_personal_code VARCHAR(13) NULL,
  organization_name VARCHAR(255) NULL,
  organization_code VARCHAR(50) NULL,
  status VARCHAR(150) NULL,
  type VARCHAR(150) NULL,
  title VARCHAR(255) NULL,
  sub_type VARCHAR(150) NULL,
  resolution_type VARCHAR(255) NULL,
  CONSTRAINT pk_comment PRIMARY KEY (comment_id),
  CONSTRAINT fk_comment_comment FOREIGN KEY (comment_parent_id)
  REFERENCES comment (comment_id)
  ON UPDATE NO ACTION ON DELETE NO ACTION
);

COMMENT ON TABLE comment
IS 'Tabelis hoitakse põhiressursside või andmeobjektide kohta esitatud kommentaare. Kommentaarid pole ühegi ressursi ega andmeobjekti ametlik kirjelduse koosseisu kuuluv info, vaid aitab kirjeldada ja lahti seletada ametlikku infot. Muuhulgas esitatakse ka kommentaaridena näiteks kooskõlastajate poolt tehtud märkused infosüsteemi kirjelduse kohta ja ka infosüsteemi omaniku endapoolsed kommentaarid kooskõlastajatele.';
COMMENT ON COLUMN comment.comment_id IS 'Kommentaari unikaalne ID.';
COMMENT ON COLUMN comment.comment_parent_id IS 'Kui on tegemist hierarhilise kommentaariumiga, siis viitab vanemale';
COMMENT ON COLUMN comment.creation_date IS 'Kirje tekitamise ajamoment.';
COMMENT ON COLUMN comment.modified_date IS 'Kirje viimase muutmise ajamoment.';
COMMENT ON COLUMN comment.infosystem_uuid IS 'InfoSystem uuid';
COMMENT ON COLUMN comment.comment IS 'Hinnangu või kommentaari sisu';
COMMENT ON COLUMN comment.author_name IS 'Hinnangu/kommentaari kasutaja nimi või muu identifikaator';
COMMENT ON COLUMN comment.author_personal_code IS 'Hinnangu/kommentaari kasutaja isikukood';
COMMENT ON COLUMN comment.organization_name IS 'Hinnangu/kommentaari kasutaja asutuse numetus';
COMMENT ON COLUMN comment.organization_code IS 'Hinnangu/kommentaari kasutaja asutuse kood';
COMMENT ON COLUMN comment.status IS 'Hinnangu staatus';
COMMENT ON COLUMN comment.type IS 'Hinnangu tüüp';
COMMENT ON COLUMN comment.title IS 'Hinnangu pealkiri';
COMMENT ON COLUMN comment.sub_type IS 'Hinnangu alamtüüp';
COMMENT ON COLUMN comment.resolution_type IS 'Hinnangu resolutsiooni tüüp';

-- GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE comment TO riha;

-- Table: large_object

-- DROP TABLE large_object;

CREATE TABLE IF NOT EXISTS large_object
(
  id integer NOT NULL,
  creation_date timestamp with time zone,
  data oid,
  hash character varying(255),
  length bigint,
  indexed boolean DEFAULT false NOT NULL,
  csv_search_content json NULL,
  CONSTRAINT large_object_pkey PRIMARY KEY (id)
);


-- GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE large_object TO riha;

-- Table: file_resource

-- DROP TABLE file_resource;

CREATE TABLE IF NOT EXISTS file_resource
(
  uuid uuid NOT NULL,
  content_type character varying(255),
  creation_date timestamp with time zone,
  name character varying(255),
  large_object_id integer NOT NULL,
  infosystem_uuid UUID NULL,
  CONSTRAINT file_resource_pkey PRIMARY KEY (uuid),
  CONSTRAINT fk_file_resource_large_object FOREIGN KEY (large_object_id)
  REFERENCES large_object (id)
  ON UPDATE NO ACTION
  ON DELETE NO ACTION
);


-- GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE file_resource TO riha;

-- Index: ixfk_comment_comment

-- DROP INDEX ixfk_comment_comment;

CREATE INDEX IF NOT EXISTS ixfk_comment_comment
  ON comment
  (comment_parent_id);


-- Table: main_resource_relation

-- DROP TABLE main_resource_relation;

CREATE TABLE IF NOT EXISTS main_resource_relation
(
  main_resource_relation_id INTEGER NOT NULL
    CONSTRAINT main_resource_relation_pkey
    PRIMARY KEY,
  creation_date             TIMESTAMP WITH TIME ZONE,
  infosystem_uuid           UUID,
  modified_date             TIMESTAMP WITH TIME ZONE,
  related_infosystem_uuid   UUID,
  type                      VARCHAR(255)
);
-- GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE main_resource_relation TO riha;

ALTER TABLE main_resource_relation
  ADD CONSTRAINT main_resource_relation_unique UNIQUE (infosystem_uuid, related_infosystem_uuid, type);

-- DROP INDEX idx_main_resource_relation_infosystem_uuid;
CREATE INDEX IF NOT EXISTS idx_main_resource_relation_infosystem_uuid
  ON main_resource_relation (infosystem_uuid);

-- DROP INDEX idx_main_resource_relation_related_infosystem_uuid;
CREATE INDEX IF NOT EXISTS idx_main_resource_relation_related_infosystem_uuid
  ON main_resource_relation (related_infosystem_uuid);

-- Table: registered_file

-- DROP TABLE IF EXISTS registered_file;

CREATE TABLE IF NOT EXISTS registered_file
(
  file_resource_uuid UUID,
  main_resource_uuid UUID,
  section            VARCHAR(150),
  CONSTRAINT registered_file_file_resource_uuid_main_resource_uuid_pk UNIQUE (file_resource_uuid, main_resource_uuid),
  CONSTRAINT registered_file_file_resource_uuid_fk FOREIGN KEY (file_resource_uuid) REFERENCES file_resource (uuid) ON DELETE CASCADE
);
COMMENT ON COLUMN registered_file.section
IS 'Main resource section that contains this file';
COMMENT ON TABLE registered_file
IS 'File resources that appear in main_resource json description';
