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

import javax.annotation.PostConstruct;

import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.security.SecuritySystem;
import ome.services.query.QueryFactory;
import ome.services.util.BeanHelper;
import ome.system.SelfConfigurableService;

/**
 * service level 2
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public abstract class AbstractLevel2Service implements SelfConfigurableService {

    protected transient BeanHelper beanHelper = new BeanHelper(this.getClass());
    
    protected transient LocalUpdate iUpdate;

    protected transient LocalQuery iQuery;
    
    protected transient QueryFactory queryFactory;
    
    protected transient SecuritySystem sec;

    // ~ Selfconfiguration (injection) for Non-JavaEE
    // =========================================================================

    /**
     * This method will be called in a container via a {@link PostConstruct}
     * action. If this bean is not being used as an EJB, then
     * {@link #selfConfigure()} will not be called, rather the context will
     * perform the configuration.
     */
    public void selfConfigure() {
        beanHelper.configure(this);
    }
    
    public final void setUpdateService(LocalUpdate update) {
        beanHelper.throwIfAlreadySet(this.iUpdate, update);
        this.iUpdate = update;
    }

    public final void setQueryFactory(QueryFactory qFactory) {
        beanHelper.throwIfAlreadySet(this.queryFactory, qFactory);
        this.queryFactory = qFactory;
    }
    
    public final void setQueryService(LocalQuery query) {
        beanHelper.throwIfAlreadySet(this.iQuery, query);
        this.iQuery = query;
    }

    public final void setSecuritySystem(SecuritySystem secSys) {
        beanHelper.throwIfAlreadySet(this.sec, secSys);
        this.sec = secSys;
    }

    public final QueryFactory getQueryFactory() {
        return this.queryFactory;
    }
    
    public final SecuritySystem getSecuritySystem() {
        return this.sec;
    }
}
