package client.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class ChatListEntry {

    @FXML
    public Circle pbCircle;

    @FXML
    public Text pbLetter;

    @FXML
    public Text title;

    @FXML
    public Text chatInfo;

    @FXML
    public Button chatButton;

    @FXML
    public Text lastMessage;

    @FXML
    public Text unreadMessages;

    @FXML
    void reactOnEntry(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #374048;-fx-background-radius: 8px;");
    }

    @FXML
    void reactOnExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #637381;-fx-background-radius: 8px;");
    }

}
