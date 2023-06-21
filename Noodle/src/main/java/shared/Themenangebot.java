package shared;

public class Themenangebot {

    private String titel;
    private String beschreibung;
    private int id;

    public Themenangebot( int id, String titel, String beschreibung){
        this.id = id;
        this.titel = titel;
        this.beschreibung = beschreibung;
    }
    public Themenangebot(){

    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
