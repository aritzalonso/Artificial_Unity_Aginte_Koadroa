package eus.artificialunity.agintekoadroa.modeloa;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Bezero batek ordaindu ez duen faktura (osorik edo zati batean). Zenbatekoak BEZarekin. */
public record FakturaZain(String faktura, String bezeroa, LocalDate data, LocalDate mugaEguna,
                          BigDecimal guztira, BigDecimal ordaintzeko, int beranduEgunak, String egoera) {

    /** Muga-eguna pasata dago. */
    public boolean berandu() {
        return beranduEgunak > 0;
    }
}
