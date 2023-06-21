package client.Controller;

import client.Launcher;
import client.RequestHandler;
import client.Response;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import shared.Enums;
import shared.Student;
import shared.Todo;
import shared.User;

import java.util.List;

public class TodoAnzeigen {


    @FXML
    Text titel, aufgabe, matrikelnummertext;

    @FXML
    ComboBox status;

    @FXML
    VBox placeholder;

    String fxml = "/fxml/todoHinzufügenNameMatrikel.fxml";

    Todo todo;
    int kursId = -1;
    public void initialize() {

        status.getItems().addAll(
                "Fertig",
                "Nicht Fertig"
        );
        status.getSelectionModel().select(1);

        if(Launcher.userArt == Enums.Current.STUDENT){
            fxml = "/fxml/todoHinzufügenName.fxml";
            matrikelnummertext.setText("");
            matrikelnummertext.setVisible(false);
        }

    }

    public void statusChanged(){
        if(todo.getId() != -1){
            todo.setIsFinished(status.getSelectionModel().getSelectedItem() == "Fertig");
            Response response = Launcher.requestHandler.request(RequestHandler.Art.POST,"/todo/change", todo);
            System.out.println(response.statusCode);
        }
    }


    public void loadTodo(int kursId, Todo todo){
        this.todo = todo;
        this.kursId = kursId;
        titel.setText("Todo: " + todo.getAnzeigename());
        aufgabe.setText(todo.getAnzeigename());

        if(todo.getIsFinished()) status.getSelectionModel().select(0);

        addTeilnehmer(todo.getVerantwortliche());
    }

    private void addTeilnehmer(List<User> teilnehmer){
        for(User user: teilnehmer){
            addTeilnehmer(user);
        }
    }

    private void addTeilnehmer(User user){
        AddToDoTeilnehmer add = Launcher.gui.addElement(placeholder,fxml);
        add.removeButton();
        add.id = user.getID();
        add.name.setText(user.getVorname() + " " + user.getNachname());
        if(Launcher.userArt == Enums.Current.STUDENT) return;

        if(user instanceof Student){
            add.matrikelnummer.setText(((Student) user).getMatrikelnummer().toString());
        }else{
            add.matrikelnummer.setText("");
        }
    }

    public void edit(){
        back();
        AddToDo edit = Launcher.gui.addOverNavigation("/fxml/todoHinzufügen.fxml");
        edit.loadTodo(kursId, todo);

        edit.aufgabe.setDisable(true);
    }

    public void back(){
        Launcher.gui.removeOverNavigation();
    }


    public void reactOnEntry(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #637381;-fx-text-fill: WHITE;-fx-background-radius: 8px;-fx-font-family: 'Montserrat Regular';");
    }

    public void reactOnExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent;-fx-text-fill: #404040;-fx-border-color:#637381;-fx-border-radius:8px;-fx-font-family: 'Montserrat Regular';");
    }
}
