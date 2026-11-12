package eus.artificialunity.agintekoadroa.utilak;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormatuaTest {

    @Test
    void diruaEspainiakoFormatuan() {
        assertEquals("1.234,56 €", Formatua.dirua(new BigDecimal("1234.555")));
        assertEquals("349.723 €", Formatua.diruaOsoa(new BigDecimal("349722.6")));
        assertEquals("0,00 €", Formatua.dirua(null));
    }

    @Test
    void zenbakiakEtaEhunekoak() {
        assertEquals("1.234", Formatua.zenbakia(new BigDecimal("1234")));
        assertEquals("12,5", Formatua.zenbakia(new BigDecimal("12.50")));
        assertEquals("+12,5 %", Formatua.portzentaia(12.5));
        assertEquals("-3,0 %", Formatua.portzentaia(-3));
    }

    @Test
    void datakEtaHilabeteakEuskaraz() {
        assertEquals("26/10/2026", Formatua.data(LocalDate.of(2026, 10, 26)));
        assertEquals("2026ko urria", Formatua.hilabetea(YearMonth.of(2026, 10)));
        assertEquals("2025 aza", Formatua.hilabeteLaburra(YearMonth.of(2025, 11)));
    }
}
