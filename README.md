# Andmehoidja (RIHA-Storage)

__Andmehoidja__,  tehnilise nimetusega __RIHA-Storage__, on serveriteenus (e RIHA _backend_ komponent), mis korraldab andmete püsihoidmist. Andmehoidla teenindab oma API kaudu RIHA teisi serveriteenuseid, olles vahendajaks PostgreSQL andmebaasi ja HTTPS päringute vahel.

**Dokumentatsioon.** RIHA-Storage dokumentatsiooni hoitakse koos koodiga, siin repos. Dokumentatsiooni osad on:

1. [RIHA-Storage API spetsifikatsioon](docs/RIHA-Storage-API.md)
2. [RIHA andmebaasi kontseptuaalmudel](docs/RIHA-Storage-Conceptual.md)
  a. Andmemudel (Enterprise Architect failina)
    - `RIHA-Andmebaas (VANA)` - RIHA vana andmebaasi mudel (palju tabeleid)
    - `RIHA-Storage` PostgreSQL andmebaasi mudel
3. Kommenteeritud SQL-skriptid  
4. [Arendusjuhend](docs/Arendusjuhend) - dokument kirjeldab komponendi paigaldamisse ja arendamisse puutuvat
5. koodis olevad kommentaarid.

Andmehoidja lühike kirjeldus on ka RIHA arhitektuuriteatmikus: [Andmehoidja](https://arhitektuur.riha.ee/Andmehoidja) (repos RIHA-Index).

## Senine arendustöö

RIHA-Storage aluseks on eelmises arendusjärgus loodud "Kirjeldusmooduli" serveripoolne komponent. Seda komponenti nimetati ka "Kirjeldusmooduli REST API" või lihtsalt "RIHA REST API".

Reposse `RIHA-Storage` on koondatud kood repost `kirjeldusmoodul-rest-api` ja asjassepuutuv, varem eraldiseisev dokumentatsioon. Dokumentatsiooni on toimetatud, kood on arendaja poolt üleantud kujul.

## Järgmised tööd 

1 RIHA-Storage ühitamine komponentidega `RIHA-Producer`, `RIHA-Approver`, `RIHA-Browser`, `RIHA-Publisher`. _Praegu salvestavad nimetatud neli komponenti andmeid JSON-failidesse. Failid tuleb asendada andmebaasi kasutamisega RIHA-Storage kaudu._

1.1 RIHA-Storage API ülevaatamine ja täiendamine.

1.2 Mittevajalike osade eemaldamine API-st. _Üks mittevajalik osa on pääsuhaldus. Pääsuhaldus lahendatakse komponentides `RIHA-Producer`, `RIHA-Approver`, `RIHA-Browser` ja `RIHA-Publisher`. Pöördumisi nimetatud komponentidest peab `RIHA-Storage` usaldama._ 

1.3 Eelmistele punktidele vastavad muudatused komponendi koodis ja andmebaasi struktuuris.

Täpsemalt vt Issues.


