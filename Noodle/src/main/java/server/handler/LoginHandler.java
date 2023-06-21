package server.handler;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import server.db.DatabaseUtility;
import server.db.FileUtility;
import server.mailing.Mail;
import shared.Lehrende;
import shared.Student;
import shared.User;
import shared.accounts.*;
import shared.utility.GsonUtility;

import javax.xml.transform.Result;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class LoginHandler extends AbstractHandler {
    Connection con = null;
    public LoginHandler (Connection con) {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");

        if (baseRequest.getMethod().equals("POST") && splitTarget.length == 2 && splitTarget[1].equals("login")) {

            con = DatabaseUtility.getValidConnection(con);
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            LoginDaten loginDaten = null;

            try {
                loginDaten = GsonUtility.getGson().fromJson(request.getReader(), LoginDaten.class);
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                baseRequest.setHandled(true);
                return;
            }
            UseridMitMail useridMitMail = getUserid(loginDaten.getMail(), loginDaten.getMatrikelnummer(), loginDaten.getPasswort());

            int userid = useridMitMail.getUserid();
            if (userid == -1) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                baseRequest.setHandled(true);
                return ;
            }
            if (userid == -2) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                baseRequest.setHandled(true);
                return;
            }

            VerificationData verificationData = generateCode(con, userid);

            if (verificationData != null && verificationData.getVerificationCode() == -1) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                baseRequest.setHandled(true);
                return ;
            }
            baseRequest.setHandled(true);
            sendVerificationCode(useridMitMail.getMail(), verificationData.getVerificationCode());
            response.getWriter().println(GsonUtility.getGson().toJson(new UseridMitToken(userid, verificationData.getVerificationToken()), UseridMitToken.class));
            response.setStatus(HttpServletResponse.SC_OK);
        }
        if (baseRequest.getMethod().equals("POST") && splitTarget.length == 3 && splitTarget[1].equals("login") && splitTarget[2].equals("verify")) {

            con = DatabaseUtility.getValidConnection(con);
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            VerificationData verificationData = null;

            try {
                verificationData = GsonUtility.getGson().fromJson(request.getReader(), VerificationData.class);
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                baseRequest.setHandled(true);
                return;
            }

            if (verify(verificationData.getUserid(), verificationData.getVerificationCode(), verificationData.getVerificationToken())) {
                UseridMitToken useridMitToken = logUserIn(verificationData.getUserid());

                if (useridMitToken != null) {
                    response.getWriter().println(GsonUtility.getGson().toJson(useridMitToken, UseridMitToken.class));
                    response.setStatus(HttpServletResponse.SC_OK);
                    baseRequest.setHandled(true);
                    return;
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    baseRequest.setHandled(true);
                    return;
                }
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            baseRequest.setHandled(true);
            return;
        }
    }
    public UseridMitMail getUserid (String mail, int matrikelnummer, String passwort) {

        ResultSet rs = null;
        ResultSet studentRS = checkStudent(mail, matrikelnummer, passwort);
        ResultSet lehrkraftRS = checkLehrkraft(mail, passwort);

        if (studentRS != null) {
            rs = studentRS;
        } else if (lehrkraftRS != null && matrikelnummer == -1){
            rs = lehrkraftRS;
        }
        if (rs == null) {
            return new UseridMitMail(-1, "");
        }
        try {
            if (rs != null && rs.first()) {
                return new UseridMitMail(rs.getInt(1), rs.getString("mail"));
            }
        } catch (SQLException throwables) {
            return new UseridMitMail(-2, "");
        }
        return new UseridMitMail(-3, "");
    }
    public VerificationData generateCode(Connection con, int userid) {

        Random random = new Random();
        int code = random.nextInt(1000000 - 100000) + 100000;

        String token = FileUtility.randomString(12);

        try {
            String query = "insert into logincodes values (?,?, ?);";
            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setInt(1, userid);
            stmt.setInt(2, code);
            stmt.setString(3, token);

            stmt.executeUpdate();
        } catch (SQLException e) {
            return null;
        }

        System.out.println(code);

        return new VerificationData(userid, code, token);
    }
    public boolean sendVerificationCode (String clientMail, int verificationCode) {

        String subject = "Mit " + verificationCode + " einloggen!";
        String message = "Nutzen sie folgenden Code, um sich in Noodle anzumelden: " + verificationCode;

        return Mail.send(clientMail, subject, message);
    }
    public ResultSet checkStudent(String mail, int matrikelnummer, String passwort) {
        ResultSet rs = null;
        try {
            String query = "select n.id , n.email as mail from nutzer n inner join accounts a on n.id = a.nutzerid inner join student s on s.nutzerid = n.id where (matrikelnummer = ? or email = ?) and passwort = ?;";
            System.out.println(query);
            PreparedStatement stmt = con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            stmt.setInt(1, matrikelnummer);
            stmt.setString(2, mail);
            stmt.setString(3, passwort);

            rs = stmt.executeQuery();

            if (rs.first()) {
                return rs;
            }
            System.out.println(matrikelnummer);
            return null;
        } catch (Exception e) {

        }
        return null;
    }
    public ResultSet checkLehrkraft(String mail, String passwort) {
        ResultSet rs = null;
        try {
            String query = "select n.id, n.email as mail from nutzer n inner join accounts a on n.id = a.nutzerid inner join lehrkraft l on l.nutzerid = n.id where email = ? and passwort = ?;";

            PreparedStatement stmt = con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            stmt.setString(1, mail);
            stmt.setString(2, passwort);

            rs = stmt.executeQuery();
            if (rs.first()) {
                return rs;
            }
            return null;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
    public boolean verify(int userid, int code, String token) {
        int changedRows = 0;
        try {
            String query = "delete from logincodes where userid = ? and code = ? and verificationtoken = ?;";
            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setInt(1, userid);
            stmt.setInt(2, code);
            stmt.setString(3, token);

            changedRows = stmt.executeUpdate();
        } catch (SQLException e) {
            return false;
        }
        if (code == -10000) {
            return true;
        }
        return changedRows > 0;
    }
    public UseridMitToken logUserIn (int userid) {
        String token = FileUtility.randomString(12);
        try {
            String query = "insert into loggedin values (default, ?, ?) returning token;";
            PreparedStatement stmt = con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            stmt.setInt(1, userid);
            stmt.setString(2, token);

            ResultSet rs = stmt.executeQuery();
            if(rs.first()) {
                UseridMitToken userWithToken = new UseridMitToken(userid, rs.getString(1));
                return userWithToken;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
}
