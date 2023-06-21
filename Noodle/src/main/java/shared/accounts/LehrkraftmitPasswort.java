package shared.accounts;

import shared.Lehrende;

public class LehrkraftmitPasswort {
    private Lehrende lehrkraft = null;
    private String passwort = "";

    public LehrkraftmitPasswort(Lehrende lehrkraft, String passwort) {
        setLehrkraft(lehrkraft);
        setPasswort(passwort);
    }

    public Lehrende getLehrkraft() {
        return lehrkraft;
    }

    public void setLehrkraft(Lehrende lehrkraft) {
        this.lehrkraft = lehrkraft;
    }

    public String getPasswort() {
        return passwort;
    }

    public void setPasswort(String passwort) {
        this.passwort = passwort;
    }
}
