package eus.artificialunity.agintekoadroa.kontroladoreak;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;

import java.util.EnumMap;
import java.util.Map;

/**
 * Laguntza-leihoa: atal bat pantaila bakoitzeko eta atal orokorrak (hasiera, datak, txostenak, arazoak).
 * Irekitzean, erabiltzailea dagoen pantailari dagokion atala zabaltzen da.
 */
public class LaguntzaKontroladorea {

    @FXML private Accordion atalak;
    @FXML private TitledPane hasieraAtala;
    @FXML private TitledPane laburpenaAtala;
    @FXML private TitledPane bilakaeraAtala;
    @FXML private TitledPane produktuakAtala;
    @FXML private TitledPane bezeroakAtala;
    @FXML private TitledPane kategoriakAtala;
    @FXML private TitledPane stockaAtala;
    @FXML private TitledPane fakturakAtala;

    private final Map<Pantaila, TitledPane> pantailenAtalak = new EnumMap<>(Pantaila.class);
    private Stage leihoa;

    @FXML
    private void initialize() {
        pantailenAtalak.put(Pantaila.LABURPENA, laburpenaAtala);
        pantailenAtalak.put(Pantaila.BILAKAERA, bilakaeraAtala);
        pantailenAtalak.put(Pantaila.PRODUKTUAK, produktuakAtala);
        pantailenAtalak.put(Pantaila.BEZEROAK, bezeroakAtala);
        pantailenAtalak.put(Pantaila.KATEGORIAK, kategoriakAtala);
        pantailenAtalak.put(Pantaila.STOCKA, stockaAtala);
        pantailenAtalak.put(Pantaila.FAKTURAK, fakturakAtala);
        atalak.setExpandedPane(hasieraAtala);
    }

    void leihoaEzarri(Stage leihoBerria) {
        this.leihoa = leihoBerria;
    }

    /** Pantaila horri buruzko atala zabaltzen du. */
    public void atalaIreki(Pantaila p) {
        TitledPane atala = pantailenAtalak.get(p);
        if (atala != null) {
            atalak.setExpandedPane(atala);
        }
    }

    @FXML
    private void itxi(ActionEvent e) {
        if (leihoa != null) {
            leihoa.hide();
        }
    }
}
