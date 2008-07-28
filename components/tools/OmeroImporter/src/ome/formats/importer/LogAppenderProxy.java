/*
 * ome.formats.testclient.LogAppenderProxy
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

package ome.formats.importer;

import java.io.File;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.LoggingEvent;

public class LogAppenderProxy extends AppenderSkeleton implements Appender
{

    private static LogAppender delegate;
    private static RollingFileAppender logfile_delegate;
    
    public final static boolean USE_LOG_FILE = true;
    public final static String saveDirectory = System.getProperty("user.home") + File.separator + "omero" + File.separator + "log";
    public final static String logfileName = "importer.log";

    public LogAppenderProxy()
    {
        super(); 
        
        delegate = LogAppender.getInstance();
        
        logfile_delegate = new RollingFileAppender();
        System.err.println(saveDirectory);
        String f = new File(saveDirectory, logfileName).getAbsolutePath();
        logfile_delegate.setFile(f);
        logfile_delegate.setMaxFileSize("1000KB");
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
