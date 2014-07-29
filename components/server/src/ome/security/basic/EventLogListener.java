/*
 * ome.security.basic.EventLogListener
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

// Java imports

// Third-party imports
import ome.model.IObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;

/**
 * responsible for responding to all Hibernate Events. Delegates tasks to
 * various components. It is assumed that graphs coming to the Hibernate methods
 * which produces these events have already been processed by the
 * {@link ome.tools.hibernate.UpdateFilter}
 */
public class EventLogListener implements PostUpdateEventListener,
        PostDeleteEventListener, PostInsertEventListener {

    private static final long serialVersionUID = 3245068515908082533L;

    private static Logger log = LoggerFactory.getLogger(EventLogListener.class);

    protected final CurrentDetails cd;

    /**
     * main constructor.
     */
    public EventLogListener(CurrentDetails cd) {
        this.cd = cd;
    }

    // 
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Acting as all hibernate triggers
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //

    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return true;
    }

    public void onPostDelete(PostDeleteEvent event) {
        add("DELETE", event.getEntity());
    }

    public void onPostInsert(PostInsertEvent event) {
        add("INSERT", event.getEntity());
    }

    public void onPostUpdate(PostUpdateEvent event) {
        add("UPDATE", event.getEntity());
    }

    // ~ Helpers
    // =========================================================================

    void add(String action, Object entity) {
        if (entity instanceof IObject) {
            Class klass = entity.getClass();
            Long id = ((IObject) entity).getId();
            cd.addLog(action, klass, id);
        }
    }

}
