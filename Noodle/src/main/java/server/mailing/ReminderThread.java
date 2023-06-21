package server.mailing;

import jakarta.servlet.http.HttpServletResponse;
import server.db.DatabaseUtility;
import shared.Reminder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ReminderThread extends Thread {

    Connection con = null;

    Timer timer = null;
    TimerTask reminderTimerTask = null;
    TimerTask bestandenTimerTask = null;

    public ReminderThread (Connection con) {
        this.con = con;

        timer = new Timer();
        reminderTimerTask = new MailReminderTask(con);
        bestandenTimerTask = new MailBestandenTask(con);
    }

    public void close () {
        timer.cancel();
        closeReminder();
        closeBestanden();
    }
    public void closeReminder() {
        reminderTimerTask.cancel();
    }
    public void closeBestanden() {
        bestandenTimerTask.cancel();
    }

    @Override
    public void run () {
        timer.scheduleAtFixedRate(reminderTimerTask, 0, 10*1000);
        timer.scheduleAtFixedRate(bestandenTimerTask, 0, 10*1000);
    }


}
