package shared.utility;

import shared.LVKategorieElement;

public class ZweiElementeMitKategorieId {
    int kategorieid = -1;
    LVKategorieElement first = null;
    LVKategorieElement second = null;

    public ZweiElementeMitKategorieId() {

    }
    public ZweiElementeMitKategorieId(LVKategorieElement first, LVKategorieElement second,  int id) {
        this.first = first;
        this.second = second;
        this.kategorieid = id;
    }
    public LVKategorieElement getFirst() {
        return first;
    }
    public void setFirst(LVKategorieElement first) {
        this.first = first;
    }
    public LVKategorieElement getSecond() {
        return second;
    }
    public void setSecond(LVKategorieElement second) {
        this.second = second;
    }
    public void setKategorieid(int id) {
        this.kategorieid = id;
    }
    public int getKategorieid() {
        return kategorieid;
    }
}
