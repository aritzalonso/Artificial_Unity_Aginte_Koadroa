# Aginte-koadroa (Artificial Unity) — JavaFX

Enpresako arduradunarentzako mahaigaineko aplikazioa: salmenten bilakaera, produktu salduenak, bezeroak,
kategoriak, stocka eta kobratzeko fakturak, Odoo-ren datuekin (PostgreSQL, irakurtzeko soilik).
PDF eta CSV txostenak deskargatzen ditu.

- Java 21, JavaFX 21.0.5, OpenPDF 2.0.3, PostgreSQL JDBC 42.7.4, JUnit 5.
- Paketea: `eus.artificialunity.agintekoadroa` (MVC + DAO).
- Dokumentazio osoa: `dokumentazioa/3_aginte_koadroa/` (teknikoa, erabiltzailearen eskuliburua, erabilgarritasuna).

## Behar dena

- **JDK 21** (`JAVA_HOME` JDK 21-era begira, edo `java` PATHean). Maven EZ da behar: proiektuak **Maven Wrapper** dakar
  (`mvnw` / `mvnw.cmd`); lehen aldian Maven 3.9.9 deskargatzen du `%USERPROFILE%\.m2\wrapper`-era (SHA-256 egiaztatuta).
- Sarbidea zerbitzariaren PostgreSQL-era (`zerbitzaria.artificialunity.eus:5432`, `aginte_irakurlea` erabiltzailea; izena PCaren `hosts` fitxategian ebazten da).

## Konfigurazioa

```powershell
Copy-Item konfigurazioa.properties.adibidea konfigurazioa.properties
notepad konfigurazioa.properties     # db.pasahitza eta, behar bada, db.ostalaria aldatu
```

Fitxategia **.jar-aren karpeta berean** jarri (edo uneko karpetan, edo `-Dkonfigurazioa=<bidea>`).
`konfigurazioa.properties` ez dago Git-en (pasahitza du). Aukera guztiak: `dokumentazioa/3_aginte_koadroa/01_dokumentazio_teknikoa.md`.

## Eraiki eta exekutatu

```powershell
cd C:\odoo\aginte-koadroa
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'   # JDK 21 ez bada lehenetsia

.\mvnw.cmd clean package          # konpilatu + 14 proba + target\aginte-koadroa.jar (dena barne)
java -jar target\aginte-koadroa.jar

.\mvnw.cmd javafx:run             # garapenean, .jar sortu gabe
```

Linux/macOS: `./mvnw clean package`. Oharra: .jar-ak Windows-erako JavaFX liburutegiak ditu (eraiki den sistemarenak).

### .jar exekutagarria (JavaFX barne)

`maven-shade-plugin`-ek mendekotasun guztiak (JavaFX Windows-eko liburutegi natiboekin, OpenPDF, JDBC driverra)
`target/aginte-koadroa.jar` bakarrean sartzen ditu. Sarrera-klasea `Abiarazlea` da (ez `Application`-en azpiklasea),
JavaFX klase-bidetik kargatu ahal izateko: `java -jar` nahikoa da, `--module-path` gabe.
Banatzeko: `aginte-koadroa.jar` + `konfigurazioa.properties` karpeta berean (adib. `C:\AginteKoadroa\`).
Abiarazleak (5. modulua) `java -jar` horrekin abiarazten du.

### Komando-lerroko aukerak

| Aukera | Zertarako |
|--------|-----------|
| `--pantaila=stocka` | Pantaila horretan hasi (`laburpena`, `bilakaera`, `produktuak`, `bezeroak`, `kategoriak`, `stocka`, `fakturak`) |
| `--esportatu=<karpeta>` | Leihorik ireki gabe PDFa eta 7 CSV-ak sortu eta irten (lehenetsia: azken 12 hilabeteak) |
| `--hasiera=AAAA-HH-EE --amaiera=AAAA-HH-EE` | `--esportatu`-rekin, data-tartea |
| `-Dkonfigurazioa=<bidea>` | Beste konfigurazio-fitxategi bat erabili |

## IntelliJ IDEA

*File → Open* → `aginte-koadroa` karpeta (pom.xml) → Maven proiektu gisa irekitzen da. Project SDK: 21.
Exekutatzeko: Maven leihoan *Plugins → javafx → javafx:run*, edo `Abiarazlea` klasea (*Run*), lan-karpeta `aginte-koadroa`
izanik (konfigurazioa hor bilatzen da).

## Egiaztapena

`egiaztapena/` karpetan zifrak egiaztatzeko tresnak daude (ez dira aplikazioaren parte): `Egiaztapena.java`-k
`sql/aginte_kontsultak.sql` exekutatzen du bere horretan eta `alderatu.py`-k emaitza aplikazioak esportatutako
CSV-ekin alderatzen du. 2026-10-26an: **dena bat dator** (KPIak, 12 hilabete, 10 produktu, 8 bezero, 16 stock-lerro,
24 faktura, 5 kategoria).

```powershell
java -jar target\aginte-koadroa.jar --esportatu=target\egiaztapena
java -cp $env:USERPROFILE\.m2\repository\org\postgresql\postgresql\42.7.4\postgresql-42.7.4.jar `
     egiaztapena\Egiaztapena.java ..\sql\aginte_kontsultak.sql 2025-11-01 2026-10-26 |
     Out-File -Encoding utf8 target\egiaztapena\sql_emaitza.txt
python egiaztapena\alderatu.py target\egiaztapena target\egiaztapena 2026-10-26
```

Errore-mezua probatzeko (existitzen ez den zerbitzaria):
`java -Dkonfigurazioa=konfigurazioa_proba_errorea.properties -jar target\aginte-koadroa.jar`

## Logak

Erroreak `logs/aginte_koadroa_AAAAHHEE.log` fitxategian (.jar-aren karpetan) gordetzen dira.
