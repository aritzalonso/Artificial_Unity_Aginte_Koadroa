package eus.artificialunity.agintekoadroa.osagaiak;

import eus.artificialunity.agintekoadroa.modeloa.StockLerroa;
import eus.artificialunity.agintekoadroa.utilak.Formatua;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;

/**
 * Stock-taulako gelaxka: barra batek erakusten du biltegia zenbateraino dagoen beteta (maximoarekiko),
 * eta testuak unitateak. Minimotik behera badago, barra gorria da.
 */
public class StockMailaZelda<S> extends TableCell<S, StockLerroa> {

    private final ProgressBar barra = new ProgressBar();
    private final Label testua = new Label();
    private final HBox edukia = new HBox(10, barra, testua);
    private final Tooltip argibidea = new Tooltip();

    public StockMailaZelda() {
        getStyleClass().add("stock-maila-zelda");
        barra.setPrefWidth(140);
        edukia.setAlignment(Pos.CENTER_LEFT);
        argibidea.setWrapText(true);
    }

    @Override
    protected void updateItem(StockLerroa lerroa, boolean hutsik) {
        super.updateItem(lerroa, hutsik);
        if (hutsik || lerroa == null) {
            setGraphic(null);
            setTooltip(null);
            return;
        }
        if (!lerroa.arauaDu()) {
            // Berrornitze-araurik gabe ez dago minimo/maximorik: unitateak bakarrik
            barra.setVisible(false);
            testua.setText(Formatua.zenbakia(lerroa.stocka()) + " unitate");
            argibidea.setText("Stocka: " + Formatua.zenbakia(lerroa.stocka())
                    + "\nProduktu honek ez du berrornitze-araurik (minimorik gabe).");
            setTooltip(argibidea);
            setGraphic(edukia);
            return;
        }
        barra.setVisible(true);
        BigDecimal maximoa = lerroa.maximoa().signum() > 0 ? lerroa.maximoa() : lerroa.minimoa().max(BigDecimal.ONE);
        double betetzea = Math.max(0, Math.min(1, lerroa.stocka().doubleValue() / maximoa.doubleValue()));
        barra.setProgress(betetzea);
        barra.getStyleClass().removeAll("stock-barra-baxua", "stock-barra-ondo");
        barra.getStyleClass().add(lerroa.stockBaxua() ? "stock-barra-baxua" : "stock-barra-ondo");
        testua.setText(Formatua.zenbakia(lerroa.stocka()) + " unitate");
        argibidea.setText("Stocka: " + Formatua.zenbakia(lerroa.stocka())
                + "\nMinimoa: " + Formatua.zenbakia(lerroa.minimoa())
                + "\nMaximoa: " + Formatua.zenbakia(lerroa.maximoa())
                + (lerroa.stockBaxua() ? "\nMinimora iristeko falta: " + Formatua.zenbakia(lerroa.falta()) : ""));
        setTooltip(argibidea);
        setGraphic(edukia);
    }
}
