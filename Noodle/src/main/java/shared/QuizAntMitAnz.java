package shared;

import shared.quiz.QuizAntwort;


public class QuizAntMitAnz {
    QuizAntwort qA;
    int anzahl;

    public QuizAntMitAnz(QuizAntwort qa, int a) {
        this.qA = qa;
        this.anzahl = a;
    }

    public QuizAntwort getQA() {
        return qA;
    }

    public int getAnzahl(){
        return anzahl;
    }
    public void setAnzahl(int a) {
        anzahl = anzahl + a;
    }
}
