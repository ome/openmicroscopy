/*   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.util;

import java.io.IOException;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.event.CacheEventListener;
import ome.system.OmeroContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 * @author Josh Moore
 * @since 3.0-Beta3
 */
public class CacheWithListenersFactoryBean extends EhCacheFactoryBean 
	implements ApplicationContextAware {

    private OmeroContext ctx;
    
    public void setApplicationContext(ApplicationContext arg0)
    		throws BeansException {
    	ctx = (OmeroContext) arg0;
    }
    
    @Override
    public Object getObject() {
    	Ehcache cache = (Ehcache) super.getObject();
    	cache.getCacheEventNotificationService()
    		.registerListener(new Listener(ctx));
    	return cache;
    }

}

class Listener implements CacheEventListener {

	private final static Log log = LogFactory.getLog(Listener.class);
	
	private final OmeroContext context;
	
	Listener(OmeroContext ctx) {
		this.context = ctx;
	}
	
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
		context.publishEvent(
				new ExpiredServantMessage(this, idString(arg1).toString()));
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
		if (key instanceof Ice.Identity) {
			return Ice.Util.identityToString((Ice.Identity)key);	
		} else { 
			return key.toString();
		}
	}
	
}