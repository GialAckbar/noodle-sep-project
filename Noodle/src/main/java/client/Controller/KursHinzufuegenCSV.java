package client.Controller;

import client.Controller.kurs.KursHinzufügenRequest;
import client.FileHandler;
import client.Launcher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import shared.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class KursHinzufuegenCSV {
    @FXML
    Button button;
    @FXML
    VBox placeholder;


    LehrveranstaltungsListe lehrveranstaltungsListe;

    public void create(){
        if(lehrveranstaltungsListe == null || lehrveranstaltungsListe.getList() == null){
            System.out.println("Keine CSV Datei ausgewählt");
            return;
        }

        ObservableList<Node> list = FXCollections.observableArrayList(placeholder.getChildren());
        ObservableList<Node> list2 = FXCollections.observableArrayList();

        int i = 0;
        for(Lehrveranstaltung lv: lehrveranstaltungsListe.getList()){
            lv.addTeilnehmer(new Lehrende(Launcher.useridMitToken.getUserid()));
            int courseID = KursHinzufügenRequest.createCourse(lv, Launcher.useridMitToken);
            System.out.println(courseID);
            if(courseID  < 0){
                list.get(i).setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px");
            }else{
                list2.add(list.get(i));
            }

            i++;
        }

        for(Node node: list2){
            placeholder.getChildren().remove(node);
        }
        Launcher.gui.navigation.reloadNavigation();
//        placeholder.getChildren().removeAll();
//        Launcher.gui.addToNavigation("/fxml/kurshinzufügen_csv.fxml");

    }

    public void back(){

    }

    public void manuell(){
        Launcher.gui.addToNavigation("/fxml/kurshinzufügen.fxml");
    }

    public void open(){
        lehrveranstaltungsListe = new LehrveranstaltungsListe();
        String order = null;
        File file = FileHandler.openCSV();

        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                if(order == null){
                    System.out.println("null");
                    order = line;
                    continue;
                }
                addKursCSV(order, line);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addKursCSV(String order,String line){
        String orderParts[] = order.split(";");
        String lineParts[] = line.split(";");

        System.out.println(orderParts[0].strip() + ": " + getPart(orderParts,lineParts,"Titel") + " " + orderParts[1].strip() + ": " + getPart(orderParts,lineParts,"Veranstaltungsart") + " " + orderParts[2].strip() + ": " + getSemester(orderParts,lineParts).getSemesterTyp() + " " + getSemester(orderParts,lineParts).getJahr());

        Lehrveranstaltung lehrveranstaltung = new Lehrveranstaltung(getPart(orderParts,lineParts,"titel"),
                getSemester(orderParts,lineParts),
                getArt(getPart(orderParts,lineParts,"Veranstaltungsart")));

        lehrveranstaltungsListe.add(lehrveranstaltung);
        KursHinzufuegenCSVAdd kursHinzufuegenCSVAdd = Launcher.gui.addElement(placeholder,"/fxml/kurshinzufügen_csv_add.fxml");
        kursHinzufuegenCSVAdd.kursHinzufuegenCSV = this;

        kursHinzufuegenCSVAdd.time.setText(getSemester(orderParts,lineParts).getSemesterTyp() + " " + getSemester(orderParts,lineParts).getJahr());
        kursHinzufuegenCSVAdd.art.setText(getPart(orderParts,lineParts,"Veranstaltungsart"));
        kursHinzufuegenCSVAdd.titel.setText(getPart(orderParts,lineParts,"titel"));


    }

    private Semester getSemester(String order[], String line[]){
        for(int i = 0; i < order.length; i++){
            if(order[i].equalsIgnoreCase("semester") || order[i].equalsIgnoreCase("\uFEFFsemester")){

                return new Semester(getSemesterJahr(line[i].strip()),getSemesterTypEnum(line[i].strip()));
            }
        }
        return null;
    }

    private String getPart(String order[], String line[],String part){
        for(int i = 0; i < order.length; i++){
            if(order[i].equalsIgnoreCase(part) || order[i].equalsIgnoreCase("\uFEFF" + part)){
                return line[i].strip();
            }
        }
        return "";
    }

    private Enums.Art getArt(String art){
        switch (art.toLowerCase()){
            case "vorlesung":
                return Enums.Art.VORLESUNG;
            case "seminar":
                return Enums.Art.SEMINAR;
            case "Projektgruppe":
                return Enums.Art.PROJEKTGRUPPE;
            default:
                return Enums.Art.VORLESUNG;
        }
    }

    private Enums.SemesterTyp getSemesterTypEnum(String semester){

        if(semester.toLowerCase().contains("sose"))
            return Enums.SemesterTyp.SS;
        return Enums.SemesterTyp.WS;
    }

    private String getSemesterTyp(String semester){

        if(semester.toLowerCase().contains("sose"))
            return "SS";
        return "WS";
    }

    private int getSemesterJahr(String semester){
        Pattern p = Pattern.compile("-?\\d+");
        Matcher m = p.matcher(semester);

        while (m.find()) {
            if(m.group().length() > 2){
                return Integer.parseInt(m.group());
            }
            return 2000+Integer.parseInt(m.group());
        }
        return 0;
    }

    public void delete(VBox element){

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
