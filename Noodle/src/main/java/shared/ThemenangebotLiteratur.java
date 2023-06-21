package shared;

public class ThemenangebotLiteratur {

    LiteraturverzeichnisListe liste;
    Themenangebot themenangebot;

    public ThemenangebotLiteratur(LiteraturverzeichnisListe liste, Themenangebot themenangebot){
        this.liste = liste;
        this.themenangebot = themenangebot;
    }

    public LiteraturverzeichnisListe getListe() {
        return liste;
    }

    public void setListe(LiteraturverzeichnisListe liste) {
        this.liste = liste;
    }

    public Themenangebot getThemenangebot() {
        return themenangebot;
    }

    public void setThemenangebot(Themenangebot themenangebot) {
        this.themenangebot = themenangebot;
    }
}
