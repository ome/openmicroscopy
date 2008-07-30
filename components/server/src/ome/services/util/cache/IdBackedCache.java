/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.util.cache;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import ome.api.local.LocalQuery;
import ome.model.IObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * Wrapper which provides clean-up functionality. If the backing type is
 * removed, then the cache element will also be removed. The {@link #reap()}
 * method checks all current ids.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 */
public class IdBackedCache<K extends IObject, V extends Serializable> extends
        DelegatingCache<K, V> {

    private static final Log logger = LogFactory.getLog(IdBackedCache.class);

    private final LocalQuery query;

    public IdBackedCache(LocalQuery query, Cache<K, V> cache) {
        super(cache);
        this.query = query;
    }

    @Override
    public V get(long id) {
        if (null == query.find(cache.getType(), id)) {
            remove(id);
        }
        return super.get(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void reap() {
        super.reap();
        int sz = 1024;
        final List<Long> keys = cache.getKeys();
        long loops = keys.size() / 1024;
        for (int i = 0; i < loops; i++) {
            final List<Long> batch = keys.subList(sz * i, Math.min(keys.size(),
                    sz * (i + 1)));
            keys.removeAll((List<Long>) query.execute(new HibernateCallback() {
                public Object doInHibernate(Session session)
                        throws HibernateException, SQLException {
                    Query q = session.createQuery(String.format(
                            "select id from %s where id in (:ids)", cache
                                    .getType().getName()));
                    q.setParameterList("ids", batch);
                    return q.list();
                }

            }));
        }
        for (Long key : keys) {
            remove(key);
        }
    }

}
