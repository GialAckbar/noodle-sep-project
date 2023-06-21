package shared.accounts;

public class UseridMitMail {
    int userid = -1;
    String mail = "";

    public UseridMitMail() {
    }

    public UseridMitMail(int userid, String mail) {
        this.userid = userid;
        this.mail = mail;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        if (mail != null) {
            this.mail = mail;
        }
    }
}
