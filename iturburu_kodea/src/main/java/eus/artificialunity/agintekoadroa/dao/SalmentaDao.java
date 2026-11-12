package eus.artificialunity.agintekoadroa.dao;

import eus.artificialunity.agintekoadroa.modeloa.BezeroSalmenta;
import eus.artificialunity.agintekoadroa.modeloa.DataTartea;
import eus.artificialunity.agintekoadroa.modeloa.HilekoSalmenta;
import eus.artificialunity.agintekoadroa.modeloa.KategoriaSalmenta;
import eus.artificialunity.agintekoadroa.modeloa.Laburpena;
import eus.artificialunity.agintekoadroa.modeloa.ProduktuSalmenta;

import java.util.List;

/**
 * Salmenten datuak (Odoo-ren salmenta-eskaera baieztatuak: sale_order.state = 'sale').
 * Data-tarte itxia: bi egunak barne. Zenbatekoak BEZik gabe.
 */
public interface SalmentaDao {

    /** KPI nagusiak aldian (eta kobratzeko zain dagoena gaur). */
    Laburpena laburpena(DataTartea tartea) throws DaoSalbuespena;

    /** Salmentak hileka; salmentarik gabeko hilabeteak 0 balioarekin. */
    List<HilekoSalmenta> hilekoSalmentak(DataTartea tartea) throws DaoSalbuespena;

    /** Gehien saldutako produktuak (unitateka), gehienez {@code muga}. */
    List<ProduktuSalmenta> produktuSalduenak(DataTartea tartea, int muga) throws DaoSalbuespena;

    /** Salmentak bezeroka, handienetik txikienera. */
    List<BezeroSalmenta> bezerokoSalmentak(DataTartea tartea) throws DaoSalbuespena;

    /** Salmentak produktu-kategoriaka, handienetik txikienera. */
    List<KategoriaSalmenta> kategoriakoSalmentak(DataTartea tartea) throws DaoSalbuespena;
}
