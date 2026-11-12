package eus.artificialunity.agintekoadroa.zerbitzuak;

import eus.artificialunity.agintekoadroa.modeloa.Aurreikuspena;
import eus.artificialunity.agintekoadroa.modeloa.HilekoSalmenta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AurreikuspenKalkulatzaileaTest {

    private final AurreikuspenKalkulatzailea kalkulatzailea = new AurreikuspenKalkulatzailea();

    private static List<HilekoSalmenta> hilekoak(YearMonth hasiera, double... balioak) {
        List<HilekoSalmenta> zerrenda = new ArrayList<>();
        for (int i = 0; i < balioak.length; i++) {
            zerrenda.add(new HilekoSalmenta(hasiera.plusMonths(i), BigDecimal.valueOf(balioak[i]), 1));
        }
        return zerrenda;
    }

    @Test
    void daturikGabeNullItzultzenDu() {
        assertNull(kalkulatzailea.kalkulatu(List.of()));
        assertNull(kalkulatzailea.kalkulatu(null));
    }

    @Test
    void joeraLinealZehatzaJarraitzenDu() {
        // 100, 200, 300, 400 -> hurrengoa 500 (malda 100)
        Aurreikuspena a = kalkulatzailea.kalkulatu(hilekoak(YearMonth.of(2026, 1), 100, 200, 300, 400));
        assertEquals(YearMonth.of(2026, 5), a.hilabetea());
        assertEquals(0, new BigDecimal("500.00").compareTo(a.balioa()));
        assertEquals(0, new BigDecimal("100.00").compareTo(a.malda()));
        assertTrue(a.goraka());
        // Azken 3 hilabeteen batez bestekoa: (200 + 300 + 400) / 3 = 300
        assertEquals(0, new BigDecimal("300.00").compareTo(a.batezBestekoMugikorra()));
        assertEquals(4, a.hilabeteKopurua());
    }

    @Test
    void ezDaNegatiboaIzaten() {
        Aurreikuspena a = kalkulatzailea.kalkulatu(hilekoak(YearMonth.of(2026, 1), 300, 200, 100, 0));
        assertEquals(0, BigDecimal.ZERO.compareTo(a.balioa()));
        assertFalse(a.goraka());
    }

    @Test
    void hilabeteBakarrarekinBalioBeraErabiltzenDu() {
        Aurreikuspena a = kalkulatzailea.kalkulatu(hilekoak(YearMonth.of(2025, 12), 1234.5));
        assertEquals(YearMonth.of(2026, 1), a.hilabetea());
        assertEquals(0, new BigDecimal("1234.50").compareTo(a.balioa()));
    }
}
