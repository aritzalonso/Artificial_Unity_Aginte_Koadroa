package eus.artificialunity.agintekoadroa.kontroladoreak;

import eus.artificialunity.agintekoadroa.dao.DaoSalbuespena;
import eus.artificialunity.agintekoadroa.konfigurazioa.KonfigurazioSalbuespena;
import eus.artificialunity.agintekoadroa.modeloa.AginteDatuak;
import eus.artificialunity.agintekoadroa.modeloa.DataTartea;
import eus.artificialunity.agintekoadroa.modeloa.Taula;
import eus.artificialunity.agintekoadroa.osagaiak.DataIragazkia;
import eus.artificialunity.agintekoadroa.osagaiak.ErrorePanela;
import eus.artificialunity.agintekoadroa.osagaiak.KargaGeruza;
import eus.artificialunity.agintekoadroa.osagaiak.SalmentaGrafikoa;
import eus.artificialunity.agintekoadroa.utilak.Baliabideak;
import eus.artificialunity.agintekoadroa.utilak.Erregistroa;
import eus.artificialunity.agintekoadroa.utilak.Formatua;
import eus.artificialunity.agintekoadroa.utilak.IrudiUtilak;
import eus.artificialunity.agintekoadroa.zerbitzuak.AginteZerbitzua;
import eus.artificialunity.agintekoadroa.zerbitzuak.TxostenZerbitzua;
import javafx.application.HostServices;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Leiho nagusiaren kontroladorea: menua (pantailen arteko nabigazioa), data-iragazkia, datuen karga
 * atzeko planoan (interfazea blokeatu gabe), erroreak "Berriro saiatu" botoiarekin, txostenen deskarga
 * (PDF eta CSV), laguntza-leihoa eta laster-teklak.
 */
public class NagusiaKontroladorea {

    private static final Logger LOG = Erregistroa.logger(NagusiaKontroladorea.class);

    @FXML private ImageView logoa;
    @FXML private Button eguneratuBotoia;
    @FXML private Button pdfBotoia;
    @FXML private Button csvBotoia;
    @FXML private Button laguntzaBotoia;
    @FXML private DataIragazkia dataIragazkia;
    @FXML private Label eguneratzeEtiketa;
    @FXML private VBox menua;
    @FXML private Label pantailaIzenburua;
    @FXML private Label pantailaAzalpena;
    @FXML private StackPane bistaEdukiontzia;
    @FXML private KargaGeruza kargaGeruza;
    @FXML private ErrorePanela errorePanela;
    @FXML private Label egoeraEtiketa;

    private final Map<Pantaila, Node> bistak = new EnumMap<>(Pantaila.class);
    private final Map<Pantaila, PantailaKontroladorea> kontroladoreak = new EnumMap<>(Pantaila.class);
    private final Map<Pantaila, ToggleButton> menuBotoiak = new EnumMap<>(Pantaila.class);
    private final ToggleGroup menuTaldea = new ToggleGroup();
    private final ExecutorService exekutatzailea = Executors.newSingleThreadExecutor(r -> {
        Thread haria = new Thread(r, "datu-karga");
        haria.setDaemon(true);
        return haria;
    });
    private final TxostenZerbitzua txostenZerbitzua = new TxostenZerbitzua();

    private ZerbitzuHornitzailea hornitzailea;
    private AginteZerbitzua zerbitzua;
    private HostServices hostServices;
    private Stage leihoa;
    private Stage laguntzaLeihoa;
    private LaguntzaKontroladorea laguntzaKontroladorea;
    private AginteDatuak datuak;
    private Pantaila unekoa = Pantaila.LABURPENA;
    private boolean kargatzen;

    @FXML
    private void initialize() {
        logoa.setImage(new Image(Baliabideak.logoa()));
        eguneratuBotoia.setTooltip(new Tooltip("Datuak berriro irakurri Odoo-tik (F5)"));
        pdfBotoia.setTooltip(new Tooltip("Txosten osoa PDFn gorde: zifrak, grafikoa eta taulak (Ctrl+P)"));
        laguntzaBotoia.setTooltip(new Tooltip("Aplikazioa nola erabili (F1)"));
        menuaSortu();
        bistakKargatu();
        deskargakGaitu(false);
    }

    private void menuaSortu() {
        for (Pantaila p : Pantaila.values()) {
            ToggleButton botoia = new ToggleButton(p.izena());
            botoia.getStyleClass().add("menu-botoia");
            botoia.setToggleGroup(menuTaldea);
            botoia.setMaxWidth(Double.MAX_VALUE);
            botoia.setMnemonicParsing(false);
            Tooltip tt = new Tooltip(p.azalpena() + "  (" + p.lasterTeklaTestua() + ")");
            tt.setWrapText(true);
            tt.setMaxWidth(320);
            botoia.setTooltip(tt);
            botoia.setOnAction(e -> pantailaErakutsi(p));
            menuBotoiak.put(p, botoia);
            menua.getChildren().add(botoia);
        }
        // Menuko botoi bat beti aukeratuta egon dadin
        menuTaldea.selectedToggleProperty().addListener((obs, zaharra, berria) -> {
            if (berria == null && zaharra != null) {
                menuTaldea.selectToggle(zaharra);
            }
        });
    }

    private void bistakKargatu() {
        for (Pantaila p : Pantaila.values()) {
            try {
                FXMLLoader kargatzailea = new FXMLLoader(Baliabideak.fxml(p.fxml()));
                Parent bista = kargatzailea.load();
                PantailaKontroladorea kontroladorea = kargatzailea.getController();
                kontroladorea.nabigazioaEzarri(this::pantailaErakutsi);
                ScrollPane korritzea = new ScrollPane(bista);
                korritzea.setFitToWidth(true);
                korritzea.setFitToHeight(true);
                korritzea.getStyleClass().add("pantaila-korritzea");
                korritzea.setVisible(false);
                bistak.put(p, korritzea);
                kontroladoreak.put(p, kontroladorea);
                bistaEdukiontzia.getChildren().add(korritzea);
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Ezin izan da bista kargatu: " + p.fxml(), e);
                throw new IllegalStateException("Ezin izan da " + p.izena() + " pantaila kargatu", e);
            }
        }
    }

    /**
     * Aplikazioa abiaraztean deitzen da (FXML kargatu ondoren).
     *
     * @param hasierakoPantaila komando-lerroan aukeratutakoa (--pantaila=...), edo Laburpena
     */
    public void hasieratu(ZerbitzuHornitzailea hornitzaileBerria, HostServices hostServicesBerria, Stage leihoBerria,
                          Pantaila hasierakoPantaila) {
        this.hornitzailea = hornitzaileBerria;
        this.hostServices = hostServicesBerria;
        this.leihoa = leihoBerria;
        lasterTeklakEzarri(leihoBerria.getScene());
        pantailaErakutsi(hasierakoPantaila);
        kargatu();
    }

    private void lasterTeklakEzarri(Scene eszena) {
        for (Pantaila p : Pantaila.values()) {
            eszena.getAccelerators().put(p.lasterTekla(), () -> pantailaErakutsi(p));
        }
        eszena.getAccelerators().put(new KeyCodeCombination(KeyCode.F1), this::laguntzaErakutsi);
        eszena.getAccelerators().put(new KeyCodeCombination(KeyCode.F5), this::kargatu);
        eszena.getAccelerators().put(new KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN), this::pdfGorde);
        eszena.getAccelerators().put(new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN), this::csvGorde);
    }

    /** Pantaila bat erakusten du eta menuan nabarmentzen du. */
    public void pantailaErakutsi(Pantaila p) {
        unekoa = p;
        bistak.forEach((pantaila, bista) -> bista.setVisible(pantaila == p));
        menuTaldea.selectToggle(menuBotoiak.get(p));
        pantailaIzenburua.setText(p.izena());
        pantailaAzalpena.setText(p.azalpena());
        csvBotoia.setTooltip(new Tooltip("Pantaila honetako datuak (" + p.izena()
                + ") Excel-en irekitzeko moduan gorde (Ctrl+E)"));
        if (laguntzaKontroladorea != null) {
            laguntzaKontroladorea.atalaIreki(p);
        }
    }

    // --- Ekitaldiak (FXML) ---

    @FXML
    private void eguneratu(ActionEvent e) {
        kargatu();
    }

    @FXML
    private void iragazkiaAplikatu(ActionEvent e) {
        kargatu();
    }

    @FXML
    private void berrizSaiatu(ActionEvent e) {
        kargatu();
    }

    @FXML
    private void pdfDeskargatu(ActionEvent e) {
        pdfGorde();
    }

    @FXML
    private void csvDeskargatu(ActionEvent e) {
        csvGorde();
    }

    @FXML
    private void laguntzaIreki(ActionEvent e) {
        laguntzaErakutsi();
    }

    // --- Datuen karga ---

    /** Datuak atzeko planoan kargatzen ditu; bitartean karga-geruza erakusten da. */
    private void kargatu() {
        if (kargatzen) {
            return;
        }
        DataTartea tartea = dataIragazkia.getTartea();
        if (tartea == null) {
            return;
        }
        kargatzen = true;
        errorePanela.setVisible(false);
        kargaGeruza.setMezua("Datuak kargatzen...");
        kargaGeruza.setVisible(true);
        dataIragazkia.gaitu(false);
        eguneratuBotoia.setDisable(true);
        deskargakGaitu(false);

        Task<AginteDatuak> zeregina = new Task<>() {
            @Override
            protected AginteDatuak call() throws Exception {
                if (zerbitzua == null) {
                    zerbitzua = hornitzailea.sortu();
                }
                return zerbitzua.kargatu(tartea);
            }
        };
        zeregina.setOnSucceeded(e -> {
            datuak = zeregina.getValue();
            kontroladoreak.forEach((p, k) -> k.erakutsi(datuak));
            eguneratzeEtiketa.setText("Azken eguneraketa: " + Formatua.dataOrdua(datuak.noiz()));
            egoeraEtiketa.setText("Datuak Odoo-tik zuzenean  ·  Aldia: " + Formatua.data(tartea.hasiera()) + " - "
                    + Formatua.data(tartea.amaiera()) + "  ·  Stocka eta fakturak: gaurko egoera");
            kargaAmaitu();
            deskargakGaitu(true);
            LOG.info("Datuak kargatuta: " + tartea);
        });
        zeregina.setOnFailed(e -> {
            Throwable errorea = zeregina.getException();
            zerbitzua = null;
            kargaAmaitu();
            deskargakGaitu(datuak != null);
            erroreaErakutsi(errorea);
        });
        exekutatzailea.submit(zeregina);
    }

    private void kargaAmaitu() {
        kargatzen = false;
        kargaGeruza.setVisible(false);
        dataIragazkia.gaitu(true);
        eguneratuBotoia.setDisable(false);
    }

    private void erroreaErakutsi(Throwable errorea) {
        String izenburua;
        String mezua;
        if (errorea instanceof KonfigurazioSalbuespena) {
            izenburua = "Konfigurazioan arazo bat dago";
            mezua = errorea.getMessage() + "\n\nAbisatu informatikariari.";
        } else if (errorea instanceof DaoSalbuespena dao) {
            izenburua = switch (dao.getMota()) {
                case KONEXIOA -> "Ezin da zerbitzariarekin konektatu";
                case SARBIDEA -> "Zerbitzariak ez du sarbidea onartu";
                case DENBORA -> "Zerbitzariak ez du garaiz erantzun";
                case DATUAK -> "Ezin izan dira datuak irakurri";
            };
            mezua = dao.getMessage();
        } else {
            izenburua = "Ustekabeko errore bat gertatu da";
            mezua = "Saiatu berriro. Errorea errepikatzen bada, abisatu informatikariari.";
        }
        LOG.log(Level.WARNING, izenburua, errorea);
        egoeraEtiketa.setText("Datuak ezin izan dira kargatu");
        errorePanela.erakutsi(izenburua, mezua, errorea);
    }

    private void deskargakGaitu(boolean bai) {
        pdfBotoia.setDisable(!bai);
        csvBotoia.setDisable(!bai);
    }

    // --- Txostenak ---

    private void pdfGorde() {
        if (datuak == null || kargatzen) {
            return;
        }
        File fitxategia = gordetzekoFitxategia("Gorde PDF txostena", "salmenten_txostena_" + LocalDate.now() + ".pdf",
                new FileChooser.ExtensionFilter("PDF dokumentua (*.pdf)", "*.pdf"));
        if (fitxategia == null) {
            return;
        }
        byte[] grafikoa;
        try {
            grafikoa = IrudiUtilak.png(SalmentaGrafikoa.irudia(datuak.hilekoak(), datuak.aurreikuspena(),
                    Baliabideak.estiloOrria(), 900, 400));
        } catch (IOException | RuntimeException ex) {
            LOG.log(Level.WARNING, "Ezin izan da grafikoaren irudia sortu; PDFa grafikorik gabe", ex);
            grafikoa = null;
        }
        AginteDatuak d = datuak;
        byte[] png = grafikoa;
        gordeAtzekoPlanoan("PDF txostena sortzen...", fitxategia,
                () -> txostenZerbitzua.pdf(d, png, fitxategia.toPath()));
    }

    private void csvGorde() {
        if (datuak == null || kargatzen) {
            return;
        }
        Pantaila p = unekoa;
        File fitxategia = gordetzekoFitxategia("Gorde datuak Excel-erako (CSV)",
                p.fitxategiIzena() + "_" + LocalDate.now() + ".csv",
                new FileChooser.ExtensionFilter("Excel-erako datuak (*.csv)", "*.csv"));
        if (fitxategia == null) {
            return;
        }
        Taula taula = p.taula(datuak);
        gordeAtzekoPlanoan("Datuak gordetzen...", fitxategia, () -> txostenZerbitzua.csv(taula, fitxategia.toPath()));
    }

    private File gordetzekoFitxategia(String izenburua, String izena, FileChooser.ExtensionFilter iragazkia) {
        FileChooser hautatzailea = new FileChooser();
        hautatzailea.setTitle(izenburua);
        hautatzailea.setInitialFileName(izena);
        hautatzailea.getExtensionFilters().add(iragazkia);
        Path dokumentuak = Path.of(System.getProperty("user.home"), "Documents");
        if (Files.isDirectory(dokumentuak)) {
            hautatzailea.setInitialDirectory(dokumentuak.toFile());
        }
        return hautatzailea.showSaveDialog(leihoa);
    }

    /** Fitxategi-idazketa atzeko planoan egiten du eta amaitzean emaitza jakinarazten du. */
    private void gordeAtzekoPlanoan(String mezua, File fitxategia, Idazketa idazketa) {
        kargaGeruza.setMezua(mezua);
        kargaGeruza.setVisible(true);
        deskargakGaitu(false);
        Task<Void> zeregina = new Task<>() {
            @Override
            protected Void call() throws Exception {
                idazketa.idatzi();
                return null;
            }
        };
        zeregina.setOnSucceeded(e -> {
            kargaGeruza.setVisible(false);
            deskargakGaitu(true);
            LOG.info("Txostena gordeta: " + fitxategia);
            gordetaJakinarazi(fitxategia);
        });
        zeregina.setOnFailed(e -> {
            kargaGeruza.setVisible(false);
            deskargakGaitu(true);
            LOG.log(Level.WARNING, "Ezin izan da txostena gorde: " + fitxategia, zeregina.getException());
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.initOwner(leihoa);
            alerta.setTitle("Ezin izan da gorde");
            alerta.setHeaderText("Ezin izan da fitxategia gorde");
            alerta.setContentText("Egiaztatu fitxategia ez dagoela beste programa batean irekita (adib. Excel-en) "
                    + "eta karpetan idazteko baimena duzula.\n\n" + fitxategia);
            alerta.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alerta.showAndWait();
        });
        exekutatzailea.submit(zeregina);
    }

    private void gordetaJakinarazi(File fitxategia) {
        ButtonType ireki = new ButtonType("Ireki", ButtonBar.ButtonData.OK_DONE);
        ButtonType ados = new ButtonType("Ados", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, "", ireki, ados);
        alerta.initOwner(leihoa);
        alerta.setTitle("Gordeta");
        alerta.setHeaderText("Fitxategia ondo gorde da");
        alerta.setContentText(fitxategia.getAbsolutePath());
        alerta.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alerta.getDialogPane().setPrefWidth(Math.max(480, fitxategia.getAbsolutePath().length() * 8.5 + 80));
        alerta.showAndWait().filter(b -> b == ireki).ifPresent(b -> hostServices.showDocument(fitxategia.toURI().toString()));
    }

    @FunctionalInterface
    private interface Idazketa {
        void idatzi() throws IOException;
    }

    // --- Laguntza ---

    private void laguntzaErakutsi() {
        try {
            if (laguntzaLeihoa == null) {
                FXMLLoader kargatzailea = new FXMLLoader(Baliabideak.fxml("laguntza.fxml"));
                Parent erroa = kargatzailea.load();
                laguntzaKontroladorea = kargatzailea.getController();
                laguntzaLeihoa = new Stage();
                laguntzaLeihoa.initOwner(leihoa);
                laguntzaLeihoa.initModality(Modality.NONE);
                laguntzaLeihoa.setTitle("Laguntza - Aginte-koadroa");
                laguntzaLeihoa.getIcons().add(new Image(Baliabideak.ikonoa()));
                Scene eszena = new Scene(erroa, 720, 680);
                eszena.getStylesheets().add(Baliabideak.estiloOrria());
                laguntzaLeihoa.setScene(eszena);
                laguntzaKontroladorea.leihoaEzarri(laguntzaLeihoa);
            }
            laguntzaKontroladorea.atalaIreki(unekoa);
            laguntzaLeihoa.show();
            laguntzaLeihoa.toFront();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Ezin izan da laguntza ireki", e);
        }
    }

    /** Leihoa ixtean atzeko planoko haria gelditu. */
    public void itxi() {
        exekutatzailea.shutdownNow();
    }
}
