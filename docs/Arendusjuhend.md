# Arendusjuhend

## Eeldused

- PostgreSQL 9.6 andmebaas
- Tomcat 8
- Ubuntu 18.04
- OpenJDK 1.8
- Maven (testitud 3.3.9 peal) 

## Andmebaasi paigaldamine

1. Paigaldada PostgreSQL versioonil 9.6 töötav andmebaas
2. Andmebaasi vajalikud tabelid tekitatakse Liquibase'iga, mis käivitatakse rakendust käivitades _runtime_ ajal. _Runtime_ jooksul peab Liquibase saama ligi paigaldatud andmebaasile. Paigaldatud andmebaasi konfiguratsioon tuleb lisada siia: [riharest.project.properties konfiguratsioonifaili](https://github.com/e-gov/RIHA-Storage/blob/develop/src/main/resources/riharest.project.properties)
3. Andmebaasiühenduse konfigureeritavate parameetrite kohta leiab infot peatükist ["Paigalduse häälestamine"](#konfiguratsioon)
4. Andmebaasis peab leiduma skeem nimega **_riha_** ning see peab olema antud kasutaja
vaikimisi skeemiks. Vajalikul kujul andmebaas ja kasutajatunnus tuleb tekitada eraldi käsitsi 
andmebaasihalduse tarkvara abil. Muud täiendavad nõudeid tekitatud andmebaasile puuduvad.

## Andmebaasi uuendamine

## Lahenduse kompileerimine

Tarkvara kompileerimine ning WAR paketi tegemine:

```bash
mvn package
```

Kompileeritud WAR paketi leiab `target/` kataloogist.

<a name="konfiguratsioon"></a>
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
riharest.jdbc.url *****  | jdbc:postgresql://192.168.50.106:5432/riha | RIHA andmebaasi JDBC andmebaasiühenduse URL
riharest.jdbc.user ***** | riha         | RIHA andmebaasi kasutajatunnus, kelle nimel peab REST API tegema andmebaasiühenduse
riharest.jdbc.password   | riha         | RIHA andmebaasi parool, kelle nimel peab REST API tegema andmebaasiühenduse 

Märkused:

- Parameetritega antud JDBC andmebaasiühendust kasutatakse ainult juhul, kui rakendusele pole Tomcat konfiguratsioonis
kirjeldatud JNDI nime `jdbc/riharest/datasource`. Sellise JNDI nime leidumise korral kasutatakse andmebaasiühenduse 
loomiseks seda.
- Andmebaasiühenduse parameetreid kasutatakse ka ühiktestide käivitamiseks. Juhul, kui andmebaas pole tarkvara kompileerimise
ajal kättesaadav, tuleb ühikteste mitte käivitada (st kasutada parameetrit `skipTests=true`).

### Konfiguratsiooni faili abil

Kõrgema prioriteediga parameetrid võib määrata `/opt/tomcat/conf/riharest.project.properties` faili abil. Selle faili olemasolu ei ole kohustuslik ja võib sisaldada ainult väike osa konfiguratsioonist. Parameetrite prioriteet on kõrgem kui vaikeväärtustel.

## Tarkvarapaketi paigaldamine

RIHA-Storage REST teenuse komponent koosneb ühest Java veebirakenduse arhiivifailist nimega _rest.war_ . Fail tuleb
paigaldada Tomcat serveri kataloogi webapps. Peale rakenduse häälestamist tuleb häälestuse jõustumiseks
teha rakendusserverile restart.

### Andmebaasiühenduse häälestamine

Komponent eeldab, et RIHA andmebaasiühenduse info on kirjeldatud JNDI ressursina. Selleks tuleb Tomcati konfiguratsioonifaili
`context.xml` lisada read:

```xml
<Resource name="jdbc/riharest/datasource"
      auth="Container"
      factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
      type="javax.sql.DataSource"
      username="{username}"
      password="{password}"
      url="{url}"
      driverClassName="org.postgresql.Driver"
      initialSize="20"
      maxWaitMillis="15000"
      maxTotal="75"
      maxIdle="20"
      maxAge="7200000"
      testOnBorrow="true"
      validationQuery="select 1"
      />
```
Neis ridades tuleb ära näidata järgmine info:

- **username** - RIHA andmebaasikasutaja nimi, mille alt on tagatud rakendusepoolne andmebaasile ligipääs
- **password** - Eelmainitud kasutaja parool
- **url** - Andmebaasiühenduse URL (näiteks `jdbc:postgresql://192.168.50.106:5432/riha`)

### Dokumentide kataloogi tekitamine

RIHA-Storage hoiab kirjeldustele kaasapandud dokumente failisüsteemis. Selleks tuleb näidata RIHA-Storage 
konfiguratsioonifailis parameetri **_PATH_ROOT_** abil ära põhikataloog, mille alla dokumendid paigaldatakse.
Lisaks on vaja hoolitseda, et kasutaja, mille õigustes Tomcati rakendusserver töötab, omaks sellele kataloogile
lugemise ja kirjutamise õigusi.
