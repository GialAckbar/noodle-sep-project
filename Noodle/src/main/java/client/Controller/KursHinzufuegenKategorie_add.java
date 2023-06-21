package client.Controller;

import client.FileHandler;
import client.Launcher;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import shared.Lernkarte;
import shared.Reminder;
import shared.Todo;
import shared.quiz.Quiz;

import java.io.File;

public class KursHinzufuegenKategorie_add {

    @FXML
    VBox wrapper;
    @FXML
    ComboBox comboBox;
    @FXML
    Button datei;

    File file = null;

    public int id = -1;

    public int dateiid = -1;

    public Reminder reminder;

    public Todo todo;

    public Quiz quiz;

    public shared.Bewertung bewertung;

    int kursid = -1;

    public Lernkarte lernkarte;

    public KursHinzufuegenKategorie kursHinzufuegenKategorie;

    @FXML
    public void initialize(){
        comboBox.getItems().clear();
        comboBox.getItems().addAll(
                "Datei",
                "Termin/Erinnerung",
                "Quiz",
                "Bewertung"
        );
        return;
    }

    public void load(){
        comboBox.getItems().clear();
        if(kursHinzufuegenKategorie.kursBearbeiten != null ){
            if(kursHinzufuegenKategorie.kursBearbeiten.bearbeitenArt == KursBearbeiten.BearbeitenArt.Kurs){
                comboBox.getItems().addAll(
                        "Datei",
                        "Termin/Erinnerung",
                        "Quiz",
                        "Bewertung"
                );
            }else{
                comboBox.getItems().addAll(
                        "Datei",
                        "Todo",
                        "Lernkarte"
                );
            }
        }else if(kursHinzufuegenKategorie.kursHinzufuegen != null ){
            if(kursHinzufuegenKategorie.kursHinzufuegen.createArt == KursHinzufuegen.CreateArt.Kurs){
                comboBox.getItems().addAll(
                        "Datei",
                        "Termin/Erinnerung",
                        "Quiz",
                        "Bewertung"
                );
            }else{
                comboBox.getItems().addAll(
                        "Datei",
                        "Todo",
                        "Lernkarte"
                );
            }
        }

        if(file != null || dateiid != -1){
            comboBox.getSelectionModel().select("Datei");
        }
        if(reminder != null) {
            comboBox.getSelectionModel().select("Termin/Erinnerung");
        }
        if(todo != null){
            comboBox.getSelectionModel().select("Todo");
        }
        if(quiz != null){
            comboBox.getSelectionModel().select("Quiz");
        }
        if(lernkarte != null){
            comboBox.getSelectionModel().select("Lernkarte");
        }
        if(bewertung != null){
            comboBox.getSelectionModel().select("Bewertung");
        }
    }

    public void open(){

        if(comboBox.getSelectionModel().getSelectedItem() == "Datei"){
            file = FileHandler.open();
            if(file != null){
                datei.setText(file.getName());
            }
            return;
        }

        if(comboBox.getSelectionModel().getSelectedItem() == "Termin/Erinnerung"){
            AddReminder addReminder = Launcher.gui.addOverNavigation("/fxml/addreminder.fxml");
            addReminder.kursHinzufuegenKategorie_add = this;
            addReminder.loadReminder(reminder);
            return;
        }

        if(comboBox.getSelectionModel().getSelectedItem() == "Quiz"){
            QuizHinzufügen quizHinzufügen = Launcher.gui.addOverNavigation("/fxml/QuizHinzufügen.fxml");
            quizHinzufügen.kursHinzufuegenKategorie_add = this;
            quizHinzufügen.loadQuiz(quiz);
            return;
        }

        if(comboBox.getSelectionModel().getSelectedItem() == "Todo"){
            AddToDo addToDo = Launcher.gui.addOverNavigation("/fxml/todoHinzufügen.fxml");
            addToDo.kursHinzufuegenKategorie_add = this;
            if(kursid != -1){
                addToDo.loadTodo(kursid,todo);
                return;
            }
            addToDo.loadTodo(todo);
            return;
        }
        if(comboBox.getSelectionModel().getSelectedItem() == "Lernkarte"){
            LernkarteCreate lernkarte = Launcher.gui.addOverNavigation("/fxml/lernkarte.fxml");
            lernkarte.kursHinzufuegenKategorie_add = this;
            lernkarte.load(this.lernkarte);
            return;
        }
        if(comboBox.getSelectionModel().getSelectedItem() == "Bewertung"){
            BewertungHinzufügen bewertungHinzufügen = Launcher.gui.addOverNavigation("/fxml/BewertungHinzufügen.fxml");
            bewertungHinzufügen.kursHinzufuegenKategorie_add = this;
            bewertungHinzufügen.load(bewertung);
            return;
        }


    }

    public void comboBoxChange(){
        file = null;
        reminder = null;
        todo = null;
        switch (comboBox.getSelectionModel().getSelectedItem().toString()){
            case "Lernkarte":
                datei.setText("Lernkarte erstellen");
                break;
            case "Datei":
                datei.setText("Datei auswählen");
                break;
            case "Termin/Erinnerung":
                datei.setText("Termin/Erinnerung erstellen");
                break;
            case "Quiz":
                datei.setText("Quiz erstellen");
                break;
            case "Todo":
                datei.setText("Todo erstellen");
                break;
            case "Bewertung":
                datei.setText("Bewertung erstellen");
                break;
        }
    }

    public void delete(){
        kursHinzufuegenKategorie.delete(wrapper);
    }

    public void up(){
        Launcher.gui.moveElementUp(kursHinzufuegenKategorie.placeholder,wrapper);
    }
    public void down(){
        Launcher.gui.moveElementDown(kursHinzufuegenKategorie.placeholder,wrapper);
    }
}
