# RIHA-Storage API

spetsifikatsioon

Märkus. Ümber nimetatud eelmises arendusjärgus loodud "Kirjeldusmooduli" juurde kuuluvast dokumendist "RIHA API spetsifikatsioon" ja toimetatud. 

## Sisukord

- Arhitektuuriline ülevaade
  - Klassikaline variant
  - Ühetaoline variant
  - Eripäringud
  - Callback lisaparameeter
  -  Sõnumite töötlemise reeglid
- Edastatavad sõnumid
  - Edukad päringuvastused
  - Päringute tüübid, lubatud käsud ja lisaparameetrite kodeerimine
  - Toetatud tabelid
  - Eripäringud
    - COUNT
    - GETNAMES
    - RESOURCE
    - FILE
    - NEWVERSION
  - Andmete küsimine
    - fields
    - filter
    - sort
    - offset
    - limit
  - Andmete lisamine
  - Andmete muutmine
  - Andmete kustutamine
- Veateated


## Ülevaade

API on mõeldud RIHA komponentide omavaheliseks suhtluseks, eeskätt aga brauseris töötava veebirakenduse suhtluseks RIHA serverirakendusega.

Kõik liidesed toimivad HTTP või HTTP kaudu REST põhimõtete alusel. Saadetud andmeid kodeeritakse reeglina JSON formaadis, erandina GET päringute puhul aga CGI nimi=urlencoded_väärtus paaridena. JSON formaat võib omakorda sisaldada stringe, mis kodeerivad mistahes formaadis faile või tekste (.zip, .docx, .sql jne).

Ainult sisekomponentide vahel toimivad API-d piiratakse väliskeskkonnast üldjuhul IP aadressi põhiselt ning piiranguinfot päringus ei edastata.

Väliseks kasutuseks mõeldud päringud võivad olla kas piiramata või piiratud autentimistokeni abil, mis tuleb päringule kaasa panna kas ühe parameetri või HTTP päises oleva väärtusena.

Päringute üldparameetreid võib esitada kahel alternatiivsel moel, kusjuures API peab ära tundma mõlemad ja kasutus on vaba:

### Klassikaline variant

Tegevus esitatakse HTTP käsuna GET (päring), POST (lisamine), PUT (muutmine) või DELETE (kustutamine). Objekt, mida päritakse, millele lisatakse uus väärtus, tehakse muudatus või kustutatakse, on antud URL teel peale API baas-urli. Näites on baas-URL http://localhost/api/

````
http://localhost/api/db/mytable
http://localhost/api/db/mytable/123
````

Teel on reeglina kõigepealt db, siis baasitabeli nimi ja seejärel tabelis oleva kirje id. Mitte-andmebaasi väärtuste korral võib tee alata mingi muu identifikaatoriga kui db, näiteks files. Keeruka andmestruktuuri pärimise korral võib path olla ka pikem.

Ligipääsutoken (kui on vajalik) esitatakse HTTP päises väljal X-Auth-Token näiteks nii:

````
X-Auth-Token: a8426a0-8eaf-4d22-8e13-7c1b16a9370c
````

### Ühetaoline variant

Kõik eelnimetatud parameetrid kodeeritakse kas CGI nimi=väärtus paaridena või JSONi objektis kujul {"nimi":"väärtus",…}, kasutades nimesid op, path, token, ning neid saadetakse api baas-URLile, milleks on näiteks http://localhost/api

- op väärtus võib olla get,post,put,delete või hoopis mõni erioperatsiooni-nimi (ei ole piiratud), näiteks op=get või {"op": "get", …}
- path on tabeli/objekti identifikaator, kasutades selleks variant 1 urli vastavat osa, näiteks path=/db/mytablename/12 või {"path":"/db/mytablename/12", …}
- token ligipääsuks (kui on vajalik) antakse kaasa kui token=aba... või {"token": "abab", …}

Näide: `http://localhost/api?op=get&path=db/mytable/123&token=abca` ehk samaväärselt `http://localhost/api` URLile HTTP POSTiga saadetud `{"op":"get","path":"db/table/123","token":"abca"}`.

Kui päringus on korraga nii klassikalisel moel esitatud parameetreid kui ühetaolisel moel esitatud parameetreid, siis kehtivad ühetaolisel moel esitatud.

### Eripäringud

Päringute puhul, mille ülesanne klassikalise REST põhimõtte järgi ei ole selgelt määratud, tuleks anda POST meetodiga ja anda kaasa vastav
`"op":väärtus`, näiteks `"op":"specialtask"`, millele võivad lisanduda mistahes muud, mistahes struktuuriga parameetrid, näiteks: `http://localhost/api` URLile HTTP POSTiga saadetud `{"op":"addnums", "token":"abca", "param1":12.3, "param2": [2, 5]}` võib anda vastuse `{"result":19.3}` või isegi lihtsalt `19.3`.

Seejuures tuleb arvestada, et parameetrinimed `token` ja `callback` on reserveeritud nende standardkasutuseks ning nende sisuline tähendus peab olema sama, mis harilikel ülalkirjeldatud REST päringutel.

### Callback lisaparameeter

Igale päringule võib lisada `callback` parameetri kujul `callback=minufunktsioon` või `{"callback":"minufunktsioon",…}`, mispeale pannakse tulemus-JSON (sh veateted) vastuses parameetriks funktsioonile minufunktsioon. 

Näide: `http://localhost/api/db/mytable/123?callback=foo` annab vastuse kujul `foo({ "value": 58.3788, "name": "lat"});`.

`Callback` on vajalik selleks, et kasutaja brauser saaks teha AJAX päringuid domeenile, mis ei ole samas domeenis, kui veebileht. Eriti oluline on see arenduse ajal, kuid võib osutuda oluliseks ka lõppkasutuses.

### Sõnumite töötlemise reeglid

HTTP GET käsuga saadetud sõnum ei tohi andmeid lisada, muuta ega kustutada, välja arvatud – potentsiaalselt – logikirjete lisamine. Iga sõnum moodustab ühe terviku, ning – kui välja arvata tema võimalik otsene efekt andmete muutmiseks, logi kirjutamiseks jms – ei tohi tekitada kõrvalefekte, mis võivad mõjutada järgmiste sõnumite tähendust ja nende töötlemist.

## Edastatavad sõnumid

### Edukad päringuvastused

Ühe konkreetse kirje klassikalise päringu http://localhost/api/db/mytable/123 vastus on JSON objekt kujul `{ "value": 58.3788, "name": "lat"}`. 

Mitme kirje päringu `http://localhost/api/db/mytable` või otsingupäringu `http://localhost/api/db/mytable?filter=...` vastus on JSON array kirjetest kujul `[{ "value": 58.3788, "name": "lat"},{ "value": 24.56, "name": "lng"}]`.

Kui vastuses olev väli on omakorda JSON-objekt või JSON-array, siis esitatakse ta JSON kujul, mitte aga stringina, näiteks: `{ "value": 58.3788, "name": "lat", "address": {"city": "Tallinn", "street": "Gonsiori"}}`.

Kirjete lisamise päringuvastus on array edukalt lisatud kirjete (uutest) identifikaatoritest, näiteks `[1000]` või `[1000,1010,1011,1012]`.

Kirjete muutmise ja kustutamise päringuvastus on üldjuhul edukalt muudetud/kustutatud kirjete arv kujul `{"ok": N}`, kus N on täisarv, mis võib olla ka 0.

Kui mingil eripäringul on keeruline või ebasoovitav anda konkreetset vastuste arvu, on edukas vastus üldjuhul selline: `{"ok": 1}` ja edutu (aga mitte veaga seotud) vastus selline: `{"ok": 0}`.

### Päringute tüübid, lubatud käsud ja lisaparameetrite kodeerimine

Üldjuhul jaotame päringu ülesanded CRUD põhimõtte järgi neljaks: andmete küsimine, uute andmete lisamine, andmete muutmine ja andmete kustutamine, pluss viienda kategooriana eripäringud. Iga päring annab alati ka mingi vastuse, sõltumatult päringutüübist.

HTTP GET päringud ei tohi kunagi andmeid lisada ega muuta.

Päringuid võib kodeerida nii CGI formaadis nimi1=väärtus&nimi2=väärtus… paaridena (väärtused sel juhul urlencoded) kui JSON formaadis objektidena `{"nimi1":"väärtus", "nimi2":"väärtus", ...}`.

Päringul võivad olla lisaks käsule, objekti identifikaatorile ja tokenile ka muud parameetreid, näiteks väljastatavate kirjete maksimaalne arv, sorteering, filtrid, callback jms.

### Toetatud tabelid

Päringutes on tabeli nimena lubatud esitada kõiki RIHA andmemudeli tabeleid (vt dokumenti "RIHA andmebaasi füüsiline mudel").

### Eripäringud

Lisaks CRUD päringutele on oluline kategooria eripäringud, mis ei kujuta otseselt mingite baasiandmete pärimist, muutmist või lisamist, või teostavad hulga selliseid operatsioone korraga. Eripäringud võivad teha arvutusi, statistikat, käivitada käske või protsesse vms.

Eripäringud saadetakse üldjuhul HTTP POST käsuga JSON objektina, kus op parameetri nimeks on eripäringu nimi. Kui eripäring mitte mingeid andmeid ei muuda, võib teda altenatiivina realiseerida ka HTTP GET käsuga, kasutades
parameetite esitamiseks harilikku CGI `nimi=väärtus` kodeeringut.

Seejuures ei ole `path` ja `token` parameetrid üldjuhul kohustuslikud, kuigi neid võib kasutada. Konkreetne parameetrite hulk ja nende nimed võivad igal eripäringul olla erinevad, samuti ei ole mingeid piiranguid parameetrite tüübile/struktuurile. Näide hüpoteetilisest eripäringust: `http://localhost/api` URLile HTTP POSTiga saadetud

````
{"op":"specialop","param1": 23, "foo": {"lat": 12.4, "lng": 15.7}}
````

Eripäringute vastus peab jälgima siin dokumendis toodud veateadete põhimõtteid. Kui vastuseks on andmehulk, on soovitav jälgida siin dokumendis toodud punktis "Edukad päringuvastused" esitatud põhimõtteid.

#### COUNT

Päring kirjete arvu lugemiseks. Vastus on kujul `{"ok":<kirjetearv>}`

Näiteks:

````
http://192.168.50.106:8080/rest/api?op=count&path=db/main_resource&filter=name,=,prepareSignature&token=testToken
````

või POST päring `http://192.168.50.106:8080/rest/api`

````
{"op":"count","path":"db/main_resource","filter":[["service_code","=","aar.valdkonnad"]}
````

#### GETNAMES

Päring asutuste ja isikute ja main_resource tabelis asuvate andmeobjektide nimede saamiseks vastavalt etteantud registri- või isikukoodile või andmeobjekti URI-le.

Vastus on kujul

````
{ "organizations": {<registrikood>: <nimi>, <registrikood2>: <nimi2>},"persons": {<isikukood>: <nimi>}}
````

Näiteks: `POST päring http://192.168.50.106:8080/rest/api`

````
{"op":"getnames", "organizations":["21345", "1234123"], "persons":["372115555", "3745555555"], "token":"testToken"}
````

Sama päring URI-dega:

````
{"op":"getnames", "organizations":["70009646", "80296167"], "persons":["37211070309", "37404192743"],"uris":["urn:fdc:riha.eesti.ee:2016:classifier:172297", "urn:fdc:riha.eesti.ee:2016:classifier:172298"], "token":"testToken"}
````

#### RESOURCE

Päring objekti täisinfo importimiseks (POST meetodi korral) või eksportimiseks (GET meetodi korral). Tehniliselt võttes teostatakse päring kõigi `main_resource`'ga seotud kirjete saamiseks. Antud päring lisab vastusesse kõik `data_object_id` ja `document_id`, mille `main_resource_id` võrdub päringus antud `id`-ga. `data_object_id` lisatakse vastusesse välja, mille nimi võetakse `data_object`'i `field_name` väljast. Samamoodi toimitakse ka `document`'iga.

POST meetodi korral tuleb päringu URL esitada kujul `/rest/api/resource?token={token}` ja GET meetodi korral tuleb päringu URL esitada kujul `/rest/api/resource/{id}?token={token}`.

Kui kirjel puuduvad juurdepääsupiirangud, siis ei pea GET meetodis token'it näitama.

POST meetodi näide:

`URL: http://192.168.50.106:8080/rest/api/resource?token=testToken`

````
{
  "uri": "urn:fdc:test.test.ee:2016:TEST_MR_123:XYZ",
  "xyz": "TEST_::123",
  "kind": "infosystem",
  "name": "ASDASD:MAIN_RESOURCE_TEST:123",
  "owner": "wone:er2",
  "state": "C",
  "creator": "TEST_ISIKUKOOD",
  "kind_id": 401,
  "version": "newV3",
  "creation_date": "2016-10-20T12:42:38",
  "main_resource_id": 1,
  "short_name":"TEST_MR_123",
  "infosystem_status":"TEST_INFOSYS_STATUS",
  "entities": [
    {
      "uri": "urn:fdc:test.test.ee:2016:TEST_DATA_123:ABC",
      "xyz": "TEST_::123",
      "kind": "entity",
      "name": "NOOOOO_:DATA_OBJECT:123_ASD",
      "owner": "wone:er2",
      "state": "C",
      "creator": "TEST_ISIKUKOOD",
      "kind_id": 407,
      "creation_date": "2016-10-20T12:42:44",
      "field_name":"entities",
      "data_object_id": 2,
      "main_resource_id": 1,
      "default_documents": [
        {
          "uri": "urn:fdc:test.test.ee:2016:TEST_DOC_123:ZXC",
          "kind": "document",
          "mime": "application\/json",
          "owner": "TEST_ASUTUS",
          "old_id": 210111,
          "content": "VEVTVCBVUERBVEUgQ09OVEVOVA==",
          "creator": "TEST_ISIKUKOOD",
          "kind_id": 408,
          "filename": "NEW_VERSION_DOC.json",
          "modifier": "-",
          "document_id": 3,
          "creation_date": "2016-10-20T12:42:52",
          "data_object_id": 2
        }
      ]
    }
  ]
}
````

GET meetodi näide:

`URL: http://192.168.50.106:8080/rest/api/resource/518215?token=testToken`

````
{
  "uri": "urn:fdc:riha.eesti.ee:2016:classifier:518215",
  "name": "Anesteesia liigid",
  "areas": "KLASSIFIKAATOR_VALDKOND_TERVISHOID",
  "owner": 21349,
  "state": "C",
  "old_id": 210,
  "creator": "-",
  "kind_id": 405,
  "version": 8.0,
  "excellent": false,
  "short_name": "AL",
  "creation_date": "2008-07-04T12:20:21",
  "modified_date": "2010-09-07T11:43:04",
  "approved_by_law": false,
  "main_resource_id": 518215,
  "reference_number": "KL000210",
  "template_version": "1.0.0",
  "update_frequency": " toimub vastavalt taotluste esitamisele ja järgnevale loendite töörühma otsusele või vastavalt seadusandlikule muudatusele",
  "approval_required": false,
  "classifier_status": "kehtestatud",
  "short_description": "Võimalikud anesteesia liigid on: üldanesteesiaid, regionaalanesteesiaid, anestesioloogiline julgestus. ",
  "access_restriction": 0,
  "kind": "classifier",
  "documents": [
    {
      "uri": "urn:fdc:riha.eesti.ee:2016:document:193243",
      "mime": "application/vnd.ms-excel",
      "type": "klassifikaator",
      "state": "C",
      "old_id": 533,
      "content": "S29vZDtM/GhpbmltZXR1cztOaW1ldHVzO1Bpa2tfbmltZXR1cztWYW5lbV9rb29kO0hpZXJhcmhpYV9hc3RlO0tlaHRpdnVzZV9hbGd1c2Vfa3B2O0tlaHRpdnVzZV9s9XB1X2twdjtWaWltYW5lX211dWRhdHVzX2twdjtNdXV0amE7U3RhYXR1cztTZWxnaXR1cw0KQTv8bGRhbmVzdGVlc2lhO/xsZGFuZXN0ZWVzaWE7/GxkYW5lc3RlZXNpYTs7MDszMS4wNS4yMDA3Ozs7OzE7DQpBMDE7ZW5kb3RyYWhoZWFhbG5lIGFuZXN0ZWVzaWE7ZW5kb3RyYWhoZWFhbG5lIGFuZXN0ZWVzaWE7ZW5kb3RyYWhoZWFhbG5lIGFuZXN0ZWVzaWE7QTsxOzMxLjA1LjIwMDc7Ozs7MTsNCkEwMTAxO3B1aGFzIGVuZG90cmFoaGVhYWxhbmVzdGVlc2lhO3B1aGFzIGVuZG90cmFoaGVhYWxhbmVzdGVlc2lhO3B1aGFzIGVuZG90cmFoaGVhYWxhbmVzdGVlc2lhO0EwMTsyOzMxLjA1LjIwMDc7Ozs7MTsNCkEwMTAyO2VuZG90cmFoaGVhYWxhbmVzdGVlc2lhIGtvbWJpbmF0c2lvb25pcyBlcGlkdXJhYWxhbmVzdGVlc2lhZ2E7ZW5kb3RyYWhoZWFhbGFuZXN0ZWVzaWEga29tYmluYXRzaW9vbmlzIGVwaWR1cmFhbGFuZXN0ZWVzaWFnYTtlbmRvdHJhaGhlYWFsYW5lc3RlZXNpYSBrb21iaW5hdHNpb29uaXMgZXBpZHVyYWFsYW5lc3RlZXNpYWdhO0EwMTsyOzMxLjA1LjIwMDc7Ozs7MTsNCkEwMTAzO2VuZG90cmFoaGVhYWxuZSBhbmVzdGVlc2lhIGtvbWJpbmF0c2lvb25pcyBzYWtyYWFsYW5lc3RlZXNpYWdhO2VuZG90cmFoaGVhYWxuZSBhbmVzdGVlc2lhIGtvbWJpbmF0c2lvb25pcyBzYWtyYWFsYW5lc3RlZXNpYWdhO2VuZG90cmFoaGVhYWxuZSBhbmVzdGVlc2lhIGtvbWJpbmF0c2lvb25pcyBzYWtyYWFsYW5lc3RlZXNpYWdhO0EwMTsyOzMxLjA1LjIwMDc7Ozs7MTsNCkEwMTA0O2VuZG90cmFoaGVhYWxuZSBqYSBwbGV4dXMtYW5lc3RlZXNpYTtlbmRvdHJhaGhlYWFsbmUgamEgcGxleHVzLWFuZXN0ZWVzaWE7ZW5kb3RyYWhoZWFhbG5lIGphIHBsZXh1cy1hbmVzdGVlc2lhO0EwMTsyOzMxLjA1LjIwMDc7Ozs7MTsNCkEwMjttYXNrbmFya29vc2lkO21hc2tuYXJrb29zaWQ7bWFza25hcmtvb3NpZDtBOzE7MzEuMDUuMjAwNzs7OzsxOw0KQTAyMDE7dGF2YWxpc2VsIG1lZXRvZGlsO3RhdmFsaXNlbCBtZWV0b2RpbDt0YXZhbGlzZWwgbWVldG9kaWw7QTAyOzI7MzEuMDUuMjAwNzs7OzsxOw0KQTAyMDI7a/VyaW1hc2tpZ2E7a/VyaW1hc2tpZ2E7a/VyaW1hc2tpZ2E7QTAyOzI7MzEuMDUuMjAwNzs7OzsxOw0KQTAzO3RvdGFhbG5lIGludHJhdmVub29zbmUgYW5lc3RlZXNpYSAoVElWQSk7dG90YWFsbmUgaW50cmF2ZW5vb3NuZSBhbmVzdGVlc2lhIChUSVZBKTt0b3RhYWxuZSBpbnRyYXZlbm9vc25lIGFuZXN0ZWVzaWEgKFRJVkEpO0E7MTszMS4wNS4yMDA3Ozs7OzE7DQpBMDk7bXV1ZDttdXVkO211dWQ7QTsxOzMxLjA1LjIwMDc7Ozs7MTsNCkI7cmVnaW9uYWFsYW5lc3RlZXNpYTtyZWdpb25hYWxhbmVzdGVlc2lhO3JlZ2lvbmFhbGFuZXN0ZWVzaWE7OzA7MzEuMDUuMjAwNzs7OzsxOw0KQjAxO3B1aGFzIHNwaW5hYWxhbmVzdGVlc2lhO3B1aGFzIHNwaW5hYWxhbmVzdGVlc2lhO3B1aGFzIHNwaW5hYWxhbmVzdGVlc2lhO0I7MTszMS4wNS4yMDA3Ozs7OzE7DQpCMDI7c3BpbmFhbCBrb21iaW5lZXJpdHVuYSBlcGlkdXJhYWxhbmVzdGVlc2lhZ2EgKENTRSk7c3BpbmFhbCBrb21iaW5lZXJpdHVuYSBlcGlkdXJhYWxhbmVzdGVlc2lhZ2EgKENTRSk7c3BpbmFhbCBrb21iaW5lZXJpdHVuYSBlcGlkdXJhYWxhbmVzdGVlc2lhZ2EgKENTRSk7QjsxOzMxLjA1LjIwMDc7Ozs7MTsNCkIwMztzcGluYWFsYW5lc3RlZXNpYSBrYXRlZXRlci1tZWV0b2RpbCAoQ1NBKTtzcGluYWFsYW5lc3RlZXNpYSBrYXRlZXRlci1tZWV0b2RpbCAoQ1NBKTtzcGluYWFsYW5lc3RlZXNpYSBrYXRlZXRlci1tZWV0b2RpbCAoQ1NBKTtCOzE7MzEuMDUuMjAwNzs7OzsxOw0KQjA0O3B1aHRhbCBrdWp1bCBlcGlkdXJhYWxhbmVzdGVlc2lhO3B1aHRhbCBrdWp1bCBlcGlkdXJhYWxhbmVzdGVlc2lhO3B1aHRhbCBrdWp1bCBlcGlkdXJhYWxhbmVzdGVlc2lhO0I7MTszMS4wNS4yMDA3Ozs7OzE7DQpCMDU7cGxleHVzIGJyYWNoaWFsaXMnZSBibG9rYWFkO3BsZXh1cyBicmFjaGlhbGlzJ2UgYmxva2FhZDtwbGV4dXMgYnJhY2hpYWxpcydlIGJsb2thYWQ7QjsxOzMxLjA1LjIwMDc7Ozs7MTsNCkIwNjtzaWxtYWJsb2thYWQ7c2lsbWFibG9rYWFkO3NpbG1hYmxva2FhZDtCOzE7MzEuMDUuMjAwNzs7OzsxOw0KQjA3O3Nha3JhYWxibG9rYWFkO3Nha3JhYWxibG9rYWFkO3Nha3JhYWxibG9rYWFkO0I7MTszMS4wNS4yMDA3Ozs7OzE7DQpCMDg7aW50cmF2ZW5vb3NuZSByZWdpb25hYWxhbmVzdGVlc2lhIChJVlJBKTtpbnRyYXZlbm9vc25lIHJlZ2lvbmFhbGFuZXN0ZWVzaWEgKElWUkEpO2ludHJhdmVub29zbmUgcmVnaW9uYWFsYW5lc3RlZXNpYSAoSVZSQSk7QjsxOzMxLjA1LjIwMDc7Ozs7MTsNCkIwOTttdXVkO211dWQ7bXV1ZDtCOzE7MzEuMDUuMjAwNzs7OzsxOw0KQzthbmVzdGVzaW9sb29naWxpbmUganVsZ2VzdHVzO2FuZXN0ZXNpb2xvb2dpbGluZSBqdWxnZXN0dXM7YW5lc3Rlc2lvbG9vZ2lsaW5lIGp1bGdlc3R1czs7MDszMS4wNS4yMDA3Ozs7OzE7DQo=",
      "creator": "45901190303",
      "kind_id": 410,
      "filename": "Anesteesia liigid.csv",
      "modifier": "45901190303",
      "field_name": "documents",
      "start_date": "2008-10-16T11:34:10",
      "document_id": 193243,
      "creation_date": "2008-10-16T11:34:10",
      "modified_date": "2008-10-16T11:34:10",
      "main_resource_id": 518215,
      "kind": "classifier_document"
    }
  ]
}
````

#### FILE

Päring failide(dokumentide) allalaadimiseks. Parameetrina tuleb ette anda dokumendi identifikaator: `/api/file/{document_id}?token={token}`.

Näiteks: `http://192.168.50.106:8080/rest/api/file/99567?token=testToken`.

#### NEWVERSION

Päring andmeobjektist uue versiooni loomiseks. Parameetrina antakse ette uus versiooni number

Näiteks POST päring `http://192.168.50.106:8080/rest/api`:

````
{"op":"newversion","path":"db/main_resource", "new_version":"v2", "uri":"urn:fdc:riha.eesti.ee:2016:TEST_infosystem:273826","token":"testToken"}
````

### Andmete küsimine

Võib kasutada nii HTTP GET kui POST käsku.

HTTP GET käsu puhul kodeeritakse päring CGI formaadis `nimi=väärtus` paaridena. Näited:

````
http://localhost/api/db/mytable/123
````

või ühetaoliselt

````
http://localhost/api?op=get&amp;path=db/mytable/123&amp;token=abca
````

HTTP POST käsu puhul eeldatakse parameetrite kodeeringut JSON formaadis kujul `{"op":"get", "path":"….", …}` juhul, kui päringu `Content-type` päis sisaldab stringi `json`. Vastasel korral eeldatakse parameetreid CGI formaadis `nimi=väärtus` paaridena.

Näide eelmisega samaväärsest HTTP POST käsust andmete küsimiseks `http://localhost/api` URLile HTTP POSTiga saadetud:

````
{"op":"get","path":"/db/mytable/123","token":"abca"}
````

Lisaks pathile võib alati lisada järgmisi filter- ja sorteerimisparameetreid, kuid need ei ole kohustuslikud ja neil on vaikeväärtus.

#### fields

Väljade array, mida väljastada. Vaikimisi väljastatakse kõik.

#### filter

Array kolmik-arraydena `*\[\[field,op,value\],...,\[field,op,value\]\]*`, mida interpreteeritakse kui `and`-iga seotud `SQL WHERE` lauset. 

Näide: `*\[\[&quot;lat&quot;,&quot;&gt;&quot;,53\],\[&quot;type&quot;,&quot;=&quot;,&quot;city&quot;\]\]*`. Vaikimisi filtrit ei ole. CGI formaadis antakse nii: `*filter=lat,&gt;,53,type,=,&quot;city&quot;*` 
kus kogu `*lat,&gt;,53,type,=,&quot;city&quot;*`  on `urlencoded`.

Filtri väärtusteks (value) võivad olla:

- stringid (stringide sees võib kasutada SQL metamärke % ja _)
- numbrid
- tõeväärtused (false, true).

Filtri operaatoriteks (`op`) võivad olla:

- võrdlusmärgid (<, >, =, <>, >=,<=)
- tõstutundlik mustri otsing LIKE ja tõstutundetu mustri otsing ILIKE (Näide: *\[\[&quot;type&quot;,&quot;ilike&quot;,&quot;%aA%&quot;\]\]*)
- null väärtuse kontrolliks ISNULL ja ISNOTNULL (Näide: *\[\[&quot;name&quot;,&quot;isnull&quot;,null\]\]*) - siinkohal filtri väärtust ignoreeritakse

#### sort

Väljanimi või väljanimi tema ees oleva -märgiga: {"sort":"lat", ...} või {"sort":"-lat", …}.
Vaikimisi puudub. CGI formaadis antakse nii: sort=lat või sort=-lat.
offset

Mitmendast kirjest hakatakse väljastama (offset kirjeid jäetakse vahele), vaikimisi 0

#### limit

Maksimaalne arv väljastatavaid kirjeid. Kui puudub, eeldame, et on peal konfiguratsiooniga määratud vaikepiirang.

Näide URL-kodeeringus ühteaoliselt esitatud päringust, kus %3E on URL-kodeeritud '>'

````
http://localhost/api?op=get&path=db/mytable&fields=id,value&filter=id,%3E,1000&sort=value&offset=10&limit=100&token=test
````

Päringu vastus on klassikalise ühe objekti HTTP GET päringu puhul see objekt `{ "value": 58.3788, "name": "lat"}` ja mitme kirje päringu `http://localhost/api/db/mytable` või otsingupäringu `http://localhost/api/db/mytable?filter=… vastus` on array kirjetest kujul

````
[{ "value": 58.3788, "name": "lat"},{ "value": 24.56, "name": "lng"}]}
````

### Uute andmete lisamine.

Kasutada võib ainult POST päringuid ja ainult JSON formaadis andme- ja lisaparameetreid. Lisada võib ühe kirje JSON
objektina või kirjete loendi JSON arrayna.

Näited:

http://localhost/api/db/mytable pathile klassikalisel viisil HTTP POST käsuga saadetud


{ "value": 58.3788, "name": "lat"}

või


[{ "value": 58.3788, "name": "lat"},{ "value": 24.56, "name": "lng"}]

või ühetaolisel viisil selliselt:


{"op":"post", "path": "/db/mytable", "data":{ "value": 58.3788, "name": "lat"}}

või selliselt:


{"op":"post", "path": "/db/mytable",  "data": [{ "value": 58.3788, "name": "lat"},{ "value": 24.56, "name": "lng"}]}

Kui lisatava välja väärtus on omakorda JSON array või JSON objekt, esitatakse ta JSON kujul, mitte stringina:


{ "value": 58.3788, "name": "lat", "address": {"city": "Tallinn", "street": "Gonsiori"}}


Päringu vastus on JSON array edukalt lisatud kirjete identifikaatoritest, näiteks *\[1000\]* või *\[1000,1002,1003\]*

### Andmete muutmine

Võib kasutada nii HTTP PUT kui HTTP POST päringuid (viimasel juhul peab olema kasutusel ühetaoline variant, sh
{"op":"put", "path":"….", ...} parameeter-väärtused) ja ainult JSON formaadis parameetreid. HTTP POST võimaldab muuta
mitut kirjet korraga.

Näited:

http://localhost/api/db/mytable/123 pathile klassikalisel viisil saadetud HTTP PUT


{ "value": 58.3788, "name": "lat"}

või ühetaolisel viisil selliselt:

http://localhost/api URLile saadetud HTTP POST


{"op":"put", "path": "/db/mytable/123", "data":{ "value": 58.3788, "name": "lat"}}

Mitme kirje korraga muutmine toimub selliselt:

http://localhost/api URLile saadetud HTTP POST

{"op":"put", "path": "/db/mytable", "key":"id", "data":[{"id":123, "value": 58.3788, "name": "lat"},{"id":456, "value": 58.3788, "name": "lat"}]

Viimasel juhul esitab "key":"id" väljanime (näites id), mille järgi kirjeid muutmise jaoks identifitseeritakse.
See väljanimi peab olema toodud järgnevates data kirjetes.

NB! Key väärtus ei pea olema unikaalne identifikaator, seega võib üks kirje sisendis muuta mitut kirjet baasis.

Oluline: andmetes esitatud väljad muudetakse, esitamata välju ei muudeta.

Päringu vastus on edukalt muudetud kirjete arv, näiteks {"ok": 2}. Kui kirjeid ei õnnestunud muuta, vastatakse lihtsalt {"ok": 0}

### Andmete kustutamine

Võib kasutada nii HTTP DELETE kui HTTP POST päringuid (viimasel juhul peab olema antud op=delete parameeter-väärtus) ja
ainult JSON formaadis lisaparameetreid. HTTP POST võimaldab kustutada korra mitu kirjet.

Näited:

http://localhost/api/db/mytable/123 pathile klassikalisel viisil saadetud HTTP DELETE kustutab antud kirje
ning ühetaolisel viisil toimub ühe kirje kustutamine selliselt:

http://localhost/api URLile saadetud HTTP POST

{"op":"delete", "path": "/db/mytable/123"}

ja mitme kirje kustutamine selliselt:

http://localhost/api URLile saadetud HTTP POST

{"op":"delete", "path": "/db/mytable", "id":[123,456,777]}

kus "id" asemel kasutatakse konkreetset väljanime, millega antud tabeli kirjeid identifitseeritakse, ning selle
väärtuseks on alati kustutatavate kirjete identifikaatorite array.

NB! Key väärtus ei pea olema unikaalne identifikaator, seega võib üks kirje sisendis kustutada mitu kirjet baasis.

Päringu vastus on edukalt kustutatud kirjete arv, näiteks {"ok": 2}. Kui kirjeid ei õnnestunud kustutada, vastatakse
lihtsalt {"ok": 0}

## Veateated

Veateade esitatakse alati JSON objekti kujul vastusena, kus on alati vähemalt kaks välja:

- `errcode`, mis on üldistav vea iseloomustaja ning mida saab kasutata näiteks kasutajaliideses sobiva veateksti näitamiseks, ning
- `errmsg`, mis on üldjuhul arendajale arusaadav veateade ilma stack traceta

ning lisaks võib soovi korral kasutada kolmandat:

- `errtrace`, mis on tehniline stack trace debugimiseks

Näide: `{"errcode": 2, "errmsg": "arusaamatu parameeter foo"}`.

### Veakoodid

`errcode` tähendus

- puhttehnilised vead, mis üldjuhul ei ole sisendiga seotud:
  - 1: arusaamatu/klassifitseerimata viga
  - 2: konfiguratsiooniviga
  - 3: timeout
  - 4: andmebaasi ühenduse loomise viga
- sisendi esitusega seotud vead:
  - 10: tundmatu/vale HTTP content-type
  - 11: arusaamatu HTTP käsk või op parameetri väärtus
  - 12: süntaktiliselt vigane sisend
  - 13: puuduvad vajalikud sisendparameetrid
  - 14: tundmatud sisendparameetrid
  - 15: sisendparameetril vale väärtustüüp
  - 16: sisendparameetri pathi ei leitud
  - 17: ligipääsuõigus puudub
  - 18: andmebaasi päringu formaadi viga
- spetsiifilised vead:
  - 100: ja edasi, mida rakendus võib kodeerida vabalt

Veateate puhul võib HTTP vastuskood olla seejuures:
- 200 – OK - mida võib kasutada ka mittetehniliste vigade korral, kus ei ole päris sobivat järgnevat HTTP vastuskoodi, või mõni neist klassikalistest HTTP vastuskoodidest:
- 400 - Bad Request – mingi sisendiprobleem,
- 403 – Forbidden – ei ole õigust antud toimingut teha,
- 404 - Not Found – api pathi ei leitud,
- 500 - Internal Server Error – tehniline viga serveri pool

JSON veateade tuleb esitada vastuskoodist sõltumatult.
