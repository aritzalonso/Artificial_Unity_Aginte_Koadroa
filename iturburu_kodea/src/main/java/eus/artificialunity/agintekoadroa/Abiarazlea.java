package eus.artificialunity.agintekoadroa;

import javafx.application.Application;

/**
 * .jar exekutagarriaren sarrera-puntua (java -jar aginte-koadroa.jar).
 * Application-en azpiklasea EZ denez, JavaFX klase-bidean kargatu daiteke modulu-biderik gabe.
 */
public final class Abiarazlea {

    private Abiarazlea() {
    }

    public static void main(String[] args) {
        Application.launch(AginteKoadroaApp.class, args);
    }
}
