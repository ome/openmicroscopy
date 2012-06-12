/*
 * org.openmicroscopy.shoola.env.log.LoggerFactory
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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

package org.openmicroscopy.shoola.env.log;


//Java imports
import java.io.File;
import java.util.Properties;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.file.IOUtil;

/** 
 * A factory for the {@link Logger}. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class LoggerFactory
{
	
	/** The name of the log configuration file in the config directory. */
	private static final String		LOG_CONFIG_FILE = "log4j.config";
	
	/**
	 * Creates a new {@link Logger}.
	 * 
     * @param c Reference to the container.
	 * @return	See above.
	 */
	public static Logger makeNew(Container c)
	{
		//NB: this can't be called outside of container b/c agents have no refs
		//to the singleton container. So we can be sure this method is going to
		//create services just once.
		if (c == null)	return null;
		
		//If logging is off, then we return the no-op adapter.
		Registry reg = c.getRegistry();
		Boolean isLoggingOn = (Boolean) reg.lookup(LookupNames.LOG_ON);
		if (!isLoggingOn.booleanValue()) return makeNoOpLogger();
		
		//Ok we have to log, so try and read the config file.
		Properties config = loadConfig(c.resolveFilePath(LOG_CONFIG_FILE, 
				Container.CONFIG_DIR));
		if (config == null)	return makeNoOpLogger();	
		
		//We have a config file, set up log4j.
		String logDirName = (String) reg.lookup(LookupNames.LOG_DIR),
				logFileName = (String) reg.lookup(LookupNames.LOG_FILE);
		String name = (String) reg.lookup(LookupNames.OMERO_HOME);
		String omeroDir = System.getProperty("user.home")+File.separator+name;
		File home = new File(omeroDir);
		if (!home.exists()) 
			home.mkdir();
		File logFile, logDir;

		if (home.isDirectory()) {
			logDir = new File(home, logDirName);
			logDir.mkdir();
			if (logDir.isDirectory()) logFile = new File(logDir, logFileName);
			else logFile = new File(home, logFileName);
		} else {
			logDir = new File(c.getHomeDir(), logDirName);
			logDir.mkdir();
			if (logDir.isDirectory()) logFile = new File(logDir, logFileName);
			else logFile = new File(c.getHomeDir(), logFileName);
		}
		
		return new LoggerImpl(config, logFile.getAbsolutePath());
	}
	
	/**
	 * Creates a no-operation implementation of {@link Logger}.
	 * 
	 * @return See above.
	 */
	private static Logger makeNoOpLogger()
	{
		return new Logger() {
			public void debug(Object c, String logMsg) {}
			public void debug(Object c, LogMessage msg) {}
			public void error(Object c, String logMsg) {}
			public void error(Object c, LogMessage msg) {}
			public void fatal(Object c, String logMsg) {}
			public void fatal(Object c, LogMessage msg) {}
			public void info(Object c, String logMsg) {}
			public void info(Object c, LogMessage msg) {}
			public void warn(Object c, String logMsg) {}
			public void warn(Object c, LogMessage msg) {}
			public String getLogFile() { return null; }
		};
	}
	
	/**
	 * Reads in the specified file as a property object.
	 * 
	 * @param file	Absolute pathname to the file.
	 * @return	The content of the file as a property object or
	 * 			<code>null</code> if an error occurred.
	 */
	private static Properties loadConfig(String file)
	{
		Properties config = new Properties();
		try { 
			config.load(IOUtil.readConfigFile(file));
		} catch (Exception e) {
			return null;
		}
		return config;
	}

}
