package server.handler;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import server.db.DatabaseManager;
import shared.Adresse;
import shared.Lehrende;
import shared.Student;
import shared.User;
import shared.accounts.LehrkraftmitPasswort;
import shared.accounts.StudentMitPasswort;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterHandler extends AbstractHandler {

    Connection con;

    public RegisterHandler(Connection con) {
        this.con = con;
    }
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");

        if (baseRequest.getMethod().equals("POST") && splitTarget.length >= 3 && splitTarget[1].equals("register")) {

            boolean responseSet = false;

            if (splitTarget[2].equals("student")) {

                StudentMitPasswort studentMitPasswort = null;
                Student student = null;

                try {

                    studentMitPasswort = new Gson().fromJson(request.getReader(), StudentMitPasswort.class);
                    student = studentMitPasswort.getStudent();
                } catch (Exception e) {

                    System.out.println("Gson unsuccessful");

                }

                if (student != null) {

                    int userid = createUserAndAddress(student);

                    if (userid >= 0) {

                        try {

                            int matrikelnummer = -1;

                            String query = "insert into student values (default, ?, default, ?) returning matrikelnummer;";
                            PreparedStatement stmt = con.prepareStatement(query);

                            stmt.setInt(1, userid);
                            stmt.setString(2, student.getStudienfach());

                            ResultSet rs = stmt.executeQuery();
                            rs.next();
                            matrikelnummer = rs.getInt(1);

                            if (createAccount(userid, studentMitPasswort.getPasswort())) {
                                response.getWriter().println(new Gson().toJson(matrikelnummer,Integer.class));
                                responseSet = true;
                            }

                        } catch (SQLException e) {

                            e.printStackTrace();

                        }
                    }
                }
            } else if (splitTarget[2].equals("lehrkraft")) {

                LehrkraftmitPasswort lehrkraftmitPasswort = null;
                Lehrende lehrkraft = null;

                try {
                    lehrkraftmitPasswort = new Gson().fromJson(request.getReader(), LehrkraftmitPasswort.class);
                    lehrkraft = lehrkraftmitPasswort.getLehrkraft();
                } catch (Exception e) {
                    System.out.println("Gson unsuccessful");
                }
                if (lehrkraft != null) {
                    int userid = createUserAndAddress(lehrkraft);
                    if (userid >= 0) {
                        try {
                            int lehrkraftid = -1;

                            String query = "insert into lehrkraft values (default, ?, ?, ?) returning id;";
                            PreparedStatement stmt = con.prepareStatement(query);

                            stmt.setInt(1, userid);
                            stmt.setString(2, lehrkraft.getLehrstuhl());
                            stmt.setString(3, lehrkraft.getForschungsgebiet());

                            ResultSet rs = stmt.executeQuery();
                            rs.next();
                            lehrkraftid = rs.getInt(1);

                            if (createAccount(userid, lehrkraftmitPasswort.getPasswort())) {
                                response.getWriter().println(new Gson().toJson(true,Boolean.class));
                                responseSet = true;
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
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
    private int createAddress(Adresse adresse) {
        ResultSet rs = null;

        int id = -1;

        String query = "insert into adressen values(default,?,?,?,?) returning id;";

        try {
            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setString(1, adresse.getStrasse());
            stmt.setString(2, adresse.getHausnummer());
            stmt.setInt(3, adresse.getPlz());
            stmt.setString(4, adresse.getOrt());

            rs = stmt.executeQuery();
            rs.next();
            id = rs.getInt(1);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return id;
    }
    private int createUser(User user, int adressID) {
        ResultSet rs = null;
        int id = -1;
        String query = "insert into nutzer values(default,?,?,?,?) returning id;";

        try {
            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setString(1, user.getVorname());
            stmt.setString(2, user.getNachname());
            stmt.setString(3, user.getEmail());
            stmt.setInt(4, adressID);
            rs = stmt.executeQuery();
            rs.next();
            id = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }
    private int createUserAndAddress(User user) {
        int adressid = createAddress(user.getAdresse());
        int userid = -1;
        if (adressid >= 0) {
            userid = createUser(user, adressid);
        }
        return userid;
    }
    private boolean createAccount(int userid, String passwort) {
        String query = "insert into accounts values(default,?,?) returning id;";

        try {
            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setInt(1, userid);
            stmt.setString(2, passwort);

            stmt.executeQuery();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
