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
 * Published when an event log should be saved at the end of a transaction.
 */
public class EventLogMessage extends InternalMessage {

    private static final long serialVersionUID = 7132548299119420025L;

    public final String action;

    public final Class<IObject> entityType;

    public final List<Long> entityIds;

    public EventLogMessage(Object source, String action, Class<IObject> entityType,
            List<Long> entityIds) {
        super(source);
        this.action = action;
        this.entityType = entityType;
        this.entityIds = entityIds;
    }

}