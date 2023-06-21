package client.Controller;

import client.Launcher;
import client.RequestHandler;
import client.Response;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import shared.quiz.Quiz;
import shared.quiz.QuizAntwort;
import shared.quiz.Quizfrage;
import shared.quiz.Quizstatistik;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuizLehrerFragen {

    @FXML
    private Text txtName;

    @FXML
    private Text beteiligungsquote;

    @FXML
    private Text bestehensquote;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox entries;

    List<Quizfrage> fragen = new ArrayList<>();
    HashMap<QuizStudentFrage, List<QuizStudentAntwort>> hashMap = new HashMap<>();
    public static int id = -1;

    @FXML
    public void change() {
        QuizLehrerTeilnehmer.id = id;
        Launcher.gui.removeOverNavigation();
        Launcher.gui.addOverNavigation("/fxml/QuizLehrerTeilnehmer.fxml");
    }

    @FXML
    public void initialize() {
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET, "/quizLehrerAufrufen?quizid=" + id, Quiz.class);
        int kursid = Kursansicht.id;
        Response preresponse = Launcher.requestHandler.request(RequestHandler.Art.GET, "/teilnehmerAnzeigen?quizid=" + QuizLehrerFragen.id + "&kursid=" + kursid, Quizstatistik.class);
        if (preresponse.statusCode == 200) {
            if (response.statusCode == 200) {
                Quiz quiz = (Quiz) response.getElement();

                Quizstatistik quizstatistik = (Quizstatistik) preresponse.getElement();
                double betQuote = (double) quizstatistik.getBeteiligte() / (double) quizstatistik.getTotal() * 100.0;
                beteiligungsquote.setText((int) betQuote + "%");

                double teilQuote = (double) quizstatistik.getBestanden() / (double) quizstatistik.getAbgaben() * 100.0;
                bestehensquote.setText((int) teilQuote + "%");

                txtName.setText("Statistik: " + quiz.getAnzeigename());
                fragen = quiz.getFragen();
                int i = 1;
                for (Quizfrage q : fragen) {
                    QuizStudentFrage frage = Launcher.gui.addElement(entries,"/fxml/QuizStudentFrage.fxml");
                    frage.setId(q.getFrageid());
                    hashMap.put(frage, new ArrayList<>());
                    frage.frage.setText(i + ") " + q.getFrage());
                    i++;
                    for (QuizAntwort a : q.getAntworten()) {
                        QuizStudentAntwort antwort = Launcher.gui.addElement(frage.placeholder,"/fxml/QuizStudentAntwort.fxml");
                        antwort.setId(a.getId());
                        hashMap.get(frage).add(antwort);
                        antwort.antwort.setText(a.getAntwort());
                        antwort.antwort.setDisable(true);
                        antwort.antwort.setStyle("-fx-opacity: 1.0;");
                        if (a.getRichtig()) {
                            antwort.antwort.setSelected(true);
                        }
                    }
                }
            }
        }
    }

    @FXML
    void close(ActionEvent event) {
        Launcher.gui.removeOverNavigation();
    }

    @FXML
    void reactOnEntry(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #637381;-fx-text-fill: WHITE;-fx-background-radius: 8px;");
    }

    @FXML
    void reactOnExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent;-fx-text-fill: #404040;-fx-border-color:#637381;-fx-border-radius:8px;");
    }

}