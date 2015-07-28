/*
 * ome.security.basic.EventListenersFactoryBean
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

// Java imports
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import ome.security.ACLEventListener;
import ome.security.ACLVoter;
import ome.tools.hibernate.EventMethodInterceptor;
import ome.tools.hibernate.ReloadingRefreshEventListener;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.event.EventListeners;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 * configuring all the possible {@link EventListeners event listeners} within
 * XML can be cumbersome.
 */
public class EventListenersFactoryBean extends AbstractFactoryBean {

    private final EventListeners eventListeners = new EventListeners();

    private final Map<String, LinkedList<Object>> map = new HashMap<String, LinkedList<Object>>();

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
     * {@link LocalSessionFactoryBean#setEventListeners(Map)}
     */
    @Override
    public Class getObjectType() {
        return Map.class;
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
        put("auto-flush", eventListeners.getAutoFlushEventListeners());
        put("merge", eventListeners.getMergeEventListeners());
        put("create", eventListeners.getPersistEventListeners());
        put("create-onflush", eventListeners.getPersistOnFlushEventListeners());
        put("delete", eventListeners.getDeleteEventListeners());
        put("dirty-check", eventListeners.getDirtyCheckEventListeners());
        put("evict", eventListeners.getEvictEventListeners());
        put("flush", eventListeners.getFlushEventListeners());
        put("flush-entity", eventListeners.getFlushEntityEventListeners());
        put("load", eventListeners.getLoadEventListeners());
        put("load-collection", eventListeners
                .getInitializeCollectionEventListeners());
        put("lock", eventListeners.getLockEventListeners());
        put("refresh", eventListeners.getRefreshEventListeners());
        put("replicate", eventListeners.getReplicateEventListeners());
        put("save-update", eventListeners.getSaveOrUpdateEventListeners());
        put("save", eventListeners.getSaveEventListeners());
        put("update", eventListeners.getUpdateEventListeners());
        put("pre-load", eventListeners.getPreLoadEventListeners());
        put("pre-update", eventListeners.getPreUpdateEventListeners());
        put("pre-delete", eventListeners.getPreDeleteEventListeners());
        put("pre-insert", eventListeners.getPreInsertEventListeners());
        put("post-load", eventListeners.getPostLoadEventListeners());
        put("post-update", eventListeners.getPostUpdateEventListeners());
        put("post-delete", eventListeners.getPostDeleteEventListeners());
        put("post-insert", eventListeners.getPostInsertEventListeners());
        put("post-commit-update", eventListeners
                .getPostCommitUpdateEventListeners());
        put("post-commit-delete", eventListeners
                .getPostCommitDeleteEventListeners());
        put("post-commit-insert", eventListeners
                .getPostCommitInsertEventListeners());
        assertHasAllKeys();
        overrides();
        additions();
        return map;
    }

    // ~ Configuration
    // =========================================================================

    protected boolean debugAll = false;

    /** for setter injection */
    public void setDebugAll(boolean debug) {
        this.debugAll = debug;
    }

    protected void overrides() {
        override("merge", new MergeEventListener(cd, th));
        override("save", new SaveEventListener(cd, th));
        override(new String[] { "replicate", "update" }, getDisablingProxy());
    }

    protected void additions() {
        // This must be prepended because it updates the alreadyRefreshed
        // cache before passing the event on the default listener.
        prepend("refresh", new ReloadingRefreshEventListener());

        for (String key : map.keySet()) {
            final String k = key;
            EventMethodInterceptor emi = new EventMethodInterceptor(
                    new EventMethodInterceptor.DisableAction() {
                        @Override
                        protected boolean disabled(MethodInvocation mi) {
                            return cd.isDisabled(k);// getType(mi));
                        }
                    });
            append(key, getProxy(emi));
        }

        if (voter != null) {
            ACLEventListener acl = new ACLEventListener(voter);
            append("post-load", acl);
            append("pre-insert", acl);
            append("pre-update", acl);
            append("pre-delete", acl);
        }

        EventLogListener ell = new ome.security.basic.EventLogListener(cd);
        append("post-insert", ell);
        append("post-update", ell);
        append("post-delete", ell);

        UpdateEventListener uel = new UpdateEventListener(cd);
        append("pre-update", uel);

        if (debugAll) {
            Object debug = getDebuggingProxy();
            for (String key : map.keySet()) {
                map.get(key).add(debug);
            }
        }
    }

    // ~ Helpers
    // =========================================================================
    private void assertHasAllKeys() {
        // eventListeners has only private state. :(
    }

    private Class[] allInterfaces() {
        Set<Class> set = new HashSet<Class>();
        for (String str : map.keySet()) {
            Class iface = eventListeners.getListenerClassFor(str);
            if (iface == null) {
                logger.warn("No interface found for " + str);
            }

            else {
                set.add(iface);
            }
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

    /** calls override for each key */
    protected void override(String[] keys, Object object) {
        for (String key : keys) {
            override(key, object);
        }
    }

    /** first re-initializes the list for key, and then adds object */
    protected void override(String key, Object object) {
        put(key, null);
        append(key, object);
    }

    /**
     * appends the objects to the existing list identified by key. If no list is
     * found, initializes. If there are no objects, just initializes if
     * necessary.
     */
    protected void append(String key, Object... objs) {
        LinkedList<Object> l = map.get(key);
        if (l == null) {
            put(key, null);
            l = map.get(key);
        }
        if (objs == null) {
            return;
        }
        for (Object object : objs) {
            l.addLast(object);
        }
    }

    /**
     * adds the objects to the existing list identified by key. If no list is
     * found, initializes. If there are no objects, just initializes if
     * necessary.
     */
    protected void prepend(String key, Object... objs) {
        LinkedList<Object> l = map.get(key);
        if (l == null) {
            put(key, null);
            l = map.get(key);
        }
        if (objs == null) {
            return;
        }
        for (Object object : objs) {
            l.addFirst(object);
        }
    }

    /**
     * replaces the key with the provided objects or an empty list if none
     * provided
     */
    protected void put(String key, Object[] objs) {
        LinkedList<Object> list = new LinkedList<Object>();
        if (objs != null) {
            Collections.addAll(list, objs);
        }
        map.put(key, list);
    }

}
