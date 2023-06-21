package shared;


import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import shared.utility.GsonUtility;

import java.io.File;
import java.util.HashSet;

public class User {

    int id = -1;
    String vorname;
    String nachname;
    String email;
    Adresse adresse;
    File image = null;
    int imageID = -1;

    private static final RuntimeTypeAdapterFactory<User> adapter = RuntimeTypeAdapterFactory.of(User.class);
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

    public User(int id){
        this.id = id;
    }

    public User(String vorname, String nachname, String email, Adresse adresse){
        registerClass();
        this.vorname = vorname;
        this.nachname = nachname;
        this.email = email;
        this.adresse = adresse;
    }
    public User(String vorname, String nachname, String email, String strasse, String hausnummer, int plz, String ort){
        registerClass();
        this.vorname = vorname;
        this.nachname = nachname;
        this.email = email;
        this.adresse = new Adresse(strasse,hausnummer,plz,ort);
    }

    public User(String vorname, String nachname, String email, Adresse adresse, File image){
        registerClass();
        this.vorname = vorname;
        this.nachname = nachname;
        this.email = email;
        this.adresse = adresse;
        this.image = image;
    }
    public User(String vorname, String nachname, String email, String strasse, String hausnummer, int plz, String ort, File image){
        registerClass();
        this.vorname = vorname;
        this.nachname = nachname;
        this.email = email;
        this.adresse = new Adresse(strasse,hausnummer,plz,ort);
        this.image = image;
    }

    public User(int id, String vorname, String nachname, String email, String strasse, String hausnummer, int plz, String ort, File image){
        registerClass();
        this.vorname = vorname;
        this.nachname = nachname;
        this.email = email;
        this.adresse = new Adresse(strasse,hausnummer,plz,ort);
        this.image = image;
        this.id = id;
    }

    public User(int id, String vorname, String nachname, String email, String strasse, String hausnummer, int plz, String ort){
        registerClass();
        this.id = id;
        this.vorname = vorname;
        this.nachname = nachname;
        this.email = email;
        this.adresse = new Adresse(strasse,hausnummer,plz,ort);
    }

    public User(int id, String strasse, String hausnummer, int plz, String ort, File image) {
        registerClass();
        this.id = id;
        this.adresse = new Adresse(strasse, hausnummer, plz, ort);
        this.image = image;
    }

    public User(int id, String vorname, String nachname, String email, String strasse, String hausnummer, int plz, String ort, int imageID) {
        registerClass();
        this.imageID = imageID;
        this.id = id;
        this.vorname = vorname;
        this.nachname = nachname;
        this.email = email;
        this.adresse = new Adresse(strasse, hausnummer, plz, ort);
    }

    public User(String vorname, String nachname, String email){
        registerClass();
        this.vorname = vorname;
        this.nachname = nachname;
        this.email = email;
    }

    public User(int id, String vorname, String nachname, String email, Adresse adresse){
        registerClass();
        this.id = id;
        this.vorname = vorname;
        this.nachname = nachname;
        this.email = email;
        this.adresse = adresse;
    }
    public User() {

    }

    public int getImageID() {
        return imageID;
    }

    public void setImageID(int imageID) {
        this.imageID = imageID;
    }

    public Adresse getAdresse() {
        return adresse;
    }

    public void setAdresse(Adresse adresse){
        this.adresse = adresse;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVorname(){
        return vorname;
    }
    public void setVorname(String vorname) {
        if (vorname != null) {
            this.vorname = vorname;
        }
    }

    public String getNachname() {
        return nachname;
    }
    public void setNachname(String nachname) {
        if (nachname != null) {
            this.nachname = nachname;
        }
    }

    public String getEmail(){
        return email;
    }

    public int getID() {
        return id;
    }

    public File getImage() {
        return image;
    }
    public void setImage(File file) {
        image = file;
    }

}
