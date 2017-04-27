# RIHA-Storage
Andmehoidja - RIHA sisemine püsimäluteenus | RIHA internal storage service 

Vt:
- [arhitektuurikirjeldus](https://arhitektuur.riha.ee/Andmehoidja) (arhitektuuriteatmikus)
- [arendusjuhend](docs/Arendusjuhend) (siinses repos)

## Tööde plaan

Aluseks võtame eelmises arendusjärgus loodud "Kirjeldusmooduli" serveripoolse komponendi (Kirjeldusmooduli REST API). Kood tarniti reposse `kirjeldusmoodul-rest-api` (mitteavalik repo). Dokumentatsioon tarniti zip-tult ja on üles pandud mitteavalikus RIA keskkonnas.

Reposse `RIHA-Storage` on koondatud kood repost `kirjeldusmoodul-rest-api` ja asjassepuutuv, varem eraldiseisev dokumentatsioon. 

Andmesalvestuse üldine arhitektuuriline kirjeldus on kantud arhitektuuriteatmikku, lehele [Andmehoidja](https://e-gov.github.io/RIHA-Index/Andmehoidja).

__RIHA-Storage ühitamine komponentidega RIHA-Producer, RIHA-Approver, RIHA-Browser, RIHA-Publisher__

Praegu salvestavad nimetatud neli komponenti andmeid JSON-failidesse. Failid tuleb asendada andmebaasi kasutamisega RIHA-Storage kaudu.

Vastavalt tuleb RIHA-Storage API-t täiendada.

Ühtlasi tuleb API-st eemaldada mittevajalikud osad.

Üks mittevajalik osa on pääsuhaldus. Pääsuhaldus lahendatakse komponentides RIHA-Producer, RIHA-Approver, RIHA-Browser ja RIHA-Publisher. Pöördumisi nimetatud komponentidest peab RIHA-Storage usaldama. 




