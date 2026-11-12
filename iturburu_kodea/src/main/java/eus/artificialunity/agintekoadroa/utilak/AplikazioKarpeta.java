package eus.artificialunity.agintekoadroa.utilak;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Aplikazioaren karpeta: .jar fitxategia dagoen karpeta (konfigurazioa eta logak bertan).
 * Garapenean (IntelliJ, mvnw javafx:run) klaseak karpeta batean daudenez, uneko karpeta erabiltzen da.
 */
public final class AplikazioKarpeta {

    private AplikazioKarpeta() {
    }

    public static Path bidea() {
        try {
            Path kokapena = Path.of(AplikazioKarpeta.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (Files.isRegularFile(kokapena)) {
                return kokapena.toAbsolutePath().getParent();
            }
        } catch (URISyntaxException | SecurityException | NullPointerException e) {
            // Kokapena ezin bada kalkulatu, uneko karpeta erabiltzen da
        }
        return Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }
}
