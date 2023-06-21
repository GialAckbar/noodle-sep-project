package server.handler;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import shared.*;

import java.io.IOException;

public class TestHandler extends AbstractHandler {
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String[] splitTarget = target.split("/");
        
        if (baseRequest.getMethod().equals("GET") && splitTarget.length >= 2 && splitTarget[1].equals("testRequest")) {
            System.out.println("Get testRequest");
            System.out.println(target);

            response.setStatus(HttpServletResponse.SC_OK);

            response.getWriter().println(new Gson().toJson(new Student("a","b","c","d","e",1,"g",1,"i"),Student.class));

            baseRequest.setHandled(true);
        }
    }
}
