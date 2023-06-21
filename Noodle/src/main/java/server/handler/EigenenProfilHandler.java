package server.handler;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import shared.Adresse;
import shared.Student;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EigenenProfilHandler extends AbstractHandler {

    Connection con = null;

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");

        if (baseRequest.getMethod().equals("GET") && splitTarget.length == 2 && splitTarget[1].equals("eigenesProfil")) {
            int userid = -1;
            String token = null;

            try {
                token = request.getParameter("token");
                userid = Integer.parseInt(request.getParameter("userid"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                baseRequest.setHandled(true);
                return;
            }
            System.out.println("ProfilDatenHandler: Retrieved userID and token successfully.");
            ResultSet profildaten = getProfilDaten(userid); // Profil Daten

            if (profildaten == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                baseRequest.setHandled(true);
                System.out.println("ProfilDatenHandler: Fetching profil data failed.");
                return;
            }

            System.out.println("ProfilDaten: Fetched profil data.");
            System.out.println("ProfilDaten: Extracting profil data...");

            Student stud = readDaten(profildaten);
            System.out.println("ProfilDatenHandler: Extraction complete.");

            System.out.println("ProfilDatenHandler: ToJson...");
            response.getWriter().println(new Gson().toJson(stud, Student.class));
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
            System.out.println("CourseListHandler: Done.");


        }
    }
        private ResultSet getProfilDaten (int userid) {
            ResultSet rs;
            try {
                String query = "select distinct vorname,nachname,email,studienfach,matrikelnummer,adresse from nutzer n inner join student s on(n.id = s.nutzerid) inner join adresse a on  n.adressid=a.id where n.id = ?";
                PreparedStatement stmt = con.prepareStatement(query);

                stmt.setInt(1, userid);

                rs = stmt.executeQuery();

                return rs;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

    private Student readDaten(ResultSet resSet ) {
        try {
            String vorname = resSet.getString(1);
            String nachname = resSet.getString(2);
            String email = resSet.getString(3);
            String studienfach = resSet.getString(4);
            Integer matrikelnummer = resSet.getInt(5);
            Adresse adresse = resSet.getObject(6,Adresse.class);


            Student stud = new Student(vorname, nachname, email,adresse,matrikelnummer,studienfach);
            return stud;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }
    }
