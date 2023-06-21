package shared.navigation;

import shared.Lehrveranstaltung;
import shared.Semester;

import java.util.ArrayList;
import java.util.List;

public class SemesterMitVeranstaltungen {

    private Semester semester = null;

    List<Lehrveranstaltung> lehrveranstaltungen = new ArrayList<Lehrveranstaltung>();

    public SemesterMitVeranstaltungen(Semester semester) {
        this.setSemester(semester);
    }
    public SemesterMitVeranstaltungen(Semester semester, List<Lehrveranstaltung> lehrveranstaltungen) {
        this.setSemester(semester);
        if (lehrveranstaltungen != null) {
            this.lehrveranstaltungen = lehrveranstaltungen;
        }
    }

    public Semester getSemester() {
        return semester;
    }
    public void setSemester(Semester semester) {
        this.semester = semester;
    }
    public List<Lehrveranstaltung> getLehrveranstaltungen () {
        return lehrveranstaltungen;
    }
    public void addLehrveranstaltung(Lehrveranstaltung lehrveranstaltung) {
        lehrveranstaltungen.add(lehrveranstaltung);
    }
    public boolean deleteLehrveranstaltung (Lehrveranstaltung lehrveranstaltung) {
        return lehrveranstaltungen.remove(lehrveranstaltung);
    }
    public boolean deleteLehrveranstaltung (int index) {
        return lehrveranstaltungen.remove(index) != null;
    }
}
