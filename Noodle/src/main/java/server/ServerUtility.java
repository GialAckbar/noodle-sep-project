package server;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shared.accounts.UseridMitToken;

public class ServerUtility {
    public static UseridMitToken extractUseridAndToken (HttpServletRequest request) {
        int userid = -1;
        String token = null;

        try {
            token = request.getParameter("token");
            userid = Integer.parseInt(request.getParameter("userid"));
        } catch (NumberFormatException e) {
            return null;
        }
        return new UseridMitToken(userid, token);
    }
}
