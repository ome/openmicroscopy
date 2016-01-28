/*
 * ome.security.basic.UpdateEventListener
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

// Java imports

// Third-party imports
import ome.model.IObject;
import ome.model.internal.Details;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.event.PreUpdateEvent;
import org.hibernate.event.PreUpdateEventListener;

/**
 * responsible for setting the
 * {@link Details#setUpdateEvent(ome.model.meta.Event) updat event} on all
 * events shortly before being saved.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see BasicSecuritySystem
 * @since 3.0-M3
 */
public class UpdateEventListener implements PreUpdateEventListener {

    public final static String UPDATE_EVENT = "UpdateEvent";

    private static final long serialVersionUID = -7607753637653567889L;

    private static Logger log = LoggerFactory.getLogger(UpdateEventListener.class);

    private final CurrentDetails cd;

    /**
     * main constructor. controls access to individual db rows..
     */
    public UpdateEventListener(CurrentDetails cd) {
        this.cd = cd;
    }

    /**
     * updates the update event field of an {@link IObject} instance.
     * 
     */
    public boolean onPreUpdate(PreUpdateEvent event) {
        Object entity = event.getEntity();
        if (entity instanceof IObject && !cd.isDisabled(UPDATE_EVENT)) {
            int[] dirty = event.getPersister().findDirty(event.getState(),
                    event.getOldState(), event.getEntity(), event.getSession());
            if (dirty == null || dirty.length == 0) {
                // return true; // veto.
            }

            else {
                // otherwise change update event (last modification)
                IObject obj = (IObject) entity;
                obj.getDetails().setUpdateEvent(cd.getEvent());
            }
        }
        return false;
    }

}
