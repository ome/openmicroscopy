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

import javax.annotation.PostConstruct;

import ome.security.SecuritySystem;
import ome.services.query.QueryFactory;
import ome.services.util.BeanHelper;
import ome.system.OmeroContext;
import ome.system.SelfConfigurableService;

import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * service level 1
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date:
 *          2006-12-15 11:39:34 +0100 (Fri, 15 Dec 2006) $) </small>
 * @since OMERO 3.0
 */
public abstract class AbstractLevel1Service implements SelfConfigurableService {

    protected transient QueryFactory queryFactory;
    
    protected transient SecuritySystem securitySystem;

    /**
     * Performs the necessary {@link OmeroContext} lookup and calls
     * {@link OmeroContext#applyBeanPropertyValues(Object, Class)} when
     * necessary.
     */
    protected transient BeanHelper beanHelper = new BeanHelper(this.getClass());

    public final void setQueryFactory(QueryFactory factory) {
        beanHelper.throwIfAlreadySet(this.queryFactory, factory);
        this.queryFactory = factory;
    }
    
    public QueryFactory getQueryFactory() {
        return this.queryFactory;
    }

    public final void setSecuritySystem(SecuritySystem security) {
        beanHelper.throwIfAlreadySet(this.securitySystem, security);
        this.securitySystem = security;
    }
    
    public SecuritySystem getSecuritySystem() {
        return this.securitySystem;
    }
    
    /**
     * This method will be called in a container via a {@link PostConstruct}
     * action. If this bean is not being used as an EJB, then
     * {@link #selfConfigure()} will not be called, rather the context will
     * perform the configuration.
     */
    public void selfConfigure() {
        beanHelper.configure(this);
    }

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
