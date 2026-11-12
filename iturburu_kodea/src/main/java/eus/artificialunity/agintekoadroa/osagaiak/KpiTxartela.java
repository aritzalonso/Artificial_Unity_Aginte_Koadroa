package eus.artificialunity.agintekoadroa.osagaiak;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.AccessibleRole;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

/**
 * Adierazle-txartela (KPI): izenburua, balio handia eta azalpen txikia (adib. aldaketa aurreko aldiarekin).
 * Mota-ren arabera kolorea aldatzen du (normala, ona, abisua, arriskua). Ekintza bat badu, klik egin daiteke
 * (saguarekin edo teklatuarekin: Enter / zuriunea), adibidez dagokion pantailara joateko.
 * <p>
 * FXML-n: {@code <KpiTxartela izenburua="Salmentak guztira" laguntza="..." onAction="#metodoa"/>}
 */
public class KpiTxartela extends VBox {

    /** Txartelaren egoera, kolorearen bidez adierazita. */
    public enum Mota { NORMALA, ONA, ABISUA, ARRISKUA }

    private final Label izenburuEtiketa = new Label();
    private final Label balioEtiketa = new Label("-");
    private final Label azalpenEtiketa = new Label();

    private final StringProperty izenburua = new SimpleStringProperty(this, "izenburua", "");
    private final StringProperty balioa = new SimpleStringProperty(this, "balioa", "-");
    private final StringProperty azalpena = new SimpleStringProperty(this, "azalpena", "");
    private final StringProperty laguntza = new SimpleStringProperty(this, "laguntza", "");
    private final ObjectProperty<Mota> mota = new SimpleObjectProperty<>(this, "mota", Mota.NORMALA);
    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>(this, "onAction");

    public KpiTxartela() {
        getStyleClass().add("kpi-txartela");
        izenburuEtiketa.getStyleClass().add("kpi-izenburua");
        balioEtiketa.getStyleClass().add("kpi-balioa");
        azalpenEtiketa.getStyleClass().add("kpi-azalpena");
        izenburuEtiketa.setWrapText(true);
        azalpenEtiketa.setWrapText(true);
        izenburuEtiketa.textProperty().bind(izenburua);
        balioEtiketa.textProperty().bind(balioa);
        azalpenEtiketa.textProperty().bind(azalpena);
        getChildren().addAll(izenburuEtiketa, balioEtiketa, azalpenEtiketa);

        motaAplikatu(Mota.NORMALA);
        mota.addListener((obs, zaharra, berria) -> motaAplikatu(berria));

        Tooltip argibidea = new Tooltip();
        argibidea.setWrapText(true);
        argibidea.setMaxWidth(320);
        argibidea.textProperty().bind(laguntza);
        laguntza.addListener((obs, zaharra, berria) -> {
            if (berria == null || berria.isBlank()) {
                Tooltip.uninstall(this, argibidea);
            } else {
                Tooltip.install(this, argibidea);
            }
        });

        onAction.addListener((obs, zaharra, berria) -> klikagarria(berria != null));
        setOnMouseClicked(e -> ekintza());
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                ekintza();
                e.consume();
            }
        });
    }

    private void motaAplikatu(Mota berria) {
        getStyleClass().removeAll("kpi-normala", "kpi-ona", "kpi-abisua", "kpi-arriskua");
        getStyleClass().add("kpi-" + (berria == null ? Mota.NORMALA : berria).name().toLowerCase());
    }

    private void klikagarria(boolean bai) {
        setCursor(bai ? Cursor.HAND : Cursor.DEFAULT);
        setFocusTraversable(bai);
        setAccessibleRole(bai ? AccessibleRole.BUTTON : AccessibleRole.NODE);
        if (bai) {
            if (!getStyleClass().contains("kpi-klikagarria")) {
                getStyleClass().add("kpi-klikagarria");
            }
        } else {
            getStyleClass().remove("kpi-klikagarria");
        }
    }

    private void ekintza() {
        EventHandler<ActionEvent> kudeatzailea = onAction.get();
        if (kudeatzailea != null) {
            kudeatzailea.handle(new ActionEvent(this, this));
        }
    }

    /** Balioa, azalpena eta mota batera ezartzen ditu. */
    public void erakutsi(String balioBerria, String azalpenBerria, Mota motaBerria) {
        setBalioa(balioBerria);
        setAzalpena(azalpenBerria);
        setMota(motaBerria);
    }

    public String getIzenburua() { return izenburua.get(); }
    public void setIzenburua(String balioBerria) { izenburua.set(balioBerria); }
    public StringProperty izenburuaProperty() { return izenburua; }

    public String getBalioa() { return balioa.get(); }
    public void setBalioa(String balioBerria) { balioa.set(balioBerria); }
    public StringProperty balioaProperty() { return balioa; }

    public String getAzalpena() { return azalpena.get(); }
    public void setAzalpena(String balioBerria) { azalpena.set(balioBerria); }
    public StringProperty azalpenaProperty() { return azalpena; }

    public String getLaguntza() { return laguntza.get(); }
    public void setLaguntza(String balioBerria) { laguntza.set(balioBerria); }
    public StringProperty laguntzaProperty() { return laguntza; }

    public Mota getMota() { return mota.get(); }
    public void setMota(Mota balioBerria) { mota.set(balioBerria); }
    public ObjectProperty<Mota> motaProperty() { return mota; }

    public EventHandler<ActionEvent> getOnAction() { return onAction.get(); }
    public void setOnAction(EventHandler<ActionEvent> balioBerria) { onAction.set(balioBerria); }
    public ObjectProperty<EventHandler<ActionEvent>> onActionProperty() { return onAction; }
}
