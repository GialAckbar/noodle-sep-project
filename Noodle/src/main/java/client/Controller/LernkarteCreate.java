package client.Controller;

import client.Launcher;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import shared.Lernkarte;
import shared.Validation;

public class LernkarteCreate {

    public KursHinzufuegenKategorie_add kursHinzufuegenKategorie_add;

    @FXML
    TextArea textArea;
    @FXML
    Text text;
    @FXML
    Button save;
    @FXML
    Button drehen;

    Lernkarte lernkarte;

    boolean create = true;
    boolean vorderseite = true;
    public void initialize() {
        create = true;
        text.setText("Lernkarte erstellen - Vorderseite");
        lernkarte = new Lernkarte();
    }

    public void back(){
        Launcher.gui.removeOverNavigation();
    }

    public void save(){
        System.out.println("---------------");
        System.out.println(lernkarte.getAnzeigename());
        System.out.println(lernkarte.getAntwort());

        if(vorderseite)
            lernkarte.setAnzeigename(textArea.getText());
        else
            lernkarte.setAntwort(textArea.getText());
        if(validation()){


            kursHinzufuegenKategorie_add.lernkarte = lernkarte;

            String name = lernkarte.getAnzeigename().replace("\n", " ").replace("\r", " ");
            if(name.length() > 100){
                name = name.substring(0, Math.min(name.length(), 100)) + "...";
            }
            kursHinzufuegenKategorie_add.datei.setText(lernkarte.getAnzeigename());
            back();
            return;
        }

    }

    private void resetStyle(){
        textArea.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        drehen.setStyle("-fx-background-color: transparent;-fx-text-fill: #404040;-fx-border-color:#637381;-fx-border-radius:8px;-fx-font-family: 'Montserrat Regular';");

    }

    private boolean validation(){
        resetStyle();
        boolean ret = true;

        if(lernkarte.getAnzeigename() == null || lernkarte.getAnzeigename().isEmpty()){
            ret = false;
            if(vorderseite)
                textArea.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            else
                drehen.setStyle("-fx-background-color: #637381;-fx-text-fill: WHITE;-fx-background-radius: 8px;-fx-font-family: 'Montserrat Regular';-fx-text-fill: #404040;-fx-border-color:#ff0000;-fx-border-radius:8px;");
        }
        if(lernkarte.getAntwort() == null || lernkarte.getAntwort().isEmpty()){
            ret = false;
            if(vorderseite) {
                drehen.setStyle("-fx-background-color: #637381;-fx-text-fill: WHITE;-fx-background-radius: 8px;-fx-font-family: 'Montserrat Regular';-fx-text-fill: #404040;-fx-border-color:#ff0000;-fx-border-radius:8px;");

            }
            else {
                textArea.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            }

        }
        return ret;
    }

    public void drehen(){
        resetStyle();
        vorderseite = !vorderseite;
        if(vorderseite){
            if(create)
                text.setText("Lernkarte erstellen - Vorderseite");
            else
                text.setText("Lernkarte anzeigen - Vorderseite");
            lernkarte.setAntwort(textArea.getText());
            textArea.setText(lernkarte.getAnzeigename());
            return;
        }
        if(create)
            text.setText("Lernkarte erstellen - Rückseite");
        else
            text.setText("Lernkarte anzeigen - Rückseite");
        lernkarte.setAnzeigename(textArea.getText());
        textArea.setText(lernkarte.getAntwort());
    }

    public void load(Lernkarte lernkarte){
        if(lernkarte == null) return;
        this.lernkarte = lernkarte;
        textArea.setText(lernkarte.getAnzeigename());
    }

    public void disable(){
        create = false;
        text.setText("Lernkarte anzeigen - Vorderseite");
        textArea.setEditable(false);
        save.setVisible(false);
    }
}
