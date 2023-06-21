package client.Controller;

import client.Launcher;
import client.RequestHandler;
import client.Response;
import client.urlconnections.KursBearbeitenRequest;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import shared.*;
import shared.quiz.Quiz;
import java.util.ArrayList;
import java.util.List;


public class KursBearbeiten {

    @FXML
    TextField titel;

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
    Button abbrechen;

    @FXML
    Button save;

    @FXML
    Text siteTitel;

    public int id = -1;

    List<KursHinzufuegenKategorie> kategorien = new ArrayList<>();

    public enum BearbeitenArt {
        Kurs,
        Projektgruppe
    }

    public static BearbeitenArt bearbeitenArt = BearbeitenArt.Kurs;

    public void initialize() {

        typ.getItems().addAll(
                "Vorlesung",
                "Seminar",
                "Projektgruppe"
        );
        semester.getItems().addAll(
                "SS",
                "WS"
        );

        if(bearbeitenArt == BearbeitenArt.Projektgruppe){
            typ.getSelectionModel().select(2);
            typ.setDisable(true);
            semester.setDisable(true);
            jahr.setDisable(true);
            siteTitel.setText("Projektgruppe bearbeiten");
            save.setText("Erstellen");
            jahr.setText("-187");
            semester.getSelectionModel().select(1);
            jahr.getParent().setVisible(false);
            semester.getParent().setVisible(false);
            typ.getParent().setVisible(false);
            jahr.setVisible(false);
            semester.setVisible(false);
            typ.setVisible(false);
        }

        if(Launcher.userArt != Enums.Current.LEHRKRAFT && bearbeitenArt != BearbeitenArt.Projektgruppe){
            abbrechen();
            return;
        }

        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET,"/showCourse?veranstaltungsid=" + Kursansicht.id, Lehrveranstaltung.class);

        if(response.getStatusCode() == 200){
            System.out.println("Kursbearbeiten");
            Lehrveranstaltung lehrveranstaltung = (Lehrveranstaltung) response.getElement();

            id = lehrveranstaltung.getVeranstaltungsID();

            titel.setText(lehrveranstaltung.getTitel());

            typ.getSelectionModel().select(lehrveranstaltung.getArt().toString());

            semester.getSelectionModel().select(Enums.SemesterTyp.getString(lehrveranstaltung.getSemester().getSemesterTyp()));

            jahr.setText(String.valueOf(lehrveranstaltung.getSemester().getJahr()));




            for (LVKategorie lvK: lehrveranstaltung.getKategorien()){
                addKategorie(lvK.getName(), lvK.getKategorieElemente(), lvK.getID());
            }
        }

    }

    public void addKategorie(String name, List<LVKategorieElement> lvkElement, int id) {



        KursHinzufuegenKategorie kursHinzufuegenKategorie = Launcher.gui.addElement(placeholder,"/fxml/kurshinzufügen_kategorie.fxml");
        kursHinzufuegenKategorie.kursBearbeiten = this;
        kursHinzufuegenKategorie.id = id;
        kursHinzufuegenKategorie.name.setText(name);
        kategorien.add(kursHinzufuegenKategorie);

        for(LVKategorieElement element: lvkElement){
            if(element instanceof KategorieDatei){
                kursHinzufuegenKategorie.add((KategorieDatei) element);
                continue;
            }else if(element instanceof Reminder){

                kursHinzufuegenKategorie.add((Reminder) element);
                continue;
            }else if(element instanceof Quiz){

                kursHinzufuegenKategorie.add((Quiz) element);
                continue;
            }else if(element instanceof Todo){
                kursHinzufuegenKategorie.add(this.id, (Todo) element);
                continue;
            }else if(element instanceof Lernkarte){
                kursHinzufuegenKategorie.add((Lernkarte) element);
                continue;
            }else if(element instanceof shared.Bewertung){
                kursHinzufuegenKategorie.add((shared.Bewertung) element);
            }

//            kursHinzufuegenKategorie.addElement(element.getAnzeigename());
        }

    }

    public void add(){
        KursHinzufuegenKategorie kursHinzufuegenKategorie = Launcher.gui.addElement(placeholder,"/fxml/kurshinzufügen_kategorie.fxml");
        kursHinzufuegenKategorie.kursBearbeiten = this;
        kategorien.add(kursHinzufuegenKategorie);
    }

    public void abbrechen(){
        Launcher.gui.addToNavigation("/fxml/kursansicht.fxml");
    }


    public void save(){
        if(!validation()) return;

        Semester sem = new Semester(Integer.parseInt(jahr.getText()), Enums.SemesterTyp.WS);;
        Enums.Art art;
        switch (typ.getSelectionModel().getSelectedItem().toString().toLowerCase()){
            case "vorlesung":
                art = Enums.Art.VORLESUNG;
                break;
            case "seminar":
                art = Enums.Art.SEMINAR;
                break;
            case "projektgruppe":
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
                art,
                id
        );

        List<LVKategorie> lvKategoriesList = new ArrayList<>();;

        for(KursHinzufuegenKategorie kat: kategorien){
            LVKategorie lvKategorie = new LVKategorie(kat.id,kat.name.getText());



            for(KursHinzufuegenKategorie_add element: kat.kursHinzufuegenKategorie_addList){
                ObservableList<Node> list = FXCollections.observableArrayList(kat.placeholder.getChildren());
                int elementIndex = list.indexOf(element.wrapper);

                if(element.dateiid != -1){
                    KategorieDatei kategorieDatei = new KategorieDatei(element.id,element.datei.getText());
                    kategorieDatei.setDateiid(element.dateiid);
                    kategorieDatei.setId(element.id);
                    kategorieDatei.setFile(element.file);
                    System.out.println("datei id:" +element.id);
                    kategorieDatei.setPosition(elementIndex);
                    lvKategorie.addDatei(kategorieDatei);
                }else if(element.file != null){
                    KategorieDatei kategorieDatei = new KategorieDatei(element.file.getName(),element.file.getName(),"./../", element.file);
                    kategorieDatei.setPosition(elementIndex);
                    lvKategorie.addDatei(kategorieDatei);
                }else if(element.reminder != null){
                    element.reminder.setPosition(elementIndex);
                    lvKategorie.add(element.reminder);
                }else if(element.quiz != null){
                    element.quiz.setPosition(elementIndex);
                    lvKategorie.add(element.quiz);
                }else if(element.todo != null) {
                    System.out.println("Hallo: " + element.todo.getVerantwortliche().size());

                    element.todo.setPosition(elementIndex);
                    lvKategorie.add(element.todo);
                }else if(element.lernkarte != null){
                    element.lernkarte.setPosition(elementIndex);
                    lvKategorie.add(element.lernkarte);
                }else if(element.bewertung != null){
                    element.bewertung.setPosition(elementIndex);
                    lvKategorie.add(element.bewertung);
                }else{
                    System.out.println("bearbeiten Error");
                }





            }


            ObservableList<Node> list2 = FXCollections.observableArrayList(placeholder.getChildren());
            int index = list2.indexOf(kat.wrapper);
            lvKategorie.setPosition(index);
            lvKategoriesList.add(lvKategorie);
        }



        lehrveranstaltung.setKategorien(lvKategoriesList);
//        lehrveranstaltung.addTeilnehmer(new Lehrende(Launcher.useridMitToken.getUserid()));

        int kursIndex = KursBearbeitenRequest.updateCourse(lehrveranstaltung, Launcher.useridMitToken);
        if(kursIndex == 0){
            System.out.println("Updated");
            Launcher.gui.addToNavigation("/fxml/kursansicht.fxml");
            return;
        }else if(kursIndex == -2){
            System.out.println("Titel is not unique");
            titel.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        }


        System.out.println("Not Updated");

        //endpunkt endern
//        int courseID = KursHinzufügenRequest.createCourse(lehrveranstaltung, Launcher.useridMitToken);
//        System.out.println(courseID);
//
//        Kursansicht.id = courseID;

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
                if(element == null) System.out.println("element ist null");
                if(element.comboBox.getSelectionModel().getSelectedItem() == null){
                    System.out.println("Combobox is invalid");
                    element.comboBox.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
                    ret = false;
                }
                if(element.file == null && element.todo == null && element.reminder == null && element.quiz == null && element.lernkarte == null && element.bewertung == null){

                    if(element.dateiid == -1){
                        System.out.println("File is invalid");
                        element.datei.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
                        ret = false;
                    }

//                    if(!element.datei.isDisabled()){
//                        System.out.println("File is invalid");
//                        element.datei.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
//                        ret = false;
//                    }
                }

            }

        }

        return ret;
    }

    public void typChanged(){
        if(typ.getSelectionModel().getSelectedItem() == "Projektgruppe"){
            semester.setDisable(true);
            jahr.setDisable(true);
            bearbeitenArt = BearbeitenArt.Projektgruppe;
            jahr.setText("-187");
            semester.getSelectionModel().select(1);
            jahr.getParent().setVisible(false);
            semester.getParent().setVisible(false);
            for(KursHinzufuegenKategorie k :kategorien){
                k.delete();
            }
            return;
        }
        if(!jahr.getParent().isVisible()){
            bearbeitenArt = BearbeitenArt.Kurs;
            for(KursHinzufuegenKategorie k :kategorien){
                k.delete();
            }
        }
        jahr.getParent().setVisible(true);
        semester.getParent().setVisible(true);
        jahr.setText("");
        bearbeitenArt = BearbeitenArt.Kurs;
        semester.setDisable(false);
        jahr.setDisable(false);
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
                if(!element.datei.isDisabled()){
                    element.datei.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
                }
            }
        }
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
