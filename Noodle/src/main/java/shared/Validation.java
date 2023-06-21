package shared;
import org.apache.commons.validator.routines.EmailValidator;

import java.io.File;
import java.util.Arrays;

public class Validation {

    public static boolean verifyPassword(String password1, String password2){
        return password1.equals(password2);
    }

    public static boolean checkEmptyAndNull(String text){
        return !text.isEmpty();
    }

    public static boolean isStringInteger(String number){
        return checkEmptyAndNull(number) && number.matches("-?(0|[1-9]\\d*)");
    }

    public static String getNumbersFromString(String text){
        return text.replaceAll("\\D+","");
    }

    public static boolean checkEmail(String email){
        return EmailValidator.getInstance().isValid(email);

    }

    public static boolean checkImage(File file){
        String[] extensions ={
                "png",
                "jpg",
                "jpeg"
        };

        if(Arrays.stream(extensions).anyMatch(getFileType(file)::equals)) return true;

        return false;
    }

    public static String getFileType(File file){
        String parts[] = file.getName().split("\\.");
        return parts[parts.length-1];
    }

    public static boolean checkStringTime(String time){
        String times[] = time.split(":");
        if(times.length != 2) return false;
        if(!isStringInteger(times[0])) return false;
        if(Integer.parseInt(times[0])< 0 || Integer.parseInt(times[0]) > 24) return false;


        if(((Character) times[0].charAt(0)).toString() == "0"){
            if(!isStringInteger(((Character)times[1].charAt(1)).toString())) return false;
            times[1] = ((Character)times[1].charAt(1)).toString();
        }
        if(Integer.parseInt(times[1])< 0 || Integer.parseInt(times[1]) > 59) return false;
        return true;
    }
}
