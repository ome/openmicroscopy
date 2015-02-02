/*
 *   $Id$
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

import ome.services.query.QueryFactory;
import ome.services.util.BeanHelper;
import ome.system.OmeroContext;
import ome.system.SelfConfigurableService;

import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

/**
 * service level 1
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date: 2006-12-15
 *          11:39:34 +0100 (Fri, 15 Dec 2006) $) </small>
 * @since OMERO 3.0
 */
public abstract class AbstractLevel1Service implements SelfConfigurableService {

    protected transient QueryFactory queryFactory;

    protected transient SessionFactory sessionFactory;
    /**
     * Performs the necessary {@link OmeroContext} lookup and calls
     * {@link OmeroContext#applyBeanPropertyValues(Object, Class)} when
     * necessary.
     */
    private transient BeanHelper beanHelper = new BeanHelper(this.getClass());

    public final void setQueryFactory(QueryFactory factory) {
        getBeanHelper().throwIfAlreadySet(this.queryFactory, factory);
        this.queryFactory = factory;
    }

    public QueryFactory getQueryFactory() {
        return this.queryFactory;
    }

    /**
     * This method was previously called by the EJB container, but is no longer
     * needed. Instead, all configuration happens within Spring.
     */
    public void selfConfigure() {
        getBeanHelper().configure(this);
    }

    protected BeanHelper getBeanHelper() {
        if (beanHelper == null) {
            beanHelper = new BeanHelper(this.getClass());
        }
        return beanHelper;
    }

    /**
     * delegates to {@link HibernateDaoSupport}. Used during initialization to
     * create a {@link HibernateTemplate}
     * 
     * @see HibernateDaoSupport#setSessionFactory(SessionFactory)
     */
    public final void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * delegates to {@link HibernateDaoSupport} to get the current
     * {@link SessionFactory}
     * 
     * @see HibernateDaoSupport#getSessionFactory()
     */
    public final SessionFactory getSessionFactory() {
        return sessionFactory;
    }

}
