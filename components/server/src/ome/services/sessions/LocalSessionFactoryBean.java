/*
 * Copyright (C) 2014 Glencoe Software, Inc.
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

package ome.services.sessions;

import java.util.Map;

import ome.security.basic.EventListeners;

import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.service.ServiceRegistry;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.jdbc.support.lob.LobHandler;

/**
 * A simple wrapper around
 * {@link org.springframework.orm.hibernate4.LocalSessionFactoryBean} to add back the
 * event listener, filter definition, and lob handler configuration propreties removed
 * from the hibernate3 version.
 */

class LocalSessionFactoryBean extends org.springframework.orm.hibernate4.LocalSessionFactoryBean {

    private EventListeners eventListeners;

    private FilterDefinition[] filterDefinitions;

    private LobHandler lobHandler;

    public void setEventListeners(EventListeners eventListeners) {
        this.eventListeners = eventListeners;
    }

    public void setFilterDefinitions(FilterDefinition... filterDefinitions) {
        this.filterDefinitions = filterDefinitions;
    }

    public void setLobHandler(LobHandler lobHandler) {
        this.lobHandler = lobHandler;
        // TODO: What do we do with this now?...
    }

    @Override
    protected SessionFactory buildSessionFactory(LocalSessionFactoryBuilder sfb) {
        if (this.filterDefinitions != null) {
            for (FilterDefinition definition : this.filterDefinitions) {
                sfb.addFilterDefinition(definition);
            }
        }

        final SessionFactoryImplementor sfi
            = (SessionFactoryImplementor) super.buildSessionFactory(sfb);
        final ServiceRegistry sr = sfi.getServiceRegistry();
        final EventListenerRegistry listenerRegistry
            = sr.getService(EventListenerRegistry.class);

        this.eventListeners.registerWith(listenerRegistry);

        return sfi;
    }

}
