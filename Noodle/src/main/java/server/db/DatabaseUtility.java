package server.db;

import com.sun.javafx.tk.Toolkit;
import javafx.scene.image.Image;
import shared.*;
import shared.accounts.UseridMitToken;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Random;

public class DatabaseUtility {

    public static Connection getValidConnection(Connection con) {
        try {
            if (con.isValid(1)) {
                return con;
            } else {
                return new DatabaseManager().connect();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static boolean isLoggedIn(Connection con, int userid, String token) {
        try {

            String query = "SELECT EXISTS(SELECT * FROM loggedin where nutzerid = ? and token = ?);";

            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setInt(1, userid);
            stmt.setString(2, token);

            ResultSet rs = stmt.executeQuery();
            rs.next();
            boolean tokenExists = rs.getBoolean(1);
            return tokenExists;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isLoggedIn(Connection con, UseridMitToken useridMitToken) {
        if (useridMitToken == null) {
            return false;
        }
        return isLoggedIn(con, useridMitToken.getUserid(), useridMitToken.getToken());
    }

    public static User getUserSubclass(Connection con, int userid) {

        Student student = checkUserIsStudent(con, userid);
        if (student != null) {
            return student;
        }
        Lehrende lehrkraft = checkUserIsLehrende(con, userid);
        if (lehrkraft != null) {
            return lehrkraft;
        }
        return null;
    }

    public static boolean isUserStudent(User user) {
        return user instanceof Student;
    }

    public static boolean isUserLehrkraft(User user) {
        return user instanceof Lehrende;
    }

    public static Lehrende checkUserIsLehrende(Connection con, int userid) {
        Lehrende lehrkraft = null;
        try {
            String query = "select n.id, vorname, nachname, email, straße, nummer, plz, ort, lehrstuhl, forschungsgebiet, imageid " +
                    "from nutzer n inner join lehrkraft l on l.nutzerid = n.id inner join adressen a on n.adressid = a.id where n.id = ?;";

            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setInt(1, userid);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                lehrkraft = new Lehrende(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
                        rs.getInt(7), rs.getString(8), rs.getString(9), rs.getString(10));
                Integer imageid = rs.getObject(11, Integer.class);
                if (imageid != null) {
                    lehrkraft.setImageID(imageid);
                } else {
                    lehrkraft.setImageID(-1);
                }
                System.out.println("ImageID: " + imageid);
            }
            return lehrkraft;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Student checkUserIsStudent(Connection con, int userid) {
        Student student = null;
        try {
            String query = "select n.id, vorname, nachname, email, straße, nummer, plz, ort, matrikelnummer, studienfach, imageid " +
                    "from nutzer n inner join student s on s.nutzerid = n.id inner join adressen a on n.adressid = a.id where n.id = ?;";

            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setInt(1, userid);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                student = new Student(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
                        rs.getInt(7), rs.getString(8), rs.getInt(9), rs.getString(10));
                Integer imageid = rs.getObject(11, Integer.class);
                if (imageid != null) {
                    student.setImageID(imageid);
                } else {
                    student.setImageID(-1);
                }
            }
            return student;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isTeacher(Connection con, int userid) {
        try {
            String query = "SELECT nutzerid FROM lehrkraft WHERE nutzerid = ?;";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, userid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                System.out.println("ProfilDatenhandler: Client ist Lehrender");
                return true;
            }
            System.out.println("ProfilDatenhandler: Client ist Student");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isFriend(Connection con, int userid, int accountid) {
        int a = userid;
        int b = accountid;
        try {
            String query = "SELECT student1_id,student2_id FROM friends WHERE (student1_id = ? AND student2_id = ?) OR (student1_id = ? AND student2_id = ?);";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, userid);
            statement.setInt(2, accountid);
            statement.setInt(3, b);
            statement.setInt(4, a);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public static FreundesListe getallUser(Connection con, int userid) {
        FreundesListe liste = new FreundesListe();
        try {
            String query = "select distinct student2_id, vorname, nachname, email, straße, nummer, plz, ort, matrikelnummer, studienfach from friends f inner join nutzer n on n.id = f.student2_id inner join student s on s.nutzerid = n.id inner join adressen a on (n.adressid = a.id) where student1_id = ?;";

            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setInt(1, userid);

            ResultSet rs = stmt.executeQuery();
            try {
                while (rs.next()) {
                    Student student = new Student(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
                            rs.getInt(7), rs.getString(8), rs.getInt(9), rs.getString(10));
                    if (student != null) {
                        liste.add(student);
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

    public static FreundesListe getallUser2(Connection con, int userid) {
        FreundesListe liste = new FreundesListe();

        try {
            String query = "select distinct student1_id, vorname, nachname, email, straße, nummer, plz, ort, matrikelnummer, studienfach from friends f inner join nutzer n on n.id = f.student1_id inner join student s on s.nutzerid = n.id inner join adressen a on (n.adressid = a.id) where student2_id = ?;";

            PreparedStatement stmt = con.prepareStatement(query);


            stmt.setInt(1, userid);

            ResultSet rs = stmt.executeQuery();

            try {
                while (rs.next()) {
                    Student student = new Student(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
                            rs.getInt(7), rs.getString(8), rs.getInt(9), rs.getString(10));
                    if (student != null) {
                        liste.add(student);
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

    public static LiteraturverzeichnisListe getAllLiteraturen(Connection con,int thid) {
        LiteraturverzeichnisListe liste = new LiteraturverzeichnisListe();

        try {
            String query = "select id,title,autor,jahr,art,thid from literaturverzeichnis where thid = ?;";

            PreparedStatement stmt = con.prepareStatement(query);


            stmt.setInt(1, thid);

            ResultSet rs = stmt.executeQuery();

            try {
                while (rs.next()) {
                    Literaturverzeichnis literaturverzeichnis = new Literaturverzeichnis(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),rs.getString(5), rs.getInt(6));
                    if (literaturverzeichnis != null) {
                        liste.add(literaturverzeichnis);
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
    public static Themenangebot getTopic(Connection con, int thid) {
        Themenangebot themenangebot = new Themenangebot();

        try {
            String query = "select id,titel,beschreibung from thema t where id = ?;";

            PreparedStatement stmt = con.prepareStatement(query);


            stmt.setInt(1, thid);

            ResultSet rs = stmt.executeQuery();

            try {
                if(rs.next()) {
                    Themenangebot th = new Themenangebot(rs.getInt(1), rs.getString(2), rs.getString(3));
                    themenangebot = th;
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return themenangebot;
    }

    public static FreundesListe getAllRequests(Connection con, int userid) {
        FreundesListe liste = new FreundesListe();
        try {
            String query = "select nutzer1_id, vorname, nachname, email, straße, nummer, plz, ort, matrikelnummer, studienfach from freundschaftsanfragen f inner join nutzer n on n.id = f.nutzer1_id inner join student s on s.nutzerid = n.id inner join adressen a on (n.adressid = a.id) where nutzer2_id = ?;";

            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setInt(1, userid);

            ResultSet rs = stmt.executeQuery();
            try {
                while (rs.next()) {
                    Student student = new Student(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
                            rs.getInt(7), rs.getString(8), rs.getInt(9), rs.getString(10));
                    if (student != null) {
                        liste.add(student);
                    }
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

}
