package client.Controller;

import client.Launcher;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import shared.Enums;
import shared.Reminder;
import shared.Validation;

import java.time.*;
import java.util.Date;

public class AddReminder {

    @FXML
    TextField nachricht, fristTime, anzeigenAbTime;
    @FXML
    Text titel;

    @FXML
    ComboBox anzeigen, art;

    @FXML
    DatePicker anzeigenAbDate, fristDate;

    @FXML
    Button create;

    int id = -1;

    public KursHinzufuegenKategorie_add kursHinzufuegenKategorie_add;

    public void initialize(){
        anzeigen.getItems().addAll(
                "Ja",
                "Nein"
        );
        art.getItems().addAll(
                "Email",
                "Popup"
        );
    }

    public void loadReminder(Reminder reminder){
        if(reminder == null) return;
        id = reminder.getId();
        nachricht.setText(reminder.getAnzeigename());

        ZonedDateTime frist = (reminder.getEventDate()).withZoneSameInstant(ZoneId.systemDefault());
        LocalDateTime fristDateTime = frist.toLocalDateTime();


        if(reminder.getShouldRemind()){

            ZonedDateTime remind = (reminder.getRemindDate()).withZoneSameInstant(ZoneId.systemDefault());
            LocalDateTime remindDateTime = remind.toLocalDateTime();
            anzeigenAbTime.setText(remindDateTime.getHour() + ":" + remindDateTime.getMinute());
            anzeigenAbDate.setValue(remindDateTime.toLocalDate());

        }


        fristTime.setText(fristDateTime.getHour() + ":" + fristDateTime.getMinute());

        anzeigen.getSelectionModel().select(0);
        if(!reminder.getShouldRemind()) anzeigen.getSelectionModel().select(1);

        art.getSelectionModel().select(0);
        if(reminder.getMessageType() != Enums.ReminderType.MAIL) art.getSelectionModel().select(1);

        fristDate.setValue(fristDateTime.toLocalDate());
        anzeigenChange();

    }

    public void disableAll(){
        titel.setText("Termin");
        create.setVisible(false);
//        create.relocate(1000,0);
        nachricht.setDisable(true);
        fristTime.setDisable(true);
        anzeigenAbTime.setDisable(true);
        anzeigen.setDisable(true);
        art.setDisable(true);
        anzeigenAbDate.setDisable(true);
        fristDate.setDisable(true);
    }

    public void anzeigenChange(){
        if(anzeigen.getSelectionModel().getSelectedItem() == "Nein"){
            anzeigenAbDate.setDisable(true);
            anzeigenAbTime.setDisable(true);
            art.setDisable(true);
            return;
        }
        anzeigenAbDate.setDisable(false);
        anzeigenAbTime.setDisable(false);
        art.setDisable(false);
    }

    public void back(){
        Launcher.gui.removeOverNavigation();
    }

    public void create(){
        if(!validation()) return;

        boolean shouldRemind = anzeigen.getSelectionModel().getSelectedItem() == "Ja";
        Enums.ReminderType reminderType = Enums.ReminderType.POPUP;
        if(art.getSelectionModel().getSelectedItem() == "Email") reminderType = Enums.ReminderType.MAIL;
        Reminder reminder;
        if(shouldRemind){
            reminder = new Reminder(id,
                    nachricht.getText(),
                    1,
                    getZonedDateTime(anzeigenAbDate,anzeigenAbTime),
                    getZonedDateTime(fristDate,fristTime),
                    reminderType,
                    shouldRemind);
        }else {
            reminder = new Reminder(id,
                    nachricht.getText(),
                    1,
                    null,
                    getZonedDateTime(fristDate,fristTime),
                    null,
                    shouldRemind);
        }


        if(kursHinzufuegenKategorie_add != null){
            kursHinzufuegenKategorie_add.datei.setText(nachricht.getText());
            kursHinzufuegenKategorie_add.reminder = reminder;
        }
        
        back();
    }

    public ZonedDateTime getZonedDateTime(DatePicker datePicker, TextField timeField){
        String time[] = timeField.getText().split(":");
        LocalDate localDate = datePicker.getValue();
        Instant instant = Instant.from(localDate.atStartOfDay(ZoneId.systemDefault()));
        Date date = Date.from(instant);
        date.setTime(date.getTime() + (Integer.parseInt(time[0])*60*60 + Integer.parseInt(time[1])*60)*1000);
        ZonedDateTime remindDate = ZonedDateTime.ofInstant(date.toInstant(),ZoneId.systemDefault());
        return remindDate;
    }

    public boolean validation(){
        boolean ret = true;

        if(!Validation.checkEmptyAndNull(nachricht.getText())){
            ret = false;
            nachricht.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        }
        if(!Validation.checkEmptyAndNull(fristTime.getText())|| !Validation.checkStringTime(fristTime.getText())){
            ret = false;
            fristTime.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        }
        if(fristDate.getValue() == null){
            ret = false;
            fristDate.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        }

        if(anzeigen.getSelectionModel().getSelectedItem() == null){
            ret = false;
            anzeigen.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
        }
        if(anzeigen.getSelectionModel().getSelectedItem() == "Ja"){
            if(art.getSelectionModel().getSelectedItem() == null){
                ret = false;
                art.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            }

            if(anzeigenAbDate.getValue() == null){
                ret = false;
                anzeigenAbDate.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            }
            if(!Validation.checkEmptyAndNull(anzeigenAbTime.getText()) || !Validation.checkStringTime(anzeigenAbTime.getText())){
                ret = false;
                anzeigenAbTime.setStyle("-fx-border-color:#ff0000; -fx-border-width: 1px; -fx-background-color: #EEEEEE; -fx-border-radius: 8px");
            }
        }




        return ret;
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
