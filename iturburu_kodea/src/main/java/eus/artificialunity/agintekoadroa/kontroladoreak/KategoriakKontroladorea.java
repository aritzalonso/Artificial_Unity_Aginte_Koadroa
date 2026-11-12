package eus.artificialunity.agintekoadroa.kontroladoreak;

import eus.artificialunity.agintekoadroa.modeloa.AginteDatuak;
import eus.artificialunity.agintekoadroa.modeloa.KategoriaSalmenta;
import eus.artificialunity.agintekoadroa.osagaiak.FormatuZelda;
import eus.artificialunity.agintekoadroa.utilak.Formatua;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Salmentak kategoriaka: tarta-grafikoa (ehunekoekin) eta taula. */
public class KategoriakKontroladorea implements PantailaKontroladorea {

    @FXML private PieChart grafikoa;
    @FXML private TableView<KategoriaSalmenta> taula;
    @FXML private TableColumn<KategoriaSalmenta, String> kategoriaZutabea;
    @FXML private TableColumn<KategoriaSalmenta, BigDecimal> unitateakZutabea;
    @FXML private TableColumn<KategoriaSalmenta, BigDecimal> zenbatekoaZutabea;
    @FXML private TableColumn<KategoriaSalmenta, String> ehunekoaZutabea;

    private BigDecimal guztira = BigDecimal.ZERO;

    @FXML
    private void initialize() {
        kategoriaZutabea.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().kategoria()));
        unitateakZutabea.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().unitateak()));
        unitateakZutabea.setCellFactory(FormatuZelda.zenbakia());
        zenbatekoaZutabea.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().zenbatekoa()));
        zenbatekoaZutabea.setCellFactory(FormatuZelda.dirua());
        ehunekoaZutabea.setCellValueFactory(c -> new ReadOnlyStringWrapper(Formatua.ehunekoa(ehunekoa(c.getValue()))));
        ehunekoaZutabea.setSortable(false);
        taula.setPlaceholder(new Label("Ez dago salmentarik aukeratutako aldian"));
        grafikoa.setAnimated(false);
        grafikoa.setLabelsVisible(false);
        grafikoa.setLegendVisible(true);
        grafikoa.setLegendSide(javafx.geometry.Side.BOTTOM);
    }

    private double ehunekoa(KategoriaSalmenta k) {
        return guztira.signum() == 0 ? 0
                : k.zenbatekoa().multiply(BigDecimal.valueOf(100)).divide(guztira, 1, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public void erakutsi(AginteDatuak d) {
        guztira = d.kategorienGuztira();
        taula.setItems(FXCollections.observableArrayList(d.kategoriak()));
        grafikoa.getData().clear();
        for (KategoriaSalmenta k : d.kategoriak()) {
            PieChart.Data zatia = new PieChart.Data(k.kategoria() + " (" + Formatua.ehunekoa(ehunekoa(k)) + ")",
                    k.zenbatekoa().doubleValue());
            grafikoa.getData().add(zatia);
            Tooltip tt = new Tooltip(k.kategoria() + "\n" + Formatua.dirua(k.zenbatekoa()) + "\n"
                    + Formatua.zenbakia(k.unitateak()) + " unitate");
            tt.setShowDelay(Duration.millis(100));
            Tooltip.install(zatia.getNode(), tt);
        }
    }
}
