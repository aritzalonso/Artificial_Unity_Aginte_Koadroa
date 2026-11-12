package eus.artificialunity.agintekoadroa.dao;

/**
 * Datuak irakurtzean gertatutako errorea. Mezua erabiltzaileari erakusteko moduan dago
 * (hizkera teknikorik gabe); xehetasun teknikoak kausan (getCause) gordetzen dira.
 */
public class DaoSalbuespena extends Exception {

    /** Errore motak, erabiltzaileari zer egin behar duen esateko. */
    public enum Mota {
        /** Ezin da zerbitzariarekin konektatu (sarea, zerbitzaria itzalita...). */
        KONEXIOA,
        /** Erabiltzailea edo pasahitza okerra. */
        SARBIDEA,
        /** Zerbitzariak denbora gehiegi behar izan du. */
        DENBORA,
        /** Bestelako errorea datuak irakurtzean. */
        DATUAK
    }

    private final Mota mota;

    public DaoSalbuespena(Mota mota, String mezua, Throwable kausa) {
        super(mezua, kausa);
        this.mota = mota;
    }

    public Mota getMota() {
        return mota;
    }
}
