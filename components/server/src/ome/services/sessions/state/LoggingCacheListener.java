/*   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions.state;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic {@link CacheEventListener} which does nothing other than log.
 * 
 * @author Josh Moore
 * @since 3.0-Beta3
 */
class LoggingCacheListener implements CacheEventListener {

    private final static Logger log = LoggerFactory
            .getLogger(LoggingCacheListener.class);

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public void dispose() {
        log.debug("Disposing cache event listener.");
    }

    public void notifyElementEvicted(Ehcache arg0, Element arg1) {
        if (log.isDebugEnabled()) {
            log.debug("Evicting element: " + idString(arg1));
        }
    }

    public void notifyElementExpired(Ehcache arg0, Element arg1) {
        if (log.isDebugEnabled()) {
            log.debug("Expiring servant: " + idString(arg1));
        }
    }

    public void notifyElementPut(Ehcache arg0, Element arg1)
            throws CacheException {
        if (log.isDebugEnabled()) {
            log.debug("Putting element: " + idString(arg1));
        }
    }

    public void notifyElementRemoved(Ehcache arg0, Element arg1)
            throws CacheException {
        if (log.isDebugEnabled()) {
            log.debug("Removing element: " + idString(arg1));
        }
    }

    public void notifyElementUpdated(Ehcache arg0, Element arg1)
            throws CacheException {
        if (log.isDebugEnabled()) {
            log.debug("Updating element: " + idString(arg1));
        }
    }

    public void notifyRemoveAll(Ehcache arg0) {
        log.debug("Removing all elements from servant cache.");
    }

    protected String idString(Element elt) {
        Object key = elt.getObjectKey();
        return key.toString();
    }

}
