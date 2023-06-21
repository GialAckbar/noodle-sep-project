package shared.utility;

import shared.Lehrveranstaltung;

public class VeranstaltungMitAnzahl {
    Lehrveranstaltung lehrveranstaltung = null;
    int anzahl = -1;

    public VeranstaltungMitAnzahl() {
    }

    public VeranstaltungMitAnzahl(Lehrveranstaltung lehrveranstaltung, int anzahl) {
        this.lehrveranstaltung = lehrveranstaltung;
        this.anzahl = anzahl;
    }

    public Lehrveranstaltung getLehrveranstaltung() {
        return lehrveranstaltung;
    }

    public void setLehrveranstaltung(Lehrveranstaltung lehrveranstaltung) {
        this.lehrveranstaltung = lehrveranstaltung;
    }

    public int getAnzahl() {
        return anzahl;
    }

    public void setAnzahl(int anzahl) {
        this.anzahl = anzahl;
    }
}
