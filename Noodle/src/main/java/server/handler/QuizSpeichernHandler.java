package server.handler;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import server.db.DatabaseManager;
import shared.*;
import shared.accounts.LehrkraftmitPasswort;
import shared.accounts.StudentMitPasswort;
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

public class QuizSpeichernHandler extends AbstractHandler {
    Connection con;

    public QuizSpeichernHandler(Connection con) {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");
        if (baseRequest.getMethod().equals("POST") && splitTarget[1].equals("quizspeichern")) {

            boolean responseSet = false;

            Quiz quiz = null;

            try {

                quiz = GsonUtility.getGson().fromJson(request.getReader(), Quiz.class);
            } catch (Exception e) {

                System.out.println("Gson unsuccessful");

            }
            if (quiz != null) {

                int id = createFragenUndAntwort(quiz);


                if (id >= 0) {

                    try {

                        int quizid = -1;

                        String query = "insert into quiz values (default, ?, ?, ?) returning id;";
                        PreparedStatement stmt = con.prepareStatement(query);

                        stmt.setString(2, quiz.getAnzeigename());
                        stmt.setString(3, quiz.getOpenDateString());
                        stmt.setString(4, quiz.getCloseDateString());


                        ResultSet rs = stmt.executeQuery();
                        rs.next();
                        quizid = rs.getInt(1);

                        response.getWriter().println(new Gson().toJson(quizid, Integer.class));
                        responseSet = true;


                    } catch (SQLException e) {

                        e.printStackTrace();

                    }
                }
            }
            if (responseSet) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.getWriter().println(new Gson().toJson(-1, Integer.class));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

            baseRequest.setHandled(true);
        }
    }

    private int createFragenUndAntwort(Quiz quiz) {
        int fragenid = createFragen(quiz.getFragen(), quiz.getId());
        int antwortid = -1;
        if (fragenid >= 0) {
            antwortid = createAntworten(quiz.getFragen(), fragenid);
        }
        return antwortid;
    }

    private int createFragen(List<Quizfrage> frage, int quizid) {
        ResultSet rs = null;

        int id = -1;
        for(Quizfrage fragen : frage) {

            String query = "insert into fragen values(default,?,?) returning id;";

            try {
                PreparedStatement stmt = con.prepareStatement(query);

                stmt.setInt(1, quizid);
                stmt.setString(2, fragen.getFrage());

                rs = stmt.executeQuery();
                rs.next();
                id = rs.getInt(1);

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return id;
    }

    private int createAntworten(List<Quizfrage> frage, int fragenid) {
        ResultSet rs = null;
        int id = -1;
        for(Quizfrage fragen : frage) {

            for (QuizAntwort antwort : fragen.getAntworten()) {
                String query = "insert into antworten values(default,?,?,?) returning id;";

                try {
                    PreparedStatement stmt = con.prepareStatement(query);

                    stmt.setInt(1, fragenid);
                    stmt.setString(2, antwort.getAntwort());
                    stmt.setBoolean(3, antwort.getRichtig());
                    rs = stmt.executeQuery();
                    rs.next();
                    id = rs.getInt(1);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return id;

    }
}