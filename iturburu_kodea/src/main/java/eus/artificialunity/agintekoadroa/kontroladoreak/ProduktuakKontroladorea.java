package eus.artificialunity.agintekoadroa.kontroladoreak;

import eus.artificialunity.agintekoadroa.modeloa.AginteDatuak;
import eus.artificialunity.agintekoadroa.modeloa.ProduktuSalmenta;
import eus.artificialunity.agintekoadroa.osagaiak.FormatuZelda;
import eus.artificialunity.agintekoadroa.utilak.Formatua;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Produktu salduenak: barra-grafiko horizontala (unitateak edo eurotan, erabiltzaileak aukeratuta) eta taula. */
public class ProduktuakKontroladorea implements PantailaKontroladorea {

    private static final int IZEN_LUZERA = 26;

    @FXML private BarChart<Number, String> grafikoa;
    @FXML private NumberAxis balioArdatza;
    @FXML private CategoryAxis produktuArdatza;
    @FXML private ToggleButton unitateakBotoia;
    @FXML private ToggleButton euroakBotoia;
    @FXML private ToggleGroup neurria;
    @FXML private TableView<ProduktuSalmenta> taula;
    @FXML private TableColumn<ProduktuSalmenta, Number> postuaZutabea;
    @FXML private TableColumn<ProduktuSalmenta, String> erreferentziaZutabea;
    @FXML private TableColumn<ProduktuSalmenta, String> produktuaZutabea;
    @FXML private TableColumn<ProduktuSalmenta, BigDecimal> unitateakZutabea;
    @FXML private TableColumn<ProduktuSalmenta, BigDecimal> zenbatekoaZutabea;

    private AginteDatuak datuak;

    @FXML
    private void initialize() {
        postuaZutabea.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(taula.getItems().indexOf(c.getValue()) + 1));
        postuaZutabea.setSortable(false);
        erreferentziaZutabea.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().erreferentzia()));
        produktuaZutabea.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().produktua()));
        unitateakZutabea.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().unitateak()));
        unitateakZutabea.setCellFactory(FormatuZelda.zenbakia());
        zenbatekoaZutabea.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().zenbatekoa()));
        zenbatekoaZutabea.setCellFactory(FormatuZelda.dirua());
        taula.setPlaceholder(new Label("Ez dago salmentarik aukeratutako aldian"));

        grafikoa.setAnimated(false);
        balioArdatza.setAnimated(false);
        produktuArdatza.setAnimated(false);
        grafikoa.setLegendVisible(false);
        grafikoa.setCategoryGap(12);
        unitateakBotoia.setTooltip(new Tooltip("Produktu bakoitzetik zenbat unitate saldu diren"));
        euroakBotoia.setTooltip(new Tooltip("Produktu bakoitzaren salmentak eurotan (BEZik gabe)"));
        neurria.selectedToggleProperty().addListener((obs, zaharra, berria) -> neurriaAldatu(zaharra, berria));
    }

    /** Toggle-a ezin da hutsik geratu: beti bat aukeratuta. */
    private void neurriaAldatu(Toggle zaharra, Toggle berria) {
        if (berria == null) {
            neurria.selectToggle(zaharra);
            return;
        }
        grafikoaMarraztu();
    }

    @Override
    public void erakutsi(AginteDatuak d) {
        this.datuak = d;
        taula.setItems(FXCollections.observableArrayList(d.produktuak()));
        grafikoaMarraztu();
    }

    private void grafikoaMarraztu() {
        if (datuak == null) {
            return;
        }
        boolean euroak = neurria.getSelectedToggle() == euroakBotoia;
        balioArdatza.setLabel(euroak ? "Salmentak (€, BEZik gabe)" : "Saldutako unitateak");
        balioArdatza.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number n) {
                BigDecimal b = BigDecimal.valueOf(n.doubleValue());
                return euroak ? Formatua.diruaOsoa(b) : Formatua.zenbakia(b);
            }

            @Override
            public Number fromString(String s) {
                return 0;
            }
        });

        // Grafiko horizontalean lehen kategoria behean agertzen da: alderantzizko ordena, onena goian egoteko
        List<ProduktuSalmenta> produktuak = new ArrayList<>(datuak.produktuak());
        java.util.Collections.reverse(produktuak);
        if (euroak) {
            produktuak.sort(java.util.Comparator.comparing(ProduktuSalmenta::zenbatekoa));
        }
        XYChart.Series<Number, String> seriea = new XYChart.Series<>();
        for (ProduktuSalmenta p : produktuak) {
            String izena = p.erreferentzia() + "  " + laburtu(p.produktua());
            seriea.getData().add(new XYChart.Data<>(euroak ? p.zenbatekoa() : p.unitateak(), izena));
        }
        grafikoa.getData().clear();
        // Kategoriak datuetatik kalkulatzen dira (ordena berean); setCategories-ekin ardatzaren marrak ez ziren beti marrazten
        produktuArdatza.setAutoRanging(true);
        grafikoa.getData().add(seriea);
        for (int i = 0; i < produktuak.size(); i++) {
            ProduktuSalmenta p = produktuak.get(i);
            Tooltip tt = new Tooltip(p.izenOsoa() + "\n" + Formatua.zenbakia(p.unitateak()) + " unitate\n"
                    + Formatua.dirua(p.zenbatekoa()));
            tt.setShowDelay(Duration.millis(100));
            Tooltip.install(seriea.getData().get(i).getNode(), tt);
        }
    }

    private static String laburtu(String izena) {
        return izena.length() <= IZEN_LUZERA ? izena : izena.substring(0, IZEN_LUZERA - 1) + "…";
    }
}
