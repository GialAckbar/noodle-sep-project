package server.handler;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import shared.*;
import shared.navigation.NavigationInformation;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class courseListHandler extends AbstractHandler {
    Connection con;

    public courseListHandler(Connection con)
    {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] splitTarget = target.split("/");

        if(baseRequest.getMethod().equals("GET") && splitTarget.length >=3 && splitTarget[1].equals("kursliste")) {
            System.out.println("CourseListHandler: Starting...");

            if (splitTarget[2].equals("NAONLY")) {
                int userid;

                try {
                    userid = Integer.parseInt(request.getParameter("userid"));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    baseRequest.setHandled(true);
                    return;
                }

                System.out.println("CourseListHandler: Retrieved userID and token successfully.");
                ResultSet semRS = getSem();
                System.out.println("CourseListHandler: Extracting semesters...");
                List<Semester> sem = extractSemester(semRS);
                System.out.println("CourseListHandler: Extraction complete.");

                ResultSet unaCourses = getLehrveranstaltungen(userid); // Nicht belegte Kurse
                ResultSet engagedCourses = getBelegteLehrveranstaltungen(userid); // Belegte Kurse

                if (unaCourses == null || engagedCourses == null) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    baseRequest.setHandled(true);
                    System.out.println("CourseListHandler: Fetching unattended courses failed.");
                    return;
                }
                System.out.println("CourseListHandler: Fetched unattended courses.");

                System.out.println("CourseListHandler: Extracting unattended courses...");
                List<Lehrveranstaltung> lv = extractVeranstaltungen(unaCourses, false);

                System.out.println("CourseListHandler: Extracting attended courses...");
                List<Lehrveranstaltung> lvB = extractVeranstaltungen(engagedCourses, true);

                System.out.println("CourseListHandler: Extraction complete.");
                System.out.println("CourseListHandler: Combining lists...");
                for(Lehrveranstaltung x : lv) {
                    if(!lvB.contains(x))
                        lvB.add(x);
                }
                System.out.println("CourseListHandler: Done.");
                System.out.println("CourseListHandler: Sorting by semester...");

//                List<LehrveranstaltungsListe> list = new ArrayList<>();
                LehrveranstaltungsListe[] list = new LehrveranstaltungsListe[sem.size()];
                int i = 0;
                for (Semester x : sem) {
                    LehrveranstaltungsListe semList = new LehrveranstaltungsListe();
                    for (Lehrveranstaltung y : lvB) {
                        if(y.getSemester().getJahr() == x.getJahr() && y.getSemester().getSemesterTyp() == x.getSemesterTyp()) {
                            semList.add(y);
                        }
                    }
                    if(semList.getList().size() != 0) {
                        list[i] = semList;
                    }
                    i++;
                }
//                System.out.println("CourseListHandler: Sorting combined list...");
//                lvB.sort((o1, o2) -> {
//                    String a = o1.getSemester().getJahr() + o1.getSemester().getSemesterTyp().name();
//                    String b = o2.getSemester().getJahr() + o2.getSemester().getSemesterTyp().name();
//                    return b.compareTo(a);
//                });
//                Comparator<Lehrveranstaltung> reverse = new Comparator<Lehrveranstaltung>() {
//                    @Override
//                    public int compare(Lehrveranstaltung o1, Lehrveranstaltung o2) {
//                        String a = o1.getSemester().getJahr() + o1.getSemester().getSemesterTyp().name();
//                        String b = o2.getSemester().getJahr() + o2.getSemester().getSemesterTyp().name();
//                        return a.compareTo(b);
//                    }
//                };
//                lvB.sort(reverse.reversed());
                System.out.println("CourseListHandler: Done.");
                System.out.println("CourseListHandler: To client...");
              //  LehrveranstaltungsListe coursesLV = new LehrveranstaltungsListe(lvB);
                response.getWriter().println(new Gson().toJson(list, LehrveranstaltungsListe[].class));
                response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);
                System.out.println("CourseListHandler: Done.");

            }
            else if(splitTarget[2].equals("ATONLY")) {
                int userid;

                try {
                    userid = Integer.parseInt(request.getParameter("uid"));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    baseRequest.setHandled(true);
                    return;
                }
                System.out.println("CourseListHandler: Retrieved userID and token successfully.");

                ResultSet semRS = getSem();
                System.out.println("CourseListHandler: Extracting semesters...");
                List<Semester> sem = extractSemester(semRS);
                System.out.println("CourseListHandler: Extraction complete.");

                ResultSet engagedCourses = getBelegteLehrveranstaltungen(userid); //Belegte Kurse

                if (engagedCourses == null) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    baseRequest.setHandled(true);
                    System.out.println("CourseListHandler: Failed fetching unattended courses.");
                    return;
                }
                System.out.println("CourseListHandler: Fetched unattended courses.");

                System.out.println("CourseListHandler: Extracting courses...");
                List<Lehrveranstaltung> lv = extractVeranstaltungen(engagedCourses, true);
                System.out.println("CourseListHandler: Extraction complete.");
                System.out.println("CourseListHandler: Sorting by semester...");

                LehrveranstaltungsListe[] list = new LehrveranstaltungsListe[sem.size()];
                int i = 0;
                for (Semester x : sem) {
                    LehrveranstaltungsListe semList = new LehrveranstaltungsListe();
                    for (Lehrveranstaltung y : lv) {
                        if(y.getSemester().getJahr() == x.getJahr() && y.getSemester().getSemesterTyp() == x.getSemesterTyp()) {
                            semList.add(y);
                        }
                    }
                    if(semList.getList().size() != 0) {
                        list[i] = semList;
                    }
                    i++;
                }
                System.out.println("CourseListHandler: Done.");

                response.getWriter().println(new Gson().toJson(list, LehrveranstaltungsListe[].class));
                response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);
                System.out.println("CourseListHandler: Done.");

            }
        }
    }

    private ResultSet getSem() {
        ResultSet rs;

        try {
            String query = "SELECT distinct semester, jahr FROM veranstaltung order by jahr desc, semester desc";
            PreparedStatement stmt = con.prepareStatement(query);

            rs = stmt.executeQuery();
            return rs;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Semester> extractSemester(ResultSet rs) {
        List<Semester> sem = new ArrayList<>();

        try {
            while(rs.next()) {
                Semester s = readSemester(rs);
                if (s != null) {
                    sem.add(s);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return sem;
    }

    private Semester readSemester(ResultSet resSet) {
        try {
            String semesterTypString = resSet.getString(1);
            int jahr = resSet.getInt(2);

            Enums.SemesterTyp semesterTyp = null;

            if (semesterTypString.equals("SS")) {
                semesterTyp = Enums.SemesterTyp.SS;
            } else if (semesterTypString.equals("WS")) {
                semesterTyp = Enums.SemesterTyp.WS;
            }
                return new Semester(jahr, semesterTyp);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }


    private ResultSet getLehrveranstaltungen (int userid) {
        ResultSet rs;

        try {
            String query = "select distinct v.id, jahr, semester, name, typ from veranstaltung v inner join veranstaltungsteilnehmer " +
                    "vt ON v.id=vt.veranstaltungsid WHERE v.typ != 'Projektgruppe' AND v.id not in(select v.id from veranstaltung v inner join veranstaltungsteilnehmer vt ON v.id = vt.veranstaltungsid WHERE nutzerid = ?) order by jahr desc, semester desc";
            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setInt(1, userid);

            rs = stmt.executeQuery();

            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    private ResultSet getBelegteLehrveranstaltungen (int userid) {
        ResultSet rs;

        try {
            String query = "select distinct v.id, jahr, semester, name, typ from veranstaltung v inner join veranstaltungsteilnehmer vt on v.id = vt.veranstaltungsid " +
                    "inner join nutzer n on n.id = vt.nutzerid where v.typ != 'Projektgruppe' AND n.id = ? order by jahr desc, semester desc";
            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setInt(1, userid);

            rs = stmt.executeQuery();

            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    private List<Lehrveranstaltung> extractVeranstaltungen (ResultSet resSet, boolean belegt) {
        List<Lehrveranstaltung> courseL = new ArrayList<>();

        try {
            while(resSet.next()) {
                Lehrveranstaltung lv = readVeranstaltung(resSet, belegt);
                if (lv != null) {
                    courseL.add(lv);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return courseL;
    }

    private Lehrveranstaltung readVeranstaltung(ResultSet resSet, boolean belegt) {
        try {
            int id = resSet.getInt(1);
            int jahr = resSet.getInt(2);
            String semesterTypString = resSet.getString(3);
            String name = resSet.getString(4);
            String typString = resSet.getString(5);

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

             try {

                Lehrveranstaltung va = new Lehrveranstaltung(name, new Semester(jahr, semesterTyp), art, id, belegt);
                if(va != null) {
                    return va;
                }
             }
            catch (NullPointerException e) {
            e.printStackTrace();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
}