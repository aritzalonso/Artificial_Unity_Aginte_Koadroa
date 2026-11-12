package eus.artificialunity.agintekoadroa.modeloa;

import java.math.BigDecimal;

/**
 * Produktu baten stocka gaur, berrornitze-araua badu bere minimo eta maximoarekin.
 * stockBaxua = araua du eta stocka minimotik behera dago (n8n-ren alerten irizpide bera).
 * Araurik gabe, minimoa eta maximoa 0 dira eta ez da inoiz stock baxutzat hartzen.
 */
public record StockLerroa(String erreferentzia, String produktua, String kategoria, BigDecimal stocka,
                          BigDecimal minimoa, BigDecimal maximoa, boolean arauaDu, boolean stockBaxua) {

    /** Minimora iristeko falta diren unitateak (0, stocka nahikoa bada). */
    public BigDecimal falta() {
        BigDecimal falta = minimoa.subtract(stocka);
        return falta.signum() > 0 ? falta : BigDecimal.ZERO;
    }
}
