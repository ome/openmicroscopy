/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.util.cache;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import ome.api.local.LocalQuery;
import ome.model.IObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Disk cache which is keyed to a particular type.
 * 
 * Possible to wrap with {@link IdBackedStore} to have elements removed if the
 * backing type is also removed.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 */
public class DiskCache<K extends IObject, V extends Serializable> implements
        Cache<K, V> {

    private static final Log logger = LogFactory.getLog(DiskCache.class);

    private final Ehcache cache;

    private final Class<K> type;

    private final String name;

    private final File dataDir;

    public DiskCache(LocalQuery query, CacheManager manager, String name,
            File dataDir, Class<K> objectType) {
        net.sf.ehcache.Cache cache = new net.sf.ehcache.Cache(name,
                100 /* inmemory */, MemoryStoreEvictionPolicy.LRU, true,
                dataDir.getPath() + this.name, true /* eternal */, 0, 0
                /*
                 * idle and live
                 */
                , true /* diskpersistent */, 10000L /*
                                                     * diskexpirtthread secs
                                                     */, null /*
                             * new RegisteredEventListeners()
                             */);
        this.dataDir = dataDir;
        this.cache = null;
        this.type = objectType;
        this.name = name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.services.util.cache.Cache#getType()
     */
    public Class<K> getType() {
        return type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.services.util.cache.Cache#getKeys()
     */
    @SuppressWarnings("unchecked")
    public List<Long> getKeys() {
        return cache.getKeysWithExpiryCheck();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.services.util.cache.Cache#get(long)
     */
    @SuppressWarnings("unchecked")
    public V get(long id) {
        return (V) cache.get(id).getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.services.util.cache.Cache#put(long, V)
     */
    public void put(long id, V s) {
        cache.put(new Element(id, s));
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.services.util.cache.Cache#remove(long)
     */
    public void remove(long id) {
        cache.remove(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.services.util.cache.Cache#reap()
     */
    @SuppressWarnings("unchecked")
    public void reap() {
        // no-op
    }
}
