package client.Controller;

import client.Launcher;
import client.RequestHandler;
import client.Response;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import shared.Termin;
import shared.TerminListe;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.controlsfx.control.PopOver;

/*TODO  Evtl.: - Date Picker der Buttons nach dem Datum filtert

 */
public class Kalender {

    @FXML private TextField appo_name;
    @FXML private JFXButton c0;
    @FXML private JFXButton c1;
    @FXML private JFXButton c2;
    @FXML private JFXButton c3;
    @FXML private JFXButton c4;
    @FXML private JFXButton c5;
    @FXML private JFXButton c6;
    @FXML private JFXButton c7;
    @FXML private JFXButton c8;
    @FXML private JFXButton c9;
    @FXML private JFXButton c10;
    @FXML private JFXButton c11;
    @FXML private JFXButton c12;
    @FXML private JFXButton c13;
    @FXML private JFXButton c14;
    @FXML private JFXButton c15;
    @FXML private JFXButton c16;
    @FXML private JFXButton c17;
    @FXML private JFXButton c18;
    @FXML private JFXButton c19;
    @FXML private JFXButton c20;
    @FXML private JFXButton c21;
    @FXML private JFXButton c22;
    @FXML private JFXButton c23;
    private List<TerminListe> list;
    JFXButton[] btns;
    List<JFXButton> enabled = new ArrayList<>();


    void searchAppointment(String input) {
        enableBtns();
        Kalender_placeholder.input=input;
        enabled.clear();

        List<Termin> termList = new ArrayList<>();
        for (TerminListe x : list) {
            if (x != null) {
                List<Termin> teL = new ArrayList<>();
                for (Termin y : x.getList()) {
                    if (y.getTitel().toLowerCase().contains(input.toLowerCase()) || y.getLV().getTitel().toLowerCase().contains(input.toLowerCase())) {
                        teL.add(y);
                    }
                }
                if (teL.size() != 0) {
                    termList.addAll(teL);
                    List<JFXButton> term = getTerminBtns(teL);
                    disableBtns(term);
                }
            }
        }
        if (termList.size() == 0) {
            disableAllBtns();
        }
        else if(input.equals("")) {
            enableBtns();
        }
    }

    @FXML
    private void initialize() {
        btns = new JFXButton[] {c0,c1,c2,c3,c4,c5,c6,c7,c8,c9,c10,c11,c12,c13,c14,c15,c16,c17,c18,c19,c20,c21,c22,c23};
        for(int i = 0; i < 24; i++) {
            final int z = i;
            btns[i].setOnAction(event -> {
                try {
                    setUpButtonListener(btns[z].getText());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET, "/terminliste/OWN", TerminListe[].class);

        if(response.statusCode == 200) {
            TerminListe[] tL = (TerminListe[]) response.getElement();
            list = new ArrayList<>(Arrays.asList(tL));
            Kalender_placeholder.list = list;
            List<String> hours = new ArrayList<>();

            for (TerminListe x : list) {
                if (x != null) {
                    List<Termin> teL = new ArrayList<>(x.getList());
                    for (Termin v : teL) {
                        if(!(hours.contains(v.getHour()))) {
                            hours.add(v.getHour());
                        }
                    }
                }
            }
            getBtns(hours);
        }
        appo_name.textProperty().addListener((ov, v, v1) -> searchAppointment(v1));
    }

//Alle Zeitbuttons farblich hervorheben, wenn zu der jeweiligen Stunde ein Termin angesetzt ist
    private void getBtns(List<String> hour) {
        for (int i = 0; i < 24; i++) {
            if(hour.contains(btns[i].getText())) {
                btns[i].setStyle("-fx-background-color: #846A77");
            }
        }
    }

//Deckkraft aller Buttons auf 0.2, welche keine Termine beinhalten, dessen Name oder Lehrveranstaltung zum Input passen
    private void disableBtns(List<JFXButton> btNS) {
        for(JFXButton x : btNS) {
        for(int i = 0; i < 24; i++) {
                if(x.getText().equals(btns[i].getText())) {
                    btns[i].setOpacity(1);
                    enabled.add(btns[i]);
                }
                else
                    if(!enabled.contains(btns[i])) {
                        btns[i].setOpacity(0.2);
                    }
            }
        }
    }

//Deckerkraft aller Buttons auf 0.2
    private void disableAllBtns() {
        for(int i = 0; i < 24; i++) {
            btns[i].setOpacity(0.2);
        }
    }

//Deckkraft aller Buttons auf 1
    private void enableBtns() {
        for(int i = 0; i < 24; i++) {
                btns[i].setOpacity(1);

        }
    }

//Zu allen Zeiten der Termine, die zugehörigen Buttons finden
    private List<JFXButton> getTerminBtns(List<Termin> teL) {
        List<JFXButton> bt = new ArrayList<>();
        for(int i = 0; i < 24; i++) {
            for (Termin t : teL) {
                if (btns[i].getText().equals(t.getHour()) && !bt.contains(t)) {
                    bt.add(btns[i]);
                }
            }
        }
        return bt;
    }

//Allen Buttons eine Funktion zuweisen
    public void setUpButtonListener(String b) throws IOException {
                switch (b) {
                    case "00": Kalender_placeholder.btnID = "00";
                        popUP(c0);
                    break;
                    case "01": Kalender_placeholder.btnID = "01";
                        popUP(c1);
                        break;
                    case "02": Kalender_placeholder.btnID = "02";
                        popUP(c2);
                        break;
                    case "03": Kalender_placeholder.btnID = "03";
                        popUP(c3);
                        break;
                    case "04": Kalender_placeholder.btnID = "04";
                        popUP(c4);
                        break;
                    case "05": Kalender_placeholder.btnID = "05";
                        popUP(c5);
                        break;
                    case "06": Kalender_placeholder.btnID = "06";
                        popUP(c6);
                        break;
                    case "07": Kalender_placeholder.btnID = "07";
                        popUP(c7);
                        break;
                    case "08": Kalender_placeholder.btnID = "08";
                        popUP(c8);
                        break;
                    case "09": Kalender_placeholder.btnID = "09";
                        popUP(c9);
                        break;
                    case "10": Kalender_placeholder.btnID = "10";
                        popUP(c10);
                        break;
                    case "11": Kalender_placeholder.btnID = "11";
                        popUP(c11);
                        break;
                    case "12": Kalender_placeholder.btnID = "12";
                        popUP(c12);
                        break;
                    case "13": Kalender_placeholder.btnID = "13";
                        popUP(c13);
                        break;
                    case "14": Kalender_placeholder.btnID = "14";
                        popUP(c14);
                        break;
                    case "15": Kalender_placeholder.btnID = "15";
                        popUP(c15);
                        break;
                    case "16": Kalender_placeholder.btnID = "16";
                        popUP(c16);
                        break;
                    case "17": Kalender_placeholder.btnID = "17";
                        popUP(c17);
                        break;
                    case "18": Kalender_placeholder.btnID = "18";
                        popUP(c18);
                        break;
                    case "19": Kalender_placeholder.btnID = "19";
                        popUP(c19);
                        break;
                    case "20": Kalender_placeholder.btnID = "20";
                        popUP(c20);
                        break;
                    case "21": Kalender_placeholder.btnID = "21";
                        popUP(c21);
                        break;
                    case "22": Kalender_placeholder.btnID = "22";
                        popUP(c22);
                        break;
                    case "23": Kalender_placeholder.btnID = "23";
                        popUP(c23);
                        break;
                }
    }

//KursPopUp erstellen
    private void popUP(JFXButton j) throws IOException {
        Pane pane = FXMLLoader.load(getClass().getResource("/fxml/calendar_placeholder.fxml"));
        PopOver p = new PopOver();
        p.setArrowLocation(PopOver.ArrowLocation.BOTTOM_CENTER);
        p.setDetachable(false);
        p.setContentNode(pane);
        p.show(j);
    }

    //Wenn Textfeld mit Inhalt angeklickt wird, wird dieser komplett markiert
    @FXML
    void markAll(MouseEvent event) {
        TextField tf = (TextField) event.getSource();
        if (!(tf.getText().isEmpty())) {
            tf.selectAll();
        }
    }

}