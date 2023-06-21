package client.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import shared.User;

public class AddToDoTeilnehmer {

    @FXML
    Text name, matrikelnummer;

    @FXML
    Button button;

    public int id = -1;
    public User user;
    public boolean belegt = false;

    public void removeButton(){
        button.setVisible(false);
    }

    public void add(){
        if(button.getText().equals("Hinzufügen")){
            button.setText("Entfernen");
            belegt = true;
            button.setStyle("-fx-background-color: transparent;-fx-text-fill: #404040;-fx-border-color:#637381;-fx-border-radius:8px;-fx-font-family: 'Montserrat Regular';");
            return;
        }
        button.setText("Hinzufügen");
        belegt = false;
        button.setStyle("-fx-background-color: #637381;-fx-text-fill: WHITE;-fx-background-radius: 8px;-fx-font-family: 'Montserrat Regular';");

    }

    public void reactOnEntryInverted(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent;-fx-text-fill: #404040;-fx-border-color:#637381;-fx-border-radius:8px;-fx-font-family: 'Montserrat Regular';");
    }

    public void reactOnExitInverted(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #637381;-fx-text-fill: WHITE;-fx-background-radius: 8px;-fx-font-family: 'Montserrat Regular';");

    }

}
