/*
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

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import ome.annotations.NotNull;
import ome.api.IShare;
import ome.api.ServiceInterface;
import ome.api.local.LocalAdmin;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.logic.AbstractLevel2Service;
import ome.logic.SimpleLifecycle;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.SessionAnnotationLink;
import ome.model.annotations.TextAnnotation;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.Session;
import ome.model.meta.Share;
import ome.parameters.Parameters;
import ome.security.AdminAction;
import ome.security.SecureAction;
import ome.services.sessions.SessionContext;
import ome.services.sessions.SessionManager;
import ome.services.sharing.data.Obj;
import ome.services.sharing.data.ShareData;
import ome.services.util.OmeroAroundInvoke;
import ome.system.EventContext;
import ome.system.Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.RemoteBindings;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see IShare
 */
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional(readOnly = true)
@Stateless
@Remote(IShare.class)
@RemoteBindings( {
        @RemoteBinding(jndiBinding = "omero/remote/ome.api.IShare"),
        @RemoteBinding(jndiBinding = "omero/secure/ome.api.IShare", clientBindUrl = "sslsocket://0.0.0.0:3843") })
@Local(IShare.class)
@LocalBinding(jndiBinding = "omero/local/ome.api.IShare")
@Interceptors( { OmeroAroundInvoke.class, SimpleLifecycle.class })
public class ShareBean extends AbstractLevel2Service implements IShare {

    public final static Log log = LogFactory.getLog(ShareBean.class);

    public final static String NS_ENABLED = "ome.share.enabled";

    public final static String NS_COMMENT = "ome.share.comment/";

    final protected LocalAdmin admin;

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

    public ShareBean(LocalAdmin admin, SessionManager mgr, ShareStore store) {
        this.admin = admin;
        this.sessionManager = mgr;
        this.store = store;
    }

    // ~ Service Methods
    // ===================================================

    @RolesAllowed("user")
    public void activate(long shareId) {

        EventContext ec = admin.getEventContext();
        long userId = ec.getCurrentUserId();
        String sessId = ec.getCurrentSessionUuid();

        // Check status of the store
        ShareData data = store.get(shareId);
        if (data == null) {
            throw new ApiUsageException("No such share:" + shareId);
        }
        if (!data.enabled) {
            throw new SecurityViolation("Share disabled."); // TODO other
            // exception
        }

        // Check if current user is a member
        if (data.owner != userId && !data.members.contains(userId)) {
            throw new SecurityViolation("User " + userId
                    + " is not a member or owner of share " + shareId);
        }

        // Ok, set share
        SessionContext sc = (SessionContext) sessionManager
                .getEventContext(new Principal(sessId));
        sc.setShareId(shareId);
    }

    // ~ Admin
    // =========================================================================

    @RolesAllowed("system")
    public Set<Session> getAllShares(boolean active) {
        List<ShareData> shares = store.getShares(active);
        return sharesToSessions(shares);
    }

    // ~ Getting shares and objects (READ)
    // =========================================================================

    @RolesAllowed("user")
    public Set<Session> getOwnShares(boolean active) {
        long id = admin.getEventContext().getCurrentUserId();
        List<ShareData> shares = store.getShares(id, true /* own */, active);
        return sharesToSessions(shares);
    }

    @RolesAllowed("user")
    public Set<Session> getMemberShares(boolean active) {
        long id = admin.getEventContext().getCurrentUserId();
        List<ShareData> shares = store.getShares(id, false /* own */, active);
        return sharesToSessions(shares);
    }

    @RolesAllowed("user")
    public Set<Session> getSharesOwnedBy(@NotNull
    Experimenter user, boolean active) {
        List<ShareData> shares = store.getShares(user.getId(), true /* own */,
                active);
        return sharesToSessions(shares);
    }

    @RolesAllowed("user")
    public Set<Session> getMemberSharesFor(@NotNull
    Experimenter user, boolean active) {
        List<ShareData> shares = store.getShares(user.getId(),
                false /* own */, active);
        return sharesToSessions(shares);
    }

    @RolesAllowed("user")
    public Session getShare(long sessionId) {
        ShareData data = store.get(sessionId);
        Session session = shareToSession(data);
        return session;
    }

    @RolesAllowed("user")
    public <T extends IObject> List<T> getContents(long shareId) {
        ShareData data = store.get(shareId);
        return list(data.objectList);
    }

    @RolesAllowed("user")
    public <T extends IObject> List<T> getContentSubList(long shareId,
            int start, int finish) {
        ShareData data = store.get(shareId);
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
        ShareData data = store.get(shareId);
        return map(data.objectMap);
    }

    @RolesAllowed("user")
    public int getContentSize(long shareId) {
        ShareData data = store.get(shareId);
        return data.objectList.size();
    }

    // ~ Creating share (WRITE)
    // =========================================================================

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public <T extends IObject> long createShare(@NotNull
    String description, Timestamp expiration, List<T> items,
            List<Experimenter> exps, List<String> guests, final boolean enabled) {

        //
        // Input validation
        //
        long time = expirationAsLong(expiration);

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
        final String omename = this.admin.getEventContext()
                .getCurrentUserName();
        final Long user = this.admin.getEventContext().getCurrentUserId();
        Share share = sessionManager.createShare(new Principal(omename),
                enabled, time, "SHARE", description);

        final List<T> _items = items;
        final List<String> _guests = guests;
        final List<Long> _users = new ArrayList<Long>(exps.size());
        for (Experimenter e : exps) {
            _users.add(e.getId());
        }

        share = iQuery.get(Share.class, share.getId());
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
    public void setDescription(long shareId, @NotNull
    String description) {
        String uuid = idToUuid(shareId);
        Session session = sessionManager.find(uuid);
        session.setMessage(description);
        sessionManager.update(session);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void setExpiration(long shareId, @NotNull
    Timestamp expiration) {
        String uuid = idToUuid(shareId);
        Session session = sessionManager.find(uuid);
        session.setTimeToLive(expirationAsLong(expiration));
        sessionManager.update(session);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void setActive(long shareId, boolean active) {
        ShareData data = store.get(shareId);
        data.enabled = active;
        store(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void closeShare(long shareId) {
        String uuid = idToUuid(shareId);
        sessionManager.close(uuid);
    }

    // ~ Getting items
    // =========================================================================

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public <T extends IObject> void addObjects(long shareId, @NotNull
    T... objects) {
        ShareData data = store.get(shareId);
        for (T object : objects) {
            List<Long> ids = data.objectMap.get(object.getClass().getName());
            if (ids == null) {
                ids = new ArrayList<Long>();
                data.objectMap.put(object.getClass().getName(), ids);
            }
            ids.add(object.getId());
            Obj obj = new Obj();
            obj.type = object.getClass().getName();
            obj.id = object.getId();
            data.objectList.add(obj);
        }
        store(shareId, data);
    }

    private void store(long shareId, ShareData data) {
        Share share = iQuery.get(Share.class, shareId);
        this.sec.doAction(new SecureStore(data), share);
        adminFlush();
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public <T extends IObject> void addObject(long shareId, @NotNull
    T object) {
        ShareData data = store.get(shareId);
        List<Long> ids = data.objectMap.get(object.getClass().getName());
        ids.add(object.getId());
        Obj obj = new Obj();
        obj.type = object.getClass().getName();
        obj.id = object.getId();
        data.objectList.add(obj);
        store(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public <T extends IObject> void removeObjects(long shareId, @NotNull
    T... objects) {
        ShareData data = store.get(shareId);
        for (T object : objects) {
            List<Long> ids = data.objectMap.get(object.getClass().getName());
            ids.remove(object.getId());
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
        store(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public <T extends IObject> void removeObject(long shareId, @NotNull
    T object) {
        ShareData data = store.get(shareId);
        List<Long> ids = data.objectMap.get(object.getClass().getName());
        ids.remove(object.getId());
        List<Obj> toRemove = new ArrayList<Obj>();
        for (Obj obj : data.objectList) {
            if (obj.type.equals(object.getClass().getName())) {
                if (obj.id == object.getId().longValue()) {
                    toRemove.add(obj);
                }
            }
        }
        data.objectList.removeAll(toRemove);
        store(shareId, data);
    }

    // ~ Getting comments
    // =========================================================================

    @RolesAllowed("user")
    public List<Annotation> getComments(long shareId) {
        return iQuery.findAllByQuery("select comment from Session s "
                + "left outer join fetch s.annotationLinks links "
                + "left outer join fetch links.child comment "
                + "where s.id = :id and comment.ns like :ns ", new Parameters()
                .addString("ns", NS_COMMENT + "%").addId(shareId));
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public TextAnnotation addComment(long shareId, @NotNull
    String comment) {
        Session s = iQuery.get(Session.class, shareId);
        TextAnnotation commentAnnotation = new TextAnnotation();
        commentAnnotation.setTextValue(comment);
        commentAnnotation.setNs(NS_COMMENT);
        SessionAnnotationLink link = s.linkAnnotation(commentAnnotation);
        link = iUpdate.saveAndReturnObject(link);
        return (TextAnnotation) link.child();
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public TextAnnotation addReply(long shareId, @NotNull
    String comment, @NotNull
    TextAnnotation replyTo) {
        throw new UnsupportedOperationException();
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void deleteComment(@NotNull
    Annotation comment) {
        iUpdate.deleteObject(comment);
    }

    // ~ Member administration
    // =========================================================================

    @RolesAllowed("user")
    public Set<Experimenter> getAllMembers(long shareId) {
        ShareData data = store.get(shareId);
        List<Experimenter> e = loadMembers(data);
        return new HashSet<Experimenter>(e);
    }

    @RolesAllowed("user")
    public Set<String> getAllGuests(long shareId) {
        ShareData data = store.get(shareId);
        return new HashSet<String>(data.guests);
    }

    @RolesAllowed("user")
    public Set<String> getAllUsers(long shareId) throws ValidationException {
        ShareData data = store.get(shareId);
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
        ShareData data = store.get(shareId);
        for (Experimenter experimenter : es) {
            data.members.remove(experimenter.getId());
        }
        store(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void addGuests(long shareId, String... emailAddresses) {
        List<String> addresses = Arrays.asList(emailAddresses);
        ShareData data = store.get(shareId);
        data.guests.addAll(addresses);
        store(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void removeUsers(long shareId, List<Experimenter> exps) {
        ShareData data = store.get(shareId);
        for (Experimenter experimenter : exps) {
            data.members.remove(experimenter.getId());
        }
        store(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void removeGuests(long shareId, String... emailAddresses) {
        List<String> addresses = Arrays.asList(emailAddresses);
        ShareData data = store.get(shareId);
        data.guests.removeAll(addresses);
        store(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void addUser(long shareId, Experimenter exp) {
        ShareData data = store.get(shareId);
        data.members.add(exp.getId());
        store(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void addGuest(long shareId, String emailAddress) {
        ShareData data = store.get(shareId);
        data.guests.add(emailAddress);
        store(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void removeUser(long shareId, Experimenter exp) {
        ShareData data = store.get(shareId);
        data.members.remove(exp.getId());
        store(shareId, data);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void removeGuest(long shareId, String emailAddress) {
        ShareData data = store.get(shareId);
        data.guests.remove(emailAddress);
        store(shareId, data);
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

    private String idToUuid(long shareId) {
        Session s = iQuery.get(Session.class, shareId);
        return s.getUuid();
    }

    private List<Experimenter> loadMembers(ShareData data) {
        List<Experimenter> members = iQuery.findAllByQuery(
                "select e from Experimenter e " + "where e.id in (:ids)",
                new Parameters().addIds(data.members));
        return members;
    }

    /**
     * Convert a {@link Timestamp expiration} into a long which can be set on
     * {@link Session#setTimeToLive(Long)}.
     * 
     * @return the time in milliseconds that this session can exist.
     */
    private long expirationAsLong(Timestamp expiration) {
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

    private Set<Session> sharesToSessions(List<ShareData> datas) {
        Set<Session> sessions = new HashSet<Session>();
        for (ShareData data : datas) {
            sessions.add(shareToSession(data));
        }
        return sessions;
    }

    private Session shareToSession(ShareData data) {
        return iQuery.find(Share.class, data.id);
    }

    @SuppressWarnings("unchecked")
    private <T extends IObject> Map<Class<T>, List<Long>> map(
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
    private <T extends IObject> List<T> list(List<Obj> objectList) {
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

    void adminFlush() {
        getSecuritySystem().runAsAdmin(new AdminAction() {
            public void runAsAdmin() {
                iUpdate.flush();
            }
        });
    }
}
