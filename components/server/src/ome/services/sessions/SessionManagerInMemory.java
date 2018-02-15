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

package ome.services.sessions;

import java.sql.Timestamp;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;

import ome.model.meta.Experimenter;
import ome.model.meta.Node;
import ome.model.meta.Session;
import ome.system.ServiceFactory;

/**
 * In-memory implementation of {@link SessionManagerInDb}.
 *
 * @author Chris Allan <callan@glencoesoftware.com>
 * @since 5.3.0
 */
public class SessionManagerInMemory
    extends BaseSessionManager {

    private final static Logger log =
            LoggerFactory.getLogger(SessionManagerInMemory.class);

    private final AtomicLong currentSessionId = new AtomicLong(-1L);

    private final Map<String, Session> openSessions = new ConcurrentHashMap<>();
    private final Map<String, Session> closedSessions = CacheBuilder.newBuilder().maximumSize(512).<String, Session>build().asMap();

    @Override
    protected Session executeUpdate(ServiceFactory sf, Session session,
            long userId, Long sudoerId) {
        Node node = nodeProvider.getManagerByUuid(internal_uuid, sf);
        if (node == null) {
            node = new Node(0L, false); // Using default node.
        }
        if (session.getId() == null) {
            session.setId(executeNextSessionId());
        }
        session.setNode(node);
        session.setOwner(new Experimenter(userId, false));
        if (sudoerId == null) {
            session.setSudoer(null);
        } else {
            session.setSudoer(new Experimenter(sudoerId, false));
        }
        /* put before remove so that the session is never missing altogether */
        if (session.getClosed() == null) {
            openSessions.put(session.getUuid(), session);
            closedSessions.remove(session.getUuid());
        } else {
            closedSessions.put(session.getUuid(), session);
            openSessions.remove(session.getUuid());
        }
        log.debug("Registered Session:{} ({})", session.getId(), session.getUuid());
        return session;
    }

    @Override
    protected Long executeNextSessionId() {
        return currentSessionId.getAndDecrement();
    }

    @Override
    protected Session executeInternalSession() {
        Node node = nodeProvider.getManagerByUuid(internal_uuid, null);
        Session session = new Session(executeNextSessionId(), true);
        define(session, internal_uuid, "Session Manager internal",
                System.currentTimeMillis(), Long.MAX_VALUE, 0L,
                "Sessions", "Internal", null);
        log.debug("Created session: {}", session);
        log.debug("Setting node: {}", node);
        session.setNode(node);
        session.setOwner(new Experimenter(roles.getRootId(), false));
        return session;
    }

    @Override
    protected Session executeCloseSession(String uuid) {
        Session session = openSessions.get(uuid);
        if (session == null) {
            if (closedSessions.containsKey(uuid)) {
                log.debug("attempt to close session {} but is already closed", uuid);
            } else {
                log.warn("attempt to close session {} but is no longer cached", uuid);
            }
        } else {
            session.setClosed(new Timestamp(System.currentTimeMillis()));
            closedSessions.put(session.getUuid(), session);
            openSessions.remove(session.getUuid());
            log.debug("closed session {}", uuid);
        }
        return null;  // No-op
    }

    @Override
    protected Session findSessionById(Long id, ServiceFactory sf) {
        final SortedSet<Long> tries = new TreeSet<Long>();
        /* in Java 8 maybe could use Stream instead of Iterables */
        for (final Session session : Iterables.concat(openSessions.values(), closedSessions.values())) {
            if (session.getId().equals(id)) {
                return session;
            } else {
                tries.add(session.getId());
            }
        }
        log.warn("Requested session {}. Only found: {}", id, tries);
        return null;
    }
}
