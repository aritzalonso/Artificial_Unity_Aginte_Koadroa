package eus.artificialunity.agintekoadroa.osagaiak;

import eus.artificialunity.agintekoadroa.modeloa.Aurreikuspena;
import eus.artificialunity.agintekoadroa.modeloa.HilekoSalmenta;
import eus.artificialunity.agintekoadroa.utilak.Formatua;
import javafx.collections.FXCollections;
import javafx.geometry.Side;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.scene.transform.Transform;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Salmenten bilakaeraren lerro-grafikoa: benetako salmentak hileka (morea) eta hurrengo hilabeteko
 * aurreikuspena (lerro urdin etena). Puntu bakoitzaren gainean sagua jartzean hilabetea eta zenbatekoa agertzen dira.
 * PDF txostenerako irudia ere sortzen du ({@link #irudia}).
 */
public class SalmentaGrafikoa extends LineChart<String, Number> {

    public SalmentaGrafikoa() {
        super(new CategoryAxis(), new NumberAxis());
        getStyleClass().add("salmenta-grafikoa");
        setAnimated(false);
        getXAxis().setAnimated(false);
        getYAxis().setAnimated(false);
        setCreateSymbols(true);
        setLegendSide(Side.BOTTOM);
        getXAxis().setLabel("Hilabetea");
        NumberAxis y = (NumberAxis) getYAxis();
        y.setLabel("Salmentak (€, BEZik gabe)");
        y.setForceZeroInRange(true);
        y.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number zenbakia) {
                return Formatua.diruaOsoa(BigDecimal.valueOf(zenbakia.doubleValue()));
            }

            @Override
            public Number fromString(String testua) {
                return 0;
            }
        });
    }

    /** Hileko salmentak eta (badago) aurreikuspena marrazten ditu. */
    public void erakutsi(List<HilekoSalmenta> hilekoak, Aurreikuspena aurreikuspena) {
        getData().clear();
        List<String> kategoriak = new ArrayList<>();
        XYChart.Series<String, Number> benetakoak = new XYChart.Series<>();
        benetakoak.setName("Salmentak");
        for (HilekoSalmenta h : hilekoak) {
            String etiketa = Formatua.hilabeteLaburra(h.hilabetea());
            kategoriak.add(etiketa);
            benetakoak.getData().add(new XYChart.Data<>(etiketa, h.salmentak()));
        }
        XYChart.Series<String, Number> aurreikusia = new XYChart.Series<>();
        aurreikusia.setName("Aurreikuspena");
        if (aurreikuspena != null && !hilekoak.isEmpty()) {
            HilekoSalmenta azkena = hilekoak.get(hilekoak.size() - 1);
            String etiketa = Formatua.hilabeteLaburra(aurreikuspena.hilabetea());
            kategoriak.add(etiketa);
            aurreikusia.getData().add(new XYChart.Data<>(Formatua.hilabeteLaburra(azkena.hilabetea()), azkena.salmentak()));
            aurreikusia.getData().add(new XYChart.Data<>(etiketa, aurreikuspena.balioa()));
        }
        ((CategoryAxis) getXAxis()).setCategories(FXCollections.observableArrayList(kategoriak));
        getData().add(benetakoak);
        if (!aurreikusia.getData().isEmpty()) {
            getData().add(aurreikusia);
        }

        for (int i = 0; i < benetakoak.getData().size(); i++) {
            HilekoSalmenta h = hilekoak.get(i);
            argibidea(benetakoak.getData().get(i), Formatua.hilabetea(h.hilabetea()) + "\n"
                    + Formatua.dirua(h.salmentak()) + "\n" + h.eskaerak() + " eskaera");
        }
        if (aurreikusia.getData().size() == 2) {
            argibidea(aurreikusia.getData().get(1), "Aurreikuspena: " + Formatua.hilabetea(aurreikuspena.hilabetea())
                    + "\n" + Formatua.dirua(aurreikuspena.balioa()));
        }
    }

    private static void argibidea(XYChart.Data<String, Number> puntua, String testua) {
        if (puntua.getNode() != null) {
            Tooltip tt = new Tooltip(testua);
            tt.setShowDelay(Duration.millis(100));
            Tooltip.install(puntua.getNode(), tt);
        }
    }

    /**
     * Grafikoaren irudia (PDFrako), leihorik gabe: grafiko berri bat eszena batean marraztu eta argazkia atera.
     * Interfazearen haritik deitu behar da.
     */
    public static WritableImage irudia(List<HilekoSalmenta> hilekoak, Aurreikuspena aurreikuspena,
                                       String estiloOrria, double zabalera, double altuera) {
        SalmentaGrafikoa grafikoa = new SalmentaGrafikoa();
        grafikoa.erakutsi(hilekoak, aurreikuspena);
        StackPane edukiontzia = new StackPane(grafikoa);
        edukiontzia.getStyleClass().add("grafiko-irudia");
        Scene eszena = new Scene(edukiontzia, zabalera, altuera);
        if (estiloOrria != null) {
            eszena.getStylesheets().add(estiloOrria);
        }
        edukiontzia.applyCss();
        edukiontzia.layout();
        SnapshotParameters parametroak = new SnapshotParameters();
        parametroak.setTransform(Transform.scale(2, 2));
        return edukiontzia.snapshot(parametroak, null);
    }
}
