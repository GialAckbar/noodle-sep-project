package client.Controller;

import client.FileHandler;
import client.Launcher;
import client.RequestHandler;
import client.Response;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import shared.*;
import shared.quiz.Quiz;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class KursansichtKategorieDatei {

    @FXML
    Text name;

    public int id = -1;
    public int kursId = -1;
    String filename;
    Todo todo;
    Reminder reminder;
    Quiz quiz;
    int chat = -1;
    Lernkarte lernkarte;
    shared.Bewertung bewertung;

    public KursansichtKategorie kursansichtKategorie;
    public void clicked(){
        if(kursansichtKategorie.kursansicht.status.getText().equals("Eintragen")) return;
        if(chat != -1){
            Chat.id = chat;
            Chat chat = Launcher.gui.addOverNavigation("/fxml/Chat.fxml");
            chat.isProjektgruppenChat = true;
            return;
        }
        if(lernkarte != null){
            LernkarteCreate lernkarteCreate = Launcher.gui.addOverNavigation("/fxml/lernkarte.fxml");
            lernkarteCreate.load(lernkarte);
            lernkarteCreate.disable();
        }
        if(todo != null){
            TodoAnzeigen todoAnzeigen = Launcher.gui.addOverNavigation("/fxml/todoanzeigen.fxml");
            todoAnzeigen.loadTodo(kursId, todo);
            return;
        }else if(reminder != null){
            AddReminder addReminder = Launcher.gui.addOverNavigation("/fxml/addreminder.fxml");
            addReminder.loadReminder(reminder);
            addReminder.disableAll();
            return;
        }else if(quiz != null){
            System.out.println("quizid: " + quiz.getId());
            if(Launcher.userArt == Enums.Current.LEHRKRAFT){
                QuizLehrerFragen.id = quiz.getId();
                Launcher.gui.addOverNavigation("/fxml/QuizLehrerFragen.fxml");
                return;
            }
            QuizStudentFragen.id = quiz.getId();
            Launcher.gui.addOverNavigation("/fxml/QuizStudentFragen.fxml");
            return;
        }else if(bewertung != null){
            System.out.println("feedbackid: " + bewertung.getId());
            Bewertung.id = bewertung.getId();
            if(Launcher.userArt == Enums.Current.LEHRKRAFT){
                Launcher.gui.addOverNavigation("/fxml/feedback_stats.fxml");
            }else{
                String args = "?bewertungid=" + bewertung.getId() + "&courseid=" + Kursansicht.id;
                Response response = Launcher.requestHandler.request(RequestHandler.Art.GET, "/bewertung/permission" + args, int[].class);
                if (response != null && response.statusCode == 200) {
                    int[] results = (int[]) response.getElement();
                    double prozent = (double) results[1] / (double) results[0] * 100;
                    if (prozent >= 50) {
                        Launcher.gui.addOverNavigation("/fxml/BewertungStudent.fxml");
                    } else {
                        int remaining = (int) Math.ceil((double) results[0] / 2) - results[1];
                        if (remaining == 0) {
                            Launcher.gui.addOverNavigation("/fxml/BewertungStudent.fxml");
                        } else {
                            PopUpBewertung popup = Launcher.gui.addPopupBewertung();
                            popup.titel.setText("Anforderung nicht erfüllt!");
                            if (remaining == 1) popup.nachricht.setText("Schließe noch ein Quiz ab, um eine Bewertung einreichen zu können.");
                            else popup.nachricht.setText("Schließe noch " + remaining + " weitere Quizze ab, um eine Bewertung einreichen zu können.");
                        }
                    }
                } else if (response != null && response.statusCode == 403) {
                    PopUpBewertung popup = Launcher.gui.addPopupBewertung();
                    popup.titel.setText("Bereits abgegeben!");
                    popup.nachricht.setText("Du hast diese Bewertung bereits abgegeben. Vielen Dank für deine Unterstützung!");
                } else {
                    PopUpBewertung popup = Launcher.gui.addPopupBewertung();
                    popup.titel.setText("Fehler beim Öffnen!");
                    popup.nachricht.setText("Ein unerwarteter Fehler ist aufgetreten!");
                }
            }
        }
        System.out.println(id);
        if(id != -1){
            URL url = null;
            try{
                url = new URL("http","127.0.0.1",1337,"/getfile?userid="+ Launcher.useridMitToken.getUserid() + "&token=" + Launcher.useridMitToken.getToken()+"&fileid=" + id);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            if(url == null) return;

            String path = FileHandler.createFilePath(filename);
            if(path == null) return;

            try {
                InputStream inputStream = url.openStream();
                Files.copy(inputStream, Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
