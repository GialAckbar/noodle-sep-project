package server.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import server.db.DatabaseUtility;
import server.db.FileUtility;
import shared.Lehrveranstaltung;
import shared.utility.GsonUtility;

import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TestTimeHandler extends AbstractHandler {
    Connection con = null;

    public TestTimeHandler (Connection con) {
        this.con = con;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");
        if (baseRequest.getMethod().equals("POST") && splitTarget.length == 2 && splitTarget[1].equals("testtime")) {

            baseRequest.setHandled(true);

            con = DatabaseUtility.getValidConnection(con);
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            ZonedDateTime dateTime = ZonedDateTime.now();

            String json = GsonUtility.getGson().toJson(dateTime, ZonedDateTime.class);
            System.out.println("Json: " + json);
            dateTime = GsonUtility.getGson().fromJson(json, ZonedDateTime.class);
            System.out.println("DateTime: " + dateTime);

            ZoneId australia = ZoneId.of("Europe/Berlin");
            String str = "2015-01-05 17:00";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime localDateAndTime = LocalDateTime.parse(str, formatter);
            ZonedDateTime dateAndTimeInSydney = ZonedDateTime.of(localDateAndTime, australia );
            ZonedDateTime utcDate = dateAndTimeInSydney.withZoneSameInstant(ZoneId.of("UTC"));


            System.out.println("Current date and time in UTC : " + utcDate);
            System.out.println("-----");
            System.out.println("dateAndTimeInSydney : ");
            getTime(insertTime(dateAndTimeInSydney));
            System.out.println("dateAndTimeInUTC : ");

            getTime(insertTime(utcDate));
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
    }
    protected int insertTime (ZonedDateTime dateTime) {
        OffsetDateTime offsetDateTime = dateTime.withZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime();
        try {
            String query = "insert into timetest (zeit) values ( ?) returning id;";
            PreparedStatement stmt = con.prepareStatement(query);

            //stmt.setObject(1, dateTime);

            OffsetDateTime val = OffsetDateTime.now(ZoneOffset.UTC);
            stmt.setObject(1, Timestamp.from(dateTime.withZoneSameInstant(ZoneId.of("UTC")).toInstant()));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    protected void getTime (int index) {
        try {
            String query = "select zeit from timetest where id = ?;";
            PreparedStatement stmt = con.prepareStatement(query);

            stmt.setInt(1, index);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ZonedDateTime dateTime = ZonedDateTime.ofInstant(rs.getTimestamp(1).toInstant(), ZoneId.of("UTC"));
                System.out.println(dateTime.withZoneSameInstant(ZoneId.systemDefault()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
