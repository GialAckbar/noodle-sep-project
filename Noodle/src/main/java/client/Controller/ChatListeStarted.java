package client.Controller;

import client.Launcher;
import client.RequestHandler;
import client.Response;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.util.Duration;
import shared.ChatKontakt;
import java.io.IOException;
import java.util.*;

public class ChatListeStarted {

    public static boolean timer = false;
    private static Timeline chatUpdater;

    @FXML private TextField searchBar;
    @FXML private VBox entries;
    ChatKontakt[] chatList;
    ChatKontakt[] currentList;
    HashMap<Integer, Paint> colors = new HashMap<>();
    int unreadCount = 0;

    @FXML
    void initialize() throws IOException {

        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET,"/chat/list/started?chatid=-1", ChatKontakt[].class);

        if (response.statusCode == 200) {

            // Alle Kontakte laden
            chatList = (ChatKontakt[]) response.getElement();
            Arrays.sort(chatList, Comparator.comparing(ChatKontakt::getName));
            currentList = Arrays.copyOf(chatList, chatList.length);

            showAllChats(currentList);
            startUpdateTimeLine();

            // Funktion für die Suchleiste
            searchBar.textProperty().addListener((ov, v, v1) -> {
                try { searchBarFilter(v1); } catch (IOException e) { e.printStackTrace(); }
            });

        }
    }

    private void showAllChats(ChatKontakt[] chats) throws IOException {
        for (ChatKontakt c : chats) {
           addChat(c.getName(), c.getMembers(), c.getLastMessage(), c.getUnreadMessages(), c.getChatID(), c.isGroup(), c.getImageID(), c.getColor(), c.isFriend(), c.getVeranstaltungen());
        }
    }

    private void addChat(String name, String[] members, String lastMessage, int unreadMessages, int chatID, boolean group, int imageID, Color color, boolean friend, List<String> veranstaltungen) {
        ChatListEntry entry = Launcher.gui.addElement(entries,"/fxml/chatListEntry.fxml");
        entry.title.setText(name);
        entry.chatInfo.setText("");
        entry.unreadMessages.setOnMouseClicked(event -> markAsRead(chatID, entry.unreadMessages));
        entry.chatButton.setOnMouseClicked(event -> openChat(chatID));

        Paint pattern;
        if (colors.containsKey(chatID)) {
            if (colors.get(chatID) instanceof ImagePattern) {
                entry.pbLetter.setText("");
            } else {
                entry.pbLetter.setText(String.valueOf(name.charAt(0)));
            }
            pattern = colors.get(chatID);
        } else {
            if (imageID != 0) {
                entry.pbLetter.setText("");
                Image img = new Image("http://127.0.0.1:1337/getfile?userid="+Launcher.useridMitToken.getUserid()+"&token="+Launcher.useridMitToken.getToken()+"&fileid="+imageID);
                pattern = new ImagePattern(img);
                colors.put(chatID, pattern);
            } else {
                entry.pbLetter.setText(String.valueOf(name.charAt(0)));
                pattern = color;
                colors.put(chatID, color);
            }
        }
        entry.pbCircle.setFill(pattern);

        entry.lastMessage.setText("Letzte Nachricht: " + lastMessage);
        if (unreadMessages != 0) {
            entry.unreadMessages.setText("(" + unreadMessages + " Ungelesene Nachrichten)");
        } else entry.unreadMessages.setText("");

        if (!group) {
            if (veranstaltungen.size() == 0) {
                if (friend) {
                    entry.chatInfo.setText("Freund");
                } else entry.chatInfo.setText("(Weder befreundet, noch in selben Kursen)");
            } else {
                if (friend) {
                    entry.chatInfo.setText("Freund | Gemeinsame Kurse: ");
                } else entry.chatInfo.setText("Gemeinsame Kurse: ");
                if (veranstaltungen.size() > 2) {
                    entry.chatInfo.setText(entry.chatInfo.getText() + veranstaltungen.get(0) + ", " + veranstaltungen.get(1) + " (" + (veranstaltungen.size() - 2) + " Weitere)");
                } else {
                    for (int i = 0; i < veranstaltungen.size(); i++) {
                        if (i != veranstaltungen.size() - 1) {
                            entry.chatInfo.setText(entry.chatInfo.getText() + veranstaltungen.get(i) + ", ");
                        } else entry.chatInfo.setText(entry.chatInfo.getText() + veranstaltungen.get(i));
                    }
                }
            }
        } else {
            if (members.length > 2) {
                entry.chatInfo.setText("Mitglieder: " + members[0] + ", " + members[1] + " (" + (members.length - 1) + " Weitere)");
            } else if (members.length == 1) {
                entry.chatInfo.setText("Mitglieder: " + members[0] + ", Du");
            } else if (members.length == 2) {
                entry.chatInfo.setText("Mitglieder: " + members[0] + ", " + members[1] + ", Du");
            } else {
                entry.chatInfo.setText("Mitglieder: Du");
            }
        }
    }

    @FXML
    void markAll(MouseEvent event) {
        TextField field = (TextField) event.getSource();
        if (!(field.getText().isEmpty())) {
            field.selectAll();
        }
    }

    @FXML
    void openNewChat(ActionEvent event) {
        Launcher.gui.addToNavigation("/fxml/chatListAvailable.fxml");
    }

    @FXML
    void reactOnEntry(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #374048;-fx-background-radius: 8px;");
    }

    @FXML
    void reactOnExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #637381;-fx-background-radius: 8px;");
    }

    void searchBarFilter(String newValue) throws IOException {
        entries.getChildren().clear();
        List<ChatKontakt> temp = new ArrayList<>();
        for (ChatKontakt kontakt : chatList) {
            String combine = kontakt.getName();
            if (combine.toLowerCase().contains(newValue.toLowerCase())) {
                temp.add(kontakt);
            }
        }
        ChatKontakt[] chatKontakte = temp.toArray(new ChatKontakt[0]);
        showAllChats(chatKontakte);
    }

    void markAsRead(int chatID, Text unreadMessages) {
        String args = "?chatid=" + chatID;
        Response response = Launcher.requestHandler.request(RequestHandler.Art.DELETE,"/chat/list/markasread" + args);
        if (response.statusCode == 200) {
            unreadMessages.setText("");
        }
    }

    void openChat(int chatID) {
        Chat.id = chatID;
        Launcher.gui.addToNavigation("/fxml/Chat.fxml");
    }

    void startUpdateTimeLine() {
        Response preCheck = Launcher.requestHandler.request(RequestHandler.Art.GET,"/chat/list/check?chatid=-1", int[].class);
        if (preCheck.statusCode == 200) {
            int[] counters = (int[]) preCheck.getElement();
            unreadCount = counters[1];
        }
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), event -> {
            if (timer) {
                Response responseCheck = Launcher.requestHandler.request(RequestHandler.Art.GET,"/chat/list/check?chatid=-1", int[].class);
                if (responseCheck.statusCode == 200) {
                    int[] counters = (int[]) responseCheck.getElement();
                    if (counters[1] != unreadCount || counters[0] != chatList.length) {
                        unreadCount = counters[1];
                        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET,"/chat/list/started?chatid=-1", ChatKontakt[].class);
                        if (response.statusCode == 200) {
                            chatList = (ChatKontakt[]) response.getElement();
                            Arrays.sort(chatList, Comparator.comparing(ChatKontakt::getName));
                            currentList = Arrays.copyOf(chatList, chatList.length);
                            try {
                                showAllChats(currentList);
                                searchBarFilter(searchBar.getText());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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

}
