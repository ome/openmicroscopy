/*
 *   $Id$
 *
 *   Copyright 2008 - 2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sharing;

import java.sql.SQLException;
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
import ome.api.IShare;
import ome.api.ServiceInterface;
import ome.api.local.LocalAdmin;
import ome.api.local.LocalShare;
import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.logic.AbstractLevel2Service;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.CommentAnnotation;
import ome.model.annotations.SessionAnnotationLink;
import ome.model.internal.Details;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.model.meta.Share;
import ome.parameters.Parameters;
import ome.security.AdminAction;
import ome.security.SecureAction;
import ome.security.basic.BasicSecuritySystem;
import ome.security.basic.CurrentDetails;
import ome.services.mail.MailUtil;
import ome.services.sessions.SessionContext;
import ome.services.sessions.SessionManager;
import ome.services.sharing.data.Obj;
import ome.services.sharing.data.ShareData;
import ome.services.util.Executor;
import ome.services.util.ServiceHandler;
import ome.system.EventContext;
import ome.system.Principal;
import ome.tools.hibernate.QueryBuilder;
import ome.util.ContextFilter;
import ome.util.Filterable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.mail.MailException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

/**
 * Note: {@link SessionManager} should not be used to obtain the {@link Share}
 * data since it may not be completely in sync. i.e. Don't use SM.find()
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see IShare
 */
@Transactional(readOnly = true)
public class ShareBean extends AbstractLevel2Service implements LocalShare {

    public final static Logger log = LoggerFactory.getLogger(ShareBean.class);

    public final static String NS_ENABLED = "ome.share.enabled";

    public final static String NS_COMMENT = "ome.share.comment/";

    final protected LocalAdmin admin;

    final protected SessionManager sessionManager;

    final protected ShareStore store;

    final protected Executor executor;

    final protected MailUtil mailUtil;

    public final Class<? extends ServiceInterface> getServiceInterface() {
        return IShare.class;
    }

    public ShareBean(LocalAdmin admin, SessionManager mgr, ShareStore store,
            Executor executor, MailUtil mailUtil) {
        this.admin = admin;
        this.sessionManager = mgr;
        this.store = store;
        this.executor = executor;
        this.mailUtil = mailUtil;
    }

    // ~ Service Methods
    // ===================================================

    @RolesAllowed("user")
    public void activate(long shareId) {

        // Check status of the store
        ShareData data = getShareIfAccessible(shareId);
        if (data == null) {
            throw new ValidationException("No accessible share:" + shareId);
        }
        if (!data.enabled) {
            throw new ValidationException("Share disabled.");
        }

        // Ok, set share
        setShareId(shareId);
    }

    @RolesAllowed("user")
    public void deactivate() {
        setShareId(null);
    }

    /**
     * Set the share id on the current session context.
     *
     * Previously this method was used throughout the code base in order to
     * "open up" a session. This however, has issues since it can lead to data
     * leakage (8037). Using the omero.group functionality (3529), this method
     * no longer needs to be public.
     *
     * @see ticket:2219
     * @see ticket:3529
     * @see ticket:8037
     */
    private Long setShareId(Long shareId) {
        String sessId = getSecuritySystem().getEventContext().getCurrentSessionUuid();
        SessionContext sc = (SessionContext) sessionManager
                .getEventContext(new Principal(sessId));
        Long old = sc.getCurrentShareId();
        sc.setShareId(shareId);
        return old;
    }


    /**
     * @see ticket:2219
     */
    public void resetReadFilter(org.hibernate.Session s) {
        // ticket:2397 and ticket:2219
        // Necessary to update the current filter in order to
        // have the new shareId be updated
        BasicSecuritySystem bss = (BasicSecuritySystem) sec;
        bss.loadEventContext(true);
        bss.updateReadFilter(s);
    }

    // ~ Getting shares and objects (READ)
    // =========================================================================

    @RolesAllowed("user")
    public Map<Long, Long> getMemberCount(final Set<Long> shareIds) {

        if (shareIds == null || shareIds.size() == 0) {
            throw new ApiUsageException("Nothing to do");
        }

        final QueryBuilder qb = new QueryBuilder();
        qb.select("share2.id", "count(distinct links2.id)");
        qb.from("ShareMember", "links2");
        qb.join("links2.parent","share2", false, false);
        qb.where();
        qb.paramList("ids", shareIds);
        qb.and("share2.id in (:ids) and share2.id in ");
        // -- subselect for all accessible shares
        {
            QueryBuilder sub = new QueryBuilder();
            sub.select("share");
            sub.from("ShareMember", "memberLinks");
            sub.join("memberLinks.parent", "share", false, false);
            sub.join("memberLinks.child", "user", false, false);
            sub.where();
            sub.and("1=1"); // WORKAROUND for ticket:1239 for root
            applyIfShareAccessible(sub);
            qb.subselect(sub);
        }
        // -- end subselect
        qb.append("group by share2.id");

        final Map<Long, Long> rv = new HashMap<Long, Long>(shareIds.size());
        sec.runAsAdmin(new AdminAction(){
            public void runAsAdmin() {
                iQuery.execute(new HibernateCallback<Object>() {
                    public Object doInHibernate(org.hibernate.Session s)
                        throws HibernateException, SQLException {
                        Query q = qb.query(s);
                        List<Object[]> results = q.list();
                        if (results.size() != shareIds.size()) {
                            throw new ValidationException(
                                    "Missing or protected shares specified");
                        }
                        for (Object[] values : results) {
                            Long shareId = (Long) values[0];
                            Long count = (Long) values[1];
                            rv.put(shareId, count);
                        }
                        return null;
                    }});
            }});
        return rv;
    }

    long getCurrentUserId() {
        return getSecuritySystem().getEventContext().getCurrentUserId();
    }

    @RolesAllowed("user")
    public Set<Session> getOwnShares(boolean active) {
        long id = getCurrentUserId();
        List<ShareData> shares = store.getShares(id, true /* own */, active);
        return sharesToSessions(shares);
    }

    @RolesAllowed("user")
    public Set<Session> getMemberShares(boolean active) {
        long id = getCurrentUserId();
        List<ShareData> shares = store.getShares(id, false /* own */, active);
        return sharesToSessions(shares);
    }

    @RolesAllowed("user")
    public Set<Session> getSharesOwnedBy(@NotNull Experimenter user,
            boolean active) {
        List<ShareData> shares = store.getShares(user.getId(), true /* own */,
                active);
        return sharesToSessions(shares);
    }

    @RolesAllowed("user")
    public Set<Session> getMemberSharesFor(@NotNull Experimenter user,
            boolean active) {
        List<ShareData> shares = store.getShares(user.getId(), false /* own */,
                active);
        return sharesToSessions(shares);
    }

    @RolesAllowed("user")
    public Share getShare(long sessionId) {
        Share session = null;
        ShareData data = getShareIfAccessible(sessionId);
        if (data != null) {
            session = shareToSession(data);
        }
        return session;
    }

    @RolesAllowed("user")
    public <T extends IObject> List<T> getContents(long shareId) {
        ShareData data = getShareIfAccessible(shareId);
        throwOnNullData(shareId, data);
        return list(data.objectList);
    }

    @RolesAllowed("user")
    public <T extends IObject> List<T> getContentSubList(long shareId,
            int start, int finish) {
        ShareData data = getShareIfAccessible(shareId);
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
        ShareData data = getShareIfAccessible(shareId);
        throwOnNullData(shareId, data);
        return map(data.objectMap);
    }

    @RolesAllowed("user")
    public int getContentSize(long shareId) {
        ShareData data = getShareIfAccessible(shareId);
        throwOnNullData(shareId, data);
        return data.objectList.size();
    }

    // ~ Creating share (WRITE)
    // =========================================================================

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public <T extends IObject> long createShare(
            @NotNull final String description, Timestamp expiration,
            List<T> items, List<Experimenter> exps, List<String> guests,
            final boolean enabled) {

        //
        // Input validation
        //
        final long time = expirationAsLong(System.currentTimeMillis(),
                expiration);

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
        final EventContext ec = getSecuritySystem().getEventContext();
        final String omename = ec.getCurrentUserName();
        final Long user = ec.getCurrentUserId();
        final Long group = ec.getCurrentGroupId();
        final Future<Share> future = executor.submit(new Callable<Share>() {
            public Share call() throws Exception {
                return sessionManager.createShare(new Principal(omename),
                        enabled, time, "SHARE", description, group);
            }
        });

        final List<T> _items = items;
        final List<String> _guests = guests;
        final List<Long> _users = new ArrayList<Long>(exps.size());
        for (Experimenter e : exps) {
            _users.add(e.getId());
        }

        final long shareId = executor.get(future).getId();
        final Share share = iQuery.find(Share.class, shareId); // Reload!
        this.sec.doAction(new SecureShare() {
            @Override
            void doUpdate(Share share) {
                store.set(share, user, _items, _users, _guests, enabled);
            }
        }, share);
        adminFlush();
        return share.getId();
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void setDescription(long shareId, String description) {
        Share share = (Share) iQuery.find(Share.class, shareId);
        ShareData data = store.get(shareId);
        share.setMessage(description);
        storeShareData(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void setExpiration(long shareId, Timestamp expiration) {
        Share share = (Share) iQuery.find(Share.class, shareId);
        ShareData data = store.get(shareId);
        share.setTimeToLive(expirationAsLong(share.getStarted().getTime(),
                expiration));
        storeShareData(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void setActive(long shareId, boolean active) {
        ShareData data = getShareIfAccessible(shareId);
        throwOnNullData(shareId, data);
        data.enabled = active;
        storeShareData(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void closeShare(long shareId) {
        final String uuid = idToUuid(shareId);
        Future<Object> future = executor.submit(new Callable<Object>() {
            public Object call() throws Exception {
                sessionManager.close(uuid);
                return null;
            }
        });
        executor.get(future);
    }

    // ~ Getting items
    // =========================================================================

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public <T extends IObject> void addObjects(long shareId,
            @NotNull T... objects) {
        ShareData data = getShareIfAccessible(shareId);
        throwOnNullData(shareId, data);
        Graph graph = new Graph();
        for (T object : objects) {
            graph.filter("top", object);
        }
        _addGraph(data, graph);
        storeShareData(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public <T extends IObject> void addObject(long shareId, @NotNull T object) {
        ShareData data = getShareIfAccessible(shareId);
        throwOnNullData(shareId, data);
        Graph graph = new Graph();
        graph.filter("top", object);
        _addGraph(data, graph);
        storeShareData(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public <T extends IObject> void removeObjects(long shareId,
            @NotNull T... objects) {
        ShareData data = getShareIfAccessible(shareId);
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
        storeShareData(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public <T extends IObject> void removeObject(long shareId, @NotNull T object) {
        ShareData data = getShareIfAccessible(shareId);
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
        storeShareData(shareId, data);
    }

    // ~ Getting comments
    // =========================================================================

    @RolesAllowed("user")
    public Map<Long, Long> getCommentCount(final Set<Long> ids) {

        if (ids == null || ids.size() == 0) {
            throw new ApiUsageException("Nothing to do");
        }

        final QueryBuilder qb = new QueryBuilder();
        qb.select("share.id","count(distinct sal)");
        qb.from("ShareMember", "sm");
        qb.join("sm.parent", "share", false, false);
        qb.join("share.annotationLinks","sal", false, false);
        qb.join("sal.child", "comment", false, false);
        qb.join("sm.child", "user", false, false);
        qb.where();
        qb.and("share.id in (:ids)");
        qb.paramList("ids", ids);
        qb.and("comment.ns like :ns");
        qb.param("ns", NS_COMMENT + "%");
        applyIfShareAccessible(qb);
        qb.append("group by share.id");

        final Map<Long, Long> rv = new HashMap<Long, Long>(ids.size());
        final Long old = setShareId(-1L); // Allow everything. ticket:2219
        try {


            iQuery.execute(new HibernateCallback<Object>() {
                public Object doInHibernate(org.hibernate.Session s)
                    throws HibernateException, SQLException {

                    resetReadFilter(s);

                    Query q = qb.query(s);
                    List<Object[]> counts = q.list();
                    // ticket:1227 - Returning 0 if missing
                    // if (counts.size() != ids.size()) {
                    //    throw new ValidationException(
                    //    "Missing or protected shares specified");
                    //}
                    for (Object[] values : counts) {
                        Long shareId = (Long) values[0];
                        Long count = (Long) values[1];
                        rv.put(shareId, count);
                    }
                    return null;
                }
            });
        } finally {
            setShareId(old);
        }
        for (Long id : ids) {
            Long value = rv.get(id);
            if (value == null) {
                rv.put(id, 0L);
            }
        }
        return rv;
    }

    @RolesAllowed("user")
    @SuppressWarnings("unchecked")
    public List<Annotation> getComments(final long shareId) {

        final List<Annotation> rv = new ArrayList<Annotation>();
        if (getShareIfAccessible(shareId) == null) {
            return rv; // EARLY EXIT.
        }

        // Now load the comments with the read filter,
        // otherwise it is necessary to add every link
        // to the share
        Long oldShareId = setShareId(-1L);
        try {
            List<SessionAnnotationLink> links =
                (List<SessionAnnotationLink>) iQuery.execute(new HibernateCallback<Object>(){
                public Object doInHibernate(org.hibernate.Session arg0)
                        throws HibernateException, SQLException {

                    resetReadFilter(arg0);
                    return arg0.createQuery(
                            "select l from SessionAnnotationLink l "
                                    + "join fetch l.details.owner "
                                    + "join fetch l.parent as share "
                                    + "join fetch l.child as comment "
                                    + "join fetch comment.details.updateEvent "
                                    + "where share.id = :id and comment.ns like :ns ")
                        .setParameter("ns", NS_COMMENT + "%")
                        .setParameter("id", shareId)
                        .list();

                }});

            for (SessionAnnotationLink link : links) {
                rv.add(link.child());
            }

        } finally {
            setShareId(oldShareId);
        }

        return rv;
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public CommentAnnotation addComment(final long shareId,
            @NotNull final String commentText) {
        getShareIfAccessible(shareId);
        ExperimenterGroup group = iQuery.get(Share.class, shareId).getGroup();
        final CommentAnnotation[] rv = new CommentAnnotation[1];

        sec.runAsAdmin(group, new AdminAction(){
            public void runAsAdmin() {
                final Share share = iQuery.get(Share.class, shareId);
                CommentAnnotation comment = new CommentAnnotation();
                comment.setTextValue(commentText);
                comment.setNs(NS_COMMENT);
                share.linkAnnotation(comment);
                iUpdate.flush();
                rv[0] = iQuery.get(CommentAnnotation.class, comment.getId());
            }});

        return rv[0];
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public CommentAnnotation addReply(long shareId, @NotNull String comment,
            @NotNull CommentAnnotation replyTo) {
        throw new UnsupportedOperationException();
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void deleteComment(@NotNull Annotation comment) {
        List<SessionAnnotationLink> links = iQuery.findAllByQuery(
                "select l from SessionAnnotationLink l "
                        + "where l.child.id = :id", new Parameters()
                        .addId(comment.getId()));
        for (SessionAnnotationLink sessionAnnotationLink : links) {
            iUpdate.deleteObject(sessionAnnotationLink);
        }
        iUpdate.deleteObject(comment);
    }

    // ~ Member administration
    // =========================================================================

    @RolesAllowed("user")
    public Set<Experimenter> getAllMembers(long shareId) {
        ShareData data = getShareIfAccessible(shareId);
        throwOnNullData(shareId, data);
        List<Experimenter> e = loadMembers(data);
        return new HashSet<Experimenter>(e);
    }

    @RolesAllowed("user")
    public Set<String> getAllGuests(long shareId) {
        ShareData data = getShareIfAccessible(shareId);
        throwOnNullData(shareId, data);
        return new HashSet<String>(data.guests);
    }

    @RolesAllowed("user")
    public Set<String> getAllUsers(long shareId) throws ValidationException {
        ShareData data = getShareIfAccessible(shareId);
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
    @Transactional(readOnly = false)
    public void addUsers(long shareId, Experimenter... exps) {
        List<Experimenter> es = Arrays.asList(exps);
        ShareData data = getShareIfAccessible(shareId);
        throwOnNullData(shareId, data);
        for (Experimenter experimenter : es) {
            data.members.add(experimenter.getId());
        }
        storeShareData(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void addGuests(long shareId, String... emailAddresses) {
        List<String> addresses = Arrays.asList(emailAddresses);
        ShareData data = getShareIfAccessible(shareId);
        throwOnNullData(shareId, data);
        data.guests.addAll(addresses);
        storeShareData(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void removeUsers(long shareId, List<Experimenter> exps) {
        ShareData data = getShareIfAccessible(shareId);
        throwOnNullData(shareId, data);
        for (Experimenter experimenter : exps) {
            data.members.remove(experimenter.getId());
        }
        storeShareData(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void removeGuests(long shareId, String... emailAddresses) {
        List<String> addresses = Arrays.asList(emailAddresses);
        ShareData data = getShareIfAccessible(shareId);
        data.guests.removeAll(addresses);
        storeShareData(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void addUser(long shareId, Experimenter exp) {
        ShareData data = getShareIfAccessible(shareId);
        throwOnNullData(shareId, data);
        data.members.add(exp.getId());
        storeShareData(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void addGuest(long shareId, String emailAddress) {
        ShareData data = getShareIfAccessible(shareId);
        throwOnNullData(shareId, data);
        data.guests.add(emailAddress);
        storeShareData(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void removeUser(long shareId, Experimenter exp) {
        ShareData data = getShareIfAccessible(shareId);
        throwOnNullData(shareId, data);
        data.members.remove(exp.getId());
        storeShareData(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void removeGuest(long shareId, String emailAddress) {
        ShareData data = getShareIfAccessible(shareId);
        throwOnNullData(shareId, data);
        data.guests.remove(emailAddress);
        storeShareData(shareId, data);
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

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void notifyMembersOfShare(long shareId, String subject, String message,
            boolean html) {

        EventContext ec = getSecuritySystem().getEventContext();

        Set<Experimenter> exps = getAllMembers(shareId);
        exps.add(getShare(shareId).getOwner());

        Map<Experimenter, String> errors = new HashMap<Experimenter, String>();
        for (final Experimenter e : exps) {
            if (e.getId() != ec.getCurrentUserId() && e.getEmail() != null
                    && mailUtil.validateEmail(e.getEmail())) {
                try {
                    mailUtil.sendEmail(e.getEmail(), subject, message, html,
                            null, null);
                } catch (MailException me) {
                    errors.put(e, me.getMessage());
                }
            }
        }
        if (!errors.isEmpty()) {
            log.error(ServiceHandler.getResultsString(errors, null));
        }
    }

    // Helpers
    // =========================================================================

    protected String idToUuid(long shareId) {
        Session s = iQuery.get(Session.class, shareId);
        return s.getUuid();
    }

    protected List<Experimenter> loadMembers(ShareData data) {
        List<Experimenter> members = new ArrayList<Experimenter>();
        if (data.members.size() > 0)
            members = iQuery.findAllByQuery("select e from Experimenter e "
                    + "where e.id in (:ids)", new Parameters()
                    .addIds(data.members));
        return members;
    }

    /**
     * Convert a {@link Timestamp expiration} into a long which can be set on
     * {@link Session#setTimeToLive(Long)}.
     *
     * @return the time in milliseconds that this session can exist.
     */
    public static long expirationAsLong(long started, Timestamp expiration) {
        long time;
        if (expiration != null) {
            time = expiration.getTime();
            if (time < System.currentTimeMillis()) {
                throw new ApiUsageException(
                        "Expiration time must be in the future.");
            }
        } else {
            time = Long.MAX_VALUE;
        }

        return time - started;
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
        final Set<Long> ids = new HashSet<Long>();
        for (ShareData data : datas) {
            ids.add(data.id);
        }
        if (ids.size() == 0) {
            return Collections.emptySet();
        }

        List<Session> list = iQuery.execute(new HibernateCallback<Object>() {
            public Object doInHibernate(org.hibernate.Session arg0)
                    throws HibernateException, SQLException {
                BasicSecuritySystem bss = (BasicSecuritySystem) sec;
                try {
                    bss.disableReadFilter(arg0);
                    return arg0
                            .createQuery(
                                    "select sh from Session sh "
                                            + "join fetch sh.owner "
                                            + "where sh.id in (:ids) ")
                            .setParameterList("ids", ids).list();
                } finally {
                    bss.enableReadFilter(arg0);
                }
            }
        });
        for (Session session : list) {
            if (session != null) {
                session.putAt("#2733", "ALLOW");
            }
        }
        return new HashSet<Session>(list);
    }

    protected Share shareToSession(final ShareData data) {
        Share share = iQuery.execute(new HibernateCallback<Object>() {
            public Object doInHibernate(org.hibernate.Session arg0)
                    throws HibernateException, SQLException {
                BasicSecuritySystem bss = (BasicSecuritySystem) sec;
                try {
                    bss.disableReadFilter(arg0);
                    return arg0
                            .createQuery(
                                    "select sh from Share sh "
                                            + "join fetch sh.owner "
                                            + "where sh.id = :id")
                            .setParameter("id", data.id).uniqueResult();
                } finally {
                    bss.enableReadFilter(arg0);
                }
            }
        });

        if (share != null) {
            share.putAt("#2733", "ALLOW");
        }
        return share;
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

    protected void adminFlush() {
        getSecuritySystem().runAsAdmin(new AdminAction() {
            public void runAsAdmin() {
                iUpdate.flush();
            }
        });
    }

    protected void throwOnNullData(long shareId, ShareData data) {
        if (data == null) {
            throw new ValidationException("Share not found: " + shareId);
        }
    }

    /**
     * If the current user is not an admin, then this methods adds a subclause
     * to the HQL:
     *
     *   AND ( share.owner.id = :userId or user.id = :userId )
     *
     * {@link QueryBuilder#where()} should already have been called.
     */
    protected void applyIfShareAccessible(QueryBuilder qb) {
        EventContext ec = getSecuritySystem().getEventContext();
        if ( ! ec.isCurrentUserAdmin()) {
            qb.param("userId", ec.getCurrentUserId());
            qb.and("(");
            qb.append("share.owner.id = :userId" );
            qb.append(" OR ");
            qb.append("user.id = :userId" );
            qb.append(" ) ");
        }
    }

    /**
     * Loads share and checks it's owner and member data against the current
     * context (owner/member/admin). This method must be kept in sync with
     * {@link #applyIfShareAccessible(QueryBuilder)} which does the same check
     * at the database rather than binary data level.
     */
    protected ShareData getShareIfAccessible(long shareId) {

        EventContext ec = getSecuritySystem().getEventContext();
        boolean isAdmin = ec.isCurrentUserAdmin();
        long userId = ec.getCurrentUserId();
        return store.getShareIfAccessible(shareId, isAdmin, userId);

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

    // All update access should be performed with these methods to keep
    // everything in sync. The other methods which mutate are create & close
    // =========================================================================

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

    protected void storeShareData(long shareId, ShareData data) {
        // This should reload the object already in the first-cache
        Share share = iQuery.get(Share.class, shareId);

        this.sec.doAction(new SecureStore(data), share);
        adminFlush();
    }

    /*
     * private void updateShare(final Share share) { Future<Object> future =
     * executor.submit(new Callable<Object>() { public Object call() throws
     * Exception { sessionManager.update(share, true); return null; } });
     * executor.get(future); }
     */

}
