package shared.navigation;

import shared.Enums;
import shared.Lehrveranstaltung;
import shared.Reminder;
import shared.User;

import java.util.ArrayList;
import java.util.List;

public class NavigationInformation {

    private String vorname = null;
    private String nachname = null;
    protected int pbid = -1;

    protected User user = null;

    List<SemesterMitVeranstaltungen> semesterMitVeranstaltungen = new ArrayList<SemesterMitVeranstaltungen>();
    List<Reminder> reminder = new ArrayList<Reminder>();

    public NavigationInformation (String vorname, String nachname) {
        this.vorname = vorname;
        this.nachname = nachname;
    }
    public NavigationInformation (String vorname, String nachname, int pbid) {
        this.vorname = vorname;
        this.nachname = nachname;
        this.pbid = pbid;
    }
    public NavigationInformation (User user) {
        this.user = user;
    }
    public NavigationInformation (String vorname, String nachname, List<SemesterMitVeranstaltungen> semesterMitVeranstaltungen) {
        this.vorname = vorname;
        this.nachname = nachname;

        if (semesterMitVeranstaltungen != null) {
            this.semesterMitVeranstaltungen = semesterMitVeranstaltungen;
        }

    }



    public String getVorname() {
        return vorname;
    }
    public void setVorname(String vorname) {
        this.vorname = vorname;
    }
    public String getNachname() {
        return nachname;
    }
    public void setNachname(String nachname) {
        this.nachname = nachname;
    }

    public void setPbid(int id) {
        pbid = id;
    }
    public int getPbid() {
        return pbid;
    }

    public void setUser(User user) {
        this.user = user;
    }
    public User getUser() {
        return user;
    }
    public List<SemesterMitVeranstaltungen> getSemesterMitVeranstaltungen(){
        return semesterMitVeranstaltungen;
    }
    public void addSemesterMitLehrveranstaltungen (SemesterMitVeranstaltungen semesterMitVeranstaltungen) {
        this.semesterMitVeranstaltungen.add(semesterMitVeranstaltungen);
    }
    public boolean setSemesterMitLehrveranstaltungen (List<SemesterMitVeranstaltungen> semesterMitVeranstaltungen) {
        if (semesterMitVeranstaltungen != null) {
            this.semesterMitVeranstaltungen = semesterMitVeranstaltungen;
            return true;
        }
        return false;
    }
    public boolean deleteSemesterMitLehrveranstaltungen (SemesterMitVeranstaltungen semesterMitVeranstaltungen) {
        return this.semesterMitVeranstaltungen.remove(semesterMitVeranstaltungen);
    }
    public boolean deleteSemesterMitLehrveranstaltungen (int index) {
        return this.semesterMitVeranstaltungen.remove(index) != null;
    }
    public boolean deleteReminder (Reminder reminder) {
        return this.reminder.remove(reminder);
    }
    public boolean deleteReminder (int index) {
        return this.reminder.remove(index) != null;
    }
    public List<Reminder> getReminder(){
        return reminder;
    }
    public void addReminder (Reminder reminder) {
        this.reminder.add(reminder);
    }
    public boolean setReminder (List<Reminder> reminder) {
        if (reminder != null) {
            this.reminder = reminder;
            return true;
        }
        return false;
    }

}
