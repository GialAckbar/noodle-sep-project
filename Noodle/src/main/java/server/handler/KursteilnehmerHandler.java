package server.handler;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import shared.Student;
import shared.Lehrende;
import shared.Teilnehmerliste;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KursteilnehmerHandler extends AbstractHandler {

    Connection con = null;

    public KursteilnehmerHandler(Connection con) {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] splitTarget = target.split("/");

        if (baseRequest.getMethod().equals("GET") && splitTarget.length == 3 && splitTarget[1].equals("kursteilnehmer")) {

            int courseid, userid, friendid;
            boolean isTeacher;
            String token;

            try {
                token = request.getParameter("token");
                userid = Integer.parseInt(request.getParameter("userid"));
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                baseRequest.setHandled(true);
                return;
            }

            switch (splitTarget[2]) {
                case "get": { System.out.println("KursteilnehmerHandler[Get]: Start");

                    try {
                        isTeacher = isTeacher(userid); // Nur Lehrende dürfen Matr-Nr. sehen
                        courseid = Integer.parseInt(request.getParameter("courseid"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        baseRequest.setHandled(true);
                        return;
                    }
                    System.out.println("KursteilnehmerHandler[GET]: userid: "+userid+" | token: "+token+" | courseid: "+courseid+" | isTeacher: "+isTeacher);

                    ResultSet students, teachers;
                    try {
                        ResultSet[] users = getUsersFromCourse(courseid, isTeacher);
                        students = users[0];
                        teachers = users[1];
                        System.out.println("KursteilnehmerHandler[Get]: Alle User gespeichert");
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        baseRequest.setHandled(true);
                        return;
                    }

                    List<Student> extractedStudents = extractStudents(students, isTeacher);
                    List<Lehrende> extractedTeachers = extractTeachers(teachers);
                    String courseName = getCourseNameFromID(courseid);
                    if (extractedStudents == null || extractedTeachers == null || courseName == null) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        baseRequest.setHandled(true);
                        return;
                    }
                    System.out.println("KursteilnehmerHandler[Get]: Studierende und Lehrende extrahiert");

                    Teilnehmerliste userList = new Teilnehmerliste(extractedStudents, extractedTeachers, courseName, isTeacher);
                    response.getWriter().println(new Gson().toJson(userList, Teilnehmerliste.class));
                    response.setStatus(HttpServletResponse.SC_OK);
                    baseRequest.setHandled(true);
                    System.out.println("KursteilnehmerHandler[Get]: Fertig");
                    break;
                }

                case "getAll": { System.out.println("KursteilnehmerHandler[GetAll]: Start");

                    try {
                        isTeacher = isTeacher(userid); // Nur Lehrende dürfen Matr-Nr. sehen
                        courseid = Integer.parseInt(request.getParameter("courseid"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        baseRequest.setHandled(true);
                        return;
                    }
                    System.out.println("KursteilnehmerHandler[GET]: userid: "+userid+" | token: "+token+" | courseid: "+courseid+" | isTeacher: "+isTeacher);

                    if (!(isTeacher)) {
                        response.setStatus((HttpServletResponse.SC_FORBIDDEN));
                        baseRequest.setHandled(true);
                        System.out.println("KursteilnehmerHandler[GetAll]: Client ist kein Lehrer");
                        return;
                    }

                    ResultSet addedStudents = null, addedTeachers = null, notAddedStudents = null, notAddedTeachers = null;
                    try {
                        ResultSet[] usersFromCourse = getUsersFromCourse(courseid, true);
                        ResultSet[] usersNotInCourse = getUsersNotInCourse(courseid);
                        addedStudents = usersFromCourse[0];
                        addedTeachers = usersFromCourse[1];
                        notAddedStudents = usersNotInCourse[0];
                        notAddedTeachers = usersNotInCourse[1];
                        System.out.println("KursteilnehmerHandler[GetAll]: Alle User gespeichert");
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        baseRequest.setHandled(true);
                        System.out.println("KursteilnehmerHandler[GetAll]: User konnten nicht gespeichert werden");
                        return;
                    }

                    List<Student> extractedAddedStudents = extractStudents(addedStudents, true);
                    List<Student> extractedNotStudents = extractStudents(notAddedStudents, true);
                    List<Lehrende> extractedAddedTeachers = extractTeachers(addedTeachers);
                    List<Lehrende> extractedNotTeachers = extractTeachers(notAddedTeachers);
                    String courseName = getCourseNameFromID(courseid);
                    if (extractedAddedStudents == null || extractedNotStudents == null || extractedAddedTeachers == null || extractedNotTeachers == null || courseName == null) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        baseRequest.setHandled(true);
                        return;
                    }
                    System.out.println("KursteilnehmerHandler[GetAll]: Studierende und Lehrende extrahiert");

                    Teilnehmerliste userListAdded = new Teilnehmerliste(extractedAddedStudents, extractedAddedTeachers, courseName);
                    Teilnehmerliste userListNot = new Teilnehmerliste(extractedNotStudents, extractedNotTeachers);
                    Teilnehmerliste[] userList = new Teilnehmerliste[] {userListAdded, userListNot};

                    response.getWriter().println(new Gson().toJson(userList, Teilnehmerliste[].class));
                    response.setStatus(HttpServletResponse.SC_OK);
                    baseRequest.setHandled(true);
                    System.out.println("KursteilnehmerHandler[GetAll]: Fertig");
                    break;
                }

                case "checkFriends": {

                    try {
                        friendid = Integer.parseInt(request.getParameter("friendid"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        baseRequest.setHandled(true);
                        return;
                    }

                    try {
                        String query = "SELECT id FROM friends WHERE (student1_id = ? AND student2_id = ?) OR (student1_id = ? AND student2_id = ?);";
                        PreparedStatement stmt = con.prepareStatement(query);
                        stmt.setInt(1, userid);
                        stmt.setInt(2, friendid);
                        stmt.setInt(3, friendid);
                        stmt.setInt(4, userid);
                        ResultSet rs = stmt.executeQuery();
                        if (!rs.next()) {
                            String query2 = "SELECT id FROM freundschaftsanfragen WHERE (nutzer1_id = ? AND nutzer2_id = ?) OR (nutzer1_id = ? AND nutzer2_id = ?);";
                            PreparedStatement stmt2 = con.prepareStatement(query2);
                            stmt2.setInt(1, userid);
                            stmt2.setInt(2, friendid);
                            stmt2.setInt(3, friendid);
                            stmt2.setInt(4, userid);
                            ResultSet rs2 = stmt2.executeQuery();
                            if (!rs2.next()) {
                                String query3 = "SELECT nutzerid FROM lehrkraft WHERE nutzerid = ?;";
                                PreparedStatement stmt3 = con.prepareStatement(query3);
                                stmt3.setInt(1, friendid);
                                ResultSet rs3 = stmt3.executeQuery();
                                if (!rs3.next()) {
                                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                                } else response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                            } else response.setStatus(HttpServletResponse.SC_CONFLICT);
                        } else response.setStatus(HttpServletResponse.SC_FOUND);
                        baseRequest.setHandled(true);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        baseRequest.setHandled(true);
                    }
                    break;
                }
            }
        }

        else if (baseRequest.getMethod().equals("POST") && splitTarget.length == 3 && splitTarget[1].equals("kursteilnehmer")) {
            if (splitTarget[2].equals("add")) {

                int courseid, userid, targetid;
                boolean isTeacher, isProjektgruppe;
                String token;

                try {
                    token = request.getParameter("token");
                    userid = Integer.parseInt(request.getParameter("userid"));
                    courseid = Integer.parseInt(request.getParameter("courseid"));
                    targetid = new Gson().fromJson(request.getReader(), Integer.class);
                    isTeacher = isTeacher(userid); // Nur Lehrende dürfen Matr-Nr. sehen
                    isProjektgruppe = isProjektgruppe(courseid);
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    baseRequest.setHandled(true);
                    return;
                }
                System.out.println("KursteilnehmerHandler[POST]: userid: " + userid + " | token: " + token + " | courseid: " + courseid + " | targetid: " + targetid);

                System.out.println("KursteilnehmerHandler[Add]: Start");
                if (!(isInCourse(courseid, targetid))) { // Prüfe, ob Ziel bereits im Kurs ist
                    if (addToCourse(courseid, targetid, isTeacher,userid)) {
                        System.out.println("KursteilnehmerHandler[Add]: targetid '" + targetid + "' erfolgreich der courseid '" + courseid + "' zugewiesen");
                        if (isProjektgruppe) {
                            if (ChatHandler.addMember(targetid, courseid, con)) {
                                System.out.println("KursteilnehmerHandler[Add]: targetid '" + targetid + "' erfolgreich dem Chat der Projektgruppe '" + courseid + "' zugewiesen");
                                response.setStatus(HttpServletResponse.SC_OK);
                            } else response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                        } else response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        System.out.println("KursteilnehmerHandler[Add]: targetid '" + targetid + "' konnte nicht der courseid '" + courseid + "' zugewiesen werden");
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                } else response.setStatus(HttpServletResponse.SC_CONFLICT);
                baseRequest.setHandled(true);
                System.out.println("KursteilnehmerHandler[Add]: Fertig");
            }

        } else if (baseRequest.getMethod().equals("DELETE") && splitTarget.length == 3 && splitTarget[1].equals("kursteilnehmer")) {
            if (splitTarget[2].equals("remove")) {

                int courseid, userid, targetid;
                boolean isTeacher, isProjektgruppe;
                String token;

                try {
                    token = request.getParameter("token");
                    userid = Integer.parseInt(request.getParameter("userid"));
                    courseid = Integer.parseInt(request.getParameter("courseid"));
                    targetid = Integer.parseInt(request.getParameter("targetid"));
                    isTeacher = isTeacher(userid); // Nur Lehrende dürfen Matr-Nr. sehen
                    isProjektgruppe = isProjektgruppe(courseid);
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    baseRequest.setHandled(true);
                    return;
                }
                System.out.println("KursteilnehmerHandler[DELETE]: userid: " + userid + " | token: " + token + " | courseid: " + courseid + " | targetid: " + targetid);

                System.out.println("KursteilnehmerHandler[Remove]: Start");
                if (isInCourse(courseid, targetid)) { // Prüfe, ob Ziel noch im Kurs
                    if (removeFromCourse(courseid, targetid, isTeacher, userid)) {
                        System.out.println("KursteilnehmerHandler[Remove]: targetid '" + targetid + "' erfolgreich von courseid '" + courseid + "' entfernt");
                        if (isProjektgruppe) {
                            if (ChatHandler.removeMember(targetid, courseid, con)) {
                                System.out.println("KursteilnehmerHandler[Remove]: targetid '" + targetid + "' erfolgreich aus dem Chat der Projektgruppe '" + courseid + "' entfernt");
                                response.setStatus(HttpServletResponse.SC_OK);
                            } else response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                        } else response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        System.out.println("KursteilnehmerHandler[Remove]: targetid '" + targetid + "' konnte nicht von courseid '" + courseid + "' entfernt werden");
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                } else response.setStatus(HttpServletResponse.SC_CONFLICT);
                baseRequest.setHandled(true);
                System.out.println("KursteilnehmerHandler[Remove]: Fertig");
            }
        }
    }

    private ResultSet[] getUsersFromCourse(int courseid, boolean isTeacher) {
        ResultSet students = null;
        ResultSet teachers = null;

        try {
            // Finde alle Schüler im Kurs
            String matrikelIfTeacher = "";
            if (isTeacher) matrikelIfTeacher = ", matrikelnummer";
            String queryStudents = "SELECT nutzer.id, vorname, nachname, email" + matrikelIfTeacher + " FROM nutzer INNER JOIN student ON nutzer.id = student.nutzerid " +
                        "INNER JOIN veranstaltungsteilnehmer ON nutzer.id = veranstaltungsteilnehmer.nutzerid  WHERE veranstaltungsid = ? ORDER BY vorname;";
            PreparedStatement statementStudents = con.prepareStatement(queryStudents);
            statementStudents.setInt(1, courseid);
            students = statementStudents.executeQuery();
            System.out.println("KursteilnehmerHandler[getUsersFromCourse]: Alle Studenten im Kurs geladen");

            // Finde alle Lehrenden im Kurs
            String queryTeachers = "SELECT nutzer.id, vorname, nachname, email FROM nutzer INNER JOIN lehrkraft ON nutzer.id = lehrkraft.nutzerid " +
                    "INNER JOIN veranstaltungsteilnehmer ON nutzer.id = veranstaltungsteilnehmer.nutzerid  WHERE veranstaltungsid = ? ORDER BY vorname;";
            PreparedStatement statementTeachers = con.prepareStatement(queryTeachers);
            statementTeachers.setInt(1, courseid);
            teachers = statementTeachers.executeQuery();
            System.out.println("KursteilnehmerHandler[getUsersFromCourse]: Alle Lehrenden im Kurs geladen");

            return new ResultSet[] {students, teachers};
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ResultSet[] getUsersNotInCourse(int courseid) {
        ResultSet students = null;
        ResultSet teachers = null;

        try {
            // Finde alle Schüler, die nicht im Kurs sind
            String queryStudents = "SELECT n.id, vorname, nachname, email, matrikelnummer FROM nutzer n INNER JOIN student s ON n.id = s.nutzerid WHERE n.id NOT IN " +
                    "(SELECT n.id FROM nutzer n INNER JOIN student s ON n.id = s.nutzerid INNER JOIN veranstaltungsteilnehmer v ON n.id = v.nutzerid WHERE veranstaltungsid = ?);";
            PreparedStatement statementStudents = con.prepareStatement(queryStudents);
            statementStudents.setInt(1, courseid);
            students = statementStudents.executeQuery();
            System.out.println("KursteilnehmerHandler[getUsersNotInCourse]: Alle Studenten, die nicht im Kurs sind, geladen");

            // Finde alle Lehrenden, die nicht im Kurs sind
            String queryTeachers = "SELECT n.id, vorname, nachname, email FROM nutzer n INNER JOIN lehrkraft l ON n.id = l.nutzerid WHERE n.id NOT IN " +
                    "(SELECT n.id FROM nutzer n INNER JOIN lehrkraft l ON n.id = l.nutzerid INNER JOIN veranstaltungsteilnehmer v ON n.id = v.nutzerid WHERE veranstaltungsid = ?);";
            PreparedStatement statementTeachers = con.prepareStatement(queryTeachers);
            statementTeachers.setInt(1, courseid);
            teachers = statementTeachers.executeQuery();
            System.out.println("KursteilnehmerHandler[getUsersNotInCourse]: Alle Lehrenden, die nicht im Kurs sind, geladen");

            return new ResultSet[] {students, teachers};
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Student> extractStudents(ResultSet resultSet, boolean isTeacher) {
        List<Student> students = new ArrayList<>();
        try {
            while(resultSet.next()) {
                Student student = getStudent(resultSet, isTeacher);
                if (student != null) {
                    students.add(student);
                    System.out.println(student.getID() + " | " + student.getVorname() + " " + student.getNachname() + " | " + student.getEmail() + " | " + student.getMatrikelnummer());
                }
            }
            return students;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Lehrende> extractTeachers(ResultSet resultSet) {
        List<Lehrende> teachers = new ArrayList<>();
        try {
            while(resultSet.next()) {
                Lehrende teacher = getTeacher(resultSet);
                if (teacher != null) {
                    teachers.add(teacher);
                    System.out.println(teacher.getID() + " | " + teacher.getVorname() + " " + teacher.getNachname() + " | " + teacher.getEmail());
                }
            }
            return teachers;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Student getStudent(ResultSet rs, boolean isTeacher) {
        try {
            int id = rs.getInt(1);
            String vorname = rs.getString(2);
            String nachname = rs.getString(3);
            String email = rs.getString(4);
            if (isTeacher) {
                int matrikelnummer = rs.getInt(5);
                return new Student(vorname, nachname, email, matrikelnummer, id);
            }
            return new Student(vorname, nachname, email, 0, id);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    private Lehrende getTeacher(ResultSet rs) {
        try {
            int id = rs.getInt(1);
            String vorname = rs.getString(2);
            String nachname = rs.getString(3);
            String email = rs.getString(4);
            return new Lehrende(vorname, nachname, email, id);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    private String getCourseNameFromID(int courseid) {
        try {
            String query = "SELECT name FROM veranstaltung WHERE id = ?;";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, courseid);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
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
                System.out.println("KursteilnehmerHandler: Client ist Lehrender");
                return true;
            }
            System.out.println("KursteilnehmerHandler: Client ist Student");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isProjektgruppe(int courseid) {
        try {
            String query = "SELECT typ FROM veranstaltung WHERE id = ?;";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, courseid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                if (resultSet.getString(1).equals("Projektgruppe")) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isInCourse(int courseid, int targetid) {
        try {
            String query = "SELECT nutzerid FROM veranstaltungsteilnehmer WHERE veranstaltungsid = ? AND nutzerid = ?;";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, courseid);
            statement.setInt(2, targetid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())  return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean addToCourse(int courseid, int targetid, boolean isTeacher, int userid) {
        try {
            if (isTeacher || userid == targetid) {
                String query = "INSERT INTO veranstaltungsteilnehmer (veranstaltungsid, nutzerid) VALUES (?, ?) RETURNING nutzerid;";
                PreparedStatement statement = con.prepareStatement(query);
                statement.setInt(1, courseid);
                statement.setInt(2, targetid);
                statement.executeQuery();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean removeFromCourse(int courseid, int targetid, boolean isTeacher, int userid) {
        try {
            if (isTeacher || userid == targetid) {
                String query = "DELETE FROM veranstaltungsteilnehmer WHERE veranstaltungsid = ? AND nutzerid = ? RETURNING nutzerid;";
                PreparedStatement statement = con.prepareStatement(query);
                statement.setInt(1, courseid);
                statement.setInt(2, targetid);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}