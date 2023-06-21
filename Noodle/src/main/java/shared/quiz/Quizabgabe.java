package shared.quiz;

import java.util.HashMap;
import java.util.List;

public class Quizabgabe {

    HashMap<Integer, List<Integer>> selected;
    boolean bestanden;
    int gesamtpunktzahl;
    int erreichtepunktzahl;

    public Quizabgabe(HashMap<Integer, List<Integer>> selected, boolean bestanden, int gesamtpunktzahl, int erreichtepunktzahl) {
        this.selected = selected;
        this.bestanden = bestanden;
        this.gesamtpunktzahl = gesamtpunktzahl;
        this.erreichtepunktzahl = erreichtepunktzahl;
    }

    public HashMap<Integer, List<Integer>> getSelected() {
        return selected;
    }

    public boolean isBestanden() {
        return bestanden;
    }

    public int getGesamtpunktzahl() {
        return gesamtpunktzahl;
    }

    public int getErreichtepunktzahl() {
        return erreichtepunktzahl;
    }
}