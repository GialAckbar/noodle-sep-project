package shared.utility;

import shared.Reminder;
import shared.User;

import java.util.ArrayList;
import java.util.List;

public class ReminderMitUsern {
    protected Reminder reminder = null;
    protected String veranstaltung = "";
    List<User> users = new ArrayList<>();

    public ReminderMitUsern() {

    }
    public ReminderMitUsern(Reminder reminder) {
        this.reminder = reminder;
    }

    public ReminderMitUsern(Reminder reminder, List<User> users) {
        this.reminder = reminder;
        this.users = users;
    }

    public ReminderMitUsern(Reminder reminder, String veranstaltung, List<User> users) {
        this.reminder = reminder;
        this.veranstaltung = veranstaltung;
        this.users = users;
    }

    public Reminder getReminder() {
        return reminder;
    }

    public void setReminder(Reminder reminder) {
        this.reminder = reminder;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        if (users != null) {
            this.users = users;
        }
    }

    public String getVeranstaltung() {
        return veranstaltung;
    }

    public void setVeranstaltung(String veranstaltung) {
        this.veranstaltung = veranstaltung;
    }
}
