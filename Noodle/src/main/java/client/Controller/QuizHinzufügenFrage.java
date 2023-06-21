package client.Controller;

import client.Launcher;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;

public class QuizHinzufügenFrage {

    @FXML TextField frage;
    @FXML VBox wrapper;
    @FXML VBox placeholder;
    public QuizHinzufügen quizHinzufügen;
    public int id = -1;
    List<QuizHinzufügenAntwort> quizHinzufügenAntwortList = new ArrayList<>();

    public void delete () {

            if (quizHinzufügen != null) {
                quizHinzufügen.delete(wrapper);
                return;
            }
        }

    public void delete(VBox element){

        for(QuizHinzufügenAntwort qha: quizHinzufügenAntwortList){
            if(qha.wrapper == element){
                quizHinzufügenAntwortList.remove(qha);
                break;
            }
        }
        placeholder.getChildren().remove(element);
    }

    public void add () {
        QuizHinzufügenAntwort quizHinzufügenAntwort = Launcher.gui.addElement(placeholder, "/fxml/QuizHinzufügenAntwort.fxml");
        quizHinzufügenAntwort.quizHinzufügenFrage = this;
        quizHinzufügenAntwortList.add(quizHinzufügenAntwort);
    }

}

