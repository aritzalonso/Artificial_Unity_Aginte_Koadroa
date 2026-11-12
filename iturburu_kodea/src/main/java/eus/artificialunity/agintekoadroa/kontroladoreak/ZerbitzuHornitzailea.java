package eus.artificialunity.agintekoadroa.kontroladoreak;

import eus.artificialunity.agintekoadroa.konfigurazioa.KonfigurazioSalbuespena;
import eus.artificialunity.agintekoadroa.zerbitzuak.AginteZerbitzua;

/**
 * Aginte-zerbitzua sortzen du (konfigurazioa irakurri eta DAOak prestatu).
 * Huts egiten badu (adib. konfigurazio-fitxategia falta), "Berriro saiatu" sakatzean berriro deitzen da.
 */
@FunctionalInterface
public interface ZerbitzuHornitzailea {

    AginteZerbitzua sortu() throws KonfigurazioSalbuespena;
}
