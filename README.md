# RIHA-Storage
Andmehoidja - RIHA sisemine püsimäluteenus | RIHA internal storage service 

Vt:
- [Andmehoidja](https://arhitektuur.riha.ee/Andmehoidja), ülevaatlik kirjeldus arhitektuuriteatmikus
- [RIHA-Storage API ](docs/RIHA-Storage-API.md), Andmehoidja poolt pakutava API spetsifikatsioon
- [arendusjuhend](docs/Arendusjuhend), teave paigaldamise kohta.

## Senine arendustöö

RIHA-Storage aluseks on eelmises arendusjärgus loodud "Kirjeldusmooduli" serveripoolse komponendi (Kirjeldusmooduli REST API). Kood tarniti reposse `kirjeldusmoodul-rest-api` (mitteavalik repo). Dokumentatsioon tarniti zip-tult ja on üles pandud mitteavalikus RIA keskkonnas. Reposse `RIHA-Storage` on koondatud kood repost `kirjeldusmoodul-rest-api` ja asjassepuutuv, varem eraldiseisev dokumentatsioon. Andmesalvestuse üldine arhitektuuriline kirjeldus on kantud arhitektuuriteatmikku, lehele [Andmehoidja](https://e-gov.github.io/RIHA-Index/Andmehoidja).

## Järgmised tööd 

1 RIHA-Storage ühitamine komponentidega `RIHA-Producer`, `RIHA-Approver`, `RIHA-Browser`, `RIHA-Publisher`

_Praegu salvestavad nimetatud neli komponenti andmeid JSON-failidesse. Failid tuleb asendada andmebaasi kasutamisega RIHA-Storage kaudu._

1.1 Vastavalt tuleb RIHA-Storage API-t täiendada.

1.2 Ühtlasi tuleb API-st eemaldada mittevajalikud osad.

_Üks mittevajalik osa on pääsuhaldus. Pääsuhaldus lahendatakse komponentides `RIHA-Producer`, `RIHA-Approver`, `RIHA-Browser` ja `RIHA-Publisher`. Pöördumisi nimetatud komponentidest peab `RIHA-Storage` usaldama._ 




