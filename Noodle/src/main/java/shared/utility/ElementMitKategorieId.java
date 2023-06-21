package shared.utility;

import shared.LVKategorieElement;

public class ElementMitKategorieId {
    int kategorieid = -1;
    LVKategorieElement element = null;

    public ElementMitKategorieId() {

    }
    public ElementMitKategorieId(LVKategorieElement element, int id) {
        this.element = element;
        this.kategorieid = id;
    }
    public void setElement(LVKategorieElement element) {
        this.element = element;
    }
    public LVKategorieElement getElement() {
        return this.element;
    }
    public void setKategorieid(int id) {
        this.kategorieid = id;
    }
    public int getKategorieid() {
        return kategorieid;
    }
}
