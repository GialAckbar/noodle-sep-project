package server.handler;

import client.Launcher;
import client.urlconnections.ProfilHinzufügenRequest;
import com.google.gson.Gson;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import server.db.DatabaseManager;
import server.db.DatabaseUtility;
import server.db.FileUtility;
import shared.*;
import shared.accounts.LehrkraftmitPasswort;
import shared.accounts.StudentMitPasswort;
import shared.accounts.UseridMitToken;
import shared.utility.GsonUtility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class ProfilÄnderungenSpeichernHandler extends AbstractHandler {
    Connection con = null;

    public static final String MULTIPART_FORMDATA_TYPE = "multipart/form-data";

    private static final MultipartConfigElement MULTI_PART_CONFIG = new MultipartConfigElement(System.getProperty("java.io.tmpdir"));

    public static boolean isMultipartRequest(ServletRequest request) {
        return request.getContentType() != null
                && request.getContentType().startsWith(MULTIPART_FORMDATA_TYPE);
    }
    public static void enableMultipartSupport(HttpServletRequest request) {
        request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, MULTI_PART_CONFIG);
    }
    public ProfilÄnderungenSpeichernHandler () {
        try {
            this.con = new DatabaseManager().getConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");

        if (request.getMethod().equals("POST") && splitTarget.length == 3 && splitTarget[1].equals("profilbearbeiten2")) {
            baseRequest.setHandled(true);

            con = DatabaseUtility.getValidConnection(con);
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            boolean multipartRequest = HttpMethod.POST.is(request.getMethod()) && isMultipartRequest(request);
            if (multipartRequest) {
                enableMultipartSupport(request);
            } else {
                editResponse(response, HttpServletResponse.SC_BAD_REQUEST, 400);
                return;
            }

            try {
                con.setAutoCommit(false);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            if (splitTarget[2].equals("student")) {
                StudentMitPasswort studentMitPasswort = extractStudentMitPasswort(request);
                Student student = studentMitPasswort.getStudent();

                try {
                    updateUserWithImage(student, request, studentMitPasswort.getPasswort());
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                editResponse(response, HttpServletResponse.SC_OK, 200);

            } else if (splitTarget[2].equals("lehrkraft")) {
                LehrkraftmitPasswort lehrerMitPasswort = extractLehrkraftMitPasswort(request);
                Lehrende lehrer =  lehrerMitPasswort.getLehrkraft();

                try {
                    updateUserWithImage(lehrer, request, lehrerMitPasswort.getPasswort());
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                editResponse(response, HttpServletResponse.SC_OK, 200);

            } else {
                editResponse(response, HttpServletResponse.SC_BAD_REQUEST, 400);
            }

            try {
                if(!con.getAutoCommit()) {
                    System.out.println("Profile Rollback");
                    con.rollback();
                }
                con.setAutoCommit(true);
                con.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    protected void updateUserWithImage(User user, HttpServletRequest request, String passwort) throws SQLException {
        int dateiid = -1;
        if (user.getImage() != null) {
            dateiid = createProfilePicture(request);
        }
        updateUser(user, dateiid, passwort);
    }

    protected int createProfilePicture(HttpServletRequest request) {
        int ret = -1;
        String[] file = new String[0];

        try {
            file = extractProfilePicture(request);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return -2;
        }
        System.out.println(file.length);

        if (file.length != 2) {
            return -1;
        }

        try {
            String query = "insert into datei values (default, ?,?, 'profilbild') returning id;";
            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setString(1, file[0]);
            stmt.setString(2, file[1]);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return -1;
    }

    protected String[] extractProfilePicture (HttpServletRequest request) throws NullPointerException {
        try {
            Part part = request.getPart("ProfilePicture");

            if (part == null) {
                throw new NullPointerException();
            }

            String path = "./../DateienDatenbank/ProfilePictures/";
            boolean isUnique = false;
            File file = null;
            String pathWithName = "";
            String fileEnding = "";
            while(!isUnique) {
                fileEnding = FileUtility.randomString() + "." + FileUtility.getFileExtensionFromName(part.getSubmittedFileName());
                pathWithName = path + fileEnding;
                file = new File(pathWithName);
                file.getParentFile().mkdirs();
                if (file.createNewFile()) {
                    isUnique = true;
                }
            }

            FileUtility.copyInputStreamToFile(part.getInputStream(), file);
            String[] ret = {path, fileEnding };
            return ret;

        } catch (IOException | ServletException e) {
            e.printStackTrace();
        }

        return new String[0];
    }

    protected void updateUser(User user, int dateiid, String passwort) throws SQLException {
        ResultSet rs = null;
        int id = user.getID();

        if (dateiid >= 0) {
            String query = "update nutzer set imageid = ? where id = ? returning id;";
            try {
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setInt(1, dateiid);
                stmt.setInt(2, id);
                rs = stmt.executeQuery();
                rs.next();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        String query = "update adressen set straße = ?, nummer = ?, plz = ?, ort = ? where id in (select adressid from nutzer where id = ?) returning id;";
        try {
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, user.getAdresse().getStrasse());
            stmt.setString(2, user.getAdresse().getHausnummer());
            stmt.setInt(3, user.getAdresse().getPlz());
            stmt.setString(4, user.getAdresse().getOrt());
            stmt.setInt(5, id);
            rs = stmt.executeQuery();
            rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!passwort.equals("")) {
            query = "update accounts set passwort = ? where nutzerid = ? returning nutzerid;";
            try {
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, passwort);
                stmt.setInt(2, id);
                rs = stmt.executeQuery();
                rs.next();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (user instanceof Student) {
            updateUserStudent(user);
        }
        else if (user instanceof Lehrende) {
            updateUserLehrende(user);
        }
        con.commit();
    }

    protected void updateUserStudent(User user) {
        ResultSet rs = null;
        int id = user.getID();

        String query = "update student set studienfach = ? where nutzerid = ? returning nutzerid;";
        try {
            PreparedStatement stmt = con.prepareStatement(query);
            Student s = (Student) user;
            stmt.setString(1, s.getStudienfach());
            stmt.setInt(2, id);
            rs = stmt.executeQuery();
            rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void updateUserLehrende(User user) {
        ResultSet rs = null;
        int id = user.getID();

        String query = "update lehrkraft set lehrstuhl = ?, forschungsgebiet = ? where nutzerid = ? returning nutzerid;";
        try {
            PreparedStatement stmt = con.prepareStatement(query);
            Lehrende l = (Lehrende) user;
            stmt.setString(1, l.getLehrstuhl());
            stmt.setString(2, l.getForschungsgebiet());
            stmt.setInt(3, id);
            rs = stmt.executeQuery();
            rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected StudentMitPasswort extractStudentMitPasswort(HttpServletRequest request) throws ServletException, IOException {
        StudentMitPasswort student = null;

        Part part = request.getPart("ProfileJson");
        String partType = part.getContentType();

        if (!partType.equals("application/json")) { return null; }

        InputStream inputStream = part.getInputStream();
        String Json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

        student = GsonUtility.getGson().fromJson(Json, StudentMitPasswort.class);

        return student;
    }

    protected LehrkraftmitPasswort extractLehrkraftMitPasswort(HttpServletRequest request) throws ServletException, IOException {
        LehrkraftmitPasswort lehrkraft = null;

        Part part = request.getPart("ProfileJson");
        String partType = part.getContentType();

        if (!partType.equals("application/json")) { return null; }

        InputStream inputStream = part.getInputStream();
        String Json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

        lehrkraft = GsonUtility.getGson().fromJson(Json, LehrkraftmitPasswort.class);

        return lehrkraft;
    }

    protected void editResponse(HttpServletResponse response, int responceCode, int ret) {
        try {
            response.getWriter().println(new Gson().toJson(ret, Integer.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        response.setStatus(responceCode);
    }
}