/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sharing;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
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
import ome.conditions.ValidationException;
import ome.logic.AbstractLevel2Service;
import ome.logic.SimpleLifecycle;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.BooleanAnnotation;
import ome.model.annotations.TextAnnotation;
import ome.model.meta.Experimenter;
import ome.model.meta.Session;
import ome.security.SecuritySystemHolder;
import ome.services.sessions.SessionManager;
import ome.services.util.OmeroAroundInvoke;
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
@Transactional
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

    final LocalAdmin admin;

    final SessionManager sessionManager;

    final ShareStore store;

    final SecuritySystemHolder holder;

    public final Class<? extends ServiceInterface> getServiceInterface() {
        return IShare.class;
    }

    public ShareBean(LocalAdmin admin, SessionManager mgr, ShareStore store,
            SecuritySystemHolder holder) {
        this.admin = admin;
        this.sessionManager = mgr;
        this.store = store;
        this.holder = holder;
    }

    // ~ Service Methods
    // ===================================================

    @RolesAllowed("user")
    public void activate(long shareId) {
        holder.chooseSharing();
        throw new RuntimeException("Need to set the id somewhere. "
                + "Perhaps we create a new SharingSecuritySystem (stateful?) "
                + "attached to the session? Also need to reset the thread"
                + "when we exit? Needs to sit in the session.");
    }

    // ~ Admin
    // =========================================================================

    @RolesAllowed("system")
    public Set<Session> getAllShares(boolean active) {
        // this.iQuery.findAllByQuery();
        return null;
    }

    // ~ Getting shares and objects (READ)
    // =========================================================================

    @RolesAllowed("user")
    public Set<Session> getOwnShares(boolean active) {
        // this.iQuery.findAllByQuery();
        return null;
    }

    @RolesAllowed("user")
    public Set<Session> getMemberShares(boolean active) {
        // this.iQuery.findAllByQuery();
        return null;
    }

    @RolesAllowed("user")
    public Set<Session> getSharesOwnedBy(@NotNull
    Experimenter user, boolean active) {
        return null;
    }

    @RolesAllowed("user")
    public Set<Session> getMemberSharesFor(@NotNull
    Experimenter user, boolean active) {
        return null;
    }

    @RolesAllowed("user")
    public <T extends IObject> Map<Session, List<T>> getShare(long sessionId) {
        return null;
    }

    @RolesAllowed("user")
    public <T extends IObject> List<T> getContents(long shareId) {
        return null;
    }

    // ~ Creating share (WRITE)
    // =========================================================================

    @RolesAllowed("user")
    public <T extends IObject> long createShare(@NotNull
    String description, Timestamp expiration, List<T> items,
            List<Experimenter> exps, List<String> guests, boolean enabled) {

        //
        // Input validation
        //
        long time, now = System.currentTimeMillis();
        if (expiration != null) {
            time = expiration.getTime();
            if (time < now) {
                throw new ApiUsageException(
                        "Expiration time must be in the future.");
            }
        } else {
            time = Long.MAX_VALUE;
        }

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
        final String user = this.admin.getEventContext().getCurrentUserName();
        Session session = sessionManager.create(new Principal(user));
        session.setTimeToLive(time - now);
        session.setDefaultEventType("SHARE");
        session.setMessage(description);

        // Enabled
        BooleanAnnotation enabledTag = new BooleanAnnotation();
        enabledTag.setBoolValue(enabled);
        enabledTag.setNs(NS_ENABLED);
        session.linkAnnotation(enabledTag);

        // Updating
        session = sessionManager.update(session);

        // Storing representation
        List<Long> members = new ArrayList<Long>(exps.size());
        for (Experimenter e : exps) {
            members.add(e.getId());
        }
        store.set(session.getId(), user, items, members, guests, enabled);

        return session.getId();
    }

    @RolesAllowed("user")
    public void setDescription(long shareId, @NotNull
    String description) {
    }

    @RolesAllowed("user")
    public void setExpiration(long shareId, @NotNull
    Timestamp expiration) {
    }

    @RolesAllowed("user")
    public void closeShare(long shareId) {
    }

    // ~ Getting items
    // =========================================================================

    @RolesAllowed("user")
    public <T extends IObject> void addObjects(long shareId, @NotNull
    T... objects) {
    }

    @RolesAllowed("user")
    public <T extends IObject> void addObject(long shareId, @NotNull
    T object) {
    }

    @RolesAllowed("user")
    public <T extends IObject> void removeObjects(long shareId, @NotNull
    T... objects) {
    }

    @RolesAllowed("user")
    public <T extends IObject> void removeObject(long shareId, @NotNull
    T object) {
    }

    // ~ Getting comments
    // =========================================================================

    @RolesAllowed("user")
    public List<Annotation> getComments(long shareId) {
        return null;
    }

    @RolesAllowed("user")
    public TextAnnotation addComment(long shareId, @NotNull
    String comment) {
        return null;
    }

    @RolesAllowed("user")
    public TextAnnotation addReply(long shareId, @NotNull
    String comment, @NotNull
    TextAnnotation replyTo) {
        return null;
    }

    @RolesAllowed("user")
    public void deleteComment(@NotNull
    Annotation comment) {
    }

    // ~ Member administration
    // =========================================================================

    @RolesAllowed("user")
    public Set<Experimenter> getAllUsers(long shareId) {
        return null;
    }

    @RolesAllowed("user")
    public Set<String> getAllGuests(long shareId) {
        return null;
    }

    @RolesAllowed("user")
    public Set<String> getAllMembers(long shareId) throws ValidationException {
        return null;
    }

    @RolesAllowed("user")
    public void addUsers(long shareId, Experimenter... exps) {
    }

    @RolesAllowed("user")
    public void addGuests(long shareId, String... emailAddresses) {
    }

    @RolesAllowed("user")
    public void removeUsers(long shareId, List<Experimenter> exps) {
    }

    @RolesAllowed("user")
    public void removeGuests(long shareId, String... emailAddresses) {
    }

    @RolesAllowed("user")
    public void addUser(long shareId, Experimenter exp) {
    }

    @RolesAllowed("user")
    public void addGuest(long shareId, String emailAddress) {
    }

    @RolesAllowed("user")
    public void removeUser(long shareId, Experimenter exp) {
    }

    @RolesAllowed("user")
    public void removeGuest(long shareId, String emailAddress) {
    }

}
