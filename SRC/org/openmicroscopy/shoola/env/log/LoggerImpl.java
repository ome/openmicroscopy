/*
 * org.openmicroscopy.shoola.env.log.LoggerImpl
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

//Third-party libraries
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

//Application-internal dependencies

/** 
 * Implements the {@link logger} interface. 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *              a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */

public class LoggerImpl
    implements Logger
{
    
    /**
     * Returns the <i>Log4j</i> logger for the specified object.
     * 
     * @param target	The object that is issuing the log message.  If 
     * 					<code>null</code>, then the root logger is returned.
     * @return A logger for the specified object.
     */
    private org.apache.log4j.Logger getAdaptee(Object target)
    { 
		if (target != null) 
			return org.apache.log4j.Logger.getLogger(
												target.getClass().getName());
		return org.apache.log4j.Logger.getRootLogger();
    }
    
    /**
     * Initializes Log4j.
     * 
     * @param absFile	The absolute pathname of the log file.
     */
    public LoggerImpl(String absFile)
    {
    	Properties config = new Properties();
    	
    	//Define the base appender.
		config.put("log4j.appender.BASE", 
					"org.apache.log4j.RollingFileAppender");
		config.put("log4j.appender.BASE.File", 
					absFile);
		config.put("log4j.appender.BASE.MaxFileSize", 
					"100KB");  //Maximum size that the output file is allowed
							//to reach before being rolled over to backup files.
		config.put("log4j.appender.BASE.MaxBackupIndex", 
					"1");  //Maximum number of backup files to keep around.	
					
    	//Define its output layout.
		config.put("log4j.appender.BASE.layout", 
					"org.apache.log4j.PatternLayout");
    	config.put("log4j.appender.BASE.layout.ConversionPattern", 
					"%r %p [thread: %t][class: %c] - %m%n%n");
		
		//Set the the root logger level and appender.
		config.put("log4j.rootLogger", 
					"debug, BASE");
		
		//Do configuration.
		PropertyConfigurator.configure(config);
    }
    
	/** Implemented as specified by {@link Logger}. */     
    public void debug(Object c, String logMsg)
    {
		getAdaptee(c).debug(logMsg);
    }
    
	/** Implemented as specified by {@link Logger}. */ 
    public void error(Object c, String logMsg)
    {
		getAdaptee(c).error(logMsg);
    }
    
	/** Implemented as specified by {@link Logger}.*/ 
    public void fatal(Object c, String logMsg)
    {
		getAdaptee(c).fatal(logMsg);
    }
    
	/** Implemented as specified by {@link Logger}. */ 
    public void info(Object c, String logMsg)
    {
		getAdaptee(c).info(logMsg);
    }
    
	/** Implemented as specified by {@link Logger}. */ 
    public void warn(Object c, String logMsg)
    {
		getAdaptee(c).warn(logMsg);
    }
  
}
