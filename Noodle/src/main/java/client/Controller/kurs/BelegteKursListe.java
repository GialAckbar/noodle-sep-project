package client.Controller.kurs;


import client.Controller.Kursansicht;
import client.Controller.ProfilView;
import client.Launcher;
import client.RequestHandler;
import client.Response;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import shared.Lehrveranstaltung;
import shared.LehrveranstaltungsListe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Icons made by https://www.flaticon.com/authors/good-ware from https://www.flaticon.com/
public class BelegteKursListe {

    @FXML private TextField courseNameAt;
    @FXML private VBox entries;
    private List<LehrveranstaltungsListe> list;
    public static String changedInput;

    @FXML
    void goToCourse(int id) {
        BelegteKursListe.changedInput = null;
        //Kursansicht Controller die ID des angeklickten Kurses übergeben
        Kursansicht.id = id;
//        Zur Kursansicht wechseln
        Launcher.gui.addToNavigation("/fxml/kursansicht.fxml");
    }
    @FXML
    void goToAll() { //Methode zum übergeben der aktuellen Suchleiste, wenn von belegten Kursliste zur globalen gewechselt wird
        //Aktuellen Inhalt der Suchleiste übergeben
        changedInput = courseNameAt.getText();
        //Zur globalen Liste wechseln
        Launcher.gui.addToNavigation("/fxml/courseList.fxml");
    }

    void searchCourse(String input) {
        entries.getChildren().clear();
        List<Lehrveranstaltung> lehrVList = new ArrayList<>();
        for(LehrveranstaltungsListe x : list) {
            if (x != null) {
                String sem = x.getList().get(0).getSemester().getSemesterTyp().toString();
                int jah = x.getList().get(0).getSemester().getJahr();

                Group r = new Group();
                r.getChildren().add(addSemesterGridEntry(sem, jah));
                entries.getChildren().add(r);

                List<Lehrveranstaltung> lV = new ArrayList<>();
                for (Lehrveranstaltung y : x.getList()) {
                    if (y.getTitel().toLowerCase().contains(input.toLowerCase())) {
                        lV.add(y);
                    }
                }
                if (lV.size() != 0) {
                    for (Lehrveranstaltung v : lV) {
                        String belegung = "";
                        if (v.getBelegung()) {
                            belegung = "BELEGT";
                        }
                        String name = v.getTitel();
                        String daten = v.getArt().toString() + "      •        "+ v.getSemester().getSemesterTyp() + v.getSemester().getJahr();
                        int id = v.getVeranstaltungsID();

                        lehrVList.add(v);
                        Group t = new Group();
                        t.getChildren().add(addGridEntry(belegung, name, daten, id));
                        entries.getChildren().add(t);
                    }
                }
                else {
                    entries.getChildren().remove(r);
                }

            }
        }
        if(lehrVList.size() == 0) {
            String unavInput = courseNameAt.getText();
            Group t = new Group();
            t.getChildren().add(addEmptyGridEntry(unavInput));
            entries.getChildren().add(t);
        }
        }

    @FXML
    private void initialize() {
        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET, "/kursliste/ATONLY?uid=" + ProfilView.accountid, LehrveranstaltungsListe[].class);

        if(response.statusCode == 200) {
            LehrveranstaltungsListe[] lvL = (LehrveranstaltungsListe[]) response.getElement();
            list= new ArrayList<>(Arrays.asList(lvL));
            courseNameAt.getText();
            if(list.stream().anyMatch(e->e != null)) {
                for (LehrveranstaltungsListe x : list) {
                    if (x != null) {
                        String sem = x.getList().get(0).getSemester().getSemesterTyp().toString();
                        int jah = x.getList().get(0).getSemester().getJahr();

                        Group r = new Group();
                        r.getChildren().add(addSemesterGridEntry(sem, jah));
                        entries.getChildren().add(r);

                        List<Lehrveranstaltung> lV = new ArrayList<>();
                        for (Lehrveranstaltung y : x.getList()) {
                            lV.add(y);
                        }
                        for (Lehrveranstaltung v : lV) {
                            String belegung = "";
                            if (v.getBelegung()) {
                                belegung = "BELEGT";
                            }
                            String name = v.getTitel();
                            String daten = v.getArt().toString() + "      •        " + v.getSemester().getSemesterTyp() + v.getSemester().getJahr();
                            int id = v.getVeranstaltungsID();

                            Group t = new Group();
                            t.getChildren().add(addGridEntry(belegung, name, daten, id));
                            entries.getChildren().add(t);
                        }
                    }
                }
            }
                 else{
                    createEmptyVeranstaltungenList();
                }
        }
        //       Übernahme des bisher gesuchten Inputs falls vorhanden
        if(BelegteKursListe.changedInput != null) {
            courseNameAt.setText(BelegteKursListe.changedInput);
            searchCourse(BelegteKursListe.changedInput);
        }
        courseNameAt.textProperty().addListener((ov, v, v1) -> searchCourse(v1));
    }
    //Gridpane für einen Kurs
    public GridPane createGridPane() {
        GridPane gp = new GridPane();

        gp.setPrefSize(1185, 120);
        gp.getColumnConstraints().add(new ColumnConstraints(562));
        gp.getColumnConstraints().add(new ColumnConstraints(562));
        gp.getRowConstraints().add(new RowConstraints(10, 30, 30));
        gp.getRowConstraints().add(new RowConstraints(10, 30, 30));
        gp.getRowConstraints().add(new RowConstraints(10, 30, 30));
        gp.getRowConstraints().add(new RowConstraints(10, 30, 30));
        gp.setPadding(new Insets(0,0,0,60));

          return gp;
    }

    private void createEmptyVeranstaltungenList() {
        GridPane gp = createEmptyPane();
        //Warning sign
        ImageView noodle = new ImageView("/images/noodles.png");
        noodle.setFitHeight(58);
        noodle.setFitWidth(58);
        //Input
        Text n = new Text("Es wurden keine Veranstaltungen gefunden");
        n.getStyleClass().add("falseInput");
        GridPane.setMargin(n, new Insets(0, 0, 110, 20));
        GridPane.setMargin(noodle, new Insets(0, 0, 110, 0));

        //Zum GridPane hinzufügen
        gp.add(noodle, 0, 0);
        gp.add(n, 1, 0);
        entries.getChildren().add(gp);
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

    //Gridpane für Semesterabschnitt
    private GridPane createSemesterGrid() {
        GridPane gp = new GridPane();

        gp.setPrefSize(1185, 56);
        gp.getColumnConstraints().add(new ColumnConstraints(1189));
        gp.getRowConstraints().add(new RowConstraints(28));
        gp.getRowConstraints().add(new RowConstraints(28));

        return gp;
    }

    //Gridpane, falls keine Kurse gefunden wurden
    private GridPane createUnavGridPane() {
        GridPane gp = new GridPane();

        gp.setPrefSize(1185, 120);
        gp.getColumnConstraints().add(new ColumnConstraints(60));
        gp.getColumnConstraints().add(new ColumnConstraints(1129));
        gp.getRowConstraints().add(new RowConstraints(203));
        gp.setPadding(new Insets(0,0,0,60));
            return gp;
        }

    //Kurseintrag
    public GridPane addGridEntry(String bss, String ccc, String daten, int id) {
        GridPane gp = createGridPane();

        //Belegungsstatus erstellen
        Text bS = new Text(bss);
        bS.getStyleClass().add("belegungsStatus");
        bS.setFill(Color.web("#637381"));

        if (bss == "") {
            bS.setText("NICHT BELEGT");
        } else {
            bS.getStyleClass().clear();
            bS.getStyleClass().add("belegt");
        }

        //Kursname
        Text n = new Text(ccc);
        n.getStyleClass().add("courseTitle");

        //Kursdaten
        Text dat = new Text(daten);
        dat.getStyleClass().add("courseData");

        //Kurslink
        Button course = new Button("Zum Kurs");
        course.setTextFill(Color.WHITE);
        course.getStyleClass().add("toCourseBtn");
        course.setPrefSize(133, 48);
        course.setMinHeight(48);
        course.setCursor(Cursor.HAND);
            course.setOnMouseEntered(event -> course.setStyle("-fx-background-color: #374048"));
            course.setOnMouseExited(event -> course.setStyle("-fx-background-color: #637381"));
        course.setOnAction(event -> goToCourse(id));
        gp.setMargin(course, new Insets(0,0,0,300));

        //Alles zum GridPane hinzufügen
        gp.addRow(0, bS);
        gp.addRow(1, n);
        gp.addRow(2, dat);
        gp.addRow(3, new Line(2390, 0, 1326, 0));
        gp.add(course, 1, 1);

            return gp;
        }
    //Nachricht, dass keine Kurse gefunden wurden
    private GridPane addEmptyGridEntry(String input) {
        GridPane gp = createUnavGridPane();
        //Warning sign
        ImageView noodle = new ImageView("/images/noodles.png");
        noodle.setFitHeight(58);
        noodle.setFitWidth(58);
        //Input
        Text n = new Text("Keinen Kurs mit dem Namen: " + input + " gefunden.");
        n.getStyleClass().add("falseInput");
        gp.setMargin(n, new Insets(0,0,110,20));
        gp.setMargin(noodle, new Insets(0,0,110,0));

        //Zum GridPane hinzufügen
        gp.add(noodle, 0, 0);
        gp.add(n, 1,0);

        return gp;
    }

    //Eintrag für Semesterabschnitt
    public GridPane addSemesterGridEntry(String semester, int jahr) {
        GridPane gp = createSemesterGrid();

        //Input
        String sem = semester + " " + jahr;

        Text n = new Text(sem);
        n.getStyleClass().add("falseInput");
        GridPane.setMargin(n, new Insets(0,0,40,40));

        //Zum GridPane hinzufügen
        gp.add(n, 0,1);
        return gp;
    }

    //Wenn Textfeld mit Inhalt angeklickt wird, wird dieser komplett markiert
    @FXML
    void markAll(MouseEvent event) {
        TextField tf = (TextField) event.getSource();
        if (!(tf.getText().isEmpty())) {
            tf.selectAll();
        }
    }

    //Knöpfe reagieren auf Maus
    @FXML
    void reactOnEntry(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #374048;-fx-background-radius: 8px;");

    }

    @FXML
    void reactOnExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #637381;-fx-background-radius: 8px;");
    }
}



