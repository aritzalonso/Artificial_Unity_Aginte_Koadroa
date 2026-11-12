package eus.artificialunity.agintekoadroa.zerbitzuak;

import eus.artificialunity.agintekoadroa.modeloa.Aurreikuspena;
import eus.artificialunity.agintekoadroa.modeloa.HilekoSalmenta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Hurrengo hilabeteko salmentak aurreikusten ditu:
 * <ul>
 *     <li>Erregresio lineala (karratu txikienak) hileko salmentekin: y = a + b·x, x = 0..n-1.
 *         Aurreikuspena x = n puntuan; ezin da negatiboa izan.</li>
 *     <li>Batez besteko mugikorra: azken 3 hilabeteen batez bestekoa (erreferentzia gisa).</li>
 * </ul>
 * Hilabete bakarra badago, joerarik ez dagoenez, balio hori bera erabiltzen da.
 */
public class AurreikuspenKalkulatzailea {

    static final int MUGIKORRAREN_HILABETEAK = 3;

    /** Aurreikuspena kalkulatzen du; null, daturik ez badago. */
    public Aurreikuspena kalkulatu(List<HilekoSalmenta> hilekoak) {
        if (hilekoak == null || hilekoak.isEmpty()) {
            return null;
        }
        int n = hilekoak.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int x = 0; x < n; x++) {
            double y = hilekoak.get(x).salmentak().doubleValue();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += (double) x * x;
        }
        double izendatzailea = n * sumX2 - sumX * sumX;
        double malda = izendatzailea == 0 ? 0 : (n * sumXY - sumX * sumY) / izendatzailea;
        double ebakidura = (sumY - malda * sumX) / n;
        double aurreikuspena = Math.max(0, ebakidura + malda * n);

        int hasiera = Math.max(0, n - MUGIKORRAREN_HILABETEAK);
        BigDecimal mugikorra = hilekoak.subList(hasiera, n).stream()
                .map(HilekoSalmenta::salmentak)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(n - hasiera), 2, RoundingMode.HALF_UP);

        return new Aurreikuspena(hilekoak.get(n - 1).hilabetea().plusMonths(1),
                BigDecimal.valueOf(aurreikuspena).setScale(2, RoundingMode.HALF_UP),
                mugikorra, n,
                BigDecimal.valueOf(malda).setScale(2, RoundingMode.HALF_UP));
    }
}
