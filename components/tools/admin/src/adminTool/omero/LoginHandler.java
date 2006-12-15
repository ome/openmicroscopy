/*
 * ome.formats.testclient.LoginHandler
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package src.adminTool.omero;

import java.awt.Point;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.prefs.Preferences;

import javax.ejb.EJBAccessException;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import src.adminTool.main.MainPanel;
import src.adminTool.ui.ImageFactory;
import src.adminTool.ui.LoginDialog;

/**
 * ImageExporter is master file format exporter for all supported formats and
 * exports the files to an OMERO database
 * 
 * @author Brian Loranger brain at lifesci.dundee.ac.uk
 * @basedOnCodeFrom Curtis Rueden ctrueden at wisc.edu
 */
public class LoginHandler {

    private String username;

    private String password;

    private String port;

    private String server;

    private static Log log = LogFactory.getLog(LoginHandler.class);

    private Preferences userPrefs = Preferences
            .userNodeForPackage(LoginDialog.class);

    private MainPanel view;

    private OMEROMetadataStore store;

    public LoginHandler(MainPanel mainview) {
        tryLogin(mainview);
    }

    public void tryLogin(MainPanel v) {
        this.view = v;

        // Display the initial login dialog
        displayLoginDialog(view);

        view.statusBar.setStatusIcon(ImageFactory.get().image(
                ImageFactory.SERVER_CONNECT_TRYING), "Trying to connect.");
        try {
            if (!isValidLogin()) {
                view.statusBar.setStatusIcon(ImageFactory.get().image(
                        ImageFactory.SERVER_CONNECT_FAILED),
                        "Incorrect username/password. Server login failed, please try to "
                                + "log in again.");

                JOptionPane.showMessageDialog(view,
                        "Incorrect username/password. Server login \nfailed, please "
                                + "try to log in again.");
                view.loggedIn = false;
                return;
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            log.info(sw);

            view.statusBar.setStatusIcon(ImageFactory.get().image(
                    ImageFactory.SERVER_CONNECT_FAILED),
                    "Server connection failure. Please try to login again.");

            JOptionPane
                    .showMessageDialog(
                            view,
                            "The application failed to login with an exception "
                                    + "\n(which is displayed on the debug window). \nYour "
                                    + "server hostname/port may be wrong, or \nthe server "
                                    + "may be offline/inaccessable.\n\nPlease try to log "
                                    + "in again.");
            view.loggedIn = false;
            return;
        }
        userPrefs.put("savedUserName", username);
        userPrefs.put("savedHostName", server);
        userPrefs.put("savedPortNo", port);

        view.loggedIn = true;
        view.statusBar.setStatusIcon(ImageFactory.get().image(
                ImageFactory.SERVER_CONNECTED), "Server connected.");

    }

    private void displayLoginDialog(MainPanel viewer) {
        Point location = viewer.getLocation();
        location.x += viewer.getWidth() - 180;
        location.y += viewer.getHeight() - 110;
        LoginDialog dialog = new LoginDialog("Login", true, location);

        // if (dialog.cancelled == true) return;
        username = dialog.username;
        password = dialog.password;
        server = dialog.server;
        port = dialog.port;

        if (username != null)
            userPrefs.put("username", username);
        if (server != null)
            userPrefs.put("server", server);
        if (port != null)
            userPrefs.put("port", port);
    }

    private boolean isValidLogin() throws Exception {
        try {
            store = new OMEROMetadataStore(username, password, server, port);

        } catch (EJBAccessException e) {
            return false;
        }

        return true;
    }

    public OMEROMetadataStore getMetadataStore() {
        return store;
    }

    public String getUsername() {
        return username;
    }
}
