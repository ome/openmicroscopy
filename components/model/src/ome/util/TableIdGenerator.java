/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

import java.io.Serializable;
import java.util.Properties;

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

    @Override
    public void configure(Type type, Properties params, Dialect dialect)
            throws MappingException {
        super.configure(type, params, dialect);
    }

    protected void init(SessionImplementor session) {

        if (sql != null) {
            return; // Already done.
        }

    }

    public synchronized Serializable generate(final SessionImplementor session,
            Object obj) {

        init(session);

        if (hiValue < 0 || value >= hiValue) {
            hiValue = sql.nextValue(getSegmentValue(), getIncrementSize());
            if (log.isDebugEnabled()) {
                log.debug("Loaded new hiValue " + hiValue + " for "
                        + getSegmentValue());
            }
            value = hiValue - getIncrementSize();
        }
        value++;
        return value;
    }

}
