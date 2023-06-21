package client.Controller;

import client.Launcher;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

    public class QuizStudentLösung {

        @FXML public Text txtName;

        @FXML public Text dateVon;

        @FXML public Text dateBis;

        @FXML public Text anzahl;

        @FXML public VBox entries;

        @FXML
        public void back() {
            Launcher.gui.removeOverNavigation();
        }

    }



