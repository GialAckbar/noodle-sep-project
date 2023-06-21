package shared;

import shared.quiz.Quizfrage;
import java.util.ArrayList;
import java.util.List;


public class Feedback {
    List<Quizfrage> fragen;
    List<QuizAntMitAnz> abgabenFailed;
    List<QuizAntMitAnz> abgabenPassed;
    String title;


    public Feedback(List<Quizfrage> fr, List<QuizAntMitAnz> f, List<QuizAntMitAnz> p, String t) {
        this.fragen = fr;
        this.abgabenFailed = f;
        this.abgabenPassed = p;
        this.title = t;
    }

    public void addAntF(QuizAntMitAnz a) {
        abgabenFailed.add(a);
    }
    public void addAntP(QuizAntMitAnz a) {
        abgabenPassed.add(a);
    }
    public List<QuizAntMitAnz> getAntFailed() {
        return abgabenFailed;
    }
    public List<QuizAntMitAnz> getAntPassed() {
        return abgabenPassed;
    }
    public List<Quizfrage> getFragen() {
        return fragen;
    }
    public String getTitle() {
        return title;
    }

}
