package eus.artificialunity.agintekoadroa.dao.jdbc;

import eus.artificialunity.agintekoadroa.dao.DaoSalbuespena;
import eus.artificialunity.agintekoadroa.dao.FakturaDao;
import eus.artificialunity.agintekoadroa.modeloa.FakturaZain;

import java.util.List;

/**
 * Kobratzeko dauden bezero-fakturak (sql/aginte_kontsultak.sql, 6. kontsulta), berandu daudenak lehenik.
 * Berandu-egunak gaurko dataren arabera kalkulatzen dira.
 */
public class JdbcFakturaDao extends JdbcDaoOinarria implements FakturaDao {

    private static final String FAKTURAK = """
            SELECT m.name                                         AS faktura,
                   rp.name                                        AS bezeroa,
                   m.invoice_date                                 AS data,
                   m.invoice_date_due                             AS muga_eguna,
                   m.amount_total                                 AS guztira_bezarekin,
                   m.amount_residual                              AS ordaintzeko,
                   GREATEST(CURRENT_DATE - m.invoice_date_due, 0) AS berandu_egunak,
                   CASE m.payment_state WHEN 'partial' THEN 'Zati bat ordainduta' ELSE 'Ordaindu gabe' END AS egoera
            FROM account_move m
            JOIN res_partner rp ON rp.id = m.partner_id
            WHERE m.move_type = 'out_invoice'
              AND m.state = 'posted'
              AND m.payment_state IN ('not_paid', 'partial')
            ORDER BY m.invoice_date_due, m.name
            """;

    public JdbcFakturaDao(KonexioFabrika konexioFabrika) {
        super(konexioFabrika);
    }

    @Override
    public List<FakturaZain> kobratzekoZain() throws DaoSalbuespena {
        return kontsultatu(FAKTURAK, ps -> { },
                rs -> new FakturaZain(testua(rs, "faktura"), testua(rs, "bezeroa"), data(rs, "data"),
                        data(rs, "muga_eguna"), dirua(rs, "guztira_bezarekin"), dirua(rs, "ordaintzeko"),
                        rs.getInt("berandu_egunak"), testua(rs, "egoera")));
    }
}
