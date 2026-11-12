package eus.artificialunity.agintekoadroa.modeloa;

import java.util.List;

/**
 * Txostenetarako taula orokorra (CSV eta PDF): zutabeak bere motarekin eta lerroak balio gordinekin.
 * Formatua (1.234,56 € edo 1234,56) txosten bakoitzak ematen dio.
 */
public record Taula(String izenburua, List<Zutabea> zutabeak, List<List<Object>> lerroak) {

    /** Zutabe baten mota: balioa nola idatzi erabakitzeko. */
    public enum Mota { TESTUA, ZENBAKIA, DIRUA, DATA, HILABETEA, EHUNEKOA, BAI_EZ }

    public record Zutabea(String izena, Mota mota) {
    }

    public Taula {
        zutabeak = List.copyOf(zutabeak);
        lerroak = List.copyOf(lerroak);
    }
}
