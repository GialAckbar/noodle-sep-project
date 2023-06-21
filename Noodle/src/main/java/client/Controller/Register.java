package client.Controller;

import client.*;
import client.urlconnections.ProfilHinzufügenRequest;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import shared.Enums;
import shared.Lehrende;
import shared.Student;
import shared.Validation;
import shared.accounts.LehrkraftmitPasswort;
import shared.accounts.StudentMitPasswort;

import java.io.File;


public class Register {

    @FXML
    Group placeholder;

    @FXML
    TextField vorname;

    @FXML
    TextField nachname;

    @FXML
    TextField email;

    @FXML
    PasswordField password;

    @FXML
    PasswordField passwordconfirm;

    @FXML
    Button image;

    @FXML
    TextField strasse;

    @FXML
    TextField hausnummer;

    @FXML
    TextField plz;

    @FXML
    TextField ort;


    Enums.Current current = Enums.Current.LEHRKRAFT;

    RegisterStudent registerStudent;
    RegisterLehrkraft registerLehrkraft;

    File fileImage = null;

    @FXML
    public void initialize() {
        change();
    }

    public void change(){
        Launcher.gui.clearElement(placeholder);
        if(current == Enums.Current.LEHRKRAFT){
            registerStudent = Launcher.gui.addElement(placeholder,"/fxml/Register_Student.fxml");
            registerStudent.register = this;
            registerLehrkraft = null;
            current = Enums.Current.STUDENT;
        }else{
            registerLehrkraft = Launcher.gui.addElement(placeholder,"/fxml/Register_Lehrkraft.fxml");
            registerLehrkraft.register = this;
            registerStudent = null;
            current = Enums.Current.LEHRKRAFT;
        }
    }




    public void login(){
        Launcher.gui.changeState("login");
    }


    public void selectImage(){
        File file = FileHandler.openImage();
        if(file == null) return;
        image.setText(file.getName());
        fileImage = file;
    }


    public boolean checkAllInputs(){
        resetStyle();
        boolean ret = true;
        if(registerStudent != null){
            if(!Validation.checkEmptyAndNull(registerStudent.studienfach.getText())){
                System.out.println("Studienfach is invalid");
//                registerStudent.studienfach.clear();
                registerStudent.studienfach.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
                ret = false;
            }
        }
        else{
            if(!Validation.checkEmptyAndNull(registerLehrkraft.lehrstuhl.getText())){
                System.out.println("Lehrstuhl is invalid");
//                registerLehrkraft.lehrstuhl.clear();
                registerLehrkraft.lehrstuhl.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
                ret = false;
            }
            if(!Validation.checkEmptyAndNull(registerLehrkraft.forschungsgebiet.getText())){
                System.out.println("Forschungsgebiet is invalid");
//                registerLehrkraft.forschungsgebiet.clear();
                registerLehrkraft.forschungsgebiet.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
                ret = false;
            }
        }

        if(!Validation.checkEmptyAndNull(vorname.getText())){
            System.out.println("Vorname is invalid");
//            vorname.clear();
            vorname.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            ret = false;
        }
        if(!Validation.checkEmptyAndNull(nachname.getText())){
            System.out.println("Nachname is invalid");
//            nachname.clear();
            nachname.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            ret = false;
        }
        if(!Validation.checkEmail(email.getText())){
            System.out.println("Email is invalid");
//            email.clear();
            email.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            ret = false;
        }
        if(!Validation.checkEmptyAndNull(password.getText())){
            System.out.println("Password is invalid");
            password.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            passwordconfirm.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            ret = false;
        }
        if(!Validation.verifyPassword(password.getText(),passwordconfirm.getText())){
            System.out.println("Passwords not matching");
            password.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            passwordconfirm.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            ret = false;
        }
        if(fileImage != null && !Validation.checkImage(fileImage)){
            System.out.println("Image is invalid");
            image.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            ret = false;
        }
        if(!Validation.checkEmptyAndNull(strasse.getText())){
            System.out.println("Strasse is invalid");
//            strasse.clear();
            strasse.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            ret = false;
        }
        if(!Validation.checkEmptyAndNull(hausnummer.getText())){
            System.out.println("Hausnummer is invalid");
//            hausnummer.clear();
            hausnummer.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            ret = false;
        }
        if(!Validation.isStringInteger(plz.getText())){
            System.out.println("PLZ is invalid");
            plz.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
//            plz.clear();
            ret = false;
        }
        if(plz.getText().length() != 5){
            System.out.println("PLZ length is invalid");
//            plz.clear();
            plz.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            ret = false;
        }
        if(!Validation.checkEmptyAndNull(ort.getText())){
            System.out.println("Ort is invalid");
//            ort.clear();
            ort.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");

            ret = false;
        }




        return ret;


    }

    public void resetStyle(){
        if(registerStudent != null){
            registerStudent.studienfach.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        }
        else{
            registerLehrkraft.lehrstuhl.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            registerLehrkraft.forschungsgebiet.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        }
        vorname.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        nachname.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        email.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        password.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        passwordconfirm.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        image.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        strasse.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        hausnummer.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        plz.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        plz.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        ort.setStyle("-fx-background-color: #EEEEEE; -fx-border-radius: 8px");
    }

    public void register(){

        if(checkAllInputs()){
            System.out.println("Data is valid");
//            Response response;
            int response;
            if(current == Enums.Current.STUDENT){
                Student student = new Student(
                        vorname.getText(),
                        nachname.getText(),
                        email.getText(),
                        strasse.getText(),
                        hausnummer.getText(),
                        Integer.parseInt(plz.getText()),
                        ort.getText(),
                        -1,
                        registerStudent.studienfach.getText(),
                        fileImage
                );


//                StudentMitPasswort studentMitPasswort = new StudentMitPasswort(student,password.getText());
                response = ProfilHinzufügenRequest.addProfile(student,password.getText());
//                response = Launcher.requestHandler.request(RequestHandler.Art.POST,"/register/student",studentMitPasswort,Integer.class);

            }
            else{
                Lehrende lehrende = new Lehrende(
                        vorname.getText(),
                        nachname.getText(),
                        email.getText(),
                        strasse.getText(),
                        hausnummer.getText(),
                        Integer.parseInt(plz.getText()),
                        ort.getText(),
                        registerLehrkraft.lehrstuhl.getText(),
                        registerLehrkraft.forschungsgebiet.getText(),
                        fileImage
                );

//                LehrkraftmitPasswort lehrkraftmitPasswort = new LehrkraftmitPasswort(lehrende,password.getText());
                response = ProfilHinzufügenRequest.addProfile(lehrende,password.getText());
//                response = Launcher.requestHandler.request(RequestHandler.Art.POST,"/register/lehrkraft",lehrkraftmitPasswort,Boolean.class);
                //wenn email existiert dann fehler returned keinen boolean
                // bild muss noch hochgeladen werden wenn es existiert
            }

            if(response != -1){
//                System.out.println(response.getStatusCode());
//                System.out.println(response.getElement() );
                Login.login(email.getText(),password.getText());


                if(response == 1){
                    Registriert.matrikel = -1;
                    Launcher.gui.changeState("registriertLehrkraft");
                    Launcher.userArt = Enums.Current.LEHRKRAFT;
                    return;
                }
                Registriert.matrikel = response;
                Launcher.gui.changeState("registriertStudent");
                return;
            }
            else{
                System.out.println("400");
                email.clear();
                password.clear();
                passwordconfirm.clear();
            }

        }
        else{
            System.out.println("Data is invalid");
            password.clear();
            passwordconfirm.clear();
        }

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
