package client;

import javafx.stage.FileChooser;

import java.io.File;

public class FileHandler {



    public static File open(){
        return open(new FileChooser());

    }

    private static File open(FileChooser fileChooser){
        File file = fileChooser.showOpenDialog(null);
        return file;
    }

    public static File openImage(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images: JPG, JPEG, PNG", "*.jpg", "*.jpeg", "*.png")
        );
        return open(fileChooser);
    }

    public static File openCSV(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV", "*.csv")
        );
        return open(fileChooser);
    }
    public static File openXML(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("XML", "*.xml")
        );
        return open(fileChooser);
    }
    public static File openbibtex(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("bibtex", "*.bib")
        );
        return open(fileChooser);
    }

    public static String createFilePath(String name){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(name);

        File file = fileChooser.showSaveDialog(null);
        if(file == null) return null;
        return file.getAbsolutePath();
    }

}
