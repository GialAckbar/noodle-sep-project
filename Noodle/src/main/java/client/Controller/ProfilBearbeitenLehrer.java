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
import shared.*;

import java.io.File;


public class ProfilBearbeitenLehrer {

    @FXML
    TextField VornameID;
    @FXML
    TextField NachnameID;
    @FXML
    TextField MailID;
    @FXML
    TextField ForschungsgebietID;
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
    TextField LehrstuhlID;
    @FXML
    Button AbbrechenID;
    @FXML
    Button SpeichernID;
    @FXML
    PasswordField PasswortID;

    File fileImage = null;


    public void initialize() {

        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET, "/profilbearbeiten", ProfilDaten.class);

        if (response.statusCode == 200) {
            ProfilDaten profil = (ProfilDaten) response.getElement();

            VornameID.setText(profil.getVorname());
            NachnameID.setText(profil.getNachname());
            MailID.setText(profil.getEmail());
            StraßeID.setText(profil.getStrasse());
            HausnrID.setText(profil.getHausnummer());
            PlzID.setText(Integer.toString(profil.getPlz()));
            OrtID.setText(profil.getOrt());
            LehrstuhlID.setText(profil.getLehrstuhl());
            ForschungsgebietID.setText(profil.getForschungsgebiet());
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
        if (!Validation.checkEmptyAndNull(LehrstuhlID.getText())) {
            System.out.println("Kein Lehrstuhl angegeben!");
            LehrstuhlID.setText("");
            ret = false;
        }
        if (!Validation.checkEmptyAndNull(ForschungsgebietID.getText())) {
            System.out.println("Kein Forschungsgebiet angegeben!");
            ForschungsgebietID.setText("");
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
             Lehrende lehrer = new Lehrende(
                     Launcher.useridMitToken.getUserid(),
                     StraßeID.getText(),
                     HausnrID.getText(),
                     Integer.parseInt(PlzID.getText()),
                     OrtID.getText(),
                     LehrstuhlID.getText(),
                     ForschungsgebietID.getText(),
                     fileImage
            );

            int response = ProfilBearbeitenRequest.editProfile(lehrer,PasswortID.getText());

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

