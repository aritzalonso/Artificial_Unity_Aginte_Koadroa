package eus.artificialunity.agintekoadroa.modeloa;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataTarteaTest {

    private static final LocalDate GAUR = LocalDate.of(2026, 10, 26);

    @Test
    void azkenHamabiHilabeteakHilabetearenLehenEgunetikHasten() {
        DataTartea t = DataTartea.azkenHamabiHilabete(GAUR);
        assertEquals(LocalDate.of(2025, 11, 1), t.hasiera());
        assertEquals(GAUR, t.amaiera());
    }

    @Test
    void aurrekoAldiakLuzeraBeraDu() {
        DataTartea t = new DataTartea(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        DataTartea a = t.aurrekoa();
        assertEquals(31, a.egunak());
        assertEquals(LocalDate.of(2026, 8, 31), a.hasiera());
        assertEquals(LocalDate.of(2026, 9, 30), a.amaiera());
    }

    @Test
    void iazUrteOsoaDa() {
        DataTartea t = DataTartea.iaz(GAUR);
        assertEquals(LocalDate.of(2025, 1, 1), t.hasiera());
        assertEquals(LocalDate.of(2025, 12, 31), t.amaiera());
    }

    @Test
    void amaieraHasieraBainoLehenagoErroreaDa() {
        assertThrows(IllegalArgumentException.class,
                () -> new DataTartea(LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 1)));
    }
}
