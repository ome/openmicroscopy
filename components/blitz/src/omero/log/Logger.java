/*
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


package omero.log;

/** 
 * Defines the log service interface.
 * Operations are defined to log messages according the severity of the event:
 * <ul>
 *  <li><i>DEBUG</i>: all debug messages.</li>
 *  <li><i>INFO</i>: regular log messages that inform about normal application
 *  	workflow.</li>
 *  <li><i>WARN</i>: messages emitted in case of abnormal or suspect
 * 		application behavior.</li>
 *  <li><i>ERROR</i>: all error conditions and failures that can be
 * 		recovered.</li>
 *  <li><i>FATAL</i>: severe failures that require the application to
 * 		terminate.</li>
 * <ul>
 * <p>Every method takes in two parameters: the originator of the log message
 * and the log message itself.  If the message spans multiple lines, then
 * a {@link LogMessage} object should be used to construct it.</p>  
 * <p>A configuration file (in the configuration directory under the
 * installation directory) provides for fine-tuning of the log settings on a
 * per-class basis.  Those settings include the choice of output locations and
 * verbosity based on priority levels &#151; <i>DEBUG</i> has a lower priority
 * than <i>INFO</i>, which, in turn, is lower priority than <i>WARN</i>, and so
 * on.</p>
 * <p>The implementation of the service is thread-safe.  Methods can be called
 * from different threads without compromising the integrity of the log records.
 * </p>
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *              a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public interface Logger
{

    /**
     * Logs a debug message.
     *
     * @param originator The originator of the message.
     * @param logMsg The log message.
     */
    public void debug(Object originator, String logMsg);

    /**
     * Logs a debug message.
     *
     * @param originator The originator of the message.
     * @param msg The log message.
     */
    public void debug(Object originator, LogMessage msg);

    /**
     * Logs an info message.
     *
     * @param originator The originator of the message.
     * @param logMsg The log message.
     */
    public void info(Object originator, String logMsg);

    /**
     * Logs an info message.
     *
     * @param originator The originator of the message.
     * @param msg The log message.
     */
    public void info(Object originator, LogMessage msg);

    /**
     * Logs a warn message.
     *
     * @param originator The originator of the message.
     * @param logMsg The log message.
     */
    public void warn(Object originator, String logMsg);

    /**
     * Logs a warn message.
     *
     * @param originator The originator of the message.
     * @param msg The log message.
     */
    public void warn(Object originator, LogMessage msg);

    /**
     * Logs an error message.
     *
     * @param originator The originator of the message.
     * @param logMsg The log message.
     */
    public void error(Object originator, String logMsg);

    /**
     * Logs an error message.
     *
     * @param originator The originator of the message.
     * @param msg The log message.
     */
    public void error(Object originator, LogMessage msg);

    /**
     * Logs a fatal message.
     *
     * @param originator The originator of the message.
     * @param logMsg The log message.
     */
    public void fatal(Object originator, String logMsg);

    /**
     * Logs a fatal message.
     *
     * @param originator The originator of the message.
     * @param msg The log message.
     */
    public void fatal(Object originator, LogMessage msg);

    /** 
     * Returns the log file.
     *
     * @return See above.
     */
    public String getLogFile();

}
