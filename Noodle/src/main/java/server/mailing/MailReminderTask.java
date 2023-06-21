package server.mailing;

import server.db.DatabaseUtility;
import shared.Reminder;
import shared.User;
import shared.utility.ReminderMitUsern;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MailReminderTask extends TimerTask {
    Connection con = null;

    public MailReminderTask (Connection con) {
        this.con = con;
    }

    @Override
    public void run() {
        con = DatabaseUtility.getValidConnection(con);
        if (con == null) {
            return;
        }
        System.out.println("TimerTaskRun");

        List<ReminderMitUsern> reminder = getReminder();
        sendMails(reminder);
        setSeen(reminder);
    }

    public List<ReminderMitUsern> getReminder () {
        List<ReminderMitUsern> reminderMitUsern = new ArrayList<>();
        try {
            String query = "select anzeigename, frist, erinnernab, e.id as id, n.vorname as vorname, n.nachname as nachname, n.email as mail, v.name as veranstaltungsname " +
                    "from erinnerung e " +
                    "inner join termin t on t.id = e.terminid " +
                    "inner join terminzukategorie tzk on t.id = tzk.terminid " +
                    "inner join kategorieelement ke on ke.id = tzk.kategorieelementid " +
                    "inner join veranstaltungskategorie vk on vk.id = ke.veranstaltungskategorieid " +
                    "inner join veranstaltung v on v.id = vk.veranstaltungsid " +
                    "left join veranstaltungsteilnehmer vt on vt.veranstaltungsid = v.id " +
                    "left join nutzer n on n.id = vt.nutzerid " +
                    "where zustellung = 'Mail' and erinnernab < ? and e.id not in (select erinnerungid from gesendeteMailErinnerung) " +
                    "order by erinnernab asc limit 100;";
            PreparedStatement stmt = con.prepareStatement(query);
            ZonedDateTime utc = ZonedDateTime.now(ZoneId.of("UTC"));
            stmt.setTimestamp(1, Timestamp.from(utc.toInstant()));
            ResultSet rs = stmt.executeQuery();



            ReminderMitUsern current = null;
            while (rs.next()) {
                System.out.println("sendMail.. " + rs.getInt("id"));
                if (current == null || current.getReminder().getId() != rs.getInt("id")) {
                    current = new ReminderMitUsern();
                    Reminder temp = new Reminder(rs.getString("anzeigename"));
                    temp.setId(rs.getInt("id"));
                    temp.setEventDate(ZonedDateTime.ofInstant(rs.getTimestamp("frist").toInstant(), ZoneId.of("UTC")));
                    temp.setRemindDate(ZonedDateTime.ofInstant(rs.getTimestamp("erinnernab").toInstant(), ZoneId.of("UTC")));
                    current.setReminder(temp);
                    reminderMitUsern.add(current);
                    current.setVeranstaltung(rs.getString("veranstaltungsname"));
                }
                User user = new User(rs.getString("vorname"), rs.getString("nachname"), rs.getString("mail"), null);
                current.getUsers().add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<ReminderMitUsern>();
        }
        return reminderMitUsern;
    }
    public void sendMails(List<ReminderMitUsern> reminderMitUser) {

        for (int i = 0; i < reminderMitUser.size(); i++) {
            List<User> users = reminderMitUser.get(i).getUsers();

            ZonedDateTime frist = reminderMitUser.get(i).getReminder().getEventDate().withZoneSameInstant(ZoneId.of("Europe/Berlin"));

            String subject = "Erinnerung für: " + reminderMitUser.get(i).getVeranstaltung() + "!";
            String message = "Dies ist eine Erinnerung der Veranstaltungs: " + reminderMitUser.get(i).getVeranstaltung() + " für: " + System.lineSeparator() +
                    reminderMitUser.get(i).getReminder().getAnzeigename() + ". Die Frist hierfür ist: " +
                    DateTimeFormatter.ofPattern("dd/MM/yyyy - hh:mm").format(frist) + " deutscher Zeit (+01:00).";
            for (int j = 0; j < users.size(); j++) {
                Mail.send(users.get(j).getEmail(), subject, message);
            }
        }
    }
    public void setSeen (List<ReminderMitUsern> reminderMitUsern) {
        String query = "insert into gesendeteMailErinnerung values (default, ?);";
        try {
            PreparedStatement stmt = con.prepareStatement(query);

            for (int i = 0; i < reminderMitUsern.size(); i++) {
                stmt.setInt(1, reminderMitUsern.get(i).getReminder().getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
