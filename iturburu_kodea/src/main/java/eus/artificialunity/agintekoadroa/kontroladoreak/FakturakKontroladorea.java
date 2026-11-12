package eus.artificialunity.agintekoadroa.kontroladoreak;

import eus.artificialunity.agintekoadroa.modeloa.AginteDatuak;
import eus.artificialunity.agintekoadroa.modeloa.FakturaZain;
import eus.artificialunity.agintekoadroa.osagaiak.EgoeraZelda;
import eus.artificialunity.agintekoadroa.osagaiak.FormatuZelda;
import eus.artificialunity.agintekoadroa.osagaiak.KpiTxartela;
import eus.artificialunity.agintekoadroa.osagaiak.KpiTxartela.Mota;
import eus.artificialunity.agintekoadroa.utilak.Formatua;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Kobratzeko dauden fakturak: laburpen-txartelak, "berandu daudenak bakarrik" iragazkia eta taula. */
public class FakturakKontroladorea implements PantailaKontroladorea {

    @FXML private KpiTxartela guztiraTxartela;
    @FXML private KpiTxartela beranduTxartela;
    @FXML private KpiTxartela garaizTxartela;
    @FXML private CheckBox beranduBakarrik;
    @FXML private TableView<FakturaZain> taula;
    @FXML private TableColumn<FakturaZain, FakturaZain> egoeraZutabea;
    @FXML private TableColumn<FakturaZain, String> fakturaZutabea;
    @FXML private TableColumn<FakturaZain, String> bezeroaZutabea;
    @FXML private TableColumn<FakturaZain, LocalDate> dataZutabea;
    @FXML private TableColumn<FakturaZain, LocalDate> mugaZutabea;
    @FXML private TableColumn<FakturaZain, BigDecimal> guztiraZutabea;
    @FXML private TableColumn<FakturaZain, BigDecimal> ordaintzekoZutabea;
    @FXML private TableColumn<FakturaZain, Number> beranduZutabea;

    private FilteredList<FakturaZain> iragazita = new FilteredList<>(FXCollections.observableArrayList());

    @FXML
    private void initialize() {
        egoeraZutabea.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        egoeraZutabea.setCellFactory(z -> new EgoeraZelda<>(FakturaZain::berandu, "Berandu", "Garaiz"));
        egoeraZutabea.setSortable(false);
        fakturaZutabea.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().faktura()));
        bezeroaZutabea.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().bezeroa()));
        dataZutabea.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().data()));
        dataZutabea.setCellFactory(FormatuZelda.data());
        mugaZutabea.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().mugaEguna()));
        mugaZutabea.setCellFactory(FormatuZelda.data());
        guztiraZutabea.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().guztira()));
        guztiraZutabea.setCellFactory(FormatuZelda.dirua());
        ordaintzekoZutabea.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().ordaintzeko()));
        ordaintzekoZutabea.setCellFactory(FormatuZelda.dirua());
        beranduZutabea.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().beranduEgunak()));
        taula.setPlaceholder(new Label("Ez dago kobratzeko fakturarik. Bezero guztiek ordaindu dute!"));
        taula.setRowFactory(t -> new TableRow<>() {
            @Override
            protected void updateItem(FakturaZain f, boolean hutsik) {
                super.updateItem(f, hutsik);
                getStyleClass().remove("lerro-abisua");
                if (!hutsik && f != null && f.berandu()) {
                    getStyleClass().add("lerro-abisua");
                }
            }
        });
    }

    @FXML
    private void iragazkiaAldatu(ActionEvent e) {
        iragazita.setPredicate(beranduBakarrik.isSelected() ? FakturaZain::berandu : f -> true);
    }

    @Override
    public void erakutsi(AginteDatuak d) {
        iragazita = new FilteredList<>(FXCollections.observableArrayList(d.fakturak()));
        SortedList<FakturaZain> ordenatuta = new SortedList<>(iragazita);
        ordenatuta.comparatorProperty().bind(taula.comparatorProperty());
        taula.setItems(ordenatuta);
        iragazkiaAldatu(null);

        long berandu = d.beranduKopurua();
        BigDecimal garaizZenbatekoa = d.laburpena().kobratzekoZain().subtract(d.beranduZenbatekoa());
        guztiraTxartela.erakutsi(Formatua.diruaOsoa(d.laburpena().kobratzekoZain()),
                d.fakturak().size() + " faktura kobratzeko (BEZarekin)", d.fakturak().isEmpty() ? Mota.ONA : Mota.NORMALA);
        beranduTxartela.erakutsi(Formatua.diruaOsoa(d.beranduZenbatekoa()),
                berandu + " faktura muga-eguna pasata", berandu > 0 ? Mota.ARRISKUA : Mota.ONA);
        garaizTxartela.erakutsi(Formatua.diruaOsoa(garaizZenbatekoa),
                (d.fakturak().size() - berandu) + " faktura oraindik garaiz", Mota.NORMALA);
    }
}
