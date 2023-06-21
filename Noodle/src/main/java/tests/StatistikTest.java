import client.Launcher;
import client.RequestHandler;
import client.Response;
import org.junit.jupiter.api.*;
import shared.Feedback;
import shared.QuizAntMitAnz;
import shared.quiz.QuizAntwort;
import shared.quiz.Quizfrage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// -------------------------------------------------------------------------------
//             Modultests funktionieren NUR mit der Online-Datenbank!
// -------------------------------------------------------------------------------

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StatistikTest {

    static int id = -1;

    @BeforeAll
    static void init() {        // Erstellung einer Bewertung mit 3 Abgaben (2 bestanden, 1 nicht bestanden)
        Launcher.requestHandler = new RequestHandler("127.0.0.1", 1337);
        Response preparationResponse = Launcher.requestHandler.request(RequestHandler.Art.GETTEST,"/feedbackTest/init", Integer.class);
        if (preparationResponse != null && preparationResponse.getStatusCode() == 200) {
            id = (Integer) preparationResponse.getElement();
        }
    }

    @Test
    @Order(1)
    void testPASSED() {        // Unterscheidung bei der Auswertung zwischen Studierenden, die bestanden bzw. nicht bestanden haben
        Launcher.requestHandler = new RequestHandler("127.0.0.1", 1337);
        Response mainResponse = Launcher.requestHandler.request(RequestHandler.Art.GETTEST,"/feedback?fbid=" + id, Feedback.class);

        if (mainResponse != null && mainResponse.getStatusCode() == 200) {
            Feedback f = (Feedback) mainResponse.getElement();
            List<QuizAntMitAnz> passed = f.getAntPassed();
            List<QuizAntMitAnz> failed = f.getAntFailed();
            int pass = passed.get(0).getAnzahl();
            int fail = failed.get(0).getAnzahl();
            Assertions.assertTrue(2 == pass && 1 == fail, "2 und 1 erwartet, aber " + pass + " und " + fail + " erhalten");
        } else Assertions.fail("Fehler bei 'mainResponse' (Wahrscheinlich keine Verbindung zum Server)");
    }

    @Test
    @Order(2)
    void testNUMBER() {        // Häufigkeit, mit der eine Antwort ausgewählt wurde
        Launcher.requestHandler = new RequestHandler("127.0.0.1", 1337);
        Response mainResponse = Launcher.requestHandler.request(RequestHandler.Art.GETTEST,"/feedback?fbid=" + id, Feedback.class);

        if (mainResponse != null && mainResponse.getStatusCode() == 200) {
            Feedback f = (Feedback) mainResponse.getElement();
            int anzahl = 0;
            for (QuizAntMitAnz x : f.getAntFailed()) {
                anzahl = anzahl + x.getAnzahl();
            }
            for (QuizAntMitAnz x : f.getAntPassed()) {
                anzahl = anzahl + x.getAnzahl();
            }
            Assertions.assertEquals(3, anzahl, "3 erwartet, aber " + anzahl + " erhalten");
        } else Assertions.fail("Fehler bei 'mainResponse' (Wahrscheinlich keine Verbindung zum Server)");
    }

    @Test
    @Order(3)
    void testSAVE() {        // Speichern einer Bewertung
        Launcher.requestHandler = new RequestHandler("127.0.0.1", 1337);
        String args = "?bewertungid=" + id + "&courseid=803&userid=136";
        Response initResponse = Launcher.requestHandler.request(RequestHandler.Art.GET, "/bewertung/load" + args, shared.Bewertung.class);

        if (initResponse != null && initResponse.getStatusCode() == 200) {
            shared.Bewertung bewertung = (shared.Bewertung) initResponse.getElement();
            List<Quizfrage> qf = bewertung.getFragen();
            int frageid = qf.get(0).getFrageid();
            List<QuizAntwort> qa = qf.get(0).getAntworten();
            int antwortid = qa.get(0).getId();
            HashMap<Integer, Integer> häschmäp = new HashMap<>();
            häschmäp.put(frageid, antwortid);

            Response mainResponse = Launcher.requestHandler.request(RequestHandler.Art.POSTTEST,"/bewertung?userid=136&courseid=803&bewertungid=" + id, häschmäp);
            if (mainResponse != null && mainResponse.getStatusCode() == 200) {
                Response secResponse = Launcher.requestHandler.request(RequestHandler.Art.GETTEST,"/feedback?fbid=" + id, Feedback.class);
                if (secResponse != null && secResponse.getStatusCode() == 200) {
                    Feedback feedback = (Feedback) secResponse.getElement();

                    List<QuizAntMitAnz> all = new ArrayList<>();
                    for (QuizAntMitAnz x : feedback.getAntFailed()) {
                        if (!all.contains(x)) {
                            all.add(x);
                        }
                    }
                    for (QuizAntMitAnz x : feedback.getAntPassed()) {
                        if (!all.contains(x)) {
                            all.add(x);
                        }
                    }

                    int anzahl = 0;
                    for (QuizAntMitAnz x : all) {
                        if (x.getQA().getAntwort().equals(qa.get(0).getAntwort())) {
                            anzahl = x.getAnzahl();
                        }
                    }
                    Assertions.assertEquals(1, anzahl, "1 erwartet, aber " + anzahl + " erhalten");
                } else Assertions.fail("Fehler bei 'secResponse'");
            } else Assertions.fail("Fehler bei 'mainResponse'");
         } else Assertions.fail("Fehler bei 'initResponse' (Wahrscheinlich keine Verbindung zum Server)");
    }

}
