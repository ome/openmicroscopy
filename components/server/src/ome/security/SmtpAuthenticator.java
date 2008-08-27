/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class SmtpAuthenticator extends Authenticator {

    private String username = "";
    private String password = "";

    public SmtpAuthenticator(String username, String password) {
        super();
        if (!"".equals(username) && !"".equals(password)) {
            this.username = username;
            this.password = password;
        }
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        if (this.username != "" && this.password != "") {
            return new PasswordAuthentication(username, password);
        } else {
            return super.getPasswordAuthentication();
        }
    }

}
