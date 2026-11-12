package eus.artificialunity.agintekoadroa.kontroladoreak;

import eus.artificialunity.agintekoadroa.modeloa.AginteDatuak;
import eus.artificialunity.agintekoadroa.modeloa.Taula;
import eus.artificialunity.agintekoadroa.zerbitzuak.TaulaFabrika;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.function.Function;

/**
 * Aplikazioaren pantailak, ezkerreko menuan agertzen diren ordenan.
 * Bakoitzak bere FXML bista, azalpena (argibidea), laster-tekla (Ctrl+1...7) eta CSV-rako taula ditu.
 */
public enum Pantaila {

    LABURPENA("Laburpena", "laburpena.fxml", "laburpena",
            "Enpresaren zifra nagusiak begirada batean, eta kontuan hartu beharreko abisuak",
            TaulaFabrika::laburpena),
    BILAKAERA("Salmenten bilakaera", "bilakaera.fxml", "salmentak_hileka",
            "Salmentak hilabetez hilabete, eta hurrengo hilabeterako aurreikuspena",
            TaulaFabrika::hilekoak),
    PRODUKTUAK("Produktu salduenak", "produktuak.fxml", "produktu_salduenak",
            "Gehien saldu diren produktuak (unitateak eta eurotan)",
            TaulaFabrika::produktuak),
    BEZEROAK("Bezeroak", "bezeroak.fxml", "bezeroak",
            "Bezero bakoitzak zenbat erosi duen",
            TaulaFabrika::bezeroak),
    KATEGORIAK("Kategoriak", "kategoriak.fxml", "kategoriak",
            "Salmentak produktu motaren arabera (eramangarriak, pantailak...)",
            TaulaFabrika::kategoriak),
    STOCKA("Stocka", "stocka.fxml", "stocka",
            "Biltegian zenbat dagoen gaur, eta zein produktu dauden minimotik behera",
            TaulaFabrika::stocka),
    FAKTURAK("Kobratzeko fakturak", "fakturak.fxml", "kobratzeko_fakturak",
            "Bezeroek oraindik ordaindu ez dituzten fakturak, berandu daudenak lehenik",
            TaulaFabrika::fakturak);

    private final String izena;
    private final String fxml;
    private final String fitxategiIzena;
    private final String azalpena;
    private final Function<AginteDatuak, Taula> taula;

    Pantaila(String izena, String fxml, String fitxategiIzena, String azalpena, Function<AginteDatuak, Taula> taula) {
        this.izena = izena;
        this.fxml = fxml;
        this.fitxategiIzena = fitxategiIzena;
        this.azalpena = azalpena;
        this.taula = taula;
    }

    public String izena() { return izena; }
    public String fxml() { return fxml; }
    public String fitxategiIzena() { return fitxategiIzena; }
    public String azalpena() { return azalpena; }

    /** Pantaila honetako datuak taula gisa (CSV-rako). */
    public Taula taula(AginteDatuak datuak) {
        return taula.apply(datuak);
    }

    /** Ctrl+1 ... Ctrl+7 */
    public KeyCombination lasterTekla() {
        return new KeyCodeCombination(KeyCode.getKeyCode(String.valueOf(ordinal() + 1)), KeyCombination.SHORTCUT_DOWN);
    }

    public String lasterTeklaTestua() {
        return "Ctrl+" + (ordinal() + 1);
    }

    /** Komando-lerroko izenetik (adib. "stocka"); ezezaguna bada, Laburpena. */
    public static Pantaila izenetik(String izena) {
        if (izena != null) {
            for (Pantaila p : values()) {
                if (p.name().equalsIgnoreCase(izena.trim())) {
                    return p;
                }
            }
        }
        return LABURPENA;
    }
}
