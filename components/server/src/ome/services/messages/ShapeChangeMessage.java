/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.messages;

import java.util.Iterator;
import java.util.List;

import ome.model.meta.EventLog;
import ome.util.messages.InternalMessage;

/**
 * Published on any modification to a Shape. Used to keep the geometry columns
 * on the shape tables in sync.
 */
public class ShapeChangeMessage extends InternalMessage implements Iterable<EventLog> {

    private static final long serialVersionUID = 8132548299119420025L;

    protected final List<EventLog> logs;
    
    public ShapeChangeMessage(Object source, List<EventLog> eventLogs) {
        super(source);
        this.logs = eventLogs;
    }

    public Iterator<EventLog> iterator() {
        return logs.iterator();
    }

}