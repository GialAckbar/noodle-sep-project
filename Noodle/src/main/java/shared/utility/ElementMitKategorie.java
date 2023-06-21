package shared.utility;

import shared.LVKategorie;
import shared.LVKategorieElement;

public class ElementMitKategorie {
    LVKategorie kategorie = null;
    LVKategorieElement element = null;

    public ElementMitKategorie() {

    }
    public ElementMitKategorie(LVKategorieElement element, LVKategorie kategorie) {
        this.element = element;
        this.kategorie = kategorie;
    }
    public void setElement(LVKategorieElement element) {
        this.element = element;
    }
    public LVKategorieElement getElement() {
        return this.element;
    }
    public void setKategorie(LVKategorie kategorie) {
        this.kategorie = kategorie;
    }
    public LVKategorie getKategorie () {
        return kategorie;
    }
}