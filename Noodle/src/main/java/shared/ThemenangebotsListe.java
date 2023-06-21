package shared;

import java.util.ArrayList;
import java.util.List;

public class ThemenangebotsListe {

    List<Themenangebot> list;

    public ThemenangebotsListe(List<Themenangebot> list){this.list = list;}
    public ThemenangebotsListe() {
        list = new ArrayList<>();
    }

    public List<Themenangebot> getList() {
        return list;
    }

    public void setList(List<Themenangebot> list) {
        this.list = list;
    }

    public void add(Themenangebot themenangebot){
        list.add(themenangebot);
    }
}
