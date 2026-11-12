package eus.artificialunity.agintekoadroa.konfigurazioa;

/**
 * Konfigurazio-fitxategia falta denean edo balio okerren bat duenean altxatzen da.
 * Mezua erabiltzaileari zuzenean erakusteko moduan idatzita dago.
 */
public class KonfigurazioSalbuespena extends Exception {

    public KonfigurazioSalbuespena(String mezua) {
        super(mezua);
    }

    public KonfigurazioSalbuespena(String mezua, Throwable kausa) {
        super(mezua, kausa);
    }
}
