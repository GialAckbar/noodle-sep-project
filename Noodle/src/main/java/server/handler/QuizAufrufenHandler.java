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
import java.util.List;

public class QuizAufrufenHandler extends AbstractHandler {

    Connection con = null;

    public QuizAufrufenHandler(Connection con) {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");

        if (baseRequest.getMethod().equals("GET") && splitTarget.length == 2 && splitTarget[1].equals("quizAufrufen")) {
            int quizid = -1, userid = -1;
            String token = null;
            baseRequest.setHandled(true);

            try {
                token = request.getParameter("token");
                userid = Integer.parseInt(request.getParameter("userid"));
                quizid = Integer.parseInt(request.getParameter("quizid"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                baseRequest.setHandled(true);
                return;
            }

            Quiz quiz = getQuiz(quizid, userid);
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

    private Quiz getQuiz(int quizid, int userid) {
        try {
            String quizQuery = "select name, freigabe, ende from quiz where id = ?;";
            PreparedStatement quizStmt = con.prepareStatement(quizQuery);
            quizStmt.setInt(1, quizid);
            ResultSet quizRs = quizStmt.executeQuery();
            if (quizRs.next()) {
                String queryVersuche = "select count(*) from quizabgabe where quizid = ? and nutzerid = ?;";
                PreparedStatement stmtVersuche = con.prepareStatement(queryVersuche);
                stmtVersuche.setInt(1, quizid);
                stmtVersuche.setInt(2, userid);
                ResultSet rsVersuche = stmtVersuche.executeQuery();
                if (rsVersuche.next()) {
                    String query = "select id, frage from fragen where quizid = ? order by id asc;";
                    PreparedStatement stmt = con.prepareStatement(query);
                    stmt.setInt(1, quizid);
                    ResultSet rs = stmt.executeQuery();
                    List<Quizfrage> quiz = new ArrayList<>();
                    while (rs.next()) {
                        int fragenid = rs.getInt(1);
                        String frage = rs.getString(2);
                        String query2 = "select antwort, richtig, id from antworten where fragenid = ?;";
                        PreparedStatement stmt2 = con.prepareStatement(query2);
                        stmt2.setInt(1, fragenid);
                        ResultSet rs2 = stmt2.executeQuery();
                        List<QuizAntwort> antworten = new ArrayList<>();
                        while (rs2.next()) {
                            QuizAntwort antwort = new QuizAntwort(rs2.getString(1), rs2.getBoolean(2), rs2.getInt(3));
                            antworten.add(antwort);
                        }
                        quiz.add(new Quizfrage(frage, antworten, fragenid));
                    }
                    String anzeigename = quizRs.getString(1);
                    int versuche = rsVersuche.getInt(1);
                    ZonedDateTime now = ZonedDateTime.now();
                    now.withZoneSameInstant(ZoneId.systemDefault());
                    return new Quiz(quizid, anzeigename, now, now, quiz, versuche);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}