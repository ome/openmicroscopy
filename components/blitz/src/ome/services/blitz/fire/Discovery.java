/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.fire;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import omero.internal.DiscoverCallbackPrx;
import omero.internal.DiscoverCallbackPrxHelper;
import omero.internal._DiscoverCallbackDisp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import Ice.Current;

/**
 * Callback instance which is used by {@link Ring} instances to check the
 * current status of the cluster and if necessary take actions like removing
 * 
 * @since Beta4
 */
public class Discovery extends _DiscoverCallbackDisp implements Runnable {

    /**
     * Raised when the {@link Discovery#run()} method completes since this is
     * usually called in a background {@link Thread}.
     */
    public static class Finished extends ApplicationEvent {
        public Finished(Object source) {
            super(source);
        }
    }
    
    private final static Log log = LogFactory.getLog(Discovery.class);

    final Set<String> nodeUuids = Collections
            .synchronizedSet(new HashSet<String>());

    private final ApplicationEventPublisher pub;
    
    private final Ice.ObjectAdapter adapter;

    private final DiscoverCallbackPrx self;

    private final Ring ring;

    /**
     * Only true during the execution of {@link #run()}
     */
    private boolean active = false;

    public Discovery(Ice.ObjectAdapter adapter, Ring ring, ApplicationEventPublisher pub) {
        this.adapter = adapter;
        this.ring = ring;
        this.pub = pub;
        Ice.ObjectPrx prx = adapter.addWithUUID(this);
        prx = adapter.createDirectProxy(prx.ice_getIdentity());
        this.self = DiscoverCallbackPrxHelper.uncheckedCast(prx);
    }

    public DiscoverCallbackPrx getProxy() {
        return self;
    }

    public void clusterNodeUuid(String uuid, Current __current) {
        nodeUuids.add(uuid);
        log.info("Received cluster node uuid: " + uuid);
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Sleep long enough so that all cluster nodes can respond, then carry out
     * our tasks and remove ourselves from the adapter.
     */
    public void run() {
        try {
            log.info("Starting discovery...");
            active = true;
            Set<String> knownManagers = Collections.synchronizedSet(ring
                    .knownManagers());
            long start = System.currentTimeMillis();
            while (start + 10 * 1000L > System.currentTimeMillis()) {
                try {
                    Thread.sleep(500L);
                    // Once we've account for all managers in the database
                    // we can stop.
                    if (nodeUuids.containsAll(knownManagers)) {
                        log.info("All managers accounted for.");
                        break;
                    }
                } catch (InterruptedException e) {
                    // No matter. The while block will keep retrying.
                }
            }
            
            // Now we use all the values we found to call back
            // to ring to cleanup the database.
            removeAllMissingNodes();
            
            pub.publishEvent(new Finished(this));
        } finally {
            log.info("Stopping discovery...");
            active = false;
            try {
                this.adapter.remove(self.ice_getIdentity());
            } catch (Ice.ObjectAdapterDeactivatedException oade) {
                // This is fine. adapter is going down anyway.
            } catch (Exception e) {
                log.error("Failed to remove Discovery", e);
            }
        }
    }

    /**
     * Calls back to {@link Ring} to remove all nodes which we did not
     * find during discovery. Protects against all exceptions so that
     * an {@link ApplicationEvent} can properly be raised.
     */
    private void removeAllMissingNodes() {
        try {
            ring.assertNodes(nodeUuids);
        } catch (Exception e) {
            log.error("Exception during node assertion", e);
        }
    }

}