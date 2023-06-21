package client.Controller;

import client.FileHandler;
import client.Launcher;
import client.RequestHandler;
import client.Response;
import client.urlconnections.ProfilBearbeitenRequest;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import shared.LehrveranstaltungsListe;
import shared.ProfilDaten;
import shared.Student;
import shared.Validation;

import java.io.File;


public class ProfilBearbeitenStudent {

    @FXML
    TextField VornameID;
    @FXML
    TextField NachnameID;
    @FXML
    TextField MailID;
    @FXML
    TextField MatNrID;
    @FXML
    Button AuswählenID;
    @FXML
    TextField StraßeID;
    @FXML
    TextField HausnrID;
    @FXML
    TextField OrtID;
    @FXML
    TextField PlzID;
    @FXML
    TextField StudienfachID;
    @FXML
    Button AbbrechenID;
    @FXML
    Button SpeichernID;
    @FXML
    PasswordField PasswortID;

    File fileImage = null;

    @FXML
    private void initialize() {

        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET, "/profilbearbeiten", ProfilDaten.class);

        if (response.statusCode == 200) {
            ProfilDaten profil = (ProfilDaten) response.getElement();

            VornameID.setText(profil.getVorname());
            NachnameID.setText(profil.getNachname());
            MailID.setText(profil.getEmail());
            MatNrID.setText(Integer.toString(profil.getMatrikelnummer()));
            StraßeID.setText(profil.getStrasse());
            HausnrID.setText(profil.getHausnummer());
            OrtID.setText(profil.getOrt());
            PlzID.setText(Integer.toString(profil.getPlz()));
            StudienfachID.setText(profil.getStudienfach());
        }

    }

    private boolean check() {
        boolean ret = true;
        if (StraßeID.getText().length() < 2) {
            System.out.println("Straße muss mindestens 2 Zeichen haben!");
            StraßeID.setText("");
            ret = false;
        }
        if (!Validation.checkEmptyAndNull(HausnrID.getText())) {
            System.out.println("Keine Hausnummer angegeben");
            HausnrID.setText("");
            ret = false;
        }
        if (PlzID.getText().length() != 5 || !Validation.isStringInteger(PlzID.getText())) {
            System.out.println("PLZ muss genau 5 Zahlen beinhalten!");
            PlzID.setText("");
            ret = false;
        }
        if (OrtID.getText().length() < 2) {
            System.out.println("Ort muss mindestens 2 Zeichen haben!");
            OrtID.setText("");
            ret = false;
        }
        if (!Validation.checkEmptyAndNull(StudienfachID.getText())) {
            System.out.println("Kein Studienfach angegeben!");
            StudienfachID.setText("");
            ret = false;
        }
        return ret;
    }

    @FXML
    private void BearbeitungAbbrechen(ActionEvent actionEvent) {
        Launcher.gui.addToNavigation("/fxml/EigenesProfil2.fxml");
    }

    @FXML
    private void BearbeitungSpeichern(ActionEvent actionEvent) {
        if (check()) {
            Student student = new Student(
                    Launcher.useridMitToken.getUserid(),
                    StraßeID.getText(),
                    HausnrID.getText(),
                    Integer.parseInt(PlzID.getText()),
                    OrtID.getText(),
                    StudienfachID.getText(),
                    fileImage
            );

            int response = ProfilBearbeitenRequest.editProfile(student, PasswortID.getText());

            if (response == 200) {
                Launcher.gui.addToNavigation("/fxml/EigenesProfil2.fxml");
            }
        }
    }

    @FXML
    private void ProfilbildAuswählen (ActionEvent actionEvent) {
        File file = FileHandler.openImage();
        if (file == null) return;
        AuswählenID.setText(file.getName());
        fileImage = file;
    }
}