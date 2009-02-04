/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.db;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.jdbc.TransactionalDriver;
import com.arjuna.ats.jdbc.common.jdbcPropertyManager;

/**
 * 
 * Based on code available from:
 * http://anonsvn.labs.jboss.com/labs/jbosstm/workspace/jhalliday/tomcat-integration/README.txt
 */
public class ArjunaXADataSourceWrapper implements XADataSource, DataSource {

    final private XADataSource dataSource;
    
    final private TransactionalDriver driver;

    /**
     * JNDI name for the resource to be bound to.
     */
    final private String jndiName;
    
    final private String username;
    
    final private String password;

    final private RecoveryManager recoveryManager;

    // Spring-lifecycle-management (OMERO-specific)
    // =========================================================================

    public ArjunaXADataSourceWrapper(XADataSource dataSource, String jndiName,
            String jndiInitial, String jndiPkg, String username, String password) {
        
        this.dataSource = dataSource;
        this.jndiName = jndiName;
        this.username = username;
        this.password = password;

        jdbcPropertyManager.propertyManager.setProperty(
                "Context.INITIAL_CONTEXT_FACTORY", jndiInitial);
        jdbcPropertyManager.propertyManager.setProperty(
                "Context.URL_PKG_PREFIXES", jndiPkg);

        TransactionReaper.create();
        this.recoveryManager = RecoveryManager.manager();
        recoveryManager.startRecoveryManagerThread();
        driver = new TransactionalDriver();
        try {
            setLogWriter(new PrintWriter(System.err));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void destroy() {
        recoveryManager.stop();
    }

    public XADataSource getUnwrappedXADataSource() {
        return dataSource;
    }


    // From: Jonathan Halliday jonathan.halliday@redhat.com
    // Implementation of the DataSource API is done by reusing the arjuna
    // TransactionalDriver. Its already got all the smarts for checking tx
    // context, enlisting resources etc so we just delegate to it.
    // All we need is some fudging to make the JNDI name stuff behave.

    public Connection getConnection() throws SQLException {
        return getConnection(username, password);
    }

    public Connection getConnection(String username, String password)
            throws SQLException {
        String url = TransactionalDriver.arjunaDriver + jndiName;
        Properties properties = new Properties();
        properties.setProperty(TransactionalDriver.userName, username);
        properties.setProperty(TransactionalDriver.password, password);
        return driver.connect(url, properties);
    }

    // Delegation
    // =========================================================================

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(XADataSource.class);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (isWrapperFor(iface)) {
            return (T) getUnwrappedXADataSource();
        } else {
            throw new SQLException("Not a wrapper for "
                    + iface.getCanonicalName());
        }
    }

    public XAConnection getXAConnection() throws SQLException {
        return dataSource.getXAConnection();
    }

    public XAConnection getXAConnection(String user, String password)
            throws SQLException {
        return dataSource.getXAConnection(user, password);
    }

    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);
    }

    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }
}
