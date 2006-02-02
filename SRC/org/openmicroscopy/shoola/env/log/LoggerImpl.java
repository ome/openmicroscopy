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
import java.util.Properties;

//Third-party libraries
import org.apache.log4j.PropertyConfigurator;

//Application-internal dependencies


/** 
 * Provides the log service.
 * This is just a simple adapter that forwards calls to <i>log4j</i>.
 * Thread-safety is already enforced by <i>log4j</i>, so we don't deal with it. 
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

class LoggerImpl
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
     * @param config	A property object built from the configuration file.
     * @param absFile	The absolute pathname of the log file.
     */
    LoggerImpl(Properties config, String absFile)
    {
		config.put("log4j.appender.BASE.File", absFile);
		PropertyConfigurator.configure(config);
    }
    
	/** 
     * Implemented as specified by {@link Logger}. 
     * @see Logger#debug(Object, String)
     */     
    public void debug(Object c, String logMsg)
    {
		getAdaptee(c).debug(logMsg);
    }
    
	/** 
     * Implemented as specified by {@link Logger}.
     * @see Logger#debug(Object, LogMessage)
     */     
	public void debug(Object c, LogMessage msg)
	{
		getAdaptee(c).debug(msg == null ? null : msg.toString());
	}
    
	/**
     * Implemented as specified by {@link Logger}.
     * @see Logger#error(Object, String)
     */ 
    public void error(Object c, String logMsg)
    {
		getAdaptee(c).error(logMsg);
    }
    
	/** 
     * Implemented as specified by {@link Logger}.
     * @see Logger#error(Object, LogMessage)
     */     
	public void error(Object c, LogMessage msg)
	{
		getAdaptee(c).error(msg == null ? null : msg.toString());
	}
    
	/** 
     * Implemented as specified by {@link Logger}.
     * @see Logger#fatal(Object, String)
     */ 
    public void fatal(Object c, String logMsg)
    {
		getAdaptee(c).fatal(logMsg);
    }
    
	/** 
     * Implemented as specified by {@link Logger}. 
     * @see Logger#fatal(Object, LogMessage)
     */     
	public void fatal(Object c, LogMessage msg)
	{
		getAdaptee(c).fatal(msg == null ? null : msg.toString());
	}
    
	/** 
     * Implemented as specified by {@link Logger}.
     * @see Logger#info(Object, String)
     */ 
    public void info(Object c, String logMsg)
    {
		getAdaptee(c).info(logMsg);
    }
    
	/**
     * Implemented as specified by {@link Logger}.
     * @see Logger#info(Object, LogMessage)
     */     
	public void info(Object c, LogMessage msg)
	{
		getAdaptee(c).info(msg == null ? null : msg.toString());
	}
    
	/**
     * Implemented as specified by {@link Logger}. 
     * @see Logger#warn(Object, String)
     */ 
    public void warn(Object c, String logMsg)
    {
		getAdaptee(c).warn(logMsg);
    }
    
	/**
     * Implemented as specified by {@link Logger}.
     * @see Logger#warn(Object, LogMessage)
     */     
	public void warn(Object c, LogMessage msg)
	{
		getAdaptee(c).warn(msg == null ? null : msg.toString());
	}
  
}
