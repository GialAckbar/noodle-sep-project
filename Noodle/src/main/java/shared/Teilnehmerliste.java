package shared;

import java.util.ArrayList;
import java.util.List;

public class Teilnehmerliste {

    String courseName = null;
    List<Student> studentenListe = new ArrayList<>();
    List<Lehrende> lehrendenListe = new ArrayList<>();
    boolean isTeacher = false;

    public Teilnehmerliste(List<Student> studentenListe, List<Lehrende> lehrendenListe) {
        this.studentenListe = studentenListe;
        this.lehrendenListe = lehrendenListe;
    }

    public Teilnehmerliste(List<Student> studentenListe, List<Lehrende> lehrendenListe, String courseName) {
        this.studentenListe = studentenListe;
        this.lehrendenListe = lehrendenListe;
        this.courseName = courseName;
    }

    public Teilnehmerliste(List<Student> studentenListe, List<Lehrende> lehrendenListe, String courseName, boolean isTeacher) {
        this.studentenListe = studentenListe;
        this.lehrendenListe = lehrendenListe;
        this.courseName = courseName;
        this.isTeacher = isTeacher;
    }

    public boolean getIsTeacher() {
        return isTeacher;
    }

    public List<Student> getStudentenListe() {
        return studentenListe;
    }

    public List<Lehrende> getLehrendenListe() {
        return lehrendenListe;
    }

    public String getCourseName() {
        return courseName;
    }

}
