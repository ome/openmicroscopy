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

// Third-party imports
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.event.MergeEvent;
import org.hibernate.event.MergeEventListener;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.springframework.orm.hibernate3.support.IdTransferringMergeEventListener;

// Application-internal dependencies

/**
 * responsible for responding to all Hibernate Events. Delegates tasks to
 * various components. It is assumed that graphs coming to the Hibernate methods
 * which produces these events have already been processed by the UpdateFilter.
 */
public class GlobalListener
        implements /* Turning off AutoFlushEventListener, DeleteEventListener,
        DirtyCheckEventListener, EvictEventListener, FlushEntityEventListener,
        FlushEventListener, InitializeCollectionEventListener,
        LoadEventListener, LockEventListener, MergeEventListener,
        PersistEventListener, RefreshEventListener, ReplicateEventListener,
        SaveOrUpdateEventListener,*/ /* this space intentionally left empty */
        /* BEFORE... *PreDeleteEventListener, PreInsertEventListener,
        /* and...... *PreLoadEventListener, PreUpdateEventListener,*/
        /* AFTER.... */PostDeleteEventListener, PostInsertEventListener,
        /* TRIGGERS. /PostLoadEventListener,*/ PostUpdateEventListener
{

    private static Log                   log       = LogFactory
                                                           .getLog(GlobalListener.class);

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
    }

    // 
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Acting as all hibernate listeners.
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
    
}
