package client.Controller;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class QuizStudentFrage {

    @FXML public VBox wrapper;
    @FXML public Text frage;
    @FXML public VBox placeholder;
    int id = -1;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
