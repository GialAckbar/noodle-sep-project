package server.db;

import shared.KategorieDatei;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.List;
import java.util.Random;

public class FileUtility {

    public static boolean copyPathsFromFileIds(Connection con, List<Integer> ids, List<String> paths) {
        if (con == null || ids == null || paths == null || ids.size() == 0) { return false; }
        try {
            String query = "select * from datei where id in (";

            for (int i = 0; i < ids.size(); i++) {
                query += ids.get(i);
                if (i != ids.size()-1) {
                    query += ",";
                } else {
                    query += ");";
                }
            }

            System.out.println(query);
            PreparedStatement stmt = con.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();


            while (rs.next()) {
                paths.add(rs.getString(2) + rs.getString(3));
            }
        } catch (SQLException e) {
            return false;
        }


        return true;
    }

    public static boolean deleteFilesFromFileDatabase(List<String> paths) {
        if (paths == null) {return false; }

        File file = null;

        for (int i = 0; i < paths.size(); i++) {
            file = new File(paths.get(i));
            file.delete();
        }
        return false;
    }

    public static String getFileExtensionFromName(String filename) {
        String[] splitName = filename.split("\\.");

        if (splitName.length >= 2) {
            return splitName[splitName.length-1];
        } else {
            return "";
        }
    }
    public static void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {

        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[4096];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }

    }
    public static String randomString() {
        int leftLimit = 0;
        int rightLimit = 0;
        int length = 10;
        Random random = new Random();

        StringBuilder buffer = new StringBuilder(length);
        for (int i = 0; i < length; i++) {

            int charType = random.nextInt(3);

            switch (charType) {
                case 0:
                    leftLimit = 97; //a
                    rightLimit = 122; //z
                    break;
                case 1:
                    leftLimit = 65;
                    rightLimit = 90;
                    break;
                case 2:
                    leftLimit = 48;
                    rightLimit = 57;
                    break;
            }

            int code = random.nextInt(rightLimit - leftLimit + 1) + leftLimit;
            buffer.append((char) code);
        }

        return buffer.toString();
    }
    public static String randomString(int stringLength) {
        if (stringLength < 1) {
            return "";
        }
        int leftLimit = 0;
        int rightLimit = 0;
        int length = stringLength;
        Random random = new Random();

        StringBuilder buffer = new StringBuilder(length);
        for (int i = 0; i < length; i++) {

            int charType = random.nextInt(3);

            switch (charType) {
                case 0:
                    leftLimit = 97; //a
                    rightLimit = 122; //z
                    break;
                case 1:
                    leftLimit = 65;
                    rightLimit = 90;
                    break;
                case 2:
                    leftLimit = 48;
                    rightLimit = 57;
                    break;
            }

            int code = random.nextInt(rightLimit - leftLimit + 1) + leftLimit;
            buffer.append((char) code);
        }

        return buffer.toString();
    }
}
