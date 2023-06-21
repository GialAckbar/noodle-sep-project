package server.handler;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import java.io.IOException;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class feedbackTestHandler extends AbstractHandler {
    Connection con;

    public feedbackTestHandler(Connection con) {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");

        if (baseRequest.getMethod().equals("GET") && splitTarget.length == 3 && splitTarget[1].equals("feedbackTest")) {
            switch (splitTarget[2]) {
                case "init": {
                    int id = generateFeedback();
                    response.getWriter().println(new Gson().toJson(id, Integer.class));
                    response.setStatus(HttpServletResponse.SC_OK);
                    baseRequest.setHandled(true);
                    break;
                }
//                case "passed": {
//                    int id = -1;
//                    response.getWriter().println(new Gson().toJson(id, Integer.class));
//                    response.setStatus(HttpServletResponse.SC_OK);
//                    baseRequest.setHandled(true);
//                    break;
//                }
//                case "number": {
//                    int id = -2;
//                    response.getWriter().println(new Gson().toJson(id, Integer.class));
//                    response.setStatus(HttpServletResponse.SC_OK);
//                    baseRequest.setHandled(true);
//                    break;
//                }
            }
        }
    }

    private int generateFeedback() {
        try {
            String query = "with bewertungsids as(insert into feedback values (default, ?) returning id), " +
                    "kategorieids as(insert into kategorieelement values (default, (select count(*) from kategorieelement where veranstaltungskategorieid = 707), 707) returning id) " +
                    "insert into feedbackzukategorie values (default, (select id from kategorieids), (select id from bewertungsids)) returning (select id from bewertungsids);";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, "Modultest " + getCurrentTimeDate());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                String frage = "insert into fb_fragen values (default, ?, 'Wähle ein Wort') returning id;";
                String antwort1 = "insert into fb_antworten values (default, ?, 'Was') returning id;";
                String antwort2 = "insert into fb_antworten values (default, ?, 'machen') returning id;";
                String antwort3 = "insert into fb_antworten values (default, ?, 'Sachen?') returning id;";
                String abgabeBESTANDEN = "INSERT INTO feedbackabgabe VALUES (default, ?, 136, now() + INTERVAL '2 hours', true) returning id;";
                String abgabeFAILED = "INSERT INTO feedbackabgabe VALUES (default, ?, 200, now() + INTERVAL '2 hours', false) returning id;";
                String abgabeANTWORT = "INSERT INTO fb_abgabeantworten VALUES (default, ?, 136, ?, ?) returning id; ";

                PreparedStatement frageStmt = con.prepareStatement(frage);
                frageStmt.setInt(1, id);
                ResultSet frageSet = frageStmt.executeQuery();
                if (frageSet.next()) {
                    int frageid = frageSet.getInt(1);
                    PreparedStatement antwort1Stmt = con.prepareStatement(antwort1);
                    PreparedStatement antwort2Stmt = con.prepareStatement(antwort2);
                    PreparedStatement antwort3Stmt = con.prepareStatement(antwort3);
                    antwort1Stmt.setInt(1, frageid);
                    antwort2Stmt.setInt(1, frageid);
                    antwort3Stmt.setInt(1, frageid);
                    ResultSet rs1 = antwort1Stmt.executeQuery();
                    ResultSet rs2 = antwort2Stmt.executeQuery();
                    ResultSet rs3 = antwort3Stmt.executeQuery();

                    if(rs1.next() && rs2.next() && rs3.next()) {
                        PreparedStatement abgabeBSTstmt = con.prepareStatement(abgabeBESTANDEN);
                        PreparedStatement abgabeBSTstmt2 = con.prepareStatement(abgabeBESTANDEN);
                        PreparedStatement abgabeFAILstmt = con.prepareStatement(abgabeFAILED);
                        abgabeBSTstmt.setInt(1, id);
                        abgabeBSTstmt2.setInt(1, id);
                        abgabeFAILstmt.setInt(1, id);
                        ResultSet rs4 = abgabeBSTstmt.executeQuery();
                        ResultSet rs5 = abgabeBSTstmt2.executeQuery();
                        ResultSet rs6 = abgabeFAILstmt.executeQuery();

                        if(rs4.next() && rs5.next() && rs6.next()) {
                            PreparedStatement abgabeANTstmt = con.prepareStatement(abgabeANTWORT);
                            PreparedStatement abgabeANTstmt2 = con.prepareStatement(abgabeANTWORT);
                            PreparedStatement abgabeANTstmt3 = con.prepareStatement(abgabeANTWORT);
                            abgabeANTstmt.setInt(1, rs4.getInt(1));
                            abgabeANTstmt.setInt(2, frageid);
                            abgabeANTstmt.setInt(3, rs3.getInt(1));
                            abgabeANTstmt2.setInt(1, rs5.getInt(1));
                            abgabeANTstmt2.setInt(2, frageid);
                            abgabeANTstmt2.setInt(3, rs3.getInt(1));
                            abgabeANTstmt3.setInt(1, rs6.getInt(1));
                            abgabeANTstmt3.setInt(2, frageid);
                            abgabeANTstmt3.setInt(3, rs3.getInt(1));
                            abgabeANTstmt.executeQuery();
                            abgabeANTstmt2.executeQuery();
                            abgabeANTstmt3.executeQuery();
                            return id;
                        }
                    }
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }

    private String getCurrentTimeDate() {
        ZonedDateTime now = ZonedDateTime.now();
        now.withZoneSameInstant(ZoneId.systemDefault());
        String day = (now.getDayOfMonth() < 10) ? "0" + now.getDayOfMonth() : String.valueOf(now.getDayOfMonth());
        String month = (now.getMonthValue() < 10) ? "0" + now.getMonthValue() : String.valueOf(now.getMonthValue());
        int year = now.getYear();
        String hour = (now.getHour() < 10) ? "0" + now.getHour() : String.valueOf(now.getHour());
        String minute = (now.getMinute() < 10) ? "0" + now.getMinute() : String.valueOf(now.getMinute());
        return day + "." + month + "." + year + " | " + hour + ":" + minute;
    }
}
