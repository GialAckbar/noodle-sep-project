package client.Controller;

import client.FileHandler;
import client.Launcher;
import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import shared.quiz.Quiz;
import shared.quiz.QuizAntwort;
import shared.quiz.Quizfrage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class QuizHinzufügenXML {

    @FXML TextField titel;
    @FXML TextField von;
    @FXML TextField bis;
    @FXML Button open;
    @FXML Button create;
    @FXML VBox placeholder;
    KursHinzufuegenKategorie_add kursHinzufuegenKategorie_add;

    public void back(){
        Launcher.gui.removeOverNavigation();
    }

    public void open(){
        File file = FileHandler.openXML();
        if(file == null) return;
        readFile(file);
    }

    public void readFile(File file){
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(file);

            document.getDocumentElement().normalize();

            if(document.getDocumentElement().getNodeName() != "Quizfragen") return;
            NodeList nodeList = document.getElementsByTagName("Fragenzeile");

            Quiz quiz = new Quiz("");

            for(int i = 0; i != nodeList.getLength(); i++){
                Node current = nodeList.item(i);

                if(current.getNodeType() == Node.ELEMENT_NODE){
                    Quizfrage quizfrage = new Quizfrage(((Element) current).getElementsByTagName("Frage").item(0).getTextContent());
                    quiz.addFrage(quizfrage);
                    QuizAntwort quizAntwortA = new QuizAntwort(((Element) current).getElementsByTagName("AntwortA").item(0).getTextContent());
                    QuizAntwort quizAntwortB = new QuizAntwort(((Element) current).getElementsByTagName("AntwortB").item(0).getTextContent());
                    QuizAntwort quizAntwortC = new QuizAntwort(((Element) current).getElementsByTagName("AntwortC").item(0).getTextContent());
                    QuizAntwort quizAntwortD = new QuizAntwort(((Element) current).getElementsByTagName("AntwortD").item(0).getTextContent());

                    switch (((Element) current).getElementsByTagName("KorrekteAntwort").item(0).getTextContent()){
                        case "A":
                            quizAntwortA.setRichtig(true);
                            break;
                        case "B":
                            quizAntwortB.setRichtig(true);
                            break;
                        case "C":
                            quizAntwortC.setRichtig(true);
                            break;
                        case "D":
                            quizAntwortD.setRichtig(true);
                            break;
                    }
                    quizfrage.addAntwort(quizAntwortA);
                    quizfrage.addAntwort(quizAntwortB);
                    quizfrage.addAntwort(quizAntwortC);
                    quizfrage.addAntwort(quizAntwortD);
                }
            }

            System.out.println("Quiz: " + new Gson().toJson(quiz));
            QuizHinzufügen quizHinzufügen = Launcher.gui.addOverNavigation("/fxml/QuizHinzufügen.fxml");
            quizHinzufügen.kursHinzufuegenKategorie_add = kursHinzufuegenKategorie_add;
            quizHinzufügen.loadQuiz(quiz);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void manuell(){
        Launcher.gui.addToNavigation("/fxml/QuizHinzufügen.fxml");
    }

    public void reactOnEntry(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #637381;-fx-text-fill: WHITE;-fx-background-radius: 8px;-fx-font-family: 'Montserrat Regular';");
    }

    public void reactOnExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent;-fx-text-fill: #404040;-fx-border-color:#637381;-fx-border-radius:8px;-fx-font-family: 'Montserrat Regular';");
    }

    public void reactOnEntryInverted(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent;-fx-text-fill: #404040;-fx-border-color:#637381;-fx-border-radius:8px;-fx-font-family: 'Montserrat Regular';");
    }

    public void reactOnExitInverted(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #637381;-fx-text-fill: WHITE;-fx-background-radius: 8px;-fx-font-family: 'Montserrat Regular';");
    }

}





