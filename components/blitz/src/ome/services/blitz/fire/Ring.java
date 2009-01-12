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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ome.services.blitz.util.BlitzConfiguration;
import ome.services.messages.CreateSessionMessage;
import ome.services.messages.DestroySessionMessage;
import ome.services.sessions.SessionManager;
import omero.internal.ClusterPrx;
import omero.internal.ClusterPrxHelper;
import omero.internal.DiscoverCallbackPrx;
import omero.internal._ClusterDisp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.blocks.ReplicatedHashMap;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
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
public class Ring extends _ClusterDisp implements ApplicationListener,
        ApplicationEventPublisherAware {

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

    private/* final */ApplicationEventPublisher publisher;

    private/* final */Ice.Communicator communicator;

    /**
     * Multicast-based adapter solely for cluster communication.
     */
    private/* final */Ice.ObjectAdapter clusterAdapter;

    /**
     * Standard blitz adapter which is used for the callback.
     */
    private/* final */Ice.ObjectAdapter adapter;

    /**
     * {@link Ice.ObjectPrx} for this {@link Ring} instance as added to the
     * {@link #adapter} in the {@link #init(Ice.ObjectAdapter, String)} method.
     */
    private/* final */Ice.ObjectPrx ownProxy;

    /**
     * Multicast (datagram) proxy to all other cluster nodes.
     */
    private/* final */ClusterPrx cluster;

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
     * Passed to the {@link Discovery} instance for signalling completion from
     * its background {@link Thread}
     */
    public void setApplicationEventPublisher(ApplicationEventPublisher arg0) {
        this.publisher = arg0;
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

        // The cluster we belong to.
        Ice.ObjectPrx prx = this.communicator.propertyToProxy("ClusterProxy");
        if (prx == null) {
            throw new RuntimeException("Could not obtain ClusterProxy. "
                    + "Is multicast property properly set?");
        }
        prx = prx.ice_datagram();
        if (prx == null) {
            throw new RuntimeException("Could no get datagram proxy. "
                    + "Please check your multicast configuration.");
        }
        cluster = ClusterPrxHelper.uncheckedCast(prx);

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

        // Now create the call back and start it
        // The instance registers and removes itself.
        Discovery discovery = new Discovery(adapter, this, this.publisher);
        try {
            cluster.discover(discovery.getProxy());
            new Thread(discovery).start();
        } catch (Ice.NoEndpointException nee) {
            log.warn("No cluster endpoints found", nee);
        }

        // Now our checking is done, add ourselves.
        this.clusterAdapter = this.communicator.createObjectAdapter("Cluster");
        ownProxy = this.clusterAdapter.add(this, this.communicator
                .stringToIdentity("Cluster"));
        this.clusterAdapter.activate();
    }

    /**
     * Returns the uuid for this manager instance. Other instances will use this
     * value to keep the session_ring table in sync.
     */
    public void discover(DiscoverCallbackPrx cb, Current __current) {
        try {
            cb.clusterNodeUuid(uuid);
            log.info("Sent cluster node uuid: " + uuid);
        } catch (Exception e) {
            log.warn("Exception while sending cluster node uuid: " + uuid, e);
        }
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

    public void destroy() {
        try {
            if (this.clusterAdapter != null) {
                this.clusterAdapter.deactivate();
            }
            remove(MANAGERS + uuid);
            int count = jdbc.update("delete from session_ring where value = ?", uuid);
            log.info("Removed "+count+" entries for "+uuid);
            log.info("Disconnected from OMERO.cluster");
        } catch (Exception e) {
            log.error("Error stopping ring " + this, e);
        } finally {
            if (cluster != null) {
                cluster.down(this.uuid);
            }
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
    }

    protected void purgeNode(String manager) {
        log.info("Purging node: " + manager);
        int count = jdbc.update("delete from session_ring where key like '"
                + SESSIONS + "' and value = ?", manager);
        log.info("Removed " + count + " sessions with value " + manager);
        count = jdbc.update("delete from session_ring where key = ?", MANAGERS
                + manager);
        log.info("Removed " + MANAGERS + manager);
        count = jdbc.update(
                "delete from session_ring where key = ? and value = ?", CONFIG
                        + "redirect", manager);
        if (count != 0) {
            log.info("Removed redirect to " + manager);
        }
        putRedirect(uuid);
    }

    // Events
    // =========================================================================

    public void onApplicationEvent(ApplicationEvent arg0) {
        if (arg0 instanceof CreateSessionMessage) {
            String session = ((CreateSessionMessage) arg0).getSessionId();
            put(SESSIONS + session, uuid); // Use our uuid rather than proxy
        } else if (arg0 instanceof DestroySessionMessage) {
            String session = ((DestroySessionMessage) arg0).getSessionId();
            remove(SESSIONS + session);
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