package eus.artificialunity.agintekoadroa.zerbitzuak;

import eus.artificialunity.agintekoadroa.modeloa.AginteDatuak;
import eus.artificialunity.agintekoadroa.modeloa.Taula;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Txostenak sortzen ditu: PDF osoa eta CSV-ak (pantaila bakoitzeko bat).
 * Fitxategiak idaztea motela izan daitekeenez, atzeko planoko hari batetik deitu behar da.
 */
public class TxostenZerbitzua {

    /** CSV fitxategi-izenak eta dagokien taula. */
    static final Map<String, Function<AginteDatuak, Taula>> CSV_GUZTIAK = new LinkedHashMap<>();

    static {
        CSV_GUZTIAK.put("laburpena", TaulaFabrika::laburpena);
        CSV_GUZTIAK.put("salmentak_hileka", TaulaFabrika::hilekoak);
        CSV_GUZTIAK.put("produktu_salduenak", TaulaFabrika::produktuak);
        CSV_GUZTIAK.put("bezeroak", TaulaFabrika::bezeroak);
        CSV_GUZTIAK.put("kategoriak", TaulaFabrika::kategoriak);
        CSV_GUZTIAK.put("stocka", TaulaFabrika::stocka);
        CSV_GUZTIAK.put("kobratzeko_fakturak", TaulaFabrika::fakturak);
    }

    private final PdfTxostenSortzailea pdfSortzailea;
    private final CsvIdazlea csvIdazlea;

    public TxostenZerbitzua() {
        this(new PdfTxostenSortzailea(), new CsvIdazlea());
    }

    public TxostenZerbitzua(PdfTxostenSortzailea pdfSortzailea, CsvIdazlea csvIdazlea) {
        this.pdfSortzailea = pdfSortzailea;
        this.csvIdazlea = csvIdazlea;
    }

    public void pdf(AginteDatuak datuak, byte[] grafikoaPng, Path helburua) throws IOException {
        pdfSortzailea.sortu(datuak, grafikoaPng, helburua);
    }

    public void csv(Taula taula, Path helburua) throws IOException {
        csvIdazlea.idatzi(taula, helburua);
    }

    /** PDFa eta CSV guztiak karpeta batean (komando-lerroko --esportatu aukerarako). */
    public List<Path> denakEsportatu(AginteDatuak datuak, byte[] grafikoaPng, Path karpeta) throws IOException {
        Files.createDirectories(karpeta);
        String data = datuak.noiz().toLocalDate().toString();
        List<Path> sortuak = new ArrayList<>();
        Path pdf = karpeta.resolve("salmenten_txostena_" + data + ".pdf");
        pdf(datuak, grafikoaPng, pdf);
        sortuak.add(pdf);
        for (Map.Entry<String, Function<AginteDatuak, Taula>> sarrera : CSV_GUZTIAK.entrySet()) {
            Path csv = karpeta.resolve(sarrera.getKey() + "_" + data + ".csv");
            csv(sarrera.getValue().apply(datuak), csv);
            sortuak.add(csv);
        }
        return sortuak;
    }
}
