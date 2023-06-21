package shared;

import java.util.List;

public class Chat {

    List<ChatNachricht> messages;
    List<String[]> members;
    String roomname;

    public Chat(List<ChatNachricht> messages, List<String[]> members, String roomname) {
        this.messages = messages;
        this.members = members;
        this.roomname = roomname;
    }

    public Chat(List<ChatNachricht> messages) {
        this.messages = messages;
    }

    public List<ChatNachricht> getMessages() {
        return messages;
    }

    public List<String[]> getMembers() {
        return members;
    }

    public String getRoomname() {
        return roomname;
    }

}
