/*
 * org.openmicroscopy.shoola.env.log.LoggerImpl
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-14 University of Dundee. All rights reserved.
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

//Third-party libraries
import ij.IJ;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.LookupNames;


/** 
 * Provides the log service.
 * This is just a simple adapter that forwards calls to <i>slf4j</i>.
 * Thread-safety is already enforced by <i>slf4j</i>, so we don't deal with it.
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
    
    /** The name of the log file variable the log config file. */
    private static final String LOG_FILE_NAME = "logFileName";

	/** The absolute pathname of the log file.*/
	private String absFile;
	
	/** Value identifying the plugin or <code>-1</code>.*/
	private int runAsPlugin;

    /**
     * Returns the <i>Log4j</i> logger for the specified object.
     * 
     * @param target	The object that is issuing the log message.  If 
     * 					<code>null</code>, then the root logger is returned.
     * @return A logger for the specified object.
     */
    private org.slf4j.Logger getAdaptee(Object target)
    { 
		if (target != null) 
			return org.slf4j.LoggerFactory.getLogger(
												target.getClass());
		return org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    }

    /**
     * Handles the error if run as a plug-in.
     *
     * @param logMsg The message to handle.
     */
    private void handlePlugin(String logMsg)
    {
        if (runAsPlugin == LookupNames.IMAGE_J && IJ.debugMode) {
            IJ.log(logMsg);
        }
    }

    /**
     * Initializes slf4j.
     * 
     * @param configFile The pathname of a configuration file.
     * @param absFile The absolute pathname of the log file.
     * @param runAsPlugin Value identifying the plugin or <code>-1</code>.
     */
    LoggerImpl(String configFile, String absFile, int runAsPlugin)
    {
        this.runAsPlugin = runAsPlugin;
        if (runAsPlugin < 0) {
            LoggerContext context = (LoggerContext)
                    org.slf4j.LoggerFactory.getILoggerFactory();
            try {
              JoranConfigurator configurator = new JoranConfigurator();
              configurator.setContext(context);
              context.reset();
              context.putProperty(LOG_FILE_NAME, absFile);
              configurator.doConfigure(configFile);
            } catch (JoranException je) {
              // StatusPrinter will handle this
            }
            StatusPrinter.printInCaseOfErrorsOrWarnings(context);
        }
    }
    
	/** 
     * Implemented as specified by {@link Logger}.
     * @see Logger#debug(Object, String)
     */     
    public void debug(Object c, String logMsg)
    {
        if (runAsPlugin < 0) {
            getAdaptee(c).debug(logMsg);
        } else {
            handlePlugin(logMsg);
        }
    }
    
	/** 
     * Implemented as specified by {@link Logger}
     * @see Logger#debug(Object, LogMessage)
     */     
	public void debug(Object c, LogMessage msg)
	{
	    if (runAsPlugin < 0) {
	        getAdaptee(c).debug(msg == null ? null : msg.toString());
        } else {
            handlePlugin(msg == null ? null : msg.toString());
        }
	}

	/**
     * Implemented as specified by {@link Logger}.
     * @see Logger#error(Object, String)
     */ 
    public void error(Object c, String logMsg)
    {
        if (runAsPlugin < 0) {
            getAdaptee(c).error(logMsg);
        } else {
            handlePlugin(logMsg);
        }
    }
    
	/** 
     * Implemented as specified by {@link Logger}.
     * @see Logger#error(Object, LogMessage)
     */     
	public void error(Object c, LogMessage msg)
	{
	    if (runAsPlugin < 0) {
	        getAdaptee(c).error(msg == null ? null : msg.toString());
	    } else {
	        handlePlugin(msg == null ? null : msg.toString());
	    }
	}
    
	/** 
     * Implemented as specified by {@link Logger}.
     * @see Logger#fatal(Object, String)
     */ 
    public void fatal(Object c, String logMsg)
    {
        if (runAsPlugin < 0) {
            getAdaptee(c).error(logMsg);
        } else {
            handlePlugin(logMsg);
        }
    }
    
	/** 
     * Implemented as specified by {@link Logger}.
     * @see Logger#fatal(Object, LogMessage)
     */     
	public void fatal(Object c, LogMessage msg)
	{
        if (runAsPlugin < 0) {
            getAdaptee(c).error(msg == null ? null : msg.toString());
        } else {
            handlePlugin(msg == null ? null : msg.toString());
        }
	}
    
	/** 
     * Implemented as specified by {@link Logger}.
     * @see Logger#info(Object, String)
     */ 
    public void info(Object c, String logMsg)
    {
        if (runAsPlugin < 0) {
            getAdaptee(c).info(logMsg);
        } else {
            handlePlugin(logMsg);
        }
    }
    
	/**
     * Implemented as specified by {@link Logger}.
     * @see Logger#info(Object, LogMessage)
     */     
	public void info(Object c, LogMessage msg)
	{
        if (runAsPlugin < 0) {
            getAdaptee(c).info(msg == null ? null : msg.toString());
        } else {
            handlePlugin(msg == null ? null : msg.toString());
        }
	}
    
	/**
     * Implemented as specified by {@link Logger}.
     * @see Logger#warn(Object, String)
     */ 
    public void warn(Object c, String logMsg)
    {
        if (runAsPlugin < 0) {
            getAdaptee(c).warn(logMsg);
        } else {
            handlePlugin(logMsg);
        }
    }
    
	/**
     * Implemented as specified by {@link Logger}.
     * @see Logger#warn(Object, LogMessage)
     */
	public void warn(Object c, LogMessage msg)
	{
        if (runAsPlugin < 0) {
            getAdaptee(c).warn(msg == null ? null : msg.toString());
        } else {
            handlePlugin(msg == null ? null : msg.toString());
        }
	}

	/**
     * Implemented as specified by {@link Logger}.
     * @see Logger#getLogFile()
     */
	public String getLogFile() { return absFile; }

}
