CREATE OR REPLACE FUNCTION infosystem_trg() RETURNS trigger AS
$BODY$
DECLARE
  p_staatus_kood CHARACTER VARYING;
  status CHARACTER VARYING;
  uus_id INTEGER;
  infosystem_kind INTEGER;
  kl_kind INTEGER;
  xml_kind INTEGER;
  inapproval BOOLEAN;
  old_inapproval BOOLEAN;
  kooskolastamine BOOLEAN;
  owner_id INTEGER;
  kooskolastamise_tyyp CHARACTER VARYING;
  kooskolastamine_id INTEGER;
BEGIN
  SELECT kind_id INTO infosystem_kind FROM kind WHERE name = 'infosystem';
  SELECT kind_id INTO kl_kind FROM kind WHERE name = 'classifier';
  SELECT kind_id INTO xml_kind FROM kind WHERE name = 'classifier';

  -- If object is infosystem and this is the current version:
  IF (new.kind_id = infosystem_kind OR new.kind_id = kl_kind OR new.kind_id = xml_kind) AND (new.end_date is null OR new.end_date <= current_timestamp) THEN
    kooskolastamine := FALSE;
    p_staatus_kood := NULL;
    IF new.kind_id = kl_kind THEN
      status := new.json_content->>'classifier_status';
    ELSEIF new.kind_id = xml_kind THEN
      status := new.json_content->>'status';
    ELSE
      status := new.json_content->>'infosystem_status';
    END IF;
    inapproval := cast(new.json_content->>'inapproval' as boolean);
    IF inapproval IS NULL THEN
      inapproval := FALSE;
    END IF;
    old_inapproval := inapproval;
    IF old IS NOT NULL THEN
      old_inapproval := cast(old.json_content->>'inapproval' as boolean);
      IF old_inapproval IS NULL THEN
        old_inapproval := FALSE;
      END IF;
    END IF;

    IF inapproval AND NOT old_inapproval THEN
      kooskolastamine := TRUE;
    END IF;

    IF status IS NOT NULL THEN
      IF new.kind_id = kl_kind THEN
        p_staatus_kood := 'KLASSIFIKAATOR_STAATUS_' || upper(status);
      ELSIF new.kind_id = xml_kind THEN
        p_staatus_kood := 'XMLVARA_STAATUS_' || upper(status);
      ELSE
        p_staatus_kood := 'INFOSYS_STAATUS_' || upper(status);
      END IF;
    ELSE
      RETURN new;
    END IF;

    SELECT asutus_id INTO owner_id FROM asutused.asutus WHERE registrikood = new.owner;

    -- IF tg_op = 'INSERT' THEN
    IF new.old_id IS NULL THEN
      IF new.kind_id = kl_kind THEN
        -- Insert new classifier record
        new.old_id := nextval('riha.klassifikaator_seq'::regclass);
        INSERT INTO klassifikaator (id, haldaja_asutus_id, klassifikaatori_seisund_kood, nimi, lyhend, kehtivusaeg_algus, uuendamissagedus, lyhiiseloomustus,
                                    versiooni_nr, staatus_kood, lisamise_kuupaev, on_juurdepaas_piirang, viitenumber)
          VALUES (new.old_id, owner_id, case new.state when 'N' then 'KLASSIFIKAATOR_SEISUND_PROJEKT' when 'D' then 'KLASSIFIKAATOR_SEISUND_KEHTETU' else 'KLASSIFIKAATOR_SEISUND_KEHTIV' end, new.name, new.short_name, new.start_date, 'vajadusel', ' ',
                  1, p_staatus_kood, current_timestamp, false, new.uri);
      ELSIF new.kind_id = xml_kind THEN
        -- Insert new infosysteem record
        new.old_id := nextval('riha.xmlvara_seq'::regclass);
        INSERT INTO xmlvara (id, versioon, nimetus_eesti_keeles, xmlvara_staatus_kood, publitseeritud, viitenumber, haldaja_asutus_id)
          VALUES (new.old_id, new.version, new.name, p_staatus_kood, false, new.uri, owner_id);
      ELSE
        -- Insert new infosysteem record
        new.old_id := nextval('riha.infosysteem_seq'::regclass);
        INSERT INTO infosysteem (id, staatus_kood, nimi, lyhinimi, kategooria) VALUES (new.old_id, p_staatus_kood, new.name, coalesce(new.short_name, '-'), 'INFOSYSTEEM_KATEGOORIA_XTEE');
      END IF;
    ELSE
      IF new.kind_id = kl_kind THEN
        -- Update infosysteem record
        UPDATE klassifikaator SET staatus_kood = p_staatus_kood, nimi = new.name, lyhend = new.short_name WHERE id = new.old_id;
      ELSIF new.kind_id = xml_kind THEN
        -- Update infosysteem record
        UPDATE xmlvara SET staatus_kood = p_staatus_kood, nimetus_eesti_keeles = new.name WHERE id = new.old_id;
      ELSE
        -- Update infosysteem record
        UPDATE infosysteem SET staatus_kood = p_staatus_kood, nimi = new.name, lyhinimi = coalesce(new.short_name,'-') WHERE id = new.old_id;
      END IF;
    END IF;

    IF kooskolastamine THEN
      kooskolastamise_tyyp := null;
      IF new.kind_id = kl_kind THEN
        -- sisestamisel,kehtestamisel,kehtestatud,lopetamisel,lopetatud
        CASE status
          WHEN 'kehtestamisel' THEN kooskolastamise_tyyp := 'KLA_KOOSKOLASTAMISE_TYYP_KEHTESTAMISE_KOOSKOLASTAMINE';
          WHEN 'lopetamisel' THEN kooskolastamise_tyyp := 'KLA_KOOSKOLASTAMISE_TYYP_LOPETAMISE_KOOSKOLASTAMINE';
        ELSE RAISE EXCEPTION 'Klassifikaatori olek ei vasta kooskõlastamistegevusele' USING ERRCODE = 'RRT02';
        END CASE;
      ELSIF new.kind_id = xml_kind THEN
        -- kava_sisestamisel,kava_kooskolastamisel,kava_registreerimisel,
        -- ettepanek_sisestamisel,ettepanek_kooskolastamisel,ettepanek_registreerimisel,registreeritud,
        -- lopetamine_kooskolastamisel,lopetamine_registreerimisel,lopetatud
        CASE status
          WHEN 'kava_kooskolastamisel' THEN kooskolastamise_tyyp := 'XML_KOOSKOLASTAMISE_TYYP_KAVA_KOOSKOLASTAMINE';
          WHEN 'kava_registreerimisel' THEN kooskolastamise_tyyp := 'XML_KOOSKOLASTAMISE_TYYP_KAVA_REGISTREERIMINE';
          WHEN 'ettepanek_kooskolastamisel' THEN kooskolastamise_tyyp := 'XML_KOOSKOLASTAMISE_TYYP_ETTEPANEKU_KOOSKOLASTAMINE';
          WHEN 'ettepanek_registreerimisel' THEN kooskolastamise_tyyp := 'XML_KOOSKOLASTAMISE_TYYP_ETTEPANEKU_REGISTREERIMINE';
          WHEN 'lopetamine_kooskolastamisel' THEN kooskolastamise_tyyp := 'XML_KOOSKOLASTAMISE_TYYP_LOPETAMISE_KOOSKOLASTAMINE';
          WHEN 'lopetamine_registreerimisel' THEN kooskolastamise_tyyp := 'XML_KOOSKOLASTAMISE_TYYP_LOPETAMISE_REGISTREERIMINE';
        ELSE RAISE EXCEPTION 'XML vara olek ei vasta kooskõlastamistegevusele' USING ERRCODE = 'RRT03';
        END CASE;
      ELSE
        -- ei_asutata,asutamine_sisestamisel,asutamine_kooskolastamisel,asutamine_kooskolastatud,
        -- kasutusele_votmise_kooskolastamisel,kasutusele_votmise_registreerimisel,kasutusel,
        -- lopetamise_kooskolastamisel,lopetamise_registreerimisel,lopetatud
        CASE status
          WHEN 'asutamine_kooskolastamisel' THEN kooskolastamise_tyyp := 'INF_KOOSKOLASTAMISE_TYYP_ASUTAMINE';
          WHEN 'kasutusele_votmise_kooskolastamisel' THEN kooskolastamise_tyyp := 'INF_KOOSKOLASTAMISE_TYYP_KASUTUSELE_VOTMINE';
          WHEN 'kasutusel' THEN kooskolastamise_tyyp := 'INF_KOOSKOLASTAMISE_TYYP_KOOSSEISU_MUUTMINE';
          WHEN 'lopetamise_kooskolastamisel' THEN kooskolastamise_tyyp := 'INF_KOOSKOLASTAMISE_TYYP_LOPETAMINE';
        ELSE RAISE EXCEPTION 'Infosüsteemi olek ei vasta kooskõlastamistegevusele' USING ERRCODE = 'RRT01';
        END CASE;
      END IF;

      -- Insert record to kooskolastamine table:
      kooskolastamine_id := nextval('riha.kooskolastamine_seq'::regclass);
      IF new.kind_id = kl_kind THEN
        INSERT INTO kooskolastamine (id, klassifikaator_id, kuupaev, kooskolastamise_tyyp_kood, esitaja_asutus_id)
          VALUES (kooskolastamine_id, new.old_id, current_timestamp, kooskolastamise_tyyp, owner_id);

        INSERT INTO kooskolastav_asutus_ajalugu (kooskolastamine_id, asutus_id)
          SELECT kooskolastamine_id, asutus_id FROM kooskolastav_asutus
          WHERE kooskolastamise_tyyp_kood = 'ASUTUS_KOOSKOLASTAMISE_TYYP_KLASSIFIKAATOR' AND
                kuupaev_alates <= current_timestamp AND
                (kuupaev_kuni IS NULL OR kuupaev_kuni > current_timestamp);
      ELSIF new.kind_id = xml_kind THEN
        INSERT INTO kooskolastamine (id, xmlvara_id, kuupaev, kooskolastamise_tyyp_kood, esitaja_asutus_id)
        VALUES (kooskolastamine_id, new.old_id, current_timestamp, kooskolastamise_tyyp, owner_id);

        INSERT INTO kooskolastav_asutus_ajalugu (kooskolastamine_id, asutus_id)
          SELECT kooskolastamine_id, asutus_id FROM kooskolastav_asutus
          WHERE kooskolastamise_tyyp_kood = 'ASUTUS_KOOSKOLASTAMISE_TYYP_XMLVARA' AND
                kuupaev_alates <= current_timestamp AND
                (kuupaev_kuni IS NULL OR kuupaev_kuni > current_timestamp);
      ELSE
        INSERT INTO kooskolastamine (id, infosysteem_id, kuupaev, kooskolastamise_tyyp_kood, esitaja_asutus_id)
        VALUES (kooskolastamine_id, new.old_id, current_timestamp, kooskolastamise_tyyp, owner_id);

        INSERT INTO kooskolastav_asutus_ajalugu (kooskolastamine_id, asutus_id)
          SELECT kooskolastamine_id, asutus_id FROM kooskolastav_asutus
          WHERE kooskolastamise_tyyp_kood = 'ASUTUS_KOOSKOLASTAMISE_TYYP_INFOSYSTEEM' AND
                kuupaev_alates <= current_timestamp AND
                (kuupaev_kuni IS NULL OR kuupaev_kuni > current_timestamp);
      END IF;
    END IF;
  END IF;
  RETURN new;
END
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

DROP TRIGGER IF EXISTS tr_infosystem_update ON main_resource;
CREATE TRIGGER tr_infosystem_update AFTER UPDATE ON main_resource FOR EACH ROW EXECUTE PROCEDURE infosystem_trg();
