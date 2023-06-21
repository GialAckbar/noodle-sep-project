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

public class ChatListeAvailable {

    @FXML private TextField searchBar;
    @FXML private VBox entries;
    ChatKontakt[] chatList;
    ChatKontakt[] currentList;

    @FXML
    void initialize() throws IOException {

        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET,"/chat/list/available?chatid=-1", ChatKontakt[].class);

        if (response.statusCode == 200) {

            // Alle Kontakte laden
            chatList = (ChatKontakt[]) response.getElement();
            Arrays.sort(chatList, Comparator.comparing(ChatKontakt::getName));
            currentList = Arrays.copyOf(chatList, chatList.length);

            showAllChats(currentList);

            // Funktion für die Suchleiste
            searchBar.textProperty().addListener((ov, v, v1) -> {
                try { searchBarFilter(v1); } catch (IOException e) { e.printStackTrace(); }
            });

        }
    }

    private void showAllChats(ChatKontakt[] chats) {
        for (ChatKontakt c : chats) {
            addChat(c.getName(), c.getImageID(), c.getColor(), c.isFriend(), c.getVeranstaltungen(), c.getChatID());
        }
    }

    private void addChat(String name, int imageID, Color color, boolean friend, List<String> veranstaltungen, int userid) {
        ChatListEntry entry = Launcher.gui.addElement(entries,"/fxml/chatListEntry.fxml");
        entry.title.setText(name);
        entry.chatInfo.setText("");
        entry.unreadMessages.setText("");
        entry.lastMessage.setText("");
        entry.chatButton.setText("Chat starten");
        entry.chatButton.setOnMouseClicked(event -> openChat(userid));

        Paint pattern;
        if (imageID != 0) {
            entry.pbLetter.setText("");
            Image img = new Image("http://127.0.0.1:1337/getfile?userid="+Launcher.useridMitToken.getUserid()+"&token="+Launcher.useridMitToken.getToken()+"&fileid="+imageID);
            pattern = new ImagePattern(img);
        } else {
            entry.pbLetter.setText(String.valueOf(name.charAt(0)));
            pattern = color;
        }
        entry.pbCircle.setFill(pattern);

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
    }

    @FXML
    void markAll(MouseEvent event) {
        TextField field = (TextField) event.getSource();
        if (!(field.getText().isEmpty())) {
            field.selectAll();
        }
    }

    @FXML
    void openChatList(ActionEvent event) {
        Launcher.gui.addToNavigation("/fxml/chatListStarted.fxml");
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

    void openChat(int id) {
        String args = "?chatid=" + id;
        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET,"/chat/check/exist" + args, Integer.class);
        if (response.statusCode == 200) {
            int chatid = (int) response.getElement();
            if (chatid == -1) {
                Response response2 = Launcher.requestHandler.request(RequestHandler.Art.POST, "/chat/create/private?chatid=-1", id, Integer.class);
                if (response2.statusCode == 200) {
                    Chat.id = (int) response2.getElement();
                    Launcher.gui.addToNavigation("/fxml/Chat.fxml");
                }
            } else {
                Chat.id = chatid;
                Launcher.gui.addToNavigation("/fxml/Chat.fxml");
            }
        }
    }

}
