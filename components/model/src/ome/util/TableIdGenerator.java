/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

import java.io.Serializable;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.MappingException;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.enhanced.OptimizerFactory;
import org.hibernate.id.enhanced.TableGenerator;
import org.hibernate.type.Type;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.orm.hibernate3.TransactionAwareDataSourceConnectionProvider;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * OMERO-specific id generation strategy. Combines both {@link TableGenerator}
 * and {@link OptimizerFactory.PooledOptimizer} into a single class because of
 * weirdness in their implementation. Instead, uses our own ome_nextval(?,?)
 * method to keep the Hibernate sequence values ({@link #hiValue}) in sync with
 * the database values.
 */
public class TableIdGenerator extends TableGenerator {

    private final static Log log = LogFactory.getLog(TableIdGenerator.class);

    long value;

    long hiValue = -1;

    private SqlAction sql = null;

    private DataSource ds = null;

    private TransactionTemplate tt = null;

    private SimpleJdbcTemplate jdbc = null;

    @Override
    public void configure(Type type, Properties params, Dialect dialect)
            throws MappingException {
        super.configure(type, params, dialect);
    }

    protected void init(SessionImplementor session) {
        ConnectionProvider cp = session.getJDBCContext().getConnectionManager()
                .getFactory().getConnectionProvider();

        if (sql != null) {
            return; // Already done.
        }

        if (cp instanceof TransactionAwareDataSourceConnectionProvider) {
            TransactionAwareDataSourceConnectionProvider tadscp = (TransactionAwareDataSourceConnectionProvider) cp;
            ds = tadscp.getDataSource();
            if (ds instanceof TransactionAwareDataSourceProxy) {
                TransactionAwareDataSourceProxy tadsp = (TransactionAwareDataSourceProxy) ds;
                ds = tadsp.getTargetDataSource();
                jdbc = new SimpleJdbcTemplate(ds);
                tt = new TransactionTemplate(
                        new DataSourceTransactionManager(ds));
            }
        }

        if (ds == null) {
            throw new RuntimeException(
                    "Unexpected ConnectionProvider found. Last datasource = "
                            + ds);
        }

    }

    public synchronized Serializable generate(final SessionImplementor session,
            Object obj) {

        init(session);

        if (hiValue < 0 || value >= hiValue) {
            tt.execute(new TransactionCallback<Object>() {
                public Object doInTransaction(TransactionStatus status) {
                    hiValue = sql.nextValue(ds, getSegmentValue(), getIncrementSize());
                    if (log.isDebugEnabled()) {
                        log.debug("Loaded new hiValue " + hiValue + " for "
                                + getSegmentValue());
                    }
                    return null;
                }
            });
            value = hiValue - getIncrementSize();
        }
        value++;
        return value;
    }

}
