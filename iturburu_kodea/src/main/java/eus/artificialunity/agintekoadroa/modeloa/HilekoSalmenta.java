package eus.artificialunity.agintekoadroa.modeloa;

import java.math.BigDecimal;
import java.time.YearMonth;

/** Hilabete bateko salmentak (BEZik gabe) eta eskaera kopurua. */
public record HilekoSalmenta(YearMonth hilabetea, BigDecimal salmentak, int eskaerak) {
}
