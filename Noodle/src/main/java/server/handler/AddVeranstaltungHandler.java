package server.handler;

import com.google.gson.Gson;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.AbstractHandler;
import server.ServerUtility;
import server.db.DatabaseManager;
import server.db.DatabaseUtility;
import server.db.FileUtility;
import shared.*;
import shared.accounts.UseridMitToken;
import shared.quiz.Quiz;
import shared.utility.ElementMitKategorie;
import shared.utility.GsonUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AddVeranstaltungHandler extends AbstractHandler {
    Connection con = null;

    public static final String MULTIPART_FORMDATA_TYPE = "multipart/form-data";

    private static final MultipartConfigElement MULTI_PART_CONFIG = new MultipartConfigElement(System.getProperty("java.io.tmpdir"));

    public static boolean isMultipartRequest(ServletRequest request) {
        return request.getContentType() != null && request.getContentType().startsWith(MULTIPART_FORMDATA_TYPE);
    }
    public static void enableMultipartSupport(HttpServletRequest request) {
        request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, MULTI_PART_CONFIG);
    }


    public AddVeranstaltungHandler () {
        try {
            DatabaseManager db = new DatabaseManager();
            this.con = db.connect();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    List<ElementMitKategorie> elementsToAdd = new ArrayList<ElementMitKategorie>();

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");

        if (baseRequest.getMethod().equals("POST") && splitTarget.length == 2 && splitTarget[1].equals("addCourse")) {

            baseRequest.setHandled(true);

            con = DatabaseUtility.getValidConnection(con);
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            boolean multipartRequest = HttpMethod.POST.is(request.getMethod()) && isMultipartRequest(request);
            if (multipartRequest) {
                enableMultipartSupport(request);
            }

            Lehrveranstaltung lehrveranstaltung = extractVeranstaltung(request);
            UseridMitToken useridMitToken = extractIdWithToken(request);

            if(!DatabaseUtility.isLoggedIn(con, useridMitToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }


            if (lehrveranstaltung != null) {

                if(!(DatabaseUtility.getUserSubclass(con, useridMitToken.getUserid()) instanceof Lehrende) && lehrveranstaltung.getArt() != Enums.Art.PROJEKTGRUPPE) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                int courseid = insertCourse(lehrveranstaltung);
                if (courseid == -1) {
                    response.getWriter().println(GsonUtility.getGson().toJson(-1, Integer.class));
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return;
                }
                if (courseid == -2) {
                    response.getWriter().println(GsonUtility.getGson().toJson(-2, Integer.class));
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    return;
                }

                lehrveranstaltung.setVeranstaltungsID(courseid);
                try {
                    addErsteller(lehrveranstaltung, useridMitToken.getUserid());
                    createKategorien(lehrveranstaltung);
                    addKategorien(request, lehrveranstaltung, "./../DateienDatenbank/");

                    if (lehrveranstaltung.getArt().equals(Enums.Art.PROJEKTGRUPPE)) {
                        int addChatSuccess = ChatHandler.createGroup(useridMitToken.getUserid(), courseid, con);
                    }

                    response.getWriter().println(new Gson().toJson(lehrveranstaltung.getVeranstaltungsID(), Integer.class));
                    response.setStatus(HttpServletResponse.SC_OK);

                } catch (SQLException | ServletException | IOException | NullPointerException e) {
                    e.printStackTrace();
                    response.getWriter().println(GsonUtility.getGson().toJson(-1, Integer.class));
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return;
                }
            } else {
                response.getWriter().println(GsonUtility.getGson().toJson(-1, Integer.class));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }



        }
    }
    public void addErsteller(Lehrveranstaltung lehrveranstaltung, int userid) throws SQLException{
        String query = "insert into veranstaltungsteilnehmer values(?,?) on conflict do nothing;";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, lehrveranstaltung.getVeranstaltungsID());
        stmt.setInt(2, userid);
        stmt.executeUpdate();
    }
    public void addKategorien(HttpServletRequest request, Lehrveranstaltung lehrveranstaltung, String filesDatabasePath) throws ServletException, SQLException, IOException, NullPointerException {
        elementsToAdd = new ArrayList<ElementMitKategorie>();

        List<LVKategorie> kategorien = lehrveranstaltung.getKategorien();

        for (int i = 0; i < kategorien.size(); i++) {
            List<LVKategorieElement> elemente = kategorien.get(i).getKategorieElemente();

            for (int j = 0; j < elemente.size(); j++) {



                if (elemente.get(j) instanceof KategorieDatei) {
                    createDateiElement(request, lehrveranstaltung.getVeranstaltungsID(), kategorien.get(i).getID(), i, j, filesDatabasePath);
                } else if (elemente.get(j) instanceof Reminder || elemente.get(j) instanceof Quiz || elemente.get(j) instanceof Todo || elemente.get(j) instanceof Lernkarte || elemente.get(j) instanceof shared.Bewertung) {
                    LVKategorie temp = new LVKategorie(kategorien.get(i).getID(), "Kategorie", i);
                    elementsToAdd.add(new ElementMitKategorie(elemente.get(j), temp));

                    if (elemente.get(j) instanceof Todo) {
                    }
                }
            }

        }
        try {

            UpdateKursHandler.addAllElements(lehrveranstaltung, request, con, elementsToAdd);
            elementsToAdd = new ArrayList<ElementMitKategorie>();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    protected static String[] copyFile(HttpServletRequest request, int coursePosition, int categoryPosition, String path) throws ServletException, IOException {
        Part part = request.getPart("file-" + coursePosition + "-" + categoryPosition);
        //System.out.println("file-" + coursePosition + "-" + categoryPosition);
        if (part != null) {
            //System.out.println("NOT NULL");
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

            String[] ret = {path, fileEnding, part.getSubmittedFileName()};
            return ret;
        }
        return new String[0];
    }
    protected int createKategorieElement(int position, int kategorieid) {
        int elementid = -1;

        try {
            PreparedStatement stmt = con.prepareStatement("insert into kategorieelement values (default, ?, ?) returning id;");

            stmt.setInt(1, position);
            stmt.setInt(2, kategorieid);

            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                elementid = rs.getInt(1);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return elementid;
    }
    protected int createDatei (String path, String name, String anzeigename) throws SQLException {
        int dateiid = -1;

        PreparedStatement stmt = con.prepareStatement("insert into datei values (default, ?, ?, ?) RETURNING id");

        stmt.setString(1, path);
        stmt.setString(2, name);
        stmt.setString(3, anzeigename);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            dateiid = rs.getInt(1);
        }

        return dateiid;
    }
    protected int createDateiZuElement (int dateiid, int elementid) throws SQLException {
        int id = -1;

        PreparedStatement stmt = con.prepareStatement("insert into dateizukategorie values (?, ?, default) RETURNING id;");

        stmt.setInt(1, dateiid);
        stmt.setInt(2, elementid);

        ResultSet rs = stmt.executeQuery();

        if(rs.next()) {
            id = rs.getInt(1);
        }

        return id;
    }
    public int insertCourse (Lehrveranstaltung lehrveranstaltung) {
        if (lehrveranstaltung == null) { return -1; }

        int courseID = -1;

        try {
            String query = "insert into veranstaltung values (default, ?, ?, ?, ?) returning id;";
            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setString(1, lehrveranstaltung.getTitel());
            stmt.setString(2, Enums.Art.getString(lehrveranstaltung.getArt()));
            stmt.setInt(3, lehrveranstaltung.getSemester().getJahr());
            stmt.setString(4, Enums.SemesterTyp.getString(lehrveranstaltung.getSemester().getSemesterTyp()));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                courseID = rs.getInt(1);
                lehrveranstaltung.setVeranstaltungsID(courseID);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getSQLState().equals("23505") && e.getMessage().contains("veranstaltung_name_key")) {
                return -2;
            }
        }
        return courseID;
    }
    public void createKategorien (Lehrveranstaltung lehrveranstaltung) throws SQLException {
        String query = "insert into veranstaltungskategorie values (default, ?, ?, ?);";
        PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

        List<LVKategorie> kategorien = lehrveranstaltung.getKategorien();
        for (int i = 0; i < kategorien.size(); i++) {
            stmt.setString(1, kategorien.get(i).getName());
            stmt.setInt(2, lehrveranstaltung.getVeranstaltungsID());
            stmt.setInt(3, i);
            stmt.addBatch();
        }
        stmt.executeBatch();
        ResultSet rs = stmt.getGeneratedKeys();

        for (int i = 0; i < kategorien.size() && rs.next(); i++) {
            kategorien.get(i).setID(rs.getInt(1));
            //System.out.println(rs.getInt(1));
        }
    }

    public static Lehrveranstaltung extractVeranstaltung (HttpServletRequest request) throws ServletException, IOException {
        Part part = request.getPart("CourseJson");
        String partType = part.getContentType();

        if (!partType.equals("application/json")) { return null; }

        InputStream inputStream = part.getInputStream();
        String Json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

        Lehrveranstaltung lehrveranstaltung = null;

        try {
            lehrveranstaltung = GsonUtility.getGson().fromJson(Json, Lehrveranstaltung.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lehrveranstaltung;
    }
    public static UseridMitToken extractIdWithToken (HttpServletRequest request) throws ServletException, IOException {
        Part part = request.getPart("UserWithTokenJson");
        String partType = part.getContentType();

        if (!partType.equals("application/json")) { return null; }

        InputStream inputStream = part.getInputStream();
        String Json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

        UseridMitToken useridMitToken = new Gson().fromJson(Json, UseridMitToken.class);

        return useridMitToken;
    }

    public int createDateiElement (HttpServletRequest request, int veranstaltungsid, int kategorieid, int kategorieposition,
                                   int elementposition, String filesDatabasePath) {

        int elementid = createKategorieElement(elementposition, kategorieid);
        if (elementid == -1) { return -1; }

        String path = filesDatabasePath + "veranstaltungen/" + veranstaltungsid + "/" + kategorieid + "/";
        String[] fileReturn = new String[0];
        try {
            fileReturn = copyFile(request, kategorieposition, elementposition, path);
        } catch (ServletException | IOException e) {
            e.printStackTrace();
        }

        if (fileReturn.length == 3) {

            try {
                int dateiid = createDatei(fileReturn[0], fileReturn[1], fileReturn[2]);
                int dateiZuElementId = createDateiZuElement(dateiid, elementid);
                return dateiid;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return -1;
    }
}
