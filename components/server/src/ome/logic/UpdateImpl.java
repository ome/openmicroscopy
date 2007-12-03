/*
 * ome.logic.UpdateImpl
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
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

// Third-party libraries
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.RemoteBindings;
import org.jboss.annotation.security.SecurityDomain;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

// Application-internal dependencies
import ome.api.IUpdate;
import ome.api.ServiceInterface;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.model.IObject;
import ome.services.util.OmeroAroundInvoke;
import ome.tools.hibernate.UpdateFilter;
import ome.util.Utils;

/**
 * implementation of the IUpdate service interface
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional(readOnly = false)
@Stateless
@Remote(IUpdate.class)
@RemoteBindings({
    @RemoteBinding(jndiBinding = "omero/remote/ome.api.IUpdate"),
    @RemoteBinding(jndiBinding = "omero/secure/ome.api.IUpdate",
		   clientBindUrl="sslsocket://0.0.0.0:3843")
})
@Local(LocalUpdate.class)
@LocalBinding(jndiBinding = "omero/local/ome.api.local.LocalUpdate")
@SecurityDomain("OmeroSecurity")
@Interceptors( { OmeroAroundInvoke.class, SimpleLifecycle.class })
public class UpdateImpl extends AbstractLevel1Service implements LocalUpdate {
    protected transient LocalQuery localQuery;

    public final void setQueryService(LocalQuery query) {
        getBeanHelper().throwIfAlreadySet(this.localQuery, query);
        this.localQuery = query;
    }

    public Class<? extends ServiceInterface> getServiceInterface() {
        return IUpdate.class;
    };

    // ~ LOCAL PUBLIC METHODS
    // =========================================================================

    @RolesAllowed("user")
    public void rollback() {
        getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                session.connection().rollback();
                return null;
            };
        });
    }

    @RolesAllowed("user")
    public void flush() {
        getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                session.flush();
                return null;
            };
        });
    }

    @RolesAllowed("user")
    public void commit() {
        getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                session.connection().commit();
                return null;
            };
        });
    }

    // ~ INTERFACE METHODS
    // =========================================================================

    @RolesAllowed("user")
    public void saveObject(IObject graph) {
        doAction(graph, new UpdateAction<IObject>() {
            @Override
            public IObject run(IObject value, UpdateFilter filter) {
                return internalSave(value, filter);
            }
        });
    }

    @RolesAllowed("user")
    public IObject saveAndReturnObject(IObject graph) {
        return doAction(graph, new UpdateAction<IObject>() {
            @Override
            public IObject run(IObject value, UpdateFilter filter) {
                return internalSave(value, filter);
            }
        });
    }

    @RolesAllowed("user")
    public void saveCollection(Collection graph) {
        doAction(graph, new UpdateAction<Collection>() {
            @Override
            public Collection run(Collection value, UpdateFilter filter) {
                for (Object o : value) {
                    IObject obj = (IObject) o;
                    obj = internalSave(obj, filter);
                }
                return null;
            }
        });
    }

    @RolesAllowed("user")
    public Collection saveAndReturnCollection(Collection graph) {
        throw new RuntimeException("Not implemented yet.");
    }

    @RolesAllowed("user")
    public void saveMap(Map graph) {
        throw new RuntimeException("Not implemented yet.");
    }

    @RolesAllowed("user")
    public IObject[] saveAndReturnArray(IObject[] graph) {
        return doAction(graph, new UpdateAction<IObject[]>() {
            @Override
            public IObject[] run(IObject[] value, UpdateFilter filter) {
                IObject[] copy = new IObject[value.length];
                for (int i = 0; i < value.length; i++) {
                    copy[i] = internalSave(value[i], filter);
                }
                return copy;
            }
        });
    }

    @RolesAllowed("user")
    public void saveArray(IObject[] graph) {
        doAction(graph, new UpdateAction<IObject[]>() {
            @Override
            public IObject[] run(IObject[] value, UpdateFilter filter) {
                IObject[] copy = new IObject[value.length];
                for (int i = 0; i < value.length; i++) {
                    copy[i] = internalSave(value[i], filter);
                }
                return copy;
            }
        });
    }

    @RolesAllowed("user")
    public Map saveAndReturnMap(Map map) {
        // TODO Auto-generated method stub
        // return null;
        throw new RuntimeException("Not implemented yet.");
    }

    @RolesAllowed("user")
    public void deleteObject(IObject row) {
        if (row == null) {
            return;
        }
        if (row.getId() == null) {
            throw new ApiUsageException(
                    "Non-managed IObject entity cannot be deleted. Must have an id.");
        }
        doAction(row, new UpdateAction<IObject>() {
            @Override
            public IObject run(IObject value, UpdateFilter filter) {
                internalDelete(value, filter);
                return null;
            }
        });
    }

    // ~ Internals
    // =========================================================
    private void beforeUpdate(Object argument, UpdateFilter filter) {

        if (argument == null) {
            throw new IllegalArgumentException(
                    "Argument to save cannot be null.");
        }

        if (getBeanHelper().getLogger().isDebugEnabled()) {
            getBeanHelper().getLogger().debug(" Saving event before merge. ");
        }

    }

    /**
     * Note if we use anything other than merge here, functionality from
     * {@link ome.tools.hibernate.MergeEventListener} needs to be moved to
     * {@link UpdateFilter} or to another event listener.
     */
    protected IObject internalSave(IObject obj, UpdateFilter filter) {
        if (getBeanHelper().getLogger().isDebugEnabled()) {
            getBeanHelper().getLogger().debug(" Internal save. ");
        }

        IObject result = (IObject) filter.filter(null, obj);
        result = (IObject) getHibernateTemplate().merge(result);
        return result;
    }

    protected void internalDelete(IObject obj, UpdateFilter filter) {
        if (getBeanHelper().getLogger().isDebugEnabled()) {
            getBeanHelper().getLogger().debug(" Internal delete. ");
        }

        getHibernateTemplate().delete(
                getHibernateTemplate().load(Utils.trueClass(obj.getClass()),
                        obj.getId()));
    }

    private void afterUpdate(UpdateFilter filter) {

        if (getBeanHelper().getLogger().isDebugEnabled()) {
            getBeanHelper().getLogger().debug(" Post-save cleanup. ");
        }

        // Clean up
        getHibernateTemplate().flush();
        filter.unloadReplacedObjects();

    }

    private <T> T doAction(T graph, UpdateAction<T> action) {
        T retVal;
        UpdateFilter filter = new UpdateFilter();
        try {
            beforeUpdate(graph, filter);
            retVal = action.run(graph, filter);
            afterUpdate(filter);
        } finally {
            // currently nothing.
        }
        return retVal;
    }

    private abstract class UpdateAction<T> {
        public abstract T run(T value, UpdateFilter filter);
    }

}
