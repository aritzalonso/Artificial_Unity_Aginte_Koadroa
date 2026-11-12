package eus.artificialunity.agintekoadroa.zerbitzuak;

import eus.artificialunity.agintekoadroa.dao.DaoSalbuespena;
import eus.artificialunity.agintekoadroa.dao.FakturaDao;
import eus.artificialunity.agintekoadroa.dao.SalmentaDao;
import eus.artificialunity.agintekoadroa.dao.StockDao;
import eus.artificialunity.agintekoadroa.modeloa.AginteDatuak;
import eus.artificialunity.agintekoadroa.modeloa.DataTartea;
import eus.artificialunity.agintekoadroa.modeloa.HilekoSalmenta;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Aginte-koadroaren logika nagusia: DAOetatik datu guztiak kargatu eta aurreikuspena kalkulatzen du.
 * Interfazeak (kontroladoreak) zerbitzu hau bakarrik ezagutzen du, ez JDBC-a.
 * Atzeko planoko hari batetik deitu behar da (ez interfazearen haritik).
 */
public class AginteZerbitzua {

    private final SalmentaDao salmentaDao;
    private final StockDao stockDao;
    private final FakturaDao fakturaDao;
    private final AurreikuspenKalkulatzailea aurreikuspenKalkulatzailea;
    private final int produktuKopurua;

    public AginteZerbitzua(SalmentaDao salmentaDao, StockDao stockDao, FakturaDao fakturaDao,
                           AurreikuspenKalkulatzailea aurreikuspenKalkulatzailea, int produktuKopurua) {
        this.salmentaDao = salmentaDao;
        this.stockDao = stockDao;
        this.fakturaDao = fakturaDao;
        this.aurreikuspenKalkulatzailea = aurreikuspenKalkulatzailea;
        this.produktuKopurua = produktuKopurua;
    }

    public AginteDatuak kargatu(DataTartea tartea) throws DaoSalbuespena {
        List<HilekoSalmenta> hilekoak = salmentaDao.hilekoSalmentak(tartea);
        return new AginteDatuak(
                tartea,
                salmentaDao.laburpena(tartea),
                salmentaDao.laburpena(tartea.aurrekoa()),
                hilekoak,
                aurreikuspenKalkulatzailea.kalkulatu(hilekoak),
                salmentaDao.produktuSalduenak(tartea, produktuKopurua),
                salmentaDao.bezerokoSalmentak(tartea),
                salmentaDao.kategoriakoSalmentak(tartea),
                stockDao.stockaGaur(),
                fakturaDao.kobratzekoZain(),
                LocalDateTime.now());
    }
}
