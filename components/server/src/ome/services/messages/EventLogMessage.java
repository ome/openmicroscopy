/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.messages;

import java.util.List;

import ome.model.IObject;
import ome.util.messages.InternalMessage;

/**
 * Published only when an event log should be saved at the end of a transaction.
 * Most {@link EventLog} instances are created directly in the database and so
 * a listener cannot expect to know the full state of the system just from these.
 */
public class EventLogMessage extends InternalMessage {

    private static final long serialVersionUID = 7132548299119420025L;

    public final String action;

    public final Class<? extends IObject> entityType;

    public final List<Long> entityIds;

    public EventLogMessage(Object source, String action, Class<? extends IObject> entityType,
            List<Long> entityIds) {
        super(source);
        this.action = action;
        this.entityType = entityType;
        this.entityIds = entityIds;
    }

}