package eus.artificialunity.agintekoadroa.osagaiak;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Errore-panela: zer gertatu den hizkera argian, zer egin dezakeen erabiltzaileak eta "Berriro saiatu" botoia.
 * Xehetasun teknikoak (informatikariarentzat) itxitako atal batean daude.
 * <p>
 * FXML-n: {@code <ErrorePanela fx:id="errorePanela" onBerrizSaiatu="#metodoa"/>}
 */
public class ErrorePanela extends VBox {

    private final Label izenburua = new Label();
    private final Label mezua = new Label();
    private final TextArea xehetasunak = new TextArea();
    private final TitledPane xehetasunAtala = new TitledPane("Xehetasun teknikoak (informatikariarentzat)", xehetasunak);
    private final Button berrizSaiatu = new Button("Berriro saiatu");
    private final ObjectProperty<EventHandler<ActionEvent>> onBerrizSaiatu = new SimpleObjectProperty<>(this, "onBerrizSaiatu");

    public ErrorePanela() {
        getStyleClass().add("errore-panela");
        setAlignment(Pos.CENTER);
        setSpacing(16);
        setMaxSize(720, USE_PREF_SIZE);

        Label ikonoa = new Label("!");
        ikonoa.getStyleClass().add("errore-ikonoa");
        izenburua.getStyleClass().add("errore-izenburua");
        izenburua.setWrapText(true);
        mezua.getStyleClass().add("errore-mezua");
        mezua.setWrapText(true);

        xehetasunak.setEditable(false);
        xehetasunak.setWrapText(true);
        xehetasunak.setPrefRowCount(6);
        xehetasunAtala.setExpanded(false);
        xehetasunAtala.setMaxWidth(Double.MAX_VALUE);

        berrizSaiatu.getStyleClass().addAll("botoi-nagusia", "botoi-handia");
        berrizSaiatu.setTooltip(new Tooltip("Datuak berriro kargatzen saiatu (F5)"));
        berrizSaiatu.setOnAction(e -> {
            EventHandler<ActionEvent> kudeatzailea = onBerrizSaiatu.get();
            if (kudeatzailea != null) {
                kudeatzailea.handle(new ActionEvent(this, this));
            }
        });

        getChildren().addAll(ikonoa, izenburua, mezua, berrizSaiatu, xehetasunAtala);
    }

    /** Errorea erakusten du. */
    public void erakutsi(String izenburuBerria, String mezuBerria, Throwable kausa) {
        izenburua.setText(izenburuBerria);
        mezua.setText(mezuBerria);
        if (kausa == null) {
            xehetasunak.setText("");
        } else {
            StringWriter sw = new StringWriter();
            kausa.printStackTrace(new PrintWriter(sw));
            xehetasunak.setText(sw.toString());
        }
        xehetasunAtala.setVisible(kausa != null);
        xehetasunAtala.setExpanded(false);
        setVisible(true);
        berrizSaiatu.requestFocus();
    }

    public EventHandler<ActionEvent> getOnBerrizSaiatu() { return onBerrizSaiatu.get(); }
    public void setOnBerrizSaiatu(EventHandler<ActionEvent> kudeatzailea) { onBerrizSaiatu.set(kudeatzailea); }
    public ObjectProperty<EventHandler<ActionEvent>> onBerrizSaiatuProperty() { return onBerrizSaiatu; }
}
