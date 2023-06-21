package client.Controller;

import client.Launcher;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import shared.Lernkarte;
import shared.Reminder;
import shared.Todo;
import shared.quiz.Quiz;

public class KursansichtKategorie {

    @FXML
    VBox placeholder;

    @FXML
    Text name;



    public Kursansicht kursansicht;
    public void addChat(int id){
        KursansichtKategorieDatei kursansichtKategorieDatei = Launcher.gui.addElement(placeholder,"/fxml/kursansicht_kategorieDatei.fxml");
        kursansichtKategorieDatei.kursansichtKategorie = this;
        kursansichtKategorieDatei.chat = id;
        kursansichtKategorieDatei.name.setText("Gruppenchat");
    }

    public void addDatei(String dateiName, int id){
        KursansichtKategorieDatei kursansichtKategorieDatei = Launcher.gui.addElement(placeholder,"/fxml/kursansicht_kategorieDatei.fxml");
        kursansichtKategorieDatei.kursansichtKategorie = this;
        String name = dateiName.replace("\n", " ").replace("\r", " ");
        if(name.length() > 100){
            name = name.substring(0, Math.min(name.length(), 100)) + "...";
        }
        kursansichtKategorieDatei.name.setText("Datei: " + name);
        kursansichtKategorieDatei.filename = dateiName;
        kursansichtKategorieDatei.id = id;
    }

    public void addLernkarte(Lernkarte lernkarte){
        KursansichtKategorieDatei kursansichtKategorieDatei = Launcher.gui.addElement(placeholder,"/fxml/kursansicht_kategorieDatei.fxml");
        kursansichtKategorieDatei.kursansichtKategorie = this;
        String name = lernkarte.getAnzeigename().replace("\n", " ").replace("\r", " ");
        if(name.length() > 100){
            name = name.substring(0, Math.min(name.length(), 100)) + "...";
        }
        kursansichtKategorieDatei.name.setText("Lernkarte: " + name);
        kursansichtKategorieDatei.lernkarte = lernkarte;
    }


    public void addTodo(int kursId,Todo todo){
        KursansichtKategorieDatei kursansichtKategorieDatei = Launcher.gui.addElement(placeholder,"/fxml/kursansicht_kategorieDatei.fxml");
        kursansichtKategorieDatei.kursansichtKategorie = this;
        kursansichtKategorieDatei.todo = todo;
        kursansichtKategorieDatei.kursId = kursId;
        String name = todo.getAnzeigename().replace("\n", " ").replace("\r", " ");
        if(name.length() > 100){
            name = name.substring(0, Math.min(name.length(), 100)) + "...";
        }
        kursansichtKategorieDatei.name.setText("Todo: " + name);
    }

    public void addReminder(Reminder reminder){
        KursansichtKategorieDatei kursansichtKategorieDatei = Launcher.gui.addElement(placeholder,"/fxml/kursansicht_kategorieDatei.fxml");
        kursansichtKategorieDatei.kursansichtKategorie = this;
        kursansichtKategorieDatei.reminder = reminder;
        String name = reminder.getAnzeigename().replace("\n", " ").replace("\r", " ");
        if(name.length() > 100){
            name = name.substring(0, Math.min(name.length(), 100)) + "...";
        }
        kursansichtKategorieDatei.name.setText("Reminder: " + name);
    }

    public void addQuiz(Quiz quiz){
        KursansichtKategorieDatei kursansichtKategorieDatei = Launcher.gui.addElement(placeholder,"/fxml/kursansicht_kategorieDatei.fxml");
        kursansichtKategorieDatei.kursansichtKategorie = this;
        kursansichtKategorieDatei.quiz = quiz;
        String name = quiz.getAnzeigename().replace("\n", " ").replace("\r", " ");
        if(name.length() > 100){
            name = name.substring(0, Math.min(name.length(), 100)) + "...";
        }
        kursansichtKategorieDatei.name.setText("Quiz: " + name);
    }

    public void addBewertung(shared.Bewertung bewertung){
        KursansichtKategorieDatei kursansichtKategorieDatei = Launcher.gui.addElement(placeholder,"/fxml/kursansicht_kategorieDatei.fxml");
        kursansichtKategorieDatei.kursansichtKategorie = this;
        kursansichtKategorieDatei.bewertung = bewertung;
        String name = bewertung.getAnzeigename().replace("\n", " ").replace("\r", " ");
        if(name.length() > 100){
            name = name.substring(0, Math.min(name.length(), 100)) + "...";
        }
        kursansichtKategorieDatei.name.setText("Bewertung: " + name);
    }

    public void addElement(String dateiName){
        KursansichtKategorieDatei kursansichtKategorieDatei = Launcher.gui.addElement(placeholder,"/fxml/kursansicht_kategorieDatei.fxml");
        kursansichtKategorieDatei.kursansichtKategorie = this;
        kursansichtKategorieDatei.name.setText(dateiName);

    }
}
