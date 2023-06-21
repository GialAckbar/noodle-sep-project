package shared.accounts;

import shared.Student;

public class StudentMitPasswort {
    private Student student = null;
    private String passwort = "";

    public StudentMitPasswort(Student student, String passwort) {
        this.setStudent(student);
        this.setPasswort(passwort);
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getPasswort() {
        return passwort;
    }

    public void setPasswort(String passwort) {
        this.passwort = passwort;
    }
}
