package client;


import client.Controller.Chat;
import client.Controller.ChatListeStarted;
import client.Controller.Navigation;
import client.Controller.QuizStudentFragen;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import shared.Enums;

import java.net.URL;
import java.util.Collections;
import java.util.Map;

public class GUI {

    Stage stage;
    Parent root;

    public Group navigationGroup;
    public Navigation navigation;

    String currentState;

    public GUI(Stage stage){
        this.stage = stage;
        stage.setResizable(false);
    }

    private Map<String,String> fxml = Map.ofEntries(
            Map.entry("login","/fxml/login.fxml"),
            Map.entry("register","/fxml/register.fxml"),
            Map.entry("navigation","/fxml/navigation.fxml"),
            Map.entry("navigationLehrkraft","/fxml/navigationLehrkraft.fxml"),
            Map.entry("kursteilnehmer","/fxml/kursteilnehmer.fxml"),
            Map.entry("kursteilnehmer_add","/fxml/kursteilnehmer_add.fxml"),
            Map.entry("profilbearbeiten","/fxml/ProfilBearbeiten.fxml"),
            Map.entry("eigenesprofil","/fxml/EigenesProfil2.fxml"),
            Map.entry("courseList", "/fxml/courseList.fxml"),
            Map.entry("engagedCourseList", "/fxml/engagedCourseList.fxml"),
            Map.entry("registriertLehrkraft", "/fxml/registriertLehrkraft.fxml"),
            Map.entry("registriertStudent", "/fxml/registriertStudent.fxml"),
            Map.entry("twoFactor", "/fxml/2factorauth.fxml"),
            Map.entry("freundeUebersicht", "/fxml/freundesListe_Freunde.fxml"),
            Map.entry("anfrageUebersicht", "/fxml/freundesListe_Anfrage.fxml"),
            Map.entry("veranstaltungen","/fxml/Veranstaltungen_Profil.fxml"),
            Map.entry("themenangebote","/fxml/Themenangebote_Profil.fxml"),
            Map.entry("themaHinzufügen","/fxml/ThemaHinzufügen.fxml"),
            Map.entry("themaAnzeigen","/fxml/ThemaAnzeigen.fxml"),
            Map.entry("eignesProfilVeranstaltungen","/fxml/EigenesProfil_Veranstaltung.fxml")


            );
    public void changeState(String stateName) {
        System.out.println("changeState " + stateName);
        currentState = stateName;
        if(stateName == "navigation" && Launcher.userArt == Enums.Current.LEHRKRAFT){

            currentState = "navigationLehrkraft";
        }
        URL fxmlURL = getClass().getResource(fxml.get(currentState));

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlURL);
        try{
            root = fxmlLoader.load();
        }catch (Exception e){
            System.err.println("Error loading fxml");
            e.printStackTrace();
        }
        if(currentState == "navigation" || currentState == "navigationLehrkraft"){
            navigation = fxmlLoader.getController();
        }

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/design.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public String getCurrentState(){
        return currentState;
    }

    public <T> T addToNavigation(String fxml){
        navigationGroup.getChildren().clear();
        if(navigation != null){
            navigation.reloadNavigation();
        }
        Chat.timer = false; // Timer vom Chat stoppen
        ChatListeStarted.timer = false; // Timer von Chatliste stoppen
        QuizStudentFragen.timer = false; // Timer vom Quiz stoppen
        return addElement(navigationGroup, fxml);
    }

    public <T> T addOverNavigation(String fxml){
        if(navigation != null){
            navigation.reloadNavigation();
        }
        return addElement(navigationGroup, fxml);
    }

    public void removeOverNavigation(){
        if(navigationGroup.getChildren().size() == 1) return;
        navigationGroup.getChildren().remove(navigationGroup.getChildren().size()-1);
    }

    public void removeAllOverNavigation(){
        while(navigationGroup.getChildren().size() > 1){
            removeOverNavigation();
        }
    }


    public <T> T addPopup(){
        if(navigation == null) return null;
        return addElement(navigation.placeholderPopup, "/fxml/popup.fxml");
    }

    public <T> T addPopupQuiz(){
        if(navigation == null) return null;
        return addElement(navigation.placeholderPopup, "/fxml/popupQuiz.fxml");
    }

    public <T> T addPopupBewertung(){
        if(navigation == null) return null;
        return addElement(navigation.placeholderPopup, "/fxml/popupBewertung.fxml");
    }

    public void removePopup(){
        navigation.placeholderPopup.getChildren().remove(navigation.placeholderPopup.getChildren().size()-1);
    }

    public void removePopups(){
        navigation.placeholderPopup.getChildren().clear();
    }



    public <T> T addElement(Group placeholder, String fxml){
        URL fxmlURL = getClass().getResource(fxml);
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlURL);
        try {
            placeholder.getChildren().add(fxmlLoader.load());
        }catch (Exception e){
            e.printStackTrace();
        }
        T controller = fxmlLoader.getController();

        return controller;
    }

    public <T> T addElement(VBox placeholder, String fxml){
        URL fxmlURL = getClass().getResource(fxml);
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlURL);
        try {
            placeholder.getChildren().add(fxmlLoader.load());
        }catch (Exception e){
            e.printStackTrace();
        }
        T controller = fxmlLoader.getController();

        return controller;
    }


    public void moveElementUp(VBox parent,VBox element){
        ObservableList<Node> list = FXCollections.observableArrayList(parent.getChildren());
        if(list.size() <= 1){
            return;
        }
        int index = list.indexOf(element);
        if(index == 0) return;
        Collections.swap(list,(index-1),index);
        parent.getChildren().setAll(list);
    }

    public void moveElementDown(VBox parent,VBox element){
        ObservableList<Node> list = FXCollections.observableArrayList(parent.getChildren());
        if(list.size() <= 1){
            return;
        }
        int index = list.indexOf(element);
        if(list.size()-1 == index) return;
        Collections.swap(list,index,index+1);
        parent.getChildren().setAll(list);
    }


    public void clearElement(Group placeholder){
        placeholder.getChildren().clear();
    }
}
