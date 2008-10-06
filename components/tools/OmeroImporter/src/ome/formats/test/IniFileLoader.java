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
package ome.formats.test;

import java.awt.Rectangle;
import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

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
    
    private         String          configFile;
    private         Preferences     prefs;
    private static  IniFileLoader   ini;

    
    //////////////// Class Intialization Section ////////////////
 
    /**
     * IniFileLoader contructor(). Privately called singleton method.
     * @param configFile
     */
    private IniFileLoader(String configFile)
    {
        this.configFile = configFile;
        try
        {
            File saveFile = new File(this.configFile);
            if (!saveFile.exists())
            {
                System.err.println("File not found: " + this.configFile);
            }
            
        	prefs = new IniFile(saveFile, Mode.RW);
        } catch (BackingStoreException e) {
            e.printStackTrace();
			throw new RuntimeException("Trouble setting ini file.");
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

    public static IniFileLoader getNewIniFileLoader(String filePath)
    {
            ini = new IniFileLoader(filePath);
            return ini;
    }
    
    // Initial load passing in args
    public IniFileLoader getIniFileLoader(String[] args)
    {
        String filename = args.length > 0 ? args[0] : configFile;
        
        if (ini == null && configFile == null)
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
        return prefs.node("General").get("appTitle", "Importer");
    }

    public String getVersionNumber()
    {
        return prefs.node("General").get("appVersion", "unknown");
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
	
    public void displayDebugSettings()
    {
        System.out.println("version: " + getVersionNumber());
        System.out.println("debug: " + isDebugConsole());
    }
    
    public String[] getFileList()
    {
        try
        {
            return prefs.childrenNames();
        } catch (BackingStoreException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
    }

    public String getNote(String file)
    {
        return prefs.node(file).get("note", null);
    }

    public String[] getFileTypes()
    {
        String[] types = null;
        String fileTypes = prefs.node("populate_options").get("filetypes", null);
        if (fileTypes != null)
        {
            types = fileTypes.split(",");
            for (int i = 0; i<types.length; i++)
            {
                types[i] = types[i].trim();
            }
        }
        return types;
    }
    
    public String getStringValue(String section, String key)
    {
        return prefs.node(section).get(key, null);
    }

    public void setStringValue(String section, String key, String value)
    {
        prefs.node(section).put(key, value);
    }

    public void addFile(String fileName)
    {
        prefs.node(fileName);
    }
}
