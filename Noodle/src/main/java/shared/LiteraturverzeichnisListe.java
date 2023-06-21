package shared;

import java.util.ArrayList;
import java.util.List;

public class LiteraturverzeichnisListe {
    List<Literaturverzeichnis> list;

    public LiteraturverzeichnisListe(List<Literaturverzeichnis> list){this.list = list;}
    public LiteraturverzeichnisListe() {
        list = new ArrayList<>();
    }

    public List<Literaturverzeichnis> getList() {
        return list;
    }

    public void setList(List<Literaturverzeichnis> list) {
        this.list = list;
    }

    public void add(Literaturverzeichnis literaturverzeichnis){
        list.add(literaturverzeichnis);
    }
}
