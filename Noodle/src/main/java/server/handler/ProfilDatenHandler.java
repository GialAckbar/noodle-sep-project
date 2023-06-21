package server.handler;

import client.Controller.ProfilView;
import client.Controller.ThemaHinzufügen;
import client.Controller.Themenangebote;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import server.db.DatabaseUtility;
import shared.*;
import shared.utility.GsonUtility;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfilDatenHandler extends AbstractHandler {

    Connection con = null;

    public ProfilDatenHandler(Connection con) {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");

        if (baseRequest.getMethod().equals("GET") && splitTarget.length == 3 && splitTarget[1].equals("profildaten")) {
            if (splitTarget[2].equals("profil")) {
                int accountid, userid = -1;
                String token;
                Boolean ich;
                Boolean friend;
                Boolean inCourse;

                try {
                    token = request.getParameter("token");
                    userid = Integer.parseInt(request.getParameter("userid"));
                    accountid = Integer.parseInt(request.getParameter("accountid"));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    baseRequest.setHandled(true);
                    return;
                }
                System.out.println("ProfilDatenHandler[GET]: userid: " + userid + " | token: " + token);

                System.out.println("ProfilDatenHandler: Retrieved userID and token successfully.");
                //ResultSet profildatenstudierender = getProfilDatenStudent(userid); // Profil Daten
                //ResultSet profildatenlehrender = getProfilDatenLehrender(userid);
                //ResultSet profilandereView = getProfilDatenLehrender(accountid);

                response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);
                Profil profil;
                //User myself = DatabaseUtility.getUserSubclass(con, userid);
                //User other = DatabaseUtility.getUserSubclass(con, accountid);
                Student myselfStudent = null;
                Lehrende myselfLehrende = null;
                Student otherStudent = null;
                Lehrende otherLehrende = null;
                Enums.Current aktmyself;
                Enums.Current aktother;


                System.out.println("ProfilDatenHandler: Extracting...");
                if (DatabaseUtility.getUserSubclass(con, userid) instanceof Student) {
                    myselfStudent = (Student) DatabaseUtility.getUserSubclass(con, userid);
                } else if (DatabaseUtility.getUserSubclass(con, userid) instanceof Lehrende) {
                    myselfLehrende = (Lehrende) DatabaseUtility.getUserSubclass(con, userid);
                }
                if (DatabaseUtility.getUserSubclass(con, accountid) instanceof Student) {
                    otherStudent = (Student) DatabaseUtility.getUserSubclass(con, accountid);
                } else if (DatabaseUtility.getUserSubclass(con, accountid) instanceof Lehrende) {
                    otherLehrende = (Lehrende) DatabaseUtility.getUserSubclass(con, accountid);
                }

                if (myselfLehrende != null || myselfStudent != null) {
                    if (userid != accountid && DatabaseUtility.isTeacher(con, userid) == false && DatabaseUtility.isTeacher(con, accountid) == false && DatabaseUtility.isFriend(con, userid, accountid)) {
                        aktmyself = Enums.Current.STUDENT;
                        aktother = Enums.Current.STUDENT;
                        ich = false;
                        friend = true;
                        profil = new Profil(aktmyself, aktother, ich, otherStudent, friend);
                        response.getWriter().println(GsonUtility.getGson().toJson(profil, Profil.class));
                    } else if (userid != accountid && DatabaseUtility.isTeacher(con, userid) == false && DatabaseUtility.isTeacher(con, accountid) == false && DatabaseUtility.isFriend(con, userid, accountid) == false) {
                        aktmyself = Enums.Current.STUDENT;
                        aktother = Enums.Current.STUDENT;
                        ich = false;
                        friend = false;
                        profil = new Profil(aktmyself, aktother, ich, otherStudent);
                        response.getWriter().println(GsonUtility.getGson().toJson(profil, Profil.class));
                    } else if (userid != accountid && DatabaseUtility.isTeacher(con, userid) && DatabaseUtility.isTeacher(con, accountid) == false) {
                        aktmyself = Enums.Current.LEHRKRAFT;
                        aktother = Enums.Current.STUDENT;
                        ich = false;
                        friend = false;
                        profil = new Profil(aktmyself, aktother, ich, otherStudent);
                        response.getWriter().println(GsonUtility.getGson().toJson(profil, Profil.class));
                    } else if (userid != accountid && DatabaseUtility.isTeacher(con, userid) == false && DatabaseUtility.isTeacher(con, accountid) && isStudentinClass(userid,accountid) == false) {
                        aktmyself = Enums.Current.STUDENT;
                        aktother = Enums.Current.LEHRKRAFT;
                        ich = false;
                        inCourse = false;
                        profil = new Profil(aktmyself, aktother, ich, myselfStudent, otherLehrende,inCourse);
                        response.getWriter().println(GsonUtility.getGson().toJson(profil, Profil.class));
                    } else if (userid != accountid && DatabaseUtility.isTeacher(con, userid) && DatabaseUtility.isTeacher(con, accountid)) {
                        aktmyself = Enums.Current.LEHRKRAFT;
                        aktother = Enums.Current.LEHRKRAFT;
                        ich = false;
                        profil = new Profil(aktmyself, aktother, ich, otherLehrende);
                        response.getWriter().println(GsonUtility.getGson().toJson(profil, Profil.class));
                    } else if (userid == accountid && DatabaseUtility.isTeacher(con, userid) == false) {
                        aktmyself = Enums.Current.STUDENT;
                        ich = true;
                        profil = new Profil(aktmyself, ich, myselfStudent);
                        response.getWriter().println(GsonUtility.getGson().toJson(profil, Profil.class));
                    } else if (userid == accountid && DatabaseUtility.isTeacher(con, userid)) {
                        aktmyself = Enums.Current.LEHRKRAFT;
                        ich = true;
                        profil = new Profil(aktmyself, ich, myselfLehrende);
                        response.getWriter().println(GsonUtility.getGson().toJson(profil, Profil.class));
                    }
                    else if (userid != accountid && DatabaseUtility.isTeacher(con, userid) == false && DatabaseUtility.isTeacher(con, accountid) && isStudentinClass(userid,accountid)) {
                        aktmyself = Enums.Current.STUDENT;
                        aktother = Enums.Current.LEHRKRAFT;
                        ich = false;
                        inCourse = true;
                        profil = new Profil(aktmyself, aktother, ich, myselfStudent, otherLehrende,inCourse);
                        response.getWriter().println(GsonUtility.getGson().toJson(profil, Profil.class));
                    }
                }
                if (myselfStudent == null && myselfLehrende == null && otherStudent == null && otherLehrende == null) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    baseRequest.setHandled(true);
                    System.out.println("ProfilDatenHandler: Fetching profil data failed.");
                    return;
                }

                System.out.println("ProfilDatenHandler: Done.");
            } else if (splitTarget[2].equals("themenangeboteStudent")) {
                int userid = -1;
                String token;

                try {
                    token = request.getParameter("token");
                    userid = Integer.parseInt(request.getParameter("userid"));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    baseRequest.setHandled(true);
                    return;
                }

                System.out.println("ProfilDatenHandlerThemen[GET]: userid: " + userid + " | token: " + token);

                System.out.println("ProfilDatenHandlerThemen[GET]: Retrieved userID and token successfully.");
                response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);

                ThemenangebotsListe liste;
                System.out.println("ProfilDatenHandlerThemen: Extracting Topics...");
                liste = getAllTopicsStudent(userid);

                if (liste == null) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    baseRequest.setHandled(true);
                    System.out.println("ProfilDatenHandlerThemen: Fetching topic data failed.");
                    return;
                }
                response.getWriter().println(new Gson().toJson(liste, ThemenangebotsListe.class));
                System.out.println("ProfilDatenHandlerThemen: Done.");
            } else if (splitTarget[2].equals("themenangeboteLehrender")) {
                int userid = -1;
                String token;

                try {
                    token = request.getParameter("token");
                    userid = Integer.parseInt(request.getParameter("userid"));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    baseRequest.setHandled(true);
                    return;
                }

                System.out.println("ProfilDatenHandlerThemen[GET]: userid: " + userid + " | token: " + token);

                System.out.println("ProfilDatenHandlerThemen[GET]: Retrieved userID and token successfully.");
                response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);

                ThemenangebotsListe liste;
                System.out.println("ProfilDatenHandlerThemen: Extracting Topics...");
                liste = getAllTopicsLehrender(userid);

                if (liste == null) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    baseRequest.setHandled(true);
                    System.out.println("ProfilDatenHandlerThemen: Fetching topic data failed.");
                    return;
                }
                response.getWriter().println(new Gson().toJson(liste, ThemenangebotsListe.class));
                System.out.println("ProfilDatenHandlerThemen: Done.");
            } else if (splitTarget[2].equals("themenangeboteLehrenderOther")) {
                int userid,accountid = -1;
                String token;

                try {
                    token = request.getParameter("token");
                    userid = Integer.parseInt(request.getParameter("userid"));
                    accountid = Integer.parseInt(request.getParameter("accountid"));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    baseRequest.setHandled(true);
                    return;
                }

                System.out.println("ProfilDatenHandlerThemen[GET]: userid: " + userid + " | token: " + token);

                System.out.println("ProfilDatenHandlerThemen[GET]: Retrieved userID and token successfully.");
                response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);

                ThemenangebotsListe liste;
                System.out.println("ProfilDatenHandlerThemen: Extracting Topics...");
                liste = getAllTopicsLehrender(accountid);

                if (liste == null) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    baseRequest.setHandled(true);
                    System.out.println("ProfilDatenHandlerThemen: Fetching topic data failed.");
                    return;
                }
                response.getWriter().println(new Gson().toJson(liste, ThemenangebotsListe.class));
                System.out.println("ProfilDatenHandlerThemen: Done.");
            } else if (splitTarget[2].equals("themenangebot")) {
                int userid, thid = -1;
                String token;

                try {
                    token = request.getParameter("token");
                    userid = Integer.parseInt(request.getParameter("userid"));
                    thid = Integer.parseInt(request.getParameter("thid"));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    baseRequest.setHandled(true);
                    return;
                }

                System.out.println("ProfilDatenHandlerThemen[GET]: userid: " + userid + " | token: " + token);

                System.out.println("ProfilDatenHandlerThemen[GET]: Retrieved userID and token successfully.");
                response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);

                Themenangebot themenangebot;
                System.out.println("ProfilDatenHandlerThemen: Extracting Topics...");
                themenangebot = DatabaseUtility.getTopic(con, thid);

                if (themenangebot == null) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    baseRequest.setHandled(true);
                    System.out.println("ProfilDatenHandlerThemen: Fetching topic data failed.");
                    return;
                }
                response.getWriter().println(new Gson().toJson(themenangebot, Themenangebot.class));
                System.out.println("ProfilDatenHandlerThemen: Done.");
            } else if (splitTarget[2].equals("themenangebotLiteratur")) {
                int userid, thid = -1;
                String token;

                try {
                    token = request.getParameter("token");
                    userid = Integer.parseInt(request.getParameter("userid"));
                    thid = Integer.parseInt(request.getParameter("thid"));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    baseRequest.setHandled(true);
                    return;
                }

                System.out.println("ProfilDatenHandlerThemen[GET]: userid: " + userid + " | token: " + token);

                System.out.println("ProfilDatenHandlerThemen[GET]: Retrieved userID and token successfully.");
                response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);

                Themenangebot themenangebot;
                LiteraturverzeichnisListe liste;
                ThemenangebotLiteratur themenangebotLiteratur;
                System.out.println("ProfilDatenHandlerThemen: Extracting Topics...");
                themenangebot = DatabaseUtility.getTopic(con, thid);
                liste = DatabaseUtility.getAllLiteraturen(con, thid);

                themenangebotLiteratur = new ThemenangebotLiteratur(liste, themenangebot);


                if (themenangebot == null) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    baseRequest.setHandled(true);
                    System.out.println("ProfilDatenHandlerThemen: Fetching topic data failed.");
                    return;
                }
                response.getWriter().println(new Gson().toJson(themenangebotLiteratur, ThemenangebotLiteratur.class));
                System.out.println("ProfilDatenHandlerThemen: Done.");

            }
        } else if (baseRequest.getMethod().equals("DELETE") && splitTarget.length == 3 && splitTarget[1].equals("themenangebot")) {
            if (splitTarget[2].equals("removeThema")) {

                int thid, userid;
                String token;

                try {
                    token = request.getParameter("token");
                    userid = Integer.parseInt(request.getParameter("userid"));
                    thid = Integer.parseInt(request.getParameter("thid"));
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    baseRequest.setHandled(true);
                    return;
                }
                System.out.println("ProfilDatenHandler[DELETE]: userid: " + userid + " | token: " + token + " | thid: " + thid);

                System.out.println("ProfilDatenHandler[Remove]: Start");

                if (isThema(thid)) {// Prüfe, ob beide befreundet sind
                    if (removeThema(thid)) {
                        System.out.println("ProfilDatenHandler[Remove]: user:" + userid + " wurde erfolgreich entfernt!");
                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        System.out.println("ProfilDatenHandler[Remove]: user " + userid + " konnte nicht entfernt werden!");
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }

                } else response.setStatus(HttpServletResponse.SC_CONFLICT);
                baseRequest.setHandled(true);
                System.out.println("ProfilDatenHandler[Remove]: Fertig");
            }
        }
    }

    private boolean isThema(int thid) {
        try {
            String query = "SELECT thema.id FROM thema WHERE (id = ?);";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, thid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    private boolean removeThema(int thid) {
        try {
            String query = "DELETE FROM thema t using themenangebote th inner join literaturverzeichnis l on th.thid = l.thid where t.id = th.thid and t.id = l.thid and t.id = ? RETURNING t.id;";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, thid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    private ThemenangebotsListe getAllTopicsLehrender(int userid) {
        ThemenangebotsListe liste = new ThemenangebotsListe();

        try {
            String query = "select thid,titel,beschreibung from themenangebote t inner join thema th on t.thid = th.id where userid = ?;";

            PreparedStatement stmt = con.prepareStatement(query);


            stmt.setInt(1, userid);

            ResultSet rs = stmt.executeQuery();

            try {
                while (rs.next()) {
                    Themenangebot themenangebot = new Themenangebot(rs.getInt(1), rs.getString(2), rs.getString(3));
                    if (themenangebot != null) {
                        liste.add(themenangebot);
                    }
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste != null ? liste : null;
    }
    private ThemenangebotsListe getAllTopicsStudent( int userid) {
        ThemenangebotsListe liste = new ThemenangebotsListe();

        try {
            String query = "select thid,titel,beschreibung from themenangebote t inner join thema th on t.thid = th.id where userid in (select nutzerid from veranstaltungsteilnehmer where veranstaltungsid in (select veranstaltungsid from veranstaltungsteilnehmer where nutzerid =?));";

            PreparedStatement stmt = con.prepareStatement(query);


            stmt.setInt(1, userid);

            ResultSet rs = stmt.executeQuery();

            try {
                while (rs.next()) {
                    Themenangebot themenangebot = new Themenangebot(rs.getInt(1), rs.getString(2), rs.getString(3));
                    if (themenangebot != null) {
                        liste.add(themenangebot);
                    }
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste != null ? liste : null;
    }
    private boolean isStudentinClass( int userid, int accountid) {
        Boolean a = false;
        int userid2 = userid;
        try {
            String query = "SELECT n.id FROM veranstaltungsteilnehmer m INNER JOIN veranstaltung v ON m.veranstaltungsid = v.id INNER JOIN nutzer n ON m.nutzerid = n.id WHERE m.veranstaltungsid IN (SELECT veranstaltungsid FROM veranstaltungsteilnehmer WHERE nutzerid = ?) AND n.id NOT IN (?) and nutzerid = ? and typ != 'Projektgruppe' Order by n.id;";

            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setInt(1,userid);
            stmt.setInt(2,userid2);
            stmt.setInt(3,accountid);

            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                a = true;

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return a;
    }



    /*private ResultSet getProfilDatenLehrender(int userid) {
        ResultSet rs;
        try {
            String query = "select vorname,nachname,email,lehrstuhl from nutzer n inner join lehrender l on(n.id = l.nutzerid) where n.id = ?";
            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setInt(1, userid);

            rs = stmt.executeQuery();

            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    */


   /* private ResultSet getProfilDatenStudent(int userid) {
        ResultSet rs;
        try {
            String query = "select n.id, vorname, nachname, email, straße, nummer, plz, ort, matrikelnummer, studienfach" +
                    "from nutzer n inner join student s on s.nutzerid = n.id inner join adressen a on n.adressid = a.id where n.id = ?;";
            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setInt(1, userid);

            rs = stmt.executeQuery();

            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
*/
    /*private Student readDaten(ResultSet resSet ) {
        try {
            String vorname = resSet.getString(1);
            String nachname = resSet.getString(2);
            String email = resSet.getString(3);
            String studienfach = resSet.getString(4);


            Student stud = new Student(vorname, nachname, email, studienfach);
            return stud;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }
     */


}



