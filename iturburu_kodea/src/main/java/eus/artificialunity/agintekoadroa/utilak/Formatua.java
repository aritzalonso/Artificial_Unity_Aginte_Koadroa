package eus.artificialunity.agintekoadroa.utilak;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Zenbakiak, diru-kopuruak eta datak erabiltzaileak ohi dituen moduan idazten ditu:
 * 1.234,56 €, 26/10/2026, 2026ko urria...
 * DecimalFormat ez da hari-segurua: metodo bakoitzak bere formatua sortzen du.
 */
public final class Formatua {

    private static final String[] HILABETEAK = {
            "urtarrila", "otsaila", "martxoa", "apirila", "maiatza", "ekaina",
            "uztaila", "abuztua", "iraila", "urria", "azaroa", "abendua"};
    private static final String[] HILABETE_LABURRAK = {
            "urt", "ots", "mar", "api", "mai", "eka", "uzt", "abu", "ira", "urr", "aza", "abe"};
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_ORDUA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Formatua() {
    }

    private static DecimalFormat formatua(String eredua) {
        DecimalFormatSymbols ikurrak = new DecimalFormatSymbols();
        ikurrak.setDecimalSeparator(',');
        ikurrak.setGroupingSeparator('.');
        ikurrak.setMinusSign('-');
        DecimalFormat f = new DecimalFormat(eredua, ikurrak);
        f.setRoundingMode(RoundingMode.HALF_UP);
        return f;
    }

    /** 1.234,56 € */
    public static String dirua(BigDecimal balioa) {
        return formatua("#,##0.00").format(balioa == null ? BigDecimal.ZERO : balioa) + " €";
    }

    /** 1.235 € (zentimorik gabe, grafikoetarako eta txarteletarako) */
    public static String diruaOsoa(BigDecimal balioa) {
        return formatua("#,##0").format(balioa == null ? BigDecimal.ZERO : balioa) + " €";
    }

    /** 1.234 edo 12,5 (hamartarrak behar badira bakarrik) */
    public static String zenbakia(BigDecimal balioa) {
        return formatua("#,##0.##").format(balioa == null ? BigDecimal.ZERO : balioa);
    }

    public static String zenbakia(long balioa) {
        return formatua("#,##0").format(balioa);
    }

    /** +12,5 % edo -3,0 % */
    public static String portzentaia(double balioa) {
        String zeinua = balioa > 0 ? "+" : "";
        return zeinua + formatua("0.0").format(balioa) + " %";
    }

    /** 12,5 % (zeinurik gabe) */
    public static String ehunekoa(double balioa) {
        return formatua("0.0").format(balioa) + " %";
    }

    public static String data(LocalDate data) {
        return data == null ? "" : data.format(DATA);
    }

    public static String dataOrdua(LocalDateTime noiz) {
        return noiz == null ? "" : noiz.format(DATA_ORDUA);
    }

    /** 2026ko urria */
    public static String hilabetea(YearMonth hila) {
        return hila.getYear() + "ko " + HILABETEAK[hila.getMonthValue() - 1];
    }

    /** 2026 urr (grafikoen ardatzetarako) */
    public static String hilabeteLaburra(YearMonth hila) {
        return hila.getYear() + " " + HILABETE_LABURRAK[hila.getMonthValue() - 1];
    }

    /** CSV-rako zenbakia: taldekatzerik gabe eta koma hamartarrarekin (Excel-ek zenbaki gisa irakurtzen du) */
    public static String csvZenbakia(BigDecimal balioa) {
        if (balioa == null) {
            return "";
        }
        return balioa.stripTrailingZeros().scale() <= 0
                ? balioa.setScale(0, RoundingMode.HALF_UP).toPlainString()
                : balioa.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }
}
