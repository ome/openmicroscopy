/*
 * org.openmicroscopy.shoola.env.log.LoggerFactory
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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

package org.openmicroscopy.shoola.env.log;


//Java imports
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;

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
	
	/**
	 * The name of the log configuration file in the config directory.
	 */
	public static final String		LOG_CONFIG_FILE = "log4j.config";
	
	
	/**
	 * Creates a new {@link Logger}.
	 * 
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
		if (!isLoggingOn.booleanValue())	return makeNoOpLogger();
		
		//Ok we have to log, so try and read the config file.
		Properties config = loadConfig(c.resolveConfigFile(LOG_CONFIG_FILE));
		if (config == null)	return makeNoOpLogger();	
		
		//We have a config file, set up log4j.
		String logDirName = (String) reg.lookup(LookupNames.LOG_DIR),
				logFileName = (String) reg.lookup(LookupNames.LOG_FILE);
		File logDir = new File(c.getHomeDir(), logDirName), logFile;
		logDir.mkdir();
		if (logDir.isDirectory())	logFile = new File(logDir, logFileName);
		else	logFile = new File(c.getHomeDir(), logFileName);
		return new LoggerImpl(config, logFile.getAbsolutePath());
	}
	
	/**
	 * Creates a no-op implementation of {@link Logger}.
	 * 
	 * @return See above.
	 */
	private static Logger makeNoOpLogger()
	{
		return new Logger() {
			public void debug(Object c, String logMsg) {}
			public void error(Object c, String logMsg) {}
			public void fatal(Object c, String logMsg) {}
			public void info(Object c, String logMsg) {}
			public void warn(Object c, String logMsg) {}
		};
	}
	
	/**
	 * Reads in the specified file as a property object.
	 * 
	 * @param file	Absolute pathname to the file.
	 * @return	The content of the file as a property object or
	 * 			<code>null</code> if an error occured.
	 */
	private static Properties loadConfig(String file)
	{
		Properties config = new Properties();
		try { 
			FileInputStream fis = new FileInputStream(file);
			config.load(fis);
		} catch (Exception e) {
			return null;
		}
		return config;
	}

}
