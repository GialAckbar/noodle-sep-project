package client.Controller;

import client.Launcher;
import client.RequestHandler;
import client.Response;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import shared.*;
import shared.quiz.Quiz;
import java.util.List;

public class Kursansicht {

    @FXML
    VBox placeholder;

    @FXML
    VBox placeholderZusatz;

    @FXML
    Text name;

    @FXML
    Button status;

    public static int id= -1;

    public enum KursArt {
        Kurs,
        Projektgruppe
    }

    public static KursArt kursArt = KursArt.Kurs;
    public void initialize() {


        if(id == -1) return;
        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET,"/showCourse?veranstaltungsid=" + id, Lehrveranstaltung.class);

//        Lehrveranstaltung lehrveranstaltung1 = Launcher.requestHandler.getLehrveranstaltung(id);
//        System.out.println(lehrveranstaltung1.getKategorien().get(0).getKategorieElemente().get(0) instanceof KategorieDatei);
//
        if(response.getStatusCode() == 200){
            Lehrveranstaltung lehrveranstaltung = (Lehrveranstaltung) response.getElement();


            System.out.println("Kursansicht: " + lehrveranstaltung.getArt());
            if(lehrveranstaltung.getArt().equals(Enums.Art.PROJEKTGRUPPE)){
                kursArt = KursArt.Projektgruppe;
            }else{
                kursArt = KursArt.Kurs;
            }
            if(Launcher.userArt == Enums.Current.LEHRKRAFT || (kursArt == KursArt.Projektgruppe && lehrveranstaltung.getBelegung())){
                KursansichtZusatz kursansichtZusatz = Launcher.gui.addElement(placeholderZusatz,"/fxml/kursansichtZusatz.fxml");
                kursansichtZusatz.kursansicht = this;
                if(Launcher.userArt == Enums.Current.STUDENT){
                    kursansichtZusatz.add.setVisible(false);
                }

            }
            if(kursArt == KursArt.Projektgruppe){

                Response responseChat = Launcher.requestHandler.request(RequestHandler.Art.GET,"/getchatid?lvid=" + id, Integer.class);
                if(responseChat.statusCode == 200){
                    addChat((Integer)responseChat.getElement());

                }
            }
            System.out.println("Belegung:" +  lehrveranstaltung.getBelegung());

            if(lehrveranstaltung.getBelegung()){
                status.setText("Austragen");
            }else{
                status.setText("Eintragen");
            }

            name.setText(lehrveranstaltung.getTitel());
            for (LVKategorie lvK: lehrveranstaltung.getKategorien()){
                addKategorie(lvK.getName(), lvK.getKategorieElemente());
            }
        }
    }

    public void addChat(int id){
        KursansichtKategorie kursansichtKategorie = Launcher.gui.addElement(placeholder,"/fxml/kursansicht_kategorie.fxml");
        kursansichtKategorie.name.setText("Chatraum");
        LVKategorieElement lvk = new LVKategorieElement("Gruppenchat");
        kursansichtKategorie.addChat(id);
        kursansichtKategorie.kursansicht = this;
    }

    public void addKategorie(String name, List<LVKategorieElement> lvkElement){
        KursansichtKategorie kursansichtKategorie = Launcher.gui.addElement(placeholder,"/fxml/kursansicht_kategorie.fxml");
        kursansichtKategorie.name.setText(name);
        kursansichtKategorie.kursansicht = this;

        for(LVKategorieElement element: lvkElement){
            System.out.println(element);
            if(element instanceof KategorieDatei){
                System.out.println(element.getAnzeigename());
                System.out.println("---");
                System.out.println(((KategorieDatei) element).getDateiid());
                kursansichtKategorie.addDatei(element.getAnzeigename(),((KategorieDatei) element).getDateiid());
                continue;
            }else if(element instanceof Todo){
                kursansichtKategorie.addTodo(id, (Todo)element);
                continue;
            }else if(element instanceof Reminder){
                if(Launcher.userArt == Enums.Current.STUDENT) continue;
                kursansichtKategorie.addReminder((Reminder)element);
                continue;
            }else if(element instanceof Quiz){
                kursansichtKategorie.addQuiz((Quiz) element);
                continue;
            }else if(element instanceof Lernkarte){
                System.out.println("Lernkarte");
                kursansichtKategorie.addLernkarte((Lernkarte) element);
                continue;
            }else if(element instanceof shared.Bewertung) {
                kursansichtKategorie.addBewertung((shared.Bewertung) element);
                continue;
            }
            kursansichtKategorie.addElement(element.getAnzeigename());
        }
    }




    public void showAttendees(){
        Launcher.gui.addToNavigation("/fxml/kursteilnehmer.fxml");
    }

    public void enterCourse(){
        if(status.getText().equals("Eintragen")){
            System.out.println("eintragen");
            Response response = Launcher.requestHandler.request(RequestHandler.Art.POST,"/kursteilnehmer/add?courseid=" + Kursansicht.id, Launcher.useridMitToken.getUserid());
            Launcher.gui.navigation.reloadNavigation();
            if(response.statusCode == 200){
                if(kursArt == KursArt.Projektgruppe){
                    placeholderZusatz.getChildren().clear();
                    KursansichtZusatz kursansichtZusatz = Launcher.gui.addElement(placeholderZusatz,"/fxml/kursansichtZusatz.fxml");
                    kursansichtZusatz.kursansicht = this;
                    if(Launcher.userArt == Enums.Current.STUDENT){
                        kursansichtZusatz.add.setVisible(false);
                    }
                }
                status.setText("Austragen");
            }
            return;
        }

        Response response = Launcher.requestHandler.request(RequestHandler.Art.DELETE,"/kursteilnehmer/remove?courseid=" + Kursansicht.id + "&targetid=" + Launcher.useridMitToken.getUserid());
        Launcher.gui.navigation.reloadNavigation();
        if(response.statusCode == 200){
            placeholderZusatz.getChildren().clear();
            status.setText("Eintragen");
        }

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
