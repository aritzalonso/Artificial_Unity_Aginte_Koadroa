package eus.artificialunity.agintekoadroa.dao;

import eus.artificialunity.agintekoadroa.modeloa.FakturaZain;

import java.util.List;

/** Bezeroen fakturak. */
public interface FakturaDao {

    /** Baliozkotutako bezero-fakturak, ordaindu gabe edo zati batean ordainduta; muga-egunaren arabera. */
    List<FakturaZain> kobratzekoZain() throws DaoSalbuespena;
}
