package server.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import server.ServerUtility;
import server.db.DatabaseUtility;
import shared.Lehrende;
import shared.Todo;
import shared.User;
import shared.accounts.UseridMitToken;
import shared.utility.GsonUtility;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UpdateElementsHandler extends AbstractHandler {
    Connection con = null;

    public UpdateElementsHandler (Connection con) {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");

        if (baseRequest.getMethod().equals("POST") && splitTarget.length == 3 && splitTarget[1].equals("todo") && splitTarget[2].equals("change")) {
            baseRequest.setHandled(true);

            con = DatabaseUtility.getValidConnection(con);
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            UseridMitToken useridMitToken = ServerUtility.extractUseridAndToken(request);
            if (!DatabaseUtility.isLoggedIn(con, useridMitToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Todo todo = null;
            try {
                todo = GsonUtility.getGson().fromJson(request.getReader(), Todo.class);
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            try {
                con.setAutoCommit(false);
                changeTodoStatus(todo);
                List<User> existingUsers = getTodoMembers(todo);
                List<User> newUsers = todo.getVerantwortliche();
                List<User> usersToDelete = new ArrayList<User>();
                List<User> usersToAdd = new ArrayList<User>();
                setUsersToUpdate(existingUsers, newUsers, usersToAdd, usersToDelete);
                updateUsers(todo, usersToAdd, usersToDelete);
                con.commit();
            } catch (Exception e) {
                try {
                    con.rollback();
                    con.setAutoCommit(true);
                } catch (SQLException f) {
                    f.printStackTrace();
                }
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
    }
    protected void changeTodoStatus (Todo todo) throws SQLException{
            String query = "update todo set fertig = ? where id = ?;";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setBoolean(1, todo.getIsFinished());
            stmt.setInt(2, todo.getId());
            stmt.executeUpdate();
    }
    protected List<User> getTodoMembers (Todo todo) throws SQLException {
        int todoid = todo.getId();

        String getQuery = "select n.id from nutzer n inner join todoteilnehmer tt on n.id = tt.nutzerid inner join todo t on t.id = tt.todoid where t.id = ?;";
        PreparedStatement getStatement = con.prepareStatement(getQuery);
        getStatement.setInt(1, todoid);
        ResultSet rs = getStatement.executeQuery();

        List<User> users = new ArrayList<User>();
        while(rs.next()) {
            User temp = new User(rs.getInt(1));
            users.add(temp);
        }

        return users;
    }
    protected void setUsersToUpdate (List<User> existingUsers, List<User> newUsers, List<User> usersToAdd, List<User> usersToRemove) {
        for (int i = 0; i < existingUsers.size(); i++) {
            boolean userFound = false;
            for (int j = 0; j < newUsers.size() && !userFound; j++) {
                if (existingUsers.get(i).getID() == newUsers.get(j).getID()) {
                    userFound = true;
                }
            }
            if (!userFound) {
                usersToRemove.add(existingUsers.get(i));
            }
        }
        for (int i = 0; i < newUsers.size(); i++) {
            boolean userFound = false;
            for (int j = 0; j < existingUsers.size() && !userFound; j++) {
                if (existingUsers.get(j).getID() == newUsers.get(i).getID()) {
                    userFound = true;
                }
            }
            if (!userFound) {
                usersToAdd.add(newUsers.get(i));
            }
        }
    }
    protected void updateUsers (Todo todo, List<User> usersToAdd, List<User> usersToRemove) throws SQLException {
        String addQuery = "insert into todoteilnehmer values (default, ?, ?);";
        PreparedStatement addStatement = con.prepareStatement(addQuery);

        String removeQuery = "delete from todoteilnehmer where todoid = ? and nutzerid = ?;";
        PreparedStatement removeStatement = con.prepareStatement(removeQuery);

        for (int i = 0; i < usersToAdd.size(); i++) {
            addStatement.setInt(1, todo.getId());
            addStatement.setInt(2, usersToAdd.get(i).getID());
            addStatement.addBatch();
        }
        for (int i = 0; i < usersToRemove.size(); i++) {
            removeStatement.setInt(1, todo.getId());
            removeStatement.setInt(2, usersToRemove.get(i).getID());
            removeStatement.addBatch();
        }
        addStatement.executeBatch();
        removeStatement.executeBatch();
    }
}
