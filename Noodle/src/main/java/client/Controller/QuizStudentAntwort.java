package client.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

public class QuizStudentAntwort {

    @FXML public VBox wrapper;
    @FXML public CheckBox antwort;
    public int id = -1;

    public void setId(int id) {
        this.id = id;
    }

    public CheckBox getAntwort() {
        return antwort;
    }

    public void setAntwort(CheckBox antwort) {
        this.antwort = antwort;
    }
}