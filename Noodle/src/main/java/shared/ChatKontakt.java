package shared;

import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ChatKontakt {

    int id;
    String name;
    boolean isGroup;
    boolean isFriend;
    List<String> members;
    List<String> veranstaltungen;
    int imageid;
    String lastMessage;
    int unread;
    Color color;

    public ChatKontakt(int id, String name, boolean isGroup, List<String> members, String lastMessage, int unread) {
        this.id = id;
        this.name = name;
        this.isGroup = isGroup;
        this.members = members;
        this.imageid = 0;
        this.lastMessage = lastMessage;
        this.unread = unread;
        this.color = getRandomRGB();
        this.isFriend = false;
        this.veranstaltungen = new ArrayList<>();
    }

    public ChatKontakt(int id, String name, boolean isGroup, String lastMessage, int imageid, boolean isFriend, List<String> veranstaltungen, int unread) {
        this.id = id;
        this.name = name;
        this.isGroup = isGroup;
        this.members = new ArrayList<>();
        this.imageid = imageid;
        this.lastMessage = lastMessage;
        this.unread = unread;
        this.color = getRandomRGB();
        this.isFriend = isFriend;
        this.veranstaltungen = veranstaltungen;
    }

    public ChatKontakt(int id, String name, int imageid, boolean isFriend, List<String> veranstaltungen) {
        this.id = id;
        this.name = name;
        this.imageid = imageid;
        this.color = getRandomRGB();
        this.isFriend = isFriend;
        this.veranstaltungen = veranstaltungen;
    }

    public ChatKontakt(int id, String name, int imageid, boolean isFriend) {
        this.veranstaltungen = new ArrayList<>();
        this.id = id;
        this.name = name;
        this.imageid = imageid;
        this.color = getRandomRGB();
        this.isFriend = isFriend;
    }

    private Color getRandomRGB() {
        int rN1 = ThreadLocalRandom.current().nextInt(0,200+1);
        int rN2 = ThreadLocalRandom.current().nextInt(0,200+1);
        int rN3 = ThreadLocalRandom.current().nextInt(0,200+1);
        return Color.rgb(rN1, rN2, rN3);
    }

    public String getName() {
        return this.name;
    }

    public String[] getMembers() {
        return members.toArray(new String[0]);
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public int getUnreadMessages() {
        return unread;
    }

    public int getChatID() {
        return id;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public Color getColor() {
        return color;
    }

    public int getImageID() {
        return imageid;
    }

    public int getId() {
        return id;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public List<String> getVeranstaltungen() {
        return veranstaltungen;
    }

    public int getImageid() {
        return imageid;
    }

}