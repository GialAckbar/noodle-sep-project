package shared;

import java.io.File;

public class Student extends User {

    int matrikelnummer;
    String studienfach;
    int versuche;

    public Student(String vorname, String nachname, String email, Adresse adresse, int matrikelnummer, String studienfach) {
        super(vorname, nachname, email, adresse);
        this.matrikelnummer = matrikelnummer;
        this.studienfach = studienfach;
    }

    public Student(String vorname, String nachname, String email, String strasse, String hausnummer, int plz, String ort, int matrikelnummer, String studienfach) {
        super(vorname, nachname, email, strasse, hausnummer, plz, ort);
        this.matrikelnummer = matrikelnummer;
        this.studienfach = studienfach;
    }

    public Student(String vorname, String nachname, String email, Adresse adresse, int matrikelnummer, String studienfach, File image) {
        super(vorname, nachname, email, adresse, image);
        this.matrikelnummer = matrikelnummer;
        this.studienfach = studienfach;
    }

    public Student(int id, String vorname, String nachname, String email, String strasse, String hausnummer, int plz, String ort, int matrikelnummer, String studienfach, File image) {
        super(id, vorname, nachname, email, strasse, hausnummer, plz, ort, image);
        this.matrikelnummer = matrikelnummer;
        this.studienfach = studienfach;
    }

    public Student(int id, String vorname, String nachname, String email, String strasse, String hausnummer, int plz, String ort, int matrikelnummer, String studienfach) {
        super(id, vorname, nachname, email, strasse, hausnummer, plz, ort);
        this.matrikelnummer = matrikelnummer;
        this.studienfach = studienfach;
    }

    public Student(int id, String vorname, String nachname, String email, String strasse, String hausnummer, int plz, String ort, int matrikelnummer, String studienfach, int imageID) {
        super(id, vorname, nachname, email, strasse, hausnummer, plz, ort,imageID);
        this.matrikelnummer = matrikelnummer;
        this.studienfach = studienfach;
    }
    public Student(String vorname, String nachname, String email, String studienfach){
        super(vorname, nachname, email);
        this.studienfach = studienfach;
    }

    public Student(String vorname, String nachname, String email, int matrikelnummer, int id) {
        super(vorname, nachname, email);
        super.id = id;
        this.matrikelnummer = matrikelnummer;
    }

    public Student(String vorname, String nachname, String email, String strasse, String hausnummer, int plz, String ort, int matrikelnummer, String studienfach, File image) {
        super(vorname, nachname, email, strasse, hausnummer, plz, ort, image);
        this.studienfach = studienfach;
        this.matrikelnummer = matrikelnummer;
    }

    public Student(int id, String strasse, String hausnummer, int plz, String ort, String studienfach, File fileImage) {
        super(id, strasse, hausnummer, plz, ort, fileImage);
        this.studienfach = studienfach;
    }

    public Student(String vorname, String nachname, String email, int matrikelnummer, int id, int versuche) {
        super(vorname, nachname, email);
        super.id = id;
        this.matrikelnummer = matrikelnummer;
        this.versuche = versuche;
    }
    public Student(int id, int versuche) {
        super.id = id;
        this.versuche = versuche;
    }

    public Student(int id) {
        super(id);
    }

    public Integer getMatrikelnummer(){
        return matrikelnummer;
    }

    public void setMatrikelnummer(int matrikelnummer) {
        this.matrikelnummer = matrikelnummer;
    }

    public String getStudienfach(){
        return studienfach;
    }

    public void setStudienfach(String studienfach){
        this.studienfach = studienfach;
    }

    public int getVersuche() {
        return versuche;
    }
    public void setVersuche(int versuche) {
        this.versuche = versuche;
    }
}
