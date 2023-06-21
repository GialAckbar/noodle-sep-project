package client.Controller;

import client.Launcher;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import shared.Termin;
import shared.TerminListe;
import java.util.ArrayList;
import java.util.List;
// Icons made by https://www.flaticon.com/authors/good-ware from https://www.flaticon.com/
public class Kalender_placeholder {


    @FXML private VBox appo_entries;
    public static List<TerminListe> list;
    public static String btnID;
    public static String input;

    @FXML
    private void initialize() {
        List<Termin> term = new ArrayList<>();
            for (TerminListe x : list) {
                if (x != null) {
                    List<Termin> teL = new ArrayList<>(x.getList());
                if(teL.size() != 0) {
                    for (Termin v : teL) {
                        if (v.getHour().equals(btnID)) {
                            String name = v.getTitel();
                            String datum = v.getDay() + "." + v.getMonth() + "." + v.getYear();
                            String time = v.getHour() + ":" + v.getMinute();
                            String lV = v.getLV().getTitel();
                            int id = v.getV_id();

                            term.add(v);
                            Group t = new Group();
                            t.getChildren().add(addGridEntry(name, datum, time, lV, id));
                            appo_entries.getChildren().add(t);
                        }
                    }
                }
                }
            }
            if(term.size() == 0) {
                Group t = new Group();
                if (input == null || input.equals("")) {
                    t.getChildren().add(addEmptyGridEntry2());
                }
                else {
                    t.getChildren().add(addEmptyGridEntry(input));
                }
                appo_entries.getChildren().add(t);
                appo_entries.setAlignment(Pos.CENTER);
            }
    }


    //Gridpane für einen Termin
    private GridPane createGridPane() {
        GridPane gp = new GridPane();

        gp.setPrefSize(336, 190);
        ColumnConstraints c1 = new ColumnConstraints(350);
        c1.setHalignment(HPos.CENTER);
        c1.setHgrow(Priority.ALWAYS);
        gp.getColumnConstraints().add(c1);
        gp.getRowConstraints().add(new RowConstraints(38, 38, 38));
        gp.getRowConstraints().add(new RowConstraints(38, 38, 38));
        gp.getRowConstraints().add(new RowConstraints(38, 38, 38));
        gp.getRowConstraints().add(new RowConstraints(38, 38, 38));
        gp.getRowConstraints().add(new RowConstraints(38, 38, 38));
        gp.getRowConstraints().add(new RowConstraints(38, 38, 38));
        return gp;
    }

    //Termineintrag
    private GridPane addGridEntry(String name,String frist, String zeit, String titel, int id) {
        GridPane gp = createGridPane();

        //Termintitel
        Text n = new Text(name);
        n.getStyleClass().add("courseTitlePopOver");

        //Terminfrist
        Text dat = new Text(frist);
        dat.getStyleClass().add("courseDataPopOver");

        //Terminzeitfrist
        Text zF = new Text(zeit);
        zF.getStyleClass().add("courseDataPopOver");

        //Terminveranstaltung
        Text lV = new Text(titel);
        lV.getStyleClass().add("courseDataPopOver");

        //Kurslink
        Button course = new Button("Zum Kurs");
        course.setTextFill(Color.WHITE);
        course.getStyleClass().add("toCourseBtnPopOver");
        course.setPrefSize(250, 35);
        course.setMinHeight(35);
        course.setCursor(Cursor.HAND);
        course.setOnMouseEntered(event -> course.setStyle("-fx-background-color: #374048"));
        course.setOnMouseExited(event -> course.setStyle("-fx-background-color: #91a2b0"));
        course.setOnAction(event -> goToCourse(id));

        //Linie
        Line l = new Line(-234, 0, 100, 0);
        l.setStrokeWidth(0.3);

        //Alles zum GridPane hinzufügen
        gp.addRow(0, n);
        gp.addRow(1, dat);
        gp.addRow(2, zF);
        gp.addRow(3, lV);
        gp.addRow(4, course);
        gp.addRow(5, l);

        return gp;
    }

    //Nachricht, dass keine Termine gefunden wurden
    private GridPane addEmptyGridEntry(String input) {
        GridPane gp = createGridPane();
        //Warning sign
        ImageView noodle = new ImageView("/images/noodles.png");
        noodle.setFitHeight(58);
        noodle.setFitWidth(58);
        //Input
        Text n = new Text("Kein Termin zu dieser Stunde mit");
        n.getStyleClass().add("falseInputPopOver");
        Text n1 = new Text(" dem Namen: "+ input + " gefunden.");
        n1.getStyleClass().add("falseInputPopOver");

        //Zum GridPane hinzufügen
        gp.add(noodle, 0, 0);
        gp.add(n, 0,1);
        gp.add(n1, 0,2);

        return gp;
    }
    //Nachricht, dass keine Termine gefunden wurden | Mit leerem Input
    private GridPane addEmptyGridEntry2() {
        GridPane gp = createGridPane();
        //Warning sign
        ImageView noodle = new ImageView("/images/noodles.png");
        noodle.setFitHeight(58);
        noodle.setFitWidth(58);
        //Input
        Text n = new Text("Nichts zu sehen...");
        n.getStyleClass().add("falseInputPopOver");
        //Zum GridPane hinzufügen
        gp.add(noodle, 0, 0);
        gp.add(n, 0,1);

        return gp;
    }

    @FXML
    void goToCourse(int id) {
    //Kursansicht Controller die ID des angeklickten Kurses übergeben
        Kursansicht.id = id;
    //Zur Kursansicht wechseln
        Launcher.gui.addToNavigation("/fxml/kursansicht.fxml");
    }
}