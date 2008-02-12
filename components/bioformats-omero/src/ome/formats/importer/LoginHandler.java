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
 *------------------------------------------------------------------------------
 */

package ome.formats.importer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.ejb.EJBAccessException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import ome.formats.OMEROMetadataStore;
import ome.formats.importer.util.Actions;

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
public class LoginHandler implements IObservable, PropertyChangeListener
{
    
    ArrayList<IObserver> observers = new ArrayList<IObserver>();
    
    public volatile JFrame      f;
    
    private boolean            center;
    
    private String             username;

    private String             password;

    private String             port;

    private String             server;

    private Main               viewer;

    private static Log         log       = LogFactory
                                                 .getLog(LoginHandler.class);

    private Preferences        userPrefs = Preferences
                                                 .userNodeForPackage(LoginHandler.class);

    private OMEROMetadataStore store;
    
    public LoginDialog         dialog;
    
    public LoginFrame          frame;

    private boolean modal;
    

    LoginHandler(Main viewer, boolean modal, boolean center)
    {
        this.viewer = viewer;
        this.center = center;
        this.modal = modal;
        
        viewer.enableMenus(false);
        boolean cancelled = displayLoginDialog(viewer, modal);
        
        if (modal == true && cancelled == true)
        {
            loginCancelled();
        }
        
        if (modal == true && cancelled == false)
        {
            tryLogin();
        }
        
    }

    public void tryLogin()
    {
        new Thread()
        {
            public void run()
            {
                SplashWindow.disposeSplash();
                viewer.setVisible(true);

                if (!modal)
                {
                    username = frame.username;
                    password = frame.password;
                    server = frame.currentServer;
                    port = frame.port;
                    frame.updateServerList(server);                    
                } else
                {
                    username = dialog.username;
                    password = dialog.password;
                    server = dialog.currentServer;
                    port = dialog.port;
                    dialog.updateServerList(server);                    
                }


                userPrefs.put("username", username);
                // userPrefs.put("password", password); // save the password
                userPrefs.put("server", server);
                userPrefs.put("port", port);

                viewer.statusBar.setStatusIcon("gfx/server_trying16.png",
                "Trying to connect to " + server);
                viewer.statusBar.setProgress(true, -1, "connecting....");
                try
                {
                    if (!isValidLogin())
                    {
                        viewer.statusBar.setProgress(false, 0, "");
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

                    viewer.statusBar.setProgress(false, 0, "");
                    viewer.statusBar.setStatusIcon("gfx/error_msg16.png",
                    "Server connection to " + server +" failed. " +
                            "Please try again.");

                    JOptionPane
                    .showMessageDialog(
                            viewer,
                            "\nThe application failed to log in. The hostname may be wrong or " +
                            "\nthe server may be offline." +
                            "\n\nPlease try again.");
                    viewer.appendToOutput("> Login failed. Try to relog.\n");
                    viewer.enableMenus(true);
                    viewer.loggedIn = false;
                    return;
                }

                viewer.statusBar.setProgress(false, 0, "");
                viewer.appendToOutput("> Login Successful.\n");
                viewer.enableMenus(true);
                viewer.setImportEnabled(true);
                viewer.loggedIn = true;
                notifyObservers("LOGGED_IN", null);
                // if this fails, using the old server without repositorySpace
                try {
                    long freeSpace = store.getRepositorySpace();
                    NumberFormat formatter = NumberFormat.getInstance(Locale.US);
                    String freeMB = formatter.format(freeSpace/1000);                
                    viewer.statusBar.setStatusIcon("gfx/server_connect16.png",
                            "Connected to " + server + ". Free space: " + 
                            freeMB + " MB.");
                } catch (Exception e) 
                {
                    viewer.statusBar.setStatusIcon("gfx/server_connect16.png",
                            "Connected to " + server + ".");
                }
            }
        }.start();
    }

    void loginCancelled() {
        viewer.loggedIn = false;
        viewer.enableMenus(true);
        SplashWindow.disposeSplash();
        viewer.setVisible(true);
    }
    
    private boolean displayLoginDialog(Main viewer, boolean modal)
    {
        if (modal == true)
        {
            dialog = new LoginDialog(viewer, viewer, "Login", modal, center);
            dialog.setAlwaysOnTop(true);
            if (dialog.cancelled == true) return true;
        } else {

            frame = new LoginFrame(viewer, viewer, "Login", modal, center);
            frame.addPropertyChangeListener(this);    
            
        }

        return false;
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

    public void propertyChange(PropertyChangeEvent ev)
    {
        String prop = ev.getPropertyName();
        if (prop.equals(Actions.LOGIN))
        {
            tryLogin();
        }
        if (prop.equals(Actions.LOGIN_CANCELLED))
        {
            loginCancelled();
        }
    }

    // Observable methods
    
    public boolean addObserver(IObserver object)
    {
        return observers.add(object);
    }
    
    public boolean deleteObserver(IObserver object)
    {
        return observers.remove(object);
        
    }

    public void notifyObservers(Object message, Object[] args)
    {
        for (IObserver observer:observers)
        {
            observer.update(this, message, args);
        }
    }
}
