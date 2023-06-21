package client.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class RegisterLehrkraft {
    @FXML
    public TextField lehrstuhl;

    @FXML
    public TextField forschungsgebiet;

    public Register register;

    public void change(){

        register.change();
    }
}
