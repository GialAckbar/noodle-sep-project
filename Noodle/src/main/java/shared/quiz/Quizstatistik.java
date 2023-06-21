package shared.quiz;

import shared.Student;

import java.util.ArrayList;
import java.util.List;

public class Quizstatistik {

    List<Student> liste;
    int beteiligte;
    int total;
    int abgaben;
    int bestanden;

    public Quizstatistik(int beteiligte, int total, int bestanden, int abgaben) {
        this.liste=new ArrayList<>();
        this.beteiligte=beteiligte;
        this.total=total;
        this.abgaben=abgaben;
        this.bestanden=bestanden;
    }

    public List<Student> getList() {
        return liste;
    }

    public void add(Student student){
        liste.add(student);
    }

    public List<Student> getListe() {
        return liste;
    }

    public int getBeteiligte() {
        return beteiligte;
    }

    public int getTotal() {
        return total;
    }

    public int getAbgaben() {
        return abgaben;
    }

    public int getBestanden() {
        return bestanden;
    }
}
