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

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import ome.model.meta.Node;
import ome.model.meta.Session;
import ome.security.NodeProvider;
import ome.services.util.ReadOnlyStatus;
import ome.system.Principal;
import ome.system.ServiceFactory;

/**
 * Provider for {@link Node} objects which is responsible for persisting and
 * populating such entities.
 *
 * @author Chris Allan <callan@glencoesoftware.com>
 * @see ome.services.blitz.fire.Ring
 * @since 5.3.0
 */
public class NodeProviderInMemory implements NodeProvider, ReadOnlyStatus.IsAware {

    /**
     * UUID for this cluster node. Used to uniquely identify the session manager
     * in this blitz instance. Most likely used in common with internal server
     * components. <em>Must</em> specify a valid session id.
     */
    public final String uuid;

    private final Principal principal;

    private final Map<String, Node> currentNodes = new ConcurrentHashMap<>();

    private final AtomicLong currentNodeId = new AtomicLong(-1L);

    public NodeProviderInMemory(String uuid) {
        this.uuid = uuid;
        this.principal = new Principal(uuid, "system", "Internal");
    }

    /* (non-Javadoc)
     * @see ome.services.blitz.fire.NodeProviderI#principal()
     */
    public Principal principal() {
        return this.principal;
    }

    // Database interactions
    // =========================================================================

    /* (non-Javadoc)
     * @see ome.security.NodeProvider#getManagerIdByUuid(java.lang.String, ome.util.SqlAction)
     */
    public long getManagerIdByUuid(String managerUuid, ome.util.SqlAction sql) {
        final Node manager = getManagerByUuid(managerUuid, null);
        return manager == null ? 0 : manager.getId();
    };

    /* (non-Javadoc)
     * @see ome.security.NodeProvider#getManagerByUuid(java.lang.String, ome.system.ServiceFactory)
     */
    public Node getManagerByUuid(final String managerUuid, ServiceFactory sf) {
        return currentNodes.get(managerUuid);
    };

    /* (non-Javadoc)
     * @see ome.services.blitz.fire.NodeProviderI#getManagerList(boolean)
     */
    public Set<String> getManagerList(final boolean onlyActive) {
        Set<String> nodeIds = new HashSet<String>();
        for (final Node node : currentNodes.values()) {
            if (onlyActive && node.getDown() != null) {
                continue; // Remove inactive managers
            }
            nodeIds.add(node.getUuid());
        }
        return nodeIds;
    }

    /**
     * Assumes that the given manager is no longer available and will clean up
     * all in-memory sessions.
     */
    public int closeSessionsForManager(final String managerUuid) {
        // Implementation of the following SQL query in memory:
        //
        // update session set closed = now()
        //     where closed is null and node in
        //         (select id from Node where uuid = ?)
        final Node node = currentNodes.get(managerUuid);
        int modificationCount = 0;
        if (node != null) {
            Iterator<Session> i = node.iterateSessions();
            while (i.hasNext()) {
                Session session = i.next();
                if (session.getClosed() == null) {
                    session.setClosed(
                            new Timestamp(System.currentTimeMillis()));
                    modificationCount++;
                }
            }
        }
        return modificationCount;
    }

    /* (non-Javadoc)
     * @see ome.services.blitz.fire.NodeProviderI#setManagerDown(java.lang.String)
     */
    public void setManagerDown(final String managerUuid) {
        // Implement of the following SQL query in memory:
        //
        // update Node set down = now() where uuid = ?
        final Node node = currentNodes.get(managerUuid);
        if (node != null) {
            node.setDown(new Timestamp(System.currentTimeMillis()));
        }
    }

    /* (non-Javadoc)
     * @see ome.services.blitz.fire.NodeProviderI#addManager(java.lang.String, java.lang.String)
     */
    public Node addManager(String managerUuid, String proxyString) {
        final Node node = new Node();
        node.setId(currentNodeId.getAndDecrement());
        node.setConn(proxyString);
        node.setUuid(managerUuid);
        node.setUp(new Timestamp(System.currentTimeMillis()));
        currentNodes.put(managerUuid, node);
        return node;
    }

    @Override
    public boolean isReadOnly(ReadOnlyStatus readOnly) {
        return false;
    }
}
