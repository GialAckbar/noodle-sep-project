package client.Controller;

import client.Launcher;
import client.RequestHandler;
import client.Response;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import shared.Feedback;
import shared.QuizAntMitAnz;
import shared.quiz.QuizAntwort;
import shared.quiz.Quizfrage;
import java.util.ArrayList;
import java.util.List;

public class feedback_stats {

    @FXML private Text title;
    @FXML private VBox entries;


    @FXML
    private void initialize() {
        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET, "/feedback?fbid=" + Bewertung.id, Feedback.class);

        if(response.statusCode == 200) {
            Feedback f = (Feedback) response.getElement();
            title.setText(f.getTitle()); // = Name vom Feedback

            //Alle abgegebenen Antworten der durchgefallenen und bestandenen Studierenden zu einer gemeinsamen Liste kombinieren
            List<QuizAntMitAnz> all = new ArrayList<>();
            for(QuizAntMitAnz x : f.getAntFailed()) {
                if(!all.contains(x)) {
                    all.add(x);
                }
            }
            for(QuizAntMitAnz x : f.getAntPassed()) {
                if(!all.contains(x)) {
                    all.add(x);
                }
            }

            //Zu jeder im Quiz enthaltenen Frage, alle möglichen, mit allen abgegebenen Antworten vergleichen
            // -> Liste mit allen Antwortmöglichkeiten + Liste mit allen abgegeben Antworten der durchgefallenen Studenten
            //    + Liste mit allen abgegebenen Antworten der bestandenen Studenten + Liste mit allen abgegebenen Antworten
            for(Quizfrage x : f.getFragen()) {
                String frage = x.getFrage();
                List<QuizAntwort> moegl = x.getAntworten();
                List<QuizAntMitAnz> ant = new ArrayList<>();
                List<QuizAntMitAnz> antPASS = new ArrayList<>();
                List<QuizAntMitAnz> antFAIL = new ArrayList<>();


                for(QuizAntwort z : moegl) {
                    for(QuizAntMitAnz w : f.getAntPassed()) {
                        if(z.getId() == w.getQA().getId()) {
                            antPASS.add(w);
                        }
                    }
                }
                for(QuizAntwort z : moegl) {
                    for(QuizAntMitAnz w : f.getAntFailed()) {
                        if(z.getId() == w.getQA().getId()) {
                            antFAIL.add(w);
                        }
                    }
                }
                for(QuizAntwort z : moegl) {
                    for(QuizAntMitAnz w : all) {
                        if(z.getId() == w.getQA().getId()) {
                            ant.add(w);
                        }
                    }
                }

                Group t = new Group();
                t.getChildren().add(addGridEntry(frage, ant, moegl, antPASS, antFAIL));
                entries.getChildren().add(t);
            }
        }
    }

// Neuer Statistikeintrag
    public GridPane createGridPane() {
        GridPane gp = new GridPane();

        gp.setPrefSize(1185, 256);
        gp.getColumnConstraints().add(new ColumnConstraints(735));
        gp.getColumnConstraints().add(new ColumnConstraints(440));
        gp.getRowConstraints().add(new RowConstraints(10, 118, 118));
        gp.getRowConstraints().add(new RowConstraints(10, 118, 118));
        gp.getRowConstraints().add(new RowConstraints(10, 20, 20));

        return gp;
    }



    public GridPane addGridEntry(String questionString, List<QuizAntMitAnz> aw, List<QuizAntwort> poss, List<QuizAntMitAnz> awPASS, List<QuizAntMitAnz> awFAIL) {
        GridPane gp = createGridPane();

        //Detaillierte Daten
        GridPane gp3 = new GridPane();
        gp3.setPrefSize(400,220);
        gp3.getColumnConstraints().add(new ColumnConstraints(400));
        gp3.getRowConstraints().add(new RowConstraints(18));
        gp3.getRowConstraints().add(new RowConstraints(202));
        gp.setMargin(gp3, new Insets(150,0,30,20));

        ScrollPane sp = new ScrollPane();
        sp.setPrefSize(400,220);
        gp.setRowSpan(sp, 2);
        sp.getStyleClass().add("paneBG");
        sp.getStyleClass().add("scrollPaneBlurFix");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox v2 = new VBox();
        v2.setPrefSize(425, 200);

        //Neue aktualisierte Liste mit allen abgegebenen Antworten (Anzahl aktualisiert)
        List<QuizAntMitAnz> allNEW = new ArrayList<>();

//Jede Antwortmöglichkeit durchgehen, Anzahl von Abgaben aller, nur bestandener und n. bestandener Studenenden auslesen und eintragen
        for(QuizAntwort x : poss) {
            Group c = new Group();
            GridPane g = new GridPane();
            g.setMargin(c, new Insets(10,0,0,0));
            g.setPrefSize(400, 45);
            g.getColumnConstraints().add(new ColumnConstraints(100));
            g.getColumnConstraints().add(new ColumnConstraints(100));
            g.getColumnConstraints().add(new ColumnConstraints(100));
            g.getColumnConstraints().add(new ColumnConstraints(100));
            g.getRowConstraints().add(new RowConstraints(45));



            Text antwort = new Text(x.getAntwort());
            antwort.getStyleClass().add("fbStatsANSWER");
            GridPane.setHalignment(antwort, HPos.CENTER);
            Text anzahlALL = new Text("0");
            anzahlALL.getStyleClass().add("fbStatsALL");
            GridPane.setHalignment(anzahlALL, HPos.CENTER);
            Text anzahlPASS = new Text("0");
            anzahlPASS.getStyleClass().add("fbStatsPASS");
            GridPane.setHalignment(anzahlPASS, HPos.CENTER);
            Text anzahlFAIL = new Text("0");
            anzahlFAIL.getStyleClass().add("fbStatsFAIL");
            GridPane.setHalignment(anzahlFAIL, HPos.CENTER);


            int i = 0;
            for(QuizAntMitAnz r : aw) {
                if(x.getAntwort().equals(r.getQA().getAntwort())) {
                    antwort.setText(x.getAntwort());
                    antwort.getStyleClass().add("fbStatsANSWER");
                    GridPane.setHalignment(antwort, HPos.CENTER);
                    anzahlALL.setText(String.valueOf(r.getAnzahl()+i));
                    anzahlALL.getStyleClass().add("fbStatsALL");
                    GridPane.setHalignment(anzahlALL, HPos.CENTER);
                    allNEW.add(new QuizAntMitAnz(r.getQA(), Integer.parseInt(anzahlALL.getText())));
                    i++;
                }
            }

            for(QuizAntMitAnz r : awPASS) {
                if (x.getAntwort().equals(r.getQA().getAntwort())) {
                    if (!antwort.getText().equals(x.getAntwort())) {
                        antwort.setText(x.getAntwort());
                        antwort.getStyleClass().add("fbStatsANSWER");
                        GridPane.setHalignment(antwort, HPos.CENTER);
                        anzahlPASS.setText(String.valueOf(r.getAnzahl()));
                        anzahlPASS.getStyleClass().add("fbStatsPASS");
                        GridPane.setHalignment(anzahlPASS, HPos.CENTER);
                    } else {
                        anzahlPASS.setText(String.valueOf(r.getAnzahl()));
                        anzahlPASS.getStyleClass().add("fbStatsPASS");
                        GridPane.setHalignment(anzahlPASS, HPos.CENTER);
                    }
                }
            }

            for (QuizAntMitAnz r : awFAIL) {
                if (x.getAntwort().equals(r.getQA().getAntwort())) {
                    if (!antwort.getText().equals(x.getAntwort())) {
                        antwort.setText(x.getAntwort());
                        antwort.getStyleClass().add("fbStatsANSWER");
                        GridPane.setHalignment(antwort, HPos.CENTER);
                        anzahlFAIL.setText(String.valueOf(r.getAnzahl()));
                        anzahlFAIL.getStyleClass().add("fbStatsFAIL");
                        GridPane.setHalignment(anzahlFAIL, HPos.CENTER);
                    } else {
                        anzahlFAIL.setText(String.valueOf(r.getAnzahl()));
                        anzahlFAIL.getStyleClass().add("fbStatsFAIL");
                        GridPane.setHalignment(anzahlFAIL, HPos.CENTER);
                    }
                }
            }

            g.add(antwort, 0, 0);
            g.add(anzahlALL, 1,0);
            g.add(anzahlPASS, 2,0);
            g.add(anzahlFAIL, 3,0);
            c.getChildren().add(g);
            v2.getChildren().add(c);
        }
        sp.setContent(v2);


        //Grafik erstellen
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> bc = new BarChart<>(xAxis, yAxis);
        bc.setLegendSide(Side.BOTTOM);
        gp.setRowSpan(bc, 3);
        bc.setTitle(questionString);
        xAxis.setLabel("Antwort");
        yAxis.setLabel("Anzahl");
        XYChart.Series z = new XYChart.Series();
        z.setName("Alle");

            for (QuizAntMitAnz x : allNEW) {
                z.getData().add(new XYChart.Data(x.getQA().getAntwort(), x.getAnzahl()));
            }


        XYChart.Series w = new XYChart.Series();
        w.setName("Bestanden");

        for (QuizAntMitAnz x : awPASS) {
            w.getData().add(new XYChart.Data(x.getQA().getAntwort(), x.getAnzahl()));
        }

        XYChart.Series u = new XYChart.Series();
        u.setName("Nicht Bestanden");

        for (QuizAntMitAnz x : awFAIL) {
            u.getData().add(new XYChart.Data(x.getQA().getAntwort(), x.getAnzahl()));
        }

        bc.getData().addAll(z, w, u);

// Überschriften für die Detaillierteren Daten
        GridPane gp2 = new GridPane();
        gp2.setPrefSize(400, 15);
        gp2.getColumnConstraints().add(new ColumnConstraints(100));
        gp2.getColumnConstraints().add(new ColumnConstraints(100));
        gp2.getColumnConstraints().add(new ColumnConstraints(100));
        gp2.getColumnConstraints().add(new ColumnConstraints(100));
        gp2.getRowConstraints().add(new RowConstraints(15));
        Text a = new Text("Antwort");
        Text all = new Text("Alle");
        Text b = new Text("Bestanden");
        Text nB = new Text("Nicht Best.");
        a.getStyleClass().add("fbBolds");
        a.setUnderline(true);
        all.getStyleClass().add("fbBolds");
        all.setUnderline(true);
        b.getStyleClass().add("fbBolds");
        b.setUnderline(true);
        nB.getStyleClass().add("fbBolds");
        nB.setUnderline(true);
        GridPane.setHalignment(a, HPos.CENTER);
        GridPane.setHalignment(all, HPos.CENTER);
        GridPane.setHalignment(b, HPos.CENTER);
        GridPane.setHalignment(nB, HPos.CENTER);


        gp2.add(a, 0,0);
        gp2.add(all, 1,0);
        gp2.add(b,2,0);
        gp2.add(nB,3,0);

        gp3.add(gp2, 0,0);
        gp3.add(sp,0,1);


// Trennlinie zwischen Einträgen
        Line l = new Line(2191, 0, 1074, 0);
        l.setStrokeWidth(0.3);
        gp.setMargin(l, new Insets(80, 0,0,25));

        //Alles zum GridPane hinzufügen
        gp.add(bc, 0,0);
        gp.add(gp3, 1,0);
        gp.add(l, 0,2);
        return gp;
    }
}
