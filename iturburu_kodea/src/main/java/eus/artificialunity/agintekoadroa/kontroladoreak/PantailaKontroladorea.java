package eus.artificialunity.agintekoadroa.kontroladoreak;

import eus.artificialunity.agintekoadroa.modeloa.AginteDatuak;

import java.util.function.Consumer;

/**
 * Pantaila guztien kontroladoreek betetzen duten kontratua: datu berriak iristean bista eguneratu.
 * Kontroladore nagusiak datuak behin kargatu eta pantaila guztiei banatzen dizkie.
 */
public interface PantailaKontroladorea {

    /** Datu berriak erakusten ditu (interfazearen haritik deitzen da). */
    void erakutsi(AginteDatuak datuak);

    /** Beste pantaila batera joateko aukera (adib. laburpeneko abisuetatik). */
    default void nabigazioaEzarri(Consumer<Pantaila> nabigatu) {
    }
}
