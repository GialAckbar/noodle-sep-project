/*

Startet und schließt den server und sagt dem DatabaseManager wann er eine Verbindung herstellen und beenden soll

RequestHandler werden in RequestHandlers.java hinzugefügt

 */

package server;

import server.db.DatabaseManager;
import org.eclipse.jetty.server.Server;
import server.handler.RequestHandlers;
import server.mailing.MailReminderTask;
import server.mailing.ReminderThread;

public class Controller {
    private int port;

    private Server server;

    ReminderThread reminderThread = null;

    public Controller(int port){
        this.port = port;
        server = new Server(port);
    }

    public void start() throws Exception{
        DatabaseManager dbManager = new DatabaseManager();

        server.setHandler(RequestHandlers.GetRequestHandlers(dbManager.getConnection()));
        server.start();

        reminderThread = new ReminderThread(dbManager.connect());
        reminderThread.start();
    }

    public void stop(){
        try {
            server.join();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        if (reminderThread != null) {
            reminderThread.close();
            System.out.println("MailReminderTask stopped");
        }
         /*
            Close database connection.

         */

    }

}
