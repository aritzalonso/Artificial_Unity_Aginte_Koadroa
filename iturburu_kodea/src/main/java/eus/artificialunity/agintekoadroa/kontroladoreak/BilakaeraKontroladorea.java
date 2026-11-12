package eus.artificialunity.agintekoadroa.kontroladoreak;

import eus.artificialunity.agintekoadroa.modeloa.AginteDatuak;
import eus.artificialunity.agintekoadroa.modeloa.Aurreikuspena;
import eus.artificialunity.agintekoadroa.modeloa.HilekoSalmenta;
import eus.artificialunity.agintekoadroa.osagaiak.FormatuZelda;
import eus.artificialunity.agintekoadroa.osagaiak.KpiTxartela;
import eus.artificialunity.agintekoadroa.osagaiak.KpiTxartela.Mota;
import eus.artificialunity.agintekoadroa.osagaiak.SalmentaGrafikoa;
import eus.artificialunity.agintekoadroa.utilak.Formatua;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Optional;

/** Salmenten bilakaera: lerro-grafikoa aurreikuspenarekin, hilabete onena/txarrena eta hileko taula. */
public class BilakaeraKontroladorea implements PantailaKontroladorea {

    @FXML private SalmentaGrafikoa grafikoa;
    @FXML private KpiTxartela aurreikuspenTxartela;
    @FXML private KpiTxartela onenaTxartela;
    @FXML private KpiTxartela txarrenaTxartela;
    @FXML private KpiTxartela batezBestekoaTxartela;
    @FXML private Label aurreikuspenAzalpena;
    @FXML private TableView<HilekoSalmenta> taula;
    @FXML private TableColumn<HilekoSalmenta, String> hilabeteaZutabea;
    @FXML private TableColumn<HilekoSalmenta, BigDecimal> salmentakZutabea;
    @FXML private TableColumn<HilekoSalmenta, Number> eskaerakZutabea;

    @FXML
    private void initialize() {
        hilabeteaZutabea.setCellValueFactory(c -> new ReadOnlyStringWrapper(Formatua.hilabetea(c.getValue().hilabetea())));
        hilabeteaZutabea.setComparator(Comparator.comparing(s -> s));
        hilabeteaZutabea.setSortable(false);
        salmentakZutabea.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().salmentak()));
        salmentakZutabea.setCellFactory(FormatuZelda.dirua());
        eskaerakZutabea.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().eskaerak()));
        taula.setPlaceholder(new Label("Ez dago salmentarik aukeratutako aldian"));
    }

    @Override
    public void erakutsi(AginteDatuak d) {
        grafikoa.erakutsi(d.hilekoak(), d.aurreikuspena());
        taula.setItems(FXCollections.observableArrayList(d.hilekoak()));

        Aurreikuspena ar = d.aurreikuspena();
        if (ar == null) {
            aurreikuspenTxartela.erakutsi("-", "Ez dago daturik", Mota.NORMALA);
            aurreikuspenAzalpena.setText("");
        } else {
            aurreikuspenTxartela.setIzenburua("Aurreikuspena: " + Formatua.hilabetea(ar.hilabetea()));
            aurreikuspenTxartela.erakutsi(Formatua.diruaOsoa(ar.balioa()),
                    (ar.goraka() ? "▲ joera gora: +" : "▼ joera behera: ") + Formatua.diruaOsoa(ar.malda()) + " hilean",
                    ar.goraka() ? Mota.ONA : Mota.ABISUA);
            aurreikuspenAzalpena.setText("Nola kalkulatzen da? Azken " + ar.hilabeteKopurua()
                    + " hilabeteen salmentei lerro zuzen bat egokitzen zaie (joera), eta lerro hori hurrengo hilabetera "
                    + "luzatzen da (lerro urdin etena). Azken 3 hilabeteen batez bestekoa "
                    + Formatua.diruaOsoa(ar.batezBestekoMugikorra())
                    + " da. Aurreikuspena gutxi gorabeherakoa da: ez ditu kontuan hartzen urtaroak edo kanpainak.");
        }

        Optional<HilekoSalmenta> onena = d.hilekoak().stream().max(Comparator.comparing(HilekoSalmenta::salmentak));
        Optional<HilekoSalmenta> txarrena = d.hilekoak().stream().min(Comparator.comparing(HilekoSalmenta::salmentak));
        onenaTxartela.erakutsi(onena.map(h -> Formatua.diruaOsoa(h.salmentak())).orElse("-"),
                onena.map(h -> Formatua.hilabetea(h.hilabetea())).orElse(""), Mota.ONA);
        txarrenaTxartela.erakutsi(txarrena.map(h -> Formatua.diruaOsoa(h.salmentak())).orElse("-"),
                txarrena.map(h -> Formatua.hilabetea(h.hilabetea())).orElse(""), Mota.ABISUA);
        BigDecimal guztira = d.hilekoak().stream().map(HilekoSalmenta::salmentak).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal batezBestekoa = d.hilekoak().isEmpty() ? BigDecimal.ZERO
                : guztira.divide(BigDecimal.valueOf(d.hilekoak().size()), 2, java.math.RoundingMode.HALF_UP);
        batezBestekoaTxartela.erakutsi(Formatua.diruaOsoa(batezBestekoa),
                d.hilekoak().size() + " hilabeteren batez bestekoa", Mota.NORMALA);
    }
}
