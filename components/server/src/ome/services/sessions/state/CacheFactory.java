/*   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions.state;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.event.CacheEventListener;
import ome.system.OmeroContext;

import org.springframework.beans.BeansException;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 * @author Josh Moore
 * @since 3.0-Beta3
 */
public class CacheFactory extends EhCacheFactoryBean implements
        ApplicationContextAware {

    private OmeroContext ctx;

    private CacheEventListener[] cacheListeners;

    public void setApplicationContext(ApplicationContext arg0)
            throws BeansException {
        ctx = (OmeroContext) arg0;
    }

    public void setCacheEventListeners(CacheEventListener[] listeners) {
        this.cacheListeners = listeners;
    }

    @Override
    public Ehcache getObject() {
        Ehcache cache = (Ehcache) super.getObject();
        registerAll(cache, cacheListeners);
        return cache;
    }

    public Ehcache createCache(CacheEventListener... listeners) {
        try {
            super.afterPropertiesSet();
            Ehcache cache = (Ehcache) getObject();
            registerAll(cache, listeners);
            return cache;
        } catch (Exception e) {
            throw new RuntimeException("Could not create cache", e);
        }
    }

    protected void registerAll(Ehcache cache, CacheEventListener... l) {
        if (l != null) {
            for (CacheEventListener listener : l) {
                cache.getCacheEventNotificationService().registerListener(
                        listener);
            }
        }
    }
}