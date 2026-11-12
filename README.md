# Aginte-koadroa — Artificial Unity

Unax Muñoz eta Aritz Alonso. Interfazeen Garapena. 2026ko azaroa.

**Aginte-koadroa** Artificial Unity enpresako arduradunarentzako mahaigaineko aplikazioa da (Java 21 eta JavaFX).
Enpresaren kudeaketa-programako (Odoo) datuak irakurtzen ditu zuzenean eta modu errazean erakusten ditu:
zenbat saldu den, nola doazen salmentak hilez hil, zer produktu saltzen diren gehien, nork erosten duen gehien,
zer falta den biltegian eta zer dagoen kobratzeko. Informatikan ohitua ez dagoen pertsona batentzat pentsatuta dago:
botoi handiak, letra handiak, hizkera teknikorik gabe, laguntza (F1) eta azalpenak sagua gainean jarrita.

Zazpi pantaila ditu: **Laburpena** (adierazle-txartelak eta abisuak), **Salmenten bilakaera** (lerro-grafikoa
hurrengo hilabeteko aurreikuspenarekin), **Produktu salduenak** (barrak), **Bezeroak**, **Kategoriak**, **Stocka**
(stock baxuko abisuarekin) eta **Kobratzeko fakturak**. Data-tarteka iragaz daiteke, eta txostenak deskarga daitezke
**PDF**-n eta **CSV**-n (Excel).

Aplikazioak **ez du ezer aldatzen** Odoo-n: irakurtzeko soilik den datu-baseko erabiltzaile batekin konektatzen da.

---

## 1. ZIParen edukia

```
README.md                          fitxategi hau
exekutagarria/
├─ aginte-koadroa.jar              aplikazioa (JavaFX, OpenPDF eta PostgreSQL driverra barne)
└─ konfigurazioa.properties        konexioaren konfigurazioa, erabiltzeko prest
iturburu_kodea/                    IntelliJ IDEA-rako Maven proiektua
├─ pom.xml                         menpekotasunak eta eraikuntza (.jar bakarra sortzen du)
├─ mvnw, mvnw.cmd, .mvn/           Maven Wrapper (ez da Maven instalatu behar)
├─ konfigurazioa.properties.adibidea
├─ src/main/java/                  kodea: eus.artificialunity.agintekoadroa (MVC + DAO, 55 klase)
├─ src/main/resources/             FXML bistak, CSS estiloak, logoa eta ikonoa
├─ src/test/java/                  JUnit 5 probak (14)
└─ README.md, egiaztapena/, ...    modulu honen README teknikoa eta zifrak egiaztatzeko tresnak
dokumentazioa/
├─ 01_dokumentazio_teknikoa.pdf    arkitektura, klaseak, kontsultak, osagaiak, erroreak, probak
├─ 02_erabiltzaile_eskuliburua.pdf erabiltzailearen eskuliburua, argazkiekin
├─ 03_erabilgarritasun_ebaluazioa.pdf  ebaluazio heuristikoa (aurretik/ondoren) eta SUS proba
└─ adibideak/                      aplikazioak sortutako PDF txostena eta CSV fitxategi bat
```

`target`, `.idea` eta `logs` karpetak ez daude: IntelliJ-k eta Maven-ek berriro sortzen dituzte.

---

## 2. Betekizunak

- **Windows 10 edo 11, 64 bitekoa** (`.jar`-ak Windows-erako JavaFX liburutegiak ditu barruan).
- **Java 21** (JDK edo JRE). Egiaztatzeko, PowerShell-en: `java -version` → `21.x` agertu behar da.
  Ez da JavaFX bereiz instalatu behar: `.jar`-aren barruan dago.
- **Zerbitzariaren sarean egotea**: PCak zerbitzariaren PostgreSQL-era (5432 ataka) iritsi behar du, eta
  `zerbitzaria.artificialunity.eus` izena `hosts` fitxategian egon behar da (ikus 5. atala).

---

## 3. Nola exekutatu (urratsez urrats)

1. Kopiatu `exekutagarria` karpeta PCra, adibidez `C:\ArtificialUnity\AginteKoadroa\`.
   `aginte-koadroa.jar` eta `konfigurazioa.properties` **karpeta berean** egon behar dira.
2. Egiaztatu `hosts` fitxategiko lerroa (5. atala).
3. Ireki PowerShell karpeta horretan eta exekutatu:

   ```powershell
   cd C:\ArtificialUnity\AginteKoadroa
   java -jar aginte-koadroa.jar
   ```

   (Java-k `.jar` fitxategiak irekitzen baditu, klik bikoitza ere nahikoa da.)
4. Leihoa irekitzen da, "Datuak kargatzen..." agertzen da segundo batzuetan eta ondoren **Laburpena** pantaila,
   azken 12 hilabeteetako datuekin.

Komando-lerroko aukerak (aukerakoak):

| Aukera | Zertarako |
|---|---|
| `--pantaila=stocka` | Pantaila zehatz batean hasi (`laburpena`, `bilakaera`, `produktuak`, `bezeroak`, `kategoriak`, `stocka`, `fakturak`). |
| `--esportatu=<karpeta>` | Leihorik ireki gabe, PDF txostena eta 7 CSV-ak karpeta horretan sortu eta irten. |
| `--hasiera=AAAA-HH-EE --amaiera=AAAA-HH-EE` | `--esportatu`-rekin, data-tartea (lehenetsia: azken 12 hilabeteak). |
| `-Dkonfigurazioa=<bidea>` | Beste konfigurazio-fitxategi bat erabili (`-jar`-en aurretik jarri). |

Adibidea: `java -jar aginte-koadroa.jar --esportatu=C:\Txostenak`.

Abiarazleak (Artificial Unity-ren C# programa, Langilea modua) `.jar` hau automatikoki abiarazten du.

---

## 4. Konfigurazioa (`konfigurazioa.properties`)

Konexio-datuak ez daude kodean: **`konfigurazioa.properties`** fitxategian daude. Aplikazioak ordena honetan bilatzen du:

1. `-Dkonfigurazioa=<bidea>` aukeran adierazitakoa;
2. **`.jar`-aren karpeta bera** (gomendatua);
3. uneko karpeta.

ZIPeko `exekutagarria\konfigurazioa.properties` erabiltzeko prest dago: zerbitzaria izenez
(`zerbitzaria.artificialunity.eus`) eta irakurtzeko soilik den `aginte_irakurlea` erabiltzailea.
Eredua (pasahitzik gabe) `iturburu_kodea\konfigurazioa.properties.adibidea` da.

| Aukera | Balioa | Esanahia |
|---|---|---|
| `db.ostalaria` | `zerbitzaria.artificialunity.eus` | Zerbitzariaren izena (PostgreSQL, Odoo-ren datu-basea). IPa ez jartzea gomendatzen da: izena `hosts` fitxategian ebazten da. |
| `db.ataka` | `5432` | PostgreSQL-en ataka. |
| `db.izena` | `odoo_enpresa` | Odoo-ren datu-basea. |
| `db.erabiltzailea` | `aginte_irakurlea` | Irakurtzeko soilik den erabiltzailea (SELECT baimena bakarrik). |
| `db.pasahitza` | (fitxategian) | Erabiltzaile horren pasahitza. |
| `db.konexio_denbora` | `8` | Zerbitzariarekin konektatzeko itxaron gehienez, segundotan. |
| `db.kontsulta_denbora` | `30` | Kontsulta bakoitzaren itxaronaldia gehienez, segundotan. |
| `produktuak.kopurua` | `10` | "Produktu salduenak" pantailan erakusten den produktu kopurua (1–50). |

Fitxategia aldatu ondoren ez da aplikazioa itxi behar: errore-pantailako **Berriro saiatu** botoiak (edo **F5**)
konfigurazioa berriro irakurtzen du.

---

## 5. IPak eta izenak

Aplikazioak zerbitzaria **izenez** bilatzen du (`zerbitzaria.artificialunity.eus`), eta izen hori Windows-en
**`hosts` fitxategiarekin** ebazten da: `C:\Windows\System32\drivers\etc\hosts` (Notepad **administratzaile gisa** irekita editatu).

**Zerbitzariaren IPa: `192.168.80.115`.** Gehitu lerro hau `hosts` fitxategiaren amaieran:

```
192.168.80.115 zerbitzaria.artificialunity.eus odoo.artificialunity.eus intranet.artificialunity.eus
```

Ondoren, PowerShell edo CMD-n: `ipconfig /flushdns`. Zerbitzarian bertan exekutatzen bada, IParen ordez `127.0.0.1`.

**IPa beste bat bada:** zerbitzariak IPa DHCP bidez jaso dezake. Zerbitzarian, begiratu IP berria `ipconfig`
komandoarekin ("Ethernet" interfazea) eta aldatu IPa **`hosts` lerroan bakarrik**. Ez da ezer aldatu behar ez
`konfigurazioa.properties`-en, ez aplikazioan. (`hosts` fitxategia aldatu ezin bada, `db.ostalaria`-n IPa zuzenean
jar daiteke, baina IPa aldatzen den bakoitzean berriro aldatu beharko litzateke.)

---

## 6. Konektatzen ez badu

Aplikazioak mezu argia erakusten du, **Berriro saiatu** botoiarekin. Xehetasun teknikoak "Xehetasun teknikoak"
atalean eta `logs\aginte_koadroa_AAAAHHEE.log` fitxategian daude (`.jar`-aren karpetan).

| Mezua | Zer egin |
|---|---|
| **Ezin da zerbitzariarekin konektatu** | PCa sarean dagoela eta zerbitzaria piztuta dagoela egiaztatu. `hosts` lerroa zuzena den begiratu (5. atala). PowerShell-en: `Test-NetConnection zerbitzaria.artificialunity.eus -Port 5432` → `TcpTestSucceeded : True` agertu behar da. `False` bada: IP okerra, zerbitzaria itzalita edo zerbitzariaren suebakiak 5432 ataka blokeatzen du. |
| **Zerbitzariak ez du sarbidea onartu** | Erabiltzailea edo pasahitza okerra da, edo `db.izena` ez dago zerbitzarian: zuzendu `konfigurazioa.properties`. |
| **Konfigurazioan arazo bat dago** / ez da konfigurazio-fitxategia aurkitu | `konfigurazioa.properties` ez dago `.jar`-aren karpetan edo balio bat okerra da: mezuak fitxategia eta lerroa adierazten ditu. |
| **Zerbitzariak ez du garaiz erantzun** | Sarea motel dago: saiatu berriro edo handitu `db.kontsulta_denbora`. |
| **Ezin izan da fitxategia gorde** (PDF/CSV) | Fitxategia beste programa batean irekita dago (adib. Excel): itxi edo beste karpeta bat aukeratu. |
| Aplikazioa ez da irekitzen | `java -version`-ek 21 erakusten duela egiaztatu; PowerShell-etik `java -jar aginte-koadroa.jar` exekutatu errorea ikusteko. |

---

## 7. IntelliJ IDEA-n ireki, konpilatu eta `.jar`-a berriro sortu

**Behar dena:** IntelliJ IDEA (Community nahikoa) eta **JDK 21**. Maven ez da instalatu behar: proiektuak
**Maven Wrapper** dakar (`mvnw.cmd`), eta lehen aldian Maven 3.9.9 deskargatzen du (Internet behar da lehen aldian,
Maven eta menpekotasunak deskargatzeko).

### Ireki

1. **File → Open** → hautatu `iturburu_kodea` karpeta (edo bertako `pom.xml`) → **Open as Project**.
   Maven proiektu gisa irekitzen da eta menpekotasunak deskargatzen ditu.
2. **File → Project Structure → Project → SDK**: JDK 21 (beharrezkoa bada, *Add SDK → JDK* eta `C:\Program Files\Java\jdk-21`).
3. Kopiatu `konfigurazioa.properties.adibidea` → `konfigurazioa.properties` (`iturburu_kodea` karpetan bertan) eta jarri
   pasahitza, edo kopiatu bertara `exekutagarria\konfigurazioa.properties`. IntelliJ-tik exekutatzean uneko karpeta
   proiektuaren karpeta da, eta hor bilatzen du.

### Exekutatu eta probak

- **Exekutatu:** Maven leihoan *Plugins → javafx → javafx:run* (bikoitza klik), edo `eus.artificialunity.agintekoadroa.Abiarazlea`
  klasean eskuineko botoia → **Run 'Abiarazlea.main()'**.
- **Probak:** `src/test/java` karpetan eskuineko botoia → **Run 'All Tests'** (14 proba, JUnit 5), edo Maven leihoan *Lifecycle → test*.

### `.jar`-a berriro sortu

IntelliJ-ko Maven leihoan: *Lifecycle → clean* eta gero *Lifecycle → package*. Edo PowerShell-en, `iturburu_kodea` karpetan:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'   # JDK 21 lehenetsia ez bada
.\mvnw.cmd clean package
```

Emaitza: `target\aginte-koadroa.jar` (~15 MB, dena barne: JavaFX, OpenPDF eta PostgreSQL driverra), 14 probak pasatu ondoren.
Banatzeko, kopiatu `.jar`-a eta `konfigurazioa.properties` karpeta berera, `exekutagarria`-n bezala.

---

## 8. Dokumentazioa

- `dokumentazioa\01_dokumentazio_teknikoa.pdf`: MVC + DAO arkitektura, paketeak eta klaseak, datu-kontsultak,
  osagai pertsonalizatuak, ekitaldiak eta haria, erroreen kudeaketa, aurreikuspena, txostenak, konfigurazioa eta probak.
- `dokumentazioa\02_erabiltzaile_eskuliburua.pdf`: arduradunarentzako eskuliburua, pantailaz pantaila argazkiekin.
- `dokumentazioa\03_erabilgarritasun_ebaluazioa.pdf`: barne-ebaluazio heuristikoa (arazoak eta hobekuntzak,
  aurretik/ondoren argazkiekin) eta erabiltzaileekin egiteko SUS proba (atazak, galdetegia eta emaitzen txantiloia).
- `dokumentazioa\adibideak\`: aplikazioak 2026-11-12an datu errealekin sortutako PDF txostena eta CSV bat
  (salmentak hileka; `;` bereizlea, Excel-ek zuzenean irekitzen du).
