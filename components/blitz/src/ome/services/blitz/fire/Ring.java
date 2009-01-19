/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.fire;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ome.services.blitz.util.BlitzConfiguration;
import ome.services.messages.CreateSessionMessage;
import ome.services.messages.DestroySessionMessage;
import ome.services.sessions.SessionManager;
import omero.internal.ClusterNodePrx;
import omero.internal.ClusterNodePrxHelper;
import omero.internal._ClusterNodeDisp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.blocks.ReplicatedHashMap;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import Glacier2.CannotCreateSessionException;
import Glacier2.SessionManagerPrx;
import Glacier2.SessionManagerPrxHelper;
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
public class Ring extends _ClusterNodeDisp implements ApplicationListener {

    private final static Log log = LogFactory.getLog(Ring.class);

    private final static String CONFIG = "config-";

    private final static String MANAGERS = "manager-";

    private final static String SESSIONS = "session-";

    /**
     * UUID for this cluster node. Used to uniquely identify the session manager
     * in this blitz instance.
     */
    public final String uuid = UUID.randomUUID().toString();

    private final SimpleJdbcTemplate jdbc;

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

    public Ring(SimpleJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        try {
            jdbc.update("create table session_ring "
                    + "(key varchar unique, value varchar)");
            log.info("Created OMERO.cluster table");
        } catch (BadSqlGrammarException bsge) {
            // Here we assume that this means that the table
            // already exists.
            log.info("session_ring already exists");
        }
    }

    /**
     * Sets the {@link Registry} for this instance. This is currently done in
     * {@link BlitzConfiguration}
     */
    public void setRegistry(Registry registry) {
        this.registry = registry;
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
        checkClusterAndAddSelf();
        put(MANAGERS + uuid, directProxy);
        putIfAbsent(CONFIG + "redirect", uuid);
        log.info("Current redirect: " + getRedirect());
    }

    /**
     * Method called during initialization to get all the active uuids within
     * the cluster, and remove any dead nodes. After this, we add this instance
     * to the cluster.
     */
    protected void checkClusterAndAddSelf() {

        ClusterNodePrx[] nodes = registry.lookupClusterNodes();

        // Contact each of the cluster. This instance has not been added, so
        // this will not cause a callback.
        Set<String> nodeUuids = new HashSet<String>();
        for (int i = 0; i < nodes.length; i++) {
            ClusterNodePrx prx = nodes[i];
            if (prx == null) {
                return;
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
    }

    public void destroy() {
        try {
            Ice.Identity id = this.communicator.stringToIdentity("ClusterNode/"
                    + uuid);
            registry.removeObjectSafely(id);
            remove(MANAGERS + uuid);
            int count = jdbc.update("delete from session_ring where value = ?",
                    uuid);
            log.info("Removed " + count + " entries for " + uuid);
            log.info("Disconnected from OMERO.cluster");
        } catch (Exception e) {
            log.error("Error stopping ring " + this, e);
        } finally {
            ClusterNodePrx[] nodes = null;
            try {
                // TODO this would be better served with a storm message!
                nodes = registry.lookupClusterNodes();
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
        removeIfEquals(CONFIG + "redirect", downUuid);
        if (putIfAbsent(CONFIG + "redirect", this.uuid)) {
            log.info("Installed self as new redirect: " + uuid);
        }
    }

    // Local usage
    // =========================================================================

    public boolean checkPassword(String userId) {
        boolean rv = get(SESSIONS + userId) != null;
        log.info(String.format("Checking password: %s [%s]", userId, rv));
        return rv;
    }

    /**
     * Returns the current redirect, to which all calls to
     * {@link #getProxyOrNull(String, Glacier2.SessionControlPrx, Ice.Current)}
     * will be pointed. May be null, but is typically set to a non-null value
     * when the first {@link Ring} joins the cluster.
     */
    public String getRedirect() {
        String redirect = get(CONFIG + "redirect");
        return redirect;
    }

    /**
     * Set the new redirect value and return the previous value, which might be
     * null. If the uuidOrProxy is null or empty, then the existing redirect
     * will be {@link ReplicatedHashMap#remove(Object)} removed. Otherwise the
     * value is set. In either case, the previous value is returned.
     */
    public void putRedirect(String uuidOrProxy) {
        if (uuidOrProxy == null || uuidOrProxy.length() == 0) {
            remove(CONFIG + "redirect");
        } else {
            put(CONFIG + "redirect", uuidOrProxy);
        }
    }

    public SessionPrx getProxyOrNull(String userId,
            Glacier2.SessionControlPrx control, Ice.Current current)
            throws CannotCreateSessionException {

        // If by the end of this method this string is non-null, then
        // SM.create() will be called on it.
        String proxyString = null;

        // If there is a redirect, then we honor it as long as it doesn't
        // point back to us, in which case we bail.
        String redirect = getRedirect();
        if (redirect != null) {
            log.info("Found redirect: " + redirect);
            if (redirect.equals(uuid)) {
                log.info("Redirect points to this instance; setting null");
                proxyString = null;
            } else {
                if (!containsKey(MANAGERS + redirect)) {
                    log.warn("No proxy found for manager: " + redirect);
                } else {
                    proxyString = get(MANAGERS + redirect);
                    log.info("Resolved redirect to: " + proxyString);
                }
            }
        }

        // Otherwise, if this is not a recursive invocation
        else if (!current.ctx.containsKey("omero.routed_from")) {

            // Check if the session is in ring
            proxyString = get(SESSIONS + userId);
            if (proxyString != null && !proxyString.equals(directProxy)) {
                log.info(String.format("Returning remote session on %s",
                        proxyString));
            }

            // or needs to be load balanced
            else {
                double IMPOSSIBLE = 314159.0;
                if (Math.random() > IMPOSSIBLE) {
                    Set<String> values = filter(MANAGERS);
                    if (values != null) {
                        values.remove(uuid);
                        int size = values.size();
                        if (size != 0) {
                            double rnd = Math.floor(size * Math.random());
                            int idx = (int) Math.round(rnd);
                            List v = new ArrayList(values);
                            proxyString = (String) v.get(idx);
                            proxyString = get(MANAGERS + proxyString);
                            log.info(String.format("Load balancing to %s",
                                    proxyString));
                        }
                    }
                }
            }

        }

        // If we've found a proxy string, use that.
        if (proxyString != null) {
            current.ctx.put("omero.routed_from", directProxy);
            Ice.ObjectPrx remote = communicator.stringToProxy(proxyString);
            SessionManagerPrx sessionManagerPrx = SessionManagerPrxHelper
                    .checkedCast(remote);
            try {
                return sessionManagerPrx.create(userId, control, current.ctx);
            } catch (Exception e) {
                log.error("Error while routing to " + remote, e);
                throw new CannotCreateSessionException(
                        "Error while routing to remote blitz");
            }
        }

        // Otherwise, return null.
        return null;
    }

    public Set<String> knownManagers() {
        Set<String> managers = filter(MANAGERS);
        Set<String> rv = new HashSet<String>();
        for (String manager : managers) {
            manager = manager.replaceFirst(MANAGERS, "");
            rv.add(manager);
        }
        return rv;
    }

    public void assertNodes(Set<String> nodeUuids) {
        Set<String> managers = knownManagers();
        for (String manager : managers) {
            if (!nodeUuids.contains(manager)) {
                // Also verify this is not ourself, since
                // possibly we haven't finished registration
                // yet
                if (!uuid.equals(manager)) {
                    purgeNode(manager);
                }
            }
        }
        // Now removing any stale sessions
        List<String> params = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        sb.append("delete from session_ring ");
        sb.append("where key like 'session-%' ");
        sb.append("and (");
        sb.append(" value != ?");
        params.add(0, uuid);
        for (String nodeUuid : nodeUuids) {
            sb.append(" and ");
            sb.append("value != ?");
            params.add(nodeUuid);
        }
        sb.append(")");
        int count = jdbc.update(sb.toString(), (Object[]) params
                .toArray(new Object[params.size()]));
        if (count != 0) {
            log.info("Removed " + count + " stale sessions");
        }
    }

    protected void purgeNode(String manager) {
        log.info("Purging node: " + manager);
        try {
            Ice.Identity id = this.communicator.stringToIdentity("ClusterNode/"
                    + manager);
            registry.removeObjectSafely(id);
            int count = jdbc.update("delete from session_ring where value = ?",
                    manager);
            log.info("Removed " + count + " entries with value " + manager);
            count = jdbc.update("delete from session_ring where key = ?",
                    MANAGERS + manager);
            log.info("Removed " + MANAGERS + manager);
        } catch (Exception e) {
            log.error("Failed to purge node " + manager, e);
        }
        putIfAbsent(CONFIG + "redirect", uuid);
    }

    // Events
    // =========================================================================

    public void onApplicationEvent(ApplicationEvent arg0) {
        if (arg0 instanceof CreateSessionMessage) {
            String session = ((CreateSessionMessage) arg0).getSessionId();
            log.info("Adding session " + session + " to manager " + uuid);
            put(SESSIONS + session, uuid); // Use our uuid rather than proxy
        } else if (arg0 instanceof DestroySessionMessage) {
            String session = ((DestroySessionMessage) arg0).getSessionId();
            remove(SESSIONS + session);
            log.info("Removing session " + session + " from manager " + uuid);
        } else if (arg0 instanceof ContextClosedEvent) {
            // This happens 3 times for each nested context. Perhaps we
            // should print and destroy?
        }
    }

    // Map interface
    // =========================================================================
    // If the implementation of this class needs to be changed, these should be
    // the only methods which need to be replaced. i.e. subclass implementors
    // may want to start here.

    public boolean containsKey(String key) {
        int count = jdbc.queryForInt("select count(key) "
                + "from session_ring " + "where key = ?", key);
        return count > 0;
    }

    public String get(String key) {
        try {
            String value = (String) jdbc
                    .queryForObject("select value " + "from session_ring "
                            + "where key = ?", String.class, key);
            return value;
        } catch (EmptyResultDataAccessException erdae) {
            return null;
        }
    }

    public void put(String key, String value) {
        boolean wasPut = putIfAbsent(key, value);
        if (!wasPut) {
            int count = jdbc.update(
                    "update session_ring set value = ? where key = ?", value,
                    key);
            if (count == 0) {
                log.info("Key not found for update: " + key);
            } else {
                log.info(String.format("Updated key %s to %s", key, value));
            }
        }
    }

    public boolean putIfAbsent(String key, String value) {
        try {
            int count = jdbc.update(
                    "insert into session_ring (key, value) values (?, ?)", key,
                    value);
            if (count > 0) {
                log.info(String.format("Put new key: %s with value: %s ", key,
                        value));
                return true;
            }
        } catch (DataIntegrityViolationException dive) {
            // The key already exists in the table. This is therefore
            // a no-op
        }
        return false;
    }

    /**
     * Removes a key from the session_ring table if present.
     * 
     * @param key
     */
    public boolean remove(String key) {
        int count = jdbc.update("delete from session_ring where key = ?", key);
        if (count == 0) {
            log.info("Key not found to remove: " + key);
            return false;
        } else {
            log.info("Removed key: " + key);
            return true;
        }
    }

    /**
     * Used to remove a key/value pair iff the value equals the value give here.
     */
    public boolean removeIfEquals(String key, String value) {
        int count = jdbc.update(
                "delete from session_ring where key = ? and value = ?", key,
                value);
        if (count == 0) {
            log.info("Key and value do not match: " + key + "=" + value);
            return false;
        } else {
            log.info("Removed key: " + key + " since value matched: " + value);
            return true;
        }
    }

    public List<String> keySet() {
        return jdbc.query("select key from session_ring",
                new ParameterizedRowMapper<String>() {
                    public String mapRow(ResultSet arg0, int arg1)
                            throws SQLException {
                        return arg0.getString(1);
                    }
                });
    }

    // Helpers
    // =========================================================================

    private Set<String> filter(String prefix) {
        Set<String> values = new HashSet<String>(keySet());
        Set<String> remove = new HashSet<String>();
        for (String value : values) {
            if (!value.startsWith(prefix)) {
                remove.add(value);
            }
        }
        values.removeAll(remove);
        return values;
    }

}