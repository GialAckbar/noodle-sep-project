package server.mailing;


import server.db.DatabaseUtility;
import shared.Enums;
import shared.Lehrveranstaltung;
import shared.Semester;
import shared.utility.BestandenMailInformation;
import shared.utility.VeranstaltungMitAnzahl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import shared.Enums.*;

public class MailBestandenTask extends TimerTask {
    Connection con = null;

    public MailBestandenTask(Connection con) {
        this.con = con;
    }

    @Override
    public void run() {
        con = DatabaseUtility.getValidConnection(con);
        if (con == null) {
            return;
        }
        System.out.println("BestandenTaskRun");

        List<BestandenMailInformation> bestandenMailInformationList = getMailInformation();
        System.out.println("ListLength: " + bestandenMailInformationList.size());
        sendMails(bestandenMailInformationList);
    }
    protected List<BestandenMailInformation> getMailInformation () {
        List<BestandenMailInformation> bestandenMailInformationList = new ArrayList<>();
        String getQuery =
                "with veranstaltungen as (select v.* from veranstaltung v where v.id not in (select veranstaltungsid from gesendetebestandenerinnerung) and " +
                        "(jahr < ? or (jahr = ? and semester = 'SS'))), " +
                "nutzermindestenseinmalbestanden as (select n.id as nutzerid, n.email as mail, count(qa) as anzahlbestanden, v2.id as veranstaltungsid, " +
                "   v2.name as name, v2.jahr as jahr, v2.semester as semester, " +
                "       cast((select count(*) from veranstaltung v " +
                "       inner join veranstaltungskategorie vk on vk.veranstaltungsid = v.id " +
                "       inner join kategorieelement ke on ke.veranstaltungskategorieid = vk.id " +
                "       inner join quizzukategorie qzk on qzk.kategorieelementid = ke.id " +
                "       inner join quiz q on qzk.quizid = q.id where v.id = v2.id) as float) as quizanzahl " +
                "   from veranstaltungen v2, veranstaltung v " +
                "   inner join veranstaltungskategorie vk on vk.veranstaltungsid = v.id " +
                "   inner join kategorieelement ke on ke.veranstaltungskategorieid = vk.id " +
                "   inner join quizzukategorie qzk on qzk.kategorieelementid = ke.id " +
                "   inner join quiz q on qzk.quizid = q.id " +
                "   inner join quizabgabe qa on qa.quizid = q.id " +
                "   inner join nutzer n on n.id = qa.nutzerid " +
                "   inner join student st on n.id = st.nutzerid " +
                "   where v.id = v2.id and qa.bestanden = true group by (n.id, n.email, v2.id, v2.name, v2.jahr, v2.semester)) " +
                "select nutzerid, mail, anzahlbestanden, veranstaltungsid, name, jahr, semester, (anzahlbestanden >= (quizanzahl / 2)) as bestanden " +
                "   from nutzermindestenseinmalbestanden " +
                "union " +
                "select n.id as nutzerid, n.email as mail, 0 as anzahlbestanden, v.id as veranstaltungsid, v.name as name, v.jahr as jahr, v.semester as semester, " +
                "       (select count(*) = 0 from veranstaltung v3 " +
                "       inner join veranstaltungskategorie vk on vk.veranstaltungsid = v.id " +
                "       inner join kategorieelement ke on ke.veranstaltungskategorieid = vk.id " +
                "       inner join quizzukategorie qzk on qzk.kategorieelementid = ke.id " +
                "       inner join quiz q on qzk.quizid = q.id where v3.id = v.id) as bestanden " +
                "   from nutzer n " +
                "   inner join student st on n.id = st.nutzerid " +
                "   inner join veranstaltungsteilnehmer vt on n.id = vt.nutzerid " +
                "   inner join veranstaltungen v on v.id = vt.veranstaltungsid " +
                "   where (n.id, v.id) not in (select nutzerid, veranstaltungsid from nutzermindestenseinmalbestanden) " +
                "   group by n.id, n.email, v.id, v.jahr, v.name, v.semester " +
                "   order by veranstaltungsid asc";
        String setSentQuery = "insert into gesendetebestandenerinnerung values (default, ?);";
        try {
            PreparedStatement getStatement = con.prepareStatement(getQuery);

            Semester currentSemester = getCurrentSemester();

            if (currentSemester.getSemesterTyp().equals(SemesterTyp.SS)) {
                getStatement.setInt(1, currentSemester.getJahr());
                getStatement.setInt(2, currentSemester.getJahr() - 1);
            } else {
                getStatement.setInt(1, currentSemester.getJahr());
                getStatement.setInt(2, currentSemester.getJahr());
            }

            ResultSet rs = getStatement.executeQuery();

            List<Integer> veranstaltungsids = new ArrayList<>();
            while (rs.next()) {
                if (!veranstaltungsids.contains(rs.getInt("veranstaltungsid"))) {
                    veranstaltungsids.add(rs.getInt("veranstaltungsid"));
                }
                Enums.SemesterTyp semesterTyp = (rs.getString("semester").equals("WS")) ? Enums.SemesterTyp.WS : Enums.SemesterTyp.SS;
                Semester semester = new Semester(rs.getInt("jahr"), semesterTyp);
                BestandenMailInformation temp = new BestandenMailInformation(rs.getString("name"), semester, rs.getString("mail"), rs.getBoolean("bestanden"));

                bestandenMailInformationList.add(temp);
            }

            PreparedStatement setSentStatement = con.prepareStatement(setSentQuery);

            for (int i = 0; i < veranstaltungsids.size(); i++) {
                setSentStatement.setInt(1, veranstaltungsids.get(i));
                setSentStatement.addBatch();
            }
            if (veranstaltungsids.size() >= 1) {
                setSentStatement.executeBatch();
            }
            setSentStatement.close();
            getStatement.close();
            return bestandenMailInformationList;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<BestandenMailInformation>();
    }
    public void sendMails(List<BestandenMailInformation> bestandenMailInformationList) {
        for (int i = 0; i < bestandenMailInformationList.size(); i++) {
            BestandenMailInformation temp = bestandenMailInformationList.get(i);

            StringBuilder subjectBuilder = new StringBuilder();
            subjectBuilder.append("Veranstaltung \"")
                    .append(temp.getVeranstaltungsname())
                    .append("\" ")
                    .append(temp.isBestanden() ? "" : "nicht ")
                    .append("bestanden!");

            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Sie haben die Veranstaltung \"")
                    .append(temp.getVeranstaltungsname())
                    .append("\" im ")
                    .append((temp.getSemester().getSemesterTyp().equals(SemesterTyp.SS)) ? "Sommersemester " : "Wintersemester ")
                    .append(temp.getSemester().getJahr())
                    .append(temp.isBestanden() ? " " : " leider nicht ")
                    .append("bestanden!");

            System.out.println(temp.getMailAdresse());
            System.out.println(subjectBuilder.toString());
            System.out.println(messageBuilder.toString());

            Mail.send(temp.getMailAdresse(), subjectBuilder.toString(), messageBuilder.toString());
        }
    }

    public static Semester getCurrentSemester() {
        LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.of("UTC"));

        int year = currentDateTime.getYear();
        int month = currentDateTime.getMonthValue();

        Semester semester;

        if (month < 4) {
            semester = new Semester(year - 1, SemesterTyp.WS);
        } else if (month < 10) {
            semester = new Semester(year, SemesterTyp.SS);
        } else {
            semester = new Semester(year, SemesterTyp.WS);
        }
        return semester;
    }
}
