/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Wrapper around a datasource which logs all connection failures.
 * 
 * @author Josh Moore, josh at glencoesoftwarecom
 * @since Beta4
 */
public class LoggingDataSource implements DataSource {

    public final static Log log = LogFactory.getLog(LoggingDataSource.class);

    private final DataSource delegate;
    
    public LoggingDataSource(final DataSource dataSource) {
        this.delegate = dataSource;
    }

    public Connection getConnection() throws SQLException {
        try {
            return delegate.getConnection();
        } catch (SQLException e) {
            log.error(e);
            throw e;
        }
    }

    public Connection getConnection(String username, String password)
            throws SQLException {
        try {
            return delegate.getConnection(username, password);
        } catch (SQLException e) {
            log.error(e);
            throw e;
        }
    }

    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    
}
