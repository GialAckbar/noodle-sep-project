package client.Controller;

import client.Controller.kurs.KursHinzufügenRequest;
import client.Launcher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import shared.*;
import java.util.ArrayList;
import java.util.List;


public class KursHinzufuegen {

    @FXML
    TextField titel;

    @FXML
    Text siteTitel;

    @FXML
    ComboBox typ;

    @FXML
    ComboBox semester;

    @FXML
    TextField jahr;

    @FXML
    VBox placeholder;

    @FXML
    Button add;

    @FXML
    Button back;

    @FXML
    Button create;

    @FXML
    HBox changeView;

    List<KursHinzufuegenKategorie> kategorien = new ArrayList<>();


    public enum CreateArt {
            Kurs,
            Projektgruppe
    }

    public static CreateArt createArt = CreateArt.Kurs;

    @FXML
    public void initialize(){

        typ.getItems().addAll(
                "Vorlesung",
                "Seminar",
                "Projektgruppe"
        );
        semester.getItems().addAll(
                "SS",
                "WS"
        );

        if(createArt == CreateArt.Projektgruppe){
            typ.getSelectionModel().select(2);
            typ.setDisable(true);
            semester.setDisable(true);
            jahr.setDisable(true);
            changeView.setVisible(false);
            siteTitel.setText("Projektgruppe hinzufügen");
            create.setText("Erstellen");
            jahr.setText("-187");
            semester.getSelectionModel().select(1);
            jahr.getParent().setVisible(false);
            semester.getParent().setVisible(false);
            typ.getParent().setVisible(false);
            jahr.setVisible(false);
            semester.setVisible(false);
            typ.setVisible(false);
            return;
        }
    }


    public void typChanged(){
        if(typ.getSelectionModel().getSelectedItem() == "Projektgruppe"){
            semester.setDisable(true);
            jahr.setDisable(true);
            createArt = CreateArt.Projektgruppe;
            jahr.setText("-187");
            semester.getSelectionModel().select(1);
            jahr.getParent().setVisible(false);
            semester.getParent().setVisible(false);
            if(kategorien.size() == 0) return;
            for(KursHinzufuegenKategorie k :kategorien){
                if(k == null) continue;
                k.delete();
            }
            return;
        }
        if(!jahr.getParent().isVisible()){
            createArt = CreateArt.Kurs;
            for(KursHinzufuegenKategorie k :kategorien){
                k.delete();
            }
        }
        jahr.getParent().setVisible(true);
        semester.getParent().setVisible(true);
        jahr.setText("");
        createArt = CreateArt.Kurs;
        semester.setDisable(false);
        jahr.setDisable(false);
    }


    public void create(){
        if(!validation())return;

        Semester sem = new Semester(Integer.parseInt(jahr.getText()), Enums.SemesterTyp.WS);
        Enums.Art art;
        switch (typ.getSelectionModel().getSelectedItem().toString()){
            case "Vorlesung":
                art = Enums.Art.VORLESUNG;
                break;
            case "Seminar":
                art = Enums.Art.SEMINAR;
                break;
            case "Projektgruppe":
                art = Enums.Art.PROJEKTGRUPPE;
                break;
            default:
                art = Enums.Art.VORLESUNG;
                break;

        }

        if(semester.getSelectionModel().getSelectedItem() == "SS"){
            sem = new Semester(Integer.parseInt(jahr.getText()), Enums.SemesterTyp.SS);
        }

        Lehrveranstaltung lehrveranstaltung = new Lehrveranstaltung(
                titel.getText(),
                sem,
                art
        );

        List<LVKategorie> lvKategoriesList = new ArrayList<LVKategorie>();;

        for(KursHinzufuegenKategorie kat: kategorien){
            LVKategorie lvKategorie = new LVKategorie(kat.name.getText());
            ObservableList<Node> list = FXCollections.observableArrayList(placeholder.getChildren());
            int katIndex = list.indexOf(kat.wrapper);
            lvKategorie.setPosition(katIndex);
            for(KursHinzufuegenKategorie_add element: kat.kursHinzufuegenKategorie_addList){
                if(element.file != null){
                    System.out.println("Datei");
                    KategorieDatei kategorieDatei = new KategorieDatei(element.file.getName(),element.file.getName(),"./../", element.file);
                    ObservableList<Node> list2 = FXCollections.observableArrayList(kat.placeholder.getChildren());
                    int dateiIndex = list2.indexOf(element.wrapper);
//                    lvKategorie.setPosition(dateiIndex);
                    kategorieDatei.setPosition(dateiIndex);
                    lvKategorie.addDatei(kategorieDatei);
                }
                else if(element.reminder != null || element.todo != null || element.quiz != null || element.lernkarte != null || element.bewertung != null){
                    System.out.println("reminder");
                    ObservableList<Node> list2 = FXCollections.observableArrayList(kat.placeholder.getChildren());
                    int dateiIndex = list2.indexOf(element.wrapper);
//                    lvKategorie.setPosition(dateiIndex);
                    if(element.reminder != null){
                        element.reminder.setPosition(dateiIndex);
                        lvKategorie.add(element.reminder);
                    }else if(element.todo != null){
                        System.out.println("Hallo: " + element.todo.getVerantwortliche().size());
                        element.todo.setPosition(dateiIndex);
                        lvKategorie.add(element.todo);
                    }else if(element.quiz != null){
                        element.quiz.setPosition(dateiIndex);
                        lvKategorie.add(element.quiz);
                    }else if(element.lernkarte != null){
                        element.lernkarte.setPosition(dateiIndex);
                        lvKategorie.add(element.lernkarte);
                    }else if(element.bewertung != null){
                        element.bewertung.setPosition(dateiIndex);
                        lvKategorie.add(element.bewertung);
                    }


                }


            }
            lvKategoriesList.add(lvKategorie);
        }
        lehrveranstaltung.setKategorien(lvKategoriesList);
        lehrveranstaltung.addTeilnehmer(new Lehrende(Launcher.useridMitToken.getUserid()));
        int courseID = KursHinzufügenRequest.createCourse(lehrveranstaltung, Launcher.useridMitToken);
        System.out.println(courseID);
        if(courseID == -2){
            titel.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            return;
        }
        Kursansicht.id = courseID;

        Launcher.gui.addToNavigation("/fxml/kursansicht.fxml");

    }

    public void resetStyle(){

        titel.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        jahr.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        semester.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        typ.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        for(KursHinzufuegenKategorie kat: kategorien){
            kat.name.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");

            for(KursHinzufuegenKategorie_add element: kat.kursHinzufuegenKategorie_addList){
                element.comboBox.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
                element.datei.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            }
        }
    }

    public boolean validation(){
        resetStyle();
        boolean ret = true;

        if(titel.getText().isEmpty()){
            System.out.println("Titel is invalid");
            titel.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            ret = false;
        }
        if(!Validation.isStringInteger(jahr.getText())){
            System.out.println("Jahr is invalid");
            jahr.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            ret = false;
        }
        if(semester.getSelectionModel().getSelectedItem() == null){
            System.out.println("Semester is invalid");
            semester.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            ret = false;
        }
        if(typ.getSelectionModel().getSelectedItem() == null){
            System.out.println("Typ is invalid");
            typ.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            ret = false;
        }

        for(KursHinzufuegenKategorie kat: kategorien){
            if(kat.name.getText().isEmpty()){
                System.out.println("Kategoriename is invalid");
                kat.name.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
                ret = false;
            }

            for(KursHinzufuegenKategorie_add element: kat.kursHinzufuegenKategorie_addList){
                if(element.comboBox.getSelectionModel().getSelectedItem() == null){
                    System.out.println("Combobox is invalid");
                    element.comboBox.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
                    ret = false;
                }
                if(element.file == null && element.todo == null && element.reminder == null && element.quiz == null && element.lernkarte == null && element.bewertung == null){
                    System.out.println("File is invalid");
                    element.datei.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
                    ret = false;
                }
            }

        }

        return ret;
    }

    public void back(){

    }

    public void add(){
        KursHinzufuegenKategorie kursHinzufuegenKategorie = Launcher.gui.addElement(placeholder,"/fxml/kurshinzufügen_kategorie.fxml");
        kursHinzufuegenKategorie.kursHinzufuegen = this;
        kategorien.add(kursHinzufuegenKategorie);
    }

    public void change(){
        Launcher.gui.addToNavigation("/fxml/kurshinzufügen_csv.fxml");
    }

    public void deleteKategorie(VBox element){
        for(KursHinzufuegenKategorie khk: kategorien){
            if(khk.wrapper == element){
                kategorien.remove(khk);
                break;
            }
        }
        placeholder.getChildren().remove(element);
    }
    public void reactOnEntry(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #637381;-fx-text-fill: WHITE;-fx-background-radius: 8px;-fx-font-family: 'Montserrat Regular';");
    }

    public void reactOnExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent;-fx-text-fill: #404040;-fx-border-color:#637381;-fx-border-radius:8px;-fx-font-family: 'Montserrat Regular';");
    }

    public void reactOnEntryInverted(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent;-fx-text-fill: #404040;-fx-border-color:#637381;-fx-border-radius:8px;-fx-font-family: 'Montserrat Regular';");
    }

    public void reactOnExitInverted(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #637381;-fx-text-fill: WHITE;-fx-background-radius: 8px;-fx-font-family: 'Montserrat Regular';");

    }
}
