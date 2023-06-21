package server.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import shared.*;
import shared.quiz.QuizAntwort;
import shared.quiz.Quizfrage;
import shared.utility.GsonUtility;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class feedbackHandler extends AbstractHandler {
    Connection con;

    public feedbackHandler(Connection con)
    {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] splitTarget = target.split("/");

        if(baseRequest.getMethod().equals("GET") && splitTarget.length >=2 && splitTarget[1].equals("feedback")) {
            System.out.println("FeedbackHandler: Starting...");

                int fbid;

                try {
                    fbid = Integer.parseInt(request.getParameter("fbid"));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    baseRequest.setHandled(true);
                    return;
                }

                System.out.println("FeedbackHandler: Retrieved feedbackid successfully.");


                ResultSet questions = getAllQuestions(fbid);
                ResultSet ansWcntP = getAnswersWithCountP(fbid);
                ResultSet ansWcntF = getAnswersWithCountF(fbid);

                if (ansWcntP == null || ansWcntF == null || questions == null) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    baseRequest.setHandled(true);
                    System.out.println("FeedbackHandler: Fetching questions, passedAnswers or failedAnswers failed.");
                    return;
                }
                System.out.println("FeedbackHandler: Fetched questions, passedAnswers and failedAnswers.");

                System.out.println("FeedbackHandler: Extracting questions...");
                List<Quizfrage> qF = extractQF(questions);
                System.out.println("FeedbackHandler: Extraction complete.");

                System.out.println("FeedbackHandler: Extracting answers...");
                List<QuizAntMitAnz> passedA = extractAnswers(ansWcntP);
                List<QuizAntMitAnz> failedA = extractAnswers(ansWcntF);
                System.out.println("FeedbackHandler: Extraction complete.");

                String feedbackName = getName(fbid);

                Feedback f = new Feedback(qF, failedA, passedA, feedbackName);

                System.out.println("FeedbackHandler: Done.");
                System.out.println("FeedbackHandler: To client...");
                response.getWriter().println(GsonUtility.getGson().toJson(f, Feedback.class));
                response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);
                System.out.println("FeedbackHandler: Done.");

        }
    }

    private ResultSet getAllQuestions(int fbid) {
        ResultSet rs;
        try {
            String query = "SELECT distinct f.id, f.frage, a.id, a.antwort from fb_fragen f inner join fb_antworten a on a.fb_fragenid = f.id WHERE f.fb_id = ?;";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, fbid);

            rs = stmt.executeQuery();
            return rs;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    private ResultSet getAnswersWithCountP(int fbid) {
        ResultSet rs;
        try {
            String query = "SELECT f.id frageID, a.id antwortID, a.antwort, count(antwort) from fb_fragen f inner join fb_abgabeantworten aa " +
                    "on f.id = aa.frageid inner join fb_antworten a on aa.antwortid = a.id inner join feedbackabgabe fa on aa.abgabeid = fa.id " +
                    "where fa.feedbackid = ? AND bestanden = true group by f.id, a.id";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, fbid);

            rs = stmt.executeQuery();
            return rs;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ResultSet getAnswersWithCountF(int fbid) {
        ResultSet rs;
        try {
            String query = "SELECT f.id frageID, a.id antwortID, a.antwort, count(antwort) from fb_fragen f inner join fb_abgabeantworten aa " +
                    "on f.id = aa.frageid inner join fb_antworten a on aa.antwortid = a.id inner join feedbackabgabe fa on aa.abgabeid = fa.id " +
                    "where fa.feedbackid = ? AND bestanden = false group by f.id, a.id";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, fbid);

            rs = stmt.executeQuery();
            return rs;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Quizfrage> extractQF(ResultSet rs) {
        List<Quizfrage> qF = new ArrayList<>();

        try {
            while(rs.next()) {
                boolean exists = false;
                for(Quizfrage q : qF) {
                    if(q.getFrage().equals(rs.getString(2))) {
                        q.getAntworten().add(new QuizAntwort(rs.getString(4), rs.getInt(3)));
                        exists = true;
                    }
                }
                if(!exists) {
                    Quizfrage q = new Quizfrage(rs.getString(2));
                    q.getAntworten().add(new QuizAntwort(rs.getString(4), rs.getInt(3)));
                    qF.add(q);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return qF;
    }


    private List<QuizAntMitAnz> extractAnswers(ResultSet rs) {
        List<QuizAntMitAnz> qA = new ArrayList<>();

        try {
            while(rs.next()) {
                QuizAntMitAnz s = new QuizAntMitAnz(new QuizAntwort(rs.getString(3), rs.getInt(2)), rs.getInt(4));
                qA.add(s);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return qA;
    }

    private String getName(int fbid) {
        ResultSet rs;
        try {
            String query = "SELECT name from feedback where id = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, fbid);

            rs = stmt.executeQuery();

            rs.next();
            return rs.getString(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


}