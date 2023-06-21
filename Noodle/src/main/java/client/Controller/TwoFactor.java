package client.Controller;

import client.Launcher;
import client.RequestHandler;
import client.Response;
import com.sun.javafx.scene.control.InputField;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import shared.Enums;
import shared.Lehrende;
import shared.Validation;
import shared.accounts.UseridMitToken;
import shared.accounts.VerificationData;
import shared.navigation.NavigationInformation;

public class TwoFactor {

    @FXML
    TextField code;


    public void login(){

        if(!Validation.isStringInteger(code.getText()) || code.getText().length() != 6){
            code.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            return;
        }


        VerificationData verificationData = new VerificationData(Launcher.useridMitToken.getUserid(), Integer.parseInt(code.getText()), Launcher.useridMitToken.getToken());


        Response response = Launcher.requestHandler.request(RequestHandler.Art.POST,"/login/verify", verificationData, UseridMitToken.class);

        if(response.statusCode != 200){
            code.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            return;
        }
        Launcher.useridMitToken = (UseridMitToken) response.getElement();

        response = Launcher.requestHandler.request(RequestHandler.Art.GET,"/navigation", NavigationInformation.class);

        if(response.statusCode == 200) {
            NavigationInformation navInfo = (NavigationInformation) response.getElement();

            if(navInfo.getUser() instanceof Lehrende){
                Launcher.userArt = Enums.Current.LEHRKRAFT;
                Launcher.gui.changeState("navigationLehrkraft");
                return;
            }
        }
        Launcher.gui.changeState("navigation");

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
