package shared;

import shared.LVKategorieElement;

public class Lernkarte extends LVKategorieElement {

    protected String antwort;

    public Lernkarte() {
        super();
    }

    public Lernkarte(String anzeigename) {
        super(anzeigename);
    }

    public Lernkarte(String anzeigename, int position) {
        super(anzeigename, position);
    }

    public Lernkarte(String anzeigename, String id) {
        super(anzeigename, id);
    }

    public Lernkarte(int id, String anzeigename, int position) {
        super(id, anzeigename, position);
    }

    public String getAntwort() {
        return antwort;
    }

    public void setAntwort(String antwort) {
        this.antwort = antwort;
    }
}
