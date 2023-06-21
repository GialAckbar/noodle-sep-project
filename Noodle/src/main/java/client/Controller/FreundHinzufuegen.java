package client.Controller;

import client.Controller.kurs.BelegteKursListe;
import client.Controller.kurs.KursListe;
import client.Launcher;
import client.RequestHandler;
import client.Response;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import shared.*;

import java.io.IOException;
import java.util.*;
// Icons made by https://www.flaticon.com/authors/good-ware from https://www.flaticon.com/
public class FreundHinzufuegen {


    public List<Student> list;
    @FXML private VBox entries;
    @FXML public TextField searchBar;
    @FXML private Text errorText;
    List<String[]> allUsers;
    List<String[]> allUsers2;

    List<GridPane> allGrids = new ArrayList<>();
    private void searchPerson(String input) {
        entries.getChildren().clear();

        allUsers2 = new ArrayList<>();
        for(String[] u:allUsers){
           if(u[0].toLowerCase().contains(input.toLowerCase()) || u[1].toLowerCase().contains(input.toLowerCase())){
                allUsers2.add(u);
           }
        }
      /*  for (Student x : sorted) {
            if (x != null) {
                String name = x.getVorname() + x.getNachname();
                String email = x.getEmail();
                String id = Integer.toString(x.getID());


                allUsers2.add(new String[]{name, email, id, "remove"});
            }
        }*/
        allUsers2.sort(Comparator.comparing(strings -> strings[0].toLowerCase()));
        if(!allUsers2.isEmpty()){
            showAllUsers(allUsers2);
        }
        else{
            createEmptyFriendList();
        }
    }



    @FXML
    private void initialize() throws IOException {
        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET, "/freundesliste/freunde", FreundesListe.class);
        int code = (response != null) ? response.statusCode : -1;

        if (code == 400) errorText.setText("* Code 400: Es wurden ungültige Daten übergeben!");
        else if (code == 403) errorText.setText("* Code 403: Zugriff verweigert!");
        else if (code == 500) errorText.setText("* Code 500: Serverseitiger Fehler!");
        if(response.statusCode == 200) {
            errorText.setVisible(false);
            FreundesListe fl = (FreundesListe) response.getElement();
            list= fl.getList();

            allUsers = new ArrayList<>();
            for (Student x : list) {
                if (x != null) {
                    String name = x.getVorname() + " " + x.getNachname();
                    String email = x.getEmail();
                    String id = Integer.toString(x.getID());
                    allUsers.add(new String[]{name,email,id,"remove"});

                   // Group t = new Group();
                   // t.getChildren().add(addGridEntry(name, email,id));
                   // entries.getChildren().add(t);
                }
            }
            allUsers.sort(Comparator.comparing(strings -> strings[0].toLowerCase()));
            if(!allUsers.isEmpty()){
                showAllUsers(allUsers);
            }
            else{
                createEmptyFriendList();
            }

        }

    }

    void showAllUsers(List<String[]> Users) {
        for (String[] u : Users) {
            addFriend(u[0], u[1], u[2], u[3]);
        }
    }

    void addFriend(String name, String email, String id, String added) {
        //Group group = new Group();
        GridPane gridPane = addGridEntry(name, email, id,added);
        allGrids.add(gridPane);
        //group.getChildren().add(gridPane);
        entries.getChildren().add(gridPane);
    }

    public GridPane createGridPane() {
        GridPane gp = new GridPane();
        gp.setPrefSize(1600, 120);
        gp.getColumnConstraints().add(new ColumnConstraints(800));
        gp.getColumnConstraints().add(new ColumnConstraints(800));
        gp.getRowConstraints().add(new RowConstraints(10, 30, 30));
        gp.getRowConstraints().add(new RowConstraints(10, 30, 30));
        gp.setPadding(new Insets(0,0,0,60));
        return gp;
    }

    private GridPane createEmptyPane() {
        GridPane gp = new GridPane();
        gp.setPrefSize(1185, 120);
        gp.getColumnConstraints().add(new ColumnConstraints(60));
        gp.getColumnConstraints().add(new ColumnConstraints(1129));
        gp.getRowConstraints().add(new RowConstraints(203));
        gp.setPadding(new Insets(0,0,0,60));
        return gp;
    }

    private GridPane addGridEntry(String name, String email,String id,String added) {
        GridPane gp = createGridPane();

        Text n = new Text(name);
        n.getStyleClass().add("courseTitle");

        Text e = new Text(email);
        e.getStyleClass().add("courseData");
        Button entfernen = new Button();

        entfernen.setTextFill(Color.WHITE);
        entfernen.getStyleClass().add("toCourseBtn");
        entfernen.setPrefSize(110, 30);
        entfernen.setMinHeight(48);
        entfernen.setCursor(Cursor.HAND);
        //entfernen.setOnMouseEntered(event -> entfernen.setStyle("-fx-background-color: #374048"));
        //entfernen.setOnMouseExited(event -> entfernen.setStyle("-fx-background-color: #637381"));

        // Grauer Rahmen, um die GridPanes, wenn man mit der Maus drübergeht
        gp.setOnMouseEntered(event -> gp.setStyle("-fx-background-color: #E6E6E6"));
        gp.setOnMouseExited(event -> gp.setStyle("-fx-background-color: transparent"));
        gp.setOnMouseClicked(event -> showProfile(gp,id));
        gp.setCursor(Cursor.HAND);


        gp.addRow(0, n);
        gp.addRow(1, e);
        gp.add(entfernen, 1, 1);

        if (added.equals("add")) {
            setButtonStatus(entfernen, id, true,gp);
        } else {
            setButtonStatus(entfernen, id, false,gp);
        }
        return gp;
    }

    void setButtonStatus(Button neu, String id, boolean add,GridPane pane) {
        if (add) {
            neu.setText("Anfragen");
            neu.setOnAction(event -> sendRequest(id, neu, pane));
            neu.setStyle("-fx-background-color: #637381; -fx-background-radius: 8px");
            neu.setOnMouseEntered(event -> neu.setStyle("-fx-background-color: #45505a"));
            neu.setOnMouseExited(event -> neu.setStyle("-fx-background-color: #637381"));

        } else {
            neu.setText("Entfernen");
            neu.setOnAction(event -> deleteFromFriend(id, neu,pane));
            neu.setStyle("-fx-background-color: #816363; -fx-background-radius: 8px");
            neu.setOnMouseEntered(event -> neu.setStyle("-fx-background-color: #5a4545"));
            neu.setOnMouseExited(event -> neu.setStyle("-fx-background-color: #816363"));
        }
    }

    void showProfile(GridPane gp,String id){
        for (GridPane gridPane: allGrids){
            if(gridPane.equals(gp)){
                ProfilView.accountid = Integer.parseInt(id);
            }
        }
        Launcher.gui.addOverNavigation("/fxml/EigenesProfil2.fxml");
    }
    void sendRequest(String id, Button neu, GridPane pane) {
        String args = "?accountid=" + id;
        Response response = Launcher.requestHandler.request(RequestHandler.Art.POST,"/freundesliste/addRequest" + args,args);
        entries.getChildren().remove(pane);
        if(entries.getChildren().isEmpty()){
            createEmptyFriendList();
        }

        int code = (response != null) ? response.statusCode : -1;

        errorText.setVisible(true);
        if (code == 200 || code == 409) {
            setButtonStatus(neu, id, false,pane);
            switchStatus(id);
            if (code == 200) errorText.setVisible(false);
            else errorText.setText("* Code 409: Nutzer ist bereits im Kurs!");
        }
        else if (code == 500) errorText.setText("* Code 500: Konnte Nutzer nicht hinzufügen!");
        else errorText.setText("* Unbekannter Fehler aufgetreten!");
    }

    public void deleteFromFriend(String id, Button neu,GridPane pane){
        String args = "?accountid=" + id;
        Response response = Launcher.requestHandler.request(RequestHandler.Art.DELETE,"/freundesliste/removeFriend" + args);

        int code = (response != null) ? response.statusCode : -1;

        errorText.setVisible(true);
        if (code == 200 || code == 409) {
            setButtonStatus(neu, id, true,pane);
            switchStatus(id);
            if (code == 200) errorText.setVisible(false);
            else errorText.setText("* Code 409: Nutzer wurde bereits entfernt!");
        }
        else if (code == 500) errorText.setText("* Code 500: Konnte Nutzer nicht entfernen!");
        else errorText.setText("* Unbekannter Fehler aufgetreten!");
    }

    void switchStatus(String id) {
        for (String[] allUser : allUsers) {
            if (allUser[2].equals(id)) {
                if (allUser[3].equals("remove")) {
                    allUser[3] = "add";
                } else if (allUser[3].equals("add")) {
                    allUser[3] = "remove";
                }
            }
        }
    }

    private GridPane addEmptyGridEntry(String input) {
        GridPane gp = createEmptyPane();
        //Warning sign
        ImageView noodle = new ImageView("/images/noodles.png");
        noodle.setFitHeight(58);
        noodle.setFitWidth(58);
        //Input
        Text n = new Text(" Keine Person mit dem Namen" + input + " gefunden.");
        n.getStyleClass().add("falseInput");
        GridPane.setMargin(n, new Insets(0,0,110,20));
        GridPane.setMargin(noodle, new Insets(0,0,110,0));

        //Zum GridPane hinzufügen
        gp.add(noodle, 0, 0);
        gp.add(n, 1,0);

        return gp;
    }

    private void createEmptyFriendList() {
        GridPane gp = createEmptyPane();
        //Warning sign
        ImageView noodle = new ImageView("/images/noodles.png");
        noodle.setFitHeight(58);
        noodle.setFitWidth(58);
        //Input
        Text n = new Text("Es wurden keine Freunde registriert! Das schaffst du noch :)");
        n.getStyleClass().add("falseInput");
        GridPane.setMargin(n, new Insets(0,0,110,20));
        GridPane.setMargin(noodle, new Insets(0,0,110,0));

        //Zum GridPane hinzufügen
        gp.add(noodle, 0, 0);
        gp.add(n, 1,0);
        entries.getChildren().add(gp);
    }



    @FXML
    void markAll(MouseEvent event) {
        TextField tf = (TextField) event.getSource();
        if (!(tf.getText().isEmpty())) {
            tf.selectAll();
        }
    }

    //Knöpfe reagieren auf Maus
    @FXML
    void reactOnEntry(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #374048;");

    }

    @FXML
    void reactOnExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #637381;");
    }

    @FXML
    public void search(ActionEvent actionEvent) throws IOException {
        searchPerson(searchBar.getText());
    }

    public void change(MouseEvent mouseEvent) {
        Launcher.gui.addToNavigation("/fxml/freundesListe_Anfragen.fxml");
    }
}
