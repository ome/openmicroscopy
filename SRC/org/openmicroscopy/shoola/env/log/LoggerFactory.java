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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;

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
	 * Name of the log directory.
	 * The path to the log directory is the absolute path to the installation
	 * directory followed by the value of this field. 
	 */
	public static final String		LOG_DIR = "log";
	
	/** 
	 * Name of the log file.
	 * The log file is contained in the log directory.  Under exceptional
	 * circumstances, it could be located under the installation directory &151;
	 * this can only happen if someone fiddled with the structure of the
	 * install directory.  
	 */
	public static final String		LOG_FILE = "shoola.log";
	
	
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
		String off = (String) c.getRegistry().lookup("/services/LOG");
		if ("OFF".equalsIgnoreCase(off))	return makeNoOpLogger();
		File logDir = new File(c.getHomeDir(), LOG_DIR), logFile;
		logDir.mkdir();
		if (logDir.isDirectory())	logFile = new File(logDir, LOG_FILE);
		else	logFile = new File(c.getHomeDir(), LOG_FILE);
		return new LoggerImpl(logFile.getAbsolutePath());
	}
	
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

}
