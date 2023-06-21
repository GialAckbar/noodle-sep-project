package client.Controller;

import client.FileHandler;
import client.Launcher;
import client.RequestHandler;
import client.Response;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.jbibtex.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import shared.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public class ThemaHinzufügen {

    @FXML
    public TextField titel;

    @FXML
    public TextField beschreibung;

    @FXML
    public Button open;

    @FXML
    public VBox placeholder;

    @FXML
    public Button back;

    @FXML
    public Button create;

    @FXML
    Text errorText;

    List<String[]> allliteraturen = new ArrayList<>();
    LiteraturverzeichnisListe list = new LiteraturverzeichnisListe();
    Boolean bool = false;

    public void create() {
        int id = -1;
        if (!validation()) {
            return;
        }
        // Literaturverzeichnis literaturverzeichnis = new Literaturverzeichnis("erste Literatur","autorrrr","202321","Book");
        //  list.add(literaturverzeichnis);
        Themenangebot thema = new Themenangebot(id, titel.getText(), beschreibung.getText());
        ThemenangebotLiteratur themenangebotsListe = new ThemenangebotLiteratur(list, thema);

        Response response = Launcher.requestHandler.request(RequestHandler.Art.POST, "/postliteraturverzeichnis", themenangebotsListe, ThemenangebotLiteratur.class);
        back();
    }

    @FXML
    public void initialize() throws IOException {
        errorText.setVisible(false);
    }

    public boolean validation() {
        resetStyle();
        boolean ret = true;

        if (titel.getText().isEmpty()) {
            System.out.println("Titel is invalid");
            titel.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            ret = false;
        }
        if (beschreibung.getText().isEmpty()) {
            System.out.println("Titel is invalid");
            beschreibung.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            ret = false;
        }
        if (!bool) {
            ret = false;
        }
        errorText.setVisible(true);
        errorText.setText("Thema beinhaltet noch Fehler!");

        return ret;
    }

    public void resetStyle() {

        titel.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        beschreibung.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");

    }

    public void open() throws ParseException, FileNotFoundException {
        File file = FileHandler.openbibtex();
        if (file == null) return;
        readFile(file);
    }

    public void back() {
        Launcher.gui.addToNavigation("/fxml/EigenesProfil2.fxml");
    }

    public void readFile(File file) throws ParseException, FileNotFoundException {
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            StringBuilder readerAsString = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                readerAsString.append(line);
                readerAsString.append('\n');
            }

            String[] split = readerAsString.toString().split("\\{\\.,");
            StringBuilder temp = new StringBuilder();
            for (int i = 0, j = 2000; i < split.length; i++, j++) {
                if (i == split.length - 1) {
                    temp.append(split[i]);
                } else temp.append(split[i]).append("{temp.").append(j).append(",");
            }

            String[] split2 = temp.toString().split("\\{\\.");
            StringBuilder temp2 = new StringBuilder();
            for (int i = 0; i < split2.length; i++) {
                if (i == split2.length - 1) {
                    temp2.append(split2[i]);
                } else temp2.append(split2[i]).append("{date.");
            }

            Reader reader = new StringReader(temp2.toString());

            BibTeXParser bibtexParser = new BibTeXParser() {
                @Override
                public void checkStringResolution(org.jbibtex.Key key, org.jbibtex.BibTeXString string) {

                    if (string == null) {
                        System.err.println("Unresolved string: \"" + key.getValue() + "\"");
                    }
                }

                @Override
                public void checkCrossReferenceResolution(org.jbibtex.Key key, org.jbibtex.BibTeXEntry entry) {

                    if (entry == null) {
                        System.err.println("Unresolved cross-reference: \"" + key.getValue() + "\"");
                    }
                }

            };
            BibTeXDatabase database = bibtexParser.parse(reader);

            Map<Key, BibTeXEntry> entryMap = database.getEntries();
            Collection<BibTeXEntry> entries = entryMap.values();
            for (BibTeXEntry entry : entries) {

                Value title2 = entry.getField(BibTeXEntry.KEY_TITLE);
                Value autor = entry.getField(BibTeXEntry.KEY_AUTHOR);
                Value jahr = entry.getField(BibTeXEntry.KEY_YEAR);

                try {
                    String t;
                    if (title2 != null) {
                        t = title2.toUserString();
                    } else {
                        t = "Unbekannt";
                    }

                    String a;
                    if (autor != null) {
                        a = autor.toUserString();
                    } else {
                        a = "Unbekannt";
                    }

                    String j;
                    if (jahr != null) {
                        j = jahr.toUserString();
                    } else {
                        j = "Unbekannt";
                    }

                    if (entry.getType().toString().toLowerCase().contains("book")) {
                        list.add(new Literaturverzeichnis(t, a, j, "Book"));
                    }
                    else if (entry.getType().toString().toLowerCase().contains("article")) {
                        list.add(new Literaturverzeichnis(t, a, j, "Article"));
                    }
                    else if (entry.getType().toString().toLowerCase().contains("proceedings")) {
                        list.add(new Literaturverzeichnis(t, a, j, "Proceedings"));
                    }
                    else if (entry.getType().toString().toLowerCase().contains("inproceedings")) {
                        list.add(new Literaturverzeichnis(t, a, j, "Inproceedings"));
                    }
                    else if (entry.getType().toString().toLowerCase().contains("misc")) {
                        list.add(new Literaturverzeichnis(t, a, j, "Misc"));
                    }
                    else if (entry.getType().toString().toLowerCase().contains("phdthesis")) {
                        list.add(new Literaturverzeichnis(t, a, j, "PhdThesis"));
                    }
                    else if (entry.getType().toString().toLowerCase().contains("incollection")) {
                        list.add(new Literaturverzeichnis(t, a, j, "Incollection"));
                    }
                    else if (entry.getType().toString().toLowerCase().contains("booklet")) {
                        list.add(new Literaturverzeichnis(t, a, j, "Booklet"));
                    }
                    else if (entry.getType().toString().toLowerCase().contains("conference")) {
                        list.add(new Literaturverzeichnis(t, a, j, "Conference"));
                    }
                    else if (entry.getType().toString().toLowerCase().contains("inbook")) {
                        list.add(new Literaturverzeichnis(t, a, j, "Inbook"));
                    }
                    else if (entry.getType().toString().toLowerCase().contains("mastersthesis")) {
                        list.add(new Literaturverzeichnis(t, a, j, "Mastersthesis"));
                    }
                    else if (entry.getType().toString().toLowerCase().contains("manual")) {
                        list.add(new Literaturverzeichnis(t, a, j, "Manual"));
                    }
                    else if (entry.getType().toString().toLowerCase().contains("techreport")) {
                        list.add(new Literaturverzeichnis(t, a, j, "Techreport"));
                    }
                    else if (entry.getType().toString().toLowerCase().contains("unpublished")) {
                        list.add(new Literaturverzeichnis(t, a, j, "Unpublished"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                bool = true;

            }
            literaturAnzeigen();
        } catch (TokenMgrException | IOException err) {
            throw new ParseException("Could not parse BibTeX library: " +
                    err.getMessage());
        }
    }

    public void literaturAnzeigen(){
        if(list != null){
            int counter = 1;
            for(Literaturverzeichnis l : list.getList()){
                String id = Integer.toString(l.getId());
                String titel = Integer.toString(counter) + ". " + l.getTitel();
                String autor = l.getAutor();
                String jahr = l.getJahr();
                String art = l.getArt();
                String thid = Integer.toString(l.getThid());

                String daten = "Autor: "  + autor + "     •     Jahr: " + jahr + "     •     Art: " + art;
                allliteraturen.add(new String[]{id,titel,daten,thid});
                counter++;
            }

            showAllLiteraturen(allliteraturen);
        }
    }
    void showAllLiteraturen(List<String[]> Users) {
        for (String[] u : Users) {
            addTopic(u[0], u[1],u[2],u[3]);
        }
    }
    void addTopic(String id,String titel, String daten,String thid) {
        GridPane gridPane = addGridEntry(id,titel,daten,thid);
        Group t = new Group();
        t.getChildren().add(gridPane);
        placeholder.getChildren().add(t);
    }
    private GridPane addGridEntry(String id,String titel, String daten, String thid) {
        GridPane gp = createGridPane();

        Text tit = new Text(titel);
        Text dat = new Text(daten);

        tit.getStyleClass().add("courseTitle");
        dat.getStyleClass().add("courseData");


        gp.addRow(0, tit);
        gp.addRow(1,dat);

        return gp;
    }

    public GridPane createGridPane() {
        GridPane gp = new GridPane();
        gp.setPrefSize(1087, 120);
        gp.getColumnConstraints().add(new ColumnConstraints(800));
        gp.getRowConstraints().add(new RowConstraints(10, 30, 30));
        gp.getRowConstraints().add(new RowConstraints(10, 30, 30));
        gp.setPadding(new Insets(0,0,0,60));
        return gp;
    }

    public void reactOnEntryInverted(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent;-fx-text-fill: #404040;-fx-border-color:#637381;-fx-border-radius:8px;");
    }

    public void reactOnExitInverted(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #637381;-fx-text-fill: WHITE;-fx-background-radius: 8px;");

    }

}


