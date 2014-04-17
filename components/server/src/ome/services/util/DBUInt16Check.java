/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.util;

import ome.conditions.InternalException;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spring bean run on start-up to make sure that the bit-size of uint16 is correct.
 * A SQL upgrade script handles this issue for 5.1 and beyond.
 * 
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0
 */
public class DBUInt16Check {

    private static final Logger log = LoggerFactory.getLogger(DBUInt16Check.class);

    private static final String HQL_UPDATE = "UPDATE PixelsType SET bitSize = 16 WHERE value = 'uint16' AND bitSize != 16";

    private final SessionFactory sessionFactory;

    public DBUInt16Check(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void start() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            final int updateCount = session.createQuery(HQL_UPDATE).executeUpdate();
            if (updateCount > 0) {
                log.info("updated bit-size of uint16 among pixel types");
            } else if (log.isDebugEnabled()) {
                log.debug("verified bit-size of uint16 among pixel types");
            }
        } catch (HibernateException e) {
            final String message = "error in ensuring bit-size correctness of uint16";
            log.error(message, e);
            throw new InternalException(message);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
