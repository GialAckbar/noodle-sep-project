package shared.accounts;

public class LoginDaten {
    private String mail = null;
    private int matrikelnummer = -1;
    private String passwort = null;

    public LoginDaten (String mail, int matrikelnummer, String passwort) {
        setMail(mail);
        setMatrikelnummer(matrikelnummer);
        setPasswort(passwort);
    }
    public LoginDaten (String mail, String passwort) {
        setMail(mail);
        setPasswort(passwort);
    }
    public LoginDaten (int matrikelnummer, String passwort) {
        setMatrikelnummer(matrikelnummer);
        setPasswort(passwort);
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public int getMatrikelnummer() {
        return matrikelnummer;
    }

    public void setMatrikelnummer(int matrikelnummer) {
        this.matrikelnummer = matrikelnummer;
    }

    public String getPasswort() {
        return passwort;
    }

    public void setPasswort(String passwort) {
        this.passwort = passwort;
    }
}
