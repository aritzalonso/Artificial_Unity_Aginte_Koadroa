package eus.artificialunity.agintekoadroa.utilak;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Erroreak eta gertaerak fitxategi batean gordetzen ditu: logs/aginte_koadroa_AAAAHHEE.log
 * (.jar-aren karpetan). Informatikariak arazoak aztertzeko erabiltzen du.
 */
public final class Erregistroa {

    private static final Logger ERRO_LOGGER = Logger.getLogger("eus.artificialunity.agintekoadroa");
    private static boolean hasieratuta;

    private Erregistroa() {
    }

    public static synchronized void hasieratu() {
        if (hasieratuta) {
            return;
        }
        hasieratuta = true;
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tF %1$tT %4$-7s %3$s - %5$s%6$s%n");
        try {
            Path karpeta = AplikazioKarpeta.bidea().resolve("logs");
            Files.createDirectories(karpeta);
            String izena = "aginte_koadroa_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".log";
            FileHandler fitxategia = new FileHandler(karpeta.resolve(izena).toString(), true);
            fitxategia.setEncoding("UTF-8");
            fitxategia.setFormatter(new SimpleFormatter());
            ERRO_LOGGER.addHandler(fitxategia);
            ERRO_LOGGER.setLevel(Level.INFO);
        } catch (IOException | SecurityException e) {
            System.err.println("Ezin izan da log fitxategia sortu: " + e.getMessage());
        }
    }

    public static Logger logger(Class<?> klasea) {
        return Logger.getLogger(klasea.getName());
    }
}
