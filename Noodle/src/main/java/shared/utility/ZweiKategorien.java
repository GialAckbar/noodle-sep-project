package shared.utility;

import shared.LVKategorie;

public class ZweiKategorien {
    LVKategorie first = null;
    LVKategorie second = null;

    public ZweiKategorien() {

    }
    public ZweiKategorien(LVKategorie first, LVKategorie second) {
        this.first = first;
        this.second = second;
    }

    public LVKategorie getFirst() {
        return first;
    }
    public void setFirst(LVKategorie first) {
        this.first = first;
    }
    public LVKategorie getSecond () {
        return second;
    }
    public void setSecond(LVKategorie second) {
        this.second = second;
    }
}
