package server.handler;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import server.db.DatabaseUtility;
import shared.FreundesListe;
import shared.LehrveranstaltungsListe;
import shared.Student;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FreundHinzufügenFreundeslisteHandler  extends AbstractHandler {

    Connection con = null;

    public FreundHinzufügenFreundeslisteHandler(Connection con) {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");

        if (baseRequest.getMethod().equals("GET") && splitTarget.length == 3 && splitTarget[1].equals("freundesliste")) {
            {
                if (splitTarget[2].equals("freunde")) {
                    int userid = -1;
                    String token;
                    try {
                        token = request.getParameter("token");
                        userid = Integer.parseInt(request.getParameter("userid"));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        baseRequest.setHandled(true);
                        return;
                    }
                    System.out.println("FreundHinzufuegenFreundeslisteHandler[GET]: userid: " + userid + " | token: " + token);
                    System.out.println("FreundHinzufuegenFreundeslisteHandler: Retrieved userID and token successfully.");
                    response.setStatus(HttpServletResponse.SC_OK);
                    baseRequest.setHandled(true);

                    FreundesListe liste;

                    System.out.println("FreundHinzufügenFreundeslisteHandler: Extracting...");
                    liste = DatabaseUtility.getallUser2(con, userid);
                   // FreundesListe otherlist = DatabaseUtility.getallUser2(con,userid);

                    if (liste == null) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        baseRequest.setHandled(true);
                        System.out.println("FreundHinzufügenFreundeslisteHandler: Fetching profil data failed.");
                        return;
                    }
                   /* for(Student s : otherlist.getList()){
                        liste.add(s);
                    }*/
                    response.getWriter().println(new Gson().toJson(liste, FreundesListe.class));
                    System.out.println("FreundHinzufügenFreundeslisteHandler: Done.");
                } else if (splitTarget[2].equals("anfragen")) {
                    int userid = -1;
                    String token;
                    try {
                        token = request.getParameter("token");
                        userid = Integer.parseInt(request.getParameter("userid"));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        baseRequest.setHandled(true);
                        return;
                    }
                    System.out.println("FreundHinzufügenFreundeslisteHandler[GET]: userid: " + userid + " | token: " + token);
                    System.out.println("FreundHinzufügenFreundeslisteHandler: Retrieved userID and token successfully.");
                    response.setStatus(HttpServletResponse.SC_OK);
                    baseRequest.setHandled(true);

                    FreundesListe liste;

                    System.out.println("FreundHinzufügenFreundeslisteHandler: Extracting...");
                    liste = DatabaseUtility.getAllRequests(con, userid);

                    if (liste == null) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        baseRequest.setHandled(true);
                        System.out.println("FreundHinzufügenFreundeslisteHandler: Fetching profil data failed.");
                        return;
                    }
                    response.getWriter().println(new Gson().toJson(liste, FreundesListe.class));
                    System.out.println("FreundHinzufügenFreundeslisteHandler: Done.");

                }

            }
        } else if (baseRequest.getMethod().equals("DELETE") && splitTarget.length == 3 && splitTarget[1].equals("freundesliste")) {
            if (splitTarget[2].equals("removeFriend")) {

                int userid, accountid;
                String token;

                try {
                    token = request.getParameter("token");
                    userid = Integer.parseInt(request.getParameter("userid"));
                    accountid = Integer.parseInt(request.getParameter("accountid"));
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    baseRequest.setHandled(true);
                    return;
                }
                System.out.println("FreundHinzufügenFreundesListeHandler[DELETE]: userid: " + userid + " | token: " + token + " | accountid: " + accountid);

                System.out.println("FreundHinzufügenFreundesListeHandler[Remove]: Start");

                if (isFriend(userid, accountid)) {// Prüfe, ob beide befreundet sind
                    if (removeFriend(userid, accountid)) {
                        System.out.println("FreundHinzufügenFreundesListeHandler[Remove]: user:" + accountid + " wurde erfolgreich entfernt!");
                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        System.out.println("FreundHinzufügenFreundesListeHandler[Remove]: user " + accountid + " konnte nicht entfernt werden!");
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }

                } else response.setStatus(HttpServletResponse.SC_CONFLICT);
                baseRequest.setHandled(true);
                System.out.println("FreundHinzufügenFreundesListeHandler[Remove]: Fertig");
            } else if (splitTarget[2].equals("removeRequest")) {
                int userid, accountid;
                String token;

                try {
                    token = request.getParameter("token");
                    userid = Integer.parseInt(request.getParameter("userid"));
                    accountid = Integer.parseInt(request.getParameter("accountid"));
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    baseRequest.setHandled(true);
                    return;
                }
                System.out.println("FreundHinzufügenFreundesListeHandler[DELETE]: userid: " + userid + " | token: " + token + " | accountid: " + accountid);

                System.out.println("FreundHinzufügenFreundesListeHandler[Remove]: Start");

                if (isRequest(userid, accountid)) {// Prüfe, ob beide befreundet sind
                    if (removeRequest(userid, accountid)) {
                        System.out.println("FreundHinzufügenFreundesListeHandler[Remove]: user:" + accountid + " wurde erfolgreich entfernt!");
                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        System.out.println("FreundHinzufügenFreundesListeHandler[Remove]: user " + accountid + " konnte nicht entfernt werden!");
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }

                } else response.setStatus(HttpServletResponse.SC_CONFLICT);
                baseRequest.setHandled(true);
                System.out.println("FreundHinzufügenFreundesListeHandler[Remove]: Fertig");
            }
        } else if (baseRequest.getMethod().equals("POST") && splitTarget.length == 3 && splitTarget[1].equals("freundesliste")) {
            if (splitTarget[2].equals("addFriend")) {
                int userid, accountid;
                String token;

                try {
                    token = request.getParameter("token");
                    userid = Integer.parseInt(request.getParameter("userid"));
                    accountid = Integer.parseInt(request.getParameter("accountid"));
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    baseRequest.setHandled(true);
                    return;
                }
                System.out.println("FreundHinzufügenFreundesListeHandler[POST]: userid: " + userid + " | token: " + token + " | accountid" + accountid);

                System.out.println("FreundHinzufügenFreundesListeHandler[Add]: Start");
                if (!(isFriend(userid, accountid))) { // Prüfe, ob Ziel bereits im Kurs ist
                    if (addFriend(userid, accountid)) {
                        System.out.println("FreundHinzufügenFreundesListeHandler[Add]: user '" + accountid + " wurde erfolgreich als Freund registriert!");
                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        System.out.println("FreundHinzufügenFreundesListeHandler[Add]: user '" + accountid + " konnte nicht als Freund zugewiesen werden!");
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                } else response.setStatus(HttpServletResponse.SC_CONFLICT);
                baseRequest.setHandled(true);
                System.out.println("FreundHinzufügenFreundesListeHandler[Add]: Fertig");
            } else if (splitTarget[2].equals("addRequest")) {
                int userid, accountid;
                String token;

                try {
                    token = request.getParameter("token");
                    userid = Integer.parseInt(request.getParameter("userid"));
                    accountid = Integer.parseInt(request.getParameter("accountid"));
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    baseRequest.setHandled(true);
                    return;
                }
                System.out.println("FreundHinzufügenFreundesListeHandler[POST]: userid: " + userid + " | token: " + token + " | accountid" + accountid);

                System.out.println("FreundHinzufügenFreundesListeHandler[Add]: Start");
                if (!(isRequest(accountid, userid)) && !(isFriend(accountid,userid))) { // Prüfe, ob Ziel bereits in Request ist
                    if (addRequest(userid, accountid)) {
                        System.out.println("FreundHinzufügenFreundesListeHandler[Add]: An '" + accountid + "' wurde eine Anfrage geschickt!");
                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        System.out.println("FreundHinzufügenFreundesListeHandler[Add]:An '" + accountid + " konnte keine Anfrage geschickt werden! ");
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                } else response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);
                System.out.println("FreundHinzufügenFreundesListeHandler[Add]: Fertig");

            } else if (baseRequest.getMethod().equals("POST") && splitTarget.length == 3 && splitTarget[1].equals("freundesliste")) {
                if (splitTarget[2].equals("addFriend")) {
                    int userid, accountid;
                    String token;

                    try {
                        token = request.getParameter("token");
                        userid = Integer.parseInt(request.getParameter("userid"));
                        accountid = Integer.parseInt(request.getParameter("accountid"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        baseRequest.setHandled(true);
                        return;
                    }
                    System.out.println("FreundHinzufügenFreundesListeHandler[POST]: userid: " + userid + " | token: " + token + " | accountid" + accountid);

                    System.out.println("FreundHinzufügenFreundesListeHandler[Add]: Start");
                    if (!(isFriend(userid, accountid))) { // Prüfe, ob Ziel bereits im Kurs ist
                        if (addFriend(userid, accountid)) {
                            System.out.println("FreundHinzufügenFreundesListeHandler[Add]: user '" + accountid + " wurde erfolgreich als Freund registriert!");
                            response.setStatus(HttpServletResponse.SC_OK);
                        } else {
                            System.out.println("FreundHinzufügenFreundesListeHandler[Add]: user '" + accountid + " konnte nicht als Freund zugewiesen werden!");
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        }
                    } else response.setStatus(HttpServletResponse.SC_CONFLICT);
                    baseRequest.setHandled(true);
                    System.out.println("FreundHinzufügenFreundesListeHandler[Add]: Fertig");
                } else if (splitTarget[2].equals("removeRequestaddFriend")) {
                    int userid, accountid;
                    String token;

                    try {
                        token = request.getParameter("token");
                        userid = Integer.parseInt(request.getParameter("userid"));
                        accountid = Integer.parseInt(request.getParameter("accountid"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        baseRequest.setHandled(true);
                        return;
                    }
                    System.out.println("FreundHinzufügenFreundesListeHandler[POST]: userid: " + userid + " | token: " + token + " | accountid" + accountid);

                    System.out.println("FreundHinzufügenFreundesListeHandler[Add]: Start");
                    if (!(isFriend(userid, accountid))) { // Prüfe, ob Ziel bereits in Request ist
                        if (addFriendremoveRequest(userid, accountid)) {
                            System.out.println("FreundHinzufügenFreundesListeHandler[Add]: Die Anfrage von '" + accountid + "' ");
                            response.setStatus(HttpServletResponse.SC_OK);
                        } else {
                            System.out.println("FreundHinzufügenFreundesListeHandler[Add]:An '" + accountid + " konnte keine Anfrage geschickt werden! ");
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        }
                    } else response.setStatus(HttpServletResponse.SC_CONFLICT);
                    baseRequest.setHandled(true);
                    System.out.println("FreundHinzufügenFreundesListeHandler[Add]: Fertig");

                }
            }
        }
    }
    private boolean isFriend(int userid, int accountid){
        int a  = userid;
        int b = accountid;
        try {
            String query = "SELECT student1_id,student2_id FROM friends WHERE (student1_id = ? AND student2_id = ?) OR (student1_id = ? AND student2_id = ?);";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, userid);
            statement.setInt(2, accountid);
            statement.setInt(3, b);
            statement.setInt(4, a);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())  return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }
    private boolean isRequest(int userid, int accountid){
        try {
            String query = "SELECT distinct nutzer1_id,nutzer2_id FROM freundschaftsanfragen WHERE nutzer1_id = ? AND nutzer2_id = ?;";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, accountid);
            statement.setInt(2, userid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())  return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    private boolean removeFriend(int userid, int accountid) {
        int a = userid;
        int b = accountid;
        try {
            String query = "DELETE FROM friends WHERE (student1_id = ? AND student2_id = ?) OR (student1_id = ? AND student2_id = ?) RETURNING student1_id;";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, userid);
            statement.setInt(2, accountid);
            statement.setInt(3, b);
            statement.setInt(4, a);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    private boolean removeRequest(int userid, int accountid) {
        int a = userid;
        int b = accountid;
        try {
            String query = "DELETE FROM freundschaftsanfragen WHERE (nutzer1_id = ? AND nutzer2_id = ?) OR (nutzer1_id = ? AND nutzer2_id = ?) RETURNING nutzer1_id;";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, accountid);
            statement.setInt(2, userid);
            statement.setInt(3, a);
            statement.setInt(4, b);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    private boolean addFriend(int userid, int accountid) {
        try {
            String query = "INSERT INTO friends (student1_id, student2_id) VALUES (?, ?) RETURNING student1_id;";
            String query2 = "INSERT INTO friends (student1_id, student2_id) VALUES (?, ?) RETURNING student2_id;";
            PreparedStatement statement = con.prepareStatement(query);
            PreparedStatement statement2= con.prepareStatement(query2);
            statement.setInt(1, userid);
            statement.setInt(2, accountid);
            statement2.setInt(1, accountid);
            statement2.setInt(2, userid);
            statement2.executeQuery();
            statement.executeQuery();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean addRequest(int userid, int accountid) {
        try {
            String query = "INSERT INTO freundschaftsanfragen (nutzer1_id, nutzer2_id) VALUES (?, ?) RETURNING nutzer1_id;";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, userid);
            statement.setInt(2, accountid);
            statement.executeQuery();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean addFriendremoveRequest(int userid, int accountid) {
        if(isRequest(userid,accountid) && !isFriend(userid,accountid)){
            removeRequest(userid,accountid);
            addFriend(userid,accountid);
            return true;
        }
        return false;
    }
}
