package server.handler;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import server.ServerUtility;
import server.db.DatabaseManager;
import server.db.DatabaseUtility;
import shared.*;
import shared.accounts.UseridMitToken;
import shared.navigation.NavigationInformation;
import shared.navigation.SemesterMitVeranstaltungen;
import shared.utility.GsonUtility;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class NavigationHandler extends AbstractHandler {

    Connection con = null;

    public NavigationHandler (Connection con) {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        String[] splitTarget = target.split("/");
        response.setCharacterEncoding("UTF-8");
        request.setCharacterEncoding("UTF-8");
        if (baseRequest.getMethod().equals("GET") && splitTarget.length==2 && splitTarget[1].equals("navigation")) {

            con = DatabaseUtility.getValidConnection(con);
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            int userid = -1;
            String token = null;

            UseridMitToken useridMitToken = ServerUtility.extractUseridAndToken(request);
            if(!DatabaseUtility.isLoggedIn(con, useridMitToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            userid = useridMitToken.getUserid();

            ResultSet rs = getLehrveranstaltungen(userid);

            if (rs == null) {
                response.setStatus((HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
                baseRequest.setHandled(true);
                return;
            }
            NavigationInformation navigationInformation = initInformation(userid);
            if (navigationInformation == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                baseRequest.setHandled(true);
                return;
            }
            List<Lehrveranstaltung> lehrveranstaltungen = extractVeranstaltungen(rs);
            List<SemesterMitVeranstaltungen> semesterMitVeranstaltungen = groupVeranstaltungen(lehrveranstaltungen);
            navigationInformation.setSemesterMitLehrveranstaltungen(semesterMitVeranstaltungen);

            try {
                List<Reminder> reminder = getReminder(userid);
                navigationInformation.setReminder(reminder);
            } catch (SQLException e) {
                e.printStackTrace();
                baseRequest.setHandled(true);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            try {
                response.getWriter().println(GsonUtility.getGson().toJson(navigationInformation, NavigationInformation.class));
            } catch (IOException e) {
                e.printStackTrace();
            }

            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
        }
        if (baseRequest.getMethod().equals("POST") && splitTarget.length==3 && splitTarget[1].equals("seenReminder") && splitTarget[2].equals("add")) {
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

            int terminid = -1;

            try {
                terminid = Integer.parseInt(request.getParameter("terminid"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            try {
                String query = "insert into geseheneErinnerung values (default, ?, ?)";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setInt(1, useridMitToken.getUserid());
                stmt.setInt(2, terminid);
                stmt.executeUpdate();

                response.setStatus(HttpServletResponse.SC_OK);
                return;
            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
        }

    }

    protected NavigationInformation initInformation (int userid) {
        User user = DatabaseUtility.getUserSubclass(con, userid);
        if (user != null) {
            NavigationInformation navigationInformation = new NavigationInformation(user.getVorname(), user.getNachname(), user.getImageID());
            navigationInformation.setUser(user);
            return navigationInformation;
        }

        return null;
    }
    protected ResultSet getLehrveranstaltungen (int userid) {
        ResultSet rs = null;

        try {
            String query = "select v.id, jahr, semester, name, typ from veranstaltung v inner join veranstaltungsteilnehmer vt on v.id = vt.veranstaltungsid " +
                    "inner join nutzer n on n.id = vt.nutzerid where n.id = ? and typ !=  'Projektgruppe' order by jahr desc, semester desc;";
            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setInt(1, userid);

            rs = stmt.executeQuery();

            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    protected Lehrveranstaltung readVeranstaltung(ResultSet rs) {
        try {
            int id = rs.getInt(1);
            int jahr = rs.getInt(2);
            String semesterTypString = rs.getString(3);
            String name = rs.getString(4);
            String typString = rs.getString(5);

            Enums.SemesterTyp semesterTyp = null;
            Enums.Art art = null;



            if (semesterTypString.equals("SS")) {
                semesterTyp = Enums.SemesterTyp.SS;
            } else if (semesterTypString.equals("WS")) {
                semesterTyp = Enums.SemesterTyp.WS;
            }

            if (typString.equals("Vorlesung")) {
                art = Enums.Art.VORLESUNG;
            } else if (typString.equals("Seminar")) {
                art = Enums.Art.SEMINAR;
            } else if (typString.equals("Projektgruppe")) {
                art = Enums.Art.PROJEKTGRUPPE;
            }

            Lehrveranstaltung veranstaltung = new Lehrveranstaltung(name, new Semester(jahr, semesterTyp), art, id);
            return veranstaltung;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


        return null;
    }
    protected List<Lehrveranstaltung> extractVeranstaltungen (ResultSet rs) {
        List<Lehrveranstaltung> lehrveranstaltungen = new ArrayList<Lehrveranstaltung>();

        try {
            while(rs.next()) {
                Lehrveranstaltung ret = readVeranstaltung(rs);
                if (ret != null) {
                    lehrveranstaltungen.add(ret);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return lehrveranstaltungen;
    }
    protected List<SemesterMitVeranstaltungen> groupVeranstaltungen(List<Lehrveranstaltung> lehrveranstaltungen) {
        List<SemesterMitVeranstaltungen> semesterMitVeranstaltungen = new ArrayList<SemesterMitVeranstaltungen>();

        Semester currentSemester = null;
        SemesterMitVeranstaltungen currentSemesterMitVeranstaltungen = null;

        for (int i = 0; i < lehrveranstaltungen.size(); i++) {
            if (currentSemester == null || !currentSemester.equals(lehrveranstaltungen.get(i).getSemester())) {
                currentSemester = lehrveranstaltungen.get(i).getSemester();

                if(currentSemesterMitVeranstaltungen != null) {
                    semesterMitVeranstaltungen.add(currentSemesterMitVeranstaltungen);
                }
                currentSemesterMitVeranstaltungen = new SemesterMitVeranstaltungen(currentSemester);
            }

            currentSemesterMitVeranstaltungen.addLehrveranstaltung(lehrveranstaltungen.get(i));
        }
        if (lehrveranstaltungen.size() > 0) {
            semesterMitVeranstaltungen.add(currentSemesterMitVeranstaltungen);
        }


        return semesterMitVeranstaltungen;
    }
    protected List<Reminder> getReminder (int userid) throws SQLException {
        String query = "select t.id as terminid, frist, anzeigename, erinnernab, zustellung from termin t inner join erinnerung e on t.id = e.terminid " +
                "inner join terminzukategorie tzk on tzk.terminid = t.id " +
                "inner join kategorieelement ke on tzk.kategorieelementid = ke.id " +
                "inner join veranstaltungskategorie k on k.id = ke.veranstaltungskategorieid " +
                "inner join veranstaltung v on k.veranstaltungsid = v.id " +
                "inner join veranstaltungsteilnehmer vt on vt.veranstaltungsid = v.id " +
                "inner join nutzer n on n.id = vt.nutzerid " +
                "where n.id = ? and e.zustellung = 'Popup' and t.id not in (select terminid from geseheneErinnerung where nutzerid = ?) order by erinnernab asc;";
        PreparedStatement stmt = con.prepareStatement(query);

        String addSeenQuery = "insert into geseheneErinnerung values (default, ?, ?);";
        PreparedStatement addSeenStatement = con.prepareStatement(addSeenQuery);

        stmt.setInt(1, userid);
        stmt.setInt(2, userid);
        ResultSet rs = stmt.executeQuery();
        List<Reminder> reminder = new ArrayList<Reminder>();

        while(rs.next()) {
            Reminder current = new Reminder(rs.getInt(1), rs.getString(3), -1);
            current.setEventDate(ZonedDateTime.ofInstant(rs.getTimestamp(2).toInstant(), ZoneId.of("UTC")));
            current.setRemindDate(ZonedDateTime.ofInstant(rs.getTimestamp(4).toInstant(), ZoneId.of("UTC")));
            current.setShouldRemind(true);

            String messageTypeString = rs.getString("zustellung");
            Enums.ReminderType reminderType = null;
            if (messageTypeString.equals(Enums.ReminderType.getString(Enums.ReminderType.POPUP))) {
                reminderType = Enums.ReminderType.POPUP;
            } else {
                reminderType = Enums.ReminderType.MAIL;
            }
            current.setMessageType(reminderType);

            reminder.add(current);
        }

        return reminder;
    }
}
