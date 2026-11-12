package eus.artificialunity.agintekoadroa.modeloa;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Aginte-koadroak pantaila guztietan erakusten dituen datuak, behin kargatuta.
 * Aldaezina da: datuak eguneratzean objektu berri bat sortzen da.
 */
public record AginteDatuak(DataTartea tartea,
                           Laburpena laburpena,
                           Laburpena aurrekoLaburpena,
                           List<HilekoSalmenta> hilekoak,
                           Aurreikuspena aurreikuspena,
                           List<ProduktuSalmenta> produktuak,
                           List<BezeroSalmenta> bezeroak,
                           List<KategoriaSalmenta> kategoriak,
                           List<StockLerroa> stocka,
                           List<FakturaZain> fakturak,
                           LocalDateTime noiz) {

    public AginteDatuak {
        hilekoak = List.copyOf(hilekoak);
        produktuak = List.copyOf(produktuak);
        bezeroak = List.copyOf(bezeroak);
        kategoriak = List.copyOf(kategoriak);
        stocka = List.copyOf(stocka);
        fakturak = List.copyOf(fakturak);
    }

    public long stockBaxukoKopurua() {
        return stocka.stream().filter(StockLerroa::stockBaxua).count();
    }

    public long beranduKopurua() {
        return fakturak.stream().filter(FakturaZain::berandu).count();
    }

    public BigDecimal beranduZenbatekoa() {
        return fakturak.stream().filter(FakturaZain::berandu)
                .map(FakturaZain::ordaintzeko).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Salmenten aldaketa aurreko aldiarekin (%); hutsik, aurreko aldian salmentarik ez bazegoen. */
    public OptionalDouble salmentenAldaketa() {
        BigDecimal aurrekoa = aurrekoLaburpena.salmentak();
        if (aurrekoa.signum() == 0) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(laburpena.salmentak().subtract(aurrekoa)
                .multiply(BigDecimal.valueOf(100)).divide(aurrekoa, 2, RoundingMode.HALF_UP).doubleValue());
    }

    /** Kategorien salmentak guztira (ehunekoak kalkulatzeko). */
    public BigDecimal kategorienGuztira() {
        return kategoriak.stream().map(KategoriaSalmenta::zenbatekoa).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
