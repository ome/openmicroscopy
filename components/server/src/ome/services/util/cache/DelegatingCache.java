/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.util.cache;

import java.io.Serializable;
import java.util.List;

import ome.model.IObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple base {@link Cache} which delegates all methods to the {@link Cache}
 * with which it is constructed. Good for subclassing other delegaters.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 */
public class DelegatingCache<K extends IObject, V extends Serializable>
        implements Cache<K, V> {

    private static final Log logger = LogFactory.getLog(DelegatingCache.class);

    protected final Cache<K, V> cache;

    public DelegatingCache(Cache<K, V> cache) {
        this.cache = cache;
    }

    public V get(long id) {
        return cache.get(id);
    }

    public void put(long id, V s) {
        cache.put(id, s);
    }

    public void remove(long id) {
        cache.remove(id);
    }

    @SuppressWarnings("unchecked")
    public void reap() {
        cache.reap();
    }

    public List<Long> getKeys() {
        return cache.getKeys();
    }

    public Class<K> getType() {
        return cache.getType();
    }
}
