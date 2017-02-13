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

package ome.services.blitz.fire;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import ome.model.meta.Node;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;

/**
 * Provider for {@link Node} objects which is responsible for persisting and
 * populating such entities.
 * 
 * @author Chris Allan <callan@glencoesoftware.com>
 * @see Ring
 * @since 5.3.0
 */
public class NodeProvider implements NodeProviderI {

    /**
     * UUID for this cluster node. Used to uniquely identify the session manager
     * in this blitz instance. Most likely used in common with internal server
     * components. <em>Must</em> specify a valid session id.
     */
    public final String uuid;

    private final Executor executor;

    private final Principal principal;

    public NodeProvider(String uuid, Executor executor) {
        this.uuid = uuid;
        this.executor = executor;
        this.principal = new Principal(uuid, "system", "Internal");
    }

    public Principal principal() {
        return this.principal;
    }

    // Database interactions
    // =========================================================================

    @SuppressWarnings("unchecked")
    public Set<String> getManagerList(final boolean onlyActive) {
        return (Set<String>) executor.execute(principal,
                new Executor.SimpleWork(this, "getManagerList") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        List<Node> nodes = sf.getQueryService().findAll(
                                Node.class, null);
                        Set<String> nodeIds = new HashSet<String>();
                        for (Node node : nodes) {
                            if (onlyActive && node.getDown() != null) {
                                continue; // Remove none active managers
                            }
                            nodeIds.add(node.getUuid());
                        }
                        return nodeIds;
                    }
                });
    }

    /**
     * Assumes that the given manager is no longer available and so will not
     * attempt to call cache.removeSession() since that requires the session to
     * be in memory. Instead directly modifies the database to set the session
     * to closed.
     * 
     * @param managerUuid
     * @return
     */
    public int closeSessionsForManager(final String managerUuid) {

        // First look up the sessions in on transaction
        return (Integer) executor.execute(principal, new Executor.SimpleWork(
                this, "executeUpdate - set closed = now()") {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                return getSqlAction().closeNodeSessions(managerUuid);
            }
        });
    }

    public void setManagerDown(final String managerUuid) {
        executor.execute(principal, new Executor.SimpleWork(this,
                "setManagerDown") {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                return getSqlAction().closeNode(managerUuid);
            }
        });
    }

    public Node addManager(String managerUuid, String proxyString) {
        final Node node = new Node();
        node.setConn(proxyString);
        node.setUuid(managerUuid);
        node.setUp(new Timestamp(System.currentTimeMillis()));
        return (Node) executor.execute(principal, new Executor.SimpleWork(this,
                "addManager") {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                return sf.getUpdateService().saveAndReturnObject(node);
            }
        });
    }

}
