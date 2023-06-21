package shared;

public class Literaturverzeichnis {

    private int id;
    private String titel;
    private String autor;
    private String jahr;
    private int thid;
    private String art;

    public Literaturverzeichnis(int id, String titel, String autor, String jahr, String art, int thid){
        this.id = id;
        this.titel = titel;
        this.autor = autor;
        this.jahr = jahr;
        this.art = art;
        this.thid = thid;
    }
    public Literaturverzeichnis(String titel, String autor, String jahr, String art){
        this.titel = titel;
        this.autor = autor;
        this.jahr = jahr;
        this.art = art;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getJahr() {
        return jahr;
    }

    public void setJahr(String jahr) {
        this.jahr = jahr;
    }

    public int getThid() {
        return thid;
    }

    public void setThid(int thid) {
        this.thid = thid;
    }

    public String getArt() {
        return art;
    }

    public void setArt(String art) {
        this.art = art;
    }
}
