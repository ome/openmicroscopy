/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.eventlogs;


import ome.model.meta.EventLog;
import ome.util.messages.InternalMessage;

/**
 * Signifies that the processing of a {@link EventLog} has failed.
 * Previously, this was being handled by a try/catch
 * block within the processor (e.g. FullTextIndexer)
 * but in order to allow each {@link EventLogLoader} to handle the error
 * differently, this message was created.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 5.0.3
 */
public class EventLogFailure extends InternalMessage {

    private static final long serialVersionUID = -6829538576297712696L;

    public final EventLogLoader loader;

    public final EventLog log;

    public final Throwable throwable;

    public EventLogFailure(EventLogLoader loader, EventLog log, Throwable t) {
        super(loader);
        this.loader = loader;
        this.log = log;
        this.throwable = t;
    }

    public boolean wasSource(EventLogLoader loader) {
        return loader == this.loader;
    }
}
