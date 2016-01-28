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

/**
 * Basic {@link CacheEventListener} which does nothing.
 * 
 * @author Josh Moore
 * @since 3.0-Beta3
 */
public class CacheListener implements CacheEventListener {

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public void dispose() {
    }

    public void notifyElementEvicted(Ehcache arg0, Element arg1) {
    }

    public void notifyElementExpired(Ehcache arg0, Element arg1) {
    }

    public void notifyElementPut(Ehcache arg0, Element arg1)
            throws CacheException {
    }

    public void notifyElementRemoved(Ehcache arg0, Element arg1)
            throws CacheException {
    }

    public void notifyElementUpdated(Ehcache arg0, Element arg1)
            throws CacheException {
    }

    public void notifyRemoveAll(Ehcache arg0) {
    }

}