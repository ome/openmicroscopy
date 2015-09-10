/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

import java.io.Serializable;

import ome.annotations.RolesAllowed;
import ome.api.StatefulServiceInterface;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
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

    protected transient LocalUpdate iUpdate;

    protected transient SecuritySystem sec;

    /**
     * True if any write operation took place on this bean.
     * Allows for updating the database representation if needed.
     * @see <a href="http://trac.openmicroscopy.org/ome/ticket/1961">ticket:1961</a>
     */
    protected transient boolean modified;

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

    public final void setUpdateService(LocalUpdate update) {
        getBeanHelper().throwIfAlreadySet(this.iUpdate, update);
        this.iUpdate = update;
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

    protected boolean isModified() {
        return modified;
    }

    protected void modified() {
        modified = true;
    }

    @RolesAllowed("user")
    public final EventContext getCurrentEventContext() {
        return new SimpleEventContext(sec.getEventContext());
    }

}
