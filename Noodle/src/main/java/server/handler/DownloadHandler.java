package server.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import server.ServerUtility;
import server.db.DatabaseManager;
import server.db.DatabaseUtility;


public class DownloadHandler extends AbstractHandler {

    Connection con = null;

    public DownloadHandler() {
        try {
            DatabaseManager db = new DatabaseManager();
            con = db.connect();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");

        if (request.getMethod().equals("GET") && splitTarget.length == 2 && splitTarget[1].equals("getfile")) {
            baseRequest.setHandled(true);

            con = DatabaseUtility.getValidConnection(con);
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            if(!DatabaseUtility.isLoggedIn(con, ServerUtility.extractUseridAndToken(request))) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            int fileid = -1;

            try {
                fileid = Integer.parseInt(request.getParameter("fileid"));
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            File file = null;
            try {
                file = retrieveFile(fileid);
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            if (file.exists()) {
                System.out.println("File Exists");
                byte[] bytes = FileUtils.readFileToByteArray(file);
                response.getOutputStream().write(bytes);
                response.flushBuffer();
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                System.out.println("File doesn't exist");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
        }
    }
    protected File retrieveFile(int id) throws Exception {
        File file = null;

        String query = "select * from datei where id = ?";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            String path = rs.getString(2) + rs.getString(3);
            System.out.println(path);
            file = new File(path);
        }

        return file;
    }
}
