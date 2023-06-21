package client.Controller;

import client.Launcher;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class DropdownElement {

    @FXML
    Text titel;

    String fxml;

    public void dropdownClick(){
        Launcher.gui.navigation.closeDropdown();
        if(fxml == "/fxml/login.fxml"){
            Launcher.gui.changeState("login");
            return;
        }

        Launcher.gui.addToNavigation(fxml);

    }
}
