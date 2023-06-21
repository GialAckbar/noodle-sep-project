package client;

import client.Controller.kurs.KursHinzufügenRequest;
import client.urlconnections.ProfilHinzufügenRequest;
import com.google.gson.Gson;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import server.handler.ProfilHinzufügenHandler;
import shared.*;
import shared.accounts.UseridMitToken;

import java.util.ArrayList;
import java.util.List;

public class Launcher extends Application {
    public static GUI gui;
    public static RequestHandler requestHandler;
    public static UseridMitToken useridMitToken = null;
    public static User user = null;
    public static Enums.Current userArt = Enums.Current.STUDENT;
    public static void main(String[] args) {
        System.out.println("Client");
        launch(args);
    }

    private static void uploadTest() {

        Lehrveranstaltung lv = new Lehrveranstaltung("Neue Lehrveranstaltung", new Semester(2021, Enums.SemesterTyp.SS), Enums.Art.VORLESUNG);

        List<LVKategorie> kategorien = new ArrayList<LVKategorie>();

        LVKategorie kategorie1 = new LVKategorie("Neue Kategorie 1");
        kategorie1.addDatei(new KategorieDatei("Lehrveranstaltung 1 Kategorie 1 Datei 1", "ER-Diagramm_Datenbank.png", "./../"));
        kategorie1.addDatei(new KategorieDatei("Lehrveranstaltung 1 Kategorie 1 Datei 2", "schema.sql", "./../"));

        LVKategorie kategorie2 = new LVKategorie("Neue Kategorie 2");
        kategorie2.addDatei(new KategorieDatei("Lehrveranstaltung 1 Kategorie 2 Datei 1", "README.md", "./../"));
//        kategorie2.addDatei(new KategorieDatei("Lehrveranstaltung 1 Kategorie 2 Datei 2", "", "./../", new File("./../schema.sql")));

        kategorien.add(kategorie1);
        kategorien.add(kategorie2);

        UseridMitToken useridMitToken = new UseridMitToken(22, "myToken");

        lv.setKategorien(kategorien);

        int courseID = KursHinzufügenRequest.createCourse(lv, useridMitToken);
        System.out.println(courseID);

    }
    private void addProfileTest() {
        Lehrende lehrkraft = new Lehrende("Andreas", "Lemken", "dasxyxyxsdsnbfdgads@gmail.com", "Zur Straße",
                "1", 12345, "Duisburg", "MyLehrstuhl", "MyForschungsgebiet");
        Student student = new Student("Andreas", "Lemken", "allsdsadsadssdnbdsdsdsadsesidioten@gmail.com", "Zur Straße",
                "1", 12345, "Duisburg", -1, "MyForschungsgebiet");
        //lehrkraft.setImage(new File("./../schema.sql"));
        //student.setImage(new File("./../schema.sql"));
        int response = ProfilHinzufügenRequest.addProfile(lehrkraft, "Passwort");
        System.out.println(response);
    }

    @Override
    public void start(Stage stage){


        requestHandler = new RequestHandler("127.0.0.1", 1337);
        gui = new GUI(stage);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Montserrat-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Montserrat-Light.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Montserrat-Thin.ttf"),14);
        gui.changeState("login");
        //addProfileTest();
        //uploadTest();

//        System.out.println(((Student)requestHandler.request(RequestHandler.Art.GET, "/testRequest", Student.class).getElement()).getMatrikelnummer());

//33333@e.de
//        useridMitToken = new UseridMitToken(22,"hallo");
//        try {
//            Response response = requestHandler.request(RequestHandler.Art.GET, "/navigation", NavigationInformation.class);
//            System.out.println(response.getStatusCode());
//            System.out.println(((NavigationInformation)response.getElement()).getNachname());
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }


    }

}
