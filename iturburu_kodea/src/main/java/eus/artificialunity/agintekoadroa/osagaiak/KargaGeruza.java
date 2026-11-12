package eus.artificialunity.agintekoadroa.osagaiak;

import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Datuak kargatzen ari diren bitartean pantailaren gainean agertzen den geruza: zirkulu birakaria eta mezua.
 * Interfazea ez da blokeatzen; geruzak klikak hartzen ditu, datu erdi-kargatuak ez ukitzeko.
 */
public class KargaGeruza extends StackPane {

    private final Label mezua = new Label("Datuak kargatzen...");

    public KargaGeruza() {
        getStyleClass().add("karga-geruza");
        ProgressIndicator zirkulua = new ProgressIndicator();
        zirkulua.setPrefSize(64, 64);
        mezua.getStyleClass().add("karga-mezua");
        VBox kutxa = new VBox(16, zirkulua, mezua);
        kutxa.setAlignment(Pos.CENTER);
        kutxa.getStyleClass().add("karga-kutxa");
        kutxa.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        getChildren().add(kutxa);
    }

    public String getMezua() { return mezua.getText(); }
    public void setMezua(String testua) { mezua.setText(testua); }
    public StringProperty mezuaProperty() { return mezua.textProperty(); }
}
