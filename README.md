# Andmehoidja (RIHA-Storage)

__Andmehoidja__,  tehnilise nimetusega __RIHA-Storage__, on serveriteenus (e RIHA backend komponent), mis korraldab andmete püsihoidmist. Andmehoidla teenindab oma API kaudu RIHA teisi serveriteenuseid, olles vahendajaks PostgreSQL andmebaasi ja HTTPS päringute vahel.

Kõige olulisemad dokumendid:

- [Andmehoidja](https://arhitektuur.riha.ee/Andmehoidja), ülevaatlik kirjeldus arhitektuuriteatmikus (repos RIHA-Index)
- [RIHA-Storage API ](docs/RIHA-Storage-API.md), Andmehoidja poolt pakutava API spetsifikatsioon (siinses repos)
- [arendusjuhend](docs/Arendusjuhend), teave paigaldamise kohta (siinses repos).

## Senine arendustöö

RIHA-Storage aluseks on eelmises arendusjärgus loodud "Kirjeldusmooduli" serveripoolne komponent. Seda kompnenti nimetati ka "Kirjeldusmooduli REST API" või lihsalt "RIHA REST API".

Reposse `RIHA-Storage` on koondatud kood repost `kirjeldusmoodul-rest-api` ja asjassepuutuv, varem eraldiseisev dokumentatsioon. Dokumentatsiooni on toimetatud, kood on arendaja poolt üleantud kujul.

## Järgmised tööd 

1 RIHA-Storage ühitamine komponentidega `RIHA-Producer`, `RIHA-Approver`, `RIHA-Browser`, `RIHA-Publisher`

_Praegu salvestavad nimetatud neli komponenti andmeid JSON-failidesse. Failid tuleb asendada andmebaasi kasutamisega RIHA-Storage kaudu._

1.1 Vastavalt tuleb RIHA-Storage API-t täiendada.

1.2 Ühtlasi tuleb API-st eemaldada mittevajalikud osad.

_Üks mittevajalik osa on pääsuhaldus. Pääsuhaldus lahendatakse komponentides `RIHA-Producer`, `RIHA-Approver`, `RIHA-Browser` ja `RIHA-Publisher`. Pöördumisi nimetatud komponentidest peab `RIHA-Storage` usaldama._ 




