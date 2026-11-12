package eus.artificialunity.agintekoadroa.kontroladoreak;

import eus.artificialunity.agintekoadroa.modeloa.AginteDatuak;
import eus.artificialunity.agintekoadroa.modeloa.StockLerroa;
import eus.artificialunity.agintekoadroa.osagaiak.EgoeraZelda;
import eus.artificialunity.agintekoadroa.osagaiak.FormatuZelda;
import eus.artificialunity.agintekoadroa.osagaiak.StockMailaZelda;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.util.stream.Collectors;

/** Stocka gaur: abisu-banda, "stock baxukoak bakarrik" iragazkia eta taula (egoera eta maila-barrarekin). */
public class StockaKontroladorea implements PantailaKontroladorea {

    @FXML private Label abisuBanda;
    @FXML private CheckBox baxuakBakarrik;
    @FXML private TableView<StockLerroa> taula;
    @FXML private TableColumn<StockLerroa, StockLerroa> egoeraZutabea;
    @FXML private TableColumn<StockLerroa, String> erreferentziaZutabea;
    @FXML private TableColumn<StockLerroa, String> produktuaZutabea;
    @FXML private TableColumn<StockLerroa, String> kategoriaZutabea;
    @FXML private TableColumn<StockLerroa, StockLerroa> stockaZutabea;
    @FXML private TableColumn<StockLerroa, BigDecimal> minimoaZutabea;
    @FXML private TableColumn<StockLerroa, BigDecimal> maximoaZutabea;
    @FXML private TableColumn<StockLerroa, BigDecimal> faltaZutabea;

    private FilteredList<StockLerroa> iragazita = new FilteredList<>(FXCollections.observableArrayList());

    @FXML
    private void initialize() {
        egoeraZutabea.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        egoeraZutabea.setCellFactory(z -> new EgoeraZelda<>(StockLerroa::stockBaxua, "Baxua", "Ondo"));
        egoeraZutabea.setSortable(false);
        erreferentziaZutabea.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().erreferentzia()));
        produktuaZutabea.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().produktua()));
        kategoriaZutabea.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().kategoria()));
        stockaZutabea.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        stockaZutabea.setCellFactory(z -> new StockMailaZelda<>());
        stockaZutabea.setSortable(false);
        minimoaZutabea.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().arauaDu() ? c.getValue().minimoa() : null));
        minimoaZutabea.setCellFactory(FormatuZelda.zenbakia());
        maximoaZutabea.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().arauaDu() ? c.getValue().maximoa() : null));
        maximoaZutabea.setCellFactory(FormatuZelda.zenbakia());
        faltaZutabea.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().arauaDu() ? c.getValue().falta() : null));
        faltaZutabea.setCellFactory(FormatuZelda.zenbakia());
        taula.setPlaceholder(new Label("Ez dago produkturik"));
        taula.setRowFactory(t -> new javafx.scene.control.TableRow<>() {
            @Override
            protected void updateItem(StockLerroa lerroa, boolean hutsik) {
                super.updateItem(lerroa, hutsik);
                getStyleClass().remove("lerro-abisua");
                if (!hutsik && lerroa != null && lerroa.stockBaxua()) {
                    getStyleClass().add("lerro-abisua");
                }
            }
        });
    }

    @FXML
    private void iragazkiaAldatu(ActionEvent e) {
        iragazita.setPredicate(baxuakBakarrik.isSelected() ? StockLerroa::stockBaxua : s -> true);
    }

    @Override
    public void erakutsi(AginteDatuak d) {
        iragazita = new FilteredList<>(FXCollections.observableArrayList(d.stocka()));
        taula.setItems(iragazita);
        iragazkiaAldatu(null);

        long baxuak = d.stockBaxukoKopurua();
        abisuBanda.getStyleClass().removeAll("banda-arriskua", "banda-ondo");
        if (baxuak > 0) {
            String zerrenda = d.stocka().stream().filter(StockLerroa::stockBaxua)
                    .map(s -> s.erreferentzia() + " " + s.produktua()).collect(Collectors.joining(", "));
            abisuBanda.setText("Kontuz: " + baxuak + " produkturen stocka minimotik behera dago. "
                    + "Erosketa-arduradunari abisatu: " + zerrenda + ".");
            abisuBanda.getStyleClass().add("banda-arriskua");
        } else {
            abisuBanda.setText("Produktu guztiek stock nahikoa dute (minimoaren gainetik).");
            abisuBanda.getStyleClass().add("banda-ondo");
        }
    }
}
