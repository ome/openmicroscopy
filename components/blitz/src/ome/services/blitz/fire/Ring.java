/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.fire;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ome.model.meta.Node;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.services.blitz.redirect.Redirector;
import ome.services.blitz.util.BlitzConfiguration;
import ome.services.sessions.SessionManager;
import ome.services.sessions.state.SessionCache;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;
import omero.internal.ClusterNodePrx;
import omero.internal.ClusterNodePrxHelper;
import omero.internal._ClusterNodeDisp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import Glacier2.CannotCreateSessionException;
import Glacier2.SessionPrx;
import Ice.Current;

/**
 * Distributed ring of {@link BlitzConfiguration} objects which manages lookups
 * of sessions and other resources from all the blitzes which take part in the
 * cluster. Membership in the {@link Ring} is based on a single token --
 * "omero.instance" -- retrieved from the current context, or if missing, a
 * calculated value which will prevent this instance from taking part in
 * clustering.
 * 
 * The {@link Ring} also listens for
 * 
 *@since Beta4
 */
public class Ring extends _ClusterNodeDisp implements Redirector.Context {

    private final static Log log = LogFactory.getLog(Ring.class);

    /**
     * UUID for this cluster node. Used to uniquely identify the session manager
     * in this blitz instance. Most likely used in common with internal server
     * components. <em>Must</em> specify a valid session id.
     */
    public final String uuid;

    public final Principal principal;

    private final Executor executor;

    private final Redirector redirector;

    private final SessionCache cache;

    private/* final */Ice.Communicator communicator;

    private/* final */Registry registry;

    /**
     * Standard blitz adapter which is used for the callback.
     */
    private/* final */Ice.ObjectAdapter adapter;

    /**
     * Direct proxy value to the {@link SessionManager} in this blitz instance.
     */
    private/* final */String directProxy;

    public Ring(String uuid, Executor executor, SessionCache cache,
            Redirector redirector) {
        this.uuid = uuid;
        this.executor = executor;
        this.cache = cache;
        this.redirector = redirector;
        this.principal = new Principal(uuid, "system", "Internal");
    }

    /**
     * Sets the {@link Registry} for this instance. This is currently done in
     * {@link BlitzConfiguration}
     */
    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    // Redirector.Context API
    // =========================================================================

    public String uuid() {
        return this.uuid;
    }

    public Principal principal() {
        return this.principal;
    }

    /**
     * Returns the proxy information for the local {@link SessionManager}.
     * 
     * @return
     */
    public String getDirectProxy() {
        return this.directProxy;
    }

    public Ice.Communicator getCommunicator() {
        return this.communicator;
    }

    // Configuration and cluster usage
    // =========================================================================

    /**
     * Typically called from within {@link BlitzConfiguration} after the
     * communicator and adapter have been properly setup.
     */
    public void init(Ice.ObjectAdapter adapter, String directProxy) {
        this.adapter = adapter;
        this.communicator = adapter.getCommunicator();
        this.directProxy = directProxy;

        // Before we add our self we check the validity of the cluster.
        Set<String> nodeUuids = checkClusterAndAddSelf();
        addManager(uuid, directProxy);
        nodeUuids.add(uuid);
        redirector.chooseNextRedirect(this, nodeUuids);
    }

    /**
     * Method called during initialization to get all the active uuids within
     * the cluster, and remove any dead nodes. After this, we add this instance
     * to the cluster. May return null if lookup fails.
     */
    protected Set<String> checkClusterAndAddSelf() {

        ClusterNodePrx[] nodes = registry.lookupClusterNodes();
        if (nodes == null) {
            log.error("Could not lookup nodes. Skipping initialization...");
            return null; // EARLY EXIT
        }

        // Contact each of the cluster. This instance has not been added, so
        // this will not cause a callback.
        Set<String> nodeUuids = new HashSet<String>();
        for (int i = 0; i < nodes.length; i++) {
            ClusterNodePrx prx = nodes[i];
            if (prx == null) {
                log.warn("Null proxy found");
                continue;
            } else {
                try {
                    nodeUuids.add(nodes[i].getNodeUuid());
                } catch (Exception e) {
                    log.warn("Error getting uuid from node " + nodes[i]
                            + " -- removing.");
                    registry.removeObjectSafely(prx.ice_getIdentity());
                }
            }
        }
        log.info("Got " + nodeUuids.size() + " cluster uuids : " + nodeUuids);

        // Now any stale nodes (ones not found in the registry) are forcibly
        // removed, since it is assumed they didn't shut down cleanly.
        assertNodes(nodeUuids);

        try {
            // Now our checking is done, add ourselves.
            Ice.Identity clusterNode = this.communicator
                    .stringToIdentity("ClusterNode/" + uuid);
            this.adapter.add(this, clusterNode);
            registry.addObject(this.adapter.createDirectProxy(clusterNode));
        } catch (Exception e) {
            throw new RuntimeException("Cannot register self as node: ", e);
        }

        return nodeUuids;
    }

    public void destroy() {
        try {
            Ice.Identity id = this.communicator.stringToIdentity("ClusterNode/"
                    + uuid);
            registry.removeObjectSafely(id);
            redirector.handleRingShutdown(this, this.uuid);
            int count = closeSessionsForManager(uuid);
            log.info("Removed " + count + " entries for " + uuid);
            log.info("Disconnected from OMERO.cluster");
        } catch (Exception e) {
            log.error("Error stopping ring " + this, e);
        } finally {
            ClusterNodePrx[] nodes = null;
            try {
                // TODO this would be better served with a storm message!
                nodes = registry.lookupClusterNodes();
                if (nodes != null) {
                    for (ClusterNodePrx clusterNodePrx : nodes) {
                        try {
                            clusterNodePrx = ClusterNodePrxHelper
                                    .uncheckedCast(clusterNodePrx.ice_oneway());
                            clusterNodePrx.down(this.uuid);
                        } catch (Exception e) {
                            String msg = "Error signaling down to "
                                    + clusterNodePrx;
                            log.warn(msg, e);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error signaling down to: "
                        + Arrays.deepToString(nodes), e);
            }
        }
    }

    // Cluster Node API
    // =========================================================================

    public String getNodeUuid(Current __current) {
        return this.uuid;
    }

    /**
     * Called when any node goes down. First we try to remove any redirect for
     * that instance. Then we try to install ourselves.
     */
    public void down(String downUuid, Current __current) {
        redirector.handleRingShutdown(this, downUuid);
    }

    // Local usage
    // =========================================================================

    /**
     * Currently only returns false since if the regular password check
     * performed by {@link ome.services.sessions.SessionManager} cannot find the
     * session, then the cluster has no extra information.
     */
    public boolean checkPassword(String userId) {
        return false;
    }

    /**
     * Delegates to the {@link #redirector} strategy configured for this
     * instance.
     */
    public SessionPrx getProxyOrNull(String userId,
            Glacier2.SessionControlPrx control, Ice.Current current)
            throws CannotCreateSessionException {
        return redirector.getProxyOrNull(this, userId, control, current);
    }

    public Set<String> knownManagers() {
        return getManagerList(true);
    }

    public void assertNodes(Set<String> nodeUuids) {
        Set<String> managers = knownManagers();

        for (String manager : managers) {
            if (!nodeUuids.contains(manager)) {
                // Also verify this is not ourself, since
                // possibly we haven't finished registration
                // yet
                if (!uuid.equals(manager)) {
                    // And also don't try to purge the original manager.
                    if (!"000000000000000000000000000000000000".equals(manager)) {
                        purgeNode(manager);
                    }
                }
            }
        }
    }

    protected void purgeNode(String manager) {
        log.info("Purging node: " + manager);
        try {
            Ice.Identity id = this.communicator.stringToIdentity("ClusterNode/"
                    + manager);
            registry.removeObjectSafely(id);
            int count = closeSessionsForManager(manager);
            log.info("Removed " + count + " entries with value " + manager);
            setManagerDown(manager);
            log.info("Removed manager: " + manager);
        } catch (Exception e) {
            log.error("Failed to purge node " + manager, e);
        }
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

    @SuppressWarnings("unchecked")
    private int closeSessionsForManager(String managerUuid) {
        final String query = "select session from Session session where "
            + "session.node.uuid = :uuid and session.closed is null";
        final Parameters p = new Parameters().addString("uuid", managerUuid);

        // First look up the sessions in on transaction
        final List<ome.model.meta.Session> sessions = (List<ome.model.meta.Session>) executor
                .execute(principal, new Executor.SimpleWork(this,
                        "findAllSessions") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        List<ome.model.meta.Session> sessions = sf
                                .getQueryService().findAllByQuery(query, p);
                        return sessions;
                    }
                });

        // Then call removeSession which will start it's own transaction
        for (ome.model.meta.Session s : sessions) {
            // TODO this could possibly be better done by raising a
            // new message which is received by SessionManagerImpl
            // and then passes it to SessionCache.
            try {
                cache.removeSession(s.getUuid());
            } catch (Exception e) {
                log
                        .error("Error calling cache.removeSession "
                                + s.getUuid(), e);
            }

        }
        return sessions.size();
    }

    private void setManagerDown(final String managerUuid) {
        executor.execute(principal, new Executor.SimpleWork(this,
                "setManagerDown") {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                Node node = sf.getQueryService().findByQuery(
                        "select n from Node n where uuid = :uuid",
                        new Parameters().addString("uuid", managerUuid)
                                .setFilter(new Filter().page(0, 1)));
                node.setDown(new Timestamp(System.currentTimeMillis()));
                return sf.getUpdateService().saveAndReturnObject(node);
            }
        });
    }

    private Node addManager(String managerUuid, String proxyString) {
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
