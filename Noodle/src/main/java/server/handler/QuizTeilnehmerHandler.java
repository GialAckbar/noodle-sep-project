package server.handler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import shared.FreundesListe;
import shared.Student;
import shared.quiz.Quiz;
import shared.quiz.QuizAntwort;
import shared.quiz.Quizfrage;
import shared.quiz.Quizstatistik;
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

public class QuizTeilnehmerHandler extends AbstractHandler {

    Connection con = null;

    public QuizTeilnehmerHandler(Connection con) {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");

        if (baseRequest.getMethod().equals("GET") && splitTarget.length == 2 && splitTarget[1].equals("teilnehmerAnzeigen")) {
            int quizid = -1;
            int kursid = -1;
            String token = null;


            try {
                token = request.getParameter("token");
                quizid = Integer.parseInt(request.getParameter("quizid"));
                kursid = Integer.parseInt(request.getParameter("kursid"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                baseRequest.setHandled(true);
                return;
            }

            Quizstatistik freundesListe = getTeilnehmer(quizid, kursid);
            if (freundesListe == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                baseRequest.setHandled(true);
                return;
            }

            response.getWriter().println(GsonUtility.getGson().toJson(freundesListe, Quizstatistik.class));
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);

        }
    }

    private Quizstatistik getTeilnehmer(int quizid, int kursid) {

        int beteiligte = -1;
        int total = -1;
        int bestanden = -1;
        int abgaben = -1;
        try {
            String query = "select count(distinct nutzerid) FROM quizabgabe where quizid = ?;";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, quizid);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            beteiligte = rs.getInt(1);

            query = "select count(*) from veranstaltungsteilnehmer v inner join student s on v.nutzerid = s.nutzerid where veranstaltungsid = ?;";
            stmt = con.prepareStatement(query);
            stmt.setInt(1, kursid);
            ResultSet rs2 = stmt.executeQuery();
            rs2.next();
            total = rs2.getInt(1);

            query = "select count(*) from quizabgabe where quizid = ? AND bestanden = true;";
            stmt = con.prepareStatement(query);
            stmt.setInt(1, quizid);
            ResultSet rs3 = stmt.executeQuery();
            rs3.next();
            bestanden = rs3.getInt(1);

            query = "select count(*) from quizabgabe where quizid = ?;";
            stmt = con.prepareStatement(query);
            stmt.setInt(1, quizid);
            ResultSet rs4 = stmt.executeQuery();
            rs4.next();
            abgaben = rs4.getInt(1);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        Quizstatistik liste = new Quizstatistik(beteiligte, total, bestanden, abgaben);
        try {
            String query = "Select vorname, nachname, email, matrikelnummer, s.nutzerid, quizid From student s inner join nutzer n on s.nutzerid = n.id inner join quizabgabe q on n.id = q.nutzerid Where quizid = ?";

            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setInt(1, quizid);

            ResultSet rs = stmt.executeQuery();
            try {
                while (rs.next()) {
                    Student student = new Student(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getInt(5), rs.getInt(6));
                    liste.add(student);
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

}
