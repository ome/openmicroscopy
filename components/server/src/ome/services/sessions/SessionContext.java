/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions;

import java.util.List;

import ome.model.meta.Session;
import ome.services.sessions.state.SessionCache;
import ome.services.sessions.stats.SessionStats;
import ome.system.EventContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends {@link EventContext} to hold a {@link Session}. This is used by the
 * {@link SessionManager} to store information in the {@link SessionCache}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public interface SessionContext extends EventContext {

    Session getSession();

    List<String> getUserRoles();

    // Reference counting

    /**
     * The Share id is the only mutable bit in the {@link SessionContext}.
     */
    void setShareId(Long shareId);

    /**
     * Return a {@link SessionStats} implementation for this session.
     */
    SessionStats stats();

    /**
     * Returns the {@link Count} instance held by this context. This may be
     * shared with other contexts, so that in critical phases as when the context
     * is being copied, the reference count will be kept in sync.
     */
    Count count();

    /**
     * Synchronized counter which can be passed between {@link SessionContext}
     * instances as they are recreated.
     *
     * @see <a href="http://trac.openmicroscopy.org/ome/ticket/2804">ticket:2804</a>
     */
    public class Count {
        private final Logger log = LoggerFactory.getLogger(Count.class);
        private final Object[] refLock = new Object[0];
        private int ref;
        private String uuid;

        public Count(String uuid) {
            this.uuid = uuid;
        }

        /**
         * Return the current number of references which this session is aware of.
         */
        public int get() {
            synchronized (refLock) {
                return ref;
            }
        }

        /**
         * Increment the current {@link #ref reference count} and return the
         * new value atomically.
         */
        public int increment() {
            synchronized (refLock) {
                if (ref < 0) {
                    ref = 1;
                } else {
                    // This should never happen, but just in case
                    // some loop is incrementing indefinitely.
                    if (ref < Integer.MAX_VALUE) {
                        ref = ref + 1;
                        if (log.isDebugEnabled()) {
                            log.debug("+Reference count: " + uuid + "=" + ref);
                        }
                    } else {
                        log.warn("Reference count == MAX_VALUE");
                    }
                }
                return ref;
            }
        }

        /**
         * Decrement the current {@link #ref reference count} and return the
         * new value atomically.
         */
        public int decrement() {
            synchronized (refLock) {
                if (ref < 1) {
                    ref = 0;
                } else {
                    ref = ref - 1;
                    log.info("-Reference count: " + uuid + "=" + ref);
                }
                return ref;
            }
        }
    }
}
