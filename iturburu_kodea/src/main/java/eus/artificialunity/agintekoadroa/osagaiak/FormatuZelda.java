package eus.artificialunity.agintekoadroa.osagaiak;

import eus.artificialunity.agintekoadroa.utilak.Formatua;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Function;

/**
 * Taula-gelaxka formatudun orokorra: balioa (BigDecimal, LocalDate...) testu irakurgarri bihurtzen du,
 * baina ordenatzeko balio gordina erabiltzen da (zenbakiak zenbaki gisa ordenatzen dira, ez testu gisa).
 */
public class FormatuZelda<S, T> extends TableCell<S, T> {

    private final Function<T, String> formatua;

    public FormatuZelda(Function<T, String> formatua) {
        this.formatua = formatua;
    }

    @Override
    protected void updateItem(T balioa, boolean hutsik) {
        super.updateItem(balioa, hutsik);
        setText(hutsik || balioa == null ? null : formatua.apply(balioa));
    }

    /** 1.234,56 € */
    public static <S> Callback<TableColumn<S, BigDecimal>, TableCell<S, BigDecimal>> dirua() {
        return zutabea -> new FormatuZelda<>(Formatua::dirua);
    }

    /** 1.234 */
    public static <S> Callback<TableColumn<S, BigDecimal>, TableCell<S, BigDecimal>> zenbakia() {
        return zutabea -> new FormatuZelda<>(Formatua::zenbakia);
    }

    /** 26/10/2026 */
    public static <S> Callback<TableColumn<S, LocalDate>, TableCell<S, LocalDate>> data() {
        return zutabea -> new FormatuZelda<>(Formatua::data);
    }
}
