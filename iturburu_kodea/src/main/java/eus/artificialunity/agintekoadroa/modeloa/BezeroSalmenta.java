package eus.artificialunity.agintekoadroa.modeloa;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Bezero baten salmentak aldian (BEZik gabe). */
public record BezeroSalmenta(String bezeroa, String herria, int eskaerak, BigDecimal salmentak, LocalDate azkenEskaera) {
}
