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
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import ome.model.meta.Node;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.security.NodeProvider;
import ome.services.util.Executor;
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
public class BasicNodeProvider implements NodeProvider {

    /**
     * UUID for this cluster node. Used to uniquely identify the session manager
     * in this blitz instance. Most likely used in common with internal server
     * components. <em>Must</em> specify a valid session id.
     */
    public final String uuid;

    private final Executor executor;

    private final Principal principal;

    public BasicNodeProvider(String uuid, Executor executor) {
        this.uuid = uuid;
        this.executor = executor;
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
        long nodeId = 0L;
        try {
            nodeId = sql.nodeId(managerUuid);
        } catch (EmptyResultDataAccessException erdae) {
            // Using default node
        }
        return nodeId;
    };

    /* (non-Javadoc)
     * @see ome.security.NodeProvider#getManagerByUuid(java.lang.String, ome.system.ServiceFactory)
     */
    public Node getManagerByUuid(final String managerUuid, ServiceFactory sf) {
        Parameters p = new Parameters();
        p.addString("uuid", managerUuid).setFilter(
                new Filter().page(0, 1));
        return (Node) sf.getQueryService().findByQuery(
                "select n from Node n where uuid = :uuid", p);
    };

    /* (non-Javadoc)
     * @see ome.services.blitz.fire.NodeProviderI#getManagerList(boolean)
     */
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

    /* (non-Javadoc)
     * @see ome.services.blitz.fire.NodeProviderI#setManagerDown(java.lang.String)
     */
    public void setManagerDown(final String managerUuid) {
        executor.execute(principal, new Executor.SimpleWork(this,
                "setManagerDown") {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                return getSqlAction().closeNode(managerUuid);
            }
        });
    }

    /* (non-Javadoc)
     * @see ome.services.blitz.fire.NodeProviderI#addManager(java.lang.String, java.lang.String)
     */
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
