package client.Controller;
import client.Launcher;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;

public class QuizHinzufügenAntwort {

    @FXML VBox wrapper;
    @FXML ComboBox dropdown;
    @FXML TextField antwort;
    public int id = -1;
    public QuizHinzufügenFrage quizHinzufügenFrage;

    @FXML
    public void initialize(){
        dropdown.getItems().addAll(
                "Richtig",
                "Falsch"
        );
    }

    public void delete(){
        quizHinzufügenFrage.delete(wrapper);
    }

    public void up(){
        Launcher.gui.moveElementUp(quizHinzufügenFrage.placeholder,wrapper);
    }

    public void down(){
        Launcher.gui.moveElementDown(quizHinzufügenFrage.placeholder,wrapper);
    }
}

