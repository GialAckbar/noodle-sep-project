package client.Controller;

import client.Launcher;
import client.RequestHandler;
import client.Response;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import shared.LehrveranstaltungsListe;
import shared.Student;
import shared.Themenangebot;
import shared.ThemenangebotsListe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
// Icons made by https://www.flaticon.com/authors/good-ware from https://www.flaticon.com/
public class Themenangebote {

    @FXML
    VBox placeholder;
    @FXML
    Button btnThemaAnzeigen;
    @FXML
    Text errorText;

    public static int accountid = -1;
    public static boolean lehrender = true;
    public static boolean inCourse = false;
    public static boolean ich = false;
    List<Themenangebot> list;
    List<String[]> allTopics = new ArrayList<>();

    ProfilView profilView;

    public void change() {
        profilView.change();
    }

    @FXML
    private void initialize() {
        Response response;
        if(lehrender && inCourse == false) {
            response = Launcher.requestHandler.request(RequestHandler.Art.GET, "/profildaten/themenangeboteLehrender", ThemenangebotsListe.class);
        }
        else if(lehrender && inCourse){
            response = Launcher.requestHandler.request(RequestHandler.Art.GET, "/profildaten/themenangeboteLehrenderOther?accountid=" + accountid, ThemenangebotsListe.class);
        }
        else{
            response = Launcher.requestHandler.request(RequestHandler.Art.GET, "/profildaten/themenangeboteStudent", ThemenangebotsListe.class);

        }
        int code = (response != null) ? response.statusCode : -1;
        if (code == 400) System.out.println("* Code 400: Es wurden ungültige Daten übergeben!");
        else if (code == 403) System.out.println("* Code 403: Zugriff verweigert!");
        else if (code == 500) System.out.println("* Code 500: Serverseitiger Fehler!");
        if (response.statusCode == 200) {
            errorText.setVisible(false);

            ThemenangebotsListe th = (ThemenangebotsListe) response.getElement();
            list = th.getList();

            if (list.isEmpty() == false) {
                for (Themenangebot x : list) {
                    if (x != null) {
                        String titel = x.getTitel();
                        String beschreibung = x.getBeschreibung();
                        String id = Integer.toString(x.getId());
                        allTopics.add(new String[]{titel, beschreibung, id});
                    }
                }
                allTopics.sort(Comparator.comparing(strings -> strings[0].toLowerCase()));
                showAllTopics(allTopics);
            } else {
                createEmptyTopicList();
            }
        }
        btnThemaAnzeigen.setCursor(Cursor.HAND);
        if ((lehrender && !inCourse) ||lehrender && ich){
            btnThemaAnzeigen.setVisible(true);
        }
        else
            {
                btnThemaAnzeigen.setVisible(false);
            }
        }


    void showAllTopics(List<String[]> Topics) {
        for (String[] u : Topics) {
            addTopic(u[0], u[1], u[2]);
        }
    }

    void addTopic(String titel, String beschreibung, String id) {
        Group group = new Group();
        GridPane gridPane = addGridEntry(titel, beschreibung, id,group);
        group.getChildren().add(gridPane);
        placeholder.getChildren().add(group);
    }

    private GridPane addGridEntry(String titel, String beschreibung, String id, Group group) {
        GridPane gp = createGridPane();

        Text t = new Text(titel);
        t.getStyleClass().add("courseTitle");

        Text b = new Text(beschreibung);
        b.getStyleClass().add("courseData");
        Button zumThema = new Button("Zum Thema");
        ImageView ablehnen = new ImageView("/icon/delete.png");
        ablehnen.setFitWidth(40);
        ablehnen.setFitHeight(40);

        Button ablehnen2 = new Button();
        ablehnen2.setGraphic(ablehnen);
        ablehnen2.setPrefSize(40,40);
        ablehnen2.setCursor(Cursor.HAND);
        if((lehrender && inCourse) || !lehrender) {
            ablehnen2.setVisible(false);
        }
        else{
            ablehnen2.setVisible(true);
        }

        zumThema.setTextFill(Color.WHITE);
        zumThema.getStyleClass().add("toCourseBtn");
        zumThema.setPrefSize(110, 30);
        zumThema.setMinHeight(48);
        zumThema.setCursor(Cursor.HAND);
        zumThema.setOnAction(event -> goToTopic(Integer.parseInt(id)));
        zumThema.setOnMouseEntered(event -> zumThema.setStyle("-fx-background-color: #374048"));
        zumThema.setOnMouseExited(event -> zumThema.setStyle("-fx-background-color: #637381"));
        gp.setMargin(zumThema, new Insets(0, 0, 0, 300));


        // Grauer Rahmen, um die GridPanes, wenn man mit der Maus drübergeht
        gp.setOnMouseEntered(event -> gp.setStyle("-fx-background-color: #E6E6E6"));
        gp.setOnMouseExited(event -> gp.setStyle("-fx-background-color: transparent"));

        ablehnen2.setOnAction(event -> denyRequest(id, group));

        gp.addRow(0, t);
        gp.addRow(1, b);
        gp.add(zumThema, 1, 1);
        gp.add(ablehnen2,2,1);
        gp.addRow(2, new Line(2390, 0, 1326, 0));


        return gp;
    }


    public GridPane createGridPane() {
        GridPane gp = new GridPane();
        gp.setPrefSize(1150, 120);
        gp.getColumnConstraints().add(new ColumnConstraints(500));
        gp.getColumnConstraints().add(new ColumnConstraints(450));
        gp.getColumnConstraints().add(new ColumnConstraints(50));

        gp.getRowConstraints().add(new RowConstraints(10, 30, 30));
        gp.getRowConstraints().add(new RowConstraints(10, 30, 30));
        gp.getRowConstraints().add(new RowConstraints(10, 30, 30));
        gp.setPadding(new Insets(0, 0, 0, 60));
        return gp;
    }

    void goToTopic(int id) {
        ThemaAnzeigen.topicid = id;
        Launcher.gui.addOverNavigation("/fxml/ThemaAnzeigen.fxml");
    }

    private void createEmptyTopicList() {
        GridPane gp = createEmptyPane();
        //Warning sign
        ImageView noodle = new ImageView("/images/noodles.png");
        noodle.setFitHeight(58);
        noodle.setFitWidth(58);
        //Input
        Text n = new Text("Es wurden keine Themenangebote gefunden!");
        n.getStyleClass().add("falseInput");
        GridPane.setMargin(n, new Insets(0, 0, 110, 20));
        GridPane.setMargin(noodle, new Insets(0, 0, 110, 0));

        //Zum GridPane hinzufügen
        gp.add(noodle, 0, 0);
        gp.add(n, 1, 0);
        placeholder.getChildren().add(gp);
    }

    private GridPane createEmptyPane() {
        GridPane gp = new GridPane();
        gp.setPrefSize(1100, 120);
        gp.getColumnConstraints().add(new ColumnConstraints(50));
        gp.getColumnConstraints().add(new ColumnConstraints(1000));
        gp.getRowConstraints().add(new RowConstraints(203));
        gp.setPadding(new Insets(0, 0, 0, 50));
        return gp;
    }

    void denyRequest(String thid, Group pane) {
        String args = "?thid=" + thid;
        Response response = Launcher.requestHandler.request(RequestHandler.Art.DELETE, "/themenangebot/removeThema" + args);
        placeholder.getChildren().remove(pane);
        if (placeholder.getChildren().size() == 1) {
            createEmptyTopicList();
        }
        int code = (response != null) ? response.statusCode : -1;

        errorText.setVisible(true);
        if (code == 200 || code == 409) {
            if (code == 200) errorText.setVisible(false);
            else errorText.setText("* Code 409: Thema bereits gelöscht!!");
        } else if (code == 500) errorText.setText("* Code 500: Konnte Nutzer nicht hinzufügen!");
        else errorText.setText("* Unbekannter Fehler aufgetreten!");
    }

    public void next() {
        if (lehrender) {
            profilView.nextScene();
        }
    }
}

