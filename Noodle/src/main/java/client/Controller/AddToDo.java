package client.Controller;

import client.Launcher;
import client.RequestHandler;
import client.Response;
import com.sun.javafx.scene.control.InputField;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import shared.*;

import javax.xml.stream.events.StartDocument;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class AddToDo {

    @FXML
    TextField aufgabe, suche;

    @FXML
    Text matrikelnummertext;

    @FXML
    VBox placeholder;


    int id = -1;

    Todo loadedTodo = null;


    String fxml = "/fxml/todoHinzufügenNameMatrikel.fxml";


    List<AddToDoTeilnehmer> addToDoTeilnehmerList = new ArrayList<>();

    KursHinzufuegenKategorie_add kursHinzufuegenKategorie_add;


    public void initialize(){
        if(Launcher.userArt == Enums.Current.STUDENT){
            fxml = "/fxml/todoHinzufügenName.fxml";
            matrikelnummertext.setText("");
            matrikelnummertext.setVisible(false);
        }

        addTeilnehmer(Launcher.user);

//        Student temp = new Student("11","asd","asd","","",12345,"",123,"");
//        for(int i = 0; i != 10; i++){
//            addTeilnehmer(temp);
//        }

    }

    public void suche(){
        if(placeholder.getChildren().size() == 0) return;
        if(addToDoTeilnehmerList.size() == 0) return;
        if(suche.getText() == "") return;
        List<Node> nodes = placeholder.getChildren().stream().filter(node ->{
            HBox hbox = (HBox)node;
            if(hbox.getChildren().size() == 0) return false;
            Text text = (Text)hbox.getChildren().get(0);
            System.out.println(text.getText());
            return text.getText().toLowerCase().contains(suche.getText().toLowerCase());
        }).collect(Collectors.toList());


        if(Launcher.userArt != Enums.Current.STUDENT){
            List<Node> nodes2 = placeholder.getChildren().stream().filter(node ->{
                HBox hbox = (HBox)node;
                if(hbox.getChildren().size() == 0) return false;
                Text text = (Text)hbox.getChildren().get(1);
                return text.getText().toLowerCase().contains(suche.getText().toLowerCase());
            }).collect(Collectors.toList());
            nodes.addAll(nodes2);
        }
        nodes = nodes.stream().distinct().collect(Collectors.toList());

        for(Node node: placeholder.getChildren()){
            node.setVisible(false);
            node.setDisable(true);
            node.setManaged(false);
        }

        for(Node node: nodes){
            node.setVisible(true);
            node.setDisable(false);
            node.setManaged(true);
        }


    }

    public void loadTodo(Todo todo){
        if(todo == null) return;
        loadedTodo = todo;
        placeholder.getChildren().clear();
        aufgabe.setText(todo.getAnzeigename());
        id = todo.getId();


        for(User user: todo.getVerantwortliche()){
            addTeilnehmer(user,true);

        }
    }

    public void loadTodo(int veranstaltungsID, Todo todo){
        if(todo == null) return;
        loadedTodo = todo;
        id = todo.getId();
        placeholder.getChildren().clear();
        aufgabe.setText(todo.getAnzeigename());
        System.out.println("LoadTodo: " + veranstaltungsID);

        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET,"/kursteilnehmer/get?courseid=" + veranstaltungsID, Teilnehmerliste.class);

    System.out.println("Response: " + response.statusCode);

        if(response.statusCode == 200){

            List<User> teilnehmer = new ArrayList<>();


            teilnehmer.addAll(((Teilnehmerliste)response.getElement()).getStudentenListe());
            teilnehmer.addAll(((Teilnehmerliste)response.getElement()).getLehrendenListe());

            for(User user: todo.getVerantwortliche()){
                addTeilnehmer(user,true);

                for(int i = 0; i < teilnehmer.size(); i++){
                    if(user.getID() == teilnehmer.get(i).getID()){
                        teilnehmer.remove(i);
                    }
                }
            }

            for(User user: teilnehmer){
                addTeilnehmer(user,false);
            }
        }

    }

    private void addTeilnehmer(User user){
        AddToDoTeilnehmer add = Launcher.gui.addElement(placeholder,fxml);
        addToDoTeilnehmerList.add(add);
        add.id = user.getID();
        add.user = user;
        add.name.setText(user.getVorname() + " " + user.getNachname());
        if(Launcher.userArt == Enums.Current.STUDENT && user instanceof Student) return;
        if(user instanceof Student) {
            add.matrikelnummer.setText(((Student) user).getMatrikelnummer().toString());
        }else{
            add.matrikelnummer.setText("");
        }
    }

    private void addTeilnehmer(User user,boolean belegt){
        AddToDoTeilnehmer add = Launcher.gui.addElement(placeholder,fxml);
        addToDoTeilnehmerList.add(add);
        add.user = user;
        add.id = user.getID();
        add.name.setText(user.getVorname() + " " + user.getNachname());
        if(belegt){
            add.belegt = true;
            add.button.setText("Entfernen");
            add.button.setStyle("-fx-background-color: transparent;-fx-text-fill: #404040;-fx-border-color:#637381;-fx-border-radius:8px;-fx-font-family: 'Montserrat Regular';");
        }

        if(Launcher.userArt == Enums.Current.STUDENT && user instanceof Student) return;
        if(add.matrikelnummer == null) return;
        if(user instanceof Student){
            add.matrikelnummer.setText(((Student) user).getMatrikelnummer().toString());
        }else{
            add.matrikelnummer.setText("");
        }
    }


    public void back(){
        Launcher.gui.removeOverNavigation();
    }

    public void create(){
        List<User> teilnehmer = new ArrayList<>();
        for(AddToDoTeilnehmer a: addToDoTeilnehmerList){
            if(a.belegt){
                teilnehmer.add(a.user);
                System.out.println("Belegt von: " + a.user.getVorname());
            }
        }
        if(id != -1){
            loadedTodo.setVerantwortliche(teilnehmer);
            Response response = Launcher.requestHandler.request(RequestHandler.Art.POST,"/todo/change", loadedTodo);
            System.out.println("Request: " + response.statusCode);
            if(response.statusCode != 200) return;
            back();
            return;
        }

        Todo todo = new Todo(aufgabe.getText(), -1, false,teilnehmer);
        kursHinzufuegenKategorie_add.todo = todo;
        kursHinzufuegenKategorie_add.datei.setText(aufgabe.getText());
        back();
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
