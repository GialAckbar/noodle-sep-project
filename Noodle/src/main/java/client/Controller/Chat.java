package client.Controller;

import client.Launcher;
import client.RequestHandler;
import client.Response;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Text;
import javafx.util.Duration;
import shared.ChatNachricht;
import shared.ProfilDaten;
import java.util.ArrayList;
import java.util.List;

public class Chat {

    public static int id = 40;
    public static boolean timer = false;
    private static Timeline chatUpdater;

    @FXML private ScrollPane scrollPane;
    @FXML private Text titleText;
    @FXML private VBox entries;
    @FXML private TextField textField;
    private List<ChatNachricht> messages;
    private List<ProfilDaten> profilePictures;
    public boolean isProjektgruppenChat = false;

    @FXML
    void initialize() {
        isProjektgruppenChat = false;
        String args = "?chatid=" + id;
        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET,"/chat/receive" + args, shared.Chat.class);

        if (response.statusCode == 200) {

            shared.Chat chat = (shared.Chat) response.getElement();
            String roomname = chat.getRoomname();
            getProfilePictures(chat.getMembers()); // [0] nutzerid, [1] imageid, [2] name
            messages = chat.getMessages();
            titleText.setText(roomname);
            entries.heightProperty().addListener(observable -> autoScroll());

            // Alle Nachrichten anzeigen und Timer starten, der nach neuen Nachrichten Ausschau hält
            showMessages();
            startUpdateTimeLine(args);

            // Automatisch nach ganz unten scrollen, wenn Entries eingetragen sind oder neue dazu kommen
            Platform.runLater(() -> scrollPane.setVvalue(1.0));
            entries.heightProperty().addListener(observable -> autoScroll());
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        }

    }

    void showMessages() {
        for (ChatNachricht m : messages) {
            addMessage(m.getNachricht(), m.getZeitpunkt(), m.getSenderid(), m.getName(), m.isMe());
        }
    }

    void addMessage(String nachricht, String zeitpunkt, int senderid, String name, boolean me) {
        ChatEntry entry;
        if (me) entry = Launcher.gui.addElement(entries,"/fxml/ChatEntryOwn.fxml");
        else entry = Launcher.gui.addElement(entries,"/fxml/ChatEntry.fxml");

        entry.textName.setText(name);
        entry.textMessage.setText(nachricht);
        entry.textTimestamp.setText(zeitpunkt);

        for (ProfilDaten profilePicture : profilePictures) {
            if (senderid == profilePicture.getID()) {
                if (profilePicture.getProfilbild() == null) {
                    entry.pbLetter.setText(String.valueOf(name.charAt(0)));
                    entry.pbCircle.setFill(profilePicture.getColor());
                } else {
                    entry.pbLetter.setText("");
                    entry.pbCircle.setFill(new ImagePattern(profilePicture.getProfilbild()));
                }
            }
        }
    }

    void getProfilePictures(List<String[]> members) {
        profilePictures = new ArrayList<>();
        for (String[] m : members) {
            if (Integer.parseInt(m[1]) == 0) {
                profilePictures.add(new ProfilDaten(m[2], Integer.parseInt(m[0]), null));
            } else {
                String url = "http://127.0.0.1:1337/getfile?userid="+Launcher.useridMitToken.getUserid()+"&token="+Launcher.useridMitToken.getToken()+"&fileid="+m[1];
                profilePictures.add(new ProfilDaten(m[2], Integer.parseInt(m[0]), new Image(url)));
            }
        }
    }

    void autoScroll() {
        if (scrollPane.getVvalue() > 0.85) {
            scrollPane.setVvalue(1.0);
        }
    }

    void startUpdateTimeLine(String args) {
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), event -> {
            if (timer) {
                Response responseCheck = Launcher.requestHandler.request(RequestHandler.Art.GET,"/chat/check/unread" + args, Integer.class);
                if (responseCheck.statusCode == 200) {
                    int counter = (int) responseCheck.getElement();
                    if (counter != messages.size()) {
                        Response responseUpdate = Launcher.requestHandler.request(RequestHandler.Art.GET,"/chat/update" + args, shared.Chat.class);
                        if (responseUpdate.statusCode == 200) {
                            entries.getChildren().clear();
                            shared.Chat chatUpdate = (shared.Chat) responseUpdate.getElement();
                            messages = chatUpdate.getMessages();
                            showMessages();
                        }
                    }
                }
            } else chatUpdater.stop();
        });
        timer = true;
        chatUpdater = new Timeline(keyFrame);
        chatUpdater.setCycleCount(Animation.INDEFINITE);
        chatUpdater.play();
    }

    @FXML
    void send(ActionEvent event) {
        if (!(textField.getText().equals("") || textField.getText().equals(" "))) {
            String body = textField.getText();
            String args = "?chatid=" + id;
            Response responseCheck = Launcher.requestHandler.request(RequestHandler.Art.POST,"/chat/send" + args, body);
            if (responseCheck.statusCode == 200) {
                textField.setText("");
                Response responseUpdate = Launcher.requestHandler.request(RequestHandler.Art.GET,"/chat/update" + args, shared.Chat.class);
                if (responseUpdate.statusCode == 200) {
                    entries.getChildren().clear();
                    shared.Chat chatUpdate = (shared.Chat) responseUpdate.getElement();
                    messages = chatUpdate.getMessages();
                    showMessages();
                    Platform.runLater(() -> scrollPane.setVvalue(1.0));
                }
            }
        }
    }

    @FXML
    void backToList(MouseEvent event) {
        if (!isProjektgruppenChat) {
            Launcher.gui.addToNavigation("/fxml/chatListStarted.fxml");
        } else {
            Launcher.gui.removeOverNavigation();
            timer = false;
        }
    }

}