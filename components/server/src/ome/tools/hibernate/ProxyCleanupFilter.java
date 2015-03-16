/*
 *   Copyright 2006-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools.hibernate;

// Java imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.api.StatefulServiceInterface;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.meta.Node;
import ome.model.meta.Session;
import ome.model.meta.Share;
import ome.security.ACLVoter;
import ome.security.basic.BasicACLVoter;
import ome.security.basic.CurrentDetails;
import ome.system.EventContext;
import ome.util.ContextFilter;
import ome.util.Filterable;
import ome.util.Utils;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.Hibernate;
import org.hibernate.collection.AbstractPersistentCollection;

/**
 * removes all proxies from a return graph to prevent ClassCastExceptions and
 * Session Closed exceptions. You need to be careful with printing. Calling
 * toString() on an unitialized object will break before filtering is complete.
 * 
 * Note: we aren't setting the filtered collections here because it's "either
 * null/unloaded or filtered". We will definitiely filter here, so it would just
 * increase bandwidth.
 *
 * As of 4.4.0, this class is also responsible for various security-based
 * modifications to returned {@link Details} objects.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 1.0
 * @see ticket 8277
 */
public class ProxyCleanupFilter extends ContextFilter {

    protected Map unloadedObjectCache = new IdentityHashMap();

    protected final ACLVoter acl;

    protected final CurrentDetails current;

    /**
     * Passes null to {@link ProxyCleanupFilter#ProxyCleanupFilter(CurrentDetails)}
     * such that all restricted objects will be unloaded.
     */
    public ProxyCleanupFilter() {
        this(null, null);
    }

    /**
     * Constructor take a {@link CurrentDetails} object in order to check
     * the security restrictions on certain objects.
     */
    public ProxyCleanupFilter(ACLVoter acl, CurrentDetails current) {
        this.acl = acl;
        this.current = current;
    }

    @Override
    public Filterable filter(String fieldId, Filterable f) {
        if (f == null) {
            return null;
        }

        if (unloadedObjectCache.containsKey(f)) {
            return (IObject) unloadedObjectCache.get(f);
        }

        // Filtering all node objects for security reasons
        if (f instanceof Node) {
            return new Node((((Node) f).getId()), false);
        }
        
        // A proxy; send over the wire in altered form.
        if (!Hibernate.isInitialized(f)) {

            if (f instanceof IObject) {
                IObject proxy = (IObject) f;
                IObject unloaded = Utils.trueInstance(f.getClass().asSubclass(IObject.class));
                unloaded.setId(proxy.getId());
                unloaded.unload();
                unloadedObjectCache.put(f, unloaded);
                return unloaded;
            } else if (f instanceof Details) {
                // Currently Details is only "known" non-IObject Filterable
                // Unlikely that this is ever reached.
                Details d = (Details) f;
                d = (Details) super.filter(fieldId, d.shallowCopy());
                return d;
            } else {
                // TODO Here there's not much we can do. copy constructor?
                throw new RuntimeException(
                        "Bailing out. Don't want to set to a value to null.");
            }

            // Not a proxy; it will be serialized and sent over the wire.
        } else {

            if (f instanceof IObject) {
                if (acl != null) {
                    // When acl is null, assume this is for internal use
                    // and therefore the object will not be passed out.
                    // See ticket:8794 and OmeroMetadata.java
                    acl.postProcess((IObject) f);
                }
            }

            // Also for security reasons, we're now checking ownership
            // of sessions.
            if (f instanceof Share) {
                // ticket:2733
                Share share = (Share) f;
                if ( ! share.isLoaded()) {
                    return share;
                } else if (share.retrieve("#2733") == null) {
                    return new Share(share.getId(), false);
                }
            } else if (f instanceof Session) {

                Session session = (Session) f;
                if ( ! session.isLoaded()) {
                    return session;
                } else if (acl == null) {
                    return new Session(session.getId(), false);
                } else {
                    EventContext ec = current.getCurrentEventContext();
                    if (!ec.isCurrentUserAdmin()) {
                        Long uid = session.getOwner().getId();
                        if (!ec.getCurrentUserId().equals(uid)) {
                            return new Session(session.getId(), false);
                        }
                    }
                }
            }

            // Any clean up here.
            return super.filter(fieldId, f);

        }

    }

    @Override
    public Collection filter(String fieldId, Collection c) {
        // is a proxy. null it. will be refilled by
        // MergeEventListener on re-entry.
        if (null == c || !Hibernate.isInitialized(c)) {
            return null;
        }

        Collection retVal = super.filter(fieldId, c);

        // ticket:61 : preventing Hibernate collection types from escaping.
        if (retVal instanceof AbstractPersistentCollection) {
            if (retVal instanceof Set) {
                retVal = new HashSet(retVal);
            } else if (retVal instanceof List) {
                retVal = new ArrayList(retVal);
            }

        } // end ticket:61

        return retVal;
    }

    @Override
    public Map filter(String fieldId, Map m) {

        if (null == m || !Hibernate.isInitialized(m)) {
            return null;
        }

        Map retVal = super.filter(fieldId, m);

        // ticket:61 : preventing Hibernate collection types from escaping.
        if (retVal instanceof AbstractPersistentCollection) {
            retVal = new HashMap(retVal);
        } // end ticket:61

        return retVal;
    }

    // TODO FIXME need to further test this.
    @Override
    protected void doFilter(String arg0, Object arg1) {
        if (arg1 instanceof Object[]) {
            Object[] arr = (Object[]) arg1;
            for (int i = 0; i < arr.length; i++) {
                arr[i] = this.filter(arg0, arr[i]);
            }
        }

    }

    /** wraps a filter for each invocation */
    public static class Interceptor implements MethodInterceptor {

        private final SessionHandler sessions;

        private final ACLVoter acl;

        private final CurrentDetails current;

        public Interceptor(ACLVoter acl, SessionHandler sessions,
                CurrentDetails current) {
            this.acl = acl;
            this.sessions = sessions;
            this.current = current;
        }

        private final ThreadLocal<Integer> depth = new ThreadLocal<Integer>() {
            @Override
            protected Integer initialValue() {
                return Integer.valueOf(0);
            }
        };

        public Object invoke(MethodInvocation arg0) throws Throwable {

            int d = depth.get().intValue();
            if (d == 0) {
                sessions.cleanThread();
            }

            Object result;
            d++;
            depth.set(d);
            try {

                result = arg0.proceed();
                if (!StatefulServiceInterface.class.isAssignableFrom(arg0
                        .getThis().getClass())) {
                    result = new ProxyCleanupFilter(acl, current)
                        .filter(null, result);
                }
            } finally {
                d--;
                depth.set(d);
            }

            return result;
        }
    }

}
