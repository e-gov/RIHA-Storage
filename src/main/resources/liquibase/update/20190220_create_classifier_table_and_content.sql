CREATE TABLE riha.classifier
(
	id  							INTEGER NOT NULL,
	type              VARCHAR(150) NOT NULL,
	code              VARCHAR(150) NOT NULL,
	value             VARCHAR(150),
	json_value				jsonb,
	discriminator     VARCHAR(150) NOT NULL,
	description       VARCHAR(255),
	creation_date     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
	modified_date     TIMESTAMP WITH TIME ZONE,
	CONSTRAINT pk_classifier UNIQUE (id)
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE riha.classifier TO riha;

COMMENT ON COLUMN riha.classifier.id IS 'Klassifikaatori ID';
COMMENT ON COLUMN riha.classifier.type IS 'Klassifikaatori tüüp';
COMMENT ON COLUMN riha.classifier.code IS 'Klassifikaatori kood';
COMMENT ON COLUMN riha.classifier.value IS 'Tekst tüüpi klassifikaatori väärtus';
COMMENT ON COLUMN riha.classifier.json_value IS 'JSON tüüpi klassifikaatori väärtus';
COMMENT ON COLUMN riha.classifier.discriminator IS 'Klassifikaatori väärtuse tüüp (TEXT/JSON)';
COMMENT ON COLUMN riha.classifier.description IS 'Klassifikaatori kirjeldus';
COMMENT ON COLUMN riha.classifier.creation_date IS 'Kirje loomise ajamoment';
COMMENT ON COLUMN riha.classifier.modified_date IS 'Kirje muutmise ajamoment';

CREATE SEQUENCE riha.classifier_seq
	INCREMENT 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
CACHE 1;
GRANT SELECT, USAGE ON SEQUENCE riha.classifier_seq TO riha;

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'system_status', 'IN_USE', 'IN_USE', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'system_status', 'ESTABLISHING', 'ESTABLISHING', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'system_status', 'FINISHED', 'FINISHED', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'development_status', 'IN_DEVELOPMENT', 'IN_DEVELOPMENT', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'development_status', 'NOT_IN_DEVELOPMENT', 'NOT_IN_DEVELOPMENT', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'x_road_status', 'JOINED', 'JOINED', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'x_road_status', 'NOT_JOINED', 'NOT_JOINED', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'relation_type', 'SUB_SYSTEM', 'SUB_SYSTEM', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'relation_type', 'SUPER_SYSTEM', 'SUPER_SYSTEM', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'relation_type', 'USED_SYSTEM', 'USED_SYSTEM', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'relation_type', 'USER_SYSTEM', 'USER_SYSTEM', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'event_type', 'CLOSED', 'CLOSED', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'event_type', 'DECISION', 'DECISION', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'issue_type', 'ESTABLISHMENT_REQUEST', 'ESTABLISHMENT_REQUEST', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'issue_type', 'TAKE_INTO_USE_REQUEST', 'TAKE_INTO_USE_REQUEST', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'issue_type', 'MODIFICATION_REQUEST', 'MODIFICATION_REQUEST', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'issue_resolution_type', 'POSITIVE', 'POSITIVE', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'issue_resolution_type', 'NEGATIVE', 'NEGATIVE', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'issue_resolution_type', 'DISMISSED', 'DISMISSED', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'audit_resolution_type', 'PASSED_WITHOUT_REMARKS', 'PASSED_WITHOUT_REMARKS', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'audit_resolution_type', 'PASSED_WITH_REMARKS', 'PASSED_WITH_REMARKS', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'audit_resolution_type', 'DID_NOT_PASS', 'DID_NOT_PASS', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'security_standard', 'iske', 'ISKE', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'security_level', 'high', 'H', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'security_level', 'medium', 'M', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'security_level', 'low', 'L', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'system_check_status', 'CANCELLED', 'CANCELLED', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'system_check_status', 'PENDING', 'PENDING', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'system_check_status', 'IN_PROGRESS', 'IN_PROGRESS', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'system_check_status', 'FAILED', 'FAILED', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'system_check_status', 'PASSED', 'PASSED', 'TEXT');

INSERT INTO riha.classifier (id, type, code, json_value, discriminator) VALUES (nextval('riha.classifier_seq'), 'access_restriction_reasons', 'CODE_18', '{"code": 18, "legislation": "AvTS § 35 lg 1 p 9", "description": "Teave turvasüsteemide, turvaorganisatsiooni või turvameetmete kirjelduse kohta"}', 'JSON');
INSERT INTO riha.classifier (id, type, code, json_value, discriminator) VALUES (nextval('riha.classifier_seq'), 'access_restriction_reasons', 'CODE_19', '{"code": 19, "legislation": "AvTS § 35 lg 1 p 10", "description": "Tehnoloogilisi lahendusi sisaldav teave"}', 'JSON');
INSERT INTO riha.classifier (id, type, code, json_value, discriminator) VALUES (nextval('riha.classifier_seq'), 'access_restriction_reasons', 'CODE_36', '{"code": 36, "legislation": "AvTS § 35 lg 1 p 17", "description": "Ärisaladus"}', 'JSON');
INSERT INTO riha.classifier (id, type, code, json_value, discriminator) VALUES (nextval('riha.classifier_seq'), 'access_restriction_reasons', 'CODE_38', '{"code": 38, "legislation": "AvTS § 35 lg 1 p 18 (1)", "description": "Elutähtsa teenuse riskianalüüsi ja toimepidevuse plaani puudutav teave"}', 'JSON');
INSERT INTO riha.classifier (id, type, code, json_value, discriminator) VALUES (nextval('riha.classifier_seq'), 'access_restriction_reasons', 'CODE_39', '{"code": 39, "legislation": "AvTS § 35 lg 2 p", "description": "Õigusaktide eelnõud enne nende kooskõlastamiseks saatmist või vastuvõtmiseks esitamist"}', 'JSON');
INSERT INTO riha.classifier (id, type, code, json_value, discriminator) VALUES (nextval('riha.classifier_seq'), 'access_restriction_reasons', 'CODE_40', '{"code": 40, "legislation": "AvTS § 35 lg 2 p 2", "description": "Dokumendi kavand ja selle juurde kuuluvad dokumendid"}', 'JSON');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'topics_that_do_not_require_feedback_on_creation', 'X_ROAD_SUBSYSTEM', 'x-tee alamsüsteem', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'topics_that_do_not_require_feedback_on_creation', 'STANDARD_SOLUTION', 'standardlahendus', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'topics_that_do_not_require_feedback_on_creation', 'INTERNAL_USAGE', 'asutusesiseseks kasutamiseks', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'topics_that_do_not_require_feedback_on_creation', 'DOCUMENT_MANAGEMENT_SYSTEM', 'dokumendihaldussüsteem', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'document_types', 'END_USER_VIEW_REFERENCE', 'Viide lõppkasutaja vaatele', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'document_types', 'DATA_REFERENCE', 'Viide (ava)andmetele', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'document_types', 'USER_DOCUMENTATION', 'Kasutaja dokumentatsioon', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'document_types', 'ARCHITECTURE_DOCUMENT', 'Arhitektuuridokument', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'document_types', 'AVAILABLE_CLASSIFIER', 'Kasutatav klassifikaator', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'document_types', 'SOURCE_CODE', 'Lähtekood', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'document_types', 'ISKE_SECURITY_CLASS_ASSIGNMENT_ACT', 'ISKE turbeosaklasside määramise akt', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'document_types', 'ISKE_AUDIT_SUMMARY', 'ISKE auditi kokkuvõte', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'document_types', 'DATA_PROTECTION_IMPACT_ASSESSMENT', 'Andmekaitseline mõjuhinnang', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'document_types', 'OTHER_TECHNICAL_DOCUMENTATION', 'Muu tehniline dokumentatsioon', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'legislation_types', 'INFO_SYSTEM_STATUTE_DRAFT', 'Infosüsteemi põhimääruse kavand/eelnõu', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'legislation_types', 'INFO_SYSTEM_STATUTE_DRAFT_HEADNOTE', 'Infosüsteemi põhimääruse kavandi/eelnõu seletuskiri', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'legislation_types', 'INFO_SYSTEM_STATUTE', 'Infosüsteemi põhimäärus', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'legislation_types', 'INFO_SYSTEM_STATUTE_HEADNOTE', 'Infosüsteemi põhimääruse seletuskiri', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'legislation_types', 'OTHER_INFO_SYSTEM_REGULATORY_LEGAL_ACT', 'Infosüsteemi reguleeriv muu õigusakt', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'legislation_types', 'OTHER_LEGAL_HEADNOTE', 'Muu õiguslik seletuskiri', 'TEXT');
