/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sharing;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import ome.annotations.NotNull;
import ome.annotations.RolesAllowed;
import ome.api.IQuery;
import ome.api.IShare;
import ome.api.IUpdate;
import ome.api.ServiceInterface;
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.CommentAnnotation;
import ome.model.annotations.SessionAnnotationLink;
import ome.model.internal.Details;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.Session;
import ome.model.meta.Share;
import ome.parameters.Parameters;
import ome.security.AdminAction;
import ome.security.SecureAction;
import ome.security.SecuritySystem;
import ome.security.basic.CurrentDetails;
import ome.services.sessions.SessionContext;
import ome.services.sessions.SessionManager;
import ome.services.sharing.data.Obj;
import ome.services.sharing.data.ShareData;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.util.ContextFilter;
import ome.util.Filterable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see IShare
 */
@Transactional(readOnly = true)
public class ShareBean implements IShare {

    public final static Log log = LogFactory.getLog(ShareBean.class);

    public final static String NS_ENABLED = "ome.share.enabled";

    public final static String NS_COMMENT = "ome.share.comment/";

    final protected Executor executor;

    final protected CurrentDetails details;

    final protected SecuritySystem secSys;

    final protected SessionManager sessionManager;

    final protected ShareStore store;

    abstract class SecureShare implements SecureAction {

        public final <T extends IObject> T updateObject(T... objs) {
            doUpdate((Share) objs[0]);
            return null;
        }

        abstract void doUpdate(Share share);

    }

    class SecureStore extends SecureShare {

        ShareData data;

        SecureStore(ShareData data) {
            this.data = data;
        }

        @Override
        void doUpdate(Share share) {
            store.update(share, data);
        }

    }

    public final Class<? extends ServiceInterface> getServiceInterface() {
        return IShare.class;
    }

    public ShareBean(CurrentDetails details, Executor executor,
            SecuritySystem secSys, SessionManager mgr, ShareStore store) {
        this.store = store;
        this.secSys = secSys;
        this.details = details;
        this.executor = executor;
        this.sessionManager = mgr;
    }

    // ~ Service Methods
    // ===================================================

    @RolesAllowed("user")
    public void activate(long shareId) {

        // Check status of the store
        ShareData data = getShareIfAccessibble(shareId);
        if (data == null) {
            throw new ValidationException("No accessible share:" + shareId);
        }
        if (!data.enabled) {
            throw new ValidationException("Share disabled.");
        }

        // Ok, set share
        SessionContext sc = (SessionContext) sessionManager
                .getEventContext(principal());
        sc.setShareId(shareId);
    }

    @RolesAllowed("user")
    public void deactivate() {
        SessionContext sc = (SessionContext) sessionManager
                .getEventContext(principal());
        sc.setShareId(null);
    }

    // ~ Admin
    // =========================================================================

    @RolesAllowed("system")
    public Set<Session> getAllShares(boolean active) {
        List<ShareData> shares = _getShares(active);
        return sharesToSessions(shares);
    }

    // ~ Getting shares and objects (READ)
    // =========================================================================

    @RolesAllowed("user")
    public Set<Session> getOwnShares(boolean active) {
        long id = userId();
        List<ShareData> shares = _getShares(id, true, active);
        return sharesToSessions(shares);
    }

    @RolesAllowed("user")
    public Set<Session> getMemberShares(boolean active) {
        long id = userId();
        List<ShareData> shares = _getShares(id, false, active);
        return sharesToSessions(shares);
    }

    @RolesAllowed("user")
    public Set<Session> getSharesOwnedBy(@NotNull Experimenter user,
            boolean active) {
        List<ShareData> shares = _getShares(user.getId(), true /* own */,
                active);
        return sharesToSessions(shares);
    }

    @RolesAllowed("user")
    public Set<Session> getMemberSharesFor(@NotNull Experimenter user,
            boolean active) {
        List<ShareData> shares = _getShares(user.getId(), false /* own */,
                active);
        return sharesToSessions(shares);
    }

    @RolesAllowed("user")
    public Session getShare(long sessionId) {
        ShareData data = _retrieve(sessionId);
        throwOnNullData(sessionId, data);
        Session session = shareToSession(data);
        return session;
    }

    @RolesAllowed("user")
    public <T extends IObject> List<T> getContents(long shareId) {
        ShareData data = _retrieve(shareId);
        throwOnNullData(shareId, data);
        return list(data.objectList);
    }

    @RolesAllowed("user")
    public <T extends IObject> List<T> getContentSubList(long shareId,
            int start, int finish) {
        ShareData data = _retrieve(shareId);
        throwOnNullData(shareId, data);
        try {
            return list(data.objectList.subList(start, finish));
        } catch (IndexOutOfBoundsException ioobe) {
            throw new ApiUsageException("Invalid range: " + start + " to "
                    + finish);
        }
    }

    @RolesAllowed("user")
    public <T extends IObject> Map<Class<T>, List<Long>> getContentMap(
            long shareId) {
        ShareData data = _retrieve(shareId);
        throwOnNullData(shareId, data);
        return map(data.objectMap);
    }

    @RolesAllowed("user")
    public int getContentSize(long shareId) {
        ShareData data = _retrieve(shareId);
        throwOnNullData(shareId, data);
        return data.objectList.size();
    }

    // ~ Creating share (WRITE)
    // =========================================================================

    @RolesAllowed("user")
    @SuppressWarnings("unchecked")
    public <T extends IObject> long createShare(
            @NotNull final String description, Timestamp expiration,
            List<T> items, List<Experimenter> exps, List<String> guests,
            final boolean enabled) {

        //
        // Input validation
        //
        final long time = expirationAsLong(expiration);

        if (exps == null) {
            exps = Collections.emptyList();
        }
        if (guests == null) {
            guests = Collections.emptyList();
        }
        if (items == null) {
            items = Collections.emptyList();
        }

        //
        // Setting defaults on new session
        //
        final String omename = userName();
        final Long user = userId();
        final Future<Long> future = executor.submit(new Callable<Long>() {
            public Long call() throws Exception {
                return sessionManager.createShare(new Principal(omename),
                        enabled, time, "SHARE", description).getId();
            }
        });

        final List<T> _items = items;
        final List<String> _guests = guests;
        final List<Long> _users = new ArrayList<Long>(exps.size());
        for (Experimenter e : exps) {
            _users.add(e.getId());
        }

        final Long shareId = executor.get(future);
        Share rv = (Share) executor.execute(null, new Executor.SimpleWork(this,
                "createShare") {
            @Transactional(readOnly = false)
            public Object doWork(org.hibernate.Session session,
                    final ServiceFactory sf) {

                Share share = sf.getQueryService().get(Share.class, shareId);

                secSys.doAction(new SecureShare() {
                    @Override
                    void doUpdate(Share share) {
                        store
                                .set(share, user, _items, _users, _guests,
                                        enabled);
                    }
                }, share);
                adminFlush(sf);
                return share;

            }

        });

        return rv.getId();
    }

    @RolesAllowed("user")
    public void setDescription(long shareId, @NotNull String description) {
        String uuid = idToUuid(shareId);
        final Session session = sessionManager.find(uuid);
        session.setMessage(description);
        Future<Object> future = executor.submit(new Callable<Object>() {
            public Object call() throws Exception {
                sessionManager.update(session);
                return null;
            }
        });
        executor.get(future);
    }

    @RolesAllowed("user")
    public void setExpiration(long shareId, @NotNull Timestamp expiration) {
        String uuid = idToUuid(shareId);
        Session session = sessionManager.find(uuid);
        session.setTimeToLive(expirationAsLong(expiration));
        sessionManager.update(session);
    }

    @RolesAllowed("user")
    public void setActive(long shareId, boolean active) {
        ShareData data = _retrieve(shareId);
        throwOnNullData(shareId, data);
        data.enabled = active;
        _store(shareId, data);
    }

    @RolesAllowed("user")
    public void closeShare(long shareId) {
        String uuid = idToUuid(shareId);
        sessionManager.close(uuid);
    }

    // ~ Getting items
    // =========================================================================

    @RolesAllowed("user")
    public <T extends IObject> void addObjects(long shareId,
            @NotNull T... objects) {
        ShareData data = _retrieve(shareId);
        throwOnNullData(shareId, data);
        Graph graph = new Graph();
        for (T object : objects) {
            graph.filter("top", object);
        }
        _addGraph(data, graph);
        _store(shareId, data);
    }

    @RolesAllowed("user")
    public <T extends IObject> void addObject(long shareId, @NotNull T object) {
        ShareData data = _retrieve(shareId);
        throwOnNullData(shareId, data);
        Graph graph = new Graph();
        graph.filter("top", object);
        _addGraph(data, graph);
        _store(shareId, data);
    }

    @RolesAllowed("user")
    public <T extends IObject> void removeObjects(long shareId,
            @NotNull T... objects) {
        ShareData data = _retrieve(shareId);
        throwOnNullData(shareId, data);
        for (T object : objects) {
            List<Long> ids = data.objectMap.get(object.getClass().getName());
            if (ids != null) {
                ids.remove(object.getId());
            }
        }
        List<Obj> toRemove = new ArrayList<Obj>();
        for (T object : objects) {
            for (Obj obj : data.objectList) {
                if (obj.type.equals(object.getClass().getName())) {
                    if (obj.id == object.getId().longValue()) {
                        toRemove.add(obj);
                    }
                }
            }
        }
        data.objectList.removeAll(toRemove);
        _store(shareId, data);
    }

    @RolesAllowed("user")
    public <T extends IObject> void removeObject(long shareId, @NotNull T object) {
        ShareData data = _retrieve(shareId);
        List<Long> ids = data.objectMap.get(object.getClass().getName());
        if (ids != null) {
            ids.remove(object.getId());
        }
        List<Obj> toRemove = new ArrayList<Obj>();
        for (Obj obj : data.objectList) {
            if (obj.type.equals(object.getClass().getName())) {
                if (obj.id == object.getId().longValue()) {
                    toRemove.add(obj);
                }
            }
        }
        data.objectList.removeAll(toRemove);
        _store(shareId, data);
    }

    // ~ Getting comments
    // =========================================================================

    @SuppressWarnings("unchecked")
    @RolesAllowed("user")
    public List<Annotation> getComments(final long shareId) {

        final List<Annotation> rv = new ArrayList<Annotation>();
        if (getShareIfAccessibble(shareId) == null) {
            return rv; // EARLY EXIT.
        }

        // Now load the comments as an administrator,
        // otherwise it is necessary to add every link
        // to the share
        return (List<Annotation>) executor.execute(null,
                new Executor.SimpleWork(this, "getComments") {
                    @Transactional(readOnly = true)
                    public Object doWork(org.hibernate.Session session,
                            final ServiceFactory sf) {
                        secSys.runAsAdmin(new AdminAction() {
                            public void runAsAdmin() {
                                List<SessionAnnotationLink> links = sf
                                        .getQueryService()
                                        .findAllByQuery(
                                                "select l from SessionAnnotationLink l "
                                                        + "join fetch l.details.owner "
                                                        + "join fetch l.parent as share "
                                                        + "join fetch l.child as comment "
                                                        + "join fetch comment.details.updateEvent "
                                                        + "where share.id = :id and comment.ns like :ns ",
                                                new Parameters().addString(
                                                        "ns", NS_COMMENT + "%")
                                                        .addId(shareId));
                                for (SessionAnnotationLink link : links) {
                                    rv.add(link.child());
                                }

                            }
                        });
                        return rv;
                    }
                });
    }

    @RolesAllowed("user")
    public CommentAnnotation addComment(long shareId, @NotNull String comment) {
        Share s = new Share(shareId, false);
        CommentAnnotation commentAnnotation = new CommentAnnotation();
        commentAnnotation.setTextValue(comment);
        commentAnnotation.setNs(NS_COMMENT);
        final SessionAnnotationLink link = new SessionAnnotationLink(s,
                commentAnnotation);
        return (CommentAnnotation) executor.execute(null,
                new Executor.SimpleWork(this, "addComment") {
                    @Transactional(readOnly = false)
                    public Object doWork(org.hibernate.Session session,
                            ServiceFactory sf) {
                        return sf.getUpdateService().saveAndReturnObject(link)
                                .child();
                    }
                });
    }

    @RolesAllowed("user")
    public CommentAnnotation addReply(long shareId, @NotNull String comment,
            @NotNull CommentAnnotation replyTo) {
        throw new UnsupportedOperationException();
    }

    @RolesAllowed("user")
    public void deleteComment(@NotNull final Annotation comment) {

        executor.execute(null, new Executor.SimpleWork(this, "deleteComment") {
            @Transactional(readOnly = false)
            public Object doWork(org.hibernate.Session session,
                    ServiceFactory sf) {

                IQuery iQuery = sf.getQueryService();
                IUpdate iUpdate = sf.getUpdateService();

                List<SessionAnnotationLink> links = iQuery.findAllByQuery(
                        "select l from SessionAnnotationLink l "
                                + "where l.child.id = :id", new Parameters()
                                .addId(comment.getId()));
                for (SessionAnnotationLink sessionAnnotationLink : links) {
                    iUpdate.deleteObject(sessionAnnotationLink);
                }
                iUpdate.deleteObject(comment);
                return null;
            }
        });

    }

    // ~ Member administration
    // =========================================================================

    @RolesAllowed("user")
    public Set<Experimenter> getAllMembers(long shareId) {
        ShareData data = _retrieve(shareId);
        throwOnNullData(shareId, data);
        List<Experimenter> e = loadMembers(data);
        return new HashSet<Experimenter>(e);
    }

    @RolesAllowed("user")
    public Set<String> getAllGuests(long shareId) {
        ShareData data = _retrieve(shareId);
        throwOnNullData(shareId, data);
        return new HashSet<String>(data.guests);
    }

    @RolesAllowed("user")
    public Set<String> getAllUsers(long shareId) throws ValidationException {
        ShareData data = _retrieve(shareId);
        List<Experimenter> members = loadMembers(data);
        Set<String> names = new HashSet<String>();
        for (Experimenter e : members) {
            names.add(e.getOmeName());
        }
        for (String string : data.guests) {
            if (names.contains(string)) {
                throw new ValidationException(string
                        + " is both a guest name and a member name");
            } else {
                names.add(string);
            }
        }
        return names;
    }

    @RolesAllowed("user")
    public void addUsers(long shareId, Experimenter... exps) {
        List<Experimenter> es = Arrays.asList(exps);
        ShareData data = _retrieve(shareId);
        throwOnNullData(shareId, data);
        for (Experimenter experimenter : es) {
            data.members.remove(experimenter.getId());
        }
        _store(shareId, data);
    }

    @RolesAllowed("user")
    public void addGuests(long shareId, String... emailAddresses) {
        List<String> addresses = Arrays.asList(emailAddresses);
        ShareData data = _retrieve(shareId);
        throwOnNullData(shareId, data);
        data.guests.addAll(addresses);
        _store(shareId, data);
    }

    @RolesAllowed("user")
    public void removeUsers(long shareId, List<Experimenter> exps) {
        ShareData data = _retrieve(shareId);
        throwOnNullData(shareId, data);
        for (Experimenter experimenter : exps) {
            data.members.remove(experimenter.getId());
        }
        _store(shareId, data);
    }

    @RolesAllowed("user")
    public void removeGuests(long shareId, String... emailAddresses) {
        List<String> addresses = Arrays.asList(emailAddresses);
        ShareData data = _retrieve(shareId);
        data.guests.removeAll(addresses);
        _store(shareId, data);
    }

    @RolesAllowed("user")
    public void addUser(long shareId, Experimenter exp) {
        ShareData data = _retrieve(shareId);
        throwOnNullData(shareId, data);
        data.members.add(exp.getId());
        _store(shareId, data);
    }

    @RolesAllowed("user")
    public void addGuest(long shareId, String emailAddress) {
        ShareData data = _retrieve(shareId);
        throwOnNullData(shareId, data);
        data.guests.add(emailAddress);
        _store(shareId, data);
    }

    @RolesAllowed("user")
    public void removeUser(long shareId, Experimenter exp) {
        ShareData data = _retrieve(shareId);
        throwOnNullData(shareId, data);
        data.members.remove(exp.getId());
        _store(shareId, data);
    }

    @RolesAllowed("user")
    public void removeGuest(long shareId, String emailAddress) {
        ShareData data = _retrieve(shareId);
        throwOnNullData(shareId, data);
        data.guests.remove(emailAddress);
        _store(shareId, data);
    }

    // Events
    // =========================================================================

    @RolesAllowed("user")
    public Map<String, Experimenter> getActiveConnections(long shareId) {
        throw new UnsupportedOperationException();
    }

    @RolesAllowed("user")
    public List<Event> getEvents(long shareId, Experimenter experimenter,
            Timestamp from, Timestamp to) {
        throw new UnsupportedOperationException();
    }

    @RolesAllowed("user")
    public Map<String, Experimenter> getPastConnections(long shareId) {
        throw new UnsupportedOperationException();
    }

    @RolesAllowed("user")
    public void invalidateConnection(long shareId, Experimenter exp) {
        throw new UnsupportedOperationException();
    }

    // Helpers
    // =========================================================================

    protected Share _share(final long shareId) {
        Share s = (Share) executor.execute(null, new Executor.SimpleWork(this,
                "idToUuid") {
            @Transactional(readOnly = true)
            public Object doWork(org.hibernate.Session session,
                    ServiceFactory sf) {
                return sf.getQueryService().get(Share.class, shareId);
            }
        });
        return s;
    }

    protected String idToUuid(final long shareId) {
        Session share = getShare(shareId);
        return share.getUuid();
    }

    protected List<Experimenter> loadMembers(ShareData data) {
        List<Experimenter> members = new ArrayList<Experimenter>();
        if (data.members.size() > 0)
            members = findAllByQuery("select e from Experimenter e "
                    + "where e.id in (:ids)", new Parameters()
                    .addIds(data.members), "loadMembers");
        return members;
    }

    /**
     * Convert a {@link Timestamp expiration} into a long which can be set on
     * {@link Session#setTimeToLive(Long)}.
     * 
     * @return the time in milliseconds that this session can exist.
     */
    protected long expirationAsLong(Timestamp expiration) {
        long now = System.currentTimeMillis();
        long time;
        if (expiration != null) {
            time = expiration.getTime();
            if (time < now) {
                throw new ApiUsageException(
                        "Expiration time must be in the future.");
            }
        } else {
            time = Long.MAX_VALUE;
        }

        return time - now;
    }

    protected Set<Session> sharesToSessions(List<ShareData> datas) {
        /*
         * TODO: When Share will have details method can be updated: +
         * "join fetch sh.details.owner where sh.id in (:ids) ",
         */
        /*
         * Set<Session> sessions = new HashSet<Session>(); for (ShareData data :
         * datas) { sessions.add(shareToSession(data)); } return sessions;
         */
        Set<Long> ids = new HashSet<Long>();
        for (ShareData data : datas) {
            ids.add(data.id);
        }
        Set<Session> sessions = new HashSet<Session>();
        if (ids.size() > 0) {
            List<Session> list = findAllByQuery(
                    "select sh from Session sh where sh.id in (:ids) ",
                    new Parameters().addIds(ids), "shareToSessions");
            sessions = new HashSet<Session>(list);
        }
        return sessions;
    }

    protected Session shareToSession(ShareData data) {
        List<Session> rv = findAllByQuery("select sh from Session sh "
                + "join fetch sh.owner where sh.id = :id ", new Parameters()
                .addId(data.id), "shareToSession");
        if (rv.size() != 1) {
            throw new ValidationException("Cannot convert data to share");
        }

        return rv.get(0);
    }

    /**
     * Can't be used from within another executor call.
     */
    @SuppressWarnings("unchecked")
    protected <T extends IObject> List<T> findAllByQuery(final String q,
            final Parameters p, String log) {
        return (List<T>) executor.execute(null, new Executor.SimpleWork(this,
                log) {
            @Transactional(readOnly = true)
            public Object doWork(org.hibernate.Session session,
                    ServiceFactory sf) {
                return sf.getQueryService().findAllByQuery(q, p);
            }
        });
    }

    @SuppressWarnings("unchecked")
    protected <T extends IObject> Map<Class<T>, List<Long>> map(
            Map<String, List<Long>> map) {
        Map<Class<T>, List<Long>> rv = new HashMap<Class<T>, List<Long>>();
        for (String key : map.keySet()) {
            try {
                Class<T> kls = (Class<T>) Class.forName(key);
                rv.put(kls, map.get(key));
            } catch (Exception e) {
                throw new ValidationException("Share contains invalid type: "
                        + key);
            }
        }
        return rv;
    }

    @SuppressWarnings("unchecked")
    protected <T extends IObject> List<T> list(List<Obj> objectList) {
        List<T> rv = new ArrayList<T>();
        for (Obj o : objectList) {
            T t;
            try {
                t = (T) Class.forName(o.type).newInstance();
            } catch (Exception e) {
                throw new ValidationException("Share contains invalid type: "
                        + o.type);
            }
            t.setId(o.id);
            t.unload();
            rv.add(t);
        }
        return rv;
    }

    protected void throwOnNullData(long shareId, ShareData data) {
        if (data == null) {
            throw new ValidationException("Share not found: " + shareId);
        }
    }

    protected ShareData getShareIfAccessibble(long shareId) {
        ShareData data = _retrieve(shareId);
        if (data == null) {
            return null;
        }

        EventContext ec = details.getCurrentEventContext();
        boolean isAdmin = ec.isCurrentUserAdmin();
        long userId = ec.getCurrentUserId();
        if (data.owner == userId || data.members.contains(userId) || isAdmin) {
            return data;
        }
        return null;
    }

    protected void _addGraph(ShareData data, Graph g) {
        for (IObject object : g.objects()) {
            List<Long> ids = data.objectMap.get(object.getClass().getName());
            if (ids == null) {
                ids = new ArrayList<Long>();
                data.objectMap.put(object.getClass().getName(), ids);
            }
            if (!ids.contains(object.getId())) {
                ids.add(object.getId());
                Obj obj = new Obj();
                obj.type = object.getClass().getName();
                obj.id = object.getId();
                data.objectList.add(obj);
            }
        }
    }

    protected void _store(final long shareId, final ShareData data) {
        executor.execute(null, new Executor.SimpleWork(this, "_store") {
            @Transactional(readOnly = false)
            public Object doWork(org.hibernate.Session session,
                    ServiceFactory sf) {
                Share share = sf.getQueryService().get(Share.class, shareId);
                secSys.doAction(new SecureStore(data), share);
                adminFlush(sf);
                return null;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private List<ShareData> _getShares(final boolean active) {
        return (List<ShareData>) executor.execute(null,
                new Executor.SimpleWork(this, "_getShares(bool)") {
                    @Transactional(readOnly = true)
                    public Object doWork(org.hibernate.Session session,
                            ServiceFactory sf) {
                        return store.getShares(active);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private List<ShareData> _getShares(final long id, final boolean own,
            final boolean active) {
        return (List<ShareData>) executor.execute(null,
                new Executor.SimpleWork(this, "_getShares") {
                    @Transactional(readOnly = true)
                    public Object doWork(org.hibernate.Session session,
                            ServiceFactory sf) {
                        return store.getShares(id, own, active);
                    }
                });
    }

    protected ShareData _retrieve(final long shareId) {
        return (ShareData) executor.execute(null, new Executor.SimpleWork(this,
                "_retrieve") {
            @Transactional(readOnly = true)
            public Object doWork(org.hibernate.Session session,
                    ServiceFactory sf) {
                return store.get(shareId);
            }
        });
    }

    private void adminFlush(final ServiceFactory sf) {
        secSys.runAsAdmin(new AdminAction() {
            public void runAsAdmin() {
                ((LocalUpdate) sf.getUpdateService()).flush();
            }
        });
    }

    private Principal principal() {
        String sessionUuid = details.getLast().getName();
        return new Principal(sessionUuid);
    }

    private long userId() {
        return sessionManager.getEventContext(principal()).getCurrentUserId();
    }

    private String userName() {
        return sessionManager.getEventContext(principal()).getCurrentUserName();
    }

    // Graph : used for
    // =========================================================================

    private static class Graph extends ContextFilter {

        public List<IObject> objects() {
            List<IObject> rv = new ArrayList<IObject>();
            for (Object o : _cache.keySet()) {
                if (o instanceof IObject) {
                    IObject obj = (IObject) o;
                    rv.add(obj);
                }
            }
            return rv;
        }

        @Override
        protected void doFilter(String fieldId, Filterable f) {
            if (!(f instanceof Details)) {
                super.doFilter(fieldId, f);
            }
        }

    }

}
