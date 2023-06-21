package client.Controller;

import client.Launcher;
import javafx.fxml.FXML;
import javafx.scene.text.Text;


public class Registriert {

    @FXML
    Text matrikelnummer;

    public static int matrikel = -1;

    public void initialize() {
        if(matrikel != -1){

            matrikelnummer.setText(String.valueOf(matrikel));
        }
    }

    public void next(){
//        if(matrikel == -1){
//            Launcher.gui.changeState("navigationLehrkraft");
//            return;
//        }
//        Launcher.gui.changeState("navigation");
        Launcher.gui.changeState("twoFactor");
    }
}
