package client.Controller;

import client.Launcher;
import client.RequestHandler;
import client.Response;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import shared.*;
import shared.navigation.NavigationInformation;
import shared.navigation.SemesterMitVeranstaltungen;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;


public class Navigation {

    @FXML
    Group placeholder;

    @FXML
    public Group placeholderPopup;
    @FXML
    VBox dropdownPlaceholder;

    @FXML
    Pane dropdownMenu;

    @FXML
    Text name;

    @FXML
    VBox kurse;

    @FXML
    ImageView image;


//    @FXML
//    Button toAttCourses;
//
//    @FXML
//    Button toGlobalCourses;

    public static Timeline timer;

    private LinkedHashMap<String,String> dropdown = new LinkedHashMap<String,String>(){{
        put("Profil", "/fxml/EigenesProfil2.fxml");
        put("Freundesliste", "/fxml/freundesListe_Freunde.fxml");
        put("Projektgruppen", "/fxml/projektgruppen.fxml");
        put("Chats", "/fxml/chatListStarted.fxml");
        put("Kalender", "/fxml/calendar.fxml");
        put("Abmelden", "/fxml/login.fxml");
    }};





    public void initialize() {


        Launcher.gui.navigationGroup = placeholder;
        Launcher.gui.navigation = this;
        ProfilView.accountid = Launcher.useridMitToken.getUserid();
        Launcher.gui.addToNavigation("/fxml/EigenesProfil2.fxml");
//        Launcher.gui.addToNavigation("/fxml/todoHinzufügen.fxml");


        if(Launcher.userArt == Enums.Current.LEHRKRAFT){
            dropdown = new LinkedHashMap<String,String>(){{
                put("Profil", "/fxml/EigenesProfil2.fxml");
                put("Projektgruppen", "/fxml/projektgruppen.fxml");
                put("Chats", "/fxml/chatListStarted.fxml");
                put("Kalender", "/fxml/calendar.fxml");
                put("Abmelden", "/fxml/login.fxml");
            }};
        }else{
            dropdown = new LinkedHashMap<String,String>(){{
                put("Profil", "/fxml/EigenesProfil2.fxml");
                put("Freundesliste", "/fxml/freundesListe_Freunde.fxml");
                put("Projektgruppen", "/fxml/projektgruppen.fxml");
                put("Chats", "/fxml/chatListStarted.fxml");
                put("Kalender", "/fxml/calendar.fxml");
                put("Abmelden", "/fxml/login.fxml");
            }};
        }

        reloadNavigation();

        timer = new Timeline(
            new KeyFrame(Duration.seconds(30),
                    event -> reloadNavigation()
            )
        );
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();

    }


    public void addElementsToDropdown(){
        dropdownPlaceholder.getChildren().clear();
        for(HashMap.Entry<String, String> entry: dropdown.entrySet()){
            addToDropdown(entry.getKey(),entry.getValue());
        }
    }

    public void addToDropdown(String title, String fxml){
        DropdownElement ddE = Launcher.gui.addElement(dropdownPlaceholder,"/fxml/dropdownElement.fxml");
        ddE.titel.setText(title);
        ddE.fxml = fxml;
    }

    public void closeDropdown(){
        dropdownMenu.setVisible(false);
    }



    public void reloadNavigation(){
        System.out.println("reloaded");
        clearKurse();
        addElementsToDropdown();
        while(placeholderPopup.getChildren().size() > 0){

            placeholderPopup.getChildren().remove(0);
        }
        System.out.println("popup = 0: " + placeholderPopup.getChildren().size());
        loadNavigation();
    }

    public void loadNavigation(){
        System.out.println(Launcher.gui.getCurrentState());
        System.out.println(Enums.Current.LEHRKRAFT);
        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET,"/navigation", NavigationInformation.class);

        if(response.statusCode == 200){
            NavigationInformation navInfo = (NavigationInformation) response.getElement();

            popup(navInfo.getReminder());

            Launcher.user = navInfo.getUser();
            setName(navInfo.getUser().getVorname(),navInfo.getUser().getNachname());

            System.out.println(navInfo.getPbid());

            if(navInfo.getPbid() != -1){
                image.setImage(new Image("http://127.0.0.1:1337/getfile?userid="+Launcher.useridMitToken.getUserid()+"&token="+Launcher.useridMitToken.getToken()+"&fileid="+navInfo.getPbid()));
            }


            for(SemesterMitVeranstaltungen semester: navInfo.getSemesterMitVeranstaltungen()){
                NavigationSemester navSemester = addSemester(semester.getSemester().getSemesterTyp() + " " + semester.getSemester().getJahr());
                for(Lehrveranstaltung lehrveranstaltung: semester.getLehrveranstaltungen()){
                    addKursToSemester(navSemester,lehrveranstaltung.getVeranstaltungsID(),lehrveranstaltung.getTitel());
                }
            }

        }


    }

    private void popup(List<Reminder> reminderList){
        if(reminderList.size() == 0) return;
        Launcher.gui.removePopups();


        ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.systemDefault());


        for(int i = 0; i != reminderList.size(); i++){
            Reminder current = reminderList.get(i);
            if((current.getRemindDate()).withZoneSameInstant(ZoneId.systemDefault()).toInstant().compareTo(currentTime.toInstant()) < 0){
                ZonedDateTime eventDate = current.getEventDate();
                eventDate = eventDate.withZoneSameInstant(ZoneId.systemDefault());


                PopUp popUp = Launcher.gui.addPopup();
                popUp.titel.setText(current.getAnzeigename());
                popUp.art.setText(current.getAnzeigename() + " bis:");
                popUp.time.setText(eventDate.getHour() + ":" + eventDate.getMinute());
                popUp.date.setText(eventDate.getDayOfMonth() + "." + eventDate.getMonthValue() + "." + eventDate.getYear());
                popUp.id = current.getId();
            }

        }

    }

    private void setName(String vorname, String nachname){
        name.setText(vorname + " " + nachname);
    }

    private void clearKurse(){
        kurse.getChildren().clear();
    }

    private NavigationSemester addSemester(String name){
        NavigationSemester navigationSemester = Launcher.gui.addElement(kurse,"/fxml/navigation_semester.fxml");
        navigationSemester.semester.setText(name);
        return navigationSemester;
    }


    private void addKursToSemester(NavigationSemester navigationSemester,int id, String name){
        NavigationKurs navigationKurs = Launcher.gui.addElement(navigationSemester.semesterPlaceholder,"/fxml/navigation_kurs.fxml");
        navigationKurs.id = id;
        navigationKurs.name.setText(name);
    }

    public void goToCreateCourse(){

        KursHinzufuegen.createArt = KursHinzufuegen.CreateArt.Kurs;
        Launcher.gui.addToNavigation("/fxml/kurshinzufügen.fxml");
    }

    public void changeView(){
//        Launcher.gui.addElement(placeholderPopup,"/fxml/popup.fxml");

//        if(Launcher.gui.getCurrentState() == "navigation"){
//            Launcher.gui.changeState("navigationLehrkraft");
//            Launcher.userArt = Enums.Current.LEHRKRAFT;
//            return;
//        }
//        Launcher.gui.changeState("navigation");
//        Launcher.userArt = Enums.Current.STUDENT;
    }


    public void showDropdown(){
        ProfilView.accountid = Launcher.useridMitToken.getUserid();
//        Launcher.gui.addToNavigation("/fxml/EigenesProfil2.fxml");
        toggleDropdown();
    }

    public void toggleDropdown(){
        if(dropdownMenu.isVisible()){
            dropdownMenu.setVisible(false);
            return;
        }
        dropdownMenu.setVisible(true);
    }

    @FXML
    void toCourseSearch(ActionEvent event) {
        Launcher.gui.addToNavigation("/fxml/courseList.fxml");
    }

    @FXML
    void goToAttentedCourses(ActionEvent event) {
        ProfilView.accountid = Launcher.useridMitToken.getUserid();
        Launcher.gui.addToNavigation("/fxml/engagedCourseList.fxml");
    }

    @FXML
    void reactOnEntry(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #637381;-fx-text-fill: WHITE;-fx-background-radius: 8px;-fx-font-family: 'Montserrat Regular';");
    }

    @FXML
    void reactOnExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent;-fx-text-fill: #404040;-fx-border-color:#637381;-fx-border-radius:8px;-fx-font-family: 'Montserrat Regular';");
    }
}


