package server.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import shared.quiz.Quiz;
import shared.quiz.QuizAntwort;
import shared.quiz.Quizfrage;
import shared.utility.GsonUtility;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class QuizLehrerAufrufenHandler extends AbstractHandler {
    Connection con;

    public QuizLehrerAufrufenHandler(Connection con) {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");
        if (baseRequest.getMethod().equals("GET") && splitTarget[1].equals("quizLehrerAufrufen")) {

            int quizid;

            try {
                quizid = Integer.parseInt(request.getParameter("quizid"));
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                baseRequest.setHandled(true);
                return;
            }

            Quiz quiz = getQuiz(quizid);
            if (quiz == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                baseRequest.setHandled(true);
                return;
            }

            response.getWriter().println(GsonUtility.getGson().toJson(quiz, Quiz.class));
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
        }
    }

    private Quiz getQuiz(int quizid) {
        try {
            String query = "select count(*) from quizabgabe where quizid = ?;";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, quizid);
            ResultSet countSet = stmt.executeQuery();
            if (countSet.next()) {
                int counter = countSet.getInt(1);
                query = "select a.frageid, a.antwortid from abgabeantworten a inner join quizabgabe q on a.abgabeid = q.id where q.quizid = ? order by a.frageid asc;";
                stmt = con.prepareStatement(query);
                stmt.setInt(1, quizid);
                ResultSet selectedSet = stmt.executeQuery();
                LinkedHashMap<Integer, HashMap<Integer, Integer>> selected = new LinkedHashMap<>();
                while (selectedSet.next()) {
                    int frageid = selectedSet.getInt(1);
                    int antwortid = selectedSet.getInt(2);
                    if (!selected.containsKey(frageid)) {
                        selected.put(frageid, new HashMap<>());
                    }
                    if (selected.get(frageid).containsKey(antwortid)) {
                        selected.get(frageid).put(antwortid, selected.get(frageid).get(antwortid) + 1);
                    } else {
                        selected.get(frageid).put(antwortid, 1);
                    }
                }
                query = "select name from quiz where id = ?;";
                stmt = con.prepareStatement(query);
                stmt.setInt(1, quizid);
                ResultSet quiznameSet = stmt.executeQuery();
                if (quiznameSet.next()) {
                    query = "select id, frage from fragen where quizid = ? order by id asc;";
                    stmt = con.prepareStatement(query);
                    stmt.setInt(1, quizid);
                    ResultSet fragenSet = stmt.executeQuery();
                    List<Quizfrage> quiz = new ArrayList<>();
                    while (fragenSet.next()) {
                        int fragenid = fragenSet.getInt(1);
                        String frage = fragenSet.getString(2);
                        query = "select antwort, richtig, id from antworten where fragenid = ?;";
                        stmt = con.prepareStatement(query);
                        stmt.setInt(1, fragenid);
                        ResultSet antwortenSet = stmt.executeQuery();
                        List<QuizAntwort> antworten = new ArrayList<>();
                        while (antwortenSet.next()) {
                            String antwortString = antwortenSet.getString(1);
                            int antwortid = antwortenSet.getInt(3);
                            int anzahl = 0;
                            if (selected.containsKey(fragenid)) {
                                anzahl = selected.get(fragenid).getOrDefault(antwortid, 0);
                            }
                            antwortString = antwortString + " (" + anzahl + " von " + counter + ")";
                            QuizAntwort antwort = new QuizAntwort(antwortString, antwortenSet.getBoolean(2), antwortid);
                            antworten.add(antwort);
                        }
                        quiz.add(new Quizfrage(frage, antworten, fragenid));
                    }
                    String anzeigename = quiznameSet.getString(1);
                    ZonedDateTime now = ZonedDateTime.now();
                    now.withZoneSameInstant(ZoneId.systemDefault());
                    return new Quiz(quizid, anzeigename, now, now, quiz, 0);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
