/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import ome.formats.Main;
import ome.system.UpgradeCheck;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class which configures the Import.
 * 
 * @since Beta4.1
 */
public class ImportConfig {

    public final static String READERS_KEY = "omero.import.readers";

    private final static Log log = LogFactory.getLog(ImportConfig.class);

    private final static String _save_directory = System.getProperty("user.home") + File.separator + "omero" + File.separator + "log";

    private final static String _importer_log = "importer.log";
    
    private final static String _home_url = 
        "http://trac.openmicroscopy.org.uk/shoola/wiki/OmeroInsightGettingStarted";

    private final static String _initial_url = "http://mage.openmicroscopy.org.uk/qa/initial/";

    private static boolean configured = false;
    
    private final Preferences _prefs = Preferences.userNodeForPackage(Main.class);

    private final String[] args;
    
    // STATE

    private boolean continueOnError;
    private String readersPath;
    private String hostname;
    private String username;
    private String password;
    private String sessionKey;
    private int port;
    private Class<?> targetClass;
    private long targetId;
    private String name;
    private String description;

    /**
     * Main constructor which iterates over all the paths calling
     * {@link #walk(File, Collection)}.
     * 
     * @param paths
     * @param verbose
     * @throws IOException
     */
    public ImportConfig() {
        this(null);
    }

    public ImportConfig(final String[] args) {

        // Load up the main ini file
        // ini = IniFileLoader.getIniFileLoader(args);
        // ini.updateFlexReaderServerMaps();

        configured = true;

        
        if (args != null) {
            this.args = new String[args.length];
            System.arraycopy(args, 0, this.args, 0, args.length);
        } else {
            this.args = null;
        }
        
        isUpgradeRequired();
    }
    

    public void save() {
        throw new UnsupportedOperationException("NYI");
    }
    

    /**
     * Check online to see if this is the current version
     */
    public boolean isUpgradeRequired()
    {
        ResourceBundle bundle = ResourceBundle.getBundle("omero");
        String version = bundle.getString("omero.version");
        String url = bundle.getString("omero.upgrades.url");
        UpgradeCheck check = new UpgradeCheck(url, version, "importer"); 
        check.run();
        return check.isUpgradeNeeded();
    }

    /**
     * User-documentation page.
     */
    public String getHomeUrl() {
        return _home_url;
    }



    


    public String getReadersPath() {
        if (readersPath == null) {
            readersPath = System.getProperty(READERS_KEY);
        }

        if (readersPath == null) {
            String readersDirectory = System.getProperty("user.dir")
                    + File.separator + "config";
            String readersFile = readersDirectory + File.separator
                    + "importer_readers.txt";
            File rFile = new File(readersFile);
            if (rFile.exists()) {
                readersPath = rFile.getAbsolutePath();
            } else {
                readersPath = "importer_readers.txt";
            }
        }
        return readersPath;
    }

    public boolean canLogin() {
        if (((username == null || password == null) && sessionKey == null)
                || hostname == null) {
            return false;
        }
        return true;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSessionkey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    public void setTargetId(long targetId) {
        this.targetId = targetId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setContinueOnErrors(boolean continueOnError) {
        this.continueOnError = continueOnError;
    }

    public void setReadersPath(String readersPath) {
        this.readersPath = readersPath;
    }


    public String getAppTitle() {
        // TODO Auto-generated method stub
        return null;
    }


    public void setUIBounds(Rectangle bounds) {
        // TODO Auto-generated method stub
        
    }


    public String getVersionNumber() {
        // TODO Auto-generated method stub
        return null;
    }


    public String getServerPort() {
        // TODO Auto-generated method stub
        return null;
    }


    public String getUserSettingsDirectory() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean getUseQuaqua() {
        // TODO Auto-generated method stub
        return false;
    }

    public String getEmail() {
        return _prefs.get("userEmail", "");
    }

    public void setEmail(String emailText) {
        _prefs.put("userEmail", emailText);
    }

    public String getFeedbackUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean getSendFiles() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setSendFiles(boolean selected) {
        // TODO Auto-generated method stub
        
    }

    public Rectangle getUIBounds() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getLogFileName() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getSaveDirectory() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getTokenUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getUploaderUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    public File getSavedDirectory() {
        String savedDirectory = _prefs.get("savedDirectory", "");
        if (savedDirectory.equals("") || !(new File(savedDirectory).exists()))
        {
            return null;
        }
        return new File(savedDirectory);

    }

    public void setSavedDirectory(String path) {
        _prefs.put("savedDirectory", path);

    }

}
