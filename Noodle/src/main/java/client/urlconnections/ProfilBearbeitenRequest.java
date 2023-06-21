package client.urlconnections;

import client.Controller.kurs.KursHinzufügenRequest;
import com.google.gson.Gson;
import shared.*;
import shared.accounts.LehrkraftmitPasswort;
import shared.accounts.StudentMitPasswort;
import shared.accounts.UseridMitToken;
import shared.utility.GsonUtility;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class ProfilBearbeitenRequest {
    private final String boundary = "CaiUniqueCaiBoundaryCai";
    private static final String LINE_FEED = "\r\n";
    private HttpURLConnection httpConn;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter writer;

    public ProfilBearbeitenRequest(String charset, User user, String passwort) throws IOException {
        this.charset = charset;

        // creates a unique boundary based on time stamp
        //boundary = "===" + System.currentTimeMillis() + "===";

        String requestURL = getURL(user);
        init(requestURL);
        addUserJson(user, passwort);
        extractProfilePicture(user);

    }
    protected void addUserJson(User user, String passwort) {
        if (user instanceof Student) {
            addJsonPart(GsonUtility.getGson().toJson(new StudentMitPasswort((Student) user, passwort), StudentMitPasswort.class), "ProfileJson");
        } else if (user instanceof Lehrende) {
            addJsonPart(GsonUtility.getGson().toJson(new LehrkraftmitPasswort((Lehrende) user, passwort), LehrkraftmitPasswort.class), "ProfileJson");
        }
    }
    protected String getURL(User user) {
        String ret = "http://localhost:1337/profilbearbeiten2";

        if (user instanceof Student) {
            ret = ret + "/student";
        } else if (user instanceof Lehrende) {
            ret = ret + "/lehrkraft";
        }

        return ret;
    }
    protected void init (String requestURL) throws IOException {
        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true); // indicates POST method
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
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

        writer.append(LINE_FEED);
        writer.flush();
    }
    protected void extractProfilePicture(User user) throws IOException {
        File profilePicture = user.getImage();
        if (profilePicture != null) {
            addFilePart("ProfilePicture", profilePicture);
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

        int profilid = -1;

        if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader((httpConn.getInputStream())));
            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            profilid = Integer.parseInt(sb.toString());
        } else {
            System.out.println("-" + httpConn.getResponseCode());
        }
        return profilid;
    }
    public static int editProfile(User user, String passwort) {
        int id = -1;
        try {
            ProfilBearbeitenRequest request = new ProfilBearbeitenRequest("UTF-8", user, passwort);
            id = request.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return id;
    }
}
