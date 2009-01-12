/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.fire;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ome.services.blitz.util.BlitzConfiguration;
import ome.services.messages.DestroySessionMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.View;
import org.jgroups.blocks.ReplicatedTree;
import org.jgroups.blocks.ReplicatedTree.ReplicatedTreeListener;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import Glacier2.CannotCreateSessionException;
import Glacier2.SessionManagerPrx;
import Glacier2.SessionManagerPrxHelper;
import Glacier2.SessionPrx;

/**
 * Distributed ring of {@link BlitzConfiguration} objects which manages lookups
 * of sessions and other resources from all the blitzes which take part in the
 * ring. Membership in the {@link Ring} is based on a single token --
 * "omero.instance" -- retrieved from the current context, or if missing, a
 * calculated value which will prevent this instance from taking part in
 * clustering.
 * 
 * The {@link Ring} also listens for
 * 
 *@since Beta4
 */
public class Ring implements ReplicatedTreeListener, ApplicationListener {

    private final static Log log = LogFactory.getLog(Ring.class);

    private final static String MANAGERS = "/managers";

    private final static String SESSIONS = "/sessions";

    private final String uuid = UUID.randomUUID().toString();

    private/* final */BlitzConfiguration blitz;

    private/* final */String directProxy;

    private final ReplicatedTree tree;

    private final String groupname;

    public Ring(String props) {
        String defaultValue = "Runtime" + Runtime.getRuntime().hashCode();
        String environValue = System.getenv("OMERO_INSTANCE");
        if (environValue == null || environValue.length() == 0) {
            environValue = defaultValue;
        }
        groupname = environValue;

        try {
            tree = new ReplicatedTree(groupname, props, 10000);
            tree.addReplicatedTreeListener(this);
            tree.start();
        } catch (Exception e) {
            throw new RuntimeException("Could not start ring: ", e);
        }
        log.info("Initialized ring in group " + groupname);

    }

    public Ring() {
        this("session_ring.xml");
    }

    public void init(BlitzConfiguration bc) {
        this.blitz = bc;
        directProxy = bc.getCommunicator().proxyToString(bc.getDirectProxy());
        tree.put(MANAGERS, uuid, directProxy);
    }

    private void checkInit() {
        if (this.blitz == null) {
            throw new IllegalStateException("Not initialized");
        }
    }

    public void destroy() {
        try {
            log.info("Shutting down ring in group " + groupname);
            tree.stop();
        } catch (Exception e) {
            log.error("Error stopping ring " + this, e);
        }
    }

    // Our usage
    // =========================================================================

    public void add(String uuid) {
        tree.put(SESSIONS, uuid, directProxy);
    }

    public boolean checkPassword(String userId) {
        return tree.get(SESSIONS, userId) != null;
    }

    public SessionPrx getProxyOrNull(String userId,
            Glacier2.SessionControlPrx control, Ice.Current current)
            throws CannotCreateSessionException {

        // If this is not a recursive invocation
        if (!current.ctx.containsKey("omero.routed_from")) {

            // Check if the session is in ring
            String proxyString = (String) tree.get(SESSIONS, userId);
            if (proxyString != null && !proxyString.equals(directProxy)) {
                log.info(String.format("Returning remote session %s",
                        proxyString));
            }

            // or needs to be load balanced
            else {
                if (Math.random() > 0.5) {
                    Set<String> values = tree.getKeys(MANAGERS);
                    if (values != null) {
                        values.remove(uuid);
                        int size = values.size();
                        if (size != 0) {
                            double rnd = Math.floor(size * Math.random());
                            int idx = (int) Math.round(rnd);
                            List v = new ArrayList(values);
                            proxyString = (String) v.get(idx);
                            proxyString = (String) tree.get(MANAGERS,
                                    proxyString);
                            log.info(String.format("Load balancing to %s",
                                    proxyString));
                        }
                    }
                }
            }

            // And if so, then return its return value
            if (proxyString != null) {
                current.ctx.put("omero.routed_from", directProxy);
                Ice.ObjectPrx remote = blitz.getCommunicator().stringToProxy(
                        proxyString);
                SessionManagerPrx sessionManagerPrx = SessionManagerPrxHelper
                        .checkedCast(remote);
                return sessionManagerPrx.create(userId, control, current.ctx);
            }
        }

        // Otherwise, return null.
        return null;
    }

    // Events
    // =========================================================================

    public void onApplicationEvent(ApplicationEvent arg0) {
        if (arg0 instanceof DestroySessionMessage) {
            tree.remove(((DestroySessionMessage) arg0).getSessionId());
        } else if (arg0 instanceof ContextClosedEvent) {
            log.info("Closing server with sessions:" + tree.getKeys(SESSIONS));
        }
    }

    // Notification interface
    // =========================================================================

    public void nodeAdded(String arg0) {
        log.info("Node added: " + arg0);
    }

    public void nodeModified(String arg0) {
        log.info("Node modified: " + arg0);
    }

    public void nodeRemoved(String arg0) {
        log.info("Node removed: " + arg0);
    }

    public void viewChange(View arg0) {
        // TODO Auto-generated method stub

    }

    // Main
    // =========================================================================

    public static void main(String[] args) throws Exception {
        Ring ring = new Ring();
        log.info(ring.tree);
    }

}