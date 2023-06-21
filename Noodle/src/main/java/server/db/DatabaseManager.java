/*

Stellt die Datenbankverbindung her. Verbindung wird im Konstruktor hergestellt, kann aber auch über connect() gemacht werden
query Funktion ermöglicht sql queries als string, sollte aber zugunsten von preparedstatements nicht verwendet werden
boolean isloggedin(Connection con, int userid, String token) gibt zurück ob der nutzer eingeloggt ist

pgAdmin: 195.90.200.105:5050 (im Browser) oder pgadmin für desktop

Datenbank Adresse 195.90.200.105:18769
Passwort: ENym/r/HR!rLg{2e
 */

package server.db;

import com.google.gson.Gson;
import shared.*;

import java.io.File;
import java.sql.*;
import java.util.PropertyPermission;

public class DatabaseManager {
    String url;
    Connection con;
    public DatabaseManager() throws SQLException {
        connect();

    }
    public Connection connect() throws SQLException {
        url = "jdbc:postgresql://195.90.200.105:18769/noodleDatabase?user=postgres&password=ENym/r/HR!rLg{2e&rewriteBatchedStatements=true";
//        url = "jdbc:postgresql://127.0.0.1:18769/noodleDatabase?user=postgres&password=123456789&rewriteBatchedStatements=true";
        try {
            con = DriverManager.getConnection(url);
            if(con != null){
                System.out.println("Conntected To DB");
                return con;
            }
            else
                System.out.println("Failed to connect To DB");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void disconnect() {
        try {
            con.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static ResultSet query(Connection con, String query) {
        try {
            Statement stmt = con.createStatement();

            ResultSet rs = stmt.executeQuery(query);

            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public Connection getConnection() {
        return con;
    }


}
