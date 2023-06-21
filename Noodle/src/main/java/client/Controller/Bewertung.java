package client.Controller;

import client.Launcher;
import client.RequestHandler;
import client.Response;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import shared.quiz.QuizAntwort;
import shared.quiz.Quizfrage;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Bewertung {

    public static int id = -1;

    @FXML private Text txtName;
    @FXML private Text startDate;
    @FXML private Text progress;
    @FXML private Text numberPassed;
    @FXML private Text passed;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox entries;
    @FXML private Button closeButton;
    @FXML private Button submitButton;
    LinkedHashMap<QuizStudentFrage, List<QuizStudentAntwort>> hashMap = new LinkedHashMap<>();

    @FXML
    void initialize() {
        String args = "?bewertungid=" + id + "&courseid=" + Kursansicht.id;
        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET, "/bewertung/load" + args, shared.Bewertung.class);

        if (response.statusCode == 200) {
            shared.Bewertung bewertung = (shared.Bewertung) response.getElement();

            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            txtName.setText("Bewertung: " + bewertung.getAnzeigename());
            startDate.setText(getCurrentTimeDate());
            progress.setText(bewertung.getAccomplished() + " / " + bewertung.getTotal());
            numberPassed.setText(Integer.toString(bewertung.getPassed()));
            double calc = (double) bewertung.getTotal() / (double) bewertung.getPassed();
            if (calc > 2) {
                passed.setText("Nein");
            } else {
                passed.setText("Ja");
            }
            List<Quizfrage> fragen = bewertung.getFragen();
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
                    antwort.antwort.setOnAction(event -> {
                        for (QuizStudentAntwort compare : hashMap.get(frage)) {
                            if (compare.antwort != antwort.antwort) {
                                compare.antwort.setSelected(false);
                            }
                        }
                    });
                }
            }
        }
    }

    @FXML
    void submit(ActionEvent event) {
        HashMap<Integer, Integer> selected = new HashMap<>();
        for (QuizStudentFrage frage : hashMap.keySet()) {
            for (int j = 0; j < hashMap.get(frage).size(); j++) {
                CheckBox antwort = hashMap.get(frage).get(j).antwort;
                antwort.setDisable(true);
                if (antwort.isSelected()) {
                    selected.put(frage.getId(), hashMap.get(frage).get(j).id);
                }
            }
        }

        PopUpBewertung popup = Launcher.gui.addPopupBewertung();
        closeButton.setText("Schließen");
        submitButton.setDisable(true);

        String args = "?bewertungid=" + id + "&courseid=" + Kursansicht.id;
        Response response = Launcher.requestHandler.request(RequestHandler.Art.POST, "/bewertung" + args, selected);
        if (response != null && response.statusCode == 200) {
            popup.titel.setText("Bewertung eingereicht");
            popup.nachricht.setText("Vielen Dank für die Einreichung deiner Bewertung!");
        } else if (response != null) {
            popup.titel.setText("Einreichung fehlgeschlagen!");
            popup.nachricht.setText("Ein unerwarteter Fehler ist aufgetreten!");
        } else {
            popup.titel.setText("Einreichung fehlgeschlagen!");
            popup.nachricht.setText("Ein unerwarteter Fehler ist vor dem Einreichen aufgetreten!");
        }
    }

    @FXML
    void back(ActionEvent event) {
        Launcher.gui.removeOverNavigation();
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
    void reactOnEntry(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #374048;-fx-background-radius: 8px;");
    }

    @FXML
    void reactOnEntryWhite(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #637381;-fx-text-fill: WHITE;-fx-background-radius: 8px;");
    }

    @FXML
    void reactOnExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #637381;-fx-background-radius: 8px;");
    }

    @FXML
    void reactOnExitWhite(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent;-fx-text-fill: #404040;-fx-border-color:#637381;-fx-border-radius:8px;");
    }


}
