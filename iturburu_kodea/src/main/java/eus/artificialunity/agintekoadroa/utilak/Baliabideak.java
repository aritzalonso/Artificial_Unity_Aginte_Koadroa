package eus.artificialunity.agintekoadroa.utilak;

import java.net.URL;
import java.util.Objects;

/** Aplikazioaren baliabideen (FXML, CSS, irudiak) bideak, leku bakarrean. */
public final class Baliabideak {

    private static final String OINARRIA = "/eus/artificialunity/agintekoadroa/";

    private Baliabideak() {
    }

    public static URL fxml(String izena) {
        return bilatu("bistak/" + izena);
    }

    public static String estiloOrria() {
        return bilatu("css/estiloak.css").toExternalForm();
    }

    public static String logoa() {
        return bilatu("irudiak/logoa.png").toExternalForm();
    }

    public static String ikonoa() {
        return bilatu("irudiak/ikonoa.png").toExternalForm();
    }

    private static URL bilatu(String bidea) {
        return Objects.requireNonNull(Baliabideak.class.getResource(OINARRIA + bidea),
                "Baliabidea falta da: " + OINARRIA + bidea);
    }
}
