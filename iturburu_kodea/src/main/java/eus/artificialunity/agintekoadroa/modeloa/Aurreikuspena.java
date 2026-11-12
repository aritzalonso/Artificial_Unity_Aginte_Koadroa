package eus.artificialunity.agintekoadroa.modeloa;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Hurrengo hilabeteko salmenten aurreikuspena.
 *
 * @param hilabetea             aurreikusten den hilabetea
 * @param balioa                erregresio linealaren emaitza (joera jarraituz)
 * @param batezBestekoMugikorra azken 3 hilabeteen batez bestekoa (alderatzeko)
 * @param hilabeteKopurua       kalkuluan erabilitako hilabeteak
 * @param malda                 hilabeteko aldaketa (joera): positiboa = gora
 */
public record Aurreikuspena(YearMonth hilabetea, BigDecimal balioa, BigDecimal batezBestekoMugikorra,
                            int hilabeteKopurua, BigDecimal malda) {

    public boolean goraka() {
        return malda.signum() > 0;
    }
}
