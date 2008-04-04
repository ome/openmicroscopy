/*
 * ome.admin.logic
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.admin.logic;

// Application-internal dependencies
import ome.admin.data.ConnectionDB;

/**
 * Delegate of password mangement.
 * 
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class PasswordManagerDelegator implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@link ome.admin.data.ConnectionDB}
     */
    private ConnectionDB db;

    /**
     * Creates a new instance of ITypesEnumManagerDelegate.
     */
    public PasswordManagerDelegator(String server, int port) {
        db = new ConnectionDB(server, port);
    }

    public void reportForgottenPassword(String omeName, String email) {
        db.reportForgottenPassword(omeName, email);
    }

    public boolean changeExpiredPassword(String omeName, String email,
            String oldPassword, String newPassword) {
        return false;
    }

}
