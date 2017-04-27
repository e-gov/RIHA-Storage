# Andmehoidja (RIHA-Storage)

__Andmehoidja__,  tehnilise nimetusega __RIHA-Storage__, on serveriteenus (e RIHA backend komponent), mis korraldab andmete püsihoidmist. Andmehoidla teenindab oma API kaudu RIHA teisi serveriteenuseid, olles vahendajaks PostgreSQL andmebaasi ja HTTPS päringute vahel.

Kõige olulisemad dokumendid:

- [Andmehoidja](https://arhitektuur.riha.ee/Andmehoidja), ülevaatlik kirjeldus arhitektuuriteatmikus (repos RIHA-Index)
- [RIHA-Storage API ](docs/RIHA-Storage-API.md), Andmehoidja poolt pakutava API spetsifikatsioon (siinses repos)
- [arendusjuhend](docs/Arendusjuhend), teave paigaldamise kohta (siinses repos).

## Senine arendustöö

RIHA-Storage aluseks on eelmises arendusjärgus loodud "Kirjeldusmooduli" serveripoolne komponent. Seda komponenti nimetati ka "Kirjeldusmooduli REST API" või lihtsalt "RIHA REST API".

Reposse `RIHA-Storage` on koondatud kood repost `kirjeldusmoodul-rest-api` ja asjassepuutuv, varem eraldiseisev dokumentatsioon. Dokumentatsiooni on toimetatud, kood on arendaja poolt üleantud kujul.

## Järgmised tööd 

1 RIHA-Storage ühitamine komponentidega `RIHA-Producer`, `RIHA-Approver`, `RIHA-Browser`, `RIHA-Publisher`. _Praegu salvestavad nimetatud neli komponenti andmeid JSON-failidesse. Failid tuleb asendada andmebaasi kasutamisega RIHA-Storage kaudu._

1.1 RIHA-Storage API ülevaatamine ja täiendamine.

1.2 Mittevajalike osade eemaldamine API-st. _Üks mittevajalik osa on pääsuhaldus. Pääsuhaldus lahendatakse komponentides `RIHA-Producer`, `RIHA-Approver`, `RIHA-Browser` ja `RIHA-Publisher`. Pöördumisi nimetatud komponentidest peab `RIHA-Storage` usaldama._ 

1.3 Eelmistele punktidele vastavad muudatused komponendi koodis ja andmebaasi struktuuris.

Täpsemalt vt Issues.


