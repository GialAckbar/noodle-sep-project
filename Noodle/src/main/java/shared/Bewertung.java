package shared;

import shared.quiz.Quizfrage;
import java.util.ArrayList;
import java.util.List;

public class Bewertung extends LVKategorieElement {

    List<Quizfrage> fragen = new ArrayList<>();
    int total, accomplished, passed;

    public Bewertung(String anzeigename) {
        super(anzeigename);
    }

    public Bewertung(int id, String anzeigename, int position) {
        super(id, anzeigename, position);
    }

    public Bewertung(int id, String anzeigename, List<Quizfrage> fragen, int total, int accomplished, int passed) {
        super(anzeigename, id);
        this.fragen = fragen;
        this.total = total;
        this.accomplished = accomplished;
        this.passed = passed;
    }

    public void addFrage(Quizfrage frage) {
        if (frage != null) {
            fragen.add(frage);
        }
    }

    public List<Quizfrage> getFragen() {
        return fragen;
    }

    public int getTotal() {
        return total;
    }

    public int getAccomplished() {
        return accomplished;
    }

    public int getPassed() {
        return passed;
    }
}
