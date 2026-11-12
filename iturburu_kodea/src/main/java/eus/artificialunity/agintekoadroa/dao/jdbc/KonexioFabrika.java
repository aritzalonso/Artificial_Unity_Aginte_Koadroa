package eus.artificialunity.agintekoadroa.dao.jdbc;

import eus.artificialunity.agintekoadroa.konfigurazioa.Konfigurazioa;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * PostgreSQL-erako konexioak sortzen ditu konfigurazio-fitxategiko datuekin.
 * Konexioak irakurtzeko soilik dira (aginte_irakurlea erabiltzaileak ere ezin du idatzi).
 */
public class KonexioFabrika {

    private final Konfigurazioa konfigurazioa;

    public KonexioFabrika(Konfigurazioa konfigurazioa) {
        this.konfigurazioa = konfigurazioa;
    }

    public Connection konektatu() throws SQLException {
        Properties p = new Properties();
        p.setProperty("user", konfigurazioa.getErabiltzailea());
        p.setProperty("password", konfigurazioa.getPasahitza());
        p.setProperty("connectTimeout", String.valueOf(konfigurazioa.getKonexioDenbora()));
        p.setProperty("loginTimeout", String.valueOf(konfigurazioa.getKonexioDenbora()));
        p.setProperty("socketTimeout", String.valueOf(konfigurazioa.getKontsultaDenbora() + 5));
        p.setProperty("ApplicationName", "Aginte-koadroa");
        p.setProperty("readOnly", "true");
        Connection konexioa = DriverManager.getConnection(konfigurazioa.jdbcUrl(), p);
        konexioa.setReadOnly(true);
        return konexioa;
    }

    public int kontsultaDenbora() {
        return konfigurazioa.getKontsultaDenbora();
    }

    public String zerbitzaria() {
        return konfigurazioa.getOstalaria() + ":" + konfigurazioa.getAtaka();
    }
}
