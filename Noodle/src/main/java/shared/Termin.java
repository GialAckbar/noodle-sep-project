package shared;


import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Termin {

    Lehrveranstaltung lv;
    ZonedDateTime frist;
    String titel;
    int v_id;

    public Termin(Lehrveranstaltung l, ZonedDateTime z, String titel, int id) {
        this.lv = l;
        this.frist = z;
        this.titel = titel;
        this.v_id = id;
    }

    public Lehrveranstaltung getLV() {
        return lv;
    }

    public ZonedDateTime getFrist() {
        return frist;
    }

    public String getTitel() { return titel; }

    public int getV_id() { return v_id; }

    public String getTimeInMinutes() {
        return String.valueOf(frist.getMinute()+toMinute(frist.getHour()));
    }
    public int toMinute(int Stunde) {
        return Stunde*60;
    }
    public String getDay() {
        frist = frist.withZoneSameInstant(ZoneId.systemDefault());

        return (frist.getDayOfMonth() < 10) ? "0" + frist.getDayOfMonth() : String.valueOf(frist.getDayOfMonth());
    }
    public String getMonth() {
        frist = frist.withZoneSameInstant(ZoneId.systemDefault());

        return (frist.getMonthValue() < 10) ? "0" + frist.getMonthValue() : String.valueOf(frist.getMonthValue());
    }
    public int getYear() {
        frist = frist.withZoneSameInstant(ZoneId.systemDefault());

        return frist.getYear();
    }
    public String getHour() {
        frist = frist.withZoneSameInstant(ZoneId.systemDefault());

        return (frist.getHour() < 10) ? "0" + frist.getHour() : String.valueOf(frist.getHour());
    }
    public String getMinute() {
        frist = frist.withZoneSameInstant(ZoneId.systemDefault());

        return (frist.getMinute() < 10) ? "0" + frist.getMinute() : String.valueOf(frist.getMinute());
    }
}
