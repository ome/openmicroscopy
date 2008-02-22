package ome.security;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class SmtpAuthenticator extends Authenticator {
    private String username = "";
    private String password = "";

    public SmtpAuthenticator(String username, String password) {
        super();
        if(username!="" && password!="") {
            this.username = username;
            this.password = password;
        }
    }

    public PasswordAuthentication getPasswordAuthentication() {
        if(this.username!="" && this.password!="") {
            return new PasswordAuthentication(username, password);
        } else {
            return super.getPasswordAuthentication();
        }
    }
    
}
