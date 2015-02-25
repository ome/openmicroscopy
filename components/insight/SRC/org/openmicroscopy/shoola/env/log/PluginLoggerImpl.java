/*
 * org.openmicroscopy.shoola.env.log.PluginLoggerImpl
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

//Application-internal dependencies
import org.openmicroscopy.shoola.env.LookupNames;


/**
 * Provides the log service for cases where execution
 * is taking place as a plugin.
 *
 * This is just a simple adapter that forwards calls to <i>slf4j</i>.
 * Thread-safety is already enforced by <i>slf4j</i>, so we don't deal with it.
 *
 * @since 5.0.0
 */

class PluginLoggerImpl
    implements Logger
{

    /** Value identifying the plugin or <code>-1</code>.*/
    private int runAsPlugin;

    /**
     * Handles the error if run as a plug-in.
     *
     * @param logMsg The message to handle.
     */
    private void handlePlugin(String logMsg)
    {
        if ((runAsPlugin == LookupNames.IMAGE_J ||
                runAsPlugin == LookupNames.IMAGE_J_IMPORT) && IJ.debugMode) {
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
    PluginLoggerImpl(int runAsPlugin)
    {
        this.runAsPlugin = runAsPlugin;
    }

    /**
     * Implemented as specified by {@link Logger}.
     * @see Logger#debug(Object, String)
     */
    public void debug(Object c, String logMsg)
    {
        handlePlugin(logMsg);
    }

    /**
     * Implemented as specified by {@link Logger}
     * @see Logger#debug(Object, LogMessage)
     */
    public void debug(Object c, LogMessage msg)
    {
        handlePlugin(msg == null ? null : msg.toString());
    }

    /**
     * Implemented as specified by {@link Logger}.
     * @see Logger#error(Object, String)
     */
    public void error(Object c, String logMsg)
    {
        handlePlugin(logMsg);
    }

    /**
     * Implemented as specified by {@link Logger}.
     * @see Logger#error(Object, LogMessage)
     */
    public void error(Object c, LogMessage msg)
    {
        handlePlugin(msg == null ? null : msg.toString());
    }

    /**
     * Implemented as specified by {@link Logger}.
     * @see Logger#fatal(Object, String)
     */
    public void fatal(Object c, String logMsg)
    {
        handlePlugin(logMsg);
    }

    /**
     * Implemented as specified by {@link Logger}.
     * @see Logger#fatal(Object, LogMessage)
     */
    public void fatal(Object c, LogMessage msg)
    {
        handlePlugin(msg == null ? null : msg.toString());
    }

    /**
     * Implemented as specified by {@link Logger}.
     * @see Logger#info(Object, String)
     */
    public void info(Object c, String logMsg)
    {
        handlePlugin(logMsg);
    }

    /**
     * Implemented as specified by {@link Logger}.
     * @see Logger#info(Object, LogMessage)
     */
    public void info(Object c, LogMessage msg)
    {
        handlePlugin(msg == null ? null : msg.toString());
    }

    /**
     * Implemented as specified by {@link Logger}.
     * @see Logger#warn(Object, String)
     */
    public void warn(Object c, String logMsg)
    {
        handlePlugin(logMsg);
    }

    /**
     * Implemented as specified by {@link Logger}.
     * @see Logger#warn(Object, LogMessage)
     */
    public void warn(Object c, LogMessage msg)
    {
        handlePlugin(msg == null ? null : msg.toString());
    }

    @Override
    public String getLogFile() {
        return null;
    }

}
