package shared.quiz;

import java.util.ArrayList;
import java.util.List;

public class Quizfrage {

    String frage = "";
    List<QuizAntwort> antworten = new ArrayList<>();
    int frageid;

    public Quizfrage (String frage) {
        setFrage(frage);
    }

    public Quizfrage (String frage, List<QuizAntwort> antworten, int frageid) {
        setFrage(frage);
        setAntworten(antworten);
        this.frageid = frageid;
    }

    public void setFrage(String frage) {
        if (frage != null) {
            this.frage = frage;
        }
    }

    public String getFrage () {
        return frage;
    }

    public void setAntworten (List<QuizAntwort> antworten) {
        if (antworten != null) {
            this.antworten = antworten;
        }
    }

    public List<QuizAntwort> getAntworten() {
        return antworten;
    }

    public void addAntwort (QuizAntwort antwort) {
        if (antwort != null) {
            antworten.add(antwort);
        }
    }

    public int getFrageid() {
        return frageid;
    }
}
