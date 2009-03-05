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
import ome.model.meta.Experimenter;
import ome.model.meta.Share;
import ome.model.meta.ShareMember;
import ome.services.sharing.data.ShareData;
import ome.services.sharing.data.ShareItem;
import ome.system.OmeroContext;
import ome.tools.hibernate.QueryBuilder;
import ome.tools.hibernate.SessionFactory;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.EmptyResultDataAccessException;

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
        this.ctx = (OmeroContext) applicationContext;
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
        return (Long) session().createQuery("select sum(itemCount) from Share")
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
        synchronizeMembers(session, data);
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
    public List<ShareData> getShares(long userId, boolean own,
            boolean activeOnly) {

        Session session = factory.getSession();
        QueryBuilder qb = new QueryBuilder();
        qb.select("share.id");
        qb.from("ShareMember", "sm");
        qb.join("sm.parent", "share", false, false);
        qb.where();
        qb.and("sm.child.id = :userId");
        qb.param("userId", userId);
        if (own) {
            qb.and("share.owner.id = sm.child.id");
        } else {
            qb.and("share.owner.id != sm.child.id");
        }
        if (activeOnly) {
            qb.and("share.active is true");
        }
        Query query = qb.query(session);
        List<Long> shareIds = query.list();

        if (shareIds.size() == 0) {
            return new ArrayList<ShareData>(); // EARLY EXIT!
        }

        List<ShareData> rv = new ArrayList<ShareData>();
        try {
            Map<Long, byte[]> data = data(shareIds);
            for (Long id : data.keySet()) {
                byte[] bs = data.get(id);
                ShareData d = parse(id, bs);
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
    private Map<Long, byte[]> data(List<Long> ids) {
        Session session = session();
        Query q = session
                .createQuery("select id, data from Share where id in (:ids)");
        q.setParameterList("ids", ids);
        List<Object[]> data = q.list();
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
    private synchronized void initialize() {

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

    private void synchronizeMembers(Session session, ShareData data) {

        Query q = session.createQuery("select sm from ShareMember sm "
                + "where sm.parent = ?");
        q.setLong(0, data.id);
        List<ShareMember> members = q.list();
        Map<Long, ShareMember> lookup = new HashMap<Long, ShareMember>();
        for (ShareMember sm : members) {
            lookup.put(sm.getChild().getId(), sm);
        }

        Set<Long> intendedUserIds = new HashSet<Long>(data.members);
        intendedUserIds.add(data.owner);

        Set<Long> currentUserIds = lookup.keySet();

        Set<Long> added = new HashSet<Long>(intendedUserIds);
        added.removeAll(currentUserIds);
        for (Long toAdd : added) {
            ShareMember sm = new ShareMember();
            sm.link(new Share(data.id, false), new Experimenter(toAdd, false));
            session.merge(sm);
        }

        Set<Long> removed = new HashSet<Long>(currentUserIds);
        removed.removeAll(intendedUserIds);
        for (Long toRemove : removed) {
            session.delete(lookup.get(toRemove));
        }

    }

}
