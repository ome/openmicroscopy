/*
 * ome.formats.importer.util.LogAppenderProxy
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

package ome.formats.importer.util;

import java.io.File;

import ome.formats.importer.gui.LogAppender;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author Brian W. Loranger
 *
 */
public class LogAppenderProxy extends AppenderSkeleton implements Appender
{
    private Layout layout;
    private static boolean configured;
    private static LogAppender delegate;
    private static RollingFileAppender logfile_delegate;
   
    public final static boolean USE_LOG_FILE = true;

    /**
     * Appenders require a null constructor. This workaround allows the GUI
     * logging to function, but is not ideal.
     * @see <a href="https://trac.openmicroscopy.org.uk/omero/ticket/1479">ticket:1479</a>
     */
    public static void configure(File logFile)
    {
        delegate = LogAppender.getInstance();
        logfile_delegate = new RollingFileAppender();
        logfile_delegate.setFile(logFile.getAbsolutePath());
        // 10MB is the default size
        logfile_delegate.setMaxBackupIndex(10);
        logfile_delegate.activateOptions();
        configured = true;
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
    protected void append(LoggingEvent arg0)
    {
        String s = getLayout().format(arg0);

        if (configured) {
            delegate.append(s);
            logfile_delegate.append(arg0);
        } else {
            System.err.println(s);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.AppenderSkeleton#close()
     */
    public void close()
    {
        return;
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.AppenderSkeleton#requiresLayout()
     */
    public boolean requiresLayout()
    {
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.apache.log4j.AppenderSkeleton#setLayout(org.apache.log4j.Layout)
     */
    public void setLayout(Layout layout)
    {
        super.setLayout(layout);
        if (logfile_delegate != null) {
            logfile_delegate.setLayout(layout);
        }
    }
}
