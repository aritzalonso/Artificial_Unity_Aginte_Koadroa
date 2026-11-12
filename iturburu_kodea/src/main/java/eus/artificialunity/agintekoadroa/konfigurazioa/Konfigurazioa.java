package eus.artificialunity.agintekoadroa.konfigurazioa;

import eus.artificialunity.agintekoadroa.utilak.AplikazioKarpeta;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Aplikazioaren konfigurazioa, kanpoko fitxategi batetik irakurrita (konfigurazioa.properties).
 * Datu-basearen helbidea, erabiltzailea eta pasahitza ez daude kodean.
 * <p>
 * Bilaketa-ordena: -Dkonfigurazioa=&lt;bidea&gt; sistema-propietatea, .jar-aren karpeta eta uneko karpeta.
 */
public final class Konfigurazioa {

    public static final String FITXATEGI_IZENA = "konfigurazioa.properties";
    public static final String SISTEMA_PROPIETATEA = "konfigurazioa";

    private final Path fitxategia;
    private final String ostalaria;
    private final int ataka;
    private final String datuBasea;
    private final String erabiltzailea;
    private final String pasahitza;
    private final int konexioDenbora;
    private final int kontsultaDenbora;
    private final int produktuKopurua;

    private Konfigurazioa(Path fitxategia, Properties p) throws KonfigurazioSalbuespena {
        this.fitxategia = fitxategia;
        this.ostalaria = beharrezkoa(p, "db.ostalaria");
        this.ataka = zenbakia(p, "db.ataka", 5432, 1, 65535);
        this.datuBasea = beharrezkoa(p, "db.izena");
        this.erabiltzailea = beharrezkoa(p, "db.erabiltzailea");
        this.pasahitza = p.getProperty("db.pasahitza", "").trim();
        this.konexioDenbora = zenbakia(p, "db.konexio_denbora", 8, 1, 120);
        this.kontsultaDenbora = zenbakia(p, "db.kontsulta_denbora", 30, 1, 600);
        this.produktuKopurua = zenbakia(p, "produktuak.kopurua", 10, 1, 50);
    }

    /** Konfigurazio-fitxategia bilatu eta kargatzen du. */
    public static Konfigurazioa kargatu() throws KonfigurazioSalbuespena {
        List<Path> hautagaiak = hautagaiak();
        for (Path bidea : hautagaiak) {
            if (Files.isRegularFile(bidea)) {
                return kargatu(bidea);
            }
        }
        throw new KonfigurazioSalbuespena("Ez da konfigurazio-fitxategia aurkitu (" + FITXATEGI_IZENA + ").\n"
                + "Kopiatu \"" + FITXATEGI_IZENA + ".adibidea\" fitxategia \"" + FITXATEGI_IZENA
                + "\" izenarekin karpeta honetan:\n" + AplikazioKarpeta.bidea());
    }

    /** Fitxategi jakin bat kargatzen du. */
    public static Konfigurazioa kargatu(Path bidea) throws KonfigurazioSalbuespena {
        Properties p = new Properties();
        try (Reader irakurlea = Files.newBufferedReader(bidea, StandardCharsets.UTF_8)) {
            p.load(irakurlea);
        } catch (IOException e) {
            throw new KonfigurazioSalbuespena("Ezin izan da konfigurazio-fitxategia irakurri:\n" + bidea, e);
        }
        return new Konfigurazioa(bidea.toAbsolutePath().normalize(), p);
    }

    private static List<Path> hautagaiak() {
        Set<Path> bideak = new LinkedHashSet<>();
        String propietatea = System.getProperty(SISTEMA_PROPIETATEA);
        if (propietatea != null && !propietatea.isBlank()) {
            bideak.add(Path.of(propietatea).toAbsolutePath().normalize());
        }
        bideak.add(AplikazioKarpeta.bidea().resolve(FITXATEGI_IZENA));
        bideak.add(Path.of(System.getProperty("user.dir")).resolve(FITXATEGI_IZENA).toAbsolutePath().normalize());
        return new ArrayList<>(bideak);
    }

    private static String beharrezkoa(Properties p, String gakoa) throws KonfigurazioSalbuespena {
        String balioa = p.getProperty(gakoa, "").trim();
        if (balioa.isEmpty()) {
            throw new KonfigurazioSalbuespena("Konfigurazio-fitxategian \"" + gakoa + "\" balioa falta da.");
        }
        return balioa;
    }

    private static int zenbakia(Properties p, String gakoa, int lehenetsia, int min, int max)
            throws KonfigurazioSalbuespena {
        String balioa = p.getProperty(gakoa, "").trim();
        if (balioa.isEmpty()) {
            return lehenetsia;
        }
        try {
            int zenbakia = Integer.parseInt(balioa);
            if (zenbakia < min || zenbakia > max) {
                throw new NumberFormatException();
            }
            return zenbakia;
        } catch (NumberFormatException e) {
            throw new KonfigurazioSalbuespena("Konfigurazio-fitxategian \"" + gakoa + "\" balioak "
                    + min + " eta " + max + " arteko zenbakia izan behar du (orain: \"" + balioa + "\").");
        }
    }

    /** JDBC helbidea, adibidez jdbc:postgresql://zerbitzaria.artificialunity.eus:5432/odoo_enpresa */
    public String jdbcUrl() {
        return "jdbc:postgresql://" + ostalaria + ":" + ataka + "/" + datuBasea;
    }

    public Path getFitxategia() { return fitxategia; }
    public String getOstalaria() { return ostalaria; }
    public int getAtaka() { return ataka; }
    public String getDatuBasea() { return datuBasea; }
    public String getErabiltzailea() { return erabiltzailea; }
    public String getPasahitza() { return pasahitza; }
    public int getKonexioDenbora() { return konexioDenbora; }
    public int getKontsultaDenbora() { return kontsultaDenbora; }
    public int getProduktuKopurua() { return produktuKopurua; }
}
