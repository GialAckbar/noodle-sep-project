package shared;

import java.util.ArrayList;
import java.util.List;

public class LVKategorie {
    private String name = null;
    private List<LVKategorieElement> kategorieElemente = new ArrayList<LVKategorieElement>();
    private int id = -1;
    protected int position = -1;

    public LVKategorie (String name) {
        this.name = name;
    }
    public LVKategorie (int id, String name) {
        this.name = name;
        this.id = id;
    }
    public LVKategorie (int id, String name, int position) {
        this.name = name;
        this.id = id;
        this.position = position;
    }
    public LVKategorie (String name, List<LVKategorieElement> kategorieElemente) {
        this.name = name;
        if (kategorieElemente != null) {
            this.kategorieElemente = kategorieElemente;
        }
    }
    public void add(LVKategorieElement element) {
        kategorieElemente.add(element);
    }
    public void addDatei (KategorieDatei datei) {
        kategorieElemente.add(datei);
    }
    public boolean removeDatei (KategorieDatei datei) {
        return kategorieElemente.remove(datei);
    }
    public boolean removeDatei (int index) {
        return kategorieElemente.remove(index) != null;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LVKategorieElement> getKategorieElemente() {
        return kategorieElemente;
    }

    public void setKategorieElemente(List<LVKategorieElement> kategorieElemente) {
        this.kategorieElemente = kategorieElemente;
    }
    public void setID (int id) {
        if (id >= -1) {
            this.id = id;
        }
    }
    public int getID () {
        return id;
    }
    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
}
