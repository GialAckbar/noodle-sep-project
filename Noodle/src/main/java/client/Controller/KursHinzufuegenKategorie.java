package client.Controller;

import client.Launcher;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import shared.*;
import shared.quiz.Quiz;

import java.util.ArrayList;
import java.util.List;


public class KursHinzufuegenKategorie {

    @FXML
    TextField name;

    @FXML
    VBox wrapper;

    @FXML
    VBox placeholder;

    public KursHinzufuegen kursHinzufuegen;

    public KursBearbeiten kursBearbeiten;

    public int id = -1;

    List<KursHinzufuegenKategorie_add> kursHinzufuegenKategorie_addList = new ArrayList<>();;

    public void up(){


        if(kursHinzufuegen != null){
            Launcher.gui.moveElementUp(kursHinzufuegen.placeholder,wrapper);
            return;
        }

        Launcher.gui.moveElementUp(kursBearbeiten.placeholder,wrapper);
    }

    public void down(){

        if(kursHinzufuegen != null){
            Launcher.gui.moveElementDown(kursHinzufuegen.placeholder,wrapper);
            return;
        }
        Launcher.gui.moveElementDown(kursBearbeiten.placeholder,wrapper);
    }

    public void delete(){

        if(kursHinzufuegen != null){
            kursHinzufuegen.deleteKategorie(wrapper);
            return;
        }
        kursBearbeiten.deleteKategorie(wrapper);
    }

    public void delete(VBox element){

        for(KursHinzufuegenKategorie_add khkadd: kursHinzufuegenKategorie_addList){
            if(khkadd.wrapper == element){
                kursHinzufuegenKategorie_addList.remove(khkadd);
                break;
            }
        }
        placeholder.getChildren().remove(element);
    }

    public void add(){
        KursHinzufuegenKategorie_add kursHinzufuegenKategorie_add = Launcher.gui.addElement(placeholder,"/fxml/kurshinzufügen_kategorie_add.fxml");
        if(KursHinzufuegen.createArt == KursHinzufuegen.CreateArt.Projektgruppe || KursBearbeiten.bearbeitenArt == KursBearbeiten.BearbeitenArt.Projektgruppe){
            kursHinzufuegenKategorie_add.comboBox.getItems().clear();
            kursHinzufuegenKategorie_add.comboBox.getItems().addAll(
                    "Datei",
                    "Todo",
                    "Lernkarte"
            );
        }
        kursHinzufuegenKategorie_add.kursHinzufuegenKategorie = this;
        kursHinzufuegenKategorie_addList.add(kursHinzufuegenKategorie_add);
        kursHinzufuegenKategorie_add.load();
    }

    public void add(KategorieDatei kategorieDatei){

        KursHinzufuegenKategorie_add kursHinzufuegenKategorie_add = Launcher.gui.addElement(placeholder,"/fxml/kurshinzufügen_kategorie_add.fxml");
        kursHinzufuegenKategorie_add.kursHinzufuegenKategorie = this;
        kursHinzufuegenKategorie_add.comboBox.getSelectionModel().select("Datei");
        kursHinzufuegenKategorie_add.datei.setText(kategorieDatei.getAnzeigename());
        kursHinzufuegenKategorie_add.datei.setDisable(true);
        kursHinzufuegenKategorie_add.comboBox.setDisable(true);
        kursHinzufuegenKategorie_add.id = kategorieDatei.getId();
        kursHinzufuegenKategorie_add.dateiid = kategorieDatei.getDateiid();
        kursHinzufuegenKategorie_addList.add(kursHinzufuegenKategorie_add);
        kursHinzufuegenKategorie_add.load();
    }

    public void add(Lernkarte lernkarte){
        KursHinzufuegenKategorie_add kursHinzufuegenKategorie_add = Launcher.gui.addElement(placeholder,"/fxml/kurshinzufügen_kategorie_add.fxml");
        kursHinzufuegenKategorie_add.kursHinzufuegenKategorie = this;
        kursHinzufuegenKategorie_add.comboBox.getSelectionModel().select("Lernkarte");
        kursHinzufuegenKategorie_add.datei.setText(lernkarte.getAnzeigename());
        kursHinzufuegenKategorie_add.lernkarte = lernkarte;
        kursHinzufuegenKategorie_add.datei.setDisable(true);
        kursHinzufuegenKategorie_add.comboBox.setDisable(true);
        kursHinzufuegenKategorie_addList.add(kursHinzufuegenKategorie_add);
        kursHinzufuegenKategorie_add.load();
    }

    public void add(Reminder reminder){

        KursHinzufuegenKategorie_add kursHinzufuegenKategorie_add = Launcher.gui.addElement(placeholder,"/fxml/kurshinzufügen_kategorie_add.fxml");
        kursHinzufuegenKategorie_add.kursHinzufuegenKategorie = this;
        kursHinzufuegenKategorie_add.comboBox.getSelectionModel().select("Termin/Erinnerung");
        kursHinzufuegenKategorie_add.datei.setText(reminder.getAnzeigename());
        kursHinzufuegenKategorie_add.id = reminder.getId();
        kursHinzufuegenKategorie_add.reminder = reminder;
        kursHinzufuegenKategorie_addList.add(kursHinzufuegenKategorie_add);
        kursHinzufuegenKategorie_add.load();
    }

    public void add(Quiz quiz){

        KursHinzufuegenKategorie_add kursHinzufuegenKategorie_add = Launcher.gui.addElement(placeholder,"/fxml/kurshinzufügen_kategorie_add.fxml");
        kursHinzufuegenKategorie_add.kursHinzufuegenKategorie = this;
        kursHinzufuegenKategorie_add.comboBox.getSelectionModel().select("Quiz");
        kursHinzufuegenKategorie_add.datei.setText(quiz.getAnzeigename());
        kursHinzufuegenKategorie_add.datei.setDisable(true);
        kursHinzufuegenKategorie_add.comboBox.setDisable(true);
        kursHinzufuegenKategorie_add.id = quiz.getId();
        kursHinzufuegenKategorie_add.quiz = quiz;
        kursHinzufuegenKategorie_addList.add(kursHinzufuegenKategorie_add);
        kursHinzufuegenKategorie_add.load();
    }

    public void add(Todo todo){

        KursHinzufuegenKategorie_add kursHinzufuegenKategorie_add = Launcher.gui.addElement(placeholder,"/fxml/kurshinzufügen_kategorie_add.fxml");
        System.out.println("aaaaa" + this);
        kursHinzufuegenKategorie_add.kursHinzufuegenKategorie = this;
        System.out.println("aaaaa2" + kursHinzufuegenKategorie_add.kursHinzufuegenKategorie);
        kursHinzufuegenKategorie_add.comboBox.getSelectionModel().select("Todo");
        kursHinzufuegenKategorie_add.datei.setText(todo.getAnzeigename());
        kursHinzufuegenKategorie_add.id = todo.getId();
        kursHinzufuegenKategorie_add.todo = todo;
        kursHinzufuegenKategorie_add.datei.setDisable(true);
        kursHinzufuegenKategorie_add.comboBox.setDisable(true);
        kursHinzufuegenKategorie_addList.add(kursHinzufuegenKategorie_add);
        kursHinzufuegenKategorie_add.load();
    }

    public void add(int kursid, Todo todo){

        KursHinzufuegenKategorie_add kursHinzufuegenKategorie_add = Launcher.gui.addElement(placeholder,"/fxml/kurshinzufügen_kategorie_add.fxml");
        System.out.println("aaaaa" + this);
        kursHinzufuegenKategorie_add.kursHinzufuegenKategorie = this;
        System.out.println("aaaaa2" + kursHinzufuegenKategorie_add.kursHinzufuegenKategorie);
        kursHinzufuegenKategorie_add.comboBox.getSelectionModel().select("Todo");
        kursHinzufuegenKategorie_add.datei.setText(todo.getAnzeigename());
        kursHinzufuegenKategorie_add.id = todo.getId();
        kursHinzufuegenKategorie_add.todo = todo;
        kursHinzufuegenKategorie_add.kursid = kursid;
        kursHinzufuegenKategorie_add.datei.setDisable(true);
        kursHinzufuegenKategorie_add.comboBox.setDisable(true);
        kursHinzufuegenKategorie_addList.add(kursHinzufuegenKategorie_add);
        kursHinzufuegenKategorie_add.load();

    }

    public void add(shared.Bewertung bewertung){

        KursHinzufuegenKategorie_add kursHinzufuegenKategorie_add = Launcher.gui.addElement(placeholder,"/fxml/kurshinzufügen_kategorie_add.fxml");
        kursHinzufuegenKategorie_add.kursHinzufuegenKategorie = this;
        kursHinzufuegenKategorie_add.comboBox.getSelectionModel().select("Bewertung");
        kursHinzufuegenKategorie_add.datei.setText(bewertung.getAnzeigename());
        kursHinzufuegenKategorie_add.datei.setDisable(true);
        kursHinzufuegenKategorie_add.comboBox.setDisable(true);
        kursHinzufuegenKategorie_add.id = bewertung.getId();
        kursHinzufuegenKategorie_add.bewertung = bewertung;
        kursHinzufuegenKategorie_addList.add(kursHinzufuegenKategorie_add);
        kursHinzufuegenKategorie_add.load();
    }
}
