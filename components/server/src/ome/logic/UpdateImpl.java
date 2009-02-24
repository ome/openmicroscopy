/*
 *   $Ids$
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

import java.sql.SQLException;
import java.util.Collection;

import ome.annotations.RolesAllowed;
import ome.api.IUpdate;
import ome.api.ServiceInterface;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.model.IObject;
import ome.model.meta.EventLog;
import ome.parameters.Parameters;
import ome.services.fulltext.EventLogLoader;
import ome.services.fulltext.FullTextBridge;
import ome.services.fulltext.FullTextIndexer;
import ome.services.fulltext.FullTextThread;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.tools.hibernate.UpdateFilter;
import ome.util.Utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * implementation of the IUpdate service interface
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
@Transactional(readOnly = false)
public class UpdateImpl extends AbstractLevel1Service implements LocalUpdate {

    private final Log log = LogFactory.getLog(UpdateImpl.class);

    protected transient LocalQuery localQuery;

    protected transient Executor executor;

    protected transient SessionManager sessionManager;

    protected transient FullTextBridge fullTextBridge;

    public final void setQueryService(LocalQuery query) {
        getBeanHelper().throwIfAlreadySet(this.localQuery, query);
        this.localQuery = query;
    }

    public void setExecutor(Executor executor) {
        getBeanHelper().throwIfAlreadySet(this.executor, executor);
        this.executor = executor;
    }

    public void setSessionManager(SessionManager sessionManager) {
        getBeanHelper().throwIfAlreadySet(this.sessionManager, sessionManager);
        this.sessionManager = sessionManager;
    }

    public void setFullTextBridge(FullTextBridge fullTextBridge) {
        getBeanHelper().throwIfAlreadySet(this.fullTextBridge, fullTextBridge);
        this.fullTextBridge = fullTextBridge;
    }

    public Class<? extends ServiceInterface> getServiceInterface() {
        return IUpdate.class;
    };

    // ~ LOCAL PUBLIC METHODS
    // =========================================================================

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

    // ~ INTERFACE METHODS
    // =========================================================================

    @RolesAllowed("user")
    public void saveObject(IObject graph) {
        doAction(graph, new UpdateAction<IObject>() {
            @Override
            public IObject run(IObject value, UpdateFilter filter, Session s) {
                return internalMerge(value, filter, s);
            }
        });
    }

    @RolesAllowed("user")
    public IObject saveAndReturnObject(IObject graph) {
        return doAction(graph, new UpdateAction<IObject>() {
            @Override
            public IObject run(IObject value, UpdateFilter filter, Session s) {
                return internalMerge(value, filter, s);
            }
        });
    }

    @RolesAllowed("user")
    public void saveCollection(Collection graph) {
        doAction(graph, new UpdateAction<Collection>() {
            @Override
            public Collection run(Collection value, UpdateFilter filter, Session s) {
                for (Object o : value) {
                    IObject obj = (IObject) o;
                    obj = internalMerge(obj, filter, s);
                }
                return null;
            }
        });
    }

    @RolesAllowed("user")
    public IObject[] saveAndReturnArray(IObject[] graph) {
        return doAction(graph, new UpdateAction<IObject[]>() {
            @Override
            public IObject[] run(IObject[] value, UpdateFilter filter, Session s) {
                IObject[] copy = new IObject[value.length];
                for (int i = 0; i < value.length; i++) {
                    if (i%1000 == 0) {
                        s.flush();
                        s.clear();
                    }
                    copy[i] = internalMerge(value[i], filter, s);
                }
                return copy;
            }
        });
    }
    
    @RolesAllowed("user")
    public long[] saveAndReturnIds(IObject[] graph) {
        
        if (graph == null || graph.length == 0) {
            return new long[0]; // EARLY EXIT!
        }
        
        final long[] ids = new long[graph.length];
        doAction(graph, new UpdateAction<IObject[]>() {
            @Override
            public IObject[] run(IObject[] value, UpdateFilter filter, Session s) {
                for (int i = 0; i < value.length; i++) {
                    if (i%1000 == 0) {
                        s.flush();
                        s.clear();
                    }
                    ids[i] = internalSave(value[i], filter, s);
                }
                return null;
            }
        });
        return ids;
    }

    @RolesAllowed("user")
    public void saveArray(IObject[] graph) {
        doAction(graph, new UpdateAction<IObject[]>() {
            @Override
            public IObject[] run(IObject[] value, UpdateFilter filter, Session s) {
                IObject[] copy = new IObject[value.length];
                for (int i = 0; i < value.length; i++) {
                    copy[i] = internalMerge(value[i], filter, s);
                }
                return copy;
            }
        });
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
            public IObject run(IObject value, UpdateFilter filter, Session s) {
                internalDelete(value, filter);
                return null;
            }
        });
    }

    @RolesAllowed("system")
    public void indexObject(IObject row) {
        if (row == null || row.getId() == null) {
            throw new ValidationException(
                    "Non-managed object cannot be indexed.");
        }

        CreationLogLoader logs = new CreationLogLoader(localQuery, row);
        FullTextIndexer fti = new FullTextIndexer(logs);

        final RuntimeException[] e = new RuntimeException[1];
        final FullTextThread ftt = new FullTextThread(sessionManager, executor,
                fti, this.fullTextBridge, true);
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    ftt.run();
                } catch (RuntimeException ex) {
                    e[0] = ex;
                }
            }
        };
        t.start();
        try {
            t.join();
        } catch (Exception e2) {
            log.error("Exception during FullTextThread.join", e2);
        }
        if (e[0] != null) {
            throw e[0];
        }
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
    protected Long internalSave(IObject obj, UpdateFilter filter, Session session) {
        if (getBeanHelper().getLogger().isDebugEnabled()) {
            getBeanHelper().getLogger().debug(" Internal save. ");
        }

        IObject result = (IObject) filter.filter(null, obj);
        Long id = (Long) getHibernateTemplate().save(result);
        return id;
    }
    
    /**
     * Note if we use anything other than merge here, functionality from
     * {@link ome.tools.hibernate.MergeEventListener} needs to be moved to
     * {@link UpdateFilter} or to another event listener.
     */
    protected IObject internalMerge(IObject obj, UpdateFilter filter, Session session) {
        if (getBeanHelper().getLogger().isDebugEnabled()) {
            getBeanHelper().getLogger().debug(" Internal merge. ");
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

    @SuppressWarnings("unchecked")
    private <T> T doAction(final T graph, final UpdateAction<T> action) {
        final UpdateFilter filter = new UpdateFilter();
        return (T) getHibernateTemplate().execute(new HibernateCallback(){
            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                
                T retVal;
                beforeUpdate(graph, filter);
                retVal = action.run(graph, filter, session);
                afterUpdate(filter);
                return retVal;

            }});
    }

    private abstract class UpdateAction<T> {
        public abstract T run(T value, UpdateFilter filter, Session s);
    }

}

/**
 * {@link EventLogLoader} which loads a single instance.
 */
class CreationLogLoader extends EventLogLoader {

    final private LocalQuery query;

    private IObject obj;

    public CreationLogLoader(LocalQuery query, IObject obj) {
        this.query = query;
        this.obj = obj;
    }

    @Override
    public EventLog query() {
        if (obj == null) {
            return null;
        } else {
            EventLog el = query.findByQuery("select el from EventLog el "
                    + "where el.action = 'INSERT' and "
                    + "el.entityType = :type and " + "el.entityId = :id",
                    new Parameters()
                            .addString("type", obj.getClass().getName()).addId(
                                    obj.getId()));
            obj = null;
            return el;
        }
    }

    @Override
    public long more() {
        return 0;
    }

}
