/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.fire;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import ome.services.blitz.util.BlitzConfiguration;
import ome.services.messages.CreateSessionMessage;
import ome.services.messages.DestroySessionMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Address;
import org.jgroups.ChannelFactory;
import org.jgroups.JChannelFactory;
import org.jgroups.View;
import org.jgroups.blocks.ReplicatedHashMap;
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
public class Ring implements ReplicatedHashMap.Notification<String, String>,
        ApplicationListener {

    private final static Log log = LogFactory.getLog(Ring.class);

    private final static String CONFIG = "config-";

    private final static String MANAGERS = "manager-";

    private final static String SESSIONS = "session-";

    private final String uuid = UUID.randomUUID().toString();

    private/* final */BlitzConfiguration blitz;

    private/* final */String directProxy;

    private final ReplicatedHashMap<String, String> map;

    private final String groupname;

    public static String determineGroupName() {
        String defaultValue = "Runtime" + Runtime.getRuntime().hashCode();
        String environValue = System.getenv("OMERO_INSTANCE");
        if (environValue == null || environValue.length() == 0) {
            environValue = defaultValue;
        }
        return environValue;
    }

    public Ring(String props) {
        this(determineGroupName(), props);
    }

    public Ring(String groupname, String props) {
        this.groupname = groupname;
        try {
            ChannelFactory factory = new JChannelFactory();

            map = new ReplicatedHashMap<String, String>(groupname, factory,
                    props, 10000);
            map.setBlockingUpdates(true);
            map.addNotifier(this);
            map.start(1000);
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
        map.put(MANAGERS + uuid, directProxy);
    }

    private void checkInit() {
        if (this.blitz == null) {
            throw new IllegalStateException("Not initialized");
        }
    }

    public void destroy() {
        try {
            log.info("Shutting down ring in group " + groupname);
            map.remove(MANAGERS + uuid);
            map.stop();
        } catch (Exception e) {
            log.error("Error stopping ring " + this, e);
        }
    }

    // Our usage
    // =========================================================================

    public boolean checkPassword(String userId) {
        return map.get(SESSIONS + userId) != null;
    }

    public SessionPrx getProxyOrNull(String userId,
            Glacier2.SessionControlPrx control, Ice.Current current)
            throws CannotCreateSessionException {

        // If by the end of this method this string is non-null, then
        // SM.create() will be called on it.
        String proxyString = null;

        // If there is a redirect, then we honor it as long as it doesn't
        // point back to us, in which case we bail.
        String redirect = map.get(CONFIG + "redirect");
        if (redirect != null) {
            log.info("Found redirect: " + redirect);
            if (redirect.equals(uuid)) {
                log.info("Redirect points to this instance; setting null");
                proxyString = null;
            } else {
                if (!map.containsKey(MANAGERS + redirect)) {
                    log.warn("No proxy found for manager: " + redirect);
                } else {
                    proxyString = map.get(MANAGERS + redirect);
                    log.info("Resolved redirect to: " + proxyString);
                }
            }
        }

        // Otherwise, if this is not a recursive invocation
        else if (!current.ctx.containsKey("omero.routed_from")) {

            // Check if the session is in ring
            proxyString = map.get(SESSIONS + userId);
            if (proxyString != null && !proxyString.equals(directProxy)) {
                log.info(String.format("Returning remote session on %s",
                        proxyString));
            }

            // or needs to be load balanced
            else {
                double IMPOSSIBLE = 309340.0;
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
                            proxyString = map.get(MANAGERS + proxyString);
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
            Ice.ObjectPrx remote = blitz.getCommunicator().stringToProxy(
                    proxyString);
            SessionManagerPrx sessionManagerPrx = SessionManagerPrxHelper
                    .checkedCast(remote);
            return sessionManagerPrx.create(userId, control, current.ctx);
        }

        // Otherwise, return null.
        return null;
    }

    // Events
    // =========================================================================

    public void onApplicationEvent(ApplicationEvent arg0) {
        if (arg0 instanceof CreateSessionMessage) {
            String uuid = ((CreateSessionMessage) arg0).getSessionId();
            map.put(SESSIONS + uuid, directProxy);
        } else if (arg0 instanceof DestroySessionMessage) {
            String uuid = ((DestroySessionMessage) arg0).getSessionId();
            map.remove(SESSIONS + uuid);
        } else if (arg0 instanceof ContextClosedEvent) {
            // This happens 3 times for each nested context. Perhaps we
            // should print and destroy?
        }
    }

    // Notification interface
    // =========================================================================

    public void contentsCleared() {
        log.info("cleared");
    }

    public void contentsSet(Map<String, String> arg0) {
        log.info("set contents:" + arg0);
    }

    public void entryRemoved(String arg0) {
        log.info("remove:" + arg0);
    }

    public void entrySet(String arg0, String arg1) {
        log.info("set:" + arg0 + "=" + arg1);
    }

    public void viewChange(View arg0, Vector<Address> arg1, Vector<Address> arg2) {
        log.info("view change:" + arg0);
    }

    // Main
    // =========================================================================

    public static void main(String[] args) throws Exception {

        Ring ring = new Ring();
        try {

            if (args == null || args.length == 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("java Ring [sessions | config | redirect [value]");
                sb.append("\n");
                sb.append("  print fqn        -    print all values at fqn\n");
                sb.append("  redirect         -    print current redirect\n");
                sb
                        .append("  redirect [value] -    set current redirect. Empty removes\n");
                sb.append(" \n");
                System.out.println(sb.toString());
            }

            if (args[0].equals("print")) {
                if (args.length > 1) {
                    for (int i = 1; i < args.length; i++) {
                        ring.printTree(args[i]);
                    }
                } else {
                    ring.printAll();
                }
            } else if (args[0].equals("redirect")) {
                if (args.length > 1) {
                    String value = args[1];
                    ring.setRedirect(value);
                } else {
                    ring.printRedirect();
                }
            } else if (args[0].equals("raw")) {
                System.out.println(ring.map);
            }

        } finally {
            ring.destroy();
        }
    }

    public void printAll() {
        for (String fqn : Arrays.asList(SESSIONS, MANAGERS, CONFIG)) {
            printTree(fqn);
        }
    }

    public void printTree(String prefix) {
        System.out.println("===== " + prefix + " =====");
        Set<String> keys = filter(prefix);
        if (keys != null && keys.size() > 0) {
            for (String key : keys) {
                System.out.println(String.format("%s\t%s", key, map.get(key)));
            }
        } else {
            System.out.println("(empty)");
        }
    }

    public void setRedirect(String uuidOrProxy) {
        if (uuidOrProxy == null || uuidOrProxy.length() == 0) {
            map.remove(CONFIG + "redirect");
        } else {
            map.put(CONFIG + "redirect", uuidOrProxy);
        }
    }

    public void printRedirect() {
        Object uuidOrProxy = map.get(CONFIG + "redirect");
        if (uuidOrProxy != null && uuidOrProxy.toString().length() > 0) {
            System.out.println(uuidOrProxy.toString());
        }
    }

    // Helpers
    // =========================================================================

    private Set<String> filter(String prefix) {
        Set<String> values = new HashSet<String>(map.keySet());
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