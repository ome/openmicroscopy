/*
 * glencoe.importer.IniFileLoader
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2007 Brian W. Loranger
 *  This class loads in the default Importer.ini file 
 *  (or one specified from the command line when starting the app)
 *
 *------------------------------------------------------------------------------
 */
package ome.formats.importer.util;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import loci.formats.FormatException;
import loci.formats.in.FlexReader;
import ome.formats.importer.Version;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ini4j.IniFile;
import org.ini4j.IniFile.Mode;

/**
 * This class loads in the default importer.ini file (or one specified from the
 * command line when starting the app)
 * 
 * @author Brian W. Loranger
 */
public class IniFileLoader {

    private final static Log log = LogFactory.getLog(IniFileLoader.class);

    private final static String LOGDIR = System.getProperty("user.home")
            + File.separator + "omero" + File.separator + "log";

    private final static String LOGFILE = LOGDIR + File.separator
            + "importer.log";

    // Dynamic user settings
    private String userSettingsDirectory;
    private String userSettingsFile;
    private Preferences userPrefs;

    // Static config settings
    private String staticConfigDirectory;
    private String staticConfigFile;
    private Preferences staticPrefs;

    // ////////////// Class Intialization Section ////////////////

    public IniFileLoader(File userConfigFile) {
        staticConfigDirectory = System.getProperty("user.dir") + File.separator
                + "config";
        staticConfigFile = staticConfigDirectory + File.separator
                + "importer.config";

        File staticFile = staticFileOrTempDummy();
        try {
            staticPrefs = new IniFile(staticFile, Mode.RO);
        } catch (BackingStoreException e) {
            log.error(e);
            throw new RuntimeException(e);
        }

        // Set up user config file
        userSettingsDirectory = System.getProperty("user.home")
                + File.separator + "omero";

        if (!new File(userSettingsDirectory).exists()) {
            new File(userSettingsDirectory).mkdir();
        }

        if (userConfigFile == null)
            userConfigFile = new File(userSettingsDirectory + File.separator
                    + "importer.ini");

        try {
            userPrefs = new IniFile(userConfigFile, Mode.RW);
        } catch (BackingStoreException e) {
            log.error(e);
            throw new RuntimeException("Error accessing ini file", e);
        }
    }

    public void flushPreferences() {
        try {
            userPrefs.flush();
        } catch (BackingStoreException e) {
            log.error(e);
        }
    }

    // ////////////// [General] Section ////////////////

    public boolean getUseQuaqua() {
        return userPrefs.node("General").getBoolean("useQuaqua", true);
    }

    public String getLogFile() {
        return staticPrefs.node("General").get("logfile", LOGFILE);
    }
    
    public String getHomeUrl() {
        return staticPrefs.node("General").get("url", "http://trac.openmicroscopy.org.uk/shoola/wiki/OmeroInsightGettingStarted");
    }
    public String getAppTitle() {
        return staticPrefs.node("General").get("appTitle", "OMERO.importer");
    }

    public void setUseQuaqua(boolean b) {
        userPrefs.node("General").putBoolean("useQuaqua", b);
        this.flushPreferences();
    }

    public String getVersionNumber() {
        // return Main.versionNumber;
        return staticPrefs.node("General").get("appVersion",
                Version.versionNumber);
    }

    public Boolean isDebugConsole() {
        return staticPrefs.node("General").getBoolean("displayDebugConsole",
                true);
    }

    public String getServerPort() {
        return staticPrefs.node("General").get("port", "4063");
    }

    /**
     * Updates the Flex reader server maps from the configuration file.
     */
    public void updateFlexReaderServerMaps() {
        Preferences maps = userPrefs.node("FlexReaderServerMaps");
        try {
            for (String key : maps.keys()) {
                String mapValues = maps.get(key, null);
                log.info("Raw Flex reader map values: " + mapValues);
                if (mapValues == null) {
                    return;
                }
                for (String mapValue : mapValues.trim().split((";"))) {
                    try {
                        FlexReader.mapServer(key, mapValue);
                        log.info(String.format(
                                "Added Flex reader server map '%s' = '%s'.",
                                key, mapValue));
                    } catch (FormatException e) {
                        log
                                .warn(
                                        String
                                                .format(
                                                        "Unable to add Flex reader server map '%s' = '%s'",
                                                        key, mapValue), e);
                    }
                }
            }
        } catch (BackingStoreException e) {
            log.warn("Error updating Flex reader server maps.", e);
        }
    }

    // ////////////// [UI] Section ////////////////
    public Boolean isDebugUI() {
        return staticPrefs.node("UI").getBoolean("displayRedBorders", false);
    }

    // TODO: UI locations should handled multiple monitors

    public Rectangle getUIBounds() {

        Rectangle rect = new Rectangle();

        rect.width = userPrefs.node("UI").getInt("width", 980);
        rect.height = userPrefs.node("UI").getInt("height", 580);
        rect.x = userPrefs.node("UI").getInt("xOffset", 10);
        rect.y = userPrefs.node("UI").getInt("yOffset", 10);

        return rect;
    }

    public void setUIBounds(Rectangle bounds) {

        if (bounds.x < 0)
            bounds.x = 0;
        if (bounds.y < 0)
            bounds.y = 0;
        if (bounds.width < 100)
            bounds.width = 100;
        if (bounds.height < 100)
            bounds.height = 100;

        userPrefs.node("UI").putInt("width", bounds.width);
        userPrefs.node("UI").putInt("height", bounds.height);
        userPrefs.node("UI").putInt("xOffset", bounds.x);
        userPrefs.node("UI").putInt("yOffset", bounds.y);
    }

    public String getUploaderTokenURL() {
        return staticPrefs.node("Uploader").get("TokenURL",
                "http://mage.openmicroscopy.org.uk/qa/initial/");
    }

    public String getUploaderURL() {
        return staticPrefs.node("Uploader").get("URL",
                "http://mage.openmicroscopy.org.uk/qa/upload_processing/");
    }

    public String getBugTrackerURL() {
        return staticPrefs.node("Uploader").get("BugTrackerURL",
                "http://mage.openmicroscopy.org.uk/qa/upload_processing/");
    }

    /**
     * @return Returns the userSettingsDirectory.
     */
    public String getUserSettingsDirectory() {
        return userSettingsDirectory;
    }

    /**
     * To prevent exceptions when the configuration directory is not present we
     * create a temporary file with no values in it.
     */
    private File staticFileOrTempDummy() {
        File staticFile = new File(staticConfigFile);
        if (!staticFile.exists() || !staticFile.canRead()) {
            try {
                staticFile = File.createTempFile(".omero.importer", "ini");
                log.warn("Creating temporary ini file: "
                        + staticFile.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            staticFile.deleteOnExit();
        }
        return staticFile;
    }
}
