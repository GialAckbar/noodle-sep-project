package shared;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.concurrent.ThreadLocalRandom;

public class ProfilDaten{

    String vorname;
    String nachname;
    String name;
    String email;
    String strasse;
    String hausnummer;
    int plz;
    String ort;
    int matrikelnummer;
    String studienfach;
    String lehrstuhl;
    String forschungsgebiet;
    boolean isTeacher;
    int id;
    Image profilbild = null;
    Color color;

    public ProfilDaten(String vorname, String nachname, String email, String strasse, String hausnummer, int plz, String ort, int matrikelnummer, String studienfach) {
        this.vorname = vorname;
        this.nachname = nachname;
        this.email = email;
        this.strasse = strasse;
        this.hausnummer = hausnummer;
        this.plz = plz;
        this.ort = ort;
        this.matrikelnummer = matrikelnummer;
        this.studienfach = studienfach;
        this.isTeacher = false;
    }
    public ProfilDaten(String vorname, String nachname, String email, String strasse, String hausnummer, int plz, String ort, String lehrstuhl, String forschungsgebiet){
        this.vorname = vorname;
        this.nachname = nachname;
        this.email = email;
        this.strasse = strasse;
        this.hausnummer = hausnummer;
        this.plz = plz;
        this.ort = ort;
        this.lehrstuhl = lehrstuhl;
        this.forschungsgebiet = forschungsgebiet;
        this.isTeacher= true;
    }
    public ProfilDaten(String name, int id, Image profilbild){
        this.name = name;
        this.id = id;
        this.profilbild = profilbild;
        this.color = getRandomRGB();
    }

    private Color getRandomRGB() {
        int rN1 = ThreadLocalRandom.current().nextInt(0,200+1);
        int rN2 = ThreadLocalRandom.current().nextInt(0,200+1);
        int rN3 = ThreadLocalRandom.current().nextInt(0,200+1);
        return Color.rgb(rN1, rN2, rN3);
    }

    public String getVorname() { return vorname; }

    public String getNachname() {
        return nachname;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getStrasse() {
        return strasse;
    }

    public String getHausnummer() {
        return hausnummer;
    }

    public int getPlz() {
        return plz;
    }

    public String getOrt() {
        return ort;
    }

    public int getMatrikelnummer() {
        return matrikelnummer;
    }

    public String getStudienfach() {
        return studienfach;
    }

    public String getLehrstuhl() {
        return lehrstuhl;
    }

    public String getForschungsgebiet() {
        return forschungsgebiet;
    }

    public boolean isTeacher() {
        return isTeacher;
    }

    public int getID() {
        return id;
    }

    public Image getProfilbild() {
        return profilbild;
    }

    public Color getColor() {
        return color;
    }

}
