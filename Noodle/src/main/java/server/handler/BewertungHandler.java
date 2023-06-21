package server.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import shared.quiz.QuizAntwort;
import shared.quiz.Quizfrage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BewertungHandler extends AbstractHandler {

    Connection con;

    public BewertungHandler(Connection con) {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");

        if (baseRequest.getMethod().equals("GET") && splitTarget.length >= 3 && splitTarget[1].equals("bewertung")) {

            int userid, bewertungid, courseid;

            try {
                userid = Integer.parseInt(request.getParameter("userid"));
                bewertungid = Integer.parseInt(request.getParameter("bewertungid"));
                courseid = Integer.parseInt(request.getParameter("courseid"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                baseRequest.setHandled(true);
                return;
            }


            if (splitTarget[2].equals("permission")) {

                if (!alreadySubmitted(bewertungid, userid)) {
                    int[] counts = getCounts(courseid, userid);
                    if (counts == null) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    } else {
                        response.getWriter().println(new Gson().toJson(counts, int[].class));
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
                baseRequest.setHandled(true);

            }

            else if (splitTarget[2].equals("load")) {

                shared.Bewertung bewertung = getBewertung(bewertungid, userid, courseid);
                if (bewertung == null) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } else {
                    response.getWriter().println(new Gson().toJson(bewertung, shared.Bewertung.class));
                    response.setStatus(HttpServletResponse.SC_OK);
                }
                baseRequest.setHandled(true);
            }

        }

        else if (baseRequest.getMethod().equals("POST") && splitTarget.length == 2 && splitTarget[1].equals("bewertung")) {

            int userid, bewertungid, courseid;
            HashMap<Integer, Integer> selected;

            try {
                userid = Integer.parseInt(request.getParameter("userid"));
                bewertungid = Integer.parseInt(request.getParameter("bewertungid"));
                courseid = Integer.parseInt(request.getParameter("courseid"));
                selected = new Gson().fromJson(request.getReader(), new TypeToken<HashMap<Integer, Integer>>(){}.getType());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                baseRequest.setHandled(true);
                return;
            }

            if (save(bewertungid, userid, courseid, selected)) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            baseRequest.setHandled(true);

        }

    }

    private shared.Bewertung getBewertung(int bewertungid, int userid, int courseid) {
        try {
            String query = "SELECT name FROM feedback WHERE id = ?;";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, bewertungid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                query = "SELECT COUNT(DISTINCT quizid) FROM quizabgabe WHERE nutzerid = ? AND bestanden = true AND quizid IN (SELECT q.id FROM quiz q INNER JOIN " +
                        "quizzukategorie qk ON q.id = qk.quizid INNER JOIN kategorieelement k ON qk.kategorieelementid = k.id INNER JOIN veranstaltungskategorie vk ON " +
                        "k.veranstaltungskategorieid = vk.id WHERE vk.veranstaltungsid = ?);";
                stmt = con.prepareStatement(query);
                stmt.setInt(1, userid);
                stmt.setInt(2, courseid);
                ResultSet rs2 = stmt.executeQuery();
                if (rs2.next()) {
                    query = "SELECT id, frage FROM fb_fragen WHERE fb_id = ? ORDER BY id ASC;";
                    stmt = con.prepareStatement(query);
                    stmt.setInt(1, bewertungid);
                    ResultSet rs3 = stmt.executeQuery();
                    List<Quizfrage> fragen = new ArrayList<>();
                    while (rs3.next()) {
                        int fragenid = rs3.getInt(1);
                        String frage = rs3.getString(2);
                        query = "SELECT antwort, id FROM fb_antworten WHERE fb_fragenid = ?;";
                        stmt = con.prepareStatement(query);
                        stmt.setInt(1, fragenid);
                        ResultSet rs4 = stmt.executeQuery();
                        List<QuizAntwort> antworten = new ArrayList<>();
                        while (rs4.next()) {
                            QuizAntwort antwort = new QuizAntwort(rs4.getString(1), rs4.getInt(2));
                            antworten.add(antwort);
                        }
                        fragen.add(new Quizfrage(frage, antworten, fragenid));
                    }
                    String titel = rs.getString(1);
                    int[] counts = getCounts(courseid, userid);
                    int total = counts[0];
                    int accomplished = counts[1];
                    int passed = rs2.getInt(1);
                    return new shared.Bewertung(bewertungid, titel, fragen, total, accomplished, passed);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean alreadySubmitted(int bewertungid, int userid) {
        try {
            String query = "SELECT nutzerid FROM feedbackabgabe WHERE nutzerid = ? AND feedbackid = ?;";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, userid);
            stmt.setInt(2, bewertungid);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return false;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }

    private int[] getCounts(int courseid, int userid) {
        try {
            String query = "SELECT COUNT(DISTINCT q.id) FROM quiz q INNER JOIN quizzukategorie qk ON q.id = qk.quizid INNER JOIN kategorieelement k ON " +
                    "qk.kategorieelementid = k.id INNER JOIN veranstaltungskategorie vk ON k.veranstaltungskategorieid = vk.id WHERE vk.veranstaltungsid = ?;";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, courseid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                query = "SELECT COUNT(DISTINCT quizid) FROM quizabgabe WHERE nutzerid = ? AND quizid IN (SELECT q.id FROM quiz q INNER JOIN quizzukategorie qk ON " +
                        "q.id = qk.quizid INNER JOIN kategorieelement k ON qk.kategorieelementid = k.id INNER JOIN veranstaltungskategorie vk ON " +
                        "k.veranstaltungskategorieid = vk.id WHERE vk.veranstaltungsid = ?)";
                stmt = con.prepareStatement(query);
                stmt.setInt(1, userid);
                stmt.setInt(2, courseid);
                ResultSet rs2 = stmt.executeQuery();
                if (rs2.next()) {
                    return new int[] {rs.getInt(1), rs2.getInt(1)};
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean save(int bewertungid, int userid, int courseid, HashMap<Integer, Integer> selected) {
        try {
            String query = "SELECT COUNT(DISTINCT q.id) FROM quiz q INNER JOIN quizzukategorie qk ON q.id = qk.quizid INNER JOIN kategorieelement k ON " +
                    "qk.kategorieelementid = k.id INNER JOIN veranstaltungskategorie vk ON k.veranstaltungskategorieid = vk.id WHERE vk.veranstaltungsid = ?;";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, courseid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                query = "SELECT COUNT(DISTINCT quizid) FROM quizabgabe WHERE nutzerid = ? AND bestanden = true AND quizid IN (SELECT q.id FROM quiz q INNER JOIN " +
                        "quizzukategorie qk ON q.id = qk.quizid INNER JOIN kategorieelement k ON qk.kategorieelementid = k.id INNER JOIN veranstaltungskategorie vk ON " +
                        "k.veranstaltungskategorieid = vk.id WHERE vk.veranstaltungsid = ?);";
                stmt = con.prepareStatement(query);
                stmt.setInt(1, userid);
                stmt.setInt(2, courseid);
                ResultSet rs2 = stmt.executeQuery();
                if (rs2.next()) {
                    query = "INSERT INTO feedbackabgabe (id, feedbackid, nutzerid, eingereicht, bestanden) VALUES (DEFAULT, ?, ?, now() + INTERVAL '2 hours', ?) RETURNING id;";
                    stmt = con.prepareStatement(query);
                    stmt.setInt(1, bewertungid);
                    stmt.setInt(2, userid);
                    double calc = (double) rs.getInt(1) / (double) rs2.getInt(1);
                    stmt.setBoolean(3, !(calc > 2));
                    ResultSet rs3 = stmt.executeQuery();
                    if (rs3.next()) {
                        int abgabeid = rs3.getInt(1);
                        query = "INSERT INTO fb_abgabeantworten (id, abgabeid, nutzerid, frageid, antwortid) VALUES (DEFAULT, ?, ?, ?, ?) RETURNING id;";
                        for (Integer frageid : selected.keySet()) {
                            stmt = con.prepareStatement(query);
                            stmt.setInt(1, abgabeid);
                            stmt.setInt(2, userid);
                            stmt.setInt(3, frageid);
                            stmt.setInt(4, selected.get(frageid));
                            ResultSet rs4 = stmt.executeQuery();
                            if (!rs4.next()) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }
}