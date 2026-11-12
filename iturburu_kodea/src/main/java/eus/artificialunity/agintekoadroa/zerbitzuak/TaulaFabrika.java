package eus.artificialunity.agintekoadroa.zerbitzuak;

import eus.artificialunity.agintekoadroa.modeloa.AginteDatuak;
import eus.artificialunity.agintekoadroa.modeloa.Aurreikuspena;
import eus.artificialunity.agintekoadroa.modeloa.BezeroSalmenta;
import eus.artificialunity.agintekoadroa.modeloa.FakturaZain;
import eus.artificialunity.agintekoadroa.modeloa.HilekoSalmenta;
import eus.artificialunity.agintekoadroa.modeloa.KategoriaSalmenta;
import eus.artificialunity.agintekoadroa.modeloa.Laburpena;
import eus.artificialunity.agintekoadroa.modeloa.ProduktuSalmenta;
import eus.artificialunity.agintekoadroa.modeloa.StockLerroa;
import eus.artificialunity.agintekoadroa.modeloa.Taula;
import eus.artificialunity.agintekoadroa.modeloa.Taula.Mota;
import eus.artificialunity.agintekoadroa.modeloa.Taula.Zutabea;
import eus.artificialunity.agintekoadroa.utilak.Formatua;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Aginte-koadroko datuak txosten-tauletara bihurtzen ditu. CSV-ak eta PDF-ak taula berberak erabiltzen dituzte,
 * pantailan ikusten diren zifra berberak izan ditzaten.
 */
public final class TaulaFabrika {

    private TaulaFabrika() {
    }

    public static Taula laburpena(AginteDatuak d) {
        Laburpena l = d.laburpena();
        Laburpena a = d.aurrekoLaburpena();
        List<List<Object>> lerroak = new ArrayList<>();
        lerroak.add(List.of("Salmentak guztira (BEZik gabe, €)", l.salmentak(), a.salmentak()));
        lerroak.add(List.of("Salmenta-eskaerak", BigDecimal.valueOf(l.eskaerak()), BigDecimal.valueOf(a.eskaerak())));
        lerroak.add(List.of("Batez besteko eskaera (€)", l.batezBestekoa(), a.batezBestekoa()));
        lerroak.add(List.of("Bezero desberdinak", BigDecimal.valueOf(l.bezeroak()), BigDecimal.valueOf(a.bezeroak())));
        lerroak.add(List.of("Kobratzeko zain gaur (BEZarekin, €)", l.kobratzekoZain(), ""));
        lerroak.add(List.of("Stock baxuko produktuak gaur", BigDecimal.valueOf(d.stockBaxukoKopurua()), ""));
        Aurreikuspena ar = d.aurreikuspena();
        if (ar != null) {
            lerroak.add(List.of("Aurreikuspena, " + Formatua.hilabetea(ar.hilabetea()) + " (€)", ar.balioa(), ""));
        }
        return new Taula("Laburpena", List.of(
                new Zutabea("Adierazlea", Mota.TESTUA),
                new Zutabea("Aukeratutako aldia", Mota.ZENBAKIA),
                new Zutabea("Aurreko aldia", Mota.ZENBAKIA)), lerroak);
    }

    public static Taula hilekoak(AginteDatuak d) {
        List<List<Object>> lerroak = new ArrayList<>();
        for (HilekoSalmenta h : d.hilekoak()) {
            lerroak.add(List.of(h.hilabetea(), h.salmentak(), BigDecimal.valueOf(h.eskaerak()), "Benetakoa"));
        }
        Aurreikuspena ar = d.aurreikuspena();
        if (ar != null) {
            lerroak.add(List.of(ar.hilabetea(), ar.balioa(), "", "Aurreikuspena"));
        }
        return new Taula("Salmentak hileka", List.of(
                new Zutabea("Hilabetea", Mota.HILABETEA),
                new Zutabea("Salmentak (BEZik gabe)", Mota.DIRUA),
                new Zutabea("Eskaerak", Mota.ZENBAKIA),
                new Zutabea("Mota", Mota.TESTUA)), lerroak);
    }

    public static Taula produktuak(AginteDatuak d) {
        List<List<Object>> lerroak = new ArrayList<>();
        int postua = 1;
        for (ProduktuSalmenta p : d.produktuak()) {
            lerroak.add(List.of(BigDecimal.valueOf(postua++), p.erreferentzia(), p.produktua(), p.unitateak(), p.zenbatekoa()));
        }
        return new Taula("Produktu salduenak", List.of(
                new Zutabea("Postua", Mota.ZENBAKIA),
                new Zutabea("Erreferentzia", Mota.TESTUA),
                new Zutabea("Produktua", Mota.TESTUA),
                new Zutabea("Unitateak", Mota.ZENBAKIA),
                new Zutabea("Salmentak (BEZik gabe)", Mota.DIRUA)), lerroak);
    }

    public static Taula bezeroak(AginteDatuak d) {
        List<List<Object>> lerroak = new ArrayList<>();
        for (BezeroSalmenta b : d.bezeroak()) {
            lerroak.add(List.of(b.bezeroa(), b.herria(), BigDecimal.valueOf(b.eskaerak()), b.salmentak(),
                    b.azkenEskaera() == null ? "" : b.azkenEskaera()));
        }
        return new Taula("Salmentak bezeroka", List.of(
                new Zutabea("Bezeroa", Mota.TESTUA),
                new Zutabea("Herria", Mota.TESTUA),
                new Zutabea("Eskaerak", Mota.ZENBAKIA),
                new Zutabea("Salmentak (BEZik gabe)", Mota.DIRUA),
                new Zutabea("Azken eskaera", Mota.DATA)), lerroak);
    }

    public static Taula kategoriak(AginteDatuak d) {
        BigDecimal guztira = d.kategorienGuztira();
        List<List<Object>> lerroak = new ArrayList<>();
        for (KategoriaSalmenta k : d.kategoriak()) {
            BigDecimal ehunekoa = guztira.signum() == 0 ? BigDecimal.ZERO
                    : k.zenbatekoa().multiply(BigDecimal.valueOf(100)).divide(guztira, 1, RoundingMode.HALF_UP);
            lerroak.add(List.of(k.kategoria(), k.unitateak(), k.zenbatekoa(), ehunekoa));
        }
        return new Taula("Salmentak kategoriaka", List.of(
                new Zutabea("Kategoria", Mota.TESTUA),
                new Zutabea("Unitateak", Mota.ZENBAKIA),
                new Zutabea("Salmentak (BEZik gabe)", Mota.DIRUA),
                new Zutabea("Ehunekoa", Mota.EHUNEKOA)), lerroak);
    }

    public static Taula stocka(AginteDatuak d) {
        List<List<Object>> lerroak = new ArrayList<>();
        for (StockLerroa s : d.stocka()) {
            lerroak.add(List.of(s.erreferentzia(), s.produktua(), s.kategoria(), s.stocka(),
                    s.arauaDu() ? s.minimoa() : "", s.arauaDu() ? s.maximoa() : "", s.stockBaxua()));
        }
        return new Taula("Stocka gaur", List.of(
                new Zutabea("Erreferentzia", Mota.TESTUA),
                new Zutabea("Produktua", Mota.TESTUA),
                new Zutabea("Kategoria", Mota.TESTUA),
                new Zutabea("Stocka", Mota.ZENBAKIA),
                new Zutabea("Minimoa", Mota.ZENBAKIA),
                new Zutabea("Maximoa", Mota.ZENBAKIA),
                new Zutabea("Stock baxua", Mota.BAI_EZ)), lerroak);
    }

    public static Taula fakturak(AginteDatuak d) {
        List<List<Object>> lerroak = new ArrayList<>();
        for (FakturaZain f : d.fakturak()) {
            lerroak.add(List.of(f.faktura(), f.bezeroa(), f.data() == null ? "" : f.data(),
                    f.mugaEguna() == null ? "" : f.mugaEguna(), f.guztira(), f.ordaintzeko(),
                    BigDecimal.valueOf(f.beranduEgunak()), f.egoera()));
        }
        return new Taula("Kobratzeko dauden fakturak", List.of(
                new Zutabea("Faktura", Mota.TESTUA),
                new Zutabea("Bezeroa", Mota.TESTUA),
                new Zutabea("Data", Mota.DATA),
                new Zutabea("Muga-eguna", Mota.DATA),
                new Zutabea("Guztira", Mota.DIRUA),
                new Zutabea("Kobratzeko", Mota.DIRUA),
                new Zutabea("Egun berandu", Mota.ZENBAKIA),
                new Zutabea("Egoera", Mota.TESTUA)), lerroak);
    }
}
