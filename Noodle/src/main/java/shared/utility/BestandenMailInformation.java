package shared.utility;

import shared.Semester;

public class BestandenMailInformation {
    protected String veranstaltungsname = "";
    protected Semester semester = null;
    protected String mailAdresse = "";
    protected boolean bestanden = false;

    public BestandenMailInformation() {
    }

    public BestandenMailInformation(String veranstaltungsname, Semester semester, String mailAdresse, boolean bestanden) {
        this.veranstaltungsname = veranstaltungsname;
        this.semester = semester;
        this.mailAdresse = mailAdresse;
        this.bestanden = bestanden;
    }

    public String getVeranstaltungsname() {
        return veranstaltungsname;
    }

    public void setVeranstaltungsname(String veranstaltungsname) {
        this.veranstaltungsname = veranstaltungsname;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public String getMailAdresse() {
        return mailAdresse;
    }

    public void setMailAdresse(String mailAdresse) {
        this.mailAdresse = mailAdresse;
    }

    public boolean isBestanden() {
        return bestanden;
    }

    public void setBestanden(boolean bestanden) {
        this.bestanden = bestanden;
    }
}
