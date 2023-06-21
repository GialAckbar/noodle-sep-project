package client.Controller;

import client.Launcher;
import client.RequestHandler;
import client.Response;
import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import shared.Lehrveranstaltung;
import shared.LehrveranstaltungsListe;
import java.util.List;
import java.util.stream.Collectors;

public class Projektgruppen {


    @FXML
    Text titel;

    @FXML
    TextField search;

    @FXML
    VBox placeholder;

    @FXML
    Button button;

    boolean all = false;

    List<Lehrveranstaltung> lehrveranstaltungen;


    public void initialize() {
        clear();
        Response response;
        if(all){
            response = Launcher.requestHandler.request(RequestHandler.Art.GET,"/projektgruppen/get", LehrveranstaltungsListe.class);
        }else{
            response = Launcher.requestHandler.request(RequestHandler.Art.GET,"/projektgruppen/get?onlyOwn=true", LehrveranstaltungsListe.class);
        }

        if(response.statusCode == 200){
            List<Lehrveranstaltung> lehrveranstaltungList = ((LehrveranstaltungsListe)response.getElement()).getList();
            lehrveranstaltungen = lehrveranstaltungList;
            addProjektgruppen(lehrveranstaltungList);
        }

    }


    public void create(){
        KursHinzufuegen.createArt = KursHinzufuegen.CreateArt.Projektgruppe;
        Launcher.gui.addToNavigation("/fxml/kurshinzufügen.fxml");
    }

    public void search(){
        if(lehrveranstaltungen.size() == 0) return;
        addProjektgruppen(lehrveranstaltungen.stream().filter(v -> v.getTitel().toLowerCase().contains(search.getText().toLowerCase())).collect(Collectors.toList()));
    }

    public void clear(){
        placeholder.getChildren().clear();
    }

    public void addProjektgruppen(List<Lehrveranstaltung> list){
        clear();
        for(Lehrveranstaltung lehr: list){
            System.out.println(new Gson().toJson(lehr));
            addProjektgruppe(lehr.getTitel(),lehr.getVeranstaltungsID(),lehr.getBelegung());
        }
    }

    public void addProjektgruppe(String titel, int id, boolean belegt){
        ProjektgruppeElement projektgruppeElement = Launcher.gui.addElement(placeholder,"/fxml/projektgruppenElement.fxml");
        projektgruppeElement.status.setText("");
        if(belegt) projektgruppeElement.status.setText("BELEGT");
        projektgruppeElement.id = id;
        projektgruppeElement.name.setText(titel);
    }

    public void change(){
        all = !all;
        initialize();
        if(all){
            titel.setText("Alle Projektgruppen");
            button.setText("Meine Projektgruppen");
            return;
        }
        titel.setText("Meine Projektgruppen");
        button.setText("Alle Projektgruppen");
    }

    public void reactOnEntry(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #637381;-fx-text-fill: WHITE;-fx-background-radius: 8px;-fx-font-family: 'Montserrat Regular';");
    }

    public void reactOnExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent;-fx-text-fill: #404040;-fx-border-color:#637381;-fx-border-radius:8px;-fx-font-family: 'Montserrat Regular';");
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
