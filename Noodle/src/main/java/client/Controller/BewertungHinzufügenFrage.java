package client.Controller;

import client.Launcher;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;

public class BewertungHinzufügenFrage {

    @FXML TextField frage;
    @FXML VBox wrapper;
    @FXML VBox placeholder;
    public BewertungHinzufügen bewertung;
    List<BewertungHinzufügenAntwort> antworten = new ArrayList<>();

    public void delete() {
        if (bewertung != null) {
            bewertung.delete(wrapper);
        }
    }

    public void delete(VBox element) {
        for (BewertungHinzufügenAntwort antwort: antworten) {
            if (antwort.wrapper == element) {
                antworten.remove(antwort);
                break;
            }
        }
        placeholder.getChildren().remove(element);
    }

    public void add() {
        BewertungHinzufügenAntwort antwort = Launcher.gui.addElement(placeholder, "/fxml/BewertungHinzufügenAntwort.fxml");
        antwort.bewertungHinzufügenFrage = this;
        antworten.add(antwort);
    }

}
