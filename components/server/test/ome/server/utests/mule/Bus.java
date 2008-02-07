/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.mule;

import java.util.ArrayList;
import java.util.List;

import ome.model.meta.Event;
import ome.parameters.Parameters;
import ome.parameters.QueryParameter;

import org.mule.config.builders.QuickConfigurationBuilder;
import org.mule.extras.client.MuleClient;
import org.mule.extras.spring.events.MuleApplicationEvent;
import org.mule.extras.spring.events.MuleEventMulticaster;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.routing.outbound.MulticastingRouter;
import org.mule.umo.UMODescriptor;
import org.mule.umo.UMOEventContext;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.Callable;
import org.mule.umo.manager.UMOManager;
import org.mule.umo.routing.UMORouter;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Need a way to remove misbehaving listeners. (a count of failures?)
 *
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
class Bus implements ApplicationListener, ApplicationEventMulticaster {

    QuickConfigurationBuilder builder;

    UMOManager manager;

    MuleClient client;

    Bus() {
        try {
            builder = new QuickConfigurationBuilder(); // don't need to close
                                                        // old
            manager = builder.createStartedManager(false, "");
            client = new MuleClient("", "");
        } catch (Exception e) {
            throw new BusException(e);
        }
    }

    List<ApplicationListener> sync = Collections
            .synchronizedList(new ArrayList<ApplicationListener>());

    public void registerMulticast(String id) {
        try {
            UMOEndpoint endpoint = new MuleEndpoint(id, true);
            MulticastingRouter router = new MulticastingRouter();
            router.addEndpoint(endpoint);
            UMODescriptor descriptor = new org.mule.impl.MuleDescriptor(id);
            // FIXME descriptor.setOutboundRouter(router);
            builder.registerComponent(descriptor);
        } catch (Exception e) {
            throw new BusException(e);
        }
    }

    public void registerAsyncListener(Callable callable, String id) {
        try {
            UMOEndpoint inboundEndpoint = new MuleEndpoint(id, true);
            UMODescriptor descriptor = new org.mule.impl.MuleDescriptor(id);
            descriptor.setImplementation(callable);
            descriptor.setInboundEndpoint(inboundEndpoint);
            builder.registerComponent(descriptor);
        } catch (Exception e) {
            throw new BusException(e);
        }
    }

    public void unregisterAsyncListener(String id) {
        try {
            builder.unregisterComponent(id);
        } catch (Exception ex) {
            throw new BusException(ex);
        }
    }

    // ~ ApplicationListener
    // =========================================================================

    public void onApplicationEvent(ApplicationEvent event) {
        multicastEvent(event);
    }

    // ~ ApplicationEventMulticaster
    // =========================================================================

    public void addApplicationListener(ApplicationListener listener) {
        sync.add(listener);
    }

    public void removeApplicationListener(ApplicationListener listener) {
        sync.remove(listener);
    }

    public void removeAllListeners() {
        sync.clear();
    }

    public void multicastEvent(ApplicationEvent event) {
        if (event instanceof M) {
            M m = (M) event;
            // synchronous
            if (m.getEndpoint() == null) {
                for (ApplicationListener listener : sync) {
                    listener.onApplicationEvent(event);
                }
            }
            // async
            else {
                try {
                    client.dispatch(m.getEndpoint(), m, null);
                } catch (Exception ex) {
                    throw new BusException(ex);
                }
            }
        }

    }

}

class BusException extends RuntimeException {
    BusException(Exception ex) {
        super(ex);
    }
}

class M extends MuleApplicationEvent {

    Parameters p;

    M(Parameters source) {
        super(source, null);
        assert source != null;
        p = source;
    }

    M(Parameters source, String endpoint) {
        super(source, endpoint);
        assert source != null;
        p = source;
    }

    public String getCategory() {
        return (String) get("ome.bus.category");
    }

    public String getConversation() {
        return (String) get("ome.bus.conversation");
    }

    public String getSession() {
        return (String) get("ome.bus.session");
    }

    public Event getEvent() {
        return (Event) get("ome.bus.event");
    }

    public Parameters getParameters() {
        return p;
    }

    private Object get(String id) {
        QueryParameter qp = p.get(id);
        if (qp == null)
            return null;
        return qp.value;
    }
}

class Adapter implements Callable {
    int calls = 0;

    public Object onCall(UMOEventContext arg0) throws Exception {
        calls++;
        return null;
    }
}