/*
 * ome.logic.AbstractLevel1Service
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.logic;

// Java imports

// Third-party libraries
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

// Application-internal dependencies

/**
 * service level 1
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public abstract class AbstractLevel1Service extends AbstractBean {

    // ~ HibernateDaoSupport methods
    // =========================================================================

    private HibernateDaoSupport support = new HibernateDaoSupport() { /* ez */
    };

    /**
     * delegates to {@link HibernateDaoSupport}. Used during initialization to
     * create a {@link HibernateTemplate}
     * 
     * @see HibernateDaoSupport#setSessionFactory(SessionFactory)
     */
    public final void setSessionFactory(SessionFactory sessionFactory) {
        support.setSessionFactory(sessionFactory);
    }

    /**
     * delegates to {@link HibernateDaoSupport} to get the current
     * {@link SessionFactory}
     * 
     * @see HibernateDaoSupport#getSessionFactory()
     */
    public final SessionFactory getSessionFactory() {
        return support.getSessionFactory();
    }

    /**
     * delegates to {@link HibernateDaoSupport}. Used during initialization to
     * set the current {@link HibernateTemplate}
     * 
     * @see HibernateDaoSupport#setHibernateTemplate(HibernateTemplate)
     */
    public final void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        support.setHibernateTemplate(hibernateTemplate);
    }

    /**
     * delegates to {@link HibernateDaoSupport} to get the current
     * {@link HibernateTemplate}
     * 
     * @see HibernateDaoSupport#getHibernateTemplate()
     */
    public final HibernateTemplate getHibernateTemplate() {
        return support.getHibernateTemplate();
    }

}
