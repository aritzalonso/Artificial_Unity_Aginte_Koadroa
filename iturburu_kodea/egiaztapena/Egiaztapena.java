import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Properties;

/**
 * Egiaztapen-tresna (ez da aplikazioaren parte): sql/aginte_kontsultak.sql-eko kontsultak bere horretan
 * exekutatzen ditu (psql-ren :'hasiera' eta :'amaiera' aldagaiak ordezkatuta), aplikazioaren zifrekin alderatzeko.
 * Konexioa aginte-koadroaren konfigurazio-fitxategitik irakurtzen du (pasahitza ez da komando-lerroan agertzen).
 * <p>
 * Erabilera (aginte-koadroa karpetatik):
 * java -cp %USERPROFILE%\.m2\repository\org\postgresql\postgresql\42.7.4\postgresql-42.7.4.jar
 * egiaztapena\Egiaztapena.java ..\sql\aginte_kontsultak.sql 2025-11-01 2026-10-26 [konfigurazioa.properties] > sql_emaitza.txt
 */
public class Egiaztapena {

    public static void main(String[] a) throws Exception {
        String sql = Files.readString(Path.of(a[0]));
        sql = sql.replace(":'hasiera'", "'" + a[1] + "'").replace(":'amaiera'", "'" + a[2] + "'");
        StringBuilder garbia = new StringBuilder();
        for (String lerroa : sql.split("\n")) {
            if (!lerroa.trim().startsWith("--")) {
                garbia.append(lerroa).append('\n');
            }
        }
        Properties k = new Properties();
        try (Reader r = Files.newBufferedReader(Path.of(a.length > 3 ? a[3] : "konfigurazioa.properties"), StandardCharsets.UTF_8)) {
            k.load(r);
        }
        Properties p = new Properties();
        p.setProperty("user", k.getProperty("db.erabiltzailea"));
        p.setProperty("password", k.getProperty("db.pasahitza", ""));
        String url = "jdbc:postgresql://" + k.getProperty("db.ostalaria") + ":" + k.getProperty("db.ataka", "5432")
                + "/" + k.getProperty("db.izena");
        try (Connection c = DriverManager.getConnection(url, p); Statement st = c.createStatement()) {
            c.setReadOnly(true);
            int zk = 1;
            for (String kontsulta : garbia.toString().split(";")) {
                if (kontsulta.isBlank()) {
                    continue;
                }
                System.out.println("=== " + zk++ + ". kontsulta ===");
                inprimatu(st.executeQuery(kontsulta));
            }
        }
    }

    static void inprimatu(ResultSet rs) throws Exception {
        ResultSetMetaData md = rs.getMetaData();
        StringBuilder g = new StringBuilder();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            g.append(i > 1 ? " | " : "").append(md.getColumnLabel(i));
        }
        System.out.println(g);
        while (rs.next()) {
            StringBuilder l = new StringBuilder();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                l.append(i > 1 ? " | " : "").append(rs.getString(i));
            }
            System.out.println(l);
        }
    }
}
