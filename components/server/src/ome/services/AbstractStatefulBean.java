/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

import java.io.Serializable;

import javax.annotation.security.RolesAllowed;

import ome.api.StatefulServiceInterface;
import ome.api.local.LocalQuery;
import ome.security.SecuritySystem;
import ome.services.util.BeanHelper;
import ome.system.EventContext;
import ome.system.SelfConfigurableService;
import ome.system.SimpleEventContext;

/**
 * Base bean implementation for stateful services. Particularly useful is the
 * implementation of
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public abstract class AbstractStatefulBean implements SelfConfigurableService,
        StatefulServiceInterface, Serializable {

    private transient BeanHelper beanHelper = new BeanHelper(this.getClass());

    protected transient LocalQuery iQuery;

    protected transient SecuritySystem sec;

    /**
     * Query service Bean injector.
     * 
     * @param iQuery
     *            an <code>IQuery</code> service.
     */
    public final void setQueryService(LocalQuery iQuery) {
        getBeanHelper().throwIfAlreadySet(this.iQuery, iQuery);
        this.iQuery = iQuery;
    }

    public final void setSecuritySystem(SecuritySystem secSys) {
        getBeanHelper().throwIfAlreadySet(this.sec, secSys);
        this.sec = secSys;
    }

    public void selfConfigure() {
        getBeanHelper().configure(this);
    }
    
    protected BeanHelper getBeanHelper() {
        if (beanHelper == null) {
            beanHelper = new BeanHelper(this.getClass());
        }
        return beanHelper;
    }

    @RolesAllowed("user")
    public final EventContext getCurrentEventContext() {
        return new SimpleEventContext(sec.getEventContext());
    }

}
