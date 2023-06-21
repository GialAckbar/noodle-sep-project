package client.Controller;

import client.Controller.kurs.KursteilnehmerBearbeiten;
import client.Launcher;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

public class KursansichtZusatz {

    Kursansicht kursansicht;

    @FXML
    Button add;
    public void edit(){
        if(kursansicht.kursArt == Kursansicht.KursArt.Projektgruppe){
            KursBearbeiten.bearbeitenArt = KursBearbeiten.BearbeitenArt.Projektgruppe;

        }else{
            KursBearbeiten.bearbeitenArt = KursBearbeiten.BearbeitenArt.Kurs;
        }
        Launcher.gui.addToNavigation("/fxml/kursbearbeiten.fxml");
    }

    public void addAttendees(){
        KursteilnehmerBearbeiten.open(true);
    }


    public void reactOnEntry(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #637381;-fx-text-fill: WHITE;-fx-background-radius: 8px;-fx-font-family: 'Montserrat Regular';");
    }

    public void reactOnExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent;-fx-text-fill: #404040;-fx-border-color:#637381;-fx-border-radius:8px;-fx-font-family: 'Montserrat Regular';");
    }
}
