/*
 * ome.tools.spring.ManualFlushHibernateTemplate
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools.spring;

import org.hibernate.Session;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.orm.hibernate3.HibernateTemplate;

public class ManualFlushHibernateTemplate extends HibernateTemplate {

    @Override
    protected void checkWriteOperationAllowed(Session session)
            throws InvalidDataAccessApiUsageException {
        // do nothing.
    }

}
