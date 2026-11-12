package eus.artificialunity.agintekoadroa.zerbitzuak;

import eus.artificialunity.agintekoadroa.modeloa.Taula;
import eus.artificialunity.agintekoadroa.modeloa.Taula.Mota;
import eus.artificialunity.agintekoadroa.modeloa.Taula.Zutabea;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvIdazleaTest {

    private final CsvIdazlea idazlea = new CsvIdazlea();

    @Test
    void excelerakoFormatuaErabiltzenDu() {
        Taula taula = new Taula("Proba", List.of(
                new Zutabea("Bezeroa", Mota.TESTUA),
                new Zutabea("Hilabetea", Mota.HILABETEA),
                new Zutabea("Salmentak", Mota.DIRUA),
                new Zutabea("Data", Mota.DATA),
                new Zutabea("Baxua", Mota.BAI_EZ)),
                List.of(List.of("Iturri; Arkitektura \"S.L.\"", YearMonth.of(2026, 10), new BigDecimal("1234.5"),
                        LocalDate.of(2026, 10, 26), true)));
        String esperotakoa = "Bezeroa;Hilabetea;Salmentak;Data;Baxua\r\n"
                + "\"Iturri; Arkitektura \"\"S.L.\"\"\";2026-10;1234,50;26/10/2026;Bai\r\n";
        assertEquals(esperotakoa, idazlea.testua(taula));
    }

    @Test
    void zenbakiOsoakHamartarrikGabe() {
        assertEquals("12", CsvIdazlea.balioa(new BigDecimal("12.000"), Mota.ZENBAKIA));
        assertEquals("1000", CsvIdazlea.balioa(new BigDecimal("1E+3"), Mota.ZENBAKIA));
        assertEquals("0,10", CsvIdazlea.balioa(new BigDecimal("0.1"), Mota.DIRUA));
    }

    @Test
    void fitxategiakBomDu(@TempDir Path karpeta) throws Exception {
        Path fitxategia = karpeta.resolve("proba.csv");
        idazlea.idatzi(new Taula("T", List.of(new Zutabea("Izena", Mota.TESTUA)), List.of(List.of("Ñandú €"))), fitxategia);
        String edukia = Files.readString(fitxategia, StandardCharsets.UTF_8);
        assertEquals('﻿', edukia.charAt(0));
        assertEquals("Izena\r\nÑandú €\r\n", edukia.substring(1));
    }
}
