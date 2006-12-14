/*
 * ome.formats.testclient.LoginHandler
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.importer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.prefs.Preferences;

import javax.ejb.EJBAccessException;
import javax.swing.JOptionPane;

import ome.formats.OMEROMetadataStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import ome.api.IPojos;
//import ome.model.core.Pixels;
//import ome.system.ServiceFactory;

/**
 * ImageExporter is master file format exporter for all supported formats and
 * exports the files to an OMERO database
 * 
 * @author Brian Loranger brain at lifesci.dundee.ac.uk
 * @basedOnCodeFrom Curtis Rueden ctrueden at wisc.edu
 */
public class LoginHandler
{

    private String             username;

    private String             password;

    private String             port;

    private String             server;

    private Main         viewer;

    private static Log         log       = LogFactory
                                                 .getLog(LoginHandler.class);

    private Preferences        userPrefs = Preferences
                                                 .userNodeForPackage(LoginHandler.class);

    private OMEROMetadataStore store;

    LoginHandler(Main viewer)
    {
        tryLogin(viewer);
    }

    public void tryLogin(Main v)
    {
        this.viewer = v;
        viewer.enableMenus(false);

        // Display the initial login dialog
        displayLoginDialog(viewer);
        
        viewer.statusBar.setStatusIcon("gfx/server_trying16.png",
        "Trying to connect.");
        try
        {
            if (!isValidLogin())
            {
                viewer.statusBar.setStatusIcon("gfx/error_msg16.png",
                        "Incorrect username/password. Server login failed, please try to "
                                + "log in again.");

                JOptionPane.showMessageDialog(viewer,
                        "Incorrect username/password. Server login \nfailed, please "
                                + "try to log in again.");
                viewer.appendToOutput("> Login failed. Try to relog.\n");
                viewer.enableMenus(true);
                viewer.loggedIn = false;
                return;
            }
        } catch (Exception e)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            log.info(sw);

            viewer.statusBar.setStatusIcon("gfx/error_msg16.png",
                    "Server connection failure. Please try to login again.");

            JOptionPane
                    .showMessageDialog(
                            viewer,
                            "The application failed to log in. The server " 
                            + "\nhostname or port may be wrong or the server "
                            + "\nmay be offline.\n\nPlease try again.");
            viewer.appendToOutput("> Login failed. Try to relog.\n");
            viewer.enableMenus(true);
            viewer.loggedIn = false;
            return;
        }

        viewer.appendToOutput("> Login Successful.\n");
        viewer.enableMenus(true);
        viewer.setImportEnabled(true);
        viewer.loggedIn = true;
        viewer.statusBar.setStatusIcon("gfx/server_connect16.png",
                "Server connected.");

    }

    private void displayLoginDialog(Main viewer)
    {
        LoginDialog dialog = new LoginDialog(viewer, "Login", true);

        //if (dialog.cancelled == true) return;
        username = dialog.username;
        password = dialog.password;
        server = dialog.server;
        port = dialog.port;

        userPrefs.put("username", username);
        // userPrefs.put("password", password); // save the password
        userPrefs.put("server", server);
        userPrefs.put("port", port);
    }

    private boolean isValidLogin() throws Exception
    {
        try
        {
            store = new OMEROMetadataStore(username, password, server, port);
            store.getProjects();
            
        } catch (EJBAccessException e)
        {
            return false;
        }

        return true;
    }

    public OMEROMetadataStore getMetadataStore()
    {
        return store;
    }
}
