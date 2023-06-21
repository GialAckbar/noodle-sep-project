package client.Controller.kurs;

import com.google.gson.Gson;
import shared.*;
import shared.accounts.UseridMitToken;
import shared.utility.GsonUtility;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class KursHinzufügenRequest {
    private final String boundary = "CaiUniqueCaiBoundaryCai";
    private static final String LINE_FEED = "\r\n";
    private HttpURLConnection httpConn;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter writer;

    public KursHinzufügenRequest (String requestURL, String charset, Lehrveranstaltung lehrveranstaltung, UseridMitToken useridMitToken) throws IOException {
        this.charset = charset;

        // creates a unique boundary based on time stamp
        //boundary = "===" + System.currentTimeMillis() + "===";
        init();
        addJsonPart(GsonUtility.getGson().toJson(lehrveranstaltung, Lehrveranstaltung.class), "CourseJson");
        addJsonPart(new Gson().toJson(useridMitToken, UseridMitToken.class), "UserWithTokenJson");
        extractParts(lehrveranstaltung);

    }
    protected void init () throws IOException {
        URL url = new URL("http://localhost:1337/addCourse");
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
        System.out.println("ICH BIN JSON:");
        System.out.println(json);
        writer.append(json);

        writer.append(LINE_FEED);
        writer.flush();
    }
    protected void extractParts(Lehrveranstaltung lehrveranstaltung) {
        List<LVKategorie> kategorien = lehrveranstaltung.getKategorien();

        for (int i = 0; i < kategorien.size(); i++) {

            List<LVKategorieElement> elemente = kategorien.get(i).getKategorieElemente();

            for (int j = 0; j < elemente.size(); j++) {
                if (elemente.get(j) != null && elemente.get(j) instanceof KategorieDatei) {
                    KategorieDatei datei = (KategorieDatei) elemente.get(j);

                    String filename = ("file-" + i + "-" + j);

                    if (datei.getFile() != null) {
                        try {
                            addFilePart(filename, datei.getFile() );
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        File file = new File(datei.getPathWithName());
                        if (file.exists()) {
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

        int veranstaltungsid = -1;

        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader((httpConn.getInputStream())));
            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            veranstaltungsid = Integer.parseInt(sb.toString());
        } else if (status == HttpURLConnection.HTTP_CONFLICT) {
            return -2;
        }
        else {
            System.out.println(status);
        }
        return veranstaltungsid;
    }
    public static int createCourse(Lehrveranstaltung lehrveranstaltung, UseridMitToken useridMitToken) {
        int id = -1;
        try {
            KursHinzufügenRequest request = new KursHinzufügenRequest("localhost:1337/addCourse", "UTF-8", lehrveranstaltung, useridMitToken);
            id = request.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return id;
    }
}
