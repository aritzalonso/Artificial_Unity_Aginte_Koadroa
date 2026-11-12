package eus.artificialunity.agintekoadroa.zerbitzuak;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import eus.artificialunity.agintekoadroa.modeloa.AginteDatuak;
import eus.artificialunity.agintekoadroa.modeloa.Aurreikuspena;
import eus.artificialunity.agintekoadroa.modeloa.Laburpena;
import eus.artificialunity.agintekoadroa.modeloa.Taula;
import eus.artificialunity.agintekoadroa.utilak.Formatua;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Predicate;

/**
 * Salmenten txostena PDFn (OpenPDF): logoa, aldia, sortze-data, adierazleak, salmenten grafikoa
 * eta aginte-koadroko taula guztiak (TaulaFabrika-renak, pantailako zifra berberak).
 */
public class PdfTxostenSortzailea {

    private static final String LOGOA = "/eus/artificialunity/agintekoadroa/irudiak/logoa.png";
    private static final Color MOREA = new Color(0x4F, 0x46, 0xE5);
    private static final Color ZIANA = new Color(0x06, 0xB6, 0xD4);
    private static final Color GRISA = new Color(0x6B, 0x72, 0x80);
    private static final Color ARGIA = new Color(0xEE, 0xF2, 0xFF);
    private static final Color GORRI_ARGIA = new Color(0xFE, 0xE2, 0xE2);
    private static final Color GORRIA = new Color(0xB9, 0x1C, 0x1C);
    private static final Color BERDEA = new Color(0x15, 0x80, 0x3D);
    private static final Color ERTZA = new Color(0xD1, 0xD5, 0xDB);

    private static final Font IZENBURUA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, MOREA);
    private static final Font ATALA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, MOREA);
    private static final Font TESTUA = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
    private static final Font TXIKIA = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, GRISA);
    private static final Font GOIBURUA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font GELAXKA = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
    private static final Font GOIBURU_TXIKIA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
    private static final Font GELAXKA_TXIKIA = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);
    private static final Font KPI_IZENA = FontFactory.getFont(FontFactory.HELVETICA, 9, GRISA);
    private static final Font KPI_BALIOA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, Color.BLACK);

    /**
     * @param grafikoaPng salmenten grafikoaren irudia (PNG); null bada, grafikorik gabe
     */
    public void sortu(AginteDatuak d, byte[] grafikoaPng, Path helburua) throws IOException {
        Document dokumentua = new Document(PageSize.A4, 40, 40, 40, 50);
        try (OutputStream irteera = Files.newOutputStream(helburua)) {
            PdfWriter idazlea = PdfWriter.getInstance(dokumentua, irteera);
            idazlea.setPageEvent(new OrriOina());
            dokumentua.addTitle("Salmenten txostena - Artificial Unity");
            dokumentua.addAuthor("Artificial Unity S.L.");
            dokumentua.addCreator("Artificial Unity - Aginte-koadroa");
            dokumentua.open();

            goiburua(dokumentua, d);
            adierazleak(dokumentua, d);
            grafikoa(dokumentua, d, grafikoaPng);
            taula(dokumentua, TaulaFabrika.hilekoak(d), new float[]{3, 3, 2, 2},
                    lerroa -> "Aurreikuspena".equals(lerroa.get(3)), ARGIA);
            taula(dokumentua, TaulaFabrika.produktuak(d), new float[]{1.2f, 2.2f, 7, 1.8f, 2.8f}, null, null);
            taula(dokumentua, TaulaFabrika.bezeroak(d), new float[]{5, 3, 1.8f, 3, 2.4f}, null, null);
            taula(dokumentua, TaulaFabrika.kategoriak(d), new float[]{5, 2, 3, 2}, null, null);
            stockarenOharra(dokumentua, d);
            taula(dokumentua, TaulaFabrika.stocka(d), new float[]{2.4f, 6.2f, 3.6f, 1.6f, 1.8f, 1.8f, 1.8f},
                    lerroa -> Boolean.TRUE.equals(lerroa.get(6)), GORRI_ARGIA);
            fakturenOharra(dokumentua, d);
            taula(dokumentua, TaulaFabrika.fakturak(d), new float[]{2.9f, 4.1f, 2.1f, 2.1f, 2.2f, 2.3f, 1.7f, 2.8f},
                    lerroa -> ((BigDecimal) lerroa.get(6)).signum() > 0, GORRI_ARGIA);
            // Irteera-fluxua itxi aurretik itxi behar da (bestela PDFaren amaiera ez da idazten)
            dokumentua.close();
        } catch (DocumentException e) {
            throw new IOException("Ezin izan da PDF txostena sortu: " + e.getMessage(), e);
        }
    }

    private void goiburua(Document dokumentua, AginteDatuak d) throws DocumentException, IOException {
        PdfPTable goiburua = new PdfPTable(new float[]{1, 1.4f});
        goiburua.setWidthPercentage(100);
        PdfPCell logoGelaxka = new PdfPCell();
        logoGelaxka.setBorder(Rectangle.NO_BORDER);
        logoGelaxka.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Image logoa = logoa();
        if (logoa != null) {
            logoa.scaleToFit(190, 42);
            logoGelaxka.addElement(logoa);
        }
        goiburua.addCell(logoGelaxka);

        PdfPCell testuGelaxka = new PdfPCell();
        testuGelaxka.setBorder(Rectangle.NO_BORDER);
        Paragraph izenburua = new Paragraph("Salmenten txostena", IZENBURUA);
        izenburua.setAlignment(Element.ALIGN_RIGHT);
        Paragraph aldia = new Paragraph("Aldia: " + Formatua.data(d.tartea().hasiera()) + " - "
                + Formatua.data(d.tartea().amaiera()), TESTUA);
        aldia.setAlignment(Element.ALIGN_RIGHT);
        Paragraph sortua = new Paragraph("Sortua: " + Formatua.dataOrdua(d.noiz()), TXIKIA);
        sortua.setAlignment(Element.ALIGN_RIGHT);
        testuGelaxka.addElement(izenburua);
        testuGelaxka.addElement(aldia);
        testuGelaxka.addElement(sortua);
        goiburua.addCell(testuGelaxka);
        dokumentua.add(goiburua);

        PdfPTable marra = new PdfPTable(1);
        marra.setWidthPercentage(100);
        marra.setSpacingBefore(6);
        PdfPCell marraGelaxka = new PdfPCell();
        marraGelaxka.setFixedHeight(3);
        marraGelaxka.setBorder(Rectangle.NO_BORDER);
        marraGelaxka.setBackgroundColor(ZIANA);
        marra.addCell(marraGelaxka);
        dokumentua.add(marra);
        dokumentua.add(new Paragraph("Zenbatekoak eurotan. Salmentak BEZik gabe; kobratzeko dagoena BEZarekin. "
                + "Stocka eta fakturak gaurko egoera dira.", TXIKIA));
    }

    private Image logoa() throws IOException {
        try (InputStream sarrera = getClass().getResourceAsStream(LOGOA)) {
            if (sarrera == null) {
                return null;
            }
            return Image.getInstance(sarrera.readAllBytes());
        } catch (com.lowagie.text.BadElementException e) {
            return null;
        }
    }

    private void adierazleak(Document dokumentua, AginteDatuak d) throws DocumentException {
        atala(dokumentua, "Adierazle nagusiak");
        Laburpena l = d.laburpena();
        OptionalDouble aldaketa = d.salmentenAldaketa();
        String aldaketaTestua = aldaketa.isPresent()
                ? Formatua.portzentaia(aldaketa.getAsDouble()) + " aurreko aldiarekin"
                : "Aurreko aldian ez dago salmentarik";
        Aurreikuspena ar = d.aurreikuspena();

        PdfPTable taula = new PdfPTable(3);
        taula.setWidthPercentage(100);
        kpi(taula, "Salmentak guztira", Formatua.dirua(l.salmentak()), aldaketaTestua);
        kpi(taula, "Salmenta-eskaerak", Formatua.zenbakia(l.eskaerak()), l.bezeroak() + " bezero desberdin");
        kpi(taula, "Batez besteko eskaera", Formatua.dirua(l.batezBestekoa()), "eskaera bakoitzeko");
        kpi(taula, "Kobratzeko zain", Formatua.dirua(l.kobratzekoZain()),
                d.fakturak().size() + " faktura, " + d.beranduKopurua() + " berandu");
        kpi(taula, "Stock baxuko produktuak", Formatua.zenbakia(d.stockBaxukoKopurua()),
                "minimotik behera, " + d.stocka().size() + " produktutatik");
        kpi(taula, "Hurrengo hilabeterako aurreikuspena", ar == null ? "-" : Formatua.dirua(ar.balioa()),
                ar == null ? "Daturik ez" : Formatua.hilabetea(ar.hilabetea()) + (ar.goraka() ? ", joera gora" : ", joera behera"));
        dokumentua.add(taula);
    }

    private void kpi(PdfPTable taula, String izena, String balioa, String azalpena) {
        PdfPCell gelaxka = new PdfPCell();
        gelaxka.setBorderColor(ERTZA);
        gelaxka.setPadding(8);
        gelaxka.addElement(new Paragraph(izena, KPI_IZENA));
        gelaxka.addElement(new Paragraph(balioa, KPI_BALIOA));
        gelaxka.addElement(new Paragraph(azalpena, TXIKIA));
        taula.addCell(gelaxka);
    }

    private void grafikoa(Document dokumentua, AginteDatuak d, byte[] grafikoaPng) throws DocumentException, IOException {
        atala(dokumentua, "Salmenten bilakaera hileka");
        if (grafikoaPng != null) {
            Image irudia = Image.getInstance(grafikoaPng);
            irudia.scaleToFit(dokumentua.getPageSize().getWidth() - 80, 250);
            irudia.setAlignment(Element.ALIGN_CENTER);
            dokumentua.add(irudia);
        }
        Aurreikuspena ar = d.aurreikuspena();
        if (ar != null) {
            Paragraph p = new Paragraph();
            p.add(new Chunk("Aurreikuspena (" + Formatua.hilabetea(ar.hilabetea()) + "): ", TESTUA));
            p.add(new Chunk(Formatua.dirua(ar.balioa()), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, MOREA)));
            p.add(new Chunk(". Azken " + ar.hilabeteKopurua() + " hilabeteen joera (erregresio lineala). "
                    + "Azken 3 hilabeteen batez bestekoa: " + Formatua.dirua(ar.batezBestekoMugikorra()) + ".", TESTUA));
            dokumentua.add(p);
        }
    }

    private void stockarenOharra(Document dokumentua, AginteDatuak d) throws DocumentException {
        long baxuak = d.stockBaxukoKopurua();
        if (baxuak > 0) {
            Paragraph p = new Paragraph("ABISUA: " + baxuak + " produkturen stocka minimotik behera dago (gorriz markatuta).",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, GORRIA));
            p.setSpacingBefore(10);
            dokumentua.add(p);
        } else {
            Paragraph p = new Paragraph("Produktu guztien stocka minimoaren gainetik dago.",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BERDEA));
            p.setSpacingBefore(10);
            dokumentua.add(p);
        }
    }

    private void fakturenOharra(Document dokumentua, AginteDatuak d) throws DocumentException {
        Paragraph p = new Paragraph(d.fakturak().size() + " faktura kobratzeko: "
                + Formatua.dirua(d.laburpena().kobratzekoZain()) + ". Berandu: " + d.beranduKopurua()
                + " (" + Formatua.dirua(d.beranduZenbatekoa()) + "), gorriz markatuta.",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, d.beranduKopurua() > 0 ? GORRIA : BERDEA));
        p.setSpacingBefore(10);
        dokumentua.add(p);
    }

    private void atala(Document dokumentua, String izena) throws DocumentException {
        Paragraph p = new Paragraph(izena, ATALA);
        p.setSpacingBefore(12);
        p.setSpacingAfter(6);
        dokumentua.add(p);
    }

    /**
     * Taula bat gehitzen du; {@code nabarmendu} betetzen duten lerroak {@code kolorea} atzealdearekin.
     */
    private void taula(Document dokumentua, Taula t, float[] zabalerak, Predicate<List<Object>> nabarmendu, Color kolorea)
            throws DocumentException {
        atala(dokumentua, t.izenburua());
        if (t.lerroak().isEmpty()) {
            dokumentua.add(new Paragraph("Ez dago daturik aukeratutako aldian.", TESTUA));
            return;
        }
        PdfPTable taula = new PdfPTable(zabalerak);
        taula.setWidthPercentage(100);
        taula.setHeaderRows(1);
        // Zutabe asko dituzten taulak letra txikiagoarekin, testua ez zatitzeko
        boolean zabala = t.zutabeak().size() >= 7;
        Font goiburuLetra = zabala ? GOIBURU_TXIKIA : GOIBURUA;
        Font gelaxkaLetra = zabala ? GELAXKA_TXIKIA : GELAXKA;
        for (Taula.Zutabea z : t.zutabeak()) {
            PdfPCell gelaxka = new PdfPCell(new Phrase(z.izena(), goiburuLetra));
            gelaxka.setBackgroundColor(MOREA);
            gelaxka.setBorderColor(MOREA);
            gelaxka.setPadding(5);
            gelaxka.setHorizontalAlignment(zenbakizkoa(z.mota()) ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
            taula.addCell(gelaxka);
        }
        for (List<Object> lerroa : t.lerroak()) {
            boolean nabarmenduta = nabarmendu != null && nabarmendu.test(lerroa);
            for (int i = 0; i < lerroa.size(); i++) {
                Taula.Mota mota = t.zutabeak().get(i).mota();
                PdfPCell gelaxka = new PdfPCell(new Phrase(balioa(lerroa.get(i), mota), gelaxkaLetra));
                gelaxka.setBorderColor(ERTZA);
                gelaxka.setPadding(4);
                gelaxka.setHorizontalAlignment(zenbakizkoa(mota) ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
                if (nabarmenduta) {
                    gelaxka.setBackgroundColor(kolorea);
                }
                taula.addCell(gelaxka);
            }
        }
        dokumentua.add(taula);
    }

    private static boolean zenbakizkoa(Taula.Mota mota) {
        return mota == Taula.Mota.ZENBAKIA || mota == Taula.Mota.DIRUA || mota == Taula.Mota.EHUNEKOA;
    }

    /** Balioa pantailako formatuan: 1.234,56 €, 26/10/2026, 2026ko urria... */
    static String balioa(Object balioa, Taula.Mota mota) {
        if (balioa == null) {
            return "";
        }
        if (balioa instanceof BigDecimal zenbakia) {
            return switch (mota) {
                case DIRUA -> Formatua.dirua(zenbakia);
                case EHUNEKOA -> Formatua.ehunekoa(zenbakia.doubleValue());
                default -> Formatua.zenbakia(zenbakia);
            };
        }
        if (balioa instanceof LocalDate data) {
            return Formatua.data(data);
        }
        if (balioa instanceof YearMonth hila) {
            return Formatua.hilabetea(hila);
        }
        if (balioa instanceof Boolean baiEz) {
            return baiEz ? "Bai" : "Ez";
        }
        return balioa.toString();
    }

    /** Orri bakoitzaren oina: enpresa eta orri-zenbakia. */
    private static final class OrriOina extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter idazlea, Document dokumentua) {
            ColumnText.showTextAligned(idazlea.getDirectContent(), Element.ALIGN_CENTER,
                    new Phrase("Artificial Unity S.L. - Aginte-koadroa - " + idazlea.getPageNumber() + ". orria", TXIKIA),
                    (dokumentua.left() + dokumentua.right()) / 2, dokumentua.bottom() - 25, 0);
        }
    }
}
