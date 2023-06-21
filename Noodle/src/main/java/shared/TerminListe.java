package shared;

import java.util.ArrayList;
import java.util.List;



public class TerminListe {


    List<Termin> list;

    public TerminListe(List<Termin> list) {
        this.list = list;
    }
    public TerminListe() {
        list = new ArrayList<>();
    }
    public List<Termin> getList() {
        return list;
    }

    public void add(Termin t){
        list.add(t);
    }

}
