/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools.spring;

import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * WORKAROUND for lack of JTA transactions. Rather than start using JTA with a
 * single database, this {@link JdbcTemplate} delegates to a
 * {@link HibernateTemplate} for its {@link java.sql.Connection}.
 * 
 * If JTA is ever enabled, this class can be safely removed.
 */
public class DelegatingJdbcTemplate extends SimpleJdbcTemplate {

    public DelegatingJdbcTemplate(HibernateTemplate template) {
        super(new Datasource(template));
    }

    private static class Datasource extends AbstractDataSource {

        protected final HibernateTemplate template;

        Datasource(HibernateTemplate template) {
            this.template = template;
        }

        public Connection getConnection() throws SQLException {
            return getConnection(null, null);
        }

        public Connection getConnection(String username, String password)
                throws SQLException {
            return (Connection) template.execute(new HibernateCallback() {
                public Object doInHibernate(Session session)
                        throws HibernateException, SQLException {
                    return session.connection();
                }
            }, true);
        }
    }

}
