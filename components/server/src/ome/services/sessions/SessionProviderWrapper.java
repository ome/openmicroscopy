/*
 * Copyright (C) 2018 University of Dundee & Open Microscopy Environment.
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

package ome.services.sessions;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanCreationException;

import ome.model.meta.Session;
import ome.services.util.ReadOnlyStatus;
import ome.system.ServiceFactory;

/**
 * A session provider that offers a unified view of multiple underlying session providers.
 * @author m.t.b.carroll@dundee.ac.uk
 * @param <P> session providers that adjust according to read-only status
 */
public class SessionProviderWrapper<P extends SessionProvider & ReadOnlyStatus.IsAware> implements SessionProvider {

    private final List<P> read, write;

    /**
     * Construct a new Session provider.
     * @param readOnly the read-only status
     * @param providers the Session providers to wrap: the earlier providers are tried first and at least one provider must support
     * write operations according to {@link ome.services.util.ReadOnlyStatus.IsAware#isReadOnly(ReadOnlyStatus)}
     */
    public SessionProviderWrapper(ReadOnlyStatus readOnly, List<P> providers) {
        read = providers;
        write = new ArrayList<P>(read.size());
        for (final P provider : read) {
            if (!provider.isReadOnly(readOnly)) {
                write.add(provider);
            }
        }
        if (write.isEmpty()) {
            throw new BeanCreationException("must be given a read-write session provider");
        }
    }

    @Override
    public Session executeUpdate(ServiceFactory sf, Session session, String uuid, long userId, Long sudoerId) {
        /* working through all readers because we want a failure exception if the session exists as read-only */
        for (final P provider : read) {
            if (provider.findSessionIdByUuid(uuid, sf) != null) {
                return provider.executeUpdate(sf, session, uuid, userId, sudoerId);
            }
        }
        /* creating a new session */
        return write.get(0).executeUpdate(sf, session, uuid, userId, sudoerId);
    }

    @Override
    public void executeCloseSession(String uuid) {
        /* working through all readers because we want a failure exception if the session exists as read-only */
        for (final P provider : read) {
            if (provider.findSessionIdByUuid(uuid) != null) {
                provider.executeCloseSession(uuid);
            }
        }
    }

    @Override
    public Session executeInternalSession(String uuid, Session session) {
        return write.get(0).executeInternalSession(uuid, session);
    }

    @Override
    public long executeNextSessionId() {
        return write.get(0).executeNextSessionId();
    }

    @Override
    public Session findSessionById(long id, org.hibernate.Session hibernateSession) {
        for (final P provider : read) {
            final Session session = provider.findSessionById(id, hibernateSession);
            if (session != null) {
                return session;
            }
        }
        return null;
    }

    @Override
    public Session findSessionById(long id, ServiceFactory sf) {
        for (final P provider : read) {
            final Session session = provider.findSessionById(id, sf);
            if (session != null) {
                return session;
            }
        }
        return null;
    }

    @Override
    public Long findSessionIdByUuid(String uuid, ServiceFactory sf) {
        for (final P provider : read) {
            final Long sessionId = provider.findSessionIdByUuid(uuid, sf);
            if (sessionId != null) {
                return sessionId;
            }
        }
        return null;
    }

    @Override
    public Long findSessionIdByUuid(String uuid) {
        for (final P provider : read) {
            final Long sessionId = provider.findSessionIdByUuid(uuid);
            if (sessionId != null) {
                return sessionId;
            }
        }
        return null;
    }
}
