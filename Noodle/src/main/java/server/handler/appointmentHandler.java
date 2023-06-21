package server.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import shared.*;
import shared.utility.GsonUtility;

import java.io.IOException;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


public class appointmentHandler extends AbstractHandler {
    Connection con;

    public appointmentHandler(Connection con)
    {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] splitTarget = target.split("/");

        if(baseRequest.getMethod().equals("GET") && splitTarget.length >=3 && splitTarget[1].equals("terminliste")) {
            System.out.println("TerminListHandler: Starting...");

            if (splitTarget[2].equals("OWN")) {
                int userid;

                try {
                    userid = Integer.parseInt(request.getParameter("userid"));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    baseRequest.setHandled(true);
                    return;
                }

                System.out.println("TerminListHandler: Retrieved userID successfully.");

                ResultSet termRS = getTerm(userid);
                ResultSet frist = getFristen(userid);

                if (termRS == null || frist == null) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    baseRequest.setHandled(true);
                    System.out.println("TerminListHandler: Fetching appointsments or deadline failed.");
                    return;
                }
                System.out.println("TerminListHandler: Fetched appointments and deadlines.");

                System.out.println("TerminListHandler: Extracting appointments...");
                List<Termin> term = extractTermine(termRS);
                System.out.println("TerminListHandler: Extraction complete.");

                System.out.println("TerminListHandler: Extracting deadlines...");
                List<ZonedDateTime> deadl = extractFrist(frist);
                System.out.println("TerminListHandler: Extraction complete.");

                System.out.println("TerminListHandler: Sorting by timestamp...");

                List<String> DateList = new ArrayList<>();

                for(ZonedDateTime x : deadl) {
                    x.withZoneSameInstant(ZoneId.systemDefault());

                    String day = (x.getDayOfMonth() < 10) ? "0" + x.getDayOfMonth() : String.valueOf(x.getDayOfMonth());
                    String month = (x.getMonthValue() < 10) ? "0" + x.getMonthValue() : String.valueOf(x.getMonthValue());
                    String date = day+"."+month+"."+x.getYear();

                    if(!DateList.contains(date)) {
                        DateList.add(date);
                    }
                }

                TerminListe[] list = new TerminListe[term.size()];
                int i = 0;
                for (String x : DateList) {
                    TerminListe temList = new TerminListe();
                    for (Termin y : term) {
                        String date = y.getDay()+"."+y.getMonth()+"."+y.getYear();
                        if(date.equals(x)) {
                            temList.add(y);
                        }
                    }
                    if (temList.getList().size() != 0) {
                        temList.getList().sort((o1, o2) -> {
                            String a1 = o1.getTimeInMinutes();
                            String b1 = o2.getTimeInMinutes();

                            int a = Integer.parseInt(a1);
                            int b = Integer.parseInt(b1);

                            return Integer.compare(a, b);
                        });
                        list[i] = temList;
                    }
                    if(list.length != 1) {
                        i++;
                    }
                }
                System.out.println("TerminListHandler: Done.");
                System.out.println("TerminListHandler: To client...");
                response.getWriter().println(GsonUtility.getGson().toJson(list, TerminListe[].class));
                response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);
                System.out.println("TerminListHandler: Done.");

            }
        }
    }

    private ResultSet getFristen(int uid) {
        ResultSet rs;
        try {
            String query = "SELECT distinct frist FROM termin t inner join terminzukategorie kt on t.id = kt.terminid " +
                    "inner join kategorieelement kte on kt.kategorieelementid = kte.id inner join veranstaltungskategorie vkt" +
                    " on kte.veranstaltungskategorieid = vkt.id inner join veranstaltung v on vkt.veranstaltungsid = v.id inner join " +
                    "veranstaltungsteilnehmer vt on vt.veranstaltungsid = v.id where nutzerid = ?;";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, uid);

            rs = stmt.executeQuery();
            return rs;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<ZonedDateTime> extractFrist(ResultSet rs) {
        List<ZonedDateTime> fristen = new ArrayList<>();

        try {
            while(rs.next()) {
                ZonedDateTime s = readFrist(rs);
                if (s != null) {
                    fristen.add(s);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return fristen;
    }

    private ZonedDateTime readFrist(ResultSet resSet) {
        try {
            return ZonedDateTime.ofInstant(resSet.getTimestamp(1).toInstant(), ZoneId.of("UTC"));

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    private ResultSet getTerm(int uid) {
        ResultSet rs;
        try {
            String query = "SELECT distinct t.id, frist, anzeigename, v.id, v.name, v.semester, v.typ, v.jahr FROM termin t" +
                    " inner join terminzukategorie kt on t.id = kt.terminid inner join" +
                    " kategorieelement kte on kt.kategorieelementid = kte.id inner join veranstaltungskategorie vkt" +
                    " on kte.veranstaltungskategorieid = vkt.id inner join veranstaltung v" +
                    " on vkt.veranstaltungsid = v.id inner join veranstaltungsteilnehmer vt on vt.veranstaltungsid = v.id where nutzerid = ?;";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, uid);

            rs = stmt.executeQuery();
            return rs;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Termin> extractTermine(ResultSet rs) {
        List<Termin> term = new ArrayList<>();

        try {
            while(rs.next()) {
                Termin s = readTermine(rs);
                if (s != null) {
                    term.add(s);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return term;
    }

    private Termin readTermine(ResultSet resSet) {
        try {
            int terminID = resSet.getInt(1);
            ZonedDateTime frist = ZonedDateTime.ofInstant(resSet.getTimestamp(2).toInstant(), ZoneId.of("UTC"));
            String terminTitel = resSet.getString(3);
            int t_vID = resSet.getInt(4);
            String vName = resSet.getString(5);
            String sem = resSet.getString(6);
            String art = resSet.getString(7);
            int jahr = resSet.getInt(8);

            Enums.SemesterTyp semesterTyp = null;
            Enums.Art type = null;

            if (sem.equals("SS")) {
                semesterTyp = Enums.SemesterTyp.SS;
            } else if (sem.equals("WS")) {
                semesterTyp = Enums.SemesterTyp.WS;
            }

            if (art.equals("Vorlesung")) {
                type = Enums.Art.VORLESUNG;
            } else if (art.equals("Seminar")) {
                type = Enums.Art.SEMINAR;
            } else if (art.equals("Projektgruppe")) {
                type = Enums.Art.PROJEKTGRUPPE;
            }

            Lehrveranstaltung lv = new Lehrveranstaltung(vName, new Semester(jahr, semesterTyp), type);
            return new Termin(lv, frist, terminTitel, t_vID);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
}