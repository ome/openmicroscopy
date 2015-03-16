/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.messages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ome.model.meta.EventLog;
import ome.util.messages.InternalMessage;

import com.google.common.collect.Multimap;

/**
 * Published with the final collection of {@link EventLog} instances which
 * <em>will</em> be saved.
 */
public class EventLogsMessage extends InternalMessage {

    private static final long serialVersionUID = 7132548299119420025L;

    final Multimap<String, EventLog> logs;

    public EventLogsMessage(Object source, Multimap<String, EventLog> logs) {
        super(source);
        this.logs = logs;
    }

    public Collection<EventLog> matches(String klass, String action) {
        List<EventLog> rv = new ArrayList<EventLog>();
        for (EventLog el : logs.get(klass)) {
            if (el.getAction().equals(action)) {
                rv.add(el);
            }
        }
        return rv;
    }
}