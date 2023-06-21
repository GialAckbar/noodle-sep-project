package shared;

public class Profil {
    Enums.Current current1;
    Enums.Current current2;
    Boolean ich;
    Student student;
    Lehrende lehrende;
    User user;
    Boolean freund;
    Boolean inCourse;

    public Profil(Enums.Current current1, Enums.Current current2, Boolean ich, Student student, Lehrende lehrende){
        this.current1 = current1;
        this.current2 = current2;
        this.ich = ich;
        this.student = student;
        this.lehrende = lehrende;
    }

    public Profil(Enums.Current current1, Enums.Current current2, Boolean ich, Student student, Lehrende lehrende,Boolean inCourse){
        this.current1 = current1;
        this.current2 = current2;
        this.ich = ich;
        this.student = student;
        this.lehrende = lehrende;
        this.inCourse = inCourse;
    }

    public Profil(Enums.Current current1, Enums.Current current2, Boolean ich, Student student){
        this.student = student;
        this.current1 = current1;
        this.current2 = current2;
        this.ich = ich;
    }

    public Profil(Enums.Current current1, Enums.Current current2, Boolean ich, Student student, Boolean freund){
        this.student = student;
        this.current1 = current1;
        this.current2 = current2;
        this.ich = ich;
        this.freund = freund;
    }

    public Profil(Enums.Current current1, Enums.Current current2, Boolean ich, Lehrende lehrende){
        this.lehrende = lehrende;
        this.current1 = current1;
        this.current2 = current2;
        this.ich = ich;
    }
    public Profil(Enums.Current current1, Boolean ich, Student student){
        this.student = student;
        this.current1 = current1;
        this.ich = ich;
    }
    public Profil(Enums.Current current1, Boolean ich, Lehrende lehrende){
        this.lehrende = lehrende;
        this.current1 = current1;
        this.ich = ich;
    }

    public Boolean getInCourse() {
        return inCourse;
    }

    public void setInCourse(Boolean inCourse) {
        this.inCourse = inCourse;
    }

    public Enums.Current getCurrent1() {
        return current1;
    }

    public void setCurrent1(Enums.Current current1) {
        this.current1 = current1;
    }

    public Enums.Current getCurrent2() {
        return current2;
    }

    public void setCurrent2(Enums.Current current1) {
        this.current2 = current1;
    }

    public Boolean getIch() {
        return ich;
    }

    public void setIch(Boolean ich) {
        this.ich = ich;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Lehrende getLehrende() {
        return lehrende;
    }

    public void setLehrende(Lehrende lehrende) {
        this.lehrende = lehrende;
    }

    public Boolean getFreund() { return freund; }

    public void setFreund(Boolean freund) { this.freund = freund; }
}
