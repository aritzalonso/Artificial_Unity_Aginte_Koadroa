package eus.artificialunity.agintekoadroa;

import eus.artificialunity.agintekoadroa.dao.jdbc.JdbcFakturaDao;
import eus.artificialunity.agintekoadroa.dao.jdbc.JdbcSalmentaDao;
import eus.artificialunity.agintekoadroa.dao.jdbc.JdbcStockDao;
import eus.artificialunity.agintekoadroa.dao.jdbc.KonexioFabrika;
import eus.artificialunity.agintekoadroa.konfigurazioa.KonfigurazioSalbuespena;
import eus.artificialunity.agintekoadroa.konfigurazioa.Konfigurazioa;
import eus.artificialunity.agintekoadroa.kontroladoreak.NagusiaKontroladorea;
import eus.artificialunity.agintekoadroa.kontroladoreak.Pantaila;
import eus.artificialunity.agintekoadroa.modeloa.AginteDatuak;
import eus.artificialunity.agintekoadroa.modeloa.DataTartea;
import eus.artificialunity.agintekoadroa.osagaiak.SalmentaGrafikoa;
import eus.artificialunity.agintekoadroa.utilak.Baliabideak;
import eus.artificialunity.agintekoadroa.utilak.Erregistroa;
import eus.artificialunity.agintekoadroa.utilak.IrudiUtilak;
import eus.artificialunity.agintekoadroa.zerbitzuak.AginteZerbitzua;
import eus.artificialunity.agintekoadroa.zerbitzuak.AurreikuspenKalkulatzailea;
import eus.artificialunity.agintekoadroa.zerbitzuak.TxostenZerbitzua;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFX aplikazioa. Aukerak (komando-lerroa):
 * <ul>
 *     <li>{@code --pantaila=stocka}: pantaila horretan hasi (laburpena, bilakaera, produktuak, bezeroak,
 *     kategoriak, stocka, fakturak).</li>
 *     <li>{@code --esportatu=<karpeta>} [{@code --hasiera=AAAA-HH-EE --amaiera=AAAA-HH-EE}]: leihorik ireki gabe,
 *     PDF txostena eta CSV guztiak karpetan sortu eta irten (lehenetsia: azken 12 hilabeteak).</li>
 * </ul>
 */
public class AginteKoadroaApp extends Application {

    private static final Logger LOG = Erregistroa.logger(AginteKoadroaApp.class);

    @Override
    public void start(Stage leihoa) throws Exception {
        Erregistroa.hasieratu();
        Map<String, String> aukerak = getParameters().getNamed();
        if (aukerak.containsKey("esportatu")) {
            esportatu(Path.of(aukerak.get("esportatu")), aukerak);
            return;
        }

        FXMLLoader kargatzailea = new FXMLLoader(Baliabideak.fxml("nagusia.fxml"));
        Parent erroa = kargatzailea.load();
        // Leihoa pantailara egokitu (gehienez 1440x900), dena korritu gabe ikusteko
        Rectangle2D pantaila = Screen.getPrimary().getVisualBounds();
        Scene eszena = new Scene(erroa, Math.min(1440, pantaila.getWidth()), Math.min(900, pantaila.getHeight() - 40));
        eszena.getStylesheets().add(Baliabideak.estiloOrria());
        leihoa.setScene(eszena);
        leihoa.setTitle("Artificial Unity - Aginte-koadroa");
        leihoa.getIcons().add(new Image(Baliabideak.ikonoa()));
        leihoa.setMinWidth(1100);
        leihoa.setMinHeight(720);

        NagusiaKontroladorea kontroladorea = kargatzailea.getController();
        leihoa.setOnHidden(e -> kontroladorea.itxi());
        leihoa.show();
        kontroladorea.hasieratu(AginteKoadroaApp::zerbitzuaSortu, getHostServices(), leihoa,
                Pantaila.izenetik(aukerak.get("pantaila")));
        LOG.info("Aplikazioa abiarazita");
    }

    /** Konfigurazioa irakurri eta DAOak eta zerbitzua prestatzen ditu (dependentzien injekzioa, eskuz). */
    static AginteZerbitzua zerbitzuaSortu() throws KonfigurazioSalbuespena {
        Konfigurazioa konfigurazioa = Konfigurazioa.kargatu();
        LOG.info("Konfigurazioa: " + konfigurazioa.getFitxategia() + " -> " + konfigurazioa.jdbcUrl());
        KonexioFabrika fabrika = new KonexioFabrika(konfigurazioa);
        return new AginteZerbitzua(new JdbcSalmentaDao(fabrika), new JdbcStockDao(fabrika), new JdbcFakturaDao(fabrika),
                new AurreikuspenKalkulatzailea(), konfigurazioa.getProduktuKopurua());
    }

    /** --esportatu modua: datuak kargatu, grafikoaren irudia sortu (FX haria) eta fitxategiak idatzi. */
    private void esportatu(Path karpeta, Map<String, String> aukerak) {
        CompletableFuture.runAsync(() -> {
            int irteera = 0;
            try {
                LocalDate gaur = LocalDate.now();
                DataTartea tartea = aukerak.containsKey("hasiera")
                        ? new DataTartea(LocalDate.parse(aukerak.get("hasiera")),
                        aukerak.containsKey("amaiera") ? LocalDate.parse(aukerak.get("amaiera")) : gaur)
                        : DataTartea.azkenHamabiHilabete(gaur);
                AginteDatuak datuak = zerbitzuaSortu().kargatu(tartea);
                CompletableFuture<byte[]> irudia = new CompletableFuture<>();
                Platform.runLater(() -> {
                    try {
                        irudia.complete(IrudiUtilak.png(SalmentaGrafikoa.irudia(datuak.hilekoak(),
                                datuak.aurreikuspena(), Baliabideak.estiloOrria(), 900, 400)));
                    } catch (Exception e) {
                        irudia.completeExceptionally(e);
                    }
                });
                List<Path> sortuak = new TxostenZerbitzua().denakEsportatu(datuak, irudia.get(), karpeta);
                System.out.println("Aldia: " + tartea.hasiera() + " - " + tartea.amaiera());
                sortuak.forEach(p -> System.out.println("Sortuta: " + p.toAbsolutePath()));
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Esportazioak huts egin du", e);
                System.err.println("ERROREA: " + e.getMessage());
                irteera = 1;
            }
            Platform.exit();
            System.exit(irteera);
        });
    }
}
