package client.Controller;

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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import shared.FreundesListe;
import shared.Student;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
// Icons made by https://www.flaticon.com/authors/good-ware from https://www.flaticon.com/
public class FreundHinzufuegenAnfragen {

    public List<Student> list;
    @FXML
    public TextField searchBar;
    @FXML
    public VBox entries;
    @FXML
    public Text errorText;
    List<String[]> allUsers;
    List<String[]> allUsers2;

    @FXML
    private void initialize() {
        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET, "/freundesliste/anfragen", FreundesListe.class);
        int code = (response != null) ? response.statusCode : -1;

        if (code == 400) errorText.setText("* Code 400: Es wurden ungültige Daten übergeben!");
        else if (code == 403) errorText.setText("* Code 403: Zugriff verweigert!");
        else if (code == 500) errorText.setText("* Code 500: Serverseitiger Fehler!");
        if (response.statusCode == 200) {
            errorText.setVisible(false);
            FreundesListe fl = (FreundesListe) response.getElement();
            list = fl.getList();

            allUsers = new ArrayList<>();
            for (Student x : list) {
                if (x != null) {
                    String name = x.getVorname() + " " + x.getNachname();
                    String email = x.getEmail();
                    String id = Integer.toString(x.getID());
                    allUsers.add(new String[]{name, email, id});
                }
            }
            allUsers.sort(Comparator.comparing(strings -> strings[0].toLowerCase()));
            if (!allUsers.isEmpty()) {
                showAllUsers(allUsers);
            } else {
                createEmptyRequestList();
            }
        }
    }

    private void searchRequest(String input){
        entries.getChildren().clear();

        allUsers2 = new ArrayList<>();
        for (String[] u : allUsers) {
            if (u[0].toLowerCase().contains(input.toLowerCase()) || u[1].toLowerCase().contains(input.toLowerCase())) {
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
        if (!allUsers2.isEmpty()) {
            showAllUsers(allUsers2);
        } else {
            createEmptyRequestList();
        }
    }

    public GridPane createGridPane() {
        GridPane gp = new GridPane();
        gp.setPrefSize(1600, 120);
        gp.getColumnConstraints().add(new ColumnConstraints(800));
        gp.getColumnConstraints().add(new ColumnConstraints(150));
        gp.getColumnConstraints().add(new ColumnConstraints(100));
        gp.getRowConstraints().add(new RowConstraints(10, 30, 30));
        gp.getRowConstraints().add(new RowConstraints(10, 30, 30));
        gp.setPadding(new Insets(0, 0, 0, 60));
        return gp;
    }

    void showAllUsers(List<String[]> Users){
        for (String[] u : Users) {
            addFriend(u[0], u[1], u[2]);
        }
    }

    void addFriend(String name, String email, String id){
        //Group group = new Group();
        GridPane gridPane = addGridEntry(name, email, id);
        //group.getChildren().add(gridPane);
        entries.getChildren().add(gridPane);
    }

    private GridPane createEmptyPane() {
        GridPane gp = new GridPane();
        gp.setPrefSize(1185, 120);
        gp.getColumnConstraints().add(new ColumnConstraints(60));
        gp.getColumnConstraints().add(new ColumnConstraints(1129));
        gp.getRowConstraints().add(new RowConstraints(203));
        gp.setPadding(new Insets(0, 0, 0, 60));
        return gp;
    }

    private GridPane addGridEntry(String name, String email, String id) {
        GridPane gp = createGridPane();

        Text n = new Text(name);
        n.getStyleClass().add("courseTitle");

        Text e = new Text(email);
        e.getStyleClass().add("courseData");

        Button annehmen = new Button("Hinzufügen");
        ImageView ablehnen = new ImageView("/icon/delete.png");
        ablehnen.setFitWidth(40);
        ablehnen.setFitHeight(40);
        Button ablehnen2 = new Button();
        ablehnen2.setGraphic(ablehnen);
        ablehnen2.setPickOnBounds(true);
        ablehnen2.setPrefSize(40,40);

        annehmen.setTextFill(Color.WHITE);
        annehmen.getStyleClass().add("toCourseBtn");
        annehmen.setPrefSize(110, 30);
        annehmen.setMinHeight(48);
        annehmen.setCursor(Cursor.HAND);
        // entfernen.setOnAction(event -> deleteFriend(id)); Todo: freund hinzufügen aufgabe

        // Grauer Rahmen, um die GridPanes, wenn man mit der Maus drübergeht
        gp.setOnMouseEntered(event -> gp.setStyle("-fx-background-color: #E6E6E6"));
        gp.setOnMouseExited(event -> gp.setStyle("-fx-background-color: transparent"));


        annehmen.setOnAction(event -> confirmRequest(id, gp));
        annehmen.setStyle("-fx-background-color: #637381; -fx-background-radius: 8px");
        annehmen.setOnMouseEntered(event -> annehmen.setStyle("-fx-background-color: #45505a"));
        annehmen.setOnMouseExited(event -> annehmen.setStyle("-fx-background-color: #637381"));

        ablehnen2.setOnAction(event -> denyRequest(id, gp));

        gp.addRow(0, n);
        gp.addRow(1, e);
        gp.add(annehmen, 1, 1);
        gp.add(ablehnen2, 2, 1);

        return gp;
    }

    void confirmRequest(String id, GridPane pane) {
        String args = "?accountid=" + id;
        Response response = Launcher.requestHandler.request(RequestHandler.Art.POST, "/freundesliste/removeRequestaddFriend" + args, args);
        entries.getChildren().remove(pane);
        if (entries.getChildren().isEmpty()) {
            createEmptyRequestList();
        }
        int code = (response != null) ? response.statusCode : -1;

        errorText.setVisible(true);
        if (code == 200 || code == 409) {
            if (code == 200) errorText.setVisible(false);
            else errorText.setText("* Code 409: Nutzer ist bereits im Kurs!");
        } else if (code == 500) errorText.setText("* Code 500: Konnte Nutzer nicht hinzufügen!");
        else errorText.setText("* Unbekannter Fehler aufgetreten!");
    }

    void denyRequest(String id, GridPane pane) {
        String args = "?accountid=" + id;
        Response response = Launcher.requestHandler.request(RequestHandler.Art.DELETE, "/freundesliste/removeRequest" + args);
        entries.getChildren().remove(pane);
        if (entries.getChildren().isEmpty()) {
            createEmptyRequestList();
        }
        int code = (response != null) ? response.statusCode : -1;

        errorText.setVisible(true);
        if (code == 200 || code == 409) {
            if (code == 200) errorText.setVisible(false);
            else errorText.setText("* Code 409: Nutzer ist bereits im Kurs!");
        } else if (code == 500) errorText.setText("* Code 500: Konnte Nutzer nicht hinzufügen!");
        else errorText.setText("* Unbekannter Fehler aufgetreten!");
    }

    private void createEmptyRequestList() {
        GridPane gp = createEmptyPane();
        //Warning sign
        ImageView noodle = new ImageView("/images/noodles.png");
        noodle.setFitHeight(58);
        noodle.setFitWidth(58);
        //Input
        Text n = new Text("Es wurden keine Anfragen gefunden! Warts ab :)");
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

    public void search(ActionEvent actionEvent){
        searchRequest(searchBar.getText());
    }

    public void change(MouseEvent mouseEvent) {
        Launcher.gui.addToNavigation("/fxml/freundesListe_Freunde.fxml");
    }
}
