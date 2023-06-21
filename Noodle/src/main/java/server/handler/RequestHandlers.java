/*

Hier werden die RequestHandler hinzugefügt (Funktion: GetRequestHandlers() )
Die Funktion wird vom Server Controller aufgerufen, der die RequestHandler dem server hinzufügt
 */

package server.handler;

import org.eclipse.jetty.server.handler.HandlerCollection;

import java.sql.Connection;

public class RequestHandlers {
    public static HandlerCollection GetRequestHandlers(Connection con) {
        HandlerCollection handlers = new HandlerCollection();
        handlers.addHandler(new NavigationHandler(con));


        handlers.addHandler(new ChatHandler(con));
        handlers.addHandler(new TestHandler());
        handlers.addHandler(new RegisterHandler(con));
        handlers.addHandler(new LoginHandler(con));
        handlers.addHandler(new AddVeranstaltungHandler());
        handlers.addHandler(new DownloadHandler());
        handlers.addHandler(new KursteilnehmerHandler(con));
        handlers.addHandler(new courseListHandler(con));
        handlers.addHandler(new ProfilDatenHandler(con));
        handlers.addHandler(new ProfilHinzufügenHandler());
        handlers.addHandler(new KursAnzeigenHandler(con));
        handlers.addHandler(new ProfilBearbeitenHandler(con));
        handlers.addHandler(new UpdateKursHandler());
        handlers.addHandler(new ProfilÄnderungenSpeichernHandler());
        handlers.addHandler(new appointmentHandler(con));
        handlers.addHandler(new TestTimeHandler(con));
        handlers.addHandler(new FreundHinzufügenFreundeslisteHandler(con));
        handlers.addHandler(new ProjektgruppensucheHandler(con));
        handlers.addHandler(new UpdateElementsHandler(con));
        handlers.addHandler(new QuizAufrufenHandler(con));
        handlers.addHandler(new QuizTeilnehmerHandler(con));
        handlers.addHandler(new QuizStudentSpeichern(con));
        handlers.addHandler(new QuizSpeichernHandler(con));
        handlers.addHandler(new QuizLehrerAufrufenHandler(con));
        handlers.addHandler(new LiteraturverzeichnisBibTexHandler(con));
        handlers.addHandler(new BewertungHandler(con));
        handlers.addHandler(new feedbackHandler(con));
        handlers.addHandler(new feedbackTestHandler(con));
        return handlers;
    }
}
