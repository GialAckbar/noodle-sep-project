/*

Serverlauncher erstellt einen controller und lässt ihn starten und stoppen

TODO: lokale oder internetverbindung zur Datenbank auswählen

 */

package server;

import server.mailing.Mail;

public class Launcher {
    public static void main(String[] args) {
        System.out.println("Server");
//        Mail.send("psk09378@yuoia.com","test","contenttest");
        try{
            Controller controller = new Controller(1337);
            controller.start();
            controller.stop();

        }catch (Exception e){
            System.err.println("Error, could not start Server!");
            e.printStackTrace();
        }

    }

}
