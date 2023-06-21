package server.handler;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import shared.*;
import shared.utility.GsonUtility;
import java.io.IOException;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ChatHandler extends AbstractHandler {

    Connection con;

    public ChatHandler(Connection con) {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");

        if (baseRequest.getMethod().equals("GET") && splitTarget.length >= 3 && splitTarget[1].equals("chat")) {

            int userid, chatid;

            try {
                userid = Integer.parseInt(request.getParameter("userid"));
                chatid = Integer.parseInt(request.getParameter("chatid"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                baseRequest.setHandled(true);
                return;
            }

            if (splitTarget[2].equals("list")) {
                if (splitTarget[3].equals("started")) {

                    ResultSet chats, courses, friends, groupNames, unread;
                    try {
                        ResultSet[] rs = getStartedChats(userid);
                        chats = rs[0];
                        courses = rs[1];
                        friends = rs[2];
                        groupNames = rs[3];
                        unread = rs[4];
                    } catch(NullPointerException e) {
                        e.printStackTrace();
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        baseRequest.setHandled(true);
                        return;
                    }

                    LinkedHashMap<Integer, List<String[]>> extractedChats = extractChats(chats);
                    LinkedHashMap<Integer, List<String>> extractedCourses = extractCourses(courses);
                    List<Integer> extractedFriends = extractFriends(friends);
                    LinkedHashMap<Integer, String> extractedGroupNames = extractGroupNames(groupNames);
                    LinkedHashMap<Integer, Integer> extractedUnread = extractUnread(unread);
                    if (extractedChats == null || extractedCourses == null || extractedFriends == null || extractedGroupNames == null || extractedUnread == null) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        baseRequest.setHandled(true);
                        return;
                    }

                    List<ChatKontakt> createdChatContacts = createChatContacts(extractedChats, extractedCourses, extractedFriends, extractedGroupNames, extractedUnread);
                    ChatKontakt[] chatKontakte = createdChatContacts.toArray(new ChatKontakt[0]);
                    response.getWriter().println(new Gson().toJson(chatKontakte, ChatKontakt[].class));
                    response.setStatus(HttpServletResponse.SC_OK);
                    baseRequest.setHandled(true);

                } else if (splitTarget[3].equals("available")) {

                    ResultSet chats, friends;
                    try {
                        ResultSet[] rs = getAvailableChats(userid);
                        chats = rs[0];
                        friends = rs[1];
                    } catch(NullPointerException e) {
                        e.printStackTrace();
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        baseRequest.setHandled(true);
                        return;
                    }

                    LinkedHashMap<Integer, List<String[]>> extractedChats = extractAvailableChats(chats);
                    LinkedHashMap<Integer, String[]> extractedFriends = extractAvailableFriends(friends);
                    if (extractedChats == null || extractedFriends == null) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        baseRequest.setHandled(true);
                        return;
                    }

                    List<ChatKontakt> createdChatContacts = createAvailableChatContacts(extractedChats, extractedFriends);
                    ChatKontakt[] chatKontakte = createdChatContacts.toArray(new ChatKontakt[0]);
                    response.getWriter().println(new Gson().toJson(chatKontakte, ChatKontakt[].class));
                    response.setStatus(HttpServletResponse.SC_OK);
                    baseRequest.setHandled(true);

                } else if (splitTarget[3].equals("check")) {
                    try {
                        String query = "SELECT COUNT(*) FROM chats WHERE id IN (SELECT chatid FROM chatmitglieder WHERE nutzerid = ?);";
                        PreparedStatement stmt = con.prepareStatement(query);
                        stmt.setInt(1, userid);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            query = "SELECT SUM(nichtgelesen) FROM chatmitglieder WHERE nutzerid = ?;";
                            stmt = con.prepareStatement(query);
                            stmt.setInt(1, userid);
                            ResultSet rs2 = stmt.executeQuery();
                            if (rs2.next()) {
                                response.getWriter().println(new Gson().toJson(new int[] {rs.getInt(1), rs2.getInt(1)}, int[].class));
                                response.setStatus(HttpServletResponse.SC_OK);
                                baseRequest.setHandled(true);
                                return;
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    baseRequest.setHandled(true);
                }
            }

            else if (splitTarget[2].equals("receive")) {

                ResultSet messages, members;
                try {
                    messages = getMessages(chatid);
                    members = getMembers(chatid);
                } catch(NullPointerException e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    baseRequest.setHandled(true);
                    return;
                }

                List<ChatNachricht> extractedMessages = extractMessages(messages, userid);
                List<String[]> extractedMembers = extractMembers(members);
                if (extractedMessages == null || extractedMembers == null) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    baseRequest.setHandled(true);
                    return;
                }

                Chat chat;
                if (getIsGroup(chatid)) {
                    chat = new Chat(extractedMessages, extractedMembers, "< Gruppen-Chatraum: " + getGroupName(chatid));
                } else {
                    String name = "";
                    for (String[] extractedMember : extractedMembers) {
                        if (Integer.parseInt(extractedMember[0]) != userid) {
                            name = extractedMember[2];
                        }
                    }
                    chat = new Chat(extractedMessages, extractedMembers, "< Chatraum: " + name);
                }

                if (checkAlreadyRead(userid, chatid)) {
                    markAsRead(userid, chatid);
                }

                response.getWriter().println(GsonUtility.getGson().toJson(chat, Chat.class));
                response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);

            }

            else if (splitTarget[2].equals("check")) {

                if (splitTarget[3].equals("unread")) {
                    ResultSet rs;
                    try {
                        String query = "SELECT COUNT(*) FROM nachrichten WHERE chatid = ?;";
                        PreparedStatement stmt = con.prepareStatement(query);
                        stmt.setInt(1, chatid);
                        rs = stmt.executeQuery();
                        if (rs.next()) {
                            response.getWriter().println(new Gson().toJson(rs.getInt(1), Integer.class));
                            response.setStatus(HttpServletResponse.SC_OK);
                        } else response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        baseRequest.setHandled(true);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        baseRequest.setHandled(true);
                    }
                }

                else if (splitTarget[3].equals("exist")) {
                    ResultSet rs;
                    try {
                        String query = "SELECT DISTINCT c.id FROM chats c INNER JOIN chatmitglieder cm ON c.id = cm.chatid INNER JOIN nutzer n ON cm.nutzerid = n.id "
                                + "WHERE c.isgroup = false AND n.id = ? AND c.id IN (SELECT DISTINCT c.id FROM chats c INNER JOIN chatmitglieder cm ON c.id = cm.chatid "
                                + "INNER JOIN nutzer n ON cm.nutzerid = n.id WHERE n.id = ?);";
                        PreparedStatement stmt = con.prepareStatement(query);
                        stmt.setInt(1, userid);
                        stmt.setInt(2, chatid);
                        rs = stmt.executeQuery();
                        if (rs.next()) {
                            response.getWriter().println(new Gson().toJson(rs.getInt(1), Integer.class));
                        } else response.getWriter().println(new Gson().toJson(-1, Integer.class));
                        response.setStatus(HttpServletResponse.SC_OK);
                        baseRequest.setHandled(true);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        baseRequest.setHandled(true);
                    }
                }

            }

            else if (splitTarget[2].equals("update")) {

                ResultSet messages;
                try {
                    messages = getMessages(chatid);
                } catch(NullPointerException e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    baseRequest.setHandled(true);
                    return;
                }

                List<ChatNachricht> extractedMessages = extractMessages(messages, userid);
                if (extractedMessages == null) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    baseRequest.setHandled(true);
                    return;
                }

                Chat chat = new Chat(extractedMessages);
                if (checkAlreadyRead(userid, chatid)) {
                    markAsRead(userid, chatid);
                }

                response.getWriter().println(GsonUtility.getGson().toJson(chat, Chat.class));
                response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);

            }

        }

        else if (baseRequest.getMethod().equals("POST") && splitTarget.length >= 3 && splitTarget[1].equals("chat")) {

            int userid, chatid;

            try {
                userid = Integer.parseInt(request.getParameter("userid"));
                chatid = Integer.parseInt(request.getParameter("chatid"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                baseRequest.setHandled(true);
                return;
            }

            if (splitTarget[2].equals("send")) {
                String message = new Gson().fromJson(request.getReader(), String.class);

                if (sendMessage(message, chatid, userid)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                baseRequest.setHandled(true);

            }

            else if (splitTarget[2].equals("create")) {
                if (splitTarget[3].equals("private")) {
                    int targetid = new Gson().fromJson(request.getReader(), int.class);

                    int newChatID = createPrivateChat(userid, targetid);
                    if (newChatID != -1) {
                        response.getWriter().println(GsonUtility.getGson().toJson(newChatID, Integer.class));
                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                    baseRequest.setHandled(true);

                }

                else if (splitTarget[3].equals("group")) {
                    int groupid = new Gson().fromJson(request.getReader(), int.class);

                    int newChatID = createGroup(userid, groupid, con);
                    if (newChatID != -1) {
                        response.getWriter().println(GsonUtility.getGson().toJson(newChatID, Integer.class));
                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                    baseRequest.setHandled(true);

                }

            }

            else if (splitTarget[2].equals("add")) {
                int targetid = new Gson().fromJson(request.getReader(), int.class);

                boolean success = addMember(targetid, chatid, con);
                if (success) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                baseRequest.setHandled(true);

            }
        }

        else if (baseRequest.getMethod().equals("DELETE") && splitTarget.length >= 3 && splitTarget[1].equals("chat")) {

            int userid, chatid;
            String token;

            try {
                userid = Integer.parseInt(request.getParameter("userid"));
                chatid = Integer.parseInt(request.getParameter("chatid"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                baseRequest.setHandled(true);
                return;
            }

            if (splitTarget[2].equals("list")) {
                if (splitTarget[3].equals("markasread")) {

                    if (checkAlreadyRead(userid, chatid)) {
                        if (markAsRead(userid, chatid)) {
                            response.setStatus(HttpServletResponse.SC_OK);
                        } else {
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        }
                    } else response.setStatus(HttpServletResponse.SC_CONFLICT);
                    baseRequest.setHandled(true);

                }
            }

            else if (splitTarget[2].equals("remove")) {
                int targetid = -1;
                try {
                    targetid = Integer.parseInt(request.getParameter("targetid"));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    baseRequest.setHandled(true);
                    return;
                }

                boolean success = removeMember(targetid, chatid, con);
                if (success) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                baseRequest.setHandled(true);

            }
        }
    }

    private ResultSet[] getStartedChats(int nutzerid) {
        try {
            String query = "SELECT c.id, nutzer.id, vorname, nachname, imageid, nachricht, isgroup FROM chats c INNER JOIN chatmitglieder m ON c.id = m.chatid INNER JOIN nachrichten n ON " +
                    "c.lastmessageid = n.id INNER JOIN nutzer ON m.nutzerid = nutzer.id WHERE c.id IN (SELECT chatid FROM chatmitglieder WHERE nutzerid = ?) AND nutzer.id NOT IN (?) ORDER BY vorname ASC;";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, nutzerid);
            statement.setInt(2, nutzerid);
            ResultSet chats = statement.executeQuery();

            query = "SELECT nutzerid, name FROM veranstaltungsteilnehmer m INNER JOIN veranstaltung v ON m.veranstaltungsid = v.id WHERE m.veranstaltungsid IN " +
                    "(SELECT veranstaltungsid FROM veranstaltungsteilnehmer WHERE nutzerid = ?) AND nutzerid NOT IN (?) ORDER BY name ASC;";
            statement = con.prepareStatement(query);
            statement.setInt(1, nutzerid);
            statement.setInt(2, nutzerid);
            ResultSet courses = statement.executeQuery();

            query = "SELECT id FROM nutzer WHERE id IN (SELECT student1_id FROM friends WHERE student2_id = ?) OR id IN (SELECT student2_id FROM friends WHERE student1_id = ?) ORDER BY id ASC;";
            statement = con.prepareStatement(query);
            statement.setInt(1, nutzerid);
            statement.setInt(2, nutzerid);
            ResultSet friends = statement.executeQuery();

            query = "SELECT c.id, name FROM veranstaltung v INNER JOIN chats c ON v.id = c.projektgruppeid WHERE c.id IN (SELECT chatid FROM chatmitglieder WHERE nutzerid = ?) ORDER BY name ASC;";
            statement = con.prepareStatement(query);
            statement.setInt(1, nutzerid);
            ResultSet groupNames = statement.executeQuery();

            query = "SELECT chatid, nichtgelesen FROM chatmitglieder WHERE nutzerid = ? ORDER BY chatid ASC";
            statement = con.prepareStatement(query);
            statement.setInt(1, nutzerid);
            ResultSet unread = statement.executeQuery();

            return new ResultSet[] {chats, courses, friends, groupNames, unread};
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ResultSet[] getAvailableChats(int nutzerid) {
        try {
            String query = "SELECT n.id, v.name, vorname, nachname, imageid FROM veranstaltungsteilnehmer m INNER JOIN veranstaltung v ON m.veranstaltungsid = v.id " +
                    "INNER JOIN nutzer n ON m.nutzerid = n.id WHERE m.veranstaltungsid IN (SELECT veranstaltungsid FROM veranstaltungsteilnehmer WHERE nutzerid = ?) " +
                    "AND m.nutzerid NOT IN (SELECT nutzerid FROM chatmitglieder WHERE chatid IN (SELECT chatid FROM chatmitglieder INNER JOIN chats " +
                    "ON chatmitglieder.chatid = chats.id WHERE nutzerid = ? AND isgroup = false)) AND n.id NOT IN (?) ORDER BY vorname, v.name ASC;";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, nutzerid);
            statement.setInt(2, nutzerid);
            statement.setInt(3, nutzerid);
            ResultSet chats = statement.executeQuery();

            query = "SELECT id, vorname, nachname, imageid FROM nutzer WHERE (id IN (SELECT student1_id FROM friends WHERE student2_id = ?) OR id IN " +
                    "(SELECT student2_id FROM friends WHERE student1_id = ?)) AND id NOT IN (SELECT nutzerid FROM chatmitglieder WHERE chatid IN " +
                    "(SELECT chatid FROM chatmitglieder INNER JOIN chats ON chatmitglieder.chatid = chats.id WHERE nutzerid = ? AND isgroup = false)) " +
                    "ORDER BY vorname ASC;";
            statement = con.prepareStatement(query);
            statement.setInt(1, nutzerid);
            statement.setInt(2, nutzerid);
            statement.setInt(3, nutzerid);
            ResultSet friends = statement.executeQuery();

            return new ResultSet[] {chats, friends};
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private LinkedHashMap<Integer, List<String[]>> extractAvailableChats(ResultSet c) {
        try {
            LinkedHashMap<Integer, List<String[]>> result = new LinkedHashMap<>();
            while (c.next()) {
                int userid = c.getInt(1);
                if (!result.containsKey(userid)) {
                    List<String[]> temp = new ArrayList<>();
                    temp.add(new String[] {c.getString(2), c.getString(3), c.getString(4), Integer.toString(c.getInt(5)), null});
                    result.put(userid, temp);
                } else {
                    result.get(userid).add(new String[] {c.getString(2), c.getString(3), c.getString(4), Integer.toString(c.getInt(5)), null});
                }
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private LinkedHashMap<Integer, List<String[]>> extractChats(ResultSet c) {
        try {
            LinkedHashMap<Integer, List<String[]>> result = new LinkedHashMap<>();
            while (c.next()) {
                int chatid = c.getInt(1);
                if (!result.containsKey(chatid)) {
                    List<String[]> temp = new ArrayList<>();
                    temp.add(new String[] {Integer.toString(c.getInt(2)), c.getString(3), c.getString(4), Integer.toString(c.getInt(5)), c.getString(6), String.valueOf(c.getBoolean(7))});
                    result.put(chatid, temp);
                } else {
                    result.get(chatid).add(new String[] {Integer.toString(c.getInt(2)), c.getString(3), c.getString(4), Integer.toString(c.getInt(5)), c.getString(6), String.valueOf(c.getBoolean(7))});
                }
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private LinkedHashMap<Integer, List<String>> extractCourses(ResultSet courses) {
        try {
            LinkedHashMap<Integer, List<String>> result = new LinkedHashMap<>();
            while (courses.next()) {
                int userid = courses.getInt(1);
                if (!result.containsKey(userid)) {
                    List<String> temp = new ArrayList<>();
                    temp.add(courses.getString(2));
                    result.put(userid, temp);
                } else {
                    result.get(userid).add(courses.getString(2));
                }
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private LinkedHashMap<Integer, String[]> extractAvailableFriends(ResultSet friends) {
        try {
            LinkedHashMap<Integer, String[]> result = new LinkedHashMap<>();
            while (friends.next()) {
                int userid = friends.getInt(1);
                String name = friends.getString(2) + " "+ friends.getString(3);
                result.put(userid, new String[] {name, Integer.toString(friends.getInt(4))});
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Integer> extractFriends(ResultSet friends) {
        try {
            List<Integer> result = new ArrayList<>();
            while (friends.next()) {
                result.add(friends.getInt(1));
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private LinkedHashMap<Integer, String> extractGroupNames(ResultSet groupNames) {
        try {
            LinkedHashMap<Integer, String> result = new LinkedHashMap<>();
            while (groupNames.next()) {
                result.put(groupNames.getInt(1), groupNames.getString(2));
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private LinkedHashMap<Integer, Integer> extractUnread(ResultSet unread) {
        try {
            LinkedHashMap<Integer, Integer> result = new LinkedHashMap<>();
            while (unread.next()) {
                result.put(unread.getInt(1), unread.getInt(2));
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<ChatKontakt> createChatContacts(LinkedHashMap<Integer, List<String[]>> chats, LinkedHashMap<Integer, List<String>> courses, List<Integer> friends,
                                                 LinkedHashMap<Integer, String> groupNames, LinkedHashMap<Integer, Integer> unread) {
        List<ChatKontakt> contacts = new ArrayList<>();
        for (Integer chatid : chats.keySet()) {
            if (Boolean.parseBoolean(chats.get(chatid).get(0)[5])) { // == isGroup
                List<String> members = new ArrayList<>();
                for (int i = 0; i < chats.get(chatid).size(); i++) {
                    members.add(chats.get(chatid).get(i)[1] + " " + chats.get(chatid).get(i)[2]);
                }
                ChatKontakt groupContact = new ChatKontakt(chatid, groupNames.get(chatid), true, members, chats.get(chatid).get(0)[4], unread.get(chatid));
                contacts.add(groupContact);
            } else {
                int userid = Integer.parseInt(chats.get(chatid).get(0)[0]);
                List<String> veranstaltungen;
                if (courses.containsKey(userid)) {
                    veranstaltungen = new ArrayList<>(courses.get(userid));
                } else veranstaltungen = new ArrayList<>();
                String name = chats.get(chatid).get(0)[1] + " " + chats.get(chatid).get(0)[2];
                ChatKontakt privateContact = new ChatKontakt(chatid, name, false, chats.get(chatid).get(0)[4], Integer.parseInt(chats.get(chatid).get(0)[3]),
                        friends.contains(userid), veranstaltungen, unread.get(chatid));
                contacts.add(privateContact);
            }
        }
        return contacts;
    }

    private List<ChatKontakt> createAvailableChatContacts(LinkedHashMap<Integer, List<String[]>> chats, LinkedHashMap<Integer, String[]> friends) {
        List<ChatKontakt> contacts = new ArrayList<>();
        for (Integer userid : chats.keySet()) {
            List<String> veranstaltungen = new ArrayList<>();
            for (int i = 0; i < chats.get(userid).size(); i++) {
                veranstaltungen.add(chats.get(userid).get(i)[0]);
            }
            String name = chats.get(userid).get(0)[1] + " " + chats.get(userid).get(0)[2];
            ChatKontakt newContact = new ChatKontakt(userid, name, Integer.parseInt(chats.get(userid).get(0)[3]), friends.containsKey(userid), veranstaltungen);
            contacts.add(newContact);
        }
        for (Integer friendid : friends.keySet()) {
            if (!chats.containsKey(friendid)) {
                ChatKontakt newContact = new ChatKontakt(friendid, friends.get(friendid)[0], Integer.parseInt(friends.get(friendid)[1]), true);
                contacts.add(newContact);
            }
        }
        return contacts;
    }

    private boolean markAsRead(int nutzerid, int chatid) {
        ResultSet rs = null;

        try {
            String query = "UPDATE chatmitglieder SET nichtgelesen = 0 WHERE nutzerid = ? AND chatid = ? RETURNING nutzerid;";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, nutzerid);
            stmt.setInt(2, chatid);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean checkAlreadyRead(int nutzerid, int chatid) {
        ResultSet rs = null;

        try {
            String query = "SELECT nichtgelesen FROM chatmitglieder WHERE nutzerid = ? AND chatid = ?;";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, nutzerid);
            stmt.setInt(2, chatid);
            rs = stmt.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ResultSet getMessages(int chatid) {
        ResultSet rs = null;

        try {
            String query = "SELECT nachricht, zeitpunkt, n.senderid, vorname, nachname FROM nachrichten n INNER JOIN nutzer u ON n.senderid = u.id WHERE chatid = ? ORDER BY zeitpunkt ASC;";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, chatid);
            rs = stmt.executeQuery();
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean getIsGroup(int chatid) {
        ResultSet rs = null;

        try {
            String query = "SELECT isgroup FROM chats WHERE id = ?;";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, chatid);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getGroupName(int chatid) {
        ResultSet rs = null;

        try {
            String query = "SELECT v.name FROM chats c INNER JOIN veranstaltung v ON v.id = c.projektgruppeid WHERE isgroup = true AND c.id = ?;";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, chatid);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "(Name konnte nicht geladen werden!)";
    }

    private List<ChatNachricht> extractMessages(ResultSet resultSet, int userid) {
        List<ChatNachricht> messages = new ArrayList<>();
        try {
            while(resultSet.next()) {
                ChatNachricht message = getMessage(resultSet, userid);
                if (message != null) {
                    messages.add(message);
                }
            }
            return messages;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ChatNachricht getMessage(ResultSet rs, int userid) {
        try {
            String nachricht = rs.getString(1);
            ZonedDateTime zeitpunkt = ZonedDateTime.ofInstant(rs.getTimestamp(2).toInstant(), ZoneId.of("UTC"));
            int senderid = rs.getInt(3);
            String vorname = rs.getString(4);
            String nachname = rs.getString(5);
            return new ChatNachricht(nachricht, zeitpunkt, senderid, vorname, nachname, senderid == userid);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    private ResultSet getMembers(int chatid) {
        ResultSet rs = null;

        try {
            String query = "SELECT m.nutzerid, n.imageid, vorname, nachname FROM chatmitglieder m INNER JOIN nutzer n ON m.nutzerid = n.id WHERE chatid = ?;";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, chatid);
            rs = stmt.executeQuery();
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<String[]> extractMembers(ResultSet resultSet) {
        List<String[]> members = new ArrayList<>();
        try {
            while(resultSet.next()) {
                String[] member = getMember(resultSet);
                if (member != null) {
                    members.add(member);
                }
            }
            return members;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String[] getMember(ResultSet rs) {
        try {
            String nutzerid = Integer.toString(rs.getInt(1));
            String imageid = Integer.toString(rs.getInt(2));
            String name = rs.getString(3) + " " + rs.getString(4);
            return new String[] {nutzerid, imageid, name};
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    private boolean sendMessage(String message, int chatid, int userid) {
        ResultSet rs = null;
        try {
            String query = "INSERT INTO nachrichten (id, senderid, nachricht, chatid, zeitpunkt) VALUES (DEFAULT, ?, ?, ?, now() + INTERVAL '2 hours') RETURNING id;";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, userid);
            statement.setString(2, message);
            statement.setInt(3, chatid);
            rs = statement.executeQuery();
            if (rs.next()) {
                ResultSet rs2 = null;
                String query2 = "UPDATE chats SET lastmessageid = ? WHERE id = ? RETURNING lastmessageid;";
                PreparedStatement statement2 = con.prepareStatement(query2);
                statement2.setInt(1, rs.getInt(1));
                statement2.setInt(2, chatid);
                rs2 = statement2.executeQuery();
                if (rs2.next()) {
                    ResultSet rs3 = null;
                    String query3 = "UPDATE chatmitglieder SET nichtgelesen = nichtgelesen + 1 WHERE chatid = ? RETURNING nichtgelesen;";
                    PreparedStatement statement3 = con.prepareStatement(query3);
                    statement3.setInt(1, chatid);
                    rs3 = statement3.executeQuery();
                    if (rs3.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int createPrivateChat(int userid, int targetid) {
        ResultSet rs = null;
        try {
            String query = "INSERT INTO chats (id, isgroup, erstelldatum) VALUES (DEFAULT, false, now() + INTERVAL '2 hours') RETURNING id;";
            PreparedStatement statement = con.prepareStatement(query);
            rs = statement.executeQuery();
            if (rs.next()) {
                int chatid = rs.getInt(1);
                ResultSet rs2 = null;
                String query2 = "INSERT INTO chatmitglieder (chatid, nutzerid, nichtgelesen) VALUES (?, ?, 0) RETURNING nutzerid;";
                PreparedStatement statement2 = con.prepareStatement(query2);
                statement2.setInt(1, chatid);
                statement2.setInt(2, userid);
                rs2 = statement2.executeQuery();
                if (rs2.next()) {
                    ResultSet rs3 = null;
                    String query3 = "INSERT INTO chatmitglieder (chatid, nutzerid, nichtgelesen) VALUES (?, ?, 1) RETURNING nutzerid;";
                    PreparedStatement statement3 = con.prepareStatement(query3);
                    statement3.setInt(1, chatid);
                    statement3.setInt(2, targetid);
                    rs3 = statement3.executeQuery();
                    if (rs3.next()) {
                        ResultSet rs4 = null;
                        String query4 = "INSERT INTO nachrichten (id, senderid, nachricht, chatid, zeitpunkt) VALUES (DEFAULT, ?, ?, ?, now() + INTERVAL '2 hours') RETURNING id;";
                        PreparedStatement statement4 = con.prepareStatement(query4);
                        statement4.setInt(1, userid);
                        statement4.setString(2, "hat einen Chat mit dir gestartet");
                        statement4.setInt(3, chatid);
                        rs4 = statement4.executeQuery();
                        if (rs4.next()) {
                            ResultSet rs5 = null;
                            int lastmessageid = rs4.getInt(1);
                            String query5 = "UPDATE chats SET lastmessageid = ? WHERE id = ? RETURNING id;";
                            PreparedStatement statement5 = con.prepareStatement(query5);
                            statement5.setInt(1, lastmessageid);
                            statement5.setInt(2, chatid);
                            rs5 = statement5.executeQuery();
                            if (rs5.next()) {
                                return chatid;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static int createGroup(int userid, int groupid, Connection con) {
        try {
            String query = "INSERT INTO chats (id, isgroup, erstelldatum, projektgruppeid) VALUES (DEFAULT, true, now() + INTERVAL '2 hours', ?) RETURNING id;";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, groupid);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int chatid = rs.getInt(1);
                String query2 = "SELECT nutzerid FROM veranstaltungsteilnehmer WHERE veranstaltungsid = ?;";
                PreparedStatement statement2 = con.prepareStatement(query2);
                statement2.setInt(1, groupid);
                ResultSet rs2 = statement2.executeQuery();
                while (rs2.next()) {
                    String query3 = "INSERT INTO chatmitglieder (chatid, nutzerid, nichtgelesen) VALUES (?, ?, 1) RETURNING nutzerid;";
                    PreparedStatement statement3 = con.prepareStatement(query3);
                    statement3.setInt(1, chatid);
                    statement3.setInt(2, rs2.getInt(1));
                    statement3.executeQuery();
                }
                String query4 = "INSERT INTO nachrichten (id, senderid, nachricht, chatid, zeitpunkt) VALUES (DEFAULT, ?, ?, ?, now() + INTERVAL '2 hours') RETURNING id;";
                PreparedStatement statement4 = con.prepareStatement(query4);
                statement4.setInt(1, userid);
                statement4.setString(2, "hat einen Gruppen-Chatraum erstellt");
                statement4.setInt(3, chatid);
                ResultSet rs4 = statement4.executeQuery();
                if (rs4.next()) {
                    int lastmessageid = rs4.getInt(1);
                    String query5 = "UPDATE chats SET lastmessageid = ? WHERE id = ? RETURNING id;";
                    PreparedStatement statement5 = con.prepareStatement(query5);
                    statement5.setInt(1, lastmessageid);
                    statement5.setInt(2, chatid);
                    ResultSet rs5 = statement5.executeQuery();
                    if (rs5.next()) {
                        return chatid;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean addMember(int targetid, int groupid, Connection con) {
        try {
            String query = "SELECT id FROM chats WHERE projektgruppeid = ?;";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, groupid);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int chatid = rs.getInt(1);
                if (!isMember(targetid, chatid, con)) {
                    String query2 = "INSERT INTO chatmitglieder (chatid, nutzerid, nichtgelesen) VALUES (?, ?, 0) RETURNING nutzerid;";
                    PreparedStatement statement2 = con.prepareStatement(query2);
                    statement2.setInt(1, chatid);
                    statement2.setInt(2, targetid);
                    ResultSet rs2 = statement2.executeQuery();
                    if (rs2.next()) {
                        String query3 = "INSERT INTO nachrichten (id, senderid, nachricht, chatid, zeitpunkt) VALUES (DEFAULT, ?, ?, ?, now() + INTERVAL '2 hours') RETURNING id;";
                        PreparedStatement statement3 = con.prepareStatement(query3);
                        statement3.setInt(1, targetid);
                        statement3.setString(2, "ist der Gruppe beigetreten");
                        statement3.setInt(3, chatid);
                        ResultSet rs3 = statement3.executeQuery();
                        if (rs3.next()) {
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean removeMember(int targetid, int groupid, Connection con) {
        try {
            String query = "SELECT id FROM chats WHERE projektgruppeid = ?;";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, groupid);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int chatid = rs.getInt(1);
                if (isMember(targetid, chatid, con)) {
                    String query2 = "DELETE FROM chatmitglieder WHERE chatid = ? AND nutzerid = ? RETURNING nutzerid;";
                    PreparedStatement statement2 = con.prepareStatement(query2);
                    statement2.setInt(1, chatid);
                    statement2.setInt(2, targetid);
                    ResultSet rs2 = statement2.executeQuery();
                    if (rs2.next()) {
                        String query3 = "INSERT INTO nachrichten (id, senderid, nachricht, chatid, zeitpunkt) VALUES (DEFAULT, ?, ?, ?, now() + INTERVAL '2 hours') RETURNING id;";
                        PreparedStatement statement3 = con.prepareStatement(query3);
                        statement3.setInt(1, targetid);
                        statement3.setString(2, "hat die Gruppe verlassen");
                        statement3.setInt(3, chatid);
                        ResultSet rs3 = statement3.executeQuery();
                        if (rs3.next()) {
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isMember(int targetid, int chatid, Connection con) {
        try {
            String query = "SELECT nutzerid FROM chatmitglieder WHERE nutzerid = ? AND chatid = ?;";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, targetid);
            statement.setInt(2, chatid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())  return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}