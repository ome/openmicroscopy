/*
 * ome.formats.importer.util.HtmlMessenger
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.importer.util;

import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import loci.formats.in.FlexReader;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ini4j.IniFile;
import org.ini4j.IniFile.Mode;

/**
 * This class loads in the default importer.ini file (or one specified from the
 * command line when starting the app)
 *
 * @author Brian W. Loranger
 */
public class IniFileLoader {

    private final static Logger log = LoggerFactory.getLogger(IniFileLoader.class);

    private final static String LOGDIR = System.getProperty("user.home")
            + File.separator + "omero" + File.separator + "log";

    /**
     * Public in order to configure LogAppenderProxy, but then the value
     * might as well be configured in the log4j.properties file
     * @see <a href="http://trac.openmicroscopy.org/ome/ticket/1479">Trac ticket #1479</a>
     */
    public final static String LOGFILE = LOGDIR + File.separator
            + "importer.log";

    // Dynamic user settings
    private String userSettingsDirectory;
    private Preferences userPrefs;

    // Static config settings
    private String staticConfigDirectory;
    private String staticConfigFile;
    private Preferences staticPrefs;

    // ////////////// Class Intialization Section ////////////////

    /**
     * Load given file
     *
     * @param userConfigFile the file to load
     */
    public IniFileLoader(File userConfigFile) {
        staticConfigDirectory = System.getProperty("user.dir") + File.separator
                + "config";
        staticConfigFile = staticConfigDirectory + File.separator
                + "importer.config";

        staticPrefs = staticPrefsOrNull();

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
            log.error(e.toString()); // slf4j migration: toString()
            //throw new RuntimeException("Error accessing ini file", e);
        }
    }

    /**
     * Flush preferences to disk
     */
    public void flushPreferences() {
        try {
            userPrefs.flush();
        } catch (BackingStoreException e) {
            log.error(e.toString()); // slf4j migration: toString()
        }
    }

    // ////////////// [General] Section ////////////////

    /**
     * Returns the level of debugging which should be set on {@link ImportConfig}
     * Any value lower than null will not call configureDebug.
     * @return the debug level
     */
    public int getDebugLevel() {
    	if (userPrefs == null) return -1;
        return userPrefs.node("General").getInt("debug", -1);
    }

    /**
     * @return if quaqua should be used on mac
     */
    public boolean getUseQuaqua() {
    	if (userPrefs == null) return true;
        return userPrefs.node("General").getBoolean("useQuaqua", true);
    }

    /**
     * @return location of log file
     */
    public String getLogFile() {

        return staticPref("General", "logfile", LOGFILE);
    }

    /**
     * @return URL for product feature list
     */
    public String getHomeUrl()
    {
        return staticPref("General", "url", "http://www.openmicroscopy.org/site/products/omero/feature-list");
    }

    /**
     * @return URL for community forums
     */
    public String getForumUrl()
    {
        return staticPref("General", "forumUrl", "http://www.openmicroscopy.org/community/");
    }

    /**
     * @return application title
     */
    public String getAppTitle()
    {
        return staticPref("General", "appTitle", "OMERO.importer");
    }
    
    /**
     * @return application title
     */
    public boolean getForceFileArchiveOn()
    {
        boolean toReturn = false;
        if (staticPrefs != null) {
            Preferences ui = staticPrefs.node("UI");
            toReturn = ui.getBoolean("forceFileArchiveOn", false);
        }
        return toReturn;
    }

    /**
     * @return application title
     */
    public boolean getStaticDisableHistory()
    {
        boolean toReturn = false;
        if (staticPrefs != null) {
            Preferences ui = staticPrefs.node("UI");
            toReturn = ui.getBoolean("disableImportHistory", false);
        }
        return toReturn;
    }

    /**
     * Set if history should be disabled.
     * @param b if history should be disabled
     */
    public void setUserDisableHistory(boolean b)
    {
    	if (userPrefs == null) return;
        userPrefs.node("UI").putBoolean("disableHistory", b);
        this.flushPreferences();
    }    
    
    /**
     * @return if history should be disabled
     */
    public Boolean getUserDisableHistory()
    {
    	if (userPrefs == null) return true;
    	return userPrefs.node("UI").getBoolean("disableHistory", true);
    }
    
    /**
     * Set debug level for application
     *
     * @param level a debug level
     */
    public void setDebugLevel(int level)
    {
    	if (userPrefs == null) return;
        userPrefs.node("General").putInt("debug", level);
        this.flushPreferences();
    }

    /**
     * @param b - set to use quaqua yes/no
     */
    public void setUseQuaqua(boolean b)
    {
    	if (userPrefs == null) return;
        userPrefs.node("General").putBoolean("useQuaqua", b);
        this.flushPreferences();
    }

    /**
     * @return application version note
     */
    public String getVersionNote()
    {
        // return Main.versionNumber;
        return staticPref("General", "appVersionNote", Version.versionNote);
    }

    /**
     * @return application version number
     */
    public String getVersionNumber()
    {
        try
        {
            ResourceBundle bundle = ResourceBundle.getBundle("omero");
            return bundle.getString("omero.version");
        }
        catch (MissingResourceException e)
        {
            return "Dev Build";
        }
    }

    /**
     * @return if debug console should be shown
     */
    public Boolean isDebugConsole()
    {
        return staticBoolPref("General", "displayDebugConsole", true);
    }

    /**
     * @return server port
     */
    public String getServerPort()
    {
        return staticPref("General", "port", "4064");
    }

    /**
     * Updates the Flex reader server maps from the configuration file.
     */
    public void updateFlexReaderServerMaps()
    {
    	if (userPrefs == null) return;
        Preferences maps = userPrefs.node("FlexReaderServerMaps");
        Map<String, List<String>> values = parseFlexMaps(maps);
        for (Map.Entry<String, List<String>> entry : values.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            for (String mapValue : entry.getValue()) {
                mapFlexServer(entry.getKey(), mapValue);
            }
        }
    }

    /**
     * Parse Flex reader server maps
     *
     * @param maps the Flex reader server maps
     * @return the parsed maps
     */
    public Map<String, List<String>> parseFlexMaps(Preferences maps)
    {
        Map<String, List<String>> rv = new HashMap<String, List<String>>();
        try {
            for (String key : maps.keys())
            {
                String mapValues = maps.get(key, null);
                log.info("Raw Flex reader map values: " + mapValues);
                if (mapValues == null)
                {
                    continue;
                }
                List<String> list = new ArrayList<String>();
                rv.put(key, list);
                for(String value : mapValues.split(";"))
                {
                    value = value.trim();
                    list.add(value);
                }
            }
        } catch (BackingStoreException e)
        {
            log.warn("Error updating Flex reader server maps.", e);
        }
        return rv;
    }

    /**
     * Append keep to server map
     *
     * @param key
     * @param mapValue
     */
    protected void mapFlexServer(String key, String mapValue) {
        try {
            FlexReader.appendServerMap(key, mapValue);
            log.info(String.format("Added Flex reader server map '%s' = '%s'.",
                    key, mapValue));
        // Temporarily catching Exception to fix build
        } catch (Exception e) {
            log.warn(String.format(
                    "Unable to add Flex reader server map '%s' = '%s'", key,
                    mapValue), e);
        }
    }

    // ////////////// [UI] Section ////////////////


    /**
     * @return is debug ui present
     */
    public Boolean isDebugUI() {
        return staticBoolPref("UI", "displayRedBorders", false);
    }

    // TODO: UI locations should handled multiple monitors

    /**
     * @return the ui bounds of the application
     */
    public Rectangle getUIBounds()
    {
        Rectangle rect = new Rectangle();
        if (userPrefs == null) return rect;
        rect.width = userPrefs.node("UI").getInt("width", 980);
        rect.height = userPrefs.node("UI").getInt("height", 580);
        rect.x = userPrefs.node("UI").getInt("xOffset", 10);
        rect.y = userPrefs.node("UI").getInt("yOffset", 10);

        return rect;
    }

    /**
     * Set ui bounds for application
     *
     * @param bounds the bounds
     */
    public void setUIBounds(Rectangle bounds)
    {

        if (bounds.x < 0)
            bounds.x = 0;
        if (bounds.y < 0)
            bounds.y = 0;
        if (bounds.width < 100)
            bounds.width = 100;
        if (bounds.height < 100)
            bounds.height = 100;

        if (userPrefs == null) return;
        userPrefs.node("UI").putInt("width", bounds.width);
        userPrefs.node("UI").putInt("height", bounds.height);
        userPrefs.node("UI").putInt("xOffset", bounds.x);
        userPrefs.node("UI").putInt("yOffset", bounds.y);
    }

    public boolean getUserFullPath()
    {
    	if (userPrefs == null) return true;
	    return userPrefs.node("UI").getBoolean("userFullPath", true);
    }

    public void setUserFullPath(boolean b)
    {
    	if (userPrefs != null)
	        userPrefs.node("UI").putBoolean("userFullPath", b);
    }

    public boolean getCustomImageNaming()
    {
    	if (userPrefs == null) return true;
	    return userPrefs.node("UI").getBoolean("customImageNaming", true);
    }

    public void setCustomImageNaming(boolean b)
    {
    	if (userPrefs != null)
		    userPrefs.node("UI").putBoolean("customImageNaming", b);
    }

    public int getNumOfDirectories()
    {
    	if (userPrefs == null) return 0;
	    return userPrefs.node("UI").getInt("numOfDirectories", 0);
    }

    public void setNumOfDirectories(int i)
    {
    	if (userPrefs == null) return;
	    userPrefs.node("UI").putInt("numOfDirectories", i);
    }

    /**
     * @return Returns the userSettingsDirectory.
     */
    public String getUserSettingsDirectory()
    {
        return userSettingsDirectory;
    }

    private Preferences staticPrefsOrNull()
    {
        File staticFile = new File(staticConfigFile);
        if (!staticFile.exists() || !staticFile.canRead())
        {
            return null;
        }

        try {
            Preferences prefs = new IniFile(staticFile, Mode.RO);
            return prefs;
        } catch (BackingStoreException e) {
            log.error(e.toString()); // slf4j migration: toString()
            throw new RuntimeException(e);
        }
    }

    private String staticPref(String node, String key, String def) {
        if (staticPrefs == null) {
            return def;
        }
        return staticPrefs.node(node).get(key, def);
    }

    private Boolean staticBoolPref(String node, String key, Boolean def) {
        if (staticPrefs == null) {
            return def;
        }
        return staticPrefs.node(node).getBoolean(key, def);
    }
}
