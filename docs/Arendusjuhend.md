Märkus. Ümber nimetatud repo `kirjeldusmoodul-rest-api` failist README.md - Priit P 27.04.2017

# Arendusjuhend


## Lahenduse kompileerimine

Tarkvara kompileerimine ning WAR paketi tegemine:

```bash
mvn package
```

Kompileeritud WAR paketi leiab `target/` kataloogist.

## Paigalduse häälestamine

### Kompileerimise käigus

Tarkvarasse vaikimisi kompileeritavaid parameetreid saab määrata maven'i parameetrite abil kompileerimise käigus.

Näiteks parameetrit `skipTests` saab määrata käsurealt kujul:

```bash
mvn package -DskipTests=true
```

Parameetreid saab esitada käsureal mitu, igaüks eraldi kujul (va tähtega märgistatud):
```
-D{parameeter}={väärtus}
```

Võimalik on kasutada järgmisi parameetreid (vaikeväärtused defineeritud `src/main/resources/riharest.project.properties` failis):

Parameeter               | Vaikeväärtus | Kirjeldus
-------------------------|--------------|----------
skipTests                | false        | Kui `true`, siis ühikteste ei käivitata
riharest.pathRoot        | /home/girf/  | Failitee, kuhu alla paigaldatakse RIHA dokumentidega seotud failid.
riharest.authService     | http://192.168.50.140:8080/riha/sessionManagementServlet | Autentimisteenuse URL, mille abil toimub sessioonitokeni valideerimine.
riharest.isTest          | true         | Kui väärtus on `true`, siis API aktsepteerib tokenina väärtust `testToken`, ilma seda valideerimata.
riharest.authTimeout     | 3600000      | Autentimise tokeni kehtivus millisekundites. Peale selle aja möödumist toimub REST teenuse poolt uuesti tokeni kontroll autentimisteenuse abil. 
riharest.pathRootWindows | C:\\Users\\Praktikant\\test_folder\\ | Failitee, kuhu alla paigaldatakse RIHA dokumentidega seotud failid. Kasutatakse juhul, kui lahendus on paigaldatud Windows'i operatsioonisüsteemiga arvutisse.
riharest.base.url *****  | http://192.168.50.106:8080/rest | URL, mille kaudu on RIHA REST API kättesaadav. Antud parameetri baasilt tekitatakse taastatavates vastustes URLid allalaaditavatele failidele.
riharest.jdbc.url *****  | jdbc:postgresql://192.168.50.106:5432/riha | RIHA andmebaasi JDBC andmebaasiühenduse URL
riharest.jdbc.user ***** | riha         | RIHA andmebaasi kasutajatunnus, kelle nimel peab REST API tegema andmebaasiühenduse
riharest.jdbc.password   | riha         | RIHA andmebaasi parool, kelle nimel peab REST API tegema andmebaasiühenduse 
jmeter.ignoreFailuers    | true         | Kui tõene, siis jMeter testskriptid ei peata tööd esimese ettetulnud vea peale, vaid töötavad lõpuni ning registreerivad kõik täitmise käigus juhtunud vead. Väära väärtuse korral registreeritakse ainult esimene juhtunud viga ning lõpetatakse töö.
jmeter.test              | rest-get-object | jMeter testi nimi, mis testifaasis tuleb käivitada. Võimalikud variandid vt kataloogis `src/test/jmeter` olevate failide nimed.


Märkused:

* Parameetritega antud JDBC andmebaasiühendust kasutatakse ainult juhul, kui rakendusele pole Tomcat konfiguratsioonis
kirjeldatud JNDI nime `jdbc/riharest/datasource`. Sellise JNDI nime leidumise korral kasutatakse andmebaasiühenduse 
loomiseks seda.
* Andmebaasiühenduse parameetreid kasutatakse ka ühiktestide käivitamiseks. Juhul, kui andmebaas pole tarkvara kompileerimise
ajal kättesaadav, tuleb ühikteste mitte käivitada (st kasutada parameetrit `skipTests=true`).

### Konfiguratsiooni faili abil

Kõrgema prioriteediga parameetrid võib määrata `/opt/tomcat/conf/riharest.project.properties` faili abil. Selle faili olemasolu ei ole kohustuslik ja võib sisaldada ainult väike osa konfiguratsioonist. Parameetrite prioriteet on kõrgem kui vaikeväärtustel.
