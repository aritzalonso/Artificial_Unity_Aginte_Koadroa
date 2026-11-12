package eus.artificialunity.agintekoadroa.kontroladoreak;

import eus.artificialunity.agintekoadroa.modeloa.AginteDatuak;
import eus.artificialunity.agintekoadroa.modeloa.Aurreikuspena;
import eus.artificialunity.agintekoadroa.modeloa.Laburpena;
import eus.artificialunity.agintekoadroa.modeloa.StockLerroa;
import eus.artificialunity.agintekoadroa.osagaiak.KpiTxartela;
import eus.artificialunity.agintekoadroa.osagaiak.KpiTxartela.Mota;
import eus.artificialunity.agintekoadroa.osagaiak.SalmentaGrafikoa;
import eus.artificialunity.agintekoadroa.utilak.Formatua;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.OptionalDouble;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** Laburpena pantaila: KPI txartelak, salmenten grafiko txikia eta abisuak. */
public class LaburpenaKontroladorea implements PantailaKontroladorea {

    @FXML private KpiTxartela salmentakTxartela;
    @FXML private KpiTxartela eskaerakTxartela;
    @FXML private KpiTxartela batezBestekoaTxartela;
    @FXML private KpiTxartela bezeroakTxartela;
    @FXML private KpiTxartela kobratzekoTxartela;
    @FXML private KpiTxartela stockTxartela;
    @FXML private KpiTxartela aurreikuspenTxartela;
    @FXML private SalmentaGrafikoa grafikoa;
    @FXML private VBox abisuZerrenda;

    private Consumer<Pantaila> nabigatu = p -> { };

    @FXML
    private void initialize() {
        // Grafiko txikia: ardatzen izenburu laburragoak, lekua aurrezteko
        grafikoa.getXAxis().setLabel(null);
        grafikoa.getYAxis().setLabel("€ (BEZik gabe)");
    }

    @Override
    public void nabigazioaEzarri(Consumer<Pantaila> nabigatu) {
        this.nabigatu = nabigatu;
    }

    @FXML
    private void stockaIkusi(ActionEvent e) {
        nabigatu.accept(Pantaila.STOCKA);
    }

    @FXML
    private void fakturakIkusi(ActionEvent e) {
        nabigatu.accept(Pantaila.FAKTURAK);
    }

    @FXML
    private void bilakaeraIkusi(ActionEvent e) {
        nabigatu.accept(Pantaila.BILAKAERA);
    }

    @Override
    public void erakutsi(AginteDatuak d) {
        Laburpena l = d.laburpena();
        salmentakTxartela.setLaguntza("Aukeratutako aldiko salmentak (BEZik gabe), luzera bereko aurreko aldiarekin ("
                + Formatua.data(d.tartea().aurrekoa().hasiera()) + " - " + Formatua.data(d.tartea().aurrekoa().amaiera())
                + ") alderatuta.");

        OptionalDouble aldaketa = d.salmentenAldaketa();
        if (aldaketa.isPresent()) {
            double a = aldaketa.getAsDouble();
            salmentakTxartela.erakutsi(Formatua.diruaOsoa(l.salmentak()),
                    (a >= 0 ? "▲ " : "▼ ") + Formatua.portzentaia(a) + " aurreko aldiarekin",
                    a >= 0 ? Mota.ONA : Mota.ARRISKUA);
        } else {
            salmentakTxartela.erakutsi(Formatua.diruaOsoa(l.salmentak()), "Aurreko aldian ez dago salmentarik", Mota.NORMALA);
        }
        eskaerakTxartela.erakutsi(Formatua.zenbakia(l.eskaerak()),
                "Aurreko aldian: " + Formatua.zenbakia(d.aurrekoLaburpena().eskaerak()), Mota.NORMALA);
        batezBestekoaTxartela.erakutsi(Formatua.diruaOsoa(l.batezBestekoa()), "eskaera bakoitzeko, BEZik gabe", Mota.NORMALA);
        bezeroakTxartela.erakutsi(Formatua.zenbakia(l.bezeroak()), "aldi honetan erosi dutenak", Mota.NORMALA);

        long berandu = d.beranduKopurua();
        kobratzekoTxartela.erakutsi(Formatua.diruaOsoa(l.kobratzekoZain()),
                d.fakturak().size() + " faktura" + (berandu > 0 ? ", " + berandu + " berandu" : ""),
                berandu > 0 ? Mota.ARRISKUA : (d.fakturak().isEmpty() ? Mota.ONA : Mota.ABISUA));

        long baxuak = d.stockBaxukoKopurua();
        stockTxartela.erakutsi(Formatua.zenbakia(baxuak),
                baxuak > 0 ? "minimotik behera daude" : "denek stock nahikoa dute",
                baxuak > 0 ? Mota.ARRISKUA : Mota.ONA);

        Aurreikuspena ar = d.aurreikuspena();
        if (ar == null) {
            aurreikuspenTxartela.erakutsi("-", "Ez dago daturik aurreikuspena egiteko", Mota.NORMALA);
        } else {
            aurreikuspenTxartela.setIzenburua("Aurreikuspena: " + Formatua.hilabetea(ar.hilabetea()));
            aurreikuspenTxartela.erakutsi(Formatua.diruaOsoa(ar.balioa()),
                    ar.goraka() ? "▲ joera gora doa" : "▼ joera behera doa", ar.goraka() ? Mota.ONA : Mota.ABISUA);
        }

        grafikoa.erakutsi(d.hilekoak(), ar);
        abisuakErakutsi(d);
    }

    private void abisuakErakutsi(AginteDatuak d) {
        abisuZerrenda.getChildren().clear();
        long baxuak = d.stockBaxukoKopurua();
        if (baxuak > 0) {
            String produktuak = d.stocka().stream().filter(StockLerroa::stockBaxua)
                    .map(StockLerroa::erreferentzia).collect(Collectors.joining(", "));
            abisuZerrenda.getChildren().add(abisua("arriskua",
                    baxuak + " produkturen stocka minimotik behera dago: " + produktuak + ".",
                    "Ikusi stocka", Pantaila.STOCKA));
        }
        long berandu = d.beranduKopurua();
        if (berandu > 0) {
            abisuZerrenda.getChildren().add(abisua("arriskua",
                    berandu + " fakturaren muga-eguna pasatu da eta oraindik ez dira kobratu ("
                            + Formatua.dirua(d.beranduZenbatekoa()) + ").",
                    "Ikusi fakturak", Pantaila.FAKTURAK));
        }
        Aurreikuspena ar = d.aurreikuspena();
        // Joeraren abisua lekua dagoenean bakarrik (txartelak ere erakusten du): garrantzitsuenak lehenik
        if (ar != null && !ar.goraka() && abisuZerrenda.getChildren().size() < 2) {
            abisuZerrenda.getChildren().add(abisua("abisua",
                    "Joeraren arabera, salmentak jaisten ari dira. Begiratu hilabete bakoitzeko salmentak.",
                    "Ikusi bilakaera", Pantaila.BILAKAERA));
        }
        if (abisuZerrenda.getChildren().isEmpty()) {
            Label ondo = new Label("Ez dago abisurik. Dena ondo dago!");
            ondo.getStyleClass().add("abisu-ondo");
            abisuZerrenda.getChildren().add(ondo);
        }
    }

    private HBox abisua(String mota, String testua, String botoiTestua, Pantaila helburua) {
        Label ikonoa = new Label("!");
        ikonoa.getStyleClass().addAll("abisu-ikonoa", "abisu-ikonoa-" + mota);
        Label mezua = new Label(testua);
        mezua.setWrapText(true);
        // Zabalera finkoa: horrela testua lerro batean baino gehiagotan banatzen da, moztu gabe
        mezua.setPrefWidth(270);
        mezua.setMinHeight(Region.USE_PREF_SIZE);
        mezua.getStyleClass().add("abisu-testua");
        Button botoia = new Button(botoiTestua);
        botoia.getStyleClass().add("botoi-bigarrena");
        botoia.setTooltip(new Tooltip(helburua.izena() + " pantailara joan"));
        botoia.setOnAction(e -> nabigatu.accept(helburua));
        botoia.setMinWidth(Button.USE_PREF_SIZE);
        VBox testuKutxa = new VBox(8, mezua, botoia);
        HBox.setHgrow(testuKutxa, Priority.ALWAYS);
        HBox lerroa = new HBox(12, ikonoa, testuKutxa);
        lerroa.setAlignment(Pos.TOP_LEFT);
        lerroa.getStyleClass().addAll("abisu-lerroa", "abisu-lerroa-" + mota);
        return lerroa;
    }
}
