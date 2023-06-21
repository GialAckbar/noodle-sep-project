package server.handler;

import com.google.gson.Gson;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import server.db.DatabaseUtility;
import shared.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class LiteraturverzeichnisBibTexHandler extends AbstractHandler {

    Connection con = null;

    public LiteraturverzeichnisBibTexHandler(Connection con) {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");

        if (baseRequest.getMethod().equals("POST") && splitTarget.length == 2 && splitTarget[1].equals("postliteraturverzeichnis")) {
            baseRequest.setHandled(true);

            int userid;

            try {
                userid = Integer.parseInt(request.getParameter("userid"));
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                baseRequest.setHandled(true);
                return;
            }

            con = DatabaseUtility.getValidConnection(con);
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            ThemenangebotLiteratur themaLiteratur = new Gson().fromJson(request.getReader(), ThemenangebotLiteratur.class);

            LiteraturverzeichnisListe list = themaLiteratur.getListe();
            Themenangebot themenangebot = themaLiteratur.getThemenangebot();

                   if(themaLiteratur != null) {

                       if (addLiteratur(themaLiteratur, userid)) {
                           System.out.println("LiteraturverzeichnisListeBibTexHandler[Add]: Literaturverzeichnis wurde erfolgreich hinzugefügt!");
                           response.setStatus(HttpServletResponse.SC_OK);
                       } else {
                           System.out.println("LiteraturverzeichnisListeBibTexHandler[Add]: Literaturverzeichnis konnte nicht erfolgreich hinzugefügt werden!");
                           response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                       }
                   }else response.setStatus(HttpServletResponse.SC_CONFLICT);
                baseRequest.setHandled(true);
                System.out.println("LiteraturverzeichnisListeBibTexHandler[Add]: Fertig");

        } else if (baseRequest.getMethod().equals("GET") && splitTarget.length == 2 && splitTarget[1].equals("getliteraturverzeichnis")) {
            int userid, thid = -1;
            String token;
            try {
                token = request.getParameter("token");
                userid = Integer.parseInt(request.getParameter("userid"));
                thid = Integer.parseInt(request.getParameter("userid"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                baseRequest.setHandled(true);
                return;
            }
            System.out.println("LiteraturverzeichnisListeBibTexHandler[GET]: userid: " + userid + " | token: " + token);
            System.out.println("LiteraturverzeichnisListeBibTexHandler: Retrieved userID and token successfully.");
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);

            LiteraturverzeichnisListe liste;
            System.out.println("LiteraturverzeichnisListeBibTexHandler: Extracting...");
            liste = DatabaseUtility.getAllLiteraturen(con, thid);

            if (liste == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                baseRequest.setHandled(true);
                System.out.println("LiteraturverzeichnisListeBibTexHandler: Fetching literature data failed.");
                return;
            }
            response.getWriter().println(new Gson().toJson(liste, LiteraturverzeichnisListe.class));
            System.out.println("LiteraturverzeichnisListeBibTexHandler: Done.");

        }
    }

    private Boolean existing(String title, String author, String jahr, int id) {
        try {
            String query = "SELECT id,title,autor,jahr,thid FROM literaturverzeichnis WHERE (default ,?,?,?,?) returning thid;";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setString(1, title);
            statement.setString(2, author);
            statement.setString(3, jahr);
            statement.setInt(4, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    private boolean addLiteratur(ThemenangebotLiteratur v, int userid) {
        try {
            String query = "insert into thema (id, titel, beschreibung) values (default, ?, ?) returning id;";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, v.getThemenangebot().getTitel());
            stmt.setString(2,v.getThemenangebot().getBeschreibung());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int thid = rs.getInt(1);
                addThemenangebot(thid,userid);
                for (Literaturverzeichnis l : v.getListe().getList()) {
                    query = "INSERT INTO literaturverzeichnis (id,title, autor, jahr,art, thid) VALUES (default,?, ?, ?, ?, ?) returning thid;";
                    stmt = con.prepareStatement(query);
                    stmt.setString(1, l.getTitel());
                    stmt.setString(2, l.getAutor());
                    stmt.setString(3, l.getJahr());
                    stmt.setString(4,l.getArt());
                    stmt.setInt(5, thid);
                    stmt.executeQuery();

                }
                return true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    private boolean addThemenangebot(int thid,int userid){
       try {
            String query = "insert into themenangebote (id, thid, userid) values (default, ?, ?) returning id;";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, thid);
            stmt.setInt(2, userid);
            stmt.executeQuery();
            return true;
        } catch (SQLException e) {
           e.printStackTrace();
       }
        return false;

    }
}
