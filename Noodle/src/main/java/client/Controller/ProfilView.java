package client.Controller;

import client.Controller.kurs.BelegteKursListe;
import client.Launcher;
import client.RequestHandler;
import client.Response;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.commons.logging.Log;
import shared.*;


import javafx.event.ActionEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import shared.navigation.NavigationInformation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
// Icons made by https://www.flaticon.com/authors/good-ware from https://www.flaticon.com/
public class ProfilView {

    @FXML
    Text txtName;
    @FXML
    Text txtEmail;
    @FXML
    Text txtStudiengang;
    @FXML
    ImageView imgProfilbild;
    @FXML
    Text txtmatnr;
    @FXML
    Text txtstrassehausnr;
    @FXML
    Text txtplzstadt;
    @FXML
    Button btnbearbeiten;
    @FXML
    Group placeholder;
    @FXML
    Text txtName1;
    @FXML
    Text txtName11;
    @FXML
    Text zurueck;

    protected static int userid = Launcher.useridMitToken.getUserid();
    public static int accountid = -1;
    Boolean inCourse = false;
    Boolean ich;
    Student student;
    Lehrende lehrende;
    Enums.Current current;
    Enums.Current current2;
    Boolean friend = false;

    int imageID;
    Group mat = new Group();
    int width = 963;    //width
    int height = 640;   //height
    BufferedImage image = null;
    File f = null;
    Image img = new Image("/images/noodles.png");
    ImageView edit = new ImageView("/images/edit.png");
    //Credits:Add Friend by Christopher Holm-Hansen from the Noun Project
    ImageView add = new ImageView("/images/addFriend.png");

    Boolean themenangebote = true;
    Themenangebote angebote;
    Veranstaltungen veranstaltungen;
    EigenesProfil_onlyVeranstaltungen eigenesProfil_onlyVeranstaltungen;


    private List<LehrveranstaltungsListe> list;
    @FXML
    public void initialize() throws IOException {
        Response responseProfile = Launcher.requestHandler.request(RequestHandler.Art.GET,"/profildaten/profil?accountid=" + accountid, Profil.class);

        //Profl/Attribute laden
        if(responseProfile.statusCode == 200) { //Profil
            //mat.getChildren().add(txtName1);
            //mat.getChildren().add(txtmatnr);
            zurueck.setCursor(Cursor.HAND);
            edit.setFitWidth(20);
            edit.setFitHeight(20);
            add.setFitWidth(40);
            add.setFitHeight(40);
            Profil profil = (Profil) responseProfile.getElement();
            student = profil.getStudent() != null ? profil.getStudent() : new Student(null,null,null,null,0,null,null);

            lehrende = profil.getLehrende()!= null ? profil.getLehrende() :  new Lehrende(null,null,null,0);
            ich = profil.getIch();
            current = profil.getCurrent1();
            current2 = profil.getCurrent2();
            friend = profil.getFreund() != null ? profil.getFreund():false;
            inCourse = profil.getInCourse() != null ? profil.getInCourse() : false;
            if(friend){
                zurueck.setVisible(true);
            }
            else{
                zurueck.setVisible(false);
            }
//          getImage(new File("/images/noodles.png"));
            change();
        }

        //Daten entsprechend (Student/Lehrender) laden
        if(ich && (student !=  null || lehrende != null)) { //check
            if (ich && current == Enums.Current.STUDENT) {
                txtName.setText(student.getVorname() + " " + student.getNachname());
                txtEmail.setText(student.getEmail());
                txtStudiengang.setText(student.getStudienfach());
                txtmatnr.setText(String.valueOf(student.getMatrikelnummer()));
                txtstrassehausnr.setText(student.getAdresse().getStrasse() + " " + student.getAdresse().getHausnummer());
                txtplzstadt.setText(student.getAdresse().getPlz() + " " + student.getAdresse().getOrt());
                imageID = student.getImageID();
                btnbearbeiten.setGraphic(edit);
                Themenangebote.lehrender = false;
                Themenangebote.ich = true;
                //mat.setVisible(true);
            } else if (ich && current == Enums.Current.LEHRKRAFT) {
                txtName.setText(lehrende.getVorname() + " " + lehrende.getNachname());
                txtEmail.setText(lehrende.getEmail());
                txtstrassehausnr.setText(lehrende.getAdresse().getStrasse() + " " + lehrende.getAdresse().getHausnummer());
                txtplzstadt.setText(lehrende.getAdresse().getPlz() + " " + lehrende.getAdresse().getOrt());
                txtName1.setText("Forschungsgebiet");
                txtmatnr.setText(lehrende.getForschungsgebiet());
                txtStudiengang.setText(lehrende.getLehrstuhl());
                imageID = lehrende.getImageID();
                btnbearbeiten.setGraphic(edit);
                Themenangebote.lehrender = true;
                Themenangebote.ich = true;
            }
         } else if(ich==false && (student !=  null || lehrende != null)) { //check
            if (ich == false && current == Enums.Current.STUDENT && current2 == Enums.Current.STUDENT) {
                txtName.setText(student.getVorname() + " " + student.getNachname());
                txtEmail.setText(student.getEmail());
                txtStudiengang.setText(student.getStudienfach());
                txtName11.setVisible(false);
                txtstrassehausnr.setVisible(false);
                txtplzstadt.setVisible(false);
                txtmatnr.setVisible(false);
                txtName1.setVisible(false);
                imageID = student.getImageID();
                btnbearbeiten.setGraphic(add);
                if(!friend){
                    btnbearbeiten.setVisible(true);
                }else {
                    btnbearbeiten.setVisible(false);
                }
                Themenangebote.lehrender = false;
                Themenangebote.ich = false;
            } else if (ich == false && current == Enums.Current.LEHRKRAFT && current2 == Enums.Current.STUDENT) {//check
                txtName.setText(student.getVorname() + " " + student.getNachname());
                txtEmail.setText(student.getEmail());
                txtStudiengang.setText(student.getStudienfach());
                txtmatnr.setText(String.valueOf(student.getMatrikelnummer()));
                txtstrassehausnr.setText(student.getAdresse().getStrasse() + " " + student.getAdresse().getHausnummer());
                txtplzstadt.setText(student.getAdresse().getPlz() + " " + student.getAdresse().getOrt());
                imageID = student.getImageID();
                btnbearbeiten.setVisible(false);
                Themenangebote.lehrender = false;
                Themenangebote.ich = false;
            } else if (ich == false && current == Enums.Current.STUDENT && current2 == Enums.Current.LEHRKRAFT) { //check
                txtName.setText(lehrende.getVorname() + " " + lehrende.getNachname());
                txtEmail.setText(lehrende.getEmail());
                txtName1.setText("Forschungsgebiet");
                txtmatnr.setText(lehrende.getForschungsgebiet());
                txtStudiengang.setText(lehrende.getLehrstuhl());
                txtstrassehausnr.setVisible(false);
                txtplzstadt.setVisible(false);
                imageID = lehrende.getImageID();
                txtName11.setVisible(false);
                // mat.setVisible(false);
                btnbearbeiten.setVisible(false);
                Themenangebote.lehrender = true;
                Themenangebote.inCourse = inCourse;
                Themenangebote.accountid = accountid;
                Themenangebote.ich = false;
            } else if (ich == false && current == Enums.Current.LEHRKRAFT && current2 == Enums.Current.LEHRKRAFT) { //check
                txtName.setText(lehrende.getVorname() + " " + lehrende.getNachname());
                txtEmail.setText(lehrende.getEmail());
                txtstrassehausnr.setText(lehrende.getAdresse().getStrasse() + " " + lehrende.getAdresse().getHausnummer());
                txtplzstadt.setText(lehrende.getAdresse().getPlz() + " " + lehrende.getAdresse().getOrt());
                txtEmail.setText(lehrende.getEmail());
                txtName1.setText("Forschungsgebiet");
                txtmatnr.setText(lehrende.getForschungsgebiet());
                txtStudiengang.setText(lehrende.getLehrstuhl());
                imageID = lehrende.getImageID();
                btnbearbeiten.setVisible(false);
                // mat.setVisible(false);
                Themenangebote.lehrender = false;
                Themenangebote.ich = false;
            }
        }
        //Image aufrufen --> wenn nicht vorhanden dann standard Bild
        if(imageID != -1){
            imgProfilbild.setImage(new Image("http://127.0.0.1:1337/getfile?userid="+Launcher.useridMitToken.getUserid()+"&token="+Launcher.useridMitToken.getToken()+"&fileid="+imageID));
        }
        else{
            imgProfilbild.setImage(img);
        }
        //Kurse laden die bereits erstellt wurden
       // ScrollPane pane = FXMLLoader.load(getClass().getResource("/fxml/engagedCourseListProfileView.fxml"));
        //placeholder.getChildren().add(pane);
    }

    //zur Bearbeitungssicht wechseln
    public void bearbeiten() {
        if (current == Enums.Current.LEHRKRAFT && ich) {
            Launcher.gui.addToNavigation("/fxml/LehrerProfilEdit.fxml");
        } else if (current == Enums.Current.STUDENT && ich)
            {
                Launcher.gui.addToNavigation("/fxml/StudentProfilEdit.fxml");
            }
        else{
                pressFreundHinzufuegen();
            }
        }



    public void getImage(File file){
        //read image
        try{
                f = new File(file.getPath()); //image file path
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                image = ImageIO.read(f);
                System.out.println("Reading complete.");

        }catch(IOException e){
            System.out.println("Error: "+e);
        }
    }

   /* public GridPane belegteModule(String bss, String ccc, String daten,int id){
        //Launcher.gui.addElement(placeholder,"/fxml/engagedCourseList.fxml");
        kursliste.createGridPane();
        return kursliste.addGridEntry(bss,ccc,daten,id);
*/
      /*  VBox box = new VBox();
        GridPane gridPane  = null;
        if(pModul == null){
            System.out.println("Keine Kurse belegt!");
        }
        else {
            for (Lehrveranstaltung veranstaltung : pModul) {
                gridPane = createGridPane(veranstaltung.getTitel(), veranstaltung.getArt().toString(), "Herr Werner", veranstaltung.getSemester());
                box.getChildren().add(gridPane);
            }
            }
    */
   public void back() {
           Launcher.gui.removeOverNavigation();
   }

    public void pressFreundHinzufuegen() {
        String args = "?accountid=" + accountid;
        Response response = Launcher.requestHandler.request(RequestHandler.Art.POST,"/freundesliste/addRequest" + args, args);
        btnbearbeiten.setVisible(false);
    }

   /* private GridPane createGridPane(String mName, String mArt, String mProfessor, Semester mSemester) {
        GridPane gridPane = new GridPane();
        Pane btnPane = new Pane();

        //Größe des GridPanes und seinen Constraints
        gridPane.setPrefSize(1161, 114);
        gridPane.getColumnConstraints().add(new ColumnConstraints(100));
        gridPane.getColumnConstraints().add(new ColumnConstraints(100));

        //Name, E-Mail, (Matrikel-Nr.) in die einzelnen Constraints
        Text nameText = new Text("Belegt");
        nameText.setFill(Color.web("#404040"));
        nameText.setFont(Font.font("arial", FontWeight.BOLD, FontPosture.REGULAR, 18));
        gridPane.add(nameText, 0, 1);

        Text studiengang = new Text(mName);
        studiengang.setFill(Color.web("#404040"));
        studiengang.setFont(Font.font("system", FontWeight.NORMAL, FontPosture.REGULAR, 24));
        gridPane.add(studiengang, 0, 1);

        Text combo = new Text(mArt + "      •      " + mProfessor + "      •      " + mSemester.getSemesterTyp()+ mSemester.getJahr());
        combo.setFill(Color.web("#404040"));
        combo.setFont(Font.font("system", FontWeight.NORMAL, FontPosture.REGULAR, 16));
        gridPane.add(combo, 0, 3);

        btnPane.setPrefSize(200,200);
        Button btn = new Button();
        btnPane.getChildren().add(btn);
        btn.setLayoutX(404);
        btn.setLayoutY(-8);

        btn.setText("Zum Kurs");
        btn.setTextFill(Color.web("#ffffff"));
        btn.setFont(Font.font("system", FontWeight.NORMAL, FontPosture.REGULAR, 12));
        gridPane.add(btnPane, 1, 2);

        return gridPane;
    }
    */




/*
    ProfilView erzeugen mit nur dem Profil
    ProfilLehrenderView enthält die Module
    ProfilStudentView enthält nur den Button
 */


    @FXML
    void reactOnEntry(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #374048;");

    }

    @FXML
    void reactOnExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #637381;");
    }

    public void change(){
        Launcher.gui.clearElement(placeholder);
        if(themenangebote && (ich || inCourse)){
            veranstaltungen = Launcher.gui.addElement(placeholder,"/fxml/Veranstaltungen_Profil.fxml");
            veranstaltungen.profilView = this;
            veranstaltungen = null;
            themenangebote = false;
        }else if (themenangebote == false && ich || inCourse){
            angebote = Launcher.gui.addElement(placeholder,"/fxml/Themenangebote_Profil.fxml");
            angebote.profilView = this;
            angebote = null;
            themenangebote = true;
        }
        else {
            eigenesProfil_onlyVeranstaltungen = Launcher.gui.addElement(placeholder,"/fxml/EigenesProfil_Veranstaltung.fxml");
            eigenesProfil_onlyVeranstaltungen.profilView = this;
            eigenesProfil_onlyVeranstaltungen = null;
        }
    }

    public void nextScene() {
        Launcher.gui.addOverNavigation("/fxml/ThemaHinzufügen.fxml");
    }

}
