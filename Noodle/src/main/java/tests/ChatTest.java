import client.Launcher;
import client.RequestHandler;
import client.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import shared.ChatNachricht;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// -------------------------------------------------------------------------------
//             Modultests funktionieren NUR mit der Online-Datenbank!
// -------------------------------------------------------------------------------

class ChatTest {

    String[] messages;

    @Test
    void testSENDER() {
        messages = null;
        Launcher.requestHandler = new RequestHandler("127.0.0.1", 1337);
        Random random = new Random();
        int r = random.nextInt(100);
        messages = senderSafeTest(136, 136, 2,"Test " + r);
        if (messages != null) {
            int a = Integer.parseInt(messages[0]);
            int b = Integer.parseInt(messages[1]);
            String message = "b - a = " + (b-a) + " (Soll: 1) und 'Test " + r + "' == '" + messages[2] + "' (Soll: 'Test " + r + "' == 'Test " + r + ")";
            Assertions.assertTrue(a + 1 == b && messages[2].equals("Test " + r), message);
        } else Assertions.fail("Serverseitiger Fehler (Vlt. keine Verbindung?)");
    }

    @Test
    void testRECIEVER() {
        messages = null;
        Launcher.requestHandler = new RequestHandler("127.0.0.1", 1337);
        Random random = new Random();
        int r = random.nextInt(100);
        messages = senderSafeTest(157, 136, 2,"Test " + r);
        if (messages != null) {
            int a = Integer.parseInt(messages[0]);
            int b = Integer.parseInt(messages[1]);
            String message = "b - a = " + (b-a) + " (Soll: 1) und 'Test " + r + "' == '" + messages[2] + "' (Soll: 'Test " + r + "' == 'Test " + r + ")";
            Assertions.assertTrue(a + 1 == b && messages[2].equals("Test " + r), message);
        } else Assertions.fail("Serverseitiger Fehler (Vlt. keine Verbindung?)");
    }

    @Test
    void testGROUP() {
        messages = null;
        Launcher.requestHandler = new RequestHandler("127.0.0.1", 1337);
        Random random = new Random();
        int r = random.nextInt(100);
        messages = senderSafeTest(135, 140, 158, 12,"Test " + r);
        if (messages != null) {
            int a = Integer.parseInt(messages[0]);
            int b = Integer.parseInt(messages[1]);
            int c = Integer.parseInt(messages[2]);
            int d = Integer.parseInt(messages[3]);
            String message = "Fehlermeldung sprengt den Rahmen! Geh mit einem Debugger durch";
            Assertions.assertTrue((a + 1 == c) && (b + 1 == d) && messages[4].equals("Test " + r) && messages[5].equals("Test " + r), message);
        } else Assertions.fail("Serverseitiger Fehler (Vlt. keine Verbindung?)");
    }

    public String[] senderSafeTest(int recieverid, int senderid,  int chatid, String nachricht) {
        String args = "?chatid=" + chatid + "&userid=" + recieverid;
        String args2 = "?chatid=" + chatid + "&userid=" + senderid;
        Response PreResponse, responseSent, response;
        PreResponse = Launcher.requestHandler.request(RequestHandler.Art.GETTEST,"/chat/receive" + args, shared.Chat.class);
        if (PreResponse != null) {
            responseSent = Launcher.requestHandler.request(RequestHandler.Art.POSTTEST,"/chat/send" + args2, nachricht);
            if (responseSent != null) {
                response = Launcher.requestHandler.request(RequestHandler.Art.GETTEST,"/chat/receive" + args, shared.Chat.class);
                if (response != null) {
                    if (PreResponse.statusCode == 200 && responseSent.statusCode == 200 && response.statusCode == 200) {
                        shared.Chat preChat = (shared.Chat) PreResponse.getElement();
                        shared.Chat chat = (shared.Chat) response.getElement();
                        List<ChatNachricht> messages = chat.getMessages();
                        List<String> msgs = new ArrayList<>();

                        for(ChatNachricht x : messages) {
                            msgs.add(x.getNachricht());
                        }

                        return new String[] {
                                String.valueOf(preChat.getMessages().size()),
                                String.valueOf(chat.getMessages().size()),
                                msgs.get(msgs.size()-1)
                        };
                    }
                }
            }
        }
        return null;
    }

    public String[] senderSafeTest(int recieverid, int recieverid2, int senderid,  int chatid, String nachricht) {
        String args = "?chatid=" + chatid + "&userid=" + recieverid;
        String args2 = "?chatid=" + chatid + "&userid=" + senderid;
        String args3 = "?chatid=" + chatid + "&userid=" + recieverid2;
        Response PreResponse, PreResponse2, responseSent, response2, response;
        PreResponse = Launcher.requestHandler.request(RequestHandler.Art.GETTEST,"/chat/receive" + args, shared.Chat.class);
        if (PreResponse != null) {
            PreResponse2 = Launcher.requestHandler.request(RequestHandler.Art.GETTEST,"/chat/receive" + args3, shared.Chat.class);
            if (PreResponse2 != null) {
                responseSent = Launcher.requestHandler.request(RequestHandler.Art.POSTTEST,"/chat/send" + args2, nachricht);
                if (responseSent != null) {
                    response = Launcher.requestHandler.request(RequestHandler.Art.GETTEST,"/chat/receive" + args, shared.Chat.class);
                    if (response != null) {
                        response2 = Launcher.requestHandler.request(RequestHandler.Art.GETTEST,"/chat/receive" + args3, shared.Chat.class);
                        if (response2 != null) {
                            if (PreResponse.statusCode == 200 && PreResponse2.statusCode == 200 && responseSent.statusCode == 200 && response.statusCode == 200 && response2.statusCode == 200) {
                                shared.Chat preChat = (shared.Chat) PreResponse.getElement();
                                shared.Chat preChat2 = (shared.Chat) PreResponse2.getElement();
                                shared.Chat chat = (shared.Chat) response.getElement();
                                shared.Chat chat2 = (shared.Chat) response2.getElement();
                                List<ChatNachricht> messages = chat.getMessages();
                                List<ChatNachricht> messages2 = chat2.getMessages();
                                List<String> msgs = new ArrayList<>();
                                List<String> msgs2 = new ArrayList<>();

                                for(ChatNachricht x : messages) {
                                    msgs.add(x.getNachricht());
                                }

                                for(ChatNachricht x : messages2) {
                                    msgs2.add(x.getNachricht());
                                }

                                return new String[] {
                                        String.valueOf(preChat.getMessages().size()),
                                        String.valueOf(preChat2.getMessages().size()),
                                        String.valueOf(chat.getMessages().size()),
                                        String.valueOf(chat2.getMessages().size()),
                                        msgs.get(msgs.size()-1),
                                        msgs2.get(msgs2.size()-1)
                                };
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}