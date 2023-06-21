package client.Controller;

import client.Launcher;
import client.RequestHandler;
import client.Response;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import shared.Enums;
import shared.Lehrende;
import shared.Student;
import shared.Validation;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import shared.accounts.LoginDaten;
import shared.accounts.UseridMitToken;
import shared.navigation.NavigationInformation;


public class Login {

    @FXML
    TextField username;
    @FXML
    PasswordField password;

    public void initialize() {
        if(Launcher.useridMitToken == null) return;

        Launcher.userArt = Enums.Current.STUDENT;
        Launcher.useridMitToken = null;
        Chat.timer = false;
        ChatListeStarted.timer = false;
        Launcher.gui.navigation.timer.stop();

    };

    public void login(){
        UseridMitToken useridMitToken = login(username.getText(),password.getText());
        if(useridMitToken == null){
            password.clear();
            return;
        }

//        Response response = Launcher.requestHandler.request(RequestHandler.Art.GET,"/navigation", NavigationInformation.class);
//
//        if(response.statusCode == 200) {
//            NavigationInformation navInfo = (NavigationInformation) response.getElement();
//
//            if(navInfo.getUser() instanceof Lehrende){
//                Launcher.userArt = Enums.Current.LEHRKRAFT;
////                Launcher.gui.changeState("navigationLehrkraft");
////                return;
//            }
//        }



//        Launcher.gui.changeState("navigation");
        Launcher.gui.changeState("twoFactor");


    }

    public static UseridMitToken login(String username, String password){
        if(Validation.checkEmptyAndNull(username) && Validation.checkEmptyAndNull(password)){

            LoginDaten loginDaten;
            if(Validation.isStringInteger(username)){
                loginDaten = new LoginDaten(Integer.parseInt(username),password);
            }
            else{
                loginDaten = new LoginDaten(username,password);
            }



//            System.out.print(loginDaten);
            Response response = Launcher.requestHandler.request(RequestHandler.Art.POST,"/login", loginDaten,UseridMitToken.class);
//            System.out.println(response.statusCode);
//            System.out.println(response.getElement());



            if(response.statusCode == 200){
                UseridMitToken useridMitToken = (UseridMitToken) response.getElement();
                Launcher.useridMitToken = useridMitToken;
                return useridMitToken;
            }else {
                return null;
            }
        }
        else{
            System.out.println("Error");
            return null;
        }
    }

    public void register(){
        Launcher.gui.changeState("register");
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
