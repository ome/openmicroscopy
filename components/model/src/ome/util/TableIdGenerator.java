/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

import java.io.Serializable;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.enhanced.OptimizerFactory;
import org.hibernate.id.enhanced.TableGenerator;
import org.hibernate.type.Type;

/**
 * OMERO-specific id generation strategy. Combines both {@link TableGenerator}
 * and {@link OptimizerFactory} into a single class because of
 * weirdness in their implementation. Instead, uses our own ome_nextval(?,?)
 * method to keep the Hibernate sequence values ({@link #hiValue}) in sync with
 * the database values.
 */
public class TableIdGenerator extends TableGenerator {

    private final static Logger log = LoggerFactory.getLogger(TableIdGenerator.class);

    long value;

    long hiValue = -1;

    private SqlAction sql = null;

    @Override
    public void configure(Type type, Properties params, Dialect dialect)
            throws MappingException {
        super.configure(type, params, dialect);
    }

    public void setSqlAction(SqlAction sql) {
        this.sql = sql;
    }

    public synchronized Serializable generate(final SessionImplementor session,
            Object obj) {

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
