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

import loci.formats.FormatException;
import loci.formats.in.FlexReader;
import ome.formats.importer.Version;

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
    private final static Log                     log = LogFactory.getLog(IniFileLoader.class);

    public static interface Callback {
        public static Callback DEFAULT = new Callback(){
            public void backingStoreException(BackingStoreException bse) {
                log.error(bse);
            }};
        void backingStoreException(BackingStoreException bse);
    }
    
    // Dynamic user settings
    private         String          userSettingsDirectory;
    private static  String          userSettingsFile;
    private         Preferences     userPrefs;
    
    // Static config settings
    private         String          staticConfigDirectory;
    private static  String          staticConfigFile;
    private         Preferences     staticPrefs;
    
    private static  IniFileLoader   ini;

    
    //////////////// Class Intialization Section ////////////////
 
    /**
     * IniFileLoader contructor(). Privately called singleton method.
     * @param userConfigFile
     */
    private IniFileLoader(String userConfigFile, Callback cb)
    {       
        // Set up static config file
        staticConfigDirectory = System.getProperty("user.dir") + File.separator + "config";
        staticConfigFile = staticConfigDirectory + File.separator + "importer.config";
        
        try
        {
            staticPrefs = new IniFile(new File(staticConfigFile), Mode.RO);
        } catch (BackingStoreException e) {
            log.error(e);
            cb.backingStoreException(e);
        }
        
        // Set up user config file
        userSettingsDirectory = System.getProperty("user.home") + File.separator + "omero";

        if (!new File(userSettingsDirectory).exists()) {
            new File(userSettingsDirectory).mkdir();
        }
        
        if (userConfigFile == null)
            userConfigFile = userSettingsDirectory + File.separator + "importer.ini";
        if (IniFileLoader.userSettingsFile == null)
            IniFileLoader.userSettingsFile = userConfigFile;
        
        try
        {
        	userPrefs = new IniFile(new File(userConfigFile), Mode.RW);
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
    	flushPreferences();
    }

    // Initial load passing in args
    public static IniFileLoader getIniFileLoader(String[] args, Callback cb)
    {
        String filename;
        if (args != null && args.length > 0) {
            filename = args[0];
        } else {
            filename = userSettingsFile;
        }
        
        if (ini == null)
        // it's ok, we can call this constructor
            ini = new IniFileLoader(filename, cb);
        return ini;
    }

    // Initial load passing in args
    public static IniFileLoader getIniFileLoader(Callback cb)
    {
        if (ini == null)
        // it's ok, we can call this constructor
            ini = new IniFileLoader(null, cb);
        return ini;
    }    

    public void flushPreferences()
    {
        try
        {
            userPrefs.flush();
        } catch (BackingStoreException e)
        {
            log.error(e);
        }
    }

    //////////////// [General] Section ////////////////
    

    public boolean getUseQuaqua() 
    {
        return userPrefs.node("General").getBoolean("useQuaqua", true);
    }
    
    
    public String getAppTitle()
    {
        return staticPrefs.node("General").get("appTitle", "OMERO.importer");
    }


    public void setUseQuaqua(boolean b)
    {
        userPrefs.node("General").putBoolean("useQuaqua", b);
        this.flushPreferences();
    }
    
    public String getVersionNumber()
    {
        //return Main.versionNumber;
        return staticPrefs.node("General").get("appVersion", Version.versionNumber);
    }

    public Boolean isDebugConsole()
    {
        return staticPrefs.node("General").getBoolean("displayDebugConsole", true);
    }

    public String getServerPort()
    {
        return staticPrefs.node("General").get("port", "4063");
    }
    
    /**
     * Updates the Flex reader server maps from the configuration file.
     */
    public void updateFlexReaderServerMaps()
    {
    	Preferences maps = userPrefs.node("FlexReaderServerMaps");
    	try
    	{
    		for (String key : maps.keys())
    		{
    			String mapValues = maps.get(key, null);
    			log.info("Raw Flex reader map values: " + mapValues);
    			if (mapValues == null)
    			{
    				return;
    			}
    			for (String mapValue : mapValues.trim().split((";")))
    			{
	    			try
	    			{
	    				FlexReader.mapServer(key, mapValue);
	    				log.info(String.format(
	    					"Added Flex reader server map '%s' = '%s'.",
	    					key, mapValue));
	    			}
	    			catch (FormatException e)
	    			{
	    				log.warn(String.format(
	    					"Unable to add Flex reader server map '%s' = '%s'", 
	    					key, mapValue), e);
	    			}
    			}
    		}
    	}
    	catch (BackingStoreException e)
    	{
    		log.warn("Error updating Flex reader server maps.", e);
    	}
    }
    
    //////////////// [UI] Section ////////////////
    public Boolean isDebugUI()
    {
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

        if (bounds.x < 0) bounds.x = 0;
        if (bounds.y < 0) bounds.y = 0;
        if (bounds.width < 100) bounds.width = 100;
        if (bounds.height < 100) bounds.height = 100;
        
        userPrefs.node("UI").putInt("width", bounds.width);
        userPrefs.node("UI").putInt("height", bounds.height); 
        userPrefs.node("UI").putInt("xOffset", bounds.x);
        userPrefs.node("UI").putInt("yOffset", bounds.y);
	}
	
	public String getUploaderTokenURL()
	{
	   return staticPrefs.node("Uploader").get("TokenURL", "");
	}

   public String getUploaderURL()
    {
       return staticPrefs.node("Uploader").get("URL", "");
    }

   public String getBugTrackerURL()
   {
      return staticPrefs.node("Uploader").get("BugTrackerURL", "");
   }
   
    //////////////// Testing Section ////////////////
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        getIniFileLoader(args, Callback.DEFAULT);
    }

    public void displayDebugSettings()
    {
        System.out.println("version: " + getVersionNumber());
        System.out.println("debug: " + isDebugConsole());
    }

    
    /**
     * @return Returns the userSettingsDirectory.
     */
    public String getUserSettingsDirectory()
    {
        return userSettingsDirectory;
    }
}
