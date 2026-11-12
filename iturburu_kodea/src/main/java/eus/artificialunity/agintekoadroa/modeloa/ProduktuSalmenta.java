package eus.artificialunity.agintekoadroa.modeloa;

import java.math.BigDecimal;

/** Produktu baten salmentak aldian: unitateak eta zenbatekoa (BEZik gabe). */
public record ProduktuSalmenta(String erreferentzia, String produktua, BigDecimal unitateak, BigDecimal zenbatekoa) {

    /** "[AU-009] Sagua..." moduko izena. */
    public String izenOsoa() {
        return erreferentzia == null || erreferentzia.isBlank() ? produktua : "[" + erreferentzia + "] " + produktua;
    }
}
