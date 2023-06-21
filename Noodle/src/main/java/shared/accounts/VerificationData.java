package shared.accounts;

public class VerificationData {
    int userid = -1;
    int verificationCode = -1;
    String verificationToken = "";

    public VerificationData() {

    }
    public VerificationData (int userid, int verificationCode, String verificationToken) {
        this.userid = userid;
        this.verificationCode = verificationCode;
        this.verificationToken = verificationToken;
    }
    public void setUserid(int userid) {
        this.userid = userid;
    }
    public int getUserid () {
        return userid;
    }
    public void setVerificationCode (int verificationCode) {
        this.verificationCode = verificationCode;
    }
    public int getVerificationCode () {
        return verificationCode;
    }
    public void setVerificationToken (String verificationToken) {
        this.verificationToken = verificationToken;
    }
    public String getVerificationToken() {
        return verificationToken;
    }
}
