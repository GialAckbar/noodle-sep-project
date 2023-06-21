package server.handler;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import server.db.DatabaseManager;
import server.db.DatabaseUtility;
import server.db.FileUtility;
import shared.*;
import shared.quiz.Quiz;
import shared.quiz.QuizAntwort;
import shared.quiz.Quizfrage;
import shared.utility.ElementMitKategorie;
import shared.utility.ElementMitKategorieId;
import shared.utility.ZweiElementeMitKategorieId;
import shared.utility.ZweiKategorien;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateKursHandler extends AbstractHandler {
    Connection con = null;

    public static final String MULTIPART_FORMDATA_TYPE = "multipart/form-data";

    private static final MultipartConfigElement MULTI_PART_CONFIG = new MultipartConfigElement(System.getProperty("java.io.tmpdir"));

    public static boolean isMultipartRequest(ServletRequest request) {
        return request.getContentType() != null && request.getContentType().startsWith(MULTIPART_FORMDATA_TYPE);
    }
    public static void enableMultipartSupport(HttpServletRequest request) {
        request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, MULTI_PART_CONFIG);
    }

    public UpdateKursHandler () {
        try {
            DatabaseManager db = new DatabaseManager();
            con = db.connect();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    List<ElementMitKategorieId> elementsToDelete = new ArrayList<ElementMitKategorieId>();
    List<ElementMitKategorie> elementsToAdd = new ArrayList<ElementMitKategorie>();
    List<ZweiElementeMitKategorieId> elementsToUpdate = new ArrayList<ZweiElementeMitKategorieId>();
    List<LVKategorie> categoriesToDelete = new ArrayList<LVKategorie>();
    List<LVKategorie> categoriesToAdd = new ArrayList<LVKategorie>();
    List<ZweiKategorien> categoriesToUpdate = new ArrayList<ZweiKategorien>();
    List<Integer> fileIdsToDelete = new ArrayList<Integer>();
    List<String> filePathsToDelete = new ArrayList<String>();

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");

        if (baseRequest.getMethod().equals("POST") && splitTarget.length == 2 && splitTarget[1].equals("editCourse")) {

            baseRequest.setHandled(true);

            con = DatabaseUtility.getValidConnection(con);
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            if (HttpMethod.POST.is(request.getMethod()) && isMultipartRequest(request)) {
                enableMultipartSupport(request);
            }

            if (!DatabaseUtility.isLoggedIn(con, AddVeranstaltungHandler.extractIdWithToken(request))) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            initLists();

            try {
                con.setAutoCommit(false);
                Lehrveranstaltung updated = AddVeranstaltungHandler.extractVeranstaltung(request);
                Lehrveranstaltung current = KursAnzeigenHandler.createCompleteLehrveranstaltung(con, updated.getVeranstaltungsID());

                updateBasicInformation(current, updated);
                updateCategories(current, updated);

                System.out.println("elementstodelete: " + elementsToDelete.size()+ "elementstoadd: " + elementsToAdd.size() + "elementstoupdate: " + elementsToUpdate.size());

                deleteAllCategories();
                addAllCategories(updated);
                updateAllCategories();

                deleteAllElements();
                addAllElements(updated, request, con, elementsToAdd);
                updateAllElements();


                con.commit();
                con.setAutoCommit(true);

                FileUtility.deleteFilesFromFileDatabase(filePathsToDelete);

                response.setStatus(HttpServletResponse.SC_OK);
            } catch (Exception e) {
                e.printStackTrace();
                if (e instanceof SQLException && ((SQLException) e).getSQLState().equals("23505") && ((SQLException) e).getMessage().contains("veranstaltung_name_key")) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    System.out.println("SC_CONFLICT");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                try {
                    con.rollback();
                    con.setAutoCommit(true);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }
    protected boolean updateBasicInformation(Lehrveranstaltung current, Lehrveranstaltung updated) throws Exception {
        boolean difference = false;

        if (current.getTitel() != updated.getTitel()) {
            difference = true;
        } else if (current.getSemester().getJahr() != updated.getSemester().getJahr() || current.getSemester().getSemesterTyp() != updated.getSemester().getSemesterTyp()) {
            difference = true;
        } else if (current.getArt() != updated.getArt()) {
            difference = true;
        }
        if (difference) {
            String query = "update veranstaltung set name = ?, typ = ?, jahr = ?, semester = ? where id = ?;";
            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setString(1, updated.getTitel());
            stmt.setString(2, Enums.Art.getString(updated.getArt()));
            stmt.setInt(3, updated.getSemester().getJahr());
            stmt.setString(4, Enums.SemesterTyp.getString(updated.getSemester().getSemesterTyp()));
            stmt.setInt(5, updated.getVeranstaltungsID());

            stmt.executeUpdate();
        }
        return difference;
    }
    protected void updateCategories (Lehrveranstaltung current, Lehrveranstaltung updated) {
        // Alte Kategorien löschen und weiterhin bestehende updaten
        for (int i = 0; i < current.getKategorien().size(); i++) {
            LVKategorie updatedCategory = findCategoryWithId(updated, current.getKategorien().get(i).getID());
            if (updatedCategory != null) {
                updateCategory(current.getKategorien().get(i), updatedCategory);
            } else {
                deleteCategory(current.getKategorien().get(i));
            }
        }
        // Neue Kategorien hinzufügen
        for (int i = 0; i < updated.getKategorien().size(); i++) {
            if (findCategoryWithId(current, updated.getKategorien().get(i).getID()) == null) {
                insertCategory(updated.getKategorien().get(i));
            }
        }
    }

    protected void updateCategory(LVKategorie current, LVKategorie updated) {
        System.out.println("Current id: " + current.getID() + " updated id: " + updated.getID());
        for (int i = 0; i < current.getKategorieElemente().size(); i++) {
            LVKategorieElement updatedElement = findElementWithID(updated, current.getKategorieElemente().get(i).getId());
            if (updatedElement != null) {
                elementsToUpdate.add(new ZweiElementeMitKategorieId(current.getKategorieElemente().get(i), updatedElement, current.getID()));
            } else {
                elementsToDelete.add(new ElementMitKategorieId(current.getKategorieElemente().get(i), current.getID()));
            }
        }
        for (int i = 0; i < updated.getKategorieElemente().size(); i++) {
            if (findElementWithID(current, updated.getKategorieElemente().get(i).getId()) == null) {
                elementsToAdd.add(new ElementMitKategorie(updated.getKategorieElemente().get(i), updated));
            }
        }
        categoriesToUpdate.add(new ZweiKategorien(current, updated));
    }
    protected void deleteCategory(LVKategorie kategorie) {
        for(int i = 0; i < kategorie.getKategorieElemente().size(); i++) {
            elementsToDelete.add(new ElementMitKategorieId(kategorie.getKategorieElemente().get(i), kategorie.getID()));
        }
        categoriesToDelete.add(kategorie);
    }
    protected void insertCategory(LVKategorie kategorie) {
        for(int i = 0; i < kategorie.getKategorieElemente().size(); i++) {
            elementsToAdd.add(new ElementMitKategorie(kategorie.getKategorieElemente().get(i), kategorie));
        }
        categoriesToAdd.add(kategorie);
    }
    protected static LVKategorie findCategoryWithId(Lehrveranstaltung lehrveranstaltung, int id) {
        for (int i = 0; i < lehrveranstaltung.getKategorien().size(); i++) {
            if (lehrveranstaltung.getKategorien().get(i).getID() == id) {
                return lehrveranstaltung.getKategorien().get(i);
            }
        }
        return null;
    }
    protected static LVKategorieElement findElementWithID(LVKategorie kategorie, int id) {
        for(int i = 0; i < kategorie.getKategorieElemente().size(); i++) {
            if (kategorie.getKategorieElemente().get(i).getId() == id) {
                return kategorie.getKategorieElemente().get(i);
            }
        }
        return null;
    }

    protected void deleteAllElements() throws SQLException {
        String deleteElementQuery = "delete from kategorieelement where id = ?;";
        PreparedStatement deleteElementStatement = con.prepareStatement(deleteElementQuery);

        String deleteFileQuery = "delete from datei where id = ?;";
        PreparedStatement deleteFileStatement = con.prepareStatement(deleteFileQuery);

        String deleteFileToCategoryQuery = "delete from dateizukategorie where dateiid = ? and kategorieelementid = ?;";
        PreparedStatement deleteFileToCategoryStatement = con.prepareStatement(deleteFileToCategoryQuery);

        String deleteTodoQuery = "delete from todo where id = ?;";
        PreparedStatement deleteTodoStatement = con.prepareStatement(deleteTodoQuery);

        String deleteTodoCategoryQuery = "with kategorieids as (delete from todozukategorie where todoid = ? returning kategorieelementid as id) " +
                "delete from kategorieelement where id = (select id from kategorieids);";
        PreparedStatement deleteTodoCategoryStatement = con.prepareStatement(deleteTodoCategoryQuery);

        String deleteFalscheantwortenQuery = "delete from abgabeantworten where abgabeid in (select id from quizabgabe where quizid = ?);";
        PreparedStatement deleteFalscheantwortenStatement = con.prepareStatement(deleteFalscheantwortenQuery);

        String deleteQuizabgabeQuery = "delete from quizabgabe where quizid = ?;";
        PreparedStatement deleteQuizabgabeStatement = con.prepareStatement(deleteQuizabgabeQuery);

        String deleteAntwortenQuery = "delete from antworten where fragenid in (select id from fragen where quizid = ?);";
        PreparedStatement deleteAntwortenStatement = con.prepareStatement(deleteAntwortenQuery);

        String deleteFragenQuery = "delete from fragen where quizid = ?;";
        PreparedStatement deleteFragenStatement = con.prepareStatement(deleteFragenQuery);

        String deleteQuizQuery = "delete from quiz where id = ?;";
        PreparedStatement deleteQuizStatement = con.prepareStatement(deleteQuizQuery);

        String deleteQuizCategoryQuery = "with kategorieids as (delete from quizzukategorie where quizid = ? returning kategorieelementid as id) " +
                "delete from kategorieelement where id = (select id from kategorieids);";
        PreparedStatement deleteQuizCategoryStatement = con.prepareStatement(deleteQuizCategoryQuery);

        String deleteReminderQuery = "delete from termin where id = ?;";
        PreparedStatement deleteReminderStatement = con.prepareStatement(deleteReminderQuery);

        String deleteReminderCategoryQuery = "with kategorieids as (delete from terminzukategorie where terminid = ? returning kategorieelementid as id) " +
                "delete from kategorieelement where id = (select id from kategorieids);";
        PreparedStatement deleteReminderCategoryStatement = con.prepareStatement(deleteReminderCategoryQuery);

        String deleteLernkarteQuery = "delete from lernkarte where id = ?;";
        PreparedStatement deleteLernkarteStatement = con.prepareStatement(deleteLernkarteQuery);

        String deleteLernkarteCategoryQuery = "with kategorieids as (delete from lernkartezukategorie where lernkarteid = ? returning kategorieelementid as id) " +
                "delete from kategorieelement where id = (select id from kategorieids);";
        PreparedStatement deleteLernkarteCategoryStatement = con.prepareStatement(deleteLernkarteCategoryQuery);

        String deleteFb_abgabeantwortenQuery = "delete from fb_abgabeantworten where abgabeid in (select id from feedbackabgabe where feedbackid = ?);";
        PreparedStatement deleteFb_abgabeantwortenStatement = con.prepareStatement(deleteFb_abgabeantwortenQuery);

        String deleteFeedbackabgabeQuery = "delete from feedbackabgabe where feedbackid = ?;";
        PreparedStatement deleteFeedbackabgabeStatement = con.prepareStatement(deleteFeedbackabgabeQuery);

        String deleteFb_antwortenQuery = "delete from fb_antworten where fb_fragenid in (select id from fb_fragen where fb_id = ?);";
        PreparedStatement deleteFb_antwortenStatement = con.prepareStatement(deleteFb_antwortenQuery);

        String deleteFb_fragenQuery = "delete from fb_fragen where fb_id = ?;";
        PreparedStatement deleteFb_fragenStatement = con.prepareStatement(deleteFb_fragenQuery);

        String deleteBewertungQuery = "delete from feedback where id = ?;";
        PreparedStatement deleteBewertungStatement = con.prepareStatement(deleteBewertungQuery);

        String deleteBewertungCategoryQuery = "with kategorieids as (delete from feedbackzukategorie where feedbackid = ? returning kategorieelementid as id) " +
                "delete from kategorieelement where id = (select id from kategorieids);";
        PreparedStatement deleteBewertungCategoryStatement = con.prepareStatement(deleteBewertungCategoryQuery);

        for (int i = 0 ; i < elementsToDelete.size(); i++) {
            LVKategorieElement element = elementsToDelete.get(i).getElement();



            if(element instanceof KategorieDatei) {
                deleteElementStatement.setInt(1, element.getId());
                deleteElementStatement.addBatch();

                KategorieDatei datei = (KategorieDatei) element;
                deleteFileStatement.setInt(1, datei.getDateiid());
                deleteFileStatement.addBatch();
                fileIdsToDelete.add(datei.getDateiid());

                deleteFileToCategoryStatement.setInt(1, datei.getDateiid());
                deleteFileToCategoryStatement.setInt(2, element.getId());
                deleteFileToCategoryStatement.addBatch();
            } else if (element instanceof Quiz) {
                Quiz quiz = (Quiz) element;
                deleteFalscheantwortenStatement.setInt(1, quiz.getId());
                deleteFalscheantwortenStatement.addBatch();

                deleteQuizabgabeStatement.setInt(1, quiz.getId());
                deleteQuizabgabeStatement.addBatch();

                deleteAntwortenStatement.setInt(1, quiz.getId());
                deleteAntwortenStatement.addBatch();

                deleteFragenStatement.setInt(1, quiz.getId());
                deleteFragenStatement.addBatch();

                deleteQuizStatement.setInt(1, quiz.getId());
                deleteQuizStatement.addBatch();

                deleteQuizCategoryStatement.setInt(1, quiz.getId());
                deleteQuizCategoryStatement.addBatch();
            } else if (element instanceof Todo) {
                Todo todo = (Todo) element;
                deleteTodoStatement.setInt(1, todo.getId());
                deleteTodoStatement.addBatch();

                deleteTodoCategoryStatement.setInt(1, todo.getId());
                deleteTodoCategoryStatement.addBatch();
            } else if (element instanceof Reminder) {
                Reminder reminder = (Reminder) element;
                deleteReminderStatement.setInt(1, reminder.getId());
                deleteReminderStatement.addBatch();

                deleteReminderCategoryStatement.setInt(1, reminder.getId());
                deleteReminderCategoryStatement.addBatch();
            } else if (element instanceof Lernkarte) {
                Lernkarte lernkarte = (Lernkarte) element;

                deleteLernkarteStatement.setInt(1, lernkarte.getId());
                deleteLernkarteStatement.addBatch();

                deleteLernkarteCategoryStatement.setInt(1, lernkarte.getId());
                deleteLernkarteCategoryStatement.addBatch();
            } else if (element instanceof shared.Bewertung) {
                shared.Bewertung bewertung = (shared.Bewertung) element;
                deleteFb_abgabeantwortenStatement.setInt(1, bewertung.getId());
                deleteFb_abgabeantwortenStatement.addBatch();

                deleteFeedbackabgabeStatement.setInt(1, bewertung.getId());
                deleteFeedbackabgabeStatement.addBatch();

                deleteFb_antwortenStatement.setInt(1, bewertung.getId());
                deleteFb_antwortenStatement.addBatch();

                deleteFb_fragenStatement.setInt(1, bewertung.getId());
                deleteFb_fragenStatement.addBatch();

                deleteBewertungStatement.setInt(1, bewertung.getId());
                deleteBewertungStatement.addBatch();

                deleteBewertungCategoryStatement.setInt(1, bewertung.getId());
                deleteBewertungCategoryStatement.addBatch();
            }


        }
        FileUtility.copyPathsFromFileIds(con, fileIdsToDelete, filePathsToDelete);
        deleteFileToCategoryStatement.executeBatch();

        deleteElementStatement.executeBatch();
        deleteFileStatement.executeBatch();

        deleteFalscheantwortenStatement.executeBatch();
        deleteQuizabgabeStatement.executeBatch();
        deleteAntwortenStatement.executeBatch();
        deleteFragenStatement.executeBatch();
        deleteQuizCategoryStatement.executeBatch();
        deleteQuizStatement.executeBatch();

        deleteTodoStatement.executeBatch();
        deleteTodoCategoryStatement.executeBatch();

        deleteReminderCategoryStatement.executeBatch();
        deleteReminderStatement.executeBatch();

        deleteLernkarteCategoryStatement.executeBatch();
        deleteLernkarteStatement.executeBatch();

        deleteFb_abgabeantwortenStatement.executeBatch();
        deleteFeedbackabgabeStatement.executeBatch();
        deleteFb_antwortenStatement.executeBatch();
        deleteFb_fragenStatement.executeBatch();
        deleteBewertungCategoryStatement.executeBatch();
        deleteBewertungStatement.executeBatch();

    }
    public static void addAllElements(Lehrveranstaltung updated, HttpServletRequest request, Connection con, List<ElementMitKategorie> elementsToAdd) throws SQLException, ServletException, IOException, NullPointerException{

        String addFileQuery = "with dateiids as(insert into datei values (default, ?, ?, ?) returning id), " +
                "kategorieids as(insert into kategorieelement values (default, ?, ?) returning id) " +
                "insert into dateizukategorie values ((select id from dateiids), (select id from kategorieids), default);";
        PreparedStatement addFileStatement = con.prepareStatement(addFileQuery);

        String addMeetingQuery = "with terminids as(insert into termin values (default, ?, ?) returning id), " +
                "kategorieids as(insert into kategorieelement values (default, ?, ?) returning id) " +
                "insert into terminzukategorie values (default, (select id from kategorieids), (select id from terminids));";
        PreparedStatement addMeetingStatement = con.prepareStatement(addMeetingQuery);

        String addMeetingAndReminderQuery = "with terminids as(insert into termin values (default, ?, ?) returning id), " +
                "kategorieids as(insert into kategorieelement values (default, ?, ?) returning id), " +
                "reminderids as (insert into erinnerung values (default, (select id from terminids),?, ?) returning id) " +
                "insert into terminzukategorie values (default, (select id from kategorieids), (select id from terminids));";
        PreparedStatement addMeetingAndReminderStatement = con.prepareStatement(addMeetingAndReminderQuery);

        String addTodoQuery = "with todoids as(insert into todo values (default, ?, ?) returning id), " +
                "kategorieids as(insert into kategorieelement values (default, ?, ?) returning id) " +
                "insert into todozukategorie values (default, (select id from kategorieids), (select id from todoids)) returning id;";
        PreparedStatement addTodoStatement = con.prepareStatement(addTodoQuery, Statement.RETURN_GENERATED_KEYS);

        String addTodoMemberQuery = "insert into todoteilnehmer values (default, (select todoid from todozukategorie where id = ?), ?);";
        PreparedStatement addTodoMemberStatement = con.prepareStatement(addTodoMemberQuery);

        String addQuizQuery = "insert into quiz values (default, ?, ?, ?);";

        addQuizQuery = "with quizids as (insert into quiz values (default, ?, null, null) returning id), " +
                "kategorieids as(insert into kategorieelement values (default, ?, ?) returning id) " +
                "insert into quizzukategorie values (default, (select id from kategorieids), (select id from quizids));";
        PreparedStatement addQuizStatement = con.prepareStatement(addQuizQuery, Statement.RETURN_GENERATED_KEYS);

        String addQuestionQuery = "insert into fragen values (default, (select quizid from quizzukategorie where id = ?), ?);";
        PreparedStatement addQuestionStatement = con.prepareStatement(addQuestionQuery, Statement.RETURN_GENERATED_KEYS);

        String addAnswerQuery = "insert into antworten values (default, ?, ?, ?);";
        PreparedStatement addAnswerStatement = con.prepareStatement(addAnswerQuery, Statement.RETURN_GENERATED_KEYS);

        String addLernkartenQuery = "with lernkartenids as(insert into lernkarte values (default, ?, ?) returning id), " +
                "kategorieids as(insert into kategorieelement values (default, ?, ?) returning id) " +
                "insert into lernkartezukategorie values (default, (select id from kategorieids), (select id from lernkartenids));";
        PreparedStatement addLernkartenStatement = con.prepareStatement(addLernkartenQuery);

        String addBewertungQuery = "with bewertungsids as(insert into feedback values (default, ?) returning id), " +
                "kategorieids as(insert into kategorieelement values (default, ?, ?) returning id) " +
                "insert into feedbackzukategorie values (default, (select id from kategorieids), (select id from bewertungsids));";
        PreparedStatement addBewertungStatement = con.prepareStatement(addBewertungQuery, Statement.RETURN_GENERATED_KEYS);

        String addBewertungQuestionQuery = "insert into fb_fragen values (default, (select feedbackid from feedbackzukategorie where id = ?), ?);";
        PreparedStatement addBewertungQuestionStatement = con.prepareStatement(addBewertungQuestionQuery, Statement.RETURN_GENERATED_KEYS);

        String addBewertungAnswerQuery = "insert into fb_antworten values (default, ?, ?);";
        PreparedStatement addBewertungAnswerStatement = con.prepareStatement(addBewertungAnswerQuery, Statement.RETURN_GENERATED_KEYS);

        int numberQuiz = 0;

        for(int i = 0; i < elementsToAdd.size(); i++) {
            LVKategorieElement element = elementsToAdd.get(i).getElement();
            //LVKategorie kategorie = findCategoryWithId(updated, elementsToAdd.get(i).getKategorieid());
            LVKategorie kategorie = elementsToAdd.get(i).getKategorie();

            if (element.getClass() == LVKategorieElement.class) {
                continue;
            }
            if (element instanceof KategorieDatei) {
                System.out.println(updated.getVeranstaltungsID());
                System.out.println(kategorie.getID());
                String path = "./../DateienDatenbank/veranstaltungen/" + updated.getVeranstaltungsID() + "/" + kategorie.getID() + "/";
                System.out.println("kategoriePosition: " + kategorie.getPosition() + " elementPosition: " + element.getPosition());
                String[] fileData = AddVeranstaltungHandler.copyFile(request, kategorie.getPosition(), element.getPosition(), path);
                if (fileData.length != 3) {
                    throw new IOException();
                }
                addFileStatement.setString(1, path);
                addFileStatement.setString(2, fileData[1]);
                addFileStatement.setString(3, fileData[2]);
                addFileStatement.setInt(4, element.getPosition());
                addFileStatement.setInt(5, kategorie.getID());
                addFileStatement.addBatch();
            }
            if (element instanceof Reminder) {
                Reminder reminder = (Reminder) element;
                System.out.println(reminder.getShouldRemind());
                if (reminder.getShouldRemind()) {
                    System.out.println(Timestamp.from(reminder.getRemindDate().withZoneSameInstant(ZoneId.of("UTC")).toInstant()));
                    addMeetingAndReminderStatement.setObject(1, Timestamp.from(reminder.getEventDate().withZoneSameInstant(ZoneId.of("UTC")).toInstant()));
                    addMeetingAndReminderStatement.setString(2, reminder.getAnzeigename());
                    addMeetingAndReminderStatement.setInt(3, reminder.getPosition());
                    addMeetingAndReminderStatement.setInt(4, kategorie.getID());
                    addMeetingAndReminderStatement.setObject(5, Timestamp.from(reminder.getRemindDate().withZoneSameInstant(ZoneId.of("UTC")).toInstant()));
                    addMeetingAndReminderStatement.setString(6, Enums.ReminderType.getString(reminder.getMessageType()));
                    addMeetingAndReminderStatement.addBatch();
                } else {
                    addMeetingStatement.setObject(1, Timestamp.from(reminder.getEventDate().withZoneSameInstant(ZoneId.of("UTC")).toInstant()));
                    addMeetingStatement.setString(2, reminder.getAnzeigename());
                    addMeetingStatement.setInt(3, reminder.getPosition());
                    addMeetingStatement.setInt(4, kategorie.getID());
                    addMeetingStatement.addBatch();
                }

            }
            if (element instanceof Todo) {
                Todo todo = (Todo) element;
                System.out.println("TODO: set kategorieid: " + kategorie.getID());
                addTodoStatement.setString(1, todo.getAnzeigename());
                addTodoStatement.setBoolean(2, todo.getIsFinished());
                addTodoStatement.setInt(3, element.getPosition());
                addTodoStatement.setInt(4, kategorie.getID());

                addTodoStatement.addBatch();
            }
            if (element instanceof Quiz) {
                Quiz quiz = (Quiz) element;

                addQuizStatement.setString(1, quiz.getAnzeigename());
                addQuizStatement.setInt(2, element.getPosition());
                addQuizStatement.setInt(3, kategorie.getID());

                addQuizStatement.addBatch();
            }
            if (element instanceof Lernkarte) {
                Lernkarte lernkarte = (Lernkarte) element;

                addLernkartenStatement.setString(1, lernkarte.getAnzeigename());
                addLernkartenStatement.setString(2, lernkarte.getAntwort());
                addLernkartenStatement.setInt(3, element.getPosition());
                addLernkartenStatement.setInt(4, kategorie.getID());
                addLernkartenStatement.addBatch();
            }
            if (element instanceof shared.Bewertung) {
                shared.Bewertung bewertung = (shared.Bewertung) element;

                addBewertungStatement.setString(1, bewertung.getAnzeigename());
                addBewertungStatement.setInt(2, element.getPosition());
                addBewertungStatement.setInt(3, kategorie.getID());

                addBewertungStatement.addBatch();
            }
        }

        addFileStatement.executeBatch();
        addMeetingStatement.executeBatch();
        addMeetingAndReminderStatement.executeBatch();
        addTodoStatement.executeBatch();
        addQuizStatement.executeBatch();
        addLernkartenStatement.executeBatch();
        addBewertungStatement.executeBatch();


        //Todo Member
        ResultSet todoKeys = addTodoStatement.getGeneratedKeys();
        for(int i = 0; i < elementsToAdd.size(); i++) {
            LVKategorieElement element = elementsToAdd.get(i).getElement();

            if (element instanceof Todo) {
                Todo todo = (Todo) element;
                if (todoKeys.next()) {
                    System.out.println("Anzahl Verantwortliche: " + todo.getVerantwortliche().size());
                    for (int j = 0; j < todo.getVerantwortliche().size(); j++) {
                        addTodoMemberStatement.setInt(1, todoKeys.getInt(1));
                        addTodoMemberStatement.setInt(2, todo.getVerantwortliche().get(j).getID());
                        addTodoMemberStatement.addBatch();
                    }
                } else {
                    throw new SQLException("ResultSet doesn't match number of Todo-elements");
                }
            }
        }
        addTodoMemberStatement.executeBatch();

        //Quiz Fragen
        ResultSet quizKeys = addQuizStatement.getGeneratedKeys();
        for(int i = 0; i < elementsToAdd.size(); i++) {
            LVKategorieElement element = elementsToAdd.get(i).getElement();

            if (element instanceof Quiz) {
                Quiz quiz = (Quiz) element;

                if (quizKeys.next()) {
                    for (int j = 0; j < quiz.getFragen().size(); j++) {
                        Quizfrage tempFrage = quiz.getFragen().get(j);

                        addQuestionStatement.setInt(1, quizKeys.getInt(1));
                        addQuestionStatement.setString(2, tempFrage.getFrage());

                        addQuestionStatement.addBatch();
                    }
                } else {
                    System.out.println("i is: " + i);
                    throw new SQLException("ResultSet doesn't match number of Quiz-elements");
                }
            }
        }
        addQuestionStatement.executeBatch();

        //Quiz Antworten
        ResultSet questionKeys = addQuestionStatement.getGeneratedKeys();
        for (int i = 0; i < elementsToAdd.size(); i++) {
            LVKategorieElement element = elementsToAdd.get(i).getElement();

            if (element instanceof Quiz) {
                Quiz quiz = (Quiz) element;

                for (int j = 0; j < quiz.getFragen().size(); j++) {
                    Quizfrage frage = quiz.getFragen().get(j);
                    if (questionKeys.next()) {
                        for (int k = 0; k < frage.getAntworten().size(); k++) {
                            QuizAntwort antwort = frage.getAntworten().get(k);
                            addAnswerStatement.setInt(1, questionKeys.getInt(1));
                            addAnswerStatement.setString(2, antwort.getAntwort());
                            addAnswerStatement.setBoolean(3, antwort.getRichtig());

                            addAnswerStatement.addBatch();
                        }
                    } else {
                        throw new SQLException("ResultSet doesn't match number of Question-elements");
                    }
                }
            }
        }
        addAnswerStatement.executeBatch();

        //Bewertung Fragen
        ResultSet bewertungKeys = addBewertungStatement.getGeneratedKeys();
        for(int i = 0; i < elementsToAdd.size(); i++) {
            LVKategorieElement element = elementsToAdd.get(i).getElement();

            if (element instanceof shared.Bewertung) {
                shared.Bewertung bewertung = (shared.Bewertung) element;

                if (bewertungKeys.next()) {
                    for (int j = 0; j < bewertung.getFragen().size(); j++) {
                        Quizfrage tempFrage = bewertung.getFragen().get(j);

                        addBewertungQuestionStatement.setInt(1, bewertungKeys.getInt(1));
                        addBewertungQuestionStatement.setString(2, tempFrage.getFrage());

                        addBewertungQuestionStatement.addBatch();
                    }
                } else {
                    System.out.println("i is: " + i);
                    throw new SQLException("ResultSet doesn't match number of Bewertung-elements");
                }
            }
        }
        addBewertungQuestionStatement.executeBatch();

        //Bewertung Antworten
        ResultSet questionKeysBewertung = addBewertungQuestionStatement.getGeneratedKeys();
        for (int i = 0; i < elementsToAdd.size(); i++) {
            LVKategorieElement element = elementsToAdd.get(i).getElement();

            if (element instanceof shared.Bewertung) {
                shared.Bewertung bewertung = (shared.Bewertung) element;

                for (int j = 0; j < bewertung.getFragen().size(); j++) {
                    Quizfrage frage = bewertung.getFragen().get(j);
                    if (questionKeysBewertung.next()) {
                        for (int k = 0; k < frage.getAntworten().size(); k++) {
                            QuizAntwort antwort = frage.getAntworten().get(k);
                            addBewertungAnswerStatement.setInt(1, questionKeysBewertung.getInt(1));
                            addBewertungAnswerStatement.setString(2, antwort.getAntwort());

                            addBewertungAnswerStatement.addBatch();
                        }
                    } else {
                        throw new SQLException("ResultSet doesn't match number of Bewertung-elements");
                    }
                }
            }
        }
        addBewertungAnswerStatement.executeBatch();
    }
    protected void updateAllElements() throws SQLException {
        String fileQuery = "update kategorieelement set position = ? where id = ?;";
        PreparedStatement fileStatement = con.prepareStatement(fileQuery);

        String reminderQuery = "with ids as (select ke.id from kategorieelement ke " +
                "inner join terminzukategorie tzk on tzk.kategorieelementid = ke.id " +
                "inner join termin t on t.id = tzk.terminid where t.id = ?) " +
                "update kategorieelement set position = ? where id = (select id from ids);";
        PreparedStatement reminderStatement = con.prepareStatement(reminderQuery);

        String todoQuery = "with ids as (select ke.id from kategorieelement ke " +
                "inner join todozukategorie tzk on tzk.kategorieelementid = ke.id " +
                "inner join todo t on t.id = tzk.todoid where t.id = ?) " +
                "update kategorieelement set position = ? where id = (select id from ids);";
        PreparedStatement todoStatement = con.prepareStatement(todoQuery);

        String quizQuery = "with ids as (select ke.id from kategorieelement ke " +
                "inner join quizzukategorie qzk on qzk.kategorieelementid = ke.id " +
                "inner join quiz q on q.id = qzk.quizid where q.id = ?) " +
                "update kategorieelement set position = ? where id = (select id from ids);";
        PreparedStatement quizStatement = con.prepareStatement(quizQuery);

        String lernkartenQuery = "with ids as (select ke.id from kategorieelement ke " +
                "inner join lernkartezukategorie lzk on lzk.kategorieelementid = ke.id " +
                "inner join lernkarte l on l.id = lzk.lernkarteid where l.id = ?) " +
                "update kategorieelement set position = ? where id = (select id from ids);";
        PreparedStatement lernkartenStatement = con.prepareStatement(lernkartenQuery);

        String bewertungQuery = "with ids as (select ke.id from kategorieelement ke " +
                "inner join feedbackzukategorie fzk on fzk.kategorieelementid = ke.id " +
                "inner join feedback f on f.id = fzk.feedbackid where f.id = ?) " +
                "update kategorieelement set position = ? where id = (select id from ids);";
        PreparedStatement bewertungStatement = con.prepareStatement(bewertungQuery);

        for (int i = 0; i < elementsToUpdate.size(); i++) {
            LVKategorieElement current = elementsToUpdate.get(i).getFirst();
            LVKategorieElement updated = elementsToUpdate.get(i).getSecond();

            System.out.println("ID: " + updated.getId() + " CurrentPos: " + current.getPosition() + " UpdatedPos: " + updated.getPosition());

            if (current.getPosition() != updated.getPosition()) {
                if (current instanceof KategorieDatei) {
                    fileStatement.setInt(1, updated.getPosition());
                    fileStatement.setInt(2, updated.getId());
                    fileStatement.addBatch();
                } else if (current instanceof Reminder) {
                    reminderStatement.setInt(1, updated.getId());
                    reminderStatement.setInt(2, updated.getPosition());
                    reminderStatement.addBatch();
                } else if (current instanceof Todo) {
                    todoStatement.setInt(1, updated.getId());
                    todoStatement.setInt(2, updated.getPosition());
                    todoStatement.addBatch();
                } else if (current instanceof Quiz) {
                    quizStatement.setInt(1, updated.getId());
                    quizStatement.setInt(2, updated.getPosition());
                    quizStatement.addBatch();
                } else if (current instanceof Lernkarte) {
                    lernkartenStatement.setInt(1, current.getId());
                    lernkartenStatement.setInt(2, current.getPosition());
                    lernkartenStatement.addBatch();
                } else if (current instanceof shared.Bewertung) {
                    bewertungStatement.setInt(1, updated.getId());
                    bewertungStatement.setInt(2, updated.getPosition());
                    bewertungStatement.addBatch();
                }

            }
        }
        fileStatement.executeBatch();
        reminderStatement.executeBatch();
        todoStatement.executeBatch();
        quizStatement.executeBatch();
        lernkartenStatement.executeBatch();
        bewertungStatement.executeBatch();

        for (int i = 0; i < elementsToUpdate.size(); i++) {
            LVKategorieElement current = elementsToUpdate.get(i).getFirst();
            LVKategorieElement updated = elementsToUpdate.get(i).getSecond();
            System.out.println("ID: " + updated.getId() + " CurrentPos: " + current.getPosition() + " UpdatedPos: " + updated.getPosition());

        }
    }
    protected void deleteAllCategories() throws SQLException {
        String query = "delete from veranstaltungskategorie where id = ?;";
        PreparedStatement stmt = con.prepareStatement(query);

        for(int i = 0; i < categoriesToDelete.size(); i++) {
            stmt.setInt(1, categoriesToDelete.get(i).getID());
            stmt.addBatch();
        }
        stmt.executeBatch();
    }
    protected void addAllCategories(Lehrveranstaltung lehrveranstaltung) throws SQLException {
        String query = "insert into veranstaltungskategorie values (default, ?,?,?);";
        PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

        for (int i = 0; i < categoriesToAdd.size(); i++) {
            LVKategorie kategorie = categoriesToAdd.get(i);
            stmt.setString(1, kategorie.getName());
            stmt.setInt(2, lehrveranstaltung.getVeranstaltungsID());
            stmt.setInt(3, kategorie.getPosition());
            stmt.addBatch();
        }
        stmt.executeBatch();
        ResultSet rs = stmt.getGeneratedKeys();
        List<LVKategorie> kategorien = lehrveranstaltung.getKategorien();
        List<LVKategorie> neueKategorien = new ArrayList<LVKategorie>();
        for (int i = 0; i < kategorien.size(); i++) {
            if(kategorien.get(i).getID() < 0) {
                neueKategorien.add(kategorien.get(i));
            }
        }

        for (int i = 0; i < neueKategorien.size() && rs.next(); i++) {
            neueKategorien.get(i).setID(rs.getInt(1));
            System.out.println(rs.getInt(1));
        }
    }
    protected void updateAllCategories() throws SQLException {
        String query = "update veranstaltungskategorie set name = ?, position = ? where id = ?;";
        PreparedStatement stmt = con.prepareStatement(query);

        for (int i = 0; i < categoriesToUpdate.size(); i++) {
            LVKategorie current = categoriesToUpdate.get(i).getFirst();
            LVKategorie updated = categoriesToUpdate.get(i).getSecond();

            if (current.getName() != updated.getName() || current.getPosition() != updated.getPosition()) {
                stmt.setString(1, updated.getName());
                stmt.setInt(2, updated.getPosition());
                stmt.setInt(3, updated.getID());
                stmt.addBatch();
            }
        }
        stmt.executeBatch();
    }
    protected void initLists() {
        elementsToDelete = new ArrayList<ElementMitKategorieId>();
        elementsToAdd = new ArrayList<ElementMitKategorie>();
        elementsToUpdate = new ArrayList<ZweiElementeMitKategorieId>();
        categoriesToDelete = new ArrayList<LVKategorie>();
        categoriesToAdd = new ArrayList<LVKategorie>();
        categoriesToUpdate = new ArrayList<ZweiKategorien>();
        fileIdsToDelete = new ArrayList<Integer>();
    }
}
