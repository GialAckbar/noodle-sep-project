package shared;

import java.util.ArrayList;
import java.util.List;

public class FreundesListe {
    List<Student> liste;
    public FreundesListe(List<Student> liste){this.liste=liste; }
    public FreundesListe() {
        liste = new ArrayList<>();
    }
    public List<Student> getList() {
        return liste;
    }
    public void add(Student student){
        liste.add(student);
    }
}
