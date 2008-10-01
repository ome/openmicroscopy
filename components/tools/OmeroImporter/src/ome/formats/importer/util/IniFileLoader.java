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
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import ome.formats.importer.Main;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ini4j.IniFile;
import org.ini4j.IniFile.Mode;

/**
 *  This class loads in the default importer.ini file 
 *  (or one specified from the command line when starting the app)
 *  
 * @author Brian W. Loranger
 */
public class IniFileLoader
{
    Log                     log = LogFactory.getLog(IniFileLoader.class);
    
    private static  String          configFile;
    private         String          saveDirectory;
    private         Preferences     prefs;
    private static  IniFileLoader   ini;

    
    //////////////// Class Intialization Section ////////////////
 
    /**
     * IniFileLoader contructor(). Privately called singleton method.
     * @param configFile
     */
    private IniFileLoader(String configFile)
    {
        saveDirectory = System.getProperty("user.home") + File.separator + "omero";

        if (!new File(saveDirectory).exists()) {
            new File(saveDirectory).mkdir();
        }
        
        if (configFile == null)
            configFile = saveDirectory + File.separator + "importer.ini";
        if (IniFileLoader.configFile == null)
            IniFileLoader.configFile = configFile;
        
        try
        {
        	prefs = new IniFile(new File(configFile), Mode.RW);
        } catch (BackingStoreException e) {
            log.error(e);
			//throw new RuntimeException("Trouble backing up ini file.");
		}
        
        // Add a shutdown hook for when app closes
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() { shutdown(); }
        });
    }

    protected void shutdown()
    {
    	//flushPreferences();
    }

    // Initial load passing in args
    public static IniFileLoader getIniFileLoader(String[] args)
    {
        String filename = args.length > 0 ? args[0] : configFile;
        
        if (ini == null)
        // it's ok, we can call this constructor
            ini = new IniFileLoader(filename);
        return ini;
    }

    // Initial load passing in args
    public static IniFileLoader getIniFileLoader()
    {
        if (ini == null)
        // it's ok, we can call this constructor
            ini = new IniFileLoader(null);
        return ini;
    }    

    public void flushPreferences()
    {
        try
        {
            prefs.flush();
        } catch (BackingStoreException e)
        {
            log.error(e);
        }
    }

    //////////////// [General] Section ////////////////
    
    public String getAppTitle()
    {
        return prefs.node("General").get("appTitle", "OMERO.importer");
    }

    public String getVersionNumber()
    {
        return prefs.node("General").get("appVersion", Main.versionNumber);
    }

    public Boolean isDebugConsole()
    {
        return prefs.node("General").getBoolean("displayDebugConsole", true);
    }

    //////////////// [UI] Section ////////////////
    public Boolean isDebugUI()
    {
        return prefs.node("UI").getBoolean("displayRedBorders", false);
    }
  
    // TODO: UI locations should handled multiple monitors
    
	public Rectangle getUIBounds() {
		
		Rectangle rect = new Rectangle();
		
        rect.width = prefs.node("UI").getInt("width", 980);
        rect.height = prefs.node("UI").getInt("height", 580);
        rect.x = prefs.node("UI").getInt("xOffset", 10);
        rect.y = prefs.node("UI").getInt("yOffset", 10);
        
       return rect;
	}

	public void setUIBounds(Rectangle bounds) {

        if (bounds.x < 0) bounds.x = 0;
        if (bounds.y < 0) bounds.y = 0;
        if (bounds.width < 100) bounds.width = 100;
        if (bounds.height < 100) bounds.height = 100;
        
        prefs.node("UI").putInt("width", bounds.width);
        prefs.node("UI").putInt("height", bounds.height); 
        prefs.node("UI").putInt("xOffset", bounds.x);
        prefs.node("UI").putInt("yOffset", bounds.y);
	}
	
    //////////////// Testing Section ////////////////
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        getIniFileLoader(args);
    }

    public void displayDebugSettings()
    {
        System.out.println("version: " + getVersionNumber());
        System.out.println("debug: " + isDebugConsole());
    }
}
