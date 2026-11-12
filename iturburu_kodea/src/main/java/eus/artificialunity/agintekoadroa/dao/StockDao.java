package eus.artificialunity.agintekoadroa.dao;

import eus.artificialunity.agintekoadroa.modeloa.StockLerroa;

import java.util.List;

/** Biltegiko stocka gaur, berrornitze-arauen minimoarekin alderatuta. */
public interface StockDao {

    /** Berrornitze-araua duten produktu aktibo guztiak; stock baxukoak lehenik. */
    List<StockLerroa> stockaGaur() throws DaoSalbuespena;
}
