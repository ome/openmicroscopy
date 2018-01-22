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

package ome.security;

import ome.model.meta.Event;

/**
 * Provider for {@link Event} objects which is responsible for persisting and
 * populating such entities.
 * 
 * @author Chris Allan <callan@glencoesoftware.com>
 * @see SecuritySystem
 * @since 5.3.0
 */
public interface EventProvider {

    /**
     * Persists a given {@link Event}.
     * @param event the event to persist
     * @return updated event
     */
    Event updateEvent(Event event);

}
