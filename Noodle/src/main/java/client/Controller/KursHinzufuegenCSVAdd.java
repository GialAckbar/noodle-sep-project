package client.Controller;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class KursHinzufuegenCSVAdd {

    @FXML
    Text titel;

    @FXML
    Text art;

    @FXML
    Text time;

    @FXML
    VBox wrapper;
    public KursHinzufuegenCSV kursHinzufuegenCSV;

    public void delete(){
        kursHinzufuegenCSV.delete(wrapper);
    }


}
