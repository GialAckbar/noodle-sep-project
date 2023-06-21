package shared.quiz;

public class QuizAntwort {

    String antwort = "";
    boolean richtig = false;
    int id = 0;

    public QuizAntwort (String antwort) {
        setAntwort(antwort);
    }
    public QuizAntwort (String antwort, int id) {
        setAntwort(antwort);
        this.id = id;
    }

    public QuizAntwort (String antwort, boolean richtig, int id) {
        setAntwort(antwort);
        this.richtig = richtig;
        this.id = id;
    }

    public void setAntwort (String antwort) {
        if (antwort != null) {
            this.antwort = antwort;
        }
    }

    public String getAntwort () {
        return antwort;
    }

    public void setRichtig (boolean richtig) {
        this.richtig = richtig;
    }

    public boolean getRichtig () {
        return richtig;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
