package client.Controller;

import client.Launcher;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class BewertungHinzufügenAntwort {

    @FXML VBox wrapper;
    @FXML TextField antwort;

    public BewertungHinzufügenFrage bewertungHinzufügenFrage;

    public void delete() {
        bewertungHinzufügenFrage.delete(wrapper);
    }

    public void up() {
        Launcher.gui.moveElementUp(bewertungHinzufügenFrage.placeholder, wrapper);
    }

    public void down() {
        Launcher.gui.moveElementDown(bewertungHinzufügenFrage.placeholder, wrapper);
    }
}