package eus.artificialunity.agintekoadroa.kontroladoreak;

import eus.artificialunity.agintekoadroa.modeloa.AginteDatuak;
import eus.artificialunity.agintekoadroa.modeloa.BezeroSalmenta;
import eus.artificialunity.agintekoadroa.osagaiak.FormatuZelda;
import eus.artificialunity.agintekoadroa.utilak.Formatua;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Salmentak bezeroka: 10 bezero onenen grafikoa eta bezero guztien taula, bilatzailearekin. */
public class BezeroakKontroladorea implements PantailaKontroladorea {

    private static final int GRAFIKOKO_BEZEROAK = 10;

    @FXML private BarChart<Number, String> grafikoa;
    @FXML private NumberAxis balioArdatza;
    @FXML private CategoryAxis bezeroArdatza;
    @FXML private TextField bilatzailea;
    @FXML private Label kopuruaEtiketa;
    @FXML private TableView<BezeroSalmenta> taula;
    @FXML private TableColumn<BezeroSalmenta, String> bezeroaZutabea;
    @FXML private TableColumn<BezeroSalmenta, String> herriaZutabea;
    @FXML private TableColumn<BezeroSalmenta, Number> eskaerakZutabea;
    @FXML private TableColumn<BezeroSalmenta, BigDecimal> salmentakZutabea;
    @FXML private TableColumn<BezeroSalmenta, LocalDate> azkenaZutabea;

    private FilteredList<BezeroSalmenta> iragazita = new FilteredList<>(FXCollections.observableArrayList());

    @FXML
    private void initialize() {
        bezeroaZutabea.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().bezeroa()));
        herriaZutabea.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().herria()));
        eskaerakZutabea.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().eskaerak()));
        salmentakZutabea.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().salmentak()));
        salmentakZutabea.setCellFactory(FormatuZelda.dirua());
        azkenaZutabea.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().azkenEskaera()));
        azkenaZutabea.setCellFactory(FormatuZelda.data());
        taula.setPlaceholder(new Label("Ez dago bezerorik bilaketa honekin edo aldi honetan"));

        grafikoa.setAnimated(false);
        balioArdatza.setAnimated(false);
        bezeroArdatza.setAnimated(false);
        grafikoa.setLegendVisible(false);
        grafikoa.setCategoryGap(14);
        balioArdatza.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number n) {
                return Formatua.diruaOsoa(BigDecimal.valueOf(n.doubleValue()));
            }

            @Override
            public Number fromString(String s) {
                return 0;
            }
        });
        bilatzailea.textProperty().addListener((obs, zaharra, berria) -> iragazi(berria));
    }

    /** Bilatzailean idatzitakoaren arabera (izena edo herria) taula iragazten du. */
    private void iragazi(String testua) {
        String bilatu = testua == null ? "" : testua.trim().toLowerCase(Locale.ROOT);
        iragazita.setPredicate(b -> bilatu.isEmpty()
                || b.bezeroa().toLowerCase(Locale.ROOT).contains(bilatu)
                || b.herria().toLowerCase(Locale.ROOT).contains(bilatu));
        kopuruaEtiketa.setText(iragazita.size() + " bezero");
    }

    @Override
    public void erakutsi(AginteDatuak d) {
        iragazita = new FilteredList<>(FXCollections.observableArrayList(d.bezeroak()));
        SortedList<BezeroSalmenta> ordenatuta = new SortedList<>(iragazita);
        ordenatuta.comparatorProperty().bind(taula.comparatorProperty());
        taula.setItems(ordenatuta);
        iragazi(bilatzailea.getText());

        List<BezeroSalmenta> onenak = new ArrayList<>(d.bezeroak().subList(0, Math.min(GRAFIKOKO_BEZEROAK, d.bezeroak().size())));
        java.util.Collections.reverse(onenak);
        XYChart.Series<Number, String> seriea = new XYChart.Series<>();
        for (BezeroSalmenta b : onenak) {
            String izena = laburtu(b.bezeroa());
            seriea.getData().add(new XYChart.Data<>(b.salmentak(), izena));
        }
        grafikoa.getData().clear();
        // Kategoriak datuetatik kalkulatzen dira (ordena berean); setCategories-ekin ardatzaren marrak ez ziren marrazten
        bezeroArdatza.setAutoRanging(true);
        grafikoa.getData().add(seriea);
        for (int i = 0; i < onenak.size(); i++) {
            BezeroSalmenta b = onenak.get(i);
            Tooltip tt = new Tooltip(b.bezeroa() + " (" + b.herria() + ")\n" + Formatua.dirua(b.salmentak())
                    + "\n" + b.eskaerak() + " eskaera");
            tt.setShowDelay(Duration.millis(100));
            Tooltip.install(seriea.getData().get(i).getNode(), tt);
        }
    }

    /** Izen luzeak laburtu, ardatzean etiketa guztiak sartzeko (izen osoa taulan eta argibidean). */
    private static String laburtu(String izena) {
        return izena.length() <= 22 ? izena : izena.substring(0, 21) + "…";
    }
}
