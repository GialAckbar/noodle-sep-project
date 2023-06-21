package client.Controller;

import client.Launcher;
import client.RequestHandler;
import client.Response;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import shared.quiz.Quiz;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import shared.quiz.QuizAntwort;
import shared.quiz.Quizabgabe;
import shared.quiz.Quizfrage;

public class QuizStudentFragen {

    @FXML Text txtName;
    @FXML Text startDate;
    @FXML Text versuche;
    @FXML Text timerText;
    @FXML VBox entries;
    @FXML ScrollPane scrollPane;
    @FXML Button createButton;
    @FXML Button closeButton;
    List<Quizfrage> fragen = new ArrayList<>();
    LinkedHashMap<QuizStudentFrage, List<QuizStudentAntwort>> hashMap = new LinkedHashMap<>();
    public static int id = -1;
    public static boolean timer = false;
    private static Timeline timerUpdater;

    public void initialize() {
        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET, "/quizAufrufen?quizid=" + id, Quiz.class);

        if (response.statusCode == 200) {
            Quiz quiz = (Quiz) response.getElement();

            txtName.setText("Quiz: " + quiz.getAnzeigename());
            versuche.setText(Integer.toString(quiz.getVersuche() + 1));
            startDate.setText(getCurrentTimeDate());
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            startUpdateTimeLine();
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
                }
            }
        }
    }

    private String getCurrentTimeDate() {
        ZonedDateTime now = ZonedDateTime.now();
        now.withZoneSameInstant(ZoneId.systemDefault());
        String day = (now.getDayOfMonth() < 10) ? "0" + now.getDayOfMonth() : String.valueOf(now.getDayOfMonth());
        String month = (now.getMonthValue() < 10) ? "0" + now.getMonthValue() : String.valueOf(now.getMonthValue());
        int year = now.getYear();
        String hour = (now.getHour() < 10) ? "0" + now.getHour() : String.valueOf(now.getHour());
        String minute = (now.getMinute() < 10) ? "0" + now.getMinute() : String.valueOf(now.getMinute());
        return day + "." + month + "." + year + " | " + hour + ":" + minute + " Uhr";
    }

    @FXML
    public void back(){
        timer = false;
        Launcher.gui.removeOverNavigation();
    }

    @FXML
    public void create() {
        timer = false;
        int i = 0, punkte = 0, total = 0;
        HashMap<Integer, List<Integer>> selected = new HashMap<>();
        for (QuizStudentFrage frage : hashMap.keySet()) {
            boolean fehler = false;
            for (int j = 0; j < hashMap.get(frage).size(); j++) {
                if (fragen.get(i).getAntworten().get(j).getRichtig()) {
                    if (!hashMap.get(frage).get(j).antwort.isSelected()) {
                        fehler = true;
                        frage.frage.setFill(Color.color(1, 0, 0));
                    } else {
                        if (!selected.containsKey(frage.getId())) {
                            selected.put(frage.getId(), new ArrayList<>());
                        }
                        selected.get(frage.getId()).add(hashMap.get(frage).get(j).id);
                    }
                }
                else {
                    if (hashMap.get(frage).get(j).antwort.isSelected()) {
                        fehler = true;
                        frage.frage.setFill(Color.color(1, 0, 0));
                        if (!selected.containsKey(frage.getId())) {
                            selected.put(frage.getId(), new ArrayList<>());
                        }
                        selected.get(frage.getId()).add(hashMap.get(frage).get(j).id);
                    }
                }
                hashMap.get(frage).get(j).antwort.setDisable(true);
            }
            if (!fehler) punkte++;
            total++;
            i++;
        }

        closeButton.setText("Schließen");
        createButton.setDisable(true);

        double prozent = (double) punkte / (double) total * 100;
        Quizabgabe abgabe = new Quizabgabe(selected, prozent >= 50, total, punkte);
        Response response = Launcher.requestHandler.request(RequestHandler.Art.POST, "/bearbeitungSpeichern?quizid=" + id, abgabe);
        if (response != null && response.statusCode == 200) {
            PopUpQuiz popup = Launcher.gui.addPopupQuiz();
            if (prozent >= 50) {
                popup.titel.setText("Quiz bestanden!");
            } else popup.titel.setText("Quiz nicht bestanden!");
            popup.punktzahl.setText(punkte + "/" + total + " Fragen richtig beantwortet (" + ((int) prozent) + "%)");
        } else if (response != null) {
            PopUpBewertung popup = Launcher.gui.addPopupBewertung();
            popup.titel.setText("Einreichung fehlgeschlagen!");
            popup.nachricht.setText("Ein unerwarteter Fehler ist aufgetreten!");
        } else {
            PopUpBewertung popup = Launcher.gui.addPopupBewertung();
            popup.titel.setText("Einreichung fehlgeschlagen!");
            popup.nachricht.setText("Ein unerwarteter Fehler ist vor dem Einreichen aufgetreten!");
        }
    }

    void startUpdateTimeLine() {
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), event -> {
            if (timer) {
                String[] split = timerText.getText().split(":");
                int sec = Integer.parseInt(split[2]);
                int min = Integer.parseInt(split[1]);
                int hour = Integer.parseInt(split[0]);
                if (sec < 59) {
                    if (sec < 9) timerText.setText(split[0] + ":" + split[1] + ":0" + (sec + 1));
                    else timerText.setText(split[0] + ":" + split[1] + ":" + (sec + 1));
                } else {
                    if (min < 59) {
                        if (min < 9) timerText.setText(split[0] + ":0" + (min + 1) + ":00");
                        else timerText.setText(split[0] + ":" + (min + 1) + ":00");
                    } else {
                        if (hour < 9) timerText.setText("0" + (hour + 1) + ":00:00");
                        else timerText.setText((hour + 1) + ":00:00");
                    }
                }
            } else timerUpdater.stop();
        });
        timer = true;
        timerUpdater = new Timeline(keyFrame);
        timerUpdater.setCycleCount(Animation.INDEFINITE);
        timerUpdater.play();
    }

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

    @FXML
    void reactOnEntryWhite(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #637381;-fx-text-fill: WHITE;-fx-background-radius: 8px;");
    }

    @FXML
    void reactOnExitWhite(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent;-fx-text-fill: #404040;-fx-border-color:#637381;-fx-border-radius:8px;");
    }
}