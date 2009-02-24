/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sharing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.api.IShare;
import ome.conditions.OptimisticLockException;
import ome.model.IObject;
import ome.model.meta.Share;
import ome.services.sharing.data.ShareData;
import ome.services.sharing.data.ShareItem;
import ome.system.OmeroContext;
import ome.tools.hibernate.SessionFactory;

import org.hibernate.Session;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

/**
 * 
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see IShare
 */
public class BlobShareStore extends ShareStore implements
        ApplicationContextAware {

    /**
     * Used to obtain sessions for querying and updating the store during normal
     * operation.
     */
    protected SessionFactory factory;

    protected OmeroContext ctx;

    /**
     * Because there is a cyclical dependency SF->ACLVoter->BlobStore->SF we
     * have to lazy-load the session factory via the context.
     */
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.ctx = (OmeroContext) ctx;
    }

    // Initialization/Destruction

    @Override
    public void doInit() {
        // Currently nothing.
    }

    // Overrides
    // =========================================================================

    @Override
    public Long totalShares() {
        return (Long) session().createQuery("select count(id) from Share")
                .uniqueResult();
    }

    @Override
    public Long totalSharedItems() {
        return (Long) session().createQuery("select sum(items) from Share")
                .uniqueResult();
    }

    @Override
    public void doSet(Share share, ShareData data, List<ShareItem> items) {

        long oldOptLock = data.optlock;
        long newOptLock = oldOptLock + 1;
        Session session = session();

        List list = session.createQuery(
                "select s from Share s where s.id = " + data.id
                        + " and s.version =" + data.optlock).list();

        if (list.size() == 0) {
            throw new OptimisticLockException("Share " + data.id
                    + " has been updated by someone else.");
        }

        data.optlock = newOptLock;
        share.setData(parse(data));
        share.setActive(data.enabled);
        share.setItemCount((long) items.size());
        share.setVersion((int) newOptLock);
        session.merge(share);

    }

    @Override
    public ShareData get(final long id) {
        Session session = session();
        Share s = (Share) session.get(Share.class, id);
        if (s == null) {
            return null;
        }
        byte[] data = s.getData();
        return parse(id, data);
    }

    @Override
    public List<ShareData> getShares(boolean activeOnly) {
        List<ShareData> rv = new ArrayList<ShareData>();
        try {
            Map<Long, byte[]> data = data();
            for (Long id : data.keySet()) {
                byte[] bs = data.get(id);
                ShareData d = parse(id, bs);
                if (activeOnly && !d.enabled) {
                    continue;
                }
                rv.add(d);
            }
            return rv;
        } catch (EmptyResultDataAccessException empty) {
            return null;
        }
    }

    @Override
    public List<ShareData> getShares(long userId, boolean own,
            boolean activeOnly) {
        List<ShareData> rv = new ArrayList<ShareData>();
        try {
            Map<Long, byte[]> data = data();
            for (Long id : data.keySet()) {
                byte[] bs = data.get(id);
                ShareData d = parse(id, bs);
                if (activeOnly && !d.enabled) {
                    continue;
                }
                if (own) {
                    if (d.owner != userId) {
                        continue;
                    }
                } else {
                    if (!d.members.contains(userId)) {
                        continue;
                    }
                }
                rv.add(d);
            }
            return rv;
        } catch (EmptyResultDataAccessException empty) {
            return null;
        }
    }

    @Override
    public <T extends IObject> boolean doContains(long sessionId, Class<T> kls,
            long objId) {

        ShareData data = get(sessionId);
        if (data == null) {
            return false;
        } else {
            List<Long> ids = data.objectMap.get(kls.getName());
            if (ids == null) {
                return false;
            }
            return ids.contains(objId);
        }
    }

    @Override
    public void doClose() {
        // no-op
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Long> keys() {
        Session session = session();
        List list = session.createQuery("select id from Share").list();
        return new HashSet<Long>(list);
    }

    // Helpers
    // =========================================================================

    /**
     * Returns a list of data from all shares.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    private Map<Long, byte[]> data() {
        Session session = session();
        List<Object[]> data = session.createQuery("select id, data from Share")
                .list();
        Map<Long, byte[]> rv = new HashMap<Long, byte[]>();
        for (Object[] objects : data) {
            rv.put((Long) objects[0], (byte[]) objects[1]);
        }
        return rv;
    }

    private Session session() {
        initialize();
        return factory.getSession();
    }

    /**
     * Loads the {@link SessionFactory}
     */
    private void initialize() {

        if (factory != null) {
            return; // GOOD!
        }

        if (ctx == null) {
            throw new IllegalStateException("Have no context to load factory");
        }

        factory = (SessionFactory) ctx.getBean("omeroSessionFactory");

        if (factory == null) {
            throw new IllegalStateException("Cannot find factory");
        }

        // Finally calling init here, since before it's not possible
        init();
    }

}
