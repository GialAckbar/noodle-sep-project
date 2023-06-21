package server.handler;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import shared.ProfilDaten;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfilBearbeitenHandler extends AbstractHandler {

    Connection con = null;

    public ProfilBearbeitenHandler(Connection con) {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");

        if (baseRequest.getMethod().equals("GET") && splitTarget.length == 2 && splitTarget[1].equals("profilbearbeiten")) {
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

            boolean isTeacher = isTeacher(userid);

            System.out.println("ProfilBearbeitenHandler: Retrieved userID and token successfully.");
            ResultSet profildaten = getProfilDaten(userid, isTeacher); // Profil Daten

            if (profildaten == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                baseRequest.setHandled(true);
                System.out.println("ProfilBearbeitenHandler: Fetching profil data failed.");
                return;
            }

            System.out.println("ProfilBearbeiten: Fetched profil data.");
            System.out.println("ProfilBearbeiten: Extracting profil data...");

            ProfilDaten stud = readDaten(profildaten, isTeacher);
            System.out.println("ProfilBearbeitenHandler: Extraction complete.");

            System.out.println("ProfilBearbeitenHandler: ToJson...");
            response.getWriter().println(new Gson().toJson(stud, ProfilDaten.class));
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
            System.out.println("ProfilBearbeitenHandler: Done.");

        }
    }

    private ResultSet getProfilDaten(int userid,boolean isTeacher) {
        ResultSet rs;
        try {
            String query = "";
            if(isTeacher){
                 query = "Select distinct vorname, nachname, email, straße, nummer, plz, ort, lehrstuhl, forschungsgebiet from nutzer n inner join lehrkraft t on(n.id = t.nutzerid) inner join adressen d on (n.adressid = d.id) Where n.id = ?";

            }
            else {
                 query = "Select distinct vorname, nachname, email, straße, nummer, plz, ort, matrikelnummer, studienfach from nutzer n inner join student s on(n.id = s.nutzerid) inner join adressen d on (n.adressid = d.id) Where n.id = ?";
            }
            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setInt(1, userid);

            rs = stmt.executeQuery();
            rs.next();

            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ProfilDaten readDaten(ResultSet resSet, boolean isTeacher) {
        try {
            String vorname = resSet.getString(1);
            String nachname = resSet.getString(2);
            String email = resSet.getString(3);
            String straße = resSet.getString(4);
            String nummer = resSet.getString(5);
            int plz = resSet.getInt(6);
            String ort = resSet.getString(7);
            if(isTeacher) {
                String lehrstuhl = resSet.getString(8);
                String forschungsgebiet = resSet.getString(9);
                return new ProfilDaten(vorname, nachname, email, straße, nummer, plz, ort, lehrstuhl, forschungsgebiet);
            }
            else {
                int matrikelnummer = resSet.getInt(8);
                String studienfach = resSet.getString(9);
                return new ProfilDaten(vorname, nachname, email, straße, nummer, plz, ort, matrikelnummer, studienfach);
            }
        } catch (SQLException | NullPointerException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    private boolean isTeacher(int userid) {
        try {
            String query = "SELECT nutzerid FROM lehrkraft WHERE nutzerid = ?;";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, userid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                System.out.println("Client ist Lehrender");
                return true;
            }
            System.out.println("Client ist Student");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}