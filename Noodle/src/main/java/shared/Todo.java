package shared;

import java.util.ArrayList;
import java.util.List;

public class Todo extends LVKategorieElement {
    boolean isFinished = false;
    List<User> verantwortliche = new ArrayList<User>();

    public Todo (String anzeigename) {
        super (anzeigename);
    }
    public Todo (String anzeigename, int position) {
        super (anzeigename, position);
    }
    public Todo (int id, String anzeigename, int position) {
        super (id, anzeigename, position);
    }
    public Todo (String anzeigename, int position, boolean isFinished) {
        super (anzeigename, position);
        this.isFinished = isFinished;
    }
    public Todo (String anzeigename, int position, boolean isFinished, List<User> verantwortliche) {
        super (anzeigename, position);
        this.isFinished = isFinished;
        setVerantwortliche(verantwortliche);
    }
    public Todo (int id, String anzeigename, int position, boolean isFinished) {
        super (id, anzeigename, position);
        this.isFinished = isFinished;
    }
    public Todo (int id, String anzeigename, int position, boolean isFinished, List<User> verantwortliche) {
        super (id, anzeigename, position);
        this.isFinished = isFinished;
        setVerantwortliche(verantwortliche);
    }

    public void setIsFinished (boolean isFinished) {
        this.isFinished = isFinished;
    }
    public boolean getIsFinished () {
        return isFinished;
    }

    public void setVerantwortliche (List<User> verantwortliche) {
        if (verantwortliche != null) {
            this.verantwortliche = verantwortliche;
        }
    }
    public List<User> getVerantwortliche () {
        return verantwortliche;
    }
    public void addVerantwortlichen (User verantwortlicher) {
        verantwortliche.add(verantwortlicher);
    }
    public boolean removeVerantwortlichen (User verantwortlicher) {
        return verantwortliche.remove(verantwortlicher);
    }
    public boolean removeVerantwortlichen (int index) {
        return verantwortliche.remove(index) != null;
    }
}
