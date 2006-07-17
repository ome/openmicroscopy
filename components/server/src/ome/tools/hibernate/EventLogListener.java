/* ome.tools.hibernate.EventLogListener
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

// Third-party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.hibernate.event.def.DefaultPostLoadEventListener;
import org.hibernate.event.def.DefaultPreLoadEventListener;

// Application-internal dependencies


/**
 * responsible for responding to all Hibernate Events. Delegates tasks to
 * various components. It is assumed that graphs coming to the Hibernate methods
 * which produces these events have already been processed by the 
 * {@link ome.tools.hibernate.UpdateFilter}
 */
public class EventLogListener
        implements /* Turning off AutoFlushEventListener, DeleteEventListener,
        DirtyCheckEventListener, EvictEventListener, FlushEntityEventListener,
        FlushEventListener, InitializeCollectionEventListener,
        LoadEventListener, LockEventListener, MergeEventListener,
        PersistEventListener, RefreshEventListener, ReplicateEventListener,
        SaveOrUpdateEventListener,*/ /* this space intentionally left empty */
        /* BEFORE... */ PreDeleteEventListener, PreInsertEventListener,
        /* and...... */ PreLoadEventListener, PreUpdateEventListener,
        /* AFTER.... */ PostDeleteEventListener, PostInsertEventListener,
        /* TRIGGERS. */ PostLoadEventListener, PostUpdateEventListener
{

    // TODO does LoadEvent call PreLoad/PostLoad?
    
    private static Log                   log       = LogFactory
                                                           .getLog(EventLogListener.class);

    /** actions to be performed on insert/update/delete */
    protected EventDiffHolder            actions;

    protected PreLoadEventListener       preLoad;
    
    protected PostLoadEventListener      postLoad;
    
    // responsibilities
    /*
     * security meta items (owner/event) validation?
     */
    // log.warn("our merger on:klass-" + obj.getClass());
    /**
     * main constructor. Replaces the default Hibernate merge listener with the
     * Spring IdTransferringMergeEventListener.
     */
    public EventLogListener(EventDiffHolder actions)
    {
        this.actions = actions;
        this.preLoad = new DefaultPreLoadEventListener();
        this.postLoad = new DefaultPostLoadEventListener();
    }

    // 
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Acting as all hibernate triggers
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //

    public void onPostDelete(PostDeleteEvent event)
    {
        actions.addDeleted(event.getEntity(), event.getId());
    }

    public void onPostInsert(PostInsertEvent event)
    {
        actions.addInserted(event.getEntity(), event.getId());
    }

    public void onPostUpdate(PostUpdateEvent event)
    {
        actions.addUpdated(event.getEntity(), event.getId());
    }

    public void onPostLoad(PostLoadEvent event)
    {
        this.postLoad.onPostLoad( event );
    }

    public void onPreLoad(PreLoadEvent event)
    {
        this.preLoad.onPreLoad( event );
    }

    public boolean onPreInsert(PreInsertEvent event)
    {
        return false;
    }

    public boolean onPreUpdate(PreUpdateEvent event)
    {
//        int[] changed = event.getPersister().findModified( 
//                event.getOldState(), 
//                event.getState(),
//                event.getEntity(),
//                event.getSource());
//        String[] names = event.getPersister().getClassMetadata()
//                .getPropertyNames();
//        
//        for (int i = 0; i < changed.length; i++)
//        {
//            System.out.println(names[changed[i]]);
//        }
        return false;
    }
    
    public boolean onPreDelete(PreDeleteEvent event)
    {
        return false;
    }
    
}
