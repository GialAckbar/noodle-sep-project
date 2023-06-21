package client.Controller;

import client.Launcher;
import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import shared.Bewertung;
import shared.quiz.QuizAntwort;
import shared.quiz.Quizfrage;
import java.util.ArrayList;
import java.util.List;

public class BewertungHinzufügen {

    @FXML TextField titel;
    @FXML Button add;
    @FXML Button create;
    @FXML Button back;
    @FXML VBox placeholder;
    KursHinzufuegenKategorie_add kursHinzufuegenKategorie_add;
    List<BewertungHinzufügenFrage> fragen = new ArrayList<>();
    List<Quizfrage> loadFragen = new ArrayList<>();

    @FXML
    public void create() {

        if (validation()) {
            shared.Bewertung bewertung = new shared.Bewertung(titel.getText());

            for (BewertungHinzufügenFrage frage : fragen) {
                Quizfrage bewertungsFrage = new Quizfrage(frage.frage.getText());
                for (BewertungHinzufügenAntwort antwort : frage.antworten) {
                    QuizAntwort bewertungsAntwort = new QuizAntwort(antwort.antwort.getText());
                    bewertungsFrage.addAntwort(bewertungsAntwort);
                }
                bewertung.addFrage(bewertungsFrage);
            }
            System.out.println("Create Bewertung: " + new Gson().toJson(bewertung));
            kursHinzufuegenKategorie_add.bewertung = bewertung;
            back();
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
        for (BewertungHinzufügenFrage f : fragen) {
            if (f.frage.getText().isEmpty()) {
                System.out.println("Frage is invalid");
                f.frage.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
                ret = false;
            }
            for (BewertungHinzufügenAntwort a : f.antworten) {
                if (a.antwort.getText().isEmpty()) {
                    System.out.println("Antwort is invalid");
                    a.antwort.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
                    ret = false;
                }
            }
        }
        return ret;
    }

    public void resetStyle() {
        titel.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        for (BewertungHinzufügenFrage f : fragen) {
            f.frage.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");

            for (BewertungHinzufügenAntwort element : f.antworten) {
                element.antwort.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            }
        }
    }

    @FXML
    public void back() {
        Launcher.gui.removeAllOverNavigation();
    }

    @FXML
    public void add() {
        BewertungHinzufügenFrage frage = Launcher.gui.addElement(placeholder, "/fxml/BewertungHinzufügenFrage.fxml");
        frage.bewertung = this;
        fragen.add(frage);
    }

    public void delete(VBox element) {
        for (BewertungHinzufügenFrage f : fragen) {
            if (f.wrapper == element) {
                fragen.remove(f);
                break;
            }
        }
        placeholder.getChildren().remove(element);
    }

    public void load(Bewertung bewertung) {
        if (bewertung != null) {
            System.out.println("loadBewertung:" + new Gson().toJson(bewertung));
            titel.setText(bewertung.getAnzeigename());
            loadFragen = bewertung.getFragen();
            for (Quizfrage q : loadFragen) {
                BewertungHinzufügenFrage frage = Launcher.gui.addElement(placeholder, "/fxml/BewertungHinzufügenFrage.fxml");
                frage.frage.setText(q.getFrage());
                List<BewertungHinzufügenAntwort> antworten = new ArrayList<>();
                for (QuizAntwort a : q.getAntworten()) {
                    BewertungHinzufügenAntwort antwort = Launcher.gui.addElement(frage.placeholder, "/fxml/BewertungHinzufügenAntwort.fxml");
                    antwort.antwort.setText(a.getAntwort());
                    antworten.add(antwort);
                }
                frage.antworten = antworten;
                fragen.add(frage);
            }
        }
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
}