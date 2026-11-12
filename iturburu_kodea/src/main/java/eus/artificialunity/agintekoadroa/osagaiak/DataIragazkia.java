package eus.artificialunity.agintekoadroa.osagaiak;

import eus.artificialunity.agintekoadroa.modeloa.DataTartea;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

/**
 * Data-iragazki sinplea: aldi ohikoen zerrenda (azken 12 hilabeteak, aurten...) eta, nahi izanez gero,
 * bi data eskuz. Aldi bat aukeratzean berehala aplikatzen da; data eskuz aldatzean "Erakutsi" sakatu behar da.
 * Data okerrak (amaiera hasiera baino lehenago) mezu argi batekin adierazten dira.
 * <p>
 * FXML-n: {@code <DataIragazkia fx:id="dataIragazkia" onAplikatu="#metodoa"/>}
 */
public class DataIragazkia extends HBox {

    /** Aukeratu daitezkeen aldiak. */
    public enum Aldia {
        AZKEN_12_HILABETE("Azken 12 hilabeteak", DataTartea::azkenHamabiHilabete),
        HILABETE_HAU("Hilabete hau", DataTartea::hilabeteHau),
        AZKEN_3_HILABETE("Azken 3 hilabeteak", DataTartea::azkenHiruHilabete),
        AURTEN("Aurten", DataTartea::aurten),
        IAZ("Iaz", DataTartea::iaz),
        NIRE_DATAK("Nik aukeratutako datak", null);

        private final String izena;
        private final Function<LocalDate, DataTartea> tartea;

        Aldia(String izena, Function<LocalDate, DataTartea> tartea) {
            this.izena = izena;
            this.tartea = tartea;
        }

        public DataTartea tartea(LocalDate gaur) {
            return tartea == null ? null : tartea.apply(gaur);
        }

        @Override
        public String toString() {
            return izena;
        }
    }

    private static final DateTimeFormatter FORMATUA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ComboBox<Aldia> aldiak = new ComboBox<>();
    private final DatePicker hasiera = new DatePicker();
    private final DatePicker amaiera = new DatePicker();
    private final Button erakutsi = new Button("Erakutsi");
    private final Label mezua = new Label();
    private final ObjectProperty<EventHandler<ActionEvent>> onAplikatu = new SimpleObjectProperty<>(this, "onAplikatu");
    private boolean eguneratzen;

    public DataIragazkia() {
        getStyleClass().add("data-iragazkia");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);

        Label aldiaEtiketa = new Label("Aldia:");
        aldiaEtiketa.getStyleClass().add("iragazki-etiketa");
        aldiak.getItems().addAll(Aldia.values());
        aldiak.setTooltip(new Tooltip("Aukeratu zein aldiko datuak ikusi nahi dituzun"));
        aldiak.getStyleClass().add("aldi-zerrenda");

        StringConverter<LocalDate> bihurgailua = new StringConverter<>() {
            @Override
            public String toString(LocalDate data) {
                return data == null ? "" : data.format(FORMATUA);
            }

            @Override
            public LocalDate fromString(String testua) {
                if (testua == null || testua.isBlank()) {
                    return null;
                }
                try {
                    return LocalDate.parse(testua.trim(), FORMATUA);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        };
        for (DatePicker dp : new DatePicker[]{hasiera, amaiera}) {
            dp.setConverter(bihurgailua);
            dp.setPromptText("eg/hh/uuuu");
            dp.setPrefWidth(150);
            dp.getStyleClass().add("data-hautatzailea");
        }
        hasiera.setTooltip(new Tooltip("Hasiera-data (egun hau barne)"));
        amaiera.setTooltip(new Tooltip("Amaiera-data (egun hau barne)"));
        Label marra = new Label("-");

        erakutsi.getStyleClass().add("botoi-nagusia");
        erakutsi.setTooltip(new Tooltip("Aukeratutako datetako datuak erakutsi"));
        erakutsi.setDefaultButton(false);
        erakutsi.setOnAction(e -> aplikatu());

        mezua.getStyleClass().add("iragazki-mezua");
        mezua.setVisible(false);
        mezua.managedProperty().bind(mezua.visibleProperty());

        getChildren().addAll(aldiaEtiketa, aldiak, hasiera, marra, amaiera, erakutsi, mezua);

        aldiak.valueProperty().addListener((obs, zaharra, berria) -> {
            if (eguneratzen || berria == null || berria == Aldia.NIRE_DATAK) {
                return;
            }
            DataTartea tartea = berria.tartea(LocalDate.now());
            eguneratzen = true;
            hasiera.setValue(tartea.hasiera());
            amaiera.setValue(tartea.amaiera());
            eguneratzen = false;
            aplikatu();
        });
        hasiera.valueProperty().addListener((obs, zaharra, berria) -> eskuzAldatuta());
        amaiera.valueProperty().addListener((obs, zaharra, berria) -> eskuzAldatuta());

        eguneratzen = true;
        aldiak.setValue(Aldia.AZKEN_12_HILABETE);
        DataTartea lehenetsia = Aldia.AZKEN_12_HILABETE.tartea(LocalDate.now());
        hasiera.setValue(lehenetsia.hasiera());
        amaiera.setValue(lehenetsia.amaiera());
        eguneratzen = false;
    }

    private void eskuzAldatuta() {
        if (eguneratzen) {
            return;
        }
        eguneratzen = true;
        aldiak.setValue(Aldia.NIRE_DATAK);
        eguneratzen = false;
        balidatu();
    }

    private void aplikatu() {
        if (balidatu() == null) {
            return;
        }
        EventHandler<ActionEvent> kudeatzailea = onAplikatu.get();
        if (kudeatzailea != null) {
            kudeatzailea.handle(new ActionEvent(this, this));
        }
    }

    /** Datak egiaztatzen ditu; okerrak badira mezua erakutsi eta null itzultzen du. */
    private DataTartea balidatu() {
        LocalDate h = hasiera.getValue();
        LocalDate a = amaiera.getValue();
        String errorea = null;
        if (h == null || a == null) {
            errorea = "Aukeratu bi datak";
        } else if (a.isBefore(h)) {
            errorea = "Amaiera-data hasiera baino lehenagokoa da";
        }
        mezua.setText(errorea == null ? "" : errorea);
        mezua.setVisible(errorea != null);
        for (DatePicker dp : new DatePicker[]{hasiera, amaiera}) {
            dp.getStyleClass().remove("okerra");
            if (errorea != null) {
                dp.getStyleClass().add("okerra");
            }
        }
        return errorea == null ? new DataTartea(h, a) : null;
    }

    /** Aukeratutako data-tartea, edo null datak okerrak badira (mezua erakusten da). */
    public DataTartea getTartea() {
        return balidatu();
    }

    /** Aldi bat aukeratzen du kodetik (adib. hasieran), aplikatu gabe. */
    public void aldiaEzarri(Aldia aldia) {
        DataTartea tartea = aldia.tartea(LocalDate.now());
        eguneratzen = true;
        aldiak.setValue(aldia);
        if (tartea != null) {
            hasiera.setValue(tartea.hasiera());
            amaiera.setValue(tartea.amaiera());
        }
        eguneratzen = false;
    }

    /** Kargatzen ari den bitartean iragazkia desgaitzeko. */
    public void gaitu(boolean bai) {
        aldiak.setDisable(!bai);
        hasiera.setDisable(!bai);
        amaiera.setDisable(!bai);
        erakutsi.setDisable(!bai);
    }

    public EventHandler<ActionEvent> getOnAplikatu() { return onAplikatu.get(); }
    public void setOnAplikatu(EventHandler<ActionEvent> kudeatzailea) { onAplikatu.set(kudeatzailea); }
    public ObjectProperty<EventHandler<ActionEvent>> onAplikatuProperty() { return onAplikatu; }
}
