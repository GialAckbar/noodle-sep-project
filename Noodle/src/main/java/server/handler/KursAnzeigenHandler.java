package server.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import server.ServerUtility;
import server.db.DatabaseUtility;
import shared.*;
import shared.accounts.UseridMitToken;
import shared.quiz.Quiz;
import shared.utility.GsonUtility;

import java.io.IOException;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class KursAnzeigenHandler extends AbstractHandler {
    Connection con = null;
    public KursAnzeigenHandler (Connection con) {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        String[] splitTarget = target.split("/");

        if (baseRequest.getMethod().equals("GET") && splitTarget.length >= 2 && splitTarget[1].equals("showCourse")) {
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
            int lvid = -1;

            try {
                lvid = Integer.parseInt(request.getParameter("veranstaltungsid"));
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            Lehrveranstaltung veranstaltung = null;

            veranstaltung = createCompleteLehrveranstaltung(con, lvid, useridMitToken.getUserid());
            if (veranstaltung == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            try {
                response.getWriter().println(GsonUtility.getGson().toJson(veranstaltung, Lehrveranstaltung.class));
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (IOException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

        }
        if (baseRequest.getMethod().equals("GET") && splitTarget.length >= 2 && splitTarget[1].equals("getchatid")) {
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
            int lvid = -1;
            try {
                lvid = Integer.parseInt(request.getParameter("lvid"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                lvid = -1;
            }
            if (lvid > 0) {
                int chatid = getChatIdForCourse(con, lvid);
                if (chatid > 0) {
                    try {
                        response.getWriter().println(GsonUtility.getGson().toJson(chatid, Integer.class));
                        response.setStatus(HttpServletResponse.SC_OK);
                    } catch (IOException e) {
                        e.printStackTrace();
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        return;
                    }

                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return;
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }


        }
    }
    public static Lehrveranstaltung createCompleteLehrveranstaltung(Connection con, int lvid, int userid) {
        Lehrveranstaltung veranstaltung = null;

        try {
            veranstaltung = getLehrveranstaltung(con, lvid);
            checkBelegt(con, veranstaltung, userid);
            addKategorien(con, veranstaltung);
            addTodos(con, lvid, veranstaltung.getKategorien());
            addDateien(con, lvid, veranstaltung.getKategorien());
            addReminder(con, lvid, veranstaltung.getKategorien());
            addQuiz(con, lvid, veranstaltung.getKategorien());
            addLernkarten(con, lvid, veranstaltung.getKategorien());
            addBewertungen(con, lvid, veranstaltung.getKategorien());
            sortKategorien(con, veranstaltung.getKategorien());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return veranstaltung;
    }
    public static Lehrveranstaltung createCompleteLehrveranstaltung(Connection con, int lvid) {
        Lehrveranstaltung veranstaltung = null;

        try {
            veranstaltung = getLehrveranstaltung(con, lvid);
            //checkBelegt(con, veranstaltung);
            addKategorien(con, veranstaltung);
            addDateien(con, lvid, veranstaltung.getKategorien());
            addTodos(con, lvid, veranstaltung.getKategorien());
            addReminder(con, lvid, veranstaltung.getKategorien());
            addQuiz(con, lvid, veranstaltung.getKategorien());
            addLernkarten(con, lvid, veranstaltung.getKategorien());
            addBewertungen(con, lvid, veranstaltung.getKategorien());
            sortKategorien(con, veranstaltung.getKategorien());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return veranstaltung;
    }
    protected static Lehrveranstaltung getLehrveranstaltung(Connection con, int id) throws SQLException {
        String query = "select * from veranstaltung where id = ?";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if(rs.next()) {
            String titel = rs.getString(2);
            String semesterString = rs.getString(5);
            int jahr = rs.getInt(4);
            String artString = rs.getString(3);

            Semester semester;
            Enums.Art art;
            if (semesterString.equals("WS")) {
                semester = new Semester(jahr, Enums.SemesterTyp.WS);
            } else {
                semester = new Semester(jahr, Enums.SemesterTyp.SS);
            }
            if (artString.equals("Vorlesung")) {
                art = Enums.Art.VORLESUNG;
            } else if (artString.equals("Seminar")){
                art = Enums.Art.SEMINAR;
            } else {
                art = Enums.Art.PROJEKTGRUPPE;
            }

            Lehrveranstaltung lv = new Lehrveranstaltung(titel, semester, art, id);
            return lv;
        }
        return null;
    }
    protected static void addKategorien (Connection con, Lehrveranstaltung lv) throws SQLException {
        int id = lv.getVeranstaltungsID();

        String query = "select * from veranstaltungskategorie where veranstaltungsid = ? order by position asc";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        List<LVKategorie> kategorien = lv.getKategorien();
        while (rs.next()) {
            LVKategorie current = new LVKategorie(rs.getInt(1), rs.getString(2));
            current.setPosition(rs.getInt(4));
            kategorien.add(current);
        }
    }
    protected static List<KategorieDatei> addDateien (Connection con, int id, List<LVKategorie> kategorien) throws SQLException {
        List<KategorieDatei> dateien = new ArrayList<KategorieDatei>();

        String query = "select vk.id as kategorieid, k.position, d.id as dateiid, d.anzeigename, k.id as elementid from veranstaltung v " +
                "inner join veranstaltungskategorie vk on v.id = vk.veranstaltungsid inner join kategorieelement k on k.veranstaltungskategorieid = vk.id " +
                "inner join dateizukategorie dk on dk.kategorieelementid = k.id inner join datei d on dk.dateiid = d.id where v.id = ? order by k.position asc;";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            KategorieDatei datei = new KategorieDatei(rs.getInt(3), rs.getString(4), rs.getInt(2));
            datei.setId(rs.getInt(5));
            insertLVKategorieElement(kategorien, datei, rs.getInt(1));
        }
        return dateien;
    }
    protected static void addTodos(Connection con, int lvid, List<LVKategorie> kategorien) throws SQLException {
        String query = "select vk.id as kategorieid, k.position, t.id as todoid, t.aufgabe, k.id as elementid, n.id as userid, t.fertig as finished, n.vorname as vorname, " +
                "n.nachname as nachname, st.matrikelnummer as matrikelnummer " +
                "from veranstaltung v " +
                "inner join veranstaltungskategorie vk on v.id = vk.veranstaltungsid " +
                "inner join kategorieelement k on k.veranstaltungskategorieid = vk.id " +
                "inner join todozukategorie tzk on tzk.kategorieelementid = k.id " +
                "inner join todo t on t.id = tzk.todoid " +
                "left join todoteilnehmer tt on t.id = tt.todoid " +
                "left join nutzer n on n.id = tt.nutzerid " +
                "left join student st on st.nutzerid = n.id " +
                "where v.id = ? order by k.position asc, t.id ;";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, lvid);
        ResultSet rs = stmt.executeQuery();

//        while (rs.next()) {
//            Todo todo = new Todo (rs.getInt(3), rs.getString(4), rs.getInt(2));
//            insertLVKategorieElement(kategorien, todo, rs.getInt(1));
//        }
        Todo currentTodo = null;
        while (rs.next()) {
            if (currentTodo == null) {
                currentTodo = new Todo (rs.getInt(3), rs.getString(4), rs.getInt(2));
                currentTodo.setIsFinished(rs.getBoolean("finished"));
                insertLVKategorieElement(kategorien, currentTodo, rs.getInt(1));
            } else if (currentTodo.getId() != rs.getInt(3)) {
                currentTodo = new Todo (rs.getInt(3), rs.getString(4), rs.getInt(2));
                currentTodo.setIsFinished(rs.getBoolean("finished"));
                insertLVKategorieElement(kategorien, currentTodo, rs.getInt(1));
            }
            if (rs.getInt("userid") != 0) {
                User temp = null;
                if (rs.getInt("matrikelnummer") == 0) {
                    temp = new Lehrende(rs.getInt("userid"));
                    temp.setVorname(rs.getString("vorname"));
                    temp.setNachname(rs.getString("nachname"));
                } else {
                    temp = new Student(rs.getInt("userid"));
                    temp.setVorname(rs.getString("vorname"));
                    temp.setNachname(rs.getString("nachname"));
                    ((Student) temp).setMatrikelnummer(rs.getInt("matrikelnummer"));
                }
                currentTodo.addVerantwortlichen(temp);
            }
        }
    }
    protected static void addReminder (Connection con, int lvid, List<LVKategorie> kategorien) throws SQLException {
        String query = "select vk.id as kategorieid, k.position, t.id as todoid, t.anzeigename, k.id as elementid, " +
                            "t.frist as frist, e.erinnernab as erinnernab, e.zustellung as zustellung " +
                "from veranstaltung v " +
                "inner join veranstaltungskategorie vk on v.id = vk.veranstaltungsid " +
                "inner join kategorieelement k on k.veranstaltungskategorieid = vk.id " +
                "inner join terminzukategorie tzk on tzk.kategorieelementid = k.id " +
                "inner join termin t on t.id = tzk.terminid " +
                "left join erinnerung e on t.id = e.terminid " +
                "where v.id = ? order by k.position asc;";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, lvid);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Reminder reminder = new Reminder(rs.getInt(3), rs.getString(4), rs.getInt(2));


            reminder.setEventDate(ZonedDateTime.ofInstant(rs.getTimestamp("frist").toInstant(), ZoneId.of("UTC")));

            Timestamp erinnernab = rs.getTimestamp("erinnernab");
            if (erinnernab != null) {
                reminder.setShouldRemind(true);
                reminder.setRemindDate(ZonedDateTime.ofInstant(erinnernab.toInstant(), ZoneId.of("UTC")));


                Enums.ReminderType reminderType = null;
                String reminderTypeString = rs.getString("zustellung");
                if (reminderTypeString.equals("Popup")) {
                    reminderType = Enums.ReminderType.POPUP;
                } else {
                    reminderType = Enums.ReminderType.MAIL;
                }
                reminder.setMessageType(reminderType);
            }

            insertLVKategorieElement(kategorien, reminder, rs.getInt(1));
        }
    }
    protected static void addQuiz (Connection con, int lvid, List<LVKategorie> kategorien) throws SQLException {
        String query = "select vk.id as kategorieid, k.position, q.id as quizid, q.name, k.id as elementid " +
                "from veranstaltung v " +
                "inner join veranstaltungskategorie vk on v.id = vk.veranstaltungsid " +
                "inner join kategorieelement k on k.veranstaltungskategorieid = vk.id " +
                "inner join quizzukategorie qzk on qzk.kategorieelementid = k.id " +
                "inner join quiz q on q.id = qzk.quizid " +
                "where v.id = ? order by k.position asc;";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, lvid);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Quiz quiz = new Quiz(rs.getInt(3), rs.getString(4), rs.getInt(2));
            insertLVKategorieElement(kategorien, quiz, rs.getInt(1));
        }
    }
    protected static void addLernkarten (Connection con, int lvid, List<LVKategorie> kategorien) throws SQLException {
        String query = "select vk.id as kategorieid, k.position, l.id as lernkarteid, l.text, k.id as elementid, l.antwort as antwort " +
                "from veranstaltung v " +
                "inner join veranstaltungskategorie vk on v.id = vk.veranstaltungsid " +
                "inner join kategorieelement k on k.veranstaltungskategorieid = vk.id " +
                "inner join lernkartezukategorie lzk on lzk.kategorieelementid = k.id " +
                "inner join lernkarte l on l.id = lzk.lernkarteid " +
                "where v.id = ? order by k.position asc;";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, lvid);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Lernkarte lernkarte = new Lernkarte(rs.getInt(3), rs.getString(4), rs.getInt(2));
            lernkarte.setAntwort(rs.getString("antwort"));
            insertLVKategorieElement(kategorien, lernkarte, rs.getInt(1));
        }
    }
    protected static void addBewertungen (Connection con, int lvid, List<LVKategorie> kategorien) throws SQLException {
        String query = "select vk.id as kategorieid, k.position, f.id as feedbackid, f.name, k.id as elementid " +
                "from veranstaltung v " +
                "inner join veranstaltungskategorie vk on v.id = vk.veranstaltungsid " +
                "inner join kategorieelement k on k.veranstaltungskategorieid = vk.id " +
                "inner join feedbackzukategorie fzk on fzk.kategorieelementid = k.id " +
                "inner join feedback f on f.id = fzk.feedbackid " +
                "where v.id = ? order by k.position asc;";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, lvid);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            shared.Bewertung bewertung = new shared.Bewertung(rs.getInt(3), rs.getString(4), rs.getInt(2));
            insertLVKategorieElement(kategorien, bewertung, rs.getInt(1));
        }
    }
    protected static void insertLVKategorieElement(List<LVKategorie> kategorien, LVKategorieElement element, int kategorieid) {
        for(int i = 0; i < kategorien.size(); i++) {
            if(kategorien.get(i).getID() == kategorieid) {
                kategorien.get(i).getKategorieElemente().add(element);
                return;
            }
        }
    }
    protected static void checkBelegt(Connection con, Lehrveranstaltung veranstaltung, int userid) throws SQLException, NullPointerException {
        veranstaltung.setBelegung(false);

        String query = "select * from veranstaltung v inner join veranstaltungsteilnehmer vt on vt.veranstaltungsid = v.id " +
                "inner join nutzer n on n.id = vt.nutzerid where v.id = ? and n.id = ?;";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, veranstaltung.getVeranstaltungsID());
        stmt.setInt(2, userid);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            veranstaltung.setBelegung(true);
        }
    }
    protected static void sortKategorien(Connection con, List<LVKategorie> kategorien) {
        for (int i = 0; i < kategorien.size(); i++) {
            LVKategorie kategorie = kategorien.get(i);

            sortKategorie(kategorie);
        }
    }
    protected static void sortKategorie(LVKategorie kategorie) {
        List<LVKategorieElement> elemente = kategorie.getKategorieElemente();

        for (int i = 0; i < elemente.size(); i++) {
            LVKategorieElement outer = elemente.get(i);
            int lowest = i;
            for (int j = i + 1; j < elemente.size(); j++) {
                LVKategorieElement inner = elemente.get(j);
                if(inner.getPosition() < outer.getPosition()) {
                    lowest = j;
                }
            }
            if (lowest != i) {
                LVKategorieElement temp = outer;
                elemente.set(i, elemente.get(lowest));
                elemente.set(lowest, temp);
            }
        }
    }
    public static int getChatIdForCourse(Connection con, int lvid) {
        try {
            String query = "select c.id from chats c inner join veranstaltung v on v.id = c.projektgruppeid where v.id = ?;";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, lvid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return -1;
    }
}
