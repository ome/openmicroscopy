/*
 * ome.security.ACLEventListener
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

// Java imports

// Third-party imports
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.tools.hibernate.HibernateUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreDeleteEventListener;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.event.spi.PreLoadEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;

/**
 * responsible for intercepting all pre-INSERT, pre-UPDATE, pre-DELETE, and
 * post-LOAD events to apply access control. For each event, a call is made to
 * the {@link SecuritySystem} to see if the event is allowed, and if not,
 * another call is made to the {@link  SecuritySystem} to throw a
 * {@link SecurityViolation}.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see SecuritySystem
 * @see SecurityViolation
 * @since 3.0-M3
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class ACLEventListener implements
/* BEFORE... */PreDeleteEventListener, PreInsertEventListener,
/* and...... */PreLoadEventListener, PreUpdateEventListener,
/* AFTER.... */PostDeleteEventListener, PostInsertEventListener,
/* TRIGGERS. */PostLoadEventListener, PostUpdateEventListener {

    private static final long serialVersionUID = 3603644089117965153L;

    private static Logger log = LoggerFactory.getLogger(ACLEventListener.class);

    private final ACLVoter aclVoter;

    /**
     * main constructor. controls access to individual db rows..
     */
    public ACLEventListener(ACLVoter aclVoter) {
        this.aclVoter = aclVoter;
    }

    // 
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Acting as all hibernate triggers
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //

    /** unused */
    public void onPostDelete(PostDeleteEvent event) {
    }

    /** unused */
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }

    /** unused */
    public void onPostInsert(PostInsertEvent event) {
    }

    /** unused */
    public void onPostUpdate(PostUpdateEvent event) {
    }

    /** unused */
    public void onPreLoad(PreLoadEvent event) {
    }

    /**
     * catches all load events after the fact, and tests the current users
     * permissions to read that object. We have to catch the load after the fact
     * because the permissions information is stored in the db.
     */
    public void onPostLoad(PostLoadEvent event) {
        Object entity = event.getEntity();
        if (entity instanceof IObject) {
            IObject o = (IObject) entity;
            if (!aclVoter.allowLoad(event.getSession(), o.getClass(), o.getDetails(), o.getId())) {
                aclVoter.throwLoadViolation(o);
            }
        }
    }

    public boolean onPreInsert(PreInsertEvent event) {
        Object entity = event.getEntity();
        if (entity instanceof IObject) {
            IObject obj = (IObject) entity;
            if (!aclVoter.allowCreation(obj)) {
                aclVoter.throwCreationViolation(obj);
            }
        }
        return false;
    }

    public boolean onPreUpdate(PreUpdateEvent event) {
        Object entity = event.getEntity();
        Object[] state = event.getOldState();
        String[] names = event.getPersister().getPropertyNames();
        if (entity instanceof IObject) {
            IObject obj = (IObject) entity;

            if (!aclVoter.allowUpdate(obj,
                    HibernateUtils.getDetails(state, names))) {
                aclVoter.throwUpdateViolation(obj);
            }
        }
        return false;
    }

    public boolean onPreDelete(PreDeleteEvent event) {
        Object entity = event.getEntity();
        Object[] state = event.getDeletedState();
        String[] names = event.getPersister().getPropertyNames();
        if (entity instanceof IObject) {
            IObject obj = (IObject) entity;
            if (!aclVoter.allowDelete(obj, HibernateUtils.getDetails(state,
                    names))) {
                aclVoter.throwDeleteViolation(obj);
            }
        }
        return false;
    }

}
