package ome.admin.logic;

import ome.admin.data.ConnectionDB;

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
