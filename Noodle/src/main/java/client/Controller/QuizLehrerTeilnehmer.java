package client.Controller;

import client.Launcher;
import client.RequestHandler;
import client.Response;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import shared.Student;
import shared.quiz.Quiz;
import shared.quiz.Quizstatistik;
import java.util.Comparator;
import java.util.TreeMap;

public class QuizLehrerTeilnehmer {

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

    public static int id = -1;
    TreeMap<Integer, Integer> matrversuche;

    public void initialize() {
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        int kursid = Kursansicht.id;
        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET, "/teilnehmerAnzeigen?quizid=" + QuizLehrerFragen.id + "&kursid=" + kursid, Quizstatistik.class);
        if (response.statusCode == 200) {
            Response response2 = Launcher.requestHandler.request(RequestHandler.Art.GET, "/quizAufrufen?quizid=" + QuizLehrerFragen.id, Quiz.class);

            if (response2.statusCode == 200) {
                Quizstatistik quizstatistik = (Quizstatistik) response.getElement();
                Quiz quiz = (Quiz) response2.getElement();

                double betQuote = (double) quizstatistik.getBeteiligte() / (double) quizstatistik.getTotal() * 100.0;
                beteiligungsquote.setText((int) betQuote + "%");

                double teilQuote = (double) quizstatistik.getBestanden() / (double) quizstatistik.getAbgaben() * 100.0;
                bestehensquote.setText((int) teilQuote + "%");
                txtName.setText("Statistik: " + quiz.getAnzeigename());
                matrversuche = new TreeMap<>(Comparator.comparingInt(integer -> integer));
                for(Student s : quizstatistik.getList()) {
                    if (matrversuche.containsKey(s.getMatrikelnummer())) {
                        matrversuche.put(s.getMatrikelnummer(), matrversuche.get(s.getMatrikelnummer()) + 1);
                    } else matrversuche.put(s.getMatrikelnummer(), 1);
                }
                for(Integer matr : matrversuche.keySet()) {
                    for(Student s : quizstatistik.getList()) {
                        if (s.getMatrikelnummer().equals(matr)) {
                            String name = s.getVorname() + " " + s.getNachname();
                            QuizTeilnehmer teilnehmer = Launcher.gui.addElement(entries,"/fxml/QuizTeilnehmer.fxml");
                            VBox.setMargin(teilnehmer.gridPane, new Insets(0, 0, 0, 49));
                            teilnehmer.name.setText(name);
                            teilnehmer.matrikelnr.setText(Integer.toString(s.getMatrikelnummer()));
                            teilnehmer.versuche.setText(Integer.toString(matrversuche.get(matr)));
                            break;
                        }
                    }
                }
            }
        }
    }

    @FXML
    public void change() {
        Launcher.gui.removeOverNavigation();
        Launcher.gui.addOverNavigation("/fxml/QuizLehrerFragen.fxml");
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