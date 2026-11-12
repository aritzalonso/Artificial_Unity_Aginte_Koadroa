package eus.artificialunity.agintekoadroa.dao.jdbc;

import eus.artificialunity.agintekoadroa.dao.DaoSalbuespena;
import eus.artificialunity.agintekoadroa.dao.StockDao;
import eus.artificialunity.agintekoadroa.modeloa.StockLerroa;

import java.util.List;

/**
 * Stockaren kontsulta (sql/aginte_kontsultak.sql, 5. kontsulta): produktu biltegiragarri aktibo guztiak,
 * kokapen internoetako kopuruarekin eta, berrornitze-araua badute, haren minimo eta maximoarekin.
 * Data-tarterik gabe (gaurko egoera).
 */
public class JdbcStockDao extends JdbcDaoOinarria implements StockDao {

    private static final String STOCKA = """
            SELECT pp.default_code                                                   AS erreferentzia,
                   COALESCE(pt.name->>'eu_ES', pt.name->>'es_ES', pt.name->>'en_US') AS produktua,
                   COALESCE(pc.name, '')                                             AS kategoria,
                   COALESCE(st.kopurua, 0)                                           AS stocka,
                   COALESCE(op.product_min_qty, 0)                                   AS minimoa,
                   COALESCE(op.product_max_qty, 0)                                   AS maximoa,
                   op.id IS NOT NULL                                                 AS araua_du,
                   COALESCE(op.id IS NOT NULL AND COALESCE(st.kopurua, 0) < op.product_min_qty, false) AS stock_baxua
            FROM product_product pp
            JOIN product_template pt      ON pt.id = pp.product_tmpl_id
            LEFT JOIN product_category pc ON pc.id = pt.categ_id
            LEFT JOIN LATERAL (
                SELECT o.id, o.product_min_qty, o.product_max_qty, kok.parent_path
                FROM stock_warehouse_orderpoint o
                JOIN stock_location kok ON kok.id = o.location_id
                WHERE o.product_id = pp.id AND o.active
                ORDER BY o.id
                LIMIT 1
            ) op ON true
            LEFT JOIN LATERAL (
                SELECT SUM(q.quantity) AS kopurua
                FROM stock_quant q
                JOIN stock_location l ON l.id = q.location_id
                WHERE q.product_id = pp.id
                  AND l.usage = 'internal'
                  AND (op.parent_path IS NULL OR l.parent_path LIKE op.parent_path || '%')
            ) st ON true
            WHERE pp.active AND pt.active AND pt.is_storable
            ORDER BY stock_baxua DESC, pp.default_code
            """;

    public JdbcStockDao(KonexioFabrika konexioFabrika) {
        super(konexioFabrika);
    }

    @Override
    public List<StockLerroa> stockaGaur() throws DaoSalbuespena {
        return kontsultatu(STOCKA, ps -> { },
                rs -> new StockLerroa(testua(rs, "erreferentzia"), testua(rs, "produktua"), testua(rs, "kategoria"),
                        dirua(rs, "stocka"), dirua(rs, "minimoa"), dirua(rs, "maximoa"),
                        rs.getBoolean("araua_du"), rs.getBoolean("stock_baxua")));
    }
}
