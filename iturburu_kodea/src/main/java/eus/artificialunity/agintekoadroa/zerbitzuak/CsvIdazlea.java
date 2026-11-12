package eus.artificialunity.agintekoadroa.zerbitzuak;

import eus.artificialunity.agintekoadroa.modeloa.Taula;
import eus.artificialunity.agintekoadroa.utilak.Formatua;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Taula bat CSV fitxategi batean idazten du, Excel-ek zuzenean ireki dezan (Espainiako ezarpenak):
 * ";" bereizlea, koma hamartarra, UTF-8 BOM-arekin (azentuak eta € ondo ikusteko) eta CRLF lerro-amaierak.
 */
public class CsvIdazlea {

    static final char BEREIZLEA = ';';
    private static final String LERRO_AMAIERA = "\r\n";

    public void idatzi(Taula taula, Path helburua) throws IOException {
        try (Writer idazlea = Files.newBufferedWriter(helburua, StandardCharsets.UTF_8)) {
            idazlea.write('﻿');
            idazlea.write(testua(taula));
        }
    }

    /** CSV edukia testu gisa (BOM-ik gabe). */
    public String testua(Taula taula) {
        StringBuilder sb = new StringBuilder();
        sb.append(taula.zutabeak().stream().map(z -> komatxoak(z.izena()))
                .collect(Collectors.joining(String.valueOf(BEREIZLEA)))).append(LERRO_AMAIERA);
        for (List<Object> lerroa : taula.lerroak()) {
            for (int i = 0; i < lerroa.size(); i++) {
                if (i > 0) {
                    sb.append(BEREIZLEA);
                }
                sb.append(komatxoak(balioa(lerroa.get(i), taula.zutabeak().get(i).mota())));
            }
            sb.append(LERRO_AMAIERA);
        }
        return sb.toString();
    }

    static String balioa(Object balioa, Taula.Mota mota) {
        if (balioa == null) {
            return "";
        }
        if (balioa instanceof BigDecimal zenbakia) {
            return Formatua.csvZenbakia(zenbakia);
        }
        if (balioa instanceof LocalDate data) {
            return Formatua.data(data);
        }
        if (balioa instanceof YearMonth hila) {
            return hila.toString();
        }
        if (balioa instanceof Boolean baiEz) {
            return baiEz ? "Bai" : "Ez";
        }
        return balioa.toString();
    }

    /** Komatxoak gehitzen ditu bereizlea, komatxoak edo lerro-jauziak badaude. */
    static String komatxoak(String testua) {
        if (testua.indexOf(BEREIZLEA) >= 0 || testua.indexOf('"') >= 0
                || testua.indexOf('\n') >= 0 || testua.indexOf('\r') >= 0) {
            return '"' + testua.replace("\"", "\"\"") + '"';
        }
        return testua;
    }
}
