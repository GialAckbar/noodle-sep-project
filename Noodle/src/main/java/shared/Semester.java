package shared;

public class Semester {
    private int jahr = -1;
    private Enums.SemesterTyp semesterTyp = null;

    public Semester (int jahr, Enums.SemesterTyp semesterTyp) {
        this.jahr = jahr;
        this.semesterTyp = semesterTyp;
    }

    public static boolean equals (Semester semester1, Semester semester2) {
        if (semester1.getJahr() == semester2.getJahr() && semester1.getSemesterTyp() == semester2.getSemesterTyp()) {
            return true;
        }
        return false;
    }
    public boolean equals (Semester otherSemester) {
        return Semester.equals(this, otherSemester);
    }

    public int getJahr() {
        return jahr;
    }

    public void setJahr(int jahr) {
        this.jahr = jahr;
    }

    public Enums.SemesterTyp getSemesterTyp() {
        return semesterTyp;
    }

    public void setSemesterTyp(Enums.SemesterTyp semesterTyp) {
        this.semesterTyp = semesterTyp;
    }
}
