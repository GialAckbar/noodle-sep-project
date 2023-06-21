package client.Controller;

import client.Launcher;
import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import shared.quiz.Quiz;
import shared.quiz.QuizAntwort;
import shared.quiz.Quizfrage;
import java.util.ArrayList;
import java.util.List;

public class QuizHinzufügen {

    @FXML TextField titel;
    @FXML TextField Von;
    @FXML TextField Bis;
    @FXML Button add;
    @FXML Button create;
    @FXML Button back;
    @FXML VBox placeholder;
    KursHinzufuegenKategorie_add kursHinzufuegenKategorie_add;
    List<QuizHinzufügenFrage> fragen = new ArrayList<>();
    List<Quizfrage> fragen1 = new ArrayList<>();

    @FXML
    public void create() {
        int id = -1;
        if (!validation()) return;

        Quiz quiz = new Quiz(id, titel.getText(), -1);

        for (QuizHinzufügenFrage frage : fragen) {
            Quizfrage quizfrage = new Quizfrage(frage.frage.getText());
            for (QuizHinzufügenAntwort antwort : frage.quizHinzufügenAntwortList) {
                QuizAntwort quizAntwort = new QuizAntwort(antwort.antwort.getText());
                quizAntwort.setRichtig(antwort.dropdown.getSelectionModel().getSelectedItem().equals("Richtig"));
                quizfrage.addAntwort(quizAntwort);
            }
            quiz.addFrage(quizfrage);
        }
        System.out.println("Create quiz: " + new Gson().toJson(quiz));
        kursHinzufuegenKategorie_add.quiz = quiz;
        back();

    }


    public void resetStyle() {

        titel.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        Von.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        Bis.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");

        for (QuizHinzufügenFrage que : fragen) {
            que.frage.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");

            for (QuizHinzufügenAntwort element : que.quizHinzufügenAntwortList) {
                element.dropdown.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            }
        }
    }

    public boolean validation() {
        resetStyle();
        boolean ret = true;

        if (titel.getText().isEmpty()) {
            System.out.println("Titel is invalid");
            titel.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            ret = false;
        }

        for (QuizHinzufügenFrage que : fragen) {
            if (que.frage.getText().isEmpty()) {
                System.out.println("Frage is invalid");
                que.frage.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
                ret = false;
            }

            for (QuizHinzufügenAntwort element : que.quizHinzufügenAntwortList) {
                if (element.dropdown.getSelectionModel().getSelectedItem() == null) {
                    System.out.println("Combobox is invalid");
                    element.dropdown.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
                    ret = false;
                }
            }

        }

        return ret;
    }

    @FXML
    public void back() {
        Launcher.gui.removeAllOverNavigation();

    }

    @FXML
    public void add() {
        QuizHinzufügenFrage quizHinzufügenFrage = Launcher.gui.addElement(placeholder, "/fxml/QuizHinzufügenFrage.fxml");
        quizHinzufügenFrage.quizHinzufügen = this;
        fragen.add(quizHinzufügenFrage);
    }

    public void change() {

        QuizHinzufügenXML quizHinzufügenXML = Launcher.gui.addOverNavigation("/fxml/QuizHinzufügenXML.fxml");
        quizHinzufügenXML.kursHinzufuegenKategorie_add = kursHinzufuegenKategorie_add;
    }


    public void delete(VBox element) {
        for (QuizHinzufügenFrage qhf : fragen) {
            if (qhf.wrapper == element) {
                fragen.remove(qhf);
                break;
            }
        }
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

    public void loadQuiz(Quiz quiz) {
        System.out.println("loadQuiz:" + new Gson().toJson(quiz));
        if (quiz == null) {
            return;
    }
        else {
            titel.setText(quiz.getAnzeigename());
            Von.setText(quiz.getOpenDateString());
            Bis.setText(quiz.getCloseDateString());
            fragen1 = quiz.getFragen();

            for (Quizfrage q : fragen1) {
                QuizHinzufügenFrage frage = Launcher.gui.addElement(placeholder, "/fxml/QuizHinzufügenFrage.fxml");
                frage.frage.setText(q.getFrage());
                List<QuizHinzufügenAntwort> antworten = new ArrayList<>();
                for (QuizAntwort a : q.getAntworten()) {
                    QuizHinzufügenAntwort antwort = Launcher.gui.addElement(frage.placeholder, "/fxml/QuizHinzufügenAntwort.fxml");
                    if (a.getRichtig()) {
                        antwort.dropdown.setValue("Richtig");
                    }
                    else {
                        antwort.dropdown.setValue("Falsch");
                    }
                    antwort.antwort.setText(a.getAntwort());
                    antworten.add(antwort);
                }
                frage.quizHinzufügenAntwortList = antworten;
                fragen.add(frage);
            }
        }
    }

}