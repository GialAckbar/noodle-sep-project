package shared.quiz;

import shared.LVKategorieElement;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.ZoneId;

public class Quiz extends LVKategorieElement {

    ZonedDateTime openDate = null;
    ZonedDateTime closeDate = null;
    int versuche = -1;
    List<Quizfrage> fragen = new ArrayList<>();

    public Quiz (String anzeigename) {
        super (anzeigename);
    }

    public Quiz (int id, String anzeigename, int position) {
        super (id, anzeigename, position);
    }

    public Quiz (int id, String anzeigename, ZonedDateTime openDate, ZonedDateTime closeDate, List<Quizfrage> fragen, int versuche) {
        super(anzeigename, Integer.toString(id));
        this.openDate = openDate;
        this.closeDate = closeDate;
        this.versuche = versuche;
        setFragen(fragen);
    }

    public ZonedDateTime getOpenDate () {
        return openDate;
    }

    public String getOpenDateString() {
        if(openDate == null) {
            return "";
        }
        openDate.withZoneSameInstant(ZoneId.systemDefault());
        String day = (openDate.getDayOfMonth() < 10) ? "0" + openDate.getDayOfMonth() : String.valueOf(openDate.getDayOfMonth());
        String month = (openDate.getMonthValue() < 10) ? "0" + openDate.getMonthValue() : String.valueOf(openDate.getMonthValue());
        int year = openDate.getYear();
        String hour = (openDate.getHour() < 10) ? "0" + openDate.getHour() : String.valueOf(openDate.getHour());
        String minute = (openDate.getMinute() < 10) ? "0" + openDate.getMinute() : String.valueOf(openDate.getMinute());
        return day + "." + month + "." + year + " | " + hour + ":" + minute;
    }

    public ZonedDateTime getCloseDate () {
        return closeDate;
    }

    public String getCloseDateString() {
        if(openDate == null) {
            return "";
        }
        closeDate.withZoneSameInstant(ZoneId.systemDefault());
        String day = (closeDate.getDayOfMonth() < 10) ? "0" + closeDate.getDayOfMonth() : String.valueOf(closeDate.getDayOfMonth());
        String month = (closeDate.getMonthValue() < 10) ? "0" + closeDate.getMonthValue() : String.valueOf(closeDate.getMonthValue());
        int year = closeDate.getYear();
        String hour = (closeDate.getHour() < 10) ? "0" + closeDate.getHour() : String.valueOf(closeDate.getHour());
        String minute = (closeDate.getMinute() < 10) ? "0" + closeDate.getMinute() : String.valueOf(closeDate.getMinute());
        return day + "." + month + "." + year + " | " + hour + ":" + minute;
    }

    public void setFragen (List<Quizfrage> fragen) {
        if (fragen != null) {
            this.fragen = fragen;
        }
    }

    public List<Quizfrage> getFragen () {
        return fragen;
    }

    public void addFrage (Quizfrage frage) {
        if (frage != null) {
            fragen.add(frage);
        }
    }

    public int getVersuche() {
        return versuche;
    }
}