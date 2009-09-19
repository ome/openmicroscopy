/*
 * ome.formats.importer.util.LogAppenderProxy
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.importer.util;

import java.io.File;

import ome.formats.importer.ImportConfig;
import ome.formats.importer.gui.LogAppender;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.LoggingEvent;

public class LogAppenderProxy extends AppenderSkeleton implements Appender
{
    private static boolean configured;
    private static Layout layout;
    private static LogAppender delegate;
    private static RollingFileAppender logfile_delegate;
   
    public final static boolean USE_LOG_FILE = true;

    /**
     * Appenders require a null constructor. This workaround allows the gui
     * logging to function, but is not ideal.
     * @see <a href="https://trac.openmicroscopy.org.uk/omero/ticket/1479">ticket:1479</a>
     */
    public static void configure(File logFile)
    {
        delegate = LogAppender.getInstance();
        logfile_delegate = new RollingFileAppender();
        logfile_delegate.setFile(logFile.getAbsolutePath());
        if (layout != null) {
            logfile_delegate.setLayout(layout);
        }
        // 10MB is the default size
        logfile_delegate.setMaxBackupIndex(10);
        logfile_delegate.activateOptions();
        configured = true;
    }

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

    public void close()
    {
        return;
    }

    public boolean requiresLayout()
    {
        return true;
    }
    
    public void setLayout(Layout layout)
    {
        super.setLayout(layout);
        this.layout = layout;
        if (logfile_delegate != null) {
            logfile_delegate.setLayout(layout);
        }
    }
}
