package server.handler;

import com.google.gson.reflect.TypeToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import server.ServerUtility;
import server.db.DatabaseUtility;
import shared.Enums;
import shared.Lehrveranstaltung;
import shared.LehrveranstaltungsListe;
import shared.Semester;
import shared.accounts.UseridMitToken;
import shared.utility.GsonUtility;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProjektgruppensucheHandler extends AbstractHandler {

    Connection con = null;

    public ProjektgruppensucheHandler (Connection con) {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");
        if (baseRequest.getMethod().equals("GET") && splitTarget.length == 3 && splitTarget[1].equals("projektgruppen") && splitTarget[2].equals("get")) {
            baseRequest.setHandled(true);

            con = DatabaseUtility.getValidConnection(con);
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            UseridMitToken useridMitToken = ServerUtility.extractUseridAndToken(request);
            if(!DatabaseUtility.isLoggedIn(con, useridMitToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            boolean onlyOwn = false;
            if (request.getParameterMap().containsKey("onlyOwn")) {
                onlyOwn = true;
            }
            try {
                List<Lehrveranstaltung> veranstaltungen = getProjektgruppen(useridMitToken.getUserid(), onlyOwn);
                response.getWriter().println(GsonUtility.getGson().toJson(new LehrveranstaltungsListe(veranstaltungen), LehrveranstaltungsListe.class));
                response.setStatus(HttpServletResponse.SC_OK);
                return;
            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
        }
    }
    protected List<Lehrveranstaltung> getProjektgruppen (int userid, boolean onlyOwn) throws SQLException {
        String query = "";
        if (onlyOwn) {
            query = "select v.* from veranstaltung v inner join veranstaltungsteilnehmer vt on vt.veranstaltungsid = v.id inner join nutzer n on n.id = vt.nutzerid " +
                    "where typ = 'Projektgruppe' and nutzerid = ?;";
        } else {
            query = "select *, v.id in " +
                    "(select v.id from veranstaltung v inner join veranstaltungsteilnehmer vt on vt.veranstaltungsid = v.id " +
                    "inner join nutzer n on vt.nutzerid = n.id where n.id = ?) as belegt " +
                    "from veranstaltung v where typ = 'Projektgruppe';";
        }
        PreparedStatement stmt = con.prepareStatement(query);

        if (onlyOwn) {
            stmt.setInt(1, userid);
        } else {
            stmt.setInt(1, userid);
        }
        ResultSet rs = stmt.executeQuery();

        List<Lehrveranstaltung> veranstaltungen = new ArrayList<Lehrveranstaltung>();

        while (rs.next()) {
            Semester semester = null;
            if (rs.getString(5).equals("SS")) {
                semester = new Semester(rs.getInt(4), Enums.SemesterTyp.SS);
            } else {
                semester = new Semester(rs.getInt(4), Enums.SemesterTyp.WS);
            }
            Lehrveranstaltung temp = new Lehrveranstaltung(rs.getString(2), semester, Enums.Art.PROJEKTGRUPPE, rs.getInt(1));
            if (onlyOwn) {
                temp.setBelegung(true);
            } else {
                temp.setBelegung(rs.getBoolean("belegt"));
            }

            veranstaltungen.add(temp);
        }

        return veranstaltungen;
    }
}
