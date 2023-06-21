package shared.accounts;

public class UseridMitToken {
    private int userid = -1;
    private String token = "";

    public UseridMitToken (int userid, String token) {
        setUserid(userid);
        setToken(token);
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
