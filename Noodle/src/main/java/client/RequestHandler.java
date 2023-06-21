package client;

import com.google.gson.Gson;
import com.sun.jdi.connect.LaunchingConnector;
import shared.*;
import shared.utility.GsonUtility;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class RequestHandler {

    public enum Art {
        POST,
        GET,
        DELETE,
        GETTEST,
        POSTTEST
    }

    String ip;
    int port;
    HttpClient client;

    String url;

   public RequestHandler(String ip, int port){
       this.ip = ip;
       this.port = port;
       this.client = HttpClient.newHttpClient();
       this.url = "http://" + ip + ":" + port;
   }

   public <T> Response request(Art art, String endpoint, T body, Class className){
       if(art == Art.GET || art == Art.DELETE){
           System.out.println("Bei GET und DELETE requests darf kein body vorhanden sein!");
           return new Response(400,null);
       }
       HttpRequest req = HttpRequest.newBuilder()
               .uri(URI.create(url + endpointWithToken(endpoint)))
               .POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(body)))
               .build();
       HttpResponse<String> res;
       try{
           res = client.send(req,HttpResponse.BodyHandlers.ofString());
           Response response = new Response(res.statusCode(), GsonUtility.getGson().fromJson(res.body(), className));
           System.out.println(res.body());
//           Response response = new Response(res.statusCode(),new Gson().fromJson(res.body(), className));
           return response;

       }catch (Exception e){
           e.printStackTrace();
       }

       return null;

    }



    public <T> Response request(Art art, String endpoint, T body){
        if(art == Art.GET || art == Art.DELETE){
            System.out.println("Bei GET und DELETE requests darf kein body vorhanden sein!");
            return new Response(400,null);
        }
        if(art == Art.POST) {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url + endpointWithToken(endpoint)))

                    .POST(HttpRequest.BodyPublishers.ofString(GsonUtility.getGson().toJson(body)))
                    .build();
            HttpResponse<String> res;
            System.out.println("ich bin gson:");
            System.out.println(GsonUtility.getGson().toJson(body));
            try {
                res = client.send(req, HttpResponse.BodyHandlers.ofString());
                Response response = new Response(res.statusCode());
                return response;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (art == Art.POSTTEST){
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url + endpoint))
                    .POST(HttpRequest.BodyPublishers.ofString(GsonUtility.getGson().toJson(body)))
                    .build();
            HttpResponse<String> res;
            System.out.println(GsonUtility.getGson().toJson(body));
            try {
                res = client.send(req, HttpResponse.BodyHandlers.ofString());
                Response response = new Response(res.statusCode());
                return response;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;

    }

    private String endpointWithToken(String endpoint){
       System.out.println(Launcher.useridMitToken);
       if(Launcher.useridMitToken != null){
           System.out.println("userid + token");
           if(endpoint.contains("?")){
               return endpoint + "&userid=" + Launcher.useridMitToken.getUserid() + "&token=" + Launcher.useridMitToken.getToken();

           }else{
               return endpoint + "?userid=" + Launcher.useridMitToken.getUserid() + "&token=" + Launcher.useridMitToken.getToken();
           }

       }
        return endpoint;
    }

    public Response request(Art art, String endpoint, Class className) {
        HttpRequest req = null;
        switch (art){
            case POST:
                System.out.println("Bei Post requests muss ein body vorhanden sein!");
                return new Response(400,null);
            case GET:
                req = HttpRequest.newBuilder()
                        .uri(URI.create(url + endpointWithToken(endpoint)))
                        .GET()
                        .build();
                break;
            case GETTEST:
                req = HttpRequest.newBuilder()
                        .uri(URI.create(url + endpoint))
                        .GET()
                        .build();
                break;
            case DELETE:
                req = HttpRequest.newBuilder()
                        .uri(URI.create(url + endpointWithToken(endpoint)))
                        .DELETE()
                        .build();
                break;
        }

        HttpResponse<String> res;
        try{
            res = client.send(req,HttpResponse.BodyHandlers.ofString());
//            System.out.println(res.statusCode());
//            System.out.println(res.body());
//            System.out.println(Launcher.useridMitToken.getToken());
//            System.out.println(Launcher.useridMitToken.getUserid());

            Response response = new Response(res.statusCode(), GsonUtility.getGson().fromJson(res.body(), className));
            System.out.println(res.body());
//            Response response = new Response(res.statusCode(),new Gson().fromJson(res.body(), className));
            return response;

        }catch (Exception e){
            e.printStackTrace();
        }

        return null;

    }

   public Response request(Art art, String endpoint) {
        HttpRequest req = null;
        switch (art){
            case POST:
                System.out.println("Bei Post requests muss ein body vorhanden sein!");
                return new Response(400,null);
            case GET:
                System.out.println("Bei GET requests muss ein className vorhanden sein!");
                return new Response(400,null);
            case DELETE:
                req = HttpRequest.newBuilder()
                        .uri(URI.create(url + endpointWithToken(endpoint)))
                        .DELETE()
                        .build();
                break;
        }

        HttpResponse<String> res;
        try{
            res = client.send(req,HttpResponse.BodyHandlers.ofString());
            System.out.println(res);
            Response response = new Response(res.statusCode());
            return response;

        }catch (Exception e){
            e.printStackTrace();
        }

        return null;

    }

}
