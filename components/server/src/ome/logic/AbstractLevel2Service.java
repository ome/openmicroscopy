/*
 * ome.logic.AbstractLevel2Service
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

import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.security.SecuritySystem;
import ome.services.query.QueryFactory;
import ome.services.util.BeanHelper;
import ome.system.SelfConfigurableService;
import ome.tools.hibernate.ExtendedMetadata;

/**
 * service level 2
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public abstract class AbstractLevel2Service implements SelfConfigurableService {

    private transient BeanHelper beanHelper = new BeanHelper(this.getClass());

    protected transient LocalUpdate iUpdate;

    protected transient LocalQuery iQuery;

    protected transient QueryFactory queryFactory;

    protected transient SecuritySystem sec;

    protected transient ExtendedMetadata metadata;

    // ~ Selfconfiguration (injection) for Non-JavaEE
    // =========================================================================

    /**
     * This method was previously called by the EJB container,
     * but is no longer needed. Instead, all configuration happens
     * within Spring.
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

    public final void setUpdateService(LocalUpdate update) {
        getBeanHelper().throwIfAlreadySet(this.iUpdate, update);
        this.iUpdate = update;
    }

    public final void setQueryFactory(QueryFactory qFactory) {
        getBeanHelper().throwIfAlreadySet(this.queryFactory, qFactory);
        this.queryFactory = qFactory;
    }

    public final void setQueryService(LocalQuery query) {
        getBeanHelper().throwIfAlreadySet(this.iQuery, query);
        this.iQuery = query;
    }

    public final void setSecuritySystem(SecuritySystem secSys) {
        getBeanHelper().throwIfAlreadySet(this.sec, secSys);
        this.sec = secSys;
    }

    public final void setExtendedMetadata(ExtendedMetadata em) {
        getBeanHelper().throwIfAlreadySet(this.metadata, em);
        this.metadata = em;
    }

    public final QueryFactory getQueryFactory() {
        return this.queryFactory;
    }

    public final SecuritySystem getSecuritySystem() {
        return this.sec;
    }

    public final ExtendedMetadata getExtendedMetadata() {
        return this.metadata;
    }
}
