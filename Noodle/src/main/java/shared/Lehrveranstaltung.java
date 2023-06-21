package shared;

import client.Launcher;

import java.util.ArrayList;
import java.util.List;

public class Lehrveranstaltung {


    String titel;
    Semester semester;
    List<User> teilnehmer = new ArrayList<>();
    Enums.Art art;
    boolean belegt = false;
    String vorname;
    String nachname;

    int veranstaltungsid =-1;

    List<LVKategorie> kategorien = new ArrayList<LVKategorie>();

    public Lehrveranstaltung(String titel, Semester semester, Enums.Art art){
        this.titel = titel;
        this.semester = semester;
        this.art = art;
    }

    public Lehrveranstaltung(String titel, Semester semester, Enums.Art art, int veranstaltungsid) {
        this.titel = titel;
        this.semester = semester;
        this.art = art;
        this.veranstaltungsid = veranstaltungsid;
    }

    public Lehrveranstaltung(String titel, Semester semester, Enums.Art art, Lehrende pUser){
        this.titel = titel;
        this.semester = semester;
        this.art = art;
        this.teilnehmer.add(pUser);

        /*
            Der Ersteller muss noch als teilnehmer hinzugefügt werden
         */
    }
    public Lehrveranstaltung(String titel, Semester semester, Enums.Art art, int veranstaltungsid, boolean belegt) {
        this.titel = titel;
        this.semester = semester;
        this.art = art;
        this.veranstaltungsid = veranstaltungsid;
        this.belegt = belegt;
    }
    public Lehrveranstaltung(String titel, Semester semester, Enums.Art art, int veranstaltungsid, boolean belegt, String vorname, String nachname) {
        this.titel = titel;
        this.semester = semester;
        this.art = art;
        this.veranstaltungsid = veranstaltungsid;
        this.belegt = belegt;
        this.vorname = vorname;
        this.nachname = nachname;
    }

    public void addTeilnehmer(User pUser){
        teilnehmer.add(pUser);
    }
    public void addTeilnehmerList(List<User> pUser){
        if(pUser == null){
            return;
        }
        for(User user : pUser){
            teilnehmer.add(user);
        }
    }

    public String getTitel(){
        return titel;
    }

    public Semester getSemester(){
        return semester;
    }

    public List<User> getTeilnehmer(){
        return teilnehmer;
    }

    public Enums.Art getArt(){
        return art;
    }

    public void setVeranstaltungsID (int id) {
        veranstaltungsid = id;
    }

    public int getVeranstaltungsID () {
        return veranstaltungsid;
    }

    public boolean getBelegung() {
        return belegt;
    }

    public void setBelegung(boolean bel)  { belegt = bel; }

    public String getNachname() {
        return nachname;
    }

    public String getVorname() {
        return vorname;
    }

    public List<LVKategorie> getKategorien () {
        return kategorien;
    }
    public void setKategorien (List<LVKategorie> kategorien) {
        if (kategorien != null) {
            this.kategorien = kategorien;
        }
    }
    public void insertKategorie (LVKategorie kategorie) {
        kategorien.add(kategorie);
    }
    public boolean removeKategorie (LVKategorie kategorie) {
        return kategorien.remove(kategorie);
    }
    public boolean removeKategorie (int index) {
        return kategorien.remove(index) != null;
    }
}
