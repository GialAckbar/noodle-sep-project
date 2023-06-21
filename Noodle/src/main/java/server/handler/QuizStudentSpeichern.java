package server.handler;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import shared.quiz.Quizabgabe;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QuizStudentSpeichern extends AbstractHandler {
    Connection con;

    public QuizStudentSpeichern(Connection con) {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");
        if (baseRequest.getMethod().equals("POST") && splitTarget[1].equals("bearbeitungSpeichern")) {

            int quizid, userid;
            Quizabgabe quiz;

            try {
                userid = Integer.parseInt(request.getParameter("userid"));
                quizid = Integer.parseInt(request.getParameter("quizid"));
                quiz = new Gson().fromJson(request.getReader(), Quizabgabe.class);
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                baseRequest.setHandled(true);
                return;
            }

            try {
                String query = "insert into quizabgabe (id, quizid, nutzerid, eingereicht, gesamtpunktzahl, erzieltepunkte, bestanden) values (default, ?, ?, now() + interval '2 hours', ?, ?, ?) returning id;";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setInt(1, quizid);
                stmt.setInt(2, userid);
                stmt.setInt(3, quiz.getGesamtpunktzahl());
                stmt.setInt(4, quiz.getErreichtepunktzahl());
                stmt.setBoolean(5, quiz.isBestanden());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int abgabeid = rs.getInt(1);
                    for (Integer frageid : quiz.getSelected().keySet()) {
                        for (Integer antwortid : quiz.getSelected().get(frageid)) {
                            query = "insert into abgabeantworten (id, abgabeid, nutzerid, frageid, antwortid) values (default, ?, ?, ?, ?) returning id;";
                            stmt = con.prepareStatement(query);
                            stmt.setInt(1, abgabeid);
                            stmt.setInt(2, userid);
                            stmt.setInt(3, frageid);
                            stmt.setInt(4, antwortid);
                            stmt.executeQuery();
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                baseRequest.setHandled(true);
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
        }
    }
}