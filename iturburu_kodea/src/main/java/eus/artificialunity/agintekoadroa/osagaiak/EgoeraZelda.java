package eus.artificialunity.agintekoadroa.osagaiak;

import javafx.scene.control.Label;
import javafx.scene.control.TableCell;

import java.util.function.Predicate;

/**
 * Egoera-etiketa taula batean: kolorezko "pilula" bat (adib. gorria "Baxua" / berdea "Ondo").
 * Berrerabilgarria da: stock-taulan eta fakturen taulan erabiltzen da.
 *
 * @param <S> taulako lerroaren mota
 */
public class EgoeraZelda<S> extends TableCell<S, S> {

    private final Predicate<S> abisua;
    private final String abisuTestua;
    private final String ondoTestua;
    private final Label etiketa = new Label();

    public EgoeraZelda(Predicate<S> abisua, String abisuTestua, String ondoTestua) {
        this.abisua = abisua;
        this.abisuTestua = abisuTestua;
        this.ondoTestua = ondoTestua;
        getStyleClass().add("egoera-zelda");
    }

    @Override
    protected void updateItem(S lerroa, boolean hutsik) {
        super.updateItem(lerroa, hutsik);
        if (hutsik || lerroa == null) {
            setGraphic(null);
            return;
        }
        boolean abisuaDa = abisua.test(lerroa);
        etiketa.setText(abisuaDa ? abisuTestua : ondoTestua);
        etiketa.getStyleClass().setAll("label", "egoera-pilula", abisuaDa ? "egoera-abisua" : "egoera-ondo");
        setGraphic(etiketa);
    }
}
