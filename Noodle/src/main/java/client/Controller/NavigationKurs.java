package client.Controller;

import client.Launcher;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class NavigationKurs {

    @FXML
    Text name;

    public int id;

    public void click(){
        System.out.println(id + " clicked");
        Kursansicht.id = id;
        Launcher.gui.addToNavigation("/fxml/kursansicht.fxml");
    }

}
