/*
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
import ome.services.blitz.redirect.NullRedirector;
import ome.services.blitz.redirect.Redirector;
import ome.services.blitz.util.BlitzConfiguration;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.util.SqlAction;
import omero.grid.ClusterNodePrx;
import omero.grid.ClusterNodePrxHelper;
import omero.grid._ClusterNodeDisp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final static Logger log = LoggerFactory.getLogger(Ring.class);

    /**
     * UUID for this cluster node. Used to uniquely identify the session manager
     * in this blitz instance. Most likely used in common with internal server
     * components. <em>Must</em> specify a valid session id.
     */
    public final String uuid;

    public final Principal principal;

    private final Executor executor;

    private final Redirector redirector;

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

    public Ring(String uuid, Executor executor) {
        this(uuid, executor, new NullRedirector());
    }

    public Ring(String uuid, Executor executor, Redirector redirector) {
        this.uuid = uuid;
        this.executor = executor;
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
     * @return See above.
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
        Set<String> nodeUuids = checkCluster();
        if (nodeUuids == null) {
            log.warn("No clusters found. Aborting ring initialization");
            return; // EARLY EXIT!
        }
        
        try {
            // Now our checking is done, add ourselves.
            Ice.Identity clusterNode = this.communicator
                    .stringToIdentity("ClusterNode/" + uuid);
            this.adapter.add(this, clusterNode); // OK ADAPTER USAGE
            addManager(uuid, directProxy);
            registry.addObject(this.adapter.createDirectProxy(clusterNode));
            nodeUuids.add(uuid);
            redirector.chooseNextRedirect(this, nodeUuids);
        } catch (Exception e) {
            throw new RuntimeException("Cannot register self as node: ", e);
        }
    }

    /**
     * Method called during initialization to get all the active uuids within
     * the cluster, and remove any dead nodes. May return null if lookup fails.
     */
    public Set<String> checkCluster() {
        
        log.info("Checking cluster");
        ClusterNodePrx[] nodes = registry.lookupClusterNodes();
        if (nodes == null) {
            log.error("Could not lookup nodes. Skipping initialization...");
            return null; // EARLY EXIT
        }

        // Contact each of the cluster. During init this, instance has not been
        // added, so this will not cause a callback. On clusterCheckTrigger,
        // however, it might.
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
    public boolean checkPassword(final String userId) {
        return (Boolean) executor.executeSql(new Executor.SimpleSqlWork(this,
                        "checkPassword") {
                    @Transactional(readOnly = true)
                    public Object doWork(SqlAction sql) {
                        return sql.activeSession(userId);
                    }
                });
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
            redirector.handleRingShutdown(this, manager);
            log.info("handleRingShutdown: " + manager);
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

    /**
     * Assumes that the given manager is no longer available and so will not
     * attempt to call cache.removeSession() since that requires the session to
     * be in memory. Instead directly modifies the database to set the session
     * to closed.
     * 
     * @param managerUuid
     * @return
     */
    @SuppressWarnings("unchecked")
    private int closeSessionsForManager(final String managerUuid) {

        // First look up the sessions in on transaction
        return (Integer) executor.execute(principal, new Executor.SimpleWork(
                this, "executeUpdate - set closed = now()") {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                return getSqlAction().closeNodeSessions(managerUuid);
            }
        });
    }

    private void setManagerDown(final String managerUuid) {
        executor.execute(principal, new Executor.SimpleWork(this,
                "setManagerDown") {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                return getSqlAction().closeNode(managerUuid);
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
