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
    private final ImportConfig config;
    private final String saveDirectory;
    private final String logFileName;
    private final LogAppender delegate;
    private final RollingFileAppender logfile_delegate;
   
    public final static boolean USE_LOG_FILE = true;

    public LogAppenderProxy(ImportConfig config)
    {
        super(); 
        this.config = config;
        this.saveDirectory = config.getSaveDirectory();
        this.logFileName = config.getLogFileName();
        
        delegate = LogAppender.getInstance();
        logfile_delegate = new RollingFileAppender();
        String f = new File(saveDirectory, logFileName).getAbsolutePath();
        logfile_delegate.setFile(f);
        // 10MB is the default size
        logfile_delegate.setMaxBackupIndex(10);
        logfile_delegate.activateOptions();
    }

    @Override
    protected void append(LoggingEvent arg0)
    {
        String s = getLayout().format(arg0);
        delegate.append(s);
        logfile_delegate.append(arg0);
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
        logfile_delegate.setLayout(layout);
    }
}
