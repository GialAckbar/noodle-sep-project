package shared;

import java.util.ArrayList;
import java.util.List;



public class LehrveranstaltungsListe {


    List<Lehrveranstaltung> list;

    public LehrveranstaltungsListe(List<Lehrveranstaltung> list) {
        this.list = list;
    }
    public LehrveranstaltungsListe() {
        list = new ArrayList<>();
    }
    public List<Lehrveranstaltung> getList() {
        return list;
    }

    public void add(Lehrveranstaltung lehrveranstaltung){
        list.add(lehrveranstaltung);
    }

}
