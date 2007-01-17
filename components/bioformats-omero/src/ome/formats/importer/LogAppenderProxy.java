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

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class LogAppenderProxy extends AppenderSkeleton implements Appender
{

    private static LogAppender delegate;

    public LogAppenderProxy()
    {
        super();
        delegate = LogAppender.getInstance();
    }

    @Override
    protected void append(LoggingEvent arg0)
    {
        String s = getLayout().format(arg0);
        delegate.append(s);
    }

    public void close()
    {
        return;
    }

    public boolean requiresLayout()
    {
        return true;
    }
}
