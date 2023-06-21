package client.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;

import java.io.IOException;


public class Veranstaltungen {
    @FXML
    Group placeholder;

    ProfilView profilView;

    public void initialize() throws IOException {
        ScrollPane pane = FXMLLoader.load(getClass().getResource("/fxml/engagedCourseListProfileView.fxml"));
        placeholder.getChildren().add(pane);
    }

    public void change(){
        profilView.change();
    }
}
