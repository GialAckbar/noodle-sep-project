package client.Controller;

import client.Launcher;
import client.RequestHandler;
import client.Response;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import shared.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ThemaAnzeigen {

    @FXML
    public Text titel;

    @FXML
    public Text beschreibung;

    @FXML
    public VBox placeholder;

    @FXML
    public Button back;

    public static int topicid = -1;

    List<String[]> allliteraturen =  new ArrayList<>();
    Themenangebot themenangebot;
    LiteraturverzeichnisListe literaturverzeichnisListe;
    List<Themenangebot> list = new ArrayList<>();
    List<String[]> allTopics;

    public void initialize() {
        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET,"/profildaten/themenangebotLiteratur?thid=" + topicid, ThemenangebotLiteratur.class);
        int code = (response != null) ? response.statusCode : -1;
        if (code == 400) System.out.println("* Code 400: Es wurden ungültige Daten übergeben!");
        else if (code == 403) System.out.println("* Code 403: Zugriff verweigert!");
        else if (code == 500) System.out.println("* Code 500: Serverseitiger Fehler!");
        if(response.statusCode == 200) { //Profil

            ThemenangebotLiteratur themenangebotLiteratur = (ThemenangebotLiteratur) response.getElement();

            themenangebot = themenangebotLiteratur.getThemenangebot();
            literaturverzeichnisListe = themenangebotLiteratur.getListe();
            titel.setText(themenangebot.getTitel());
            beschreibung.setText(themenangebot.getBeschreibung());


            if(literaturverzeichnisListe != null){
                int counter = 1;
                for(Literaturverzeichnis l : literaturverzeichnisListe.getList()){
                    String id = Integer.toString(l.getId());
                    String titel = Integer.toString(counter) + ". " + l.getTitel();
                    String autor = l.getAutor();
                    String jahr = l.getJahr();
                    String art = l.getArt();
                    String thid = Integer.toString(l.getThid());

                    String daten = "Autor: "  + autor + "     •     Jahr: " + jahr + "     •     Art: " + art;
                    allliteraturen.add(new String[]{id,titel,daten,thid});
                    counter++;
                }
            }

            if(!allliteraturen.isEmpty()){
                showAllLiteraturen(allliteraturen);
            }
            else{
                createEmptyPane();
            }


            titel.getStyleClass().add("courseData");
            beschreibung.getStyleClass().add("courseData");
        }
    }
   void showAllLiteraturen(List<String[]> Users) {
        for (String[] u : Users) {
            addTopic(u[0], u[1],u[2],u[3]);
        }
    }

    void addTopic(String id,String titel, String daten,String thid) {
        Group group = new Group();
        GridPane gridPane = addGridEntry(id,titel,daten,thid,group);
        group.getChildren().add(gridPane);
        placeholder.getChildren().add(group);
    }



    public GridPane createGridPane() {
        GridPane gp = new GridPane();
        gp.setPrefSize(1600, 120);
        gp.getColumnConstraints().add(new ColumnConstraints(800));
        gp.getRowConstraints().add(new RowConstraints(10, 30, 30));
        gp.getRowConstraints().add(new RowConstraints(10, 30, 30));
        gp.setPadding(new Insets(0,0,0,60));
        return gp;
    }

    private GridPane createEmptyPane() {
        GridPane gp = new GridPane();
        gp.setPrefSize(1185, 120);
        gp.getColumnConstraints().add(new ColumnConstraints(60));
        gp.getColumnConstraints().add(new ColumnConstraints(1129));
        gp.getRowConstraints().add(new RowConstraints(203));
        gp.setPadding(new Insets(0,0,0,60));
        return gp;
    }

    private GridPane addGridEntry(String id,String titel, String daten, String thid,Group group) {
        GridPane gp = createGridPane();

        Text tit = new Text(titel);
        Text dat = new Text(daten);

        tit.getStyleClass().add("courseTitle");
        dat.getStyleClass().add("courseData");


        gp.addRow(0, tit);
        gp.addRow(1,dat);

        return gp;
    }


    public void back() {
        Launcher.gui.removeAllOverNavigation();
    }


    public void reactOnEntryInverted(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent;-fx-text-fill: #404040;-fx-border-color:#637381;-fx-border-radius:8px;");
    }

    public void reactOnExitInverted(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #637381;-fx-text-fill: WHITE;-fx-background-radius: 8px;");
    }
}
