package shared;

public class Enums {
    public enum SemesterTyp {
        SS,
        WS;
        public static String getString (SemesterTyp typ) {
            switch (typ) {
                case SS:
                    return "SS";
                case WS:
                    return "WS";
                default:
                    return "";
            }

        }
    }
    public enum Art{
        VORLESUNG,
        SEMINAR,
        PROJEKTGRUPPE;
        public static String getString (Art art) {
            switch (art) {
                case SEMINAR:
                    return "Seminar";
                case VORLESUNG:
                    return "Vorlesung";
                case PROJEKTGRUPPE:
                    return "Projektgruppe";
                default:
                    return "";
            }

        }
    }

    public enum Current{
        STUDENT,
        LEHRKRAFT
    }
    public enum ReminderType {
        POPUP,
        MAIL;
        public static String getString (ReminderType reminderType) {
            switch (reminderType) {
                case POPUP:
                    return "Popup";
                case MAIL:
                    return "Mail";
                default:
                    return "";
            }
        }
    }
}
