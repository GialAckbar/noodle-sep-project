package shared;

import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import shared.utility.GsonUtility;

import java.util.HashSet;

public class LVKategorieElement {
    String anzeigename = "";
    int position = -1;
    int id = -1;

    private static final RuntimeTypeAdapterFactory<LVKategorieElement> adapter = RuntimeTypeAdapterFactory.of(LVKategorieElement.class);
    private static final HashSet<Class<?>> registeredClasses= new HashSet<Class<?>>();
    static {
        GsonUtility.registerType(adapter);
    }
    private synchronized void registerClass() {
        if (!registeredClasses.contains(this.getClass())) {
            registeredClasses.add(this.getClass());
            adapter.registerSubtype(this.getClass());
        }
    }

    public LVKategorieElement(String anzeigename) {
        registerClass();
        this.anzeigename = anzeigename;
    }
    public LVKategorieElement(String anzeigename, int position) {
        registerClass();
        this.anzeigename = anzeigename;
        this.position = position;
    }
    public LVKategorieElement(String anzeigename, String id) {
        registerClass();
        this.anzeigename = anzeigename;
        this.id = Integer.parseInt(id);
    }
    public LVKategorieElement(int id, String anzeigename, int position) {
        registerClass();
        this.id = id;
        this.anzeigename = anzeigename;
        this.position = position;
    }

    public LVKategorieElement(){

    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getAnzeigename() {
        return anzeigename;
    }
    public void setAnzeigename(String anzeigename) {
        this.anzeigename = anzeigename;
    }

    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
}
