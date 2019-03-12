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

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'system_status', 'IN_USE', 'kasutusel', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'system_status', 'ESTABLISHING', 'asutamisel', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'system_status', 'FINISHED', 'lõpetatud', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'development_status', 'IN_DEVELOPMENT', 'aktiivses arenduses', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'development_status', 'NOT_IN_DEVELOPMENT', 'ei ole arenduses', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'x_road_status', 'JOINED', 'liidestatud', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'x_road_status', 'NOT_JOINED', 'ei ole liidestatud', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'relation_type', 'SUB_SYSTEM', 'alaminfosüsteem', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'relation_type', 'SUPER_SYSTEM', 'üleminfosüsteem', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'relation_type', 'USED_SYSTEM', 'kasutatav standardlahendus', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'relation_type', 'USER_SYSTEM', 'standardlahenduse kasutaja', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'event_type', 'CLOSED', 'CLOSED', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'event_type', 'DECISION', 'DECISION', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'issue_type', 'ESTABLISHMENT_REQUEST', 'Infosüsteemi asutamine', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'issue_type', 'TAKE_INTO_USE_REQUEST', 'Infosüsteemi kasutusele võtmine', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'issue_type', 'MODIFICATION_REQUEST', 'Andmekoosseisu muutmine', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'issue_type', 'FINALIZATION_REQUEST', 'Infosüsteemi lõpetamine', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'issue_resolution_type', 'POSITIVE', 'Kooskõlastan', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'issue_resolution_type', 'NEGATIVE', 'Ei kooskõlasta', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'issue_resolution_type', 'DISMISSED', 'Jätan läbi vaatamata', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'audit_resolution_type', 'PASSED_WITHOUT_REMARKS', 'Auditeeritud märkusteta ja soovitusteta', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'audit_resolution_type', 'PASSED_WITH_REMARKS', 'Auditeeritud märkuste või soovitustega', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'audit_resolution_type', 'DID_NOT_PASS', 'Ei läbinud auditit', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'security_standard', 'ISKE', 'ISKE', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'security_level', 'H', 'Kõrge', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'security_level', 'M', 'Keskmine', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'security_level', 'L', 'Madal', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'system_check_status', 'CANCELLED', 'CANCELLED', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'system_check_status', 'PENDING', 'PENDING', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'system_check_status', 'IN_PROGRESS', 'IN_PROGRESS', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'system_check_status', 'FAILED', 'FAILED', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'system_check_status', 'PASSED', 'PASSED', 'TEXT');

INSERT INTO riha.classifier (id, type, code, json_value, discriminator) VALUES (nextval('riha.classifier_seq'), 'access_restriction_reasons', '18', '{"code": 18, "legislation": "AvTS § 35 lg 1 p 9", "description": "Teave turvasüsteemide, turvaorganisatsiooni või turvameetmete kirjelduse kohta"}', 'JSON');
INSERT INTO riha.classifier (id, type, code, json_value, discriminator) VALUES (nextval('riha.classifier_seq'), 'access_restriction_reasons', '19', '{"code": 19, "legislation": "AvTS § 35 lg 1 p 10", "description": "Tehnoloogilisi lahendusi sisaldav teave"}', 'JSON');
INSERT INTO riha.classifier (id, type, code, json_value, discriminator) VALUES (nextval('riha.classifier_seq'), 'access_restriction_reasons', '36', '{"code": 36, "legislation": "AvTS § 35 lg 1 p 17", "description": "Ärisaladus"}', 'JSON');
INSERT INTO riha.classifier (id, type, code, json_value, discriminator) VALUES (nextval('riha.classifier_seq'), 'access_restriction_reasons', '38', '{"code": 38, "legislation": "AvTS § 35 lg 1 p 18 (1)", "description": "Elutähtsa teenuse riskianalüüsi ja toimepidevuse plaani puudutav teave"}', 'JSON');
INSERT INTO riha.classifier (id, type, code, json_value, discriminator) VALUES (nextval('riha.classifier_seq'), 'access_restriction_reasons', '39', '{"code": 39, "legislation": "AvTS § 35 lg 2 p", "description": "Õigusaktide eelnõud enne nende kooskõlastamiseks saatmist või vastuvõtmiseks esitamist"}', 'JSON');
INSERT INTO riha.classifier (id, type, code, json_value, discriminator) VALUES (nextval('riha.classifier_seq'), 'access_restriction_reasons', '40', '{"code": 40, "legislation": "AvTS § 35 lg 2 p 2", "description": "Dokumendi kavand ja selle juurde kuuluvad dokumendid"}', 'JSON');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'topics_that_do_not_require_feedback_on_creation', 'X_ROAD_SUBSYSTEM', 'x-tee alamsüsteem', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'topics_that_do_not_require_feedback_on_creation', 'INTERNAL_USAGE', 'asutusesiseseks kasutamiseks', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'topics_that_do_not_require_feedback_on_creation', 'DOCUMENT_MANAGEMENT_SYSTEM', 'dokumendihaldussüsteem', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'document_types', 'DOC_TYPE_USER_VIEW', 'Viide lõppkasutaja vaatele', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'document_types', 'DOC_TYPE_OPEN_DATA', 'Viide (ava)andmetele', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'document_types', 'DOC_TYPE_USER_GUIDE', 'Kasutaja dokumentatsioon', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'document_types', 'DOC_TYPE_ARCHITECTURE', 'Arhitektuuridokument', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'document_types', 'DOC_TYPE_CLASSIFICATOR', 'Kasutatav klassifikaator', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'document_types', 'DOC_TYPE_SOURCE_CODE', 'Lähtekood', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'document_types', 'DOC_TYPE_ISKE_ACT', 'ISKE turbeosaklasside määramise akt', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'document_types', 'DOC_TYPE_ISKE_SUMMARY', 'ISKE auditi kokkuvõte', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'document_types', 'DOC_TYPE_GDPR_IMPACT_ASSESSMENT', 'Andmekaitseline mõjuhinnang', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'document_types', 'DOC_TYPE_OTHER', 'Muu tehniline dokumentatsioon', 'TEXT');

INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'legislation_types', 'LEGAL_TYPE_DRAFT_STATUTE', 'Infosüsteemi põhimääruse kavand/eelnõu', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'legislation_types', 'LEGAL_TYPE_DRAFT_STATUTE_NOTE', 'Infosüsteemi põhimääruse kavandi/eelnõu seletuskiri', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'legislation_types', 'LEGAL_TYPE_STATUTE', 'Infosüsteemi põhimäärus', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'legislation_types', 'LEGAL_TYPE_STATUTE_NOTE', 'Infosüsteemi põhimääruse seletuskiri', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'legislation_types', 'LEGAL_TYPE_OTHER_REGULATION', 'Infosüsteemi reguleeriv muu õigusakt', 'TEXT');
INSERT INTO riha.classifier (id, type, code, value, discriminator) VALUES (nextval('riha.classifier_seq'), 'legislation_types', 'LEGAL_TYPE_OTHER', 'Muu õiguslik seletuskiri', 'TEXT');
