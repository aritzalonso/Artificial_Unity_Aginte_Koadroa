package eus.artificialunity.agintekoadroa.modeloa;

import java.math.BigDecimal;

/**
 * Aldi bateko adierazle nagusiak (KPI txartelak).
 *
 * @param salmentak      salmentak guztira, BEZik gabe
 * @param eskaerak       baieztatutako salmenta-eskaerak
 * @param batezBestekoa  eskaera bakoitzeko batez besteko zenbatekoa
 * @param bezeroak       erosi duten bezero desberdinak
 * @param kobratzekoZain bezeroek gaur ordaintzeko duten guztia (data-tarterik gabe, BEZarekin)
 */
public record Laburpena(BigDecimal salmentak, int eskaerak, BigDecimal batezBestekoa,
                        int bezeroak, BigDecimal kobratzekoZain) {
}
