package server.handler;

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

public class ProfilHinzufügenHandler extends AbstractHandler {
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
    public ProfilHinzufügenHandler () {
        try {
            this.con = new DatabaseManager().connect();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");

        if (request.getMethod().equals("POST") && splitTarget.length == 3 && splitTarget[1].equals("addProfile")) {
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
                editResponse(response, HttpServletResponse.SC_BAD_REQUEST, -1);
                return;
            }

            try {
                con.setAutoCommit(false);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            if (splitTarget[2].equals("student")) {
                System.out.println("createStudent");
                StudentMitPasswort studentMitPasswort = extractStudentMitPasswort(request);
                Student student = studentMitPasswort.getStudent();

                int userid = createUserWithAddressAndImage(student, request);
                System.out.println("UserWithAddressAndImage created: " + userid);
                if(userid >= 0) {
                    if(createAccount(userid, studentMitPasswort.getPasswort())) {
                        System.out.println("Before Matrikelnummer");
                        int matrikelnummer = createStudent(userid, student.getStudienfach());
                        System.out.println("After Matrikelnummer");
                        if (matrikelnummer >= 0) {
                            editResponse(response, HttpServletResponse.SC_OK, matrikelnummer);
                            try {
                                con.commit();
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                        } else {
                            editResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, -1);
                        }
                    } else {
                        editResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, -1);
                    }
                } else {
                    editResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, -1);
                }
            } else if (splitTarget[2].equals("lehrkraft")) {
                LehrkraftmitPasswort lehrkraftmitPasswort = extractLehrkraftMitPasswort(request);
                Lehrende lehrkraft = lehrkraftmitPasswort.getLehrkraft();

                System.out.println("CreateCompleteUser");
                int userid = createCompleteUser(lehrkraft, lehrkraftmitPasswort.getPasswort(), request, response);
                System.out.println("Userid:" + userid);
                if (userid < 0) {
                    editResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, -1);
                } else {
                    if(createLehrkraft(userid, lehrkraft.getLehrstuhl(), lehrkraft.getForschungsgebiet())) {
                        editResponse(response, HttpServletResponse.SC_OK, 1);
                        try {
                            con.commit();
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    } else {
                        editResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, -1);
                    }
                }
            } else {
                editResponse(response, HttpServletResponse.SC_BAD_REQUEST, -1);
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
    protected int createStudent(int userid, String studienfach) {

        String query = "insert into student values (default, ?, default, ?) returning matrikelnummer;";
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, userid);
            stmt.setString(2, studienfach);

            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return -1;
    }
    protected boolean createLehrkraft (int userid, String lehrstuhl, String forschungsgebiet) {
        String query = "insert into lehrkraft values (default, ?, ?, ?) returning id;";
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, userid);
            stmt.setString(2, lehrstuhl);
            stmt.setString(3, forschungsgebiet);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return true;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return false;
    }
    protected int createCompleteUser(User user, String passwort, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("BeforeUserWithAddressAndImage: ");
        int userid = createUserWithAddressAndImage(user, request);
        System.out.println("UserWithAddressAndImage: " + userid);
        if(userid >= 0) {
            if(createAccount(userid, passwort)) {
                return userid;
            } else {
                return - 1;
            }
        } else {
            return -1;
        }
    }
    protected void editResponse(HttpServletResponse response, int responceCode, int ret) {
        try {
            response.getWriter().println(new Gson().toJson(ret, Integer.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        response.setStatus(responceCode);
    }
    protected int createProfilePicture(HttpServletRequest request) {
        int ret = -1;


        String[] file = new String[0];

        try {
            file = extractProfilePicture(request);
        } catch (NullPointerException e) {
            e.printStackTrace();

            return -2; // No Image Added
        }
        System.out.println(file.length);


        if (file.length != 2) {
            return -1; // Error response
        }

        try {
            String query = "insert into datei values (default, ?,?, 'profilbild') returning id;";
            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setString(1, file[0]);
            stmt.setString(2, file[1]);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1); // Success response
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return -1; // Error response
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
    private int createAddress(Adresse adresse) {
        ResultSet rs = null;

        int id = -1;

        String query = "insert into adressen values(default,?,?,?,?) returning id;";

        try {
            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setString(1, adresse.getStrasse());
            stmt.setString(2, adresse.getHausnummer());
            stmt.setInt(3, adresse.getPlz());
            stmt.setString(4, adresse.getOrt());

            rs = stmt.executeQuery();
            rs.next();
            id = rs.getInt(1);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return id;
    }
    private int createUser(User user, int adressID, int dateiid) {
        ResultSet rs = null;
        int id = -1;
        String query = "insert into nutzer values(default,?,?,?,?,?) returning id;";

        try {
            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setString(1, user.getVorname());
            stmt.setString(2, user.getNachname());
            stmt.setString(3, user.getEmail());
            stmt.setInt(4, adressID);
            if (dateiid >= 0) {
                stmt.setInt(5, dateiid);
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            rs = stmt.executeQuery();
            rs.next();
            id = rs.getInt(1);
            System.out.println("User Created");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }
    private int createUserWithAddressAndImage(User user, HttpServletRequest request) {
        int adressid = createAddress(user.getAdresse());

        int dateiid = createProfilePicture(request);

        int userid = -1;

        if (adressid >= 0 && (dateiid >= 0 || dateiid == -2)) { // dateiid: -1 error, -2 no image, >=2 image added
            userid = createUser(user, adressid, dateiid);
        }
        return userid;
    }
    private boolean createAccount(int userid, String passwort) {
        String query = "insert into accounts values(default,?,?) returning id;";

        try {
            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setInt(1, userid);
            stmt.setString(2, passwort);

            stmt.executeQuery();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
}
