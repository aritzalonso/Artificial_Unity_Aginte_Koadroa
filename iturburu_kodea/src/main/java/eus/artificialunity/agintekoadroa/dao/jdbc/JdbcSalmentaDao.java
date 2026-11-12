package eus.artificialunity.agintekoadroa.dao.jdbc;

import eus.artificialunity.agintekoadroa.dao.DaoSalbuespena;
import eus.artificialunity.agintekoadroa.dao.SalmentaDao;
import eus.artificialunity.agintekoadroa.modeloa.BezeroSalmenta;
import eus.artificialunity.agintekoadroa.modeloa.DataTartea;
import eus.artificialunity.agintekoadroa.modeloa.HilekoSalmenta;
import eus.artificialunity.agintekoadroa.modeloa.KategoriaSalmenta;
import eus.artificialunity.agintekoadroa.modeloa.Laburpena;
import eus.artificialunity.agintekoadroa.modeloa.ProduktuSalmenta;

import java.time.YearMonth;
import java.util.List;

/**
 * Salmenten kontsultak PostgreSQL-en (Odoo 19). sql/aginte_kontsultak.sql fitxategiko 1-4 eta 7 kontsultak dira,
 * :'hasiera' eta :'amaiera' aldagaien ordez parametro prestatuekin (?).
 * Datak UTC-n gordetzen dira: Madrilgo ordura pasatzen dira egunka/hileka taldekatu aurretik.
 */
public class JdbcSalmentaDao extends JdbcDaoOinarria implements SalmentaDao {

    /** Eskaeraren data Madrilgo orduan, data-tartearen barruan (2 parametro: hasiera, amaiera). */
    private static final String DATA_TARTEAN =
            "(so.date_order AT TIME ZONE 'UTC' AT TIME ZONE 'Europe/Madrid')::date BETWEEN CAST(? AS date) AND CAST(? AS date)";

    private static final String LABURPENA = """
            SELECT COALESCE(SUM(so.amount_untaxed), 0)            AS salmentak_guztira,
                   COUNT(*)                                       AS eskaerak,
                   ROUND(COALESCE(AVG(so.amount_untaxed), 0), 2)  AS batez_besteko_eskaera,
                   COUNT(DISTINCT so.partner_id)                  AS bezeroak,
                   (SELECT COALESCE(SUM(m.amount_residual), 0)
                      FROM account_move m
                     WHERE m.move_type = 'out_invoice' AND m.state = 'posted'
                       AND m.payment_state IN ('not_paid', 'partial')) AS kobratzeko_zain
            FROM sale_order so
            WHERE so.state = 'sale'
              AND """ + DATA_TARTEAN;

    private static final String HILEKOAK = """
            WITH hilabeteak AS (
                SELECT generate_series(date_trunc('month', CAST(? AS date)),
                                       date_trunc('month', CAST(? AS date)),
                                       interval '1 month')::date AS hila
            ),
            salmentak AS (
                SELECT date_trunc('month', so.date_order AT TIME ZONE 'UTC' AT TIME ZONE 'Europe/Madrid')::date AS hila,
                       SUM(so.amount_untaxed) AS zenbatekoa,
                       COUNT(*)               AS eskaerak
                FROM sale_order so
                WHERE so.state = 'sale'
                  AND """ + DATA_TARTEAN + """

                GROUP BY 1
            )
            SELECT to_char(h.hila, 'YYYY-MM') AS hilabetea,
                   COALESCE(s.zenbatekoa, 0)  AS salmentak,
                   COALESCE(s.eskaerak, 0)    AS eskaerak
            FROM hilabeteak h
            LEFT JOIN salmentak s ON s.hila = h.hila
            ORDER BY h.hila
            """;

    private static final String PRODUKTUAK = """
            SELECT pp.default_code                                                   AS erreferentzia,
                   COALESCE(pt.name->>'eu_ES', pt.name->>'es_ES', pt.name->>'en_US') AS produktua,
                   SUM(sol.product_uom_qty)                                          AS unitateak,
                   SUM(sol.price_subtotal)                                           AS zenbatekoa
            FROM sale_order_line sol
            JOIN sale_order so        ON so.id = sol.order_id
            JOIN product_product pp   ON pp.id = sol.product_id
            JOIN product_template pt  ON pt.id = pp.product_tmpl_id
            WHERE so.state = 'sale'
              AND sol.display_type IS NULL
              AND pt.active
              AND """ + DATA_TARTEAN + """

            GROUP BY pp.default_code, produktua
            ORDER BY unitateak DESC, zenbatekoa DESC
            LIMIT ?
            """;

    private static final String BEZEROAK = """
            SELECT rp.name                AS bezeroa,
                   rp.city                AS herria,
                   COUNT(*)               AS eskaerak,
                   SUM(so.amount_untaxed) AS salmentak,
                   MAX(so.date_order AT TIME ZONE 'UTC' AT TIME ZONE 'Europe/Madrid')::date AS azken_eskaera
            FROM sale_order so
            JOIN res_partner rp ON rp.id = so.partner_id
            WHERE so.state = 'sale'
              AND """ + DATA_TARTEAN + """

            GROUP BY rp.id, rp.name, rp.city
            ORDER BY salmentak DESC
            """;

    private static final String KATEGORIAK = """
            SELECT COALESCE(pc.name, '-')   AS kategoria,
                   SUM(sol.product_uom_qty) AS unitateak,
                   SUM(sol.price_subtotal)  AS zenbatekoa
            FROM sale_order_line sol
            JOIN sale_order so        ON so.id = sol.order_id
            JOIN product_product pp   ON pp.id = sol.product_id
            JOIN product_template pt  ON pt.id = pp.product_tmpl_id
            LEFT JOIN product_category pc ON pc.id = pt.categ_id
            WHERE so.state = 'sale'
              AND sol.display_type IS NULL
              AND """ + DATA_TARTEAN + """

            GROUP BY pc.name
            ORDER BY zenbatekoa DESC
            """;

    public JdbcSalmentaDao(KonexioFabrika konexioFabrika) {
        super(konexioFabrika);
    }

    @Override
    public Laburpena laburpena(DataTartea tartea) throws DaoSalbuespena {
        return bakarra(LABURPENA,
                ps -> {
                    ps.setObject(1, tartea.hasiera());
                    ps.setObject(2, tartea.amaiera());
                },
                rs -> new Laburpena(dirua(rs, "salmentak_guztira"), rs.getInt("eskaerak"),
                        dirua(rs, "batez_besteko_eskaera"), rs.getInt("bezeroak"), dirua(rs, "kobratzeko_zain")));
    }

    @Override
    public List<HilekoSalmenta> hilekoSalmentak(DataTartea tartea) throws DaoSalbuespena {
        return kontsultatu(HILEKOAK,
                ps -> {
                    ps.setObject(1, tartea.hasiera());
                    ps.setObject(2, tartea.amaiera());
                    ps.setObject(3, tartea.hasiera());
                    ps.setObject(4, tartea.amaiera());
                },
                rs -> new HilekoSalmenta(YearMonth.parse(rs.getString("hilabetea")),
                        dirua(rs, "salmentak"), rs.getInt("eskaerak")));
    }

    @Override
    public List<ProduktuSalmenta> produktuSalduenak(DataTartea tartea, int muga) throws DaoSalbuespena {
        return kontsultatu(PRODUKTUAK,
                ps -> {
                    ps.setObject(1, tartea.hasiera());
                    ps.setObject(2, tartea.amaiera());
                    ps.setInt(3, muga);
                },
                rs -> new ProduktuSalmenta(testua(rs, "erreferentzia"), testua(rs, "produktua"),
                        dirua(rs, "unitateak"), dirua(rs, "zenbatekoa")));
    }

    @Override
    public List<BezeroSalmenta> bezerokoSalmentak(DataTartea tartea) throws DaoSalbuespena {
        return kontsultatu(BEZEROAK,
                ps -> {
                    ps.setObject(1, tartea.hasiera());
                    ps.setObject(2, tartea.amaiera());
                },
                rs -> new BezeroSalmenta(testua(rs, "bezeroa"), testua(rs, "herria"), rs.getInt("eskaerak"),
                        dirua(rs, "salmentak"), data(rs, "azken_eskaera")));
    }

    @Override
    public List<KategoriaSalmenta> kategoriakoSalmentak(DataTartea tartea) throws DaoSalbuespena {
        return kontsultatu(KATEGORIAK,
                ps -> {
                    ps.setObject(1, tartea.hasiera());
                    ps.setObject(2, tartea.amaiera());
                },
                rs -> new KategoriaSalmenta(testua(rs, "kategoria"), dirua(rs, "unitateak"), dirua(rs, "zenbatekoa")));
    }
}
