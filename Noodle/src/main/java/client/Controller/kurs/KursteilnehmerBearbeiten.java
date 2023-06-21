package client.Controller.kurs;

import client.Controller.Kursansicht;
import client.Launcher;
import client.RequestHandler;
import client.Response;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import shared.Lehrende;
import shared.Student;
import shared.Teilnehmerliste;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class KursteilnehmerBearbeiten {

    @FXML private Text kursname;
    @FXML private Text errorText;
    @FXML private VBox availableUsers;
    @FXML private TextField searchBar;
    List<String[]> allUsers;
    List<String[]> currentList;
    private static boolean fromAnsicht = false;

    @FXML
    void initialize() throws IOException {

        String args = "?courseid=" + Kursansicht.id;
        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET,"/kursteilnehmer/getAll" + args, Teilnehmerliste[].class);
        int code = (response != null) ? response.statusCode : -1;

        if (code == 400) errorText.setText("* Code 400: Es wurden ungültige Daten übergeben!");
        else if (code == 403) errorText.setText("* Code 403: Zugriff verweigert!");
        else if (code == 500) errorText.setText("* Code 500: Serverseitiger Fehler!");

        else if (code == 200) {
            // Kein Fehler, also errorText ausblenden
            errorText.setVisible(false);

            // Alle Listen aus der Response laden
            Teilnehmerliste[] userLists = (Teilnehmerliste[]) response.getElement();
            Teilnehmerliste userListAdded = userLists[0];
            Teilnehmerliste userListNot = userLists[1];
            List<Lehrende> addedTeachers = userListAdded.getLehrendenListe();
            List<Student> addedStudents = userListAdded.getStudentenListe();
            List<Lehrende> notAddedTeachers = userListNot.getLehrendenListe();
            List<Student> notAddedStudents = userListNot.getStudentenListe();
            String courseName = userListAdded.getCourseName();
            kursname.setText("Nutzer in \"" + courseName + "\" bearbeiten"); // Name des Kurses

            // Alle Listen zu einer machen
            allUsers = new ArrayList<>();
            for (Lehrende l : addedTeachers) {
                if (!(l.getID() == Launcher.useridMitToken.getUserid())) { // Client soll nicht in Liste erscheinen
                    allUsers.add(new String[] {l.getVorname() + " " + l.getNachname(), "-", Integer.toString(l.getID()), "remove"});
                }
            }
            for (Lehrende l : notAddedTeachers) {
                if (!(l.getID() == Launcher.useridMitToken.getUserid())) {
                    allUsers.add(new String[] {l.getVorname() + " " + l.getNachname(), "-", Integer.toString(l.getID()), "add"});
                }
            }
            for (Student s : addedStudents) {
                allUsers.add(new String[] {s.getVorname() + " " + s.getNachname(), String.valueOf(s.getMatrikelnummer()), String.valueOf(s.getID()), "remove"});
            }
            for (Student s : notAddedStudents) {
                allUsers.add(new String[] {s.getVorname() + " " + s.getNachname(), String.valueOf(s.getMatrikelnummer()), String.valueOf(s.getID()), "add"});
            }

            // Neue Liste nach Alphabet sortieren
            allUsers.sort(Comparator.comparing(strings -> strings[0].toLowerCase()));

            // Liste dem Client anzeigen lassen
            currentList = new ArrayList<>(allUsers);
            showAllUsers(currentList);

            // Funktion für die Suchleiste
            searchBar.textProperty().addListener((ov, v, v1) -> {
                try { searchBarFilter(v1); } catch (IOException e) { e.printStackTrace(); }
            });
        }
    }

    void showAllUsers(List<String[]> Users) throws IOException {
        for (String[] u : Users) {
            addMember(u[0], u[1], u[2], u[3]);
        }
    }

    void addMember(String name, String matrikel, String id, String added) throws IOException {
        Group group = new Group();
        GridPane gridPane = createGridPane(name, matrikel, id, added);
        group.getChildren().add(gridPane);
        availableUsers.getChildren().add(group);
        VBox.setMargin(group, new Insets(15, 0, 0, 10)); //Abstand der Namen
    }

    GridPane createGridPane(String name, String matrikel, String id, String added) throws IOException {
        // Lade GridPane Vorlage
        GridPane gridPane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/kursteilnehmerEdit_grid.fxml")));

        //Name, Matrikel-Nr., Button in die einzelnen Constraints
        Text nameText = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/kursteilnehmerEdit_text.fxml")));
        nameText.setText(name);
        gridPane.add(nameText, 0, 0);

        Text matrikelText = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/kursteilnehmerEdit_text.fxml")));
        matrikelText.setText(matrikel);
        gridPane.add(matrikelText, 1, 0);

        Button addButton = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/kursteilnehmerEdit_button.fxml")));
        if (added.equals("add")) {
            setButtonStatus(addButton, id, true);
        } else {
            setButtonStatus(addButton, id, false);
        }
        gridPane.add(addButton, 2, 0);

        // Grauer Rahmen, um die GridPanes, wenn man mit der Maus drübergeht
        gridPane.setOnMouseEntered(event -> gridPane.setStyle("-fx-background-color: #E6E6E6"));
        gridPane.setOnMouseExited(event -> gridPane.setStyle("-fx-background-color: transparent"));

        return gridPane;
    }

    void addToCourse(String id, Button neu) {
        int target = Integer.parseInt(id);
        String args = "?courseid=" + Kursansicht.id;
        Response response = Launcher.requestHandler.request(RequestHandler.Art.POST,"/kursteilnehmer/add" + args, target);
        int code = (response != null) ? response.statusCode : -1;

        errorText.setVisible(true);
        if (code == 200 || code == 409 || code == 304) {
            setButtonStatus(neu, id, false);
            switchStatus(id);
            if (code == 200) errorText.setVisible(false);
            else if (code == 409) errorText.setText("* Code 409: Nutzer ist bereits im Kurs!");
            else errorText.setText("* Code 304: Konnte Nutzer nicht dem Chat hinzufügen!");
        }
        else if (code == 500) errorText.setText("* Code 500: Konnte Nutzer nicht hinzufügen!");
        else errorText.setText("* Unbekannter Fehler aufgetreten!");
    }

    void removeFromCourse(String id, Button neu) {
        String args = "?courseid=" + Kursansicht.id + "&targetid=" + id;
        Response response = Launcher.requestHandler.request(RequestHandler.Art.DELETE,"/kursteilnehmer/remove" + args);
        int code = (response != null) ? response.statusCode : -1;

        errorText.setVisible(true);
        if (code == 200 || code == 409 || code == 304) {
            setButtonStatus(neu, id, true);
            switchStatus(id);
            if (code == 200) errorText.setVisible(false);
            else if (code == 409) errorText.setText("* Code 409: Nutzer wurde bereits entfernt!");
            else errorText.setText("* Code 304: Konnte Nutzer nicht aus dem Chat entfernen!");
        }
        else if (code == 500) errorText.setText("* Code 500: Konnte Nutzer nicht entfernen!");
        else errorText.setText("* Unbekannter Fehler aufgetreten!");
    }

    void setButtonStatus(Button neu, String id, boolean add) {
        if (add) {
            neu.setText("Hinzufügen");
            neu.setOnAction(event -> addToCourse(id, neu));
            neu.setStyle("-fx-background-color: #637381; -fx-background-radius: 8px");
        } else {
            neu.setText("Entfernen");
            neu.setOnAction(event -> removeFromCourse(id, neu));
            neu.setStyle("-fx-background-color: #816363; -fx-background-radius: 8px");
        }
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

    @FXML
    void orderBy(MouseEvent event) throws IOException {
        availableUsers.getChildren().clear();
        Text text = (Text) event.getSource();
        if (text.getText().equals("Name")) {
            currentList.sort(Comparator.comparing(strings -> strings[0].toLowerCase()));
        } else if (text.getText().equals("Matrikelnummer")) {
            currentList.sort(Comparator.comparing(strings -> strings[1].toLowerCase()));
        } showAllUsers(currentList);
    }

    void searchBarFilter(String newValue) throws IOException {
        availableUsers.getChildren().clear();
        currentList = new ArrayList<>();
        for (String[] u : allUsers) {
            String combine = u[0] + " " + u[1];
            if (combine.toLowerCase().contains(newValue.toLowerCase())) {
                currentList.add(u);
            }
        } showAllUsers(currentList);
    }

    @FXML
    void markText() {
        if (!(searchBar.getText().isEmpty())) {
            searchBar.selectAll();
        }
    }

    @FXML
    void exit() {
        if (fromAnsicht) {
            Launcher.gui.addToNavigation("/fxml/kursansicht.fxml");
        } else Launcher.gui.addToNavigation("/fxml/kursteilnehmer.fxml");
    }

    public static void open(boolean fromAnsicht) {
        KursteilnehmerBearbeiten.fromAnsicht = fromAnsicht;
        Launcher.gui.addToNavigation("/fxml/kursteilnehmerEdit.fxml");
    }
}