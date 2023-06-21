package client.urlconnections;

import client.Controller.kurs.KursHinzufügenRequest;
import com.google.gson.Gson;
import shared.KategorieDatei;
import shared.LVKategorie;
import shared.LVKategorieElement;
import shared.Lehrveranstaltung;
import shared.accounts.UseridMitToken;
import shared.utility.GsonUtility;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class KursBearbeitenRequest {
    private final String boundary = "CaiUniqueCaiBoundaryCai";
    private static final String LINE_FEED = "\r\n";
    private HttpURLConnection httpConn;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter writer;

    public KursBearbeitenRequest (String requestURL, String charset, Lehrveranstaltung lehrveranstaltung, UseridMitToken useridMitToken) throws IOException {
        this.charset = charset;

        // creates a unique boundary based on time stamp
        //boundary = "===" + System.currentTimeMillis() + "===";
        init();
        addJsonPart(GsonUtility.getGson().toJson(lehrveranstaltung, Lehrveranstaltung.class), "CourseJson");
        addJsonPart(GsonUtility.getGson().toJson(useridMitToken, UseridMitToken.class), "UserWithTokenJson");
        extractParts(lehrveranstaltung);

    }
    protected void init () throws IOException {
        URL url = new URL("http://localhost:1337/editCourse");
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true); // indicates POST method
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        httpConn.setRequestProperty("User-Agent", "CodeJava Agent");
        httpConn.setRequestProperty("Test", "Bonjour");
        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
    }
    protected void addJsonPart(String json, String name) {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\";").append(LINE_FEED);
        writer.append("Content-Type: application/json").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        writer.append(json);
        System.out.println("ich bin json:");
        System.out.println(json);
        writer.append(LINE_FEED);
        writer.flush();
    }
    protected void extractParts(Lehrveranstaltung lehrveranstaltung) {
        List<LVKategorie> kategorien = lehrveranstaltung.getKategorien();

        for (int i = 0; i < kategorien.size(); i++) {

            List<LVKategorieElement> elemente = kategorien.get(i).getKategorieElemente();

            for (int j = 0; j < elemente.size(); j++) {
                if (elemente.get(j) != null && elemente.get(j) instanceof KategorieDatei && ((KategorieDatei) elemente.get(j)).getDateiid() < 0) {
                    KategorieDatei datei = (KategorieDatei) elemente.get(j);

                    String filename = ("file-" + kategorien.get(i).getPosition() + "-" + elemente.get(j).getPosition());
                    System.out.println("filename: " + filename);
                    if (datei.getFile() != null) {
                        try {
                            addFilePart(filename, datei.getFile());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        File file = new File(datei.getPathWithName());
                        if (file.exists()) {
                            System.out.println("File exists");
                            try {
                                addFilePart(filename, file );
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }
    public void addFilePart(String fieldName, File uploadFile) throws IOException {
        String fileName = uploadFile.getName();
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"").append(LINE_FEED);
        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        System.out.println(URLConnection.guessContentTypeFromName(fileName));

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();

        writer.append(LINE_FEED); // maybe remove
        writer.flush();
    }
    public int finish() throws IOException {

        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();

        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            return 0;
        } else if(status == HttpURLConnection.HTTP_CONFLICT) {
            return -2;
        } else {
            return -1;
        }

    }

    public static int updateCourse(Lehrveranstaltung lehrveranstaltung, UseridMitToken useridMitToken) {
        int ret = -1;
        try {
            KursBearbeitenRequest request = new KursBearbeitenRequest("localhost:1337/addCourse", "UTF-8", lehrveranstaltung, useridMitToken);
            ret = request.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
