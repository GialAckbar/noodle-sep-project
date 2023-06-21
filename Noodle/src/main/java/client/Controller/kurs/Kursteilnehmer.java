package client.Controller.kurs;

import client.Controller.Chat;
import client.Controller.Kursansicht;
import client.Controller.ProfilView;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import shared.Lehrende;
import shared.Student;
import shared.Teilnehmerliste;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Kursteilnehmer {

    @FXML private Text kursname;
    @FXML private Text errorText;
    @FXML private Text matrikelnummer;
    @FXML private VBox lehrEntries;
    @FXML private VBox studEntries;
    @FXML private TextField searchBar;
    @FXML private Group btnGroup;
    @FXML private Button profileButton;
    @FXML private Button chatButton;
    @FXML private Button friendRequestButton;

    List<Lehrende> teachers;
    List<Student> students;
    List<Lehrende> filteredTeachers;
    List<Student> filteredStudents;
    Text selectedUser = null;
    boolean isTeacher;

    @FXML
    void initialize() throws IOException {

        String args = "?courseid=" + Kursansicht.id;
        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET,"/kursteilnehmer/get" + args, Teilnehmerliste.class);
        int code = (response != null) ? response.statusCode : -1;

        if (code == 400) errorText.setText("* Code 400: Es wurden ungültige Daten übergeben!");
        else if (code == 500) errorText.setText("* Code 500: Serverseitiger Fehler!");

        else if (code == 200) {
            // Kein Fehler, also errorText ausblenden
            errorText.setVisible(false);

            // Alle Listen aus der Response laden
            Teilnehmerliste userLists = (Teilnehmerliste) response.getElement();
            String courseName = userLists.getCourseName();
            isTeacher = userLists.getIsTeacher();
            teachers = userLists.getLehrendenListe();
            students = userLists.getStudentenListe();
            matrikelnummer.setVisible(isTeacher); // Wenn kein Lehrender und/oder nicht im Kurs = Keine Matr.-Nr. anzeigen
            kursname.setText("Teilnehmer von \"" + courseName + "\""); // Name des Kurses
            friendRequestButton.setDisable(!isTeacher);
            if (isTeacher) {
                friendRequestButton.setText("Teilnehmer bearbeiten");
                friendRequestButton.setOnAction(event -> KursteilnehmerBearbeiten.open(false));
            }

            // Liste dem Client anzeigen lassen
            filteredTeachers = new ArrayList<>(teachers);
            filteredStudents = new ArrayList<>(students);
            showTeachers(filteredTeachers);
            showStudents(filteredStudents);

            // Funktion für die Suchleiste
            searchBar.textProperty().addListener((ov, v, v1) -> {
                try { searchBarFilter(v1); } catch (IOException e) { e.printStackTrace(); }
            });
        }
    }

    void showTeachers(List<Lehrende> teachers) throws IOException {
        for (Lehrende t : teachers) {
            String name = t.getVorname() + " " + t.getNachname();
            addMember(name, t.getEmail(), null, t.getID(), t.getID() == Launcher.useridMitToken.getUserid());
        }
    }

    void showStudents(List<Student> students) throws IOException {
        for (Student s : students) {
            String name = s.getVorname() + " " + s.getNachname();
            String matrikel = String.valueOf(s.getMatrikelnummer());
            addMember(name, s.getEmail(), matrikel, s.getID(), s.getID() == Launcher.useridMitToken.getUserid());
        }
    }

    void addMember(String name, String email, String matrikel, int id, boolean isClient) throws IOException {
        Group group = new Group();
        GridPane gridPane = createGridPane(name, email, matrikel, id, isClient);
        group.getChildren().add(gridPane);

        if (matrikel != null) {
            studEntries.getChildren().add(group);
        } else {
            lehrEntries.getChildren().add(group);
        }
        VBox.setMargin(group, new Insets(10, 0, 0, 10)); //Abstand der Namen
    }

    GridPane createGridPane(String name, String email, String matrikel, int id, boolean isClient) throws IOException {
        // Lade GridPane Vorlage
        GridPane gridPane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/kursteilnehmer_grid.fxml")));

        //Name, E-Mail, (Matrikel-Nr.) in die einzelnen Constraints
        Text nameText = createText(name, id);
        gridPane.add(nameText, 0, 0);

        Text emailText = createText(email, -1);
        gridPane.add(emailText, 1, 0);

        if (matrikel != null && isTeacher) {
            Text matrikelText = createText(matrikel, -1);
            gridPane.add(matrikelText, 2, 0);
        }

        // Eigener Name und E-Mail Adresse sollen visuell hervorgehoben werden
        if (isClient) {
            nameText.setFont(Font.font("system", FontWeight.BOLD, FontPosture.REGULAR, 15));
            emailText.setFont(Font.font("system", FontWeight.BOLD, FontPosture.REGULAR, 15));
        }

        // Grauer Rahmen, um die GridPanes, wenn man mit der Maus drübergeht
        gridPane.setOnMouseEntered(event -> gridPane.setStyle("-fx-background-color: #E6E6E6"));
        gridPane.setOnMouseExited(event -> gridPane.setStyle("-fx-background-color: transparent"));

        return gridPane;
    }

    Text createText(String text, int id) throws IOException {
        Text neu = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/kursteilnehmer_text.fxml")));
        neu.setText(text);

        if (id == -1) {
            neu.setOnMouseClicked(event -> copyToClipboard(text, neu));
        } else {
            neu.setOnMouseClicked(event -> openProfile(id, neu));
        }

        return neu;
    }

    void openProfile(int id, Text neu) {
        chatButton.setDisable(false);
        profileButton.setDisable(false);
        friendRequestButton.setDisable(false);
        if (selectedUser != null) selectedUser.setUnderline(false);
        neu.setUnderline(true);
        selectedUser = neu;

        if (!isTeacher && id != Launcher.useridMitToken.getUserid()) {
            String args = "?friendid=" + id;
            Response response = Launcher.requestHandler.request(RequestHandler.Art.GET,"/kursteilnehmer/checkFriends" + args, Integer.class);
            int code = (response != null) ? response.statusCode : -1;
            if (code == 400) errorText.setText("* Code 400: Es wurden ungültige Daten übergeben!");
            else if (code == 500) errorText.setText("* Code 500: Serverseitiger Fehler!");
            else if (code == 204) friendRequestButton.setText("Freundschaftsanfrage senden");
            else if (code == 409) {
                friendRequestButton.setText("Bereits gesendet!");
                friendRequestButton.setDisable(true);
            } else if (code == 302) {
                friendRequestButton.setText("Bereits befreundet!");
                friendRequestButton.setDisable(true);
            } else if (code == 206) {
                friendRequestButton.setText("Freundschaftsanfrage senden");
                friendRequestButton.setDisable(true);
            }
        }

        else if (id == Launcher.useridMitToken.getUserid()) {
            chatButton.setDisable(true);
            if (!isTeacher) {
                friendRequestButton.setDisable(true);
                friendRequestButton.setText("Geht nicht!");
            }
        }

        profileButton.setOnAction(event -> {
            ProfilView.accountid = id;
            Launcher.gui.addToNavigation("/fxml/EigenesProfil2.fxml");
        });

        chatButton.setOnAction(event -> {
            String args = "?chatid=" + id;
            Response response = Launcher.requestHandler.request(RequestHandler.Art.GET,"/chat/check/exist" + args, Integer.class);
            int code = (response != null) ? response.statusCode : -1;
            if (code == 500) errorText.setText("* Code 500: Serverseitiger Fehler!");
            else if (code == 200) {
                int chatid = (int) response.getElement();
                if (chatid == -1) {
                    Response response2 = Launcher.requestHandler.request(RequestHandler.Art.POST, "/chat/create/private?chatid=-1", id, Integer.class);
                    int code2 = (response2 != null) ? response2.statusCode : -1;
                    if (code2 == 500) errorText.setText("* Code 500: Serverseitiger Fehler!");
                    else if (code2 == 200) {
                        Chat.id = (int) response2.getElement();
                        Launcher.gui.addToNavigation("/fxml/Chat.fxml");
                    }
                } else {
                    Chat.id = chatid;
                    Launcher.gui.addToNavigation("/fxml/Chat.fxml");
                }
            }
        });

        friendRequestButton.setOnAction(event -> {
            if (isTeacher) {
                KursteilnehmerBearbeiten.open(false);
            } else {
                String args = "?accountid=" + id;
                Response response = Launcher.requestHandler.request(RequestHandler.Art.POST,"/freundesliste/addRequest" + args, -1);
                int code = (response != null) ? response.statusCode : -1;
                if (code == 400) errorText.setText("* Code 400: Es wurden ungültige Daten übergeben!");
                else if (code == 500) errorText.setText("* Code 500: Serverseitiger Fehler!");
                else if (code == 200) {
                    friendRequestButton.setText("Anfrage gesendet!");
                    friendRequestButton.setDisable(true);
                }
            }
        });
    }

    void copyToClipboard(String text, Text neu) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = new StringSelection(text);
        clipboard.setContents(transferable, null); // Speichern in der Zwischenablage

        neu.setText("Zur Zwischenablage hinzugefügt!");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override public void run() {neu.setText(text);}}, 2000 // Text wird nach 2 Sekunden zurückgesetzt
        );
    }

    @FXML
    void lehrSort(MouseEvent event) throws IOException {
        lehrEntries.getChildren().clear();
        Text text = (Text) event.getSource();
        if (text.getText().equals("Name")) {
            filteredTeachers.sort(Comparator.comparing(lehrende -> lehrende.getVorname().toLowerCase()));
        } else if (text.getText().equals("E-Mail Adresse")) {
            filteredTeachers.sort(Comparator.comparing(lehrende -> lehrende.getEmail().toLowerCase()));
        } showTeachers(filteredTeachers);
    }

    @FXML
    void studSort(MouseEvent event) throws IOException {
        studEntries.getChildren().clear();
        Text text = (Text) event.getSource();
        switch (text.getText()) {
            case "Name":
                filteredStudents.sort(Comparator.comparing(student -> student.getVorname().toLowerCase()));
                break;
            case "E-Mail Adresse":
                filteredStudents.sort(Comparator.comparing(student -> student.getEmail().toLowerCase()));
                break;
            case "Matrikelnummer":
                filteredStudents.sort(Comparator.comparing(Student::getMatrikelnummer));
                break;
        }
        showStudents(filteredStudents);
    }

    void searchBarFilter(String newValue) throws IOException {
        lehrEntries.getChildren().clear();
        studEntries.getChildren().clear();
        filteredTeachers = new ArrayList<>();
        filteredStudents = new ArrayList<>();
        for (Lehrende t : teachers) {
            String combine = t.getVorname() + " " + t.getNachname() + " " + t.getEmail();
            if (combine.toLowerCase().contains(newValue.toLowerCase())) {
                filteredTeachers.add(t);
            }
        } boolean valid = isTeacher;
        for (Student s : students) {
            String matr = (valid) ? Integer.toString(s.getMatrikelnummer()) : "";
            String combine = s.getVorname() + " " + s.getNachname() + " " + s.getEmail() + " " + matr;
            if (combine.toLowerCase().contains(newValue.toLowerCase())) {
                filteredStudents.add(s);
            }
        }
        showTeachers(filteredTeachers);
        showStudents(filteredStudents);
    }

    @FXML
    void markText() {
        if (!(searchBar.getText().isEmpty())) {
            searchBar.selectAll();
        }
    }

    @FXML
    void exit() {
        Launcher.gui.addToNavigation("/fxml/kursansicht.fxml");
    }

    public static void open() {
        Launcher.gui.addToNavigation("/fxml/kursteilnehmer.fxml");
    }
}