package shared;

import java.time.ZonedDateTime;

public class Reminder extends LVKategorieElement
{
    //erbt anzeigename von LVKategorieElement - anzeigename ist die nachricht

    ZonedDateTime remindDate = null;
    ZonedDateTime eventDate = null;
    Enums.ReminderType messageType = null;
    boolean shouldRemind = false;

    public Reminder (String anzeigename) {
        super(anzeigename);
    }
    public Reminder (String anzeigename, int position) {
        super(anzeigename, position);
    }
    public Reminder (int id, String anzeigename, int position) {
        super(id, anzeigename, position);
    }
    public Reminder (int id, String anzeigename, int position, ZonedDateTime remindDate, ZonedDateTime eventDate, Enums.ReminderType type, boolean shouldRemind) {
        super(id, anzeigename, position);
        this.remindDate = remindDate;
        this.eventDate = eventDate;
        this.messageType = type;
        this.shouldRemind = shouldRemind;
    }

    public void setRemindDate(ZonedDateTime remindDate)  {
        this.remindDate = remindDate;
    }
    public ZonedDateTime getRemindDate () {
        return remindDate;
    }
    public void setEventDate(ZonedDateTime eventDate) {
        this.eventDate = eventDate;
    }
    public ZonedDateTime getEventDate () {
        return eventDate;
    }
    public void setMessageType(Enums.ReminderType messageType) {
        this.messageType = messageType;
    }
    public Enums.ReminderType getMessageType() {
        return messageType;
    }
    public void setShouldRemind (boolean shouldRemind) {
        this.shouldRemind = shouldRemind;
    }
    public boolean getShouldRemind () {
        return shouldRemind;
    }
}
