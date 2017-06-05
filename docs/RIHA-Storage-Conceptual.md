# RIHA andmebaasi kontseptuaalne mudel

Versioon 3.0, 05.06.2017

Märkus. Käesolev 
versioon on põhjalikult ümber töötatud dokumendist v 2.0, 29.04.2017, mille autorid olid Girf OÜ, Degeetia OÜ, Mindstone OÜ.

##Sisukord

- Ülevaade
- Seotud dokumendid
- Andmebaasi arendamise eesmärgid
- Andmeviisi struktuuri etapiviisiline arendamine
- Uue andmebaasi struktuuri põhimõtted
- Uue andmebaasi struktuuri füüsilise andmemudeli põhiosad
  - Infosüsteemi kirjeldamine
  - Infosüsteemi funktsioonide kirjeldamine [EI TEOSTATA]
  - Infosüsteemi loogilise andmekoosseisu kirjeldamine
  - Andmebaaside, tabelite ja väljade kirjeldamine
  - Teenuste kirjeldamine
  - Valdkondade, sõnastike ja XML varade kirjeldamine
  - Versioneerimine
    - Versioneerimise stsenaariumid
    - Versioneerimise tehnoloogia


## Ülevaade

Dokument  kirjeldab uue RIHA andmebaasi kontseptuaalset mudelit, sh

- mis eesmärkidel ja kuidas struktuuri uuendatakse,
- kuidas toimub versioneerimine,
- kuidas toimub andmete ülekandmine vana ja uue süsteemi vahel.

## Seotud dokumendid

- Uue RIHA andmebaasi füüsiline andmemudel on esitatud eraldi dokumendis ja tema lisades.
- Andmebaasi struktuur Enterprise Architecti failina: `riha_andmemudel.eap`

- RIHA andmebaasi füüsiline mudel, esitatud dokumentides:
  - `RIHA andmebaasi füüsiline mudel.docx`
  - `riha_main_tables.sql` esitab alltoodud nelja põhitabeli SQL-väljad CREATE TABLE lausetena: sama on esitatud käesolevas dokumendis allpool, kontseptuaalse mudel lugemise hõlbustamiseks.
  - `riha_tables.sql` esitab kogu RIHA andmebaasi struktuuri SQL CREATE lausetena
  - `riha_andmedetailid.xlsx` sisaldab lisaks SQL väljadele JSON struktuuris esitatavad väljad: seejuures `main_resources` tabeli asemel on esitatud eraldi `infosystem`, `service`, `classifier`, `area` kui eri tüüpi põhiobjektid erinevate JSON-väljadega.
- Andmete esitamine masinloetaval kujul on esitatud eraldi dokumentides, millest käesoleva dokumendi kontekstis tasub tutvuda põhidokumendiga:
  - `RIHA andmete masinloetavate vormingute põhimõtted.docx`.

## Andmebaasi arendamise eesmärgid

- Muuta andmebaasi struktuur senisest universaalsemaks ja paindlikumaks, mis võimaldab
- RIHAs kergemini kasutusele võtta ja hallata uusi ressursitüüpe lisaks senistele (infosüsteem, teenused, klassifikaatorid, valdkonnasõnastikud ja XML varad)
- Kergemini lisada uusi välju ja kirjeldusatribuute
- Võimaldada infosüsteemide ja muude ressursside automatiseeritud kirjeldamist väliste kirjeldusfailidega, mida RIHA automaatselt sisse loeb.
- Võimaldada ressursside versioneerimist.
- Lihtsustada andmebaasi struktuuri.

## Andmebaasi struktuuri etapiviisiline arendamine

Uut andmebaasistruktuuri ei looda nullist, vaid lähtutakse järgmistest põhimõtetest:

- Vana RIHA andmebaasist peab saama regulaarselt andmeid uude kanda ilma andmekaota.
- Senist andmebaasi struktuuri muudetakse minimaalselt, ehk vastavalt uute komponentide/kasutajaliideste vajadustele.
- Andmebaasi uuendatakse järk-järgult, vastavalt komponentide ja ressursitüüpide ärianalüüsile, mis täpsustab reaalseid vajadusi ja nende lahendamise parimaid teid.
- Uue RIHA tervikliku valmimiseni jäävad tööle nii vana kui uus RIHA, kusjuures uues RIHAs on realiseeritud osa mooduleid, ning seni uuendamata moodulid töötavad vanas RIHAs, moodustades kasutaja jaoks samas ühe tervikliku kasutajaliidese. Vanal ja uuel RIHAl on erinevad andmebaasid.
- Kõik vana RIHA andmetabelid kantakse esialgu üle uue RIHA andmebaasi, kus neile lisatakse uute põhimõtete järgi loodud uued tabelid. Vanade tabelite mittevajalikuks osutumise järel nad uue RIHA andmebaasist kustutatakse.
- Uue RIHA moodulid kasutavad ainult uut andmebaasi ning vana RIHA moodulid kasutavad ainult vana andmebaasi.

Etapiviisiline täpsustamine toimub järgnevalt:

- Esimeses etapis uuendatakse põhjalikult infosüsteemi, andmebaaside/tabelite/väljade ning teenuste esitamise struktuuri.
- Klassifikaatorite, valdkonnasõnastike ja XML varade struktuur uuendatakse esimeses etapis väga lihtsate põhimõtete alusel, põhjalikum uuendamine toimub peale nende ressursitüüpide ärianalüüsi.

## Uue andmebaasi struktuuri põhimõtted

Uue RIHA andmebaasimootorina kasutatakse uusimat Ubuntu LTS-s toetatud PostgreSQL versiooni. Alates versioonist 9.4 on PostgreSQL-l väga hea tugi vaba struktuuriga JSON andmete efektiivseks hoidmiseks ja töötlemiseks JSONB tüüpi väljal.

Struktuuri muutused lähtuvad järgmisest:
- Senised erinevad tabelid infosüsteemi, teenuse, klassifikaatori, valdkonna, valdkonna sõnastiku ja XML vara jaoks asendatakse ühise universaalse tabeliga `main_resource`.
- Kõik uute tabelite väljad esitatakse PostgreSQL JSONB tüüpi nimi-väärtus paaridest koosneva objektina, kus väärtused võivad olla nii lihtväärtused kui omakorda sisemise struktuuriga objektid või massiivid. Selline mahukas JSONB objekt on klassikalise SQL tabeli mõttes väljal `json_content`.
- Igal uuel tabelil on lisaks `json_content` väljale veel mitmeid klassikalisi SQL välju, mille põhieesmärk on võimaldada kiiremat otsingut, ehk niiöelda cache-tud väärtused.
- Väliselt kirjeldatavad ressursid (infosüsteemid, teenused, tabelid jne) identifitseeritakse stabiilse, versioonist sõltumatu tekstilise URI väljaga, mida mh kasutatakse selle ressursi kirjeldamisel väliste kirjeldusfailide abil. Primaarvõti `id` identifitseerib ressursi konkreetset versiooni.
- Ressursi spetsiifilised abitabelid, mis on ette nähtud loendite jms lihtsate struktuuride kodeerimiseks (näiteks, eri keeltes olevad nimed jms) kaotatakse ja asendatakse kas JSON massiivi või JSON nimi-väärtus objektiga otse põhitabeli vastaval JSONB-väljal.
- Kommentaaride ja dokumentide universaalsed abitabelid jäetakse alles ja restruktureeritakse uute põhimõtete järgi.

Uus andmebaasi struktuur toetub kahele peamisele tabelile: ülemise taseme tabel `main_resource` ja tema komponentide tabel `data_object` ning neile lisainfot andvatele tabelitele `document` ja `comment`.

Mõlemad tabelid võivad moodustada hierarhia oma `parent_id` välja kaudu. Seejuures näeme ette, et:
- `main_resource` tabeli rida sisaldab suurel hulgal informatsiooni, on versioneeritav ja ei moodusta üldjuhul sügavaid hierarhiaid. Tabel ei sisalda väga suurtes kogustes ridu.
- `data_object` tabeli rida on seotud konkreetse `main_resource` tabeli reaga, sisaldab vähem informatsiooni, ei ole omaette versioneeritav ja võib moodustada sügavaid hierarhiaid. Tegemist on kogu andmebaasi kõige mahukama tabeliga.

Peamised täiendavat informatsiooni sisaldavad tabelid, mis võivad viidata mistahes `main_resource` või `data_object` reale, on:

- `document`, mis sisaldab üldjuhul ametliku dokumendi (määruse vms) tervet sisu või tema viita kas URLi või failisüsteemi viidana, samuti tema metainformatsiooni: nimi, kehtivusperiood jms.
- `comment` sisaldab mõne süsteemi kasutaja tekstilist kommentaari või küsimust konkreetse süsteemi või tema osa kohta, koos metainfoga: millal lisati jms.

## Uue andmebaasi struktuuri füüsilise andmemudeli põhiosad

Esitame siin füüsilise andmemudeli põhiosad, jättes täpsustamata igas tabelis oleva `json_content` välja sisemise struktuuri, kus hoitakse tabeli ridade põhiinfot.

`json_content` välja struktuur tuuakse välja eraldi dokumendis „RIHA andmebaasi füüsiline mudel".

## Infosüsteemi kirjeldamine

Infosüsteem esitatakse `main_resource` tabelis. Väljal `kind` on kirjas `infosystem`. Kirjelduse põhiosad esitakse `json_content` väljal, mis sisaldab mh ka kõiki ülaltoodud SQL-lauses antud klassikalisi välju.

Infosüsteemi püsiv, versioonist sõltumatu identifikaator on `uri`, kuhu sisestatakse üldjuhul `riha:infosystem:lühinimi` . Hierarhilise põhiobjekti korral viidatakse ülemobjektile `parent_uri` välja kaudu, millele lisandub sekundaarse võimalusena versioonitundlik `parent_id` juhuks, kui hierarhia versioneerimise käigus muutub.

Infosüsteemi, tema andmebaaside ja tabelite versioneerimise põhimõtted on detailselt kirjas selle dokumendi hilisemas peatükis „Versioneerimine".

## Infosüsteemi funktsioonide kirjeldamine [EI TEOSTATA]

Uue võimalusena on võimalik infosüsteemi kirjelduse koosseisus kirjeldada infosüsteemi funktsioonide/eesmärkide loetelu. Funktsioonid/eesmärgid tuleb kirjeldada andmete säilitustähtaegade täpsusega - erineva säilitustähtajaga säilitatavate andmete kohta tuleb kirjeldada erinev funktsioon/eesmärk.

Funktsioonid/eesmärgid esitatakse data_object tabelis hierarhiliselt, kasutades `parent_id` välja. Tasemete arv pole piiratud. Olemi tüüp on kirjas tekstiväljal `kind` ning see on alati väärtusega `function`. Iga selline olem on `main_resource_id` kaudu alati ühe infosüsteemi konkreetse versiooniga seotud.

Kirjelduse põhiosad esitakse `json_content` väljal.

## Infosüsteemi loogilise andmekoosseisu kirjeldamine

Infosüsteemi andmekoosseis esitatakse `data_object` tabelis hierarhiliselt, kasutades `parent_id` välja. Tasemete arv pole piiratud. Olemi tüüp on kirjas tekstiväljal `kind` ning see on alati väärtusega `entity`. Iga selline olem on `main_resource_id` kaudu alati ühe infosüsteemi konkreetse versiooniga seotud.

Kirjelduse põhiosad esitakse `json_content` väljal.

## Andmebaaside, tabelite ja väljade kirjeldamine

Infosüsteemi andmebaasid, tabelid ja väljad esitatakse `data_object` tabelis hierarhiliselt, kasutades `parent_id` välja. Esimene tase on alati andmebaas. Olemi (andmebaas, tabel, ...) tüübi määrab tekstiväli `kind`. Iga selline olem on `main_resource_id` kaudu alati ühe infosüsteemi konkreetse versiooniga seotud.

Olemi püsiv, versioonist sõltumatu identifikaator on `uri`, kuhu sisestatakse üldjuhul `riha:infosystem:lühinimi:andmebaas:tabel:väli` s.t koolonitega eraldatud hierarhia infosüsteemi, andmebaasi, tabeli jne väljast kuni antud olemi nimeni. Olemi hierarhia võib olla ka sügavam, näiteks, kui kasutatakse mitte-SQL-andmebaase või sisemise struktuuriga JSON välju.

Kirjelduse põhiosad esitakse `json_content` väljal, mis sisaldab mh ka kõiki ülaltoodud SQL-lauses antud klassikalisi välju.

## Teenuste kirjeldamine

Teenused esitatakse `main_resource` tabelis, ning nad on üldjuhul konkreetse infosüsteemiga `parent_id` kaudu seotud. Teenustel võivad olla versioonid, sõltumatult infosüsteemist kui terviku versioonidest.

Teenuse WSDL jms info esitatakse `json_content` väljal, mis sisaldab mh ka kõiki ülaltoodud SQL-lauses antud klassikalisi välju.

Teenuse sisendid ja väljundid kirjeldatakse lisaks WSDL-s toodud struktuurile ka nende klassifikaatorite, sõnastike ja muu semantika jaoks eraldi igaüks ühe `data_object` reaga, mille `kind` on `input` või `output` ja mille `main_resource_id` on teenuse `id` ja mille `uri` on kujul `riha:service:teenusenimi:_input:_sisendinimi`.

Sisendite ja väljundite järjekord esitatud ei ole, konkreetne struktuur on üldjuhul loetav WSDL failist.

## Valdkondade, sõnastike ja XML varade kirjeldamine

Nende kolme põhiressursi edasine konkreetne kasutus- ja haldamisviis vajab detailset ärianalüüsi. Põhiküsimused seejuures on:

- Millises kirjelduskeeles jätkata valdkonnasõnastike esitamist: kas minna OWL-lt üle lihtsamale RDFS-le või mõnele muule kirjelduskeelele?
- Kas asuda valdkonnasõnastike juures baashierarhiana kasutama olemasolevat http://schema.org hierarhiat ja kui, siis mis moel integreerida sinna eesti hierarhiad?
- Kes ja kuidas valdkonnasõnastikke haldab: administratiivselt ja tehniliselt?
- Kas ja kuidas nö lahti-struktureerida klassifikaatorite senised Excel-failid konkreetseteks, töödeldavateks väärtuseloenditeks?
- Kuidas esitada klassifikaatoreid lahtistruktureeritud kujul?
- Kes ja kuidas klassifikaatoreid haldab: administratiivselt ja tehniliselt?
- Kas edaspidi on vaja XML-varade haldamist, või võib selle põhiressursi aktiivsest RIHA-st eemaldada?

Seetõttu ei esita me praeguses etapis veel põhimõtteliselt uut viisi nende olemite kodeerimiseks. Küll aga esitame nende kodeerimise nö ajutisel, lihtsustatud moel `main_resource` tabelisse, mis toob samas kaasa minimaalse hulga muutusi andmebaasi struktuuri.

Valdkonnasõnastike ja klassifikaatorite praktiline kasutamine andmetabelite, väljade ja teenuste kirjeldamisel edaspidi valitud kodeerimisviisist ei sõltu: praktiline kasutamine tähendab alati tekstiliste tagide lisamist kirjeldustele, mis ei sõltu sõnastiku või klassifikaatori esitamisest RIHA andmebaasi struktuuris. Tekstiliste tagide kasutamise põhimõtted on esitatud eraldi dokumendis, mis kirjeldab RIHA objektide välist esitust JSON-kujul.

Ajutine, lihtsustatud kirjeldus uue RIHA andmebaasis:

- Valdkonnasõnastikud, teenused ja XML varad lisatakse `main_resource` tabelisse, tekitades igaühele uue uri kujul a la `RIHA:classifier:classifierid`, kus `classifierid` on senise klassifikaatori `id` senises RIHAs.
- Kogu senine põhiobjekti kirjeldus lisatakse `json_content` väljale JSON nimi-väärtus paaride ojektina täpselt sellisel viisil ja selliste väljanimedega, nagu ta on olemas senises RIHAs, pluss `main_resource` tabelist tulenevad väljad. Kui mõni `main_resource` väljanimi kattub senise väljanimega, siis nimetatakse senine ümber prefiksiga `legacy_...`.
- Põhiobjekti kirjelduses olevad senised väljasisud jäävad muutmata.
- Põhiobjekti abitabelid ja nende sisu jäävad muus osas muutmata, kui nad aga sisaldavad välisvõtit senisele põhiobjektile viitamiseks, siis lisatakse neile täiendavalt väli `main_resource_uri`, mis täidetakse vastava senise põhiobjekti uue uri välja sisuga kujul a la `RIHA:classifier:classifierid`, kus `classifierid` on senise põhiobjekti `id` senises RIHAs. See võimaldab kasutada senist objekti versioneeritud kujul.

## Versioneerimine

Kirjeldame järgnevas infosüsteemi versioneerimist, kuid samad põhimõtted kehtivad ka teiste ressursside (teenused, ..., XML varad) kohta.

Infosüsteemi kirjeldusi versioneeritakse eeskätt selleks, et võimaldada infosüsteemi kirjeldust jooksvalt uuendada, tuvastades seejuures tekkinud erinevused viimasest kooskõlastatud seisus olevast kirjeldusest.

### Versioneerimise stsenaariumid

Oletame, et kuupäeval X saab infosüsteem kooskõlastuse, olles näiteks versiooniga V. Pärast seda infosüsteem areneb ja tema kirjeldust uuendatakse. Uuendamine võib olla küllalt sage, kui seda tehakse automaatselt API kaudu.

Iga kirjelduse uuendus ei too üldjuhul kaasa olukorda, et kooskõlastus enam ei kehti. Samas peab olema võimalik suhteliselt lihtsalt tuvastada, milliseid muutusi on infosüsteemi kirjelduses toimunud peale kooskõlastamiskuupäeva X.

Kui muutuste hulk osutub aja jooksul väga suureks, lisanduvad näiteks uued isikuandmete ja aadressandmete tabelid, siis võib kas infosüsteemi eest vastutaja ise või mõni kooskõlastaja leida, et süsteem vajab uuesti kooskõlastamist. Seejuures võib olla mõistlik realiseerida automaatsed kontrollreeglid, mis osutavad, et infosüsteemi muutused on sedavõrd suured/olulised, et tasuks kaaluda uue versiooni loomist ja uut kooskõlastamist.

Uue kooskõlastamise vajaduse tuvastamise puhul oleks mõistlik hakata infosüsteemi uuesti kooskõlastama. Seejuures on abiks, kui saab endiselt kergesti näha erinevusi viimase kooskõlastatud versiooni ja hetkeseisu vahel.

Kui kooskõlastamine võtab pikemalt aega, siis infosüsteemi kirjeldused muutuvad tõenäoliselt ka uue kooskõlastamise käigus, näiteks parandatakse ja täiustatakse infosüsteemi ja tema kirjeldusi vastavalt kooskõlastaja nõuetele. Seega tasub uus versioon V+1 üldjuhul luua alles siis, kui kooskõlastamine on edukalt lõppenud, mis tähendab, et kooskõlastatakse jooksvalt arenevat viimast hetkeseisu.

Samuti on võimalik stsenaarium, kus infosüsteemi eest vastutaja ise leiab, et on toimunud piisavalt suured muutused selleks, et luua kohe uus „vaheversioon" V+1, mis salvestaks hetkeseisu. Kooskõlastust hakatakse siis ikkagi küsima selle vaheversiooni jooksvalt arenevale kirjeldusele, ning kooskõlastuse saamise järel fikseeritakse seis järgmise versiooniga V+2.

Kokkuvõttes:

- Kriitiline versioneerimise eesmärk on kergesti leida, mis on erinevused viimase kooskõlastatud kirjelduse ja hetkekirjelduse vahel.
- Lisaeesmärk on see, et vaheseisu saab infosüsteemi eest vastutaja soovi korral säilitada, luues ise uue versiooni. Kõigi vaheseisude jooksev säilitamine ei ole eesmärk. Automaatselt uusi versioone ei tekitata. Kui hiljutine kirjelduse muutus uuesti muudetakse, siis vaheseis kirjutatakse üle ja ei ole taastatav, tingimusel, et pole spetsiaalselt loodud vaheversiooni.

### Versioneerimise tehnoloogia

Versioneeritavad põhitabelid infosüsteemi kirjelduses on:

- `main_resource` sisaldab infosüsteemi kirjelduse üldisel kõrgtasemel: üks rida on üks infosüsteem;
- `data_object` sisaldab erinevate detail-andmeobjektide (andmebaasid, tabelid, tulbad, teenuste sisendid-väljundid) kirjeldusi: üks rida on üks andmeobjekt. Iga andmeobjekt viitab infosüsteemi id-le.

Kummagi tabeli juures tuleb ette näha kahte võimalust muutusteks:

- inim-kasutaja uuendab mõnda välja
- API kaudu laetakse terve infosüsteemi kirjeldus automaatselt uuesti, seejuures võib mõni väli muutuda/mõni tabel lisanduda, kuid kogu sisu võib ka muutumatuks jääda

Automaatse kirjelduse laadimise juures tuleb ette näha infosüsteemi unikaalne nimi , mille API ette annab. Meie soovituseks on kasutada URI, milleks võib kasutada ka infosüsteemi URLi, kuid ei pruugi seda kasutada.

`main_resource` tabeli versioneerimiseks kasutame järgmisi põhimõtteid:

- Infosüsteemi identifikaatoriks on tabeli id väli, lisaidentifikaatoriks on tabeli uri väli.
- Infosüsteemi esialgselt loodud id püsib alati edaspidigi, loodavad versioonid on nö mitte-muudetavad vahekoopiad, mis saavad uue id.
- Sama URI-ga ja sama id-ga (sisuliselt samal) infosüsteemil võib infosüsteemide tabelis olla hulk ridu: ajaliselt esimesena loodud rida on alati aktiivne hetkeseis, kõik uuemad read on mitteaktiivsed vaheversioonid
- Vaheversioonide ridu ei muudeta
- Infosüsteemi mõne välja muutumise korral (inimese või API poolt) muudetakse lihtsalt ära aktiivse hetkeseisu mõni väli; muutuste ahelad lähevad seejuures kaduma, kui spetsiaalselt ei looda vaheversiooni.
- Infosüsteemi real on alati kehtivuse alguse ajatempli ja lõppemise ajatempli väli
- Infosüsteemi uue versiooni tekitab ainult inimkasutaja (kas siis kooskõlastaja või infosüsteemi eest vastutaja), mille käigus:* luuakse kehtivast reast koopia, mis saab uue id , kuid säilib senine uri väli: uus koopia vastab viimasele kehtinud versioonile, ning ei kuulu muutumisele, senine id vastab endiselt hetkeseisule.
  - muudetakse uue loodud koopia-rea kehtivuse lõpukuupäev hetkekuupäevaks.
  - hetkeseisu rea kehtivuse alguskuupäev muudetakse hetkekuupäevaks
  - luuakse koopiad (arvestades optimeeringuid, mis kirjas järgnevas) kõigist antud infosüsteemi senikehtinud reale viitavatest data_object tabeli ridadest: iga koopia saab uue id ja hakkab viitama infosüsteemi äsjaloodud vaheversiooni id-le, samuti muudetakse uueks vanem-andmeobjekt-id viidad (andmeobjektid moodustavad hierarhilise süsteemi).

Kokkuvõttes paneme tähele, et see infosüsteemi rida, mis on loodud esimesena ja millel on kõige väiksem `id`, on alati hetkel kehtiv ja muudetav seis. Uuemad read suuremate `id`-dega vastavad vaheversioonidele, mis salvestavad mingi hetkeseisu. Versioonide kehtivusajad on määratud kehtivusaja alguse ja lõpu timestamp väljadega.

Andmeobjekte sisaldava `data_object` tabeli uuendamisel kasutame järgmisi põhimõtteid:

- Tabeli ridadest luuakse uued versioonid juhul, kui luuakse uus infosüsteemi vaheversioon (vt ülal) kuid mitte juhul, kui lihtsalt uuendatakse andmeobjektide kirjeldust või struktuuri: viimasel juhul lihtsalt muudetakse viimast hetkeseisu.
- Andmeobjekti struktuuri uuendamisel API kaudu lähtutakse põhimõttest, et objekti identifitseerib temani viivate nimede ahel (üldjuhul infosüsteemi uri -> andmebaasi nimi -> tabeli nimi -> välja nimi) . Seda nimede ahelat käsitletakse kui andmeobjekti URI ja hoitakse andmeobjekti tabeli vastaval uri väljal.
- Andmeobjekti väljal on alati kehtivuse alguse ajatempli ja lõpu ajatempli väli.
- Infosüsteemist (`main_resource` tabel) uue versiooni loomisel kasutatakse järgmist optimeeringut ridade arvu kokkuhoiuks: Olgu äsjaloodud infosüsteemi eelmine versioon V, hetkel loodav vaheversioon V+1 ja uus hetkeversioon V+2. Vaheversiooni V+1 jaoks tuleks luua koopiad kõigist hetkeversiooni ridadest. Kui aga eksisteerib eelmine versioon V ja V-l on olemas hetkeseisuga identse sisuga (v.a. id-d ja timestampid) andmeobjekti rida R, siis uuendatakse R-i kehtivuse lõpu timestampi . See optimeering tekitab küll olukorra, kus andmeobjekti rida ei pruugi viidata temale vastavale infosüsteemi versioonile, vaid varasemale, ning kehtivus tuleb tuvastada timestampide vahemiku kaudu.

Analoogiliselt `main_resource` tabelile paneme tähele, et see andmeobjekti (`data_object`) tabeli rida, mis on loodud esimesena ja millel on kõige väiksem `id`, on alati hetkel kehtiv ja muudetav seis. Uuemad read suuremate `id`-ga vastavad vaheversioonidele, mis salvestavad mingi fikseeritud ja mittemuudetava seisu. Versioonide kehtivusajad on määratud kehtivusaja alguse ja lõpu ajatempli väljadega. Kõikidele infosüsteemi (`main_resource tabel`) vaheversioonidele vastavaid andmeobjekti-välju ei pruugi seejuures füüsiliselt eksisteerida: pikkade ahelate puhul taaskasutatakse varasemaid andmeobjekti-ridu, nihutades nende kehtivuse lõpu ajatempleid vastavalt edasi.
