package eus.artificialunity.agintekoadroa.dao.jdbc;

import eus.artificialunity.agintekoadroa.dao.DaoSalbuespena;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC DAO guztien oinarria: konexioa ireki, kontsulta prestatua exekutatu, emaitzak
 * modeloko objektuetara bihurtu eta SQLException-ak erabiltzailearentzako mezu argietara itzuli.
 */
public abstract class JdbcDaoOinarria {

    /** PreparedStatement-eko parametroak ezartzen ditu. */
    @FunctionalInterface
    protected interface ParametroEzartzailea {
        void ezarri(PreparedStatement ps) throws SQLException;
    }

    /** ResultSet-eko lerro bat modeloko objektu bihurtzen du. */
    @FunctionalInterface
    protected interface LerroMapatzailea<T> {
        T mapatu(ResultSet rs) throws SQLException;
    }

    private final KonexioFabrika konexioFabrika;

    protected JdbcDaoOinarria(KonexioFabrika konexioFabrika) {
        this.konexioFabrika = konexioFabrika;
    }

    protected <T> List<T> kontsultatu(String sql, ParametroEzartzailea parametroak, LerroMapatzailea<T> mapatzailea)
            throws DaoSalbuespena {
        try (Connection konexioa = konexioFabrika.konektatu();
             PreparedStatement ps = konexioa.prepareStatement(sql)) {
            ps.setQueryTimeout(konexioFabrika.kontsultaDenbora());
            parametroak.ezarri(ps);
            List<T> emaitza = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    emaitza.add(mapatzailea.mapatu(rs));
                }
            }
            return emaitza;
        } catch (SQLException e) {
            throw itzuli(e);
        }
    }

    protected <T> T bakarra(String sql, ParametroEzartzailea parametroak, LerroMapatzailea<T> mapatzailea)
            throws DaoSalbuespena {
        List<T> emaitza = kontsultatu(sql, parametroak, mapatzailea);
        if (emaitza.isEmpty()) {
            throw new DaoSalbuespena(DaoSalbuespena.Mota.DATUAK, "Datu-baseak ez du emaitzarik itzuli.", null);
        }
        return emaitza.get(0);
    }

    /** SQLException-a erabiltzaileak uler dezakeen mezu batera itzultzen du (SQLState-aren arabera). */
    DaoSalbuespena itzuli(SQLException e) {
        String egoera = e.getSQLState() == null ? "" : e.getSQLState();
        String zerbitzaria = konexioFabrika.zerbitzaria();
        if (e instanceof SQLTimeoutException || "57014".equals(egoera)) {
            return new DaoSalbuespena(DaoSalbuespena.Mota.DENBORA,
                    "Zerbitzariak denbora gehiegi behar izan du erantzuteko. Saiatu berriro geroxeago.", e);
        }
        if (egoera.startsWith("08")) {
            return new DaoSalbuespena(DaoSalbuespena.Mota.KONEXIOA,
                    "Ezin izan da enpresako zerbitzariarekin konektatu (" + zerbitzaria + ").\n"
                            + "Egiaztatu ordenagailua sarera konektatuta dagoela eta zerbitzaria piztuta dagoela.", e);
        }
        if (egoera.startsWith("28")) {
            return new DaoSalbuespena(DaoSalbuespena.Mota.SARBIDEA,
                    "Zerbitzariak ez du sarbidea onartu: erabiltzailea edo pasahitza okerra da.\n"
                            + "Informatikariak konfigurazio-fitxategia (konfigurazioa.properties) zuzendu behar du.", e);
        }
        if ("3D000".equals(egoera)) {
            return new DaoSalbuespena(DaoSalbuespena.Mota.SARBIDEA,
                    "Konfigurazioan adierazitako datu-basea ez dago zerbitzarian.\n"
                            + "Informatikariak konfigurazio-fitxategia (konfigurazioa.properties) zuzendu behar du.", e);
        }
        if ("42501".equals(egoera)) {
            return new DaoSalbuespena(DaoSalbuespena.Mota.SARBIDEA,
                    "Erabiltzaileak ez du datu hauek irakurtzeko baimenik. Abisatu informatikariari.", e);
        }
        return new DaoSalbuespena(DaoSalbuespena.Mota.DATUAK,
                "Errore bat gertatu da datuak irakurtzean. Saiatu berriro; errepikatzen bada, abisatu informatikariari.", e);
    }

    protected static BigDecimal dirua(ResultSet rs, String zutabea) throws SQLException {
        BigDecimal balioa = rs.getBigDecimal(zutabea);
        return balioa == null ? BigDecimal.ZERO : balioa;
    }

    protected static String testua(ResultSet rs, String zutabea) throws SQLException {
        String balioa = rs.getString(zutabea);
        return balioa == null ? "" : balioa;
    }

    protected static LocalDate data(ResultSet rs, String zutabea) throws SQLException {
        return rs.getObject(zutabea, LocalDate.class);
    }
}
