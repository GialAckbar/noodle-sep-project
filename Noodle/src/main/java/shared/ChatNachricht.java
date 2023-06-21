package shared;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ChatNachricht {

    String nachricht;
    ZonedDateTime zeitpunkt;
    int senderid;
    String name;
    boolean me;

    public ChatNachricht(String nachricht, ZonedDateTime zeitpunkt, int senderid, String vorname, String nachname, boolean me) {
        this.nachricht = nachricht;
        this.zeitpunkt = zeitpunkt;
        this.senderid = senderid;
        this.name = vorname + " " + nachname;
        this.me = me;
    }

    public String getNachricht() {
        return nachricht;
    }

    public String getZeitpunkt() {
        zeitpunkt.withZoneSameInstant(ZoneId.systemDefault());
        String day = (zeitpunkt.getDayOfMonth() < 10) ? "0" + zeitpunkt.getDayOfMonth() : String.valueOf(zeitpunkt.getDayOfMonth());
        String month = (zeitpunkt.getMonthValue() < 10) ? "0" + zeitpunkt.getMonthValue() : String.valueOf(zeitpunkt.getMonthValue());
        int year = zeitpunkt.getYear();
        String hour = (zeitpunkt.getHour() < 10) ? "0" + zeitpunkt.getHour() : String.valueOf(zeitpunkt.getHour());
        String minute = (zeitpunkt.getMinute() < 10) ? "0" + zeitpunkt.getMinute() : String.valueOf(zeitpunkt.getMinute());
        return day + "." + month + "." + year + " | " + hour + ":" + minute;
    }

    public int getSenderid() {
        return senderid;
    }

    public String getName() {
        return name;
    }

    public boolean isMe() {
        return me;
    }
}
