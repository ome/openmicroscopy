/*
 * ome.security.basic.EventListenersFactoryBean
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ome.security.ACLEventListener;
import ome.security.ACLVoter;
import ome.security.basic.EventListeners;
import ome.tools.hibernate.EventMethodInterceptor;
import ome.tools.hibernate.ReloadingRefreshEventListener;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * configuring all the possible {@link EventListeners event listeners} within
 * XML can be cumbersome.
 */
public class EventListenersFactoryBean extends AbstractFactoryBean {

    private final CurrentDetails cd;

    private final TokenHolder th;

    private final ACLVoter voter;

    private final OmeroInterceptor interceptor;

    // ~ FactoryBean
    // =========================================================================

    public EventListenersFactoryBean(CurrentDetails cd, TokenHolder th,
            ACLVoter voter, OmeroInterceptor interceptor) {
        this.cd = cd;
        this.th = th;
        this.voter = voter;
        this.interceptor = interceptor;
    }

    /**
     * this {@link FactoryBean} produces a {@link Map} instance for use in
     * {@link LocalSessionFactoryBean#setEventListeners(EventListeners)}
     */
    @Override
    public Class getObjectType() {
        return EventListeners.class;
    }

    /**
     * being a singleton implies that this {@link FactoryBean} will only ever
     * create one instance.
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * First, adds all default listeners. These are then overwritten.
     */
    @Override
    protected Object createInstance() throws Exception {
        return new Impl();
    }

    protected boolean debugAll = false;

    /** for setter injection */
    public void setDebugAll(boolean debug) {
        this.debugAll = debug;
    }

    /**
     * This inner class will have access to all of the bean-configured components needed
     * to construct our listeners, but can defer registering them until we are in the
     * process of creating the session in the {@link LocalSessionFactoryBean}.
     */
    private class Impl implements EventListeners {

        private EventListenerRegistry registry;

        public void registerWith(EventListenerRegistry registry) {
            this.registry = registry;
            overrides();
            additions();
        }

        // ~ Configuration
        // =========================================================================

        protected void overrides() {
            override(EventType.MERGE, new MergeEventListener(cd, th));
            override(EventType.SAVE, new SaveEventListener(cd, th));
            override(new EventType[] { EventType.REPLICATE, EventType.UPDATE },
                    getDisablingProxy());
        }

        protected void additions() {
            // This must be prepended because it updates the alreadyRefreshed
            // cache before passing the event on the default listener.
            prepend(EventType.REFRESH, new ReloadingRefreshEventListener());

            for (final EventType type : EventType.values()) {
                EventMethodInterceptor emi = new EventMethodInterceptor(
                        new EventMethodInterceptor.DisableAction() {
                            @Override
                            protected boolean disabled(MethodInvocation mi) {
                                return cd.isDisabled(type.eventName());
                            }
                        });
                append(type, getProxy(emi));
            }

            if (voter != null) {
                ACLEventListener acl = new ACLEventListener(voter);
                append(EventType.POST_LOAD, acl);
                append(EventType.PRE_INSERT, acl);
                append(EventType.PRE_UPDATE, acl);
                append(EventType.PRE_DELETE, acl);
            }

            EventLogListener ell = new ome.security.basic.EventLogListener(cd);
            append(EventType.POST_INSERT, ell);
            append(EventType.POST_UPDATE, ell);
            append(EventType.POST_DELETE, ell);

            UpdateEventListener uel = new UpdateEventListener(cd);
            append(EventType.PRE_UPDATE, uel);

            if (debugAll) {
                Object debug = getDebuggingProxy();
                for (EventType type : EventType.values()) {
                    append(type, debug);
                }
            }
        }

        // ~ Helpers
        // =========================================================================
        private Class[] allInterfaces() {
            final Collection<EventType> allTypes = EventType.values();
            final Set<Class> set = new HashSet<Class>();
            for (EventType type : allTypes) {
                set.add(type.baseListenerInterface());
            }
            return set.toArray(new Class[set.size()]);
        }

        private Object getDisablingProxy() {
            EventMethodInterceptor disable = new EventMethodInterceptor(
                    new EventMethodInterceptor.DisableAction());
            return getProxy(disable);
        }

        private Object getDebuggingProxy() {
            EventMethodInterceptor debug = new EventMethodInterceptor();
            debug.setDebug(true);
            return getProxy(debug);
        }

        private Object getProxy(Advice... adviceArray) {
            ProxyFactory factory = new ProxyFactory();
            factory.setInterfaces(allInterfaces());
            for (Advice advice : adviceArray) {
                factory.addAdvice(advice);
            }
            return factory.getProxy();
        }

        // ~ Collection methods
        // =========================================================================

        /** calls override for each event type */
        private void override(EventType[] types, Object object) {
            for (EventType type : types) {
                override(type, object);
            }
        }

        /** first re-initializes the list for key, and then adds object */
        private void override(EventType type, Object object) {
            this.registry.setListeners(type, object);
        }

        /**
         * appends the objects to the existing list identified by key. If no list is
         * found, initializes. If there are no objects, just initializes if
         * necessary.
         */
        private void append(EventType type, Object... objs) {
            this.registry.appendListeners(type, objs);
        }

        /**
         * adds the objects to the existing list identified by key. If no list is
         * found, initializes. If there are no objects, just initializes if
         * necessary.
         */
        private void prepend(EventType type, Object... objs) {
            this.registry.prependListeners(type, objs);
        }
    }
}
