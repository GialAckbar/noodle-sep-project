package client.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class RegisterStudent {


    @FXML
    public TextField studienfach;

    public Register register;

    public void change(){

        register.change();
    }
}
