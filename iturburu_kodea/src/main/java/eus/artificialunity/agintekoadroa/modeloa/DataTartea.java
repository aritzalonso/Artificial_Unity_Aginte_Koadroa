package eus.artificialunity.agintekoadroa.modeloa;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Data-tarte itxia [hasiera, amaiera]: bi egunak barne.
 * Aldi ohikoenak sortzeko metodoak ditu (azken 12 hilabeteak, aurten...).
 */
public record DataTartea(LocalDate hasiera, LocalDate amaiera) {

    public DataTartea {
        Objects.requireNonNull(hasiera, "Hasiera-data falta da");
        Objects.requireNonNull(amaiera, "Amaiera-data falta da");
        if (amaiera.isBefore(hasiera)) {
            throw new IllegalArgumentException("Amaiera-data ezin da hasiera-data baino lehenagokoa izan");
        }
    }

    /** Uneko hilabetea eta aurreko 11 hilabeteak (gaur arte). */
    public static DataTartea azkenHamabiHilabete(LocalDate gaur) {
        return new DataTartea(gaur.withDayOfMonth(1).minusMonths(11), gaur);
    }

    /** Uneko hilabetea eta aurreko 2 hilabeteak (gaur arte). */
    public static DataTartea azkenHiruHilabete(LocalDate gaur) {
        return new DataTartea(gaur.withDayOfMonth(1).minusMonths(2), gaur);
    }

    public static DataTartea hilabeteHau(LocalDate gaur) {
        return new DataTartea(gaur.withDayOfMonth(1), gaur);
    }

    public static DataTartea aurten(LocalDate gaur) {
        return new DataTartea(gaur.withDayOfYear(1), gaur);
    }

    public static DataTartea iaz(LocalDate gaur) {
        LocalDate iazkoUrtarrila = gaur.minusYears(1).withDayOfYear(1);
        return new DataTartea(iazkoUrtarrila, iazkoUrtarrila.withMonth(12).withDayOfMonth(31));
    }

    /** Egun kopurua, bi muturrak barne. */
    public long egunak() {
        return ChronoUnit.DAYS.between(hasiera, amaiera) + 1;
    }

    /** Luzera bereko aurreko aldia (alderatzeko), hasiera-dataren aurreko egunean amaitzen dena. */
    public DataTartea aurrekoa() {
        return new DataTartea(hasiera.minusDays(egunak()), hasiera.minusDays(1));
    }
}
