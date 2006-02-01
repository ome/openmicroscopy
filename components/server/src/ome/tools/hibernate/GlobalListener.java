/* ome.tools.hibernate.GlobalListener
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.tools.hibernate;

// Java imports
import java.io.Serializable;
import java.util.Map;

// Third-party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.event.AutoFlushEvent;
import org.hibernate.event.AutoFlushEventListener;
import org.hibernate.event.DeleteEvent;
import org.hibernate.event.DeleteEventListener;
import org.hibernate.event.DirtyCheckEvent;
import org.hibernate.event.DirtyCheckEventListener;
import org.hibernate.event.EvictEvent;
import org.hibernate.event.EvictEventListener;
import org.hibernate.event.FlushEntityEvent;
import org.hibernate.event.FlushEntityEventListener;
import org.hibernate.event.FlushEvent;
import org.hibernate.event.FlushEventListener;
import org.hibernate.event.InitializeCollectionEvent;
import org.hibernate.event.InitializeCollectionEventListener;
import org.hibernate.event.LoadEvent;
import org.hibernate.event.LoadEventListener;
import org.hibernate.event.LockEvent;
import org.hibernate.event.LockEventListener;
import org.hibernate.event.MergeEvent;
import org.hibernate.event.MergeEventListener;
import org.hibernate.event.PersistEvent;
import org.hibernate.event.PersistEventListener;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.PostLoadEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PreDeleteEvent;
import org.hibernate.event.PreDeleteEventListener;
import org.hibernate.event.PreInsertEvent;
import org.hibernate.event.PreInsertEventListener;
import org.hibernate.event.PreLoadEvent;
import org.hibernate.event.PreLoadEventListener;
import org.hibernate.event.PreUpdateEvent;
import org.hibernate.event.PreUpdateEventListener;
import org.hibernate.event.RefreshEvent;
import org.hibernate.event.RefreshEventListener;
import org.hibernate.event.ReplicateEvent;
import org.hibernate.event.ReplicateEventListener;
import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.event.SaveOrUpdateEventListener;
import org.hibernate.event.SessionEventListenerConfig;
import org.springframework.orm.hibernate3.support.IdTransferringMergeEventListener;

// Application-internal dependencies

/**
 * responsible for responding to all Hibernate Events. Delegates tasks to
 * various components. It is assumed that graphs coming to the Hibernate methods
 * which produces these events have already been processed by the UpdateFilter.
 */
public class GlobalListener
        implements AutoFlushEventListener, DeleteEventListener,
        DirtyCheckEventListener, EvictEventListener, FlushEntityEventListener,
        FlushEventListener, InitializeCollectionEventListener,
        LoadEventListener, LockEventListener, MergeEventListener,
        PersistEventListener, RefreshEventListener, ReplicateEventListener,
        SaveOrUpdateEventListener, /* this space intentionally left empty */
        /* BEFORE... */PreDeleteEventListener, PreInsertEventListener,
        /* and...... */PreLoadEventListener, PreUpdateEventListener,
        /* AFTER.... */PostDeleteEventListener, PostInsertEventListener,
        /* TRIGGERS. */PostLoadEventListener, PostUpdateEventListener
{

    private static Log                   log       = LogFactory
                                                           .getLog(GlobalListener.class);

    /** default listeners to which GlobalListener delegates */
    protected SessionEventListenerConfig listeners = new SessionEventListenerConfig();

    /** actions to be performed on insert/update/delete */
    protected EventDiffHolder            actions;

    // responsibilities
    /*
     * security meta items (owner/event) validation?
     */
    // log.warn("our merger on:klass-" + obj.getClass());
    /**
     * main constructor. Replaces the default Hibernate merge listener with the
     * Spring IdTransferringMergeEventListener.
     */
    public GlobalListener(EventDiffHolder actions)
    {
        this.actions = actions;
        listeners.setMergeEventListener(new IdTransferringMergeEventListener());
    }

    // 
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Acting as all hibernate listeners.
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //

    public boolean onAutoFlush(AutoFlushEvent event) throws HibernateException
    {
        return listeners.getAutoFlushEventListener().onAutoFlush(event);
    }

    public void onDelete(DeleteEvent event) throws HibernateException
    {
        listeners.getDeleteEventListener().onDelete(event);
    }

    public boolean onDirtyCheck(DirtyCheckEvent event)
            throws HibernateException
    {
        return listeners.getDirtyCheckEventListener().onDirtyCheck(event);
    }

    public void onEvict(EvictEvent event) throws HibernateException
    {
        listeners.getEvictEventListener().onEvict(event);
    }

    public void onFlushEntity(FlushEntityEvent event) throws HibernateException
    {
        listeners.getFlushEntityEventListener().onFlushEntity(event);
    }

    public void onFlush(FlushEvent event) throws HibernateException
    {
        listeners.getFlushEventListener().onFlush(event);
    }

    public void onInitializeCollection(InitializeCollectionEvent event)
            throws HibernateException
    {
        listeners.getInitializeCollectionEventListener()
                .onInitializeCollection(event);
    }

    public Object onLoad(LoadEvent event, LoadType loadType)
            throws HibernateException
    { // TODO: could check read permissions here.
        return listeners.getLoadEventListener().onLoad(event, loadType);
    }

    public void onLock(LockEvent event) throws HibernateException
    {
        listeners.getLockEventListener().onLock(event);
    }

    public Object onMerge(MergeEvent event) throws HibernateException
    {
        return listeners.getMergeEventListener().onMerge(event);
    }

    public Object onMerge(MergeEvent event, Map copiedAlready)
            throws HibernateException
    {
        return listeners.getMergeEventListener().onMerge(event, copiedAlready);
    }

    public void onPersist(PersistEvent event) throws HibernateException
    {
        listeners.getCreateEventListener().onPersist(event);
    }

    public void onPersist(PersistEvent event, Map createdAlready)
            throws HibernateException
    {
        listeners.getCreateEventListener().onPersist(event, createdAlready);
    }

    public void onPostDelete(PostDeleteEvent event)
    {
        listeners.getPostDeleteEventListener().onPostDelete(event);
        actions.addDeleted(event.getEntity(), event.getId());
    }

    public void onPostInsert(PostInsertEvent event)
    {
        listeners.getPostInsertEventListener().onPostInsert(event);
        actions.addInserted(event.getEntity(), event.getId());
    }

    public void onPostLoad(PostLoadEvent event)
    {
        listeners.getPostLoadEventListener().onPostLoad(event);
    }

    public void onPostUpdate(PostUpdateEvent event)
    {
        listeners.getPostUpdateEventListener().onPostUpdate(event);
        actions.addUpdated(event.getEntity(), event.getId());
    }

    public boolean onPreDelete(PreDeleteEvent event)
    {
        return listeners.getPreDeleteEventListener().onPreDelete(event);
    }

    public boolean onPreInsert(PreInsertEvent event)
    {
        return listeners.getPreInsertEventListener().onPreInsert(event);
    }

    public void onPreLoad(PreLoadEvent event)
    {
        listeners.getPreLoadEventListener().onPreLoad(event);
    }

    public boolean onPreUpdate(PreUpdateEvent event)
    {
        return listeners.getPreUpdateEventListener().onPreUpdate(event);
    }

    public void onRefresh(RefreshEvent event) throws HibernateException
    {
        listeners.getRefreshEventListener().onRefresh(event);
    }

    public void onReplicate(ReplicateEvent event) throws HibernateException
    {
        listeners.getReplicateEventListener().onReplicate(event);
    }

    public Serializable onSaveOrUpdate(SaveOrUpdateEvent event)
            throws HibernateException
    {
        return listeners.getSaveOrUpdateEventListener().onSaveOrUpdate(event);
    }

}
