/*
 * Copyright (C) 2017 Glencoe Software, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.security.basic;

import ome.api.IUpdate;
import ome.model.meta.Event;
import ome.security.EventProvider;
import ome.system.ServiceFactory;

/**
 * Provider for {@link Event} objects which is responsible for persisting and
 * populating such entities using Hibernate in accordance with the currently
 * available {@link IUpdate} implementation.
 * 
 * @author Chris Allan <callan@glencoesoftware.com>
 * @see SecuritySystem
 * @since 5.3.0
 */
public class EventProviderInDb
    implements EventProvider {

    private ServiceFactory sf;

    /**
     * Main public constructor for this {@link EventProvider} implementation.
     * @param sf the service factory
     */
    public EventProviderInDb(ServiceFactory sf) {
        this.sf = sf;
    }

    /**
     * Persists a given {@link Event}.
     * @param event the event to persist
     * @return updated event
     */
    public Event updateEvent(Event event) {
        IUpdate update = sf.getUpdateService();
        return update.saveAndReturnObject(event);
    }

}
