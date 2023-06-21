package shared;

public class Adresse {

    String strasse;
    String hausnummer;
    int plz;
    String ort;


    public Adresse(String strasse, String hausnummer, int plz, String ort){
        this.strasse = strasse;
        this.hausnummer = hausnummer;
        this.plz = plz;
        this.ort = ort;
    }

    public String getStrasse(){
        return strasse;
    }

    public String getHausnummer(){
        return hausnummer;
    }

    public int getPlz(){
        return plz;
    }

    public String getOrt(){
        return ort;
    }

    public void setStrasse(String strasse){
        this.strasse = strasse;
    }

    public void setHausnummer(String hausnummer){
        this.hausnummer = hausnummer;
    }

    public void setPlz(int plz){
        this.plz = plz;
    }

    public void setOrt(String ort){
        this.ort = ort;
    }
}
