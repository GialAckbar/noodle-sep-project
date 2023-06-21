package shared;

import java.io.File;

public class Lehrende extends User{

    String lehrstuhl;
    String forschungsgebiet;

    public Lehrende(int id){
        super(id);
    }

    public Lehrende(String vorname, String nachname, String email, Adresse adresse, String lehrstuhl, String forschungsgebiet) {
        super(vorname, nachname, email, adresse);
        this.lehrstuhl = lehrstuhl;
        this.forschungsgebiet = forschungsgebiet;
    }

    public Lehrende(String vorname, String nachname, String email, String strasse, String hausnummer, int plz, String ort, String lehrstuhl, String forschungsgebiet) {
        super(vorname, nachname, email, strasse, hausnummer, plz, ort);
        this.lehrstuhl = lehrstuhl;
        this.forschungsgebiet = forschungsgebiet;
    }

    public Lehrende(String vorname, String nachname, String email, Adresse adresse, String lehrstuhl, String forschungsgebiet, File image) {
        super(vorname, nachname, email, adresse, image);
        this.lehrstuhl = lehrstuhl;
        this.forschungsgebiet = forschungsgebiet;
    }

    public Lehrende(String vorname, String nachname, String email, String strasse, String hausnummer, int plz, String ort, String lehrstuhl, String forschungsgebiet, File image) {
        super(vorname, nachname, email, strasse, hausnummer, plz, ort, image);
        this.lehrstuhl = lehrstuhl;
        this.forschungsgebiet = forschungsgebiet;
    }

    public Lehrende(int id, String vorname, String nachname, String email, String strasse, String hausnummer, int plz, String ort, String lehrstuhl, String forschungsgebiet) {
        super(id, vorname, nachname, email, strasse, hausnummer, plz, ort);
        this.lehrstuhl = lehrstuhl;
        this.forschungsgebiet = forschungsgebiet;
    }


    public Lehrende(int id, String vorname, String nachname, String email, String strasse, String hausnummer, int plz, String ort, String lehrstuhl, String forschungsgebiet, int imageID) {
        super(id, vorname, nachname, email, strasse, hausnummer, plz, ort,imageID);
        this.lehrstuhl = lehrstuhl;
        this.forschungsgebiet = forschungsgebiet;
    }


    public Lehrende(String vorname, String nachname, String email, int id) {
        super(vorname, nachname, email);
        super.id = id;
    }

    public Lehrende(int id, String strasse, String hausnummer, int plz, String ort, String lehrstuhl,String forschungsgebiet, File fileImage) {
        super(id, strasse, hausnummer, plz, ort, fileImage);
        this.lehrstuhl = lehrstuhl;
        this.forschungsgebiet = forschungsgebiet;
    }

    public String getLehrstuhl(){
        return lehrstuhl;
    }

    public String getForschungsgebiet(){
        return forschungsgebiet;
    }

    public void setLehrstuhl(){
        this.lehrstuhl = lehrstuhl;
    }

    public void setForschungsgebiet(){
        this.forschungsgebiet = forschungsgebiet;
    }
}
