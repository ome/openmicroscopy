/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ome.api.local.LocalAdmin;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.conditions.AuthenticationException;
import ome.conditions.RemovedSessionException;
import ome.conditions.SecurityViolation;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.parameters.Parameters;
import ome.services.sessions.events.UserGroupUpdateEvent;
import ome.services.sessions.state.SessionCache;
import ome.services.sessions.state.SessionCache.StaleCacheListener;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.Roles;

import org.springframework.context.ApplicationEvent;

/**
 * Is for ISession a cache and will be kept there in sync? OR Factors out the
 * logic from ISession and SessionManagerI
 * 
 * Therefore either called directly, or via synchronous messages.
 * 
 * Uses the name of a Principal as the key to the session. We may need to limit
 * user names to prevent this. (Strictly alphanumeric)
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class SessionManagerImpl implements SessionManager, StaleCacheListener {

    private final Set<String> ids = new HashSet<String>();

    // Injected
    Roles roles;
    LocalAdmin admin;
    LocalQuery query;
    LocalUpdate update;
    SessionCache cache;
    Executor executor;
    Principal asroot;

    // ~ Injectors
    // =========================================================================

    public void setAdminService(LocalAdmin adminService) {
        admin = adminService;
    }

    public void setQueryService(LocalQuery queryService) {
        query = queryService;
    }

    public void setUpdateService(LocalUpdate updateService) {
        update = updateService;
    }

    public void setSessionCache(SessionCache sessionCache) {
        cache = sessionCache;
        this.cache.addStaleCacheListener(this);
    }

    public void setRoles(Roles securityRoles) {
        roles = securityRoles;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void setPrincipal(Principal principal) {
        this.asroot = principal;
    }

    // ~ Session management
    // =========================================================================

    /*
     * Is given trustable values by the {@link SessionBean}
     */
    public Session create(final Principal _principal, final String credentials) {

        if (!admin.checkPassword(_principal.getName(), credentials)) {
            throw new AuthenticationException("Authentication exception.");
        }

        // authentication checked. Now delegating to the admin method (no pass)
        return create(_principal);
    }

    public Session create(Principal _principal) {

        if (_principal == null || _principal.getName() == null) {
            throw new ApiUsageException(
                    "Principal and user name cannot be null");
        }

        Principal principal = checkPrincipalNameAndDefaultGroup(_principal);

        Session session = new Session();
        SessionContext ctx = currentDatabaseShapshot(principal, session);
        session.setUuid(UUID.randomUUID().toString());
        session.setStarted(new Timestamp(System.currentTimeMillis()));
        update.saveObject(session);
        cache.putSession(session.getUuid(), ctx);

        return session;
    }

    public Session update(Session session) {

        if (session == null || session.getUuid() == null) {
            throw new RemovedSessionException("Cannot update; No uuid.");
        }

        SessionContext ctx = cache.getSessionContext(session.getUuid());
        if (ctx == null) {
            throw new RemovedSessionException(
                    "Can't update; No session with uuid:" + session.getUuid());
        }
        Session orig = ctx.getSession();

        // Conditiablly settable;
        // will be checked by checkPrincipalNameAndDefaultGroup
        Details proposed = session.getDetails();
        if (proposed == null) {
            proposed = orig.getDetails();
        }
        long uid = proposed.getOwner().getId();
        long gid = proposed.getGroup().getId();
        Principal principal = checkPrincipalNameAndDefaultGroup(new Principal(
                admin.userProxy(uid).getOmeName(), admin.groupProxy(gid)
                        .getName(), null));
        if (session.getDetails() != null) {
            Details proposedDetails = session.getDetails();
            ExperimenterGroup proposedGroup = proposedDetails.getGroup();
        }

        // Unconditionally settable; these are open to the user for change
        parseAndSetDefaultType(session.getDefaultEventType(), orig);
        parseAndSetDefaultPermissions(session.getDefaultPermissions(), orig);
        orig.setUserAgent(session.getUserAgent());

        // Need to handle notifications

        ctx = currentDatabaseShapshot(principal, orig);
        update.saveObject(orig);
        cache.putSession(orig.getUuid(), ctx);

        return session;

    }

    protected SessionContext currentDatabaseShapshot(Principal principal,
            Session session) {
        // Do lookups
        final Experimenter exp = admin.userProxy(principal.getName());
        final ExperimenterGroup grp = admin.groupProxy(principal.getGroup());
        final List<Long> memberOfGroupsIds = admin.getMemberOfGroupIds(exp);
        final List<Long> leaderOfGroupsIds = admin.getLeaderOfGroupIds(exp);
        final List<String> userRoles = admin.getUserRoles(exp);

        parseAndSetDefaultType(principal.getEventType(), session);
        parseAndSetDefaultPermissions(principal.getUmask(), session);

        session.getDetails().setOwner(exp);
        session.getDetails().setGroup(grp);

        SessionContext sessionContext = new SessionContextImpl(session,
                leaderOfGroupsIds, memberOfGroupsIds, userRoles);
        return sessionContext;

    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.server.utests.sessions.SessionManager#getSession(java.lang.String)
     */
    public Session find(String uuid) {
        SessionContext sessionContext = cache.getSessionContext(uuid);
        return (sessionContext == null) ? null : sessionContext.getSession();
    }

    /*
     */
    public void close(String uuid) {

        SessionContext ctx = cache.getSessionContext(uuid);
        if (ctx == null) {
            return;
        }

        // TODO this is not safe
        Session s = ctx.getSession();
        s.setClosed(new Timestamp(System.currentTimeMillis()));
        update(s);
        cache.removeSession(uuid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.services.sessions.SessionManager#getUserRoles(String)
     */
    public List<String> getUserRoles(String uuid) {
        SessionContext ctx = cache.getSessionContext(uuid);
        if (ctx == null) {
            return Collections.emptyList();
        }
        return ctx.getUserRoles();
    }

    // ~ Security methods
    // =========================================================================

    public void assertSession(String uuid) throws SecurityViolation {
        if (find(uuid) == null) {
            throw new SecurityViolation("No session with uuid: " + uuid);
        }
    }

    public EventContext getEventContext(Principal principal) {
        final Session session = find(principal.getName());
        if (session == null) {
            return null; // EARLY EXIT.
        }
        throw new UnsupportedOperationException("CHECK FOR NULL; NYI");
        // return sessions.get(uuid);
        // null // we must check here if the Principal matches and update the
        // group/event
    }

    // ~ Notifications
    // =========================================================================

    public String[] notifications(String sessionId) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.server.utests.sessions.SessionManager#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationEvent event) {

        if (event instanceof UserGroupUpdateEvent) {
            cache.setNeedsUpdate(true);
        }

        // TODO
        // if is a session event or admin event (user change, etc.) act.
        // send to all notifications.
    }

    // ~ Callbacks (Registering session-based components)
    // =========================================================================

    public void addCallback(String sessionId, SessionCallback cb) {

    }

    public Object getCallbackObject(String sessionId, String name) {
        return null;
    }

    // ~ Misc
    // =========================================================================

    /**
     * Checks the validity of the given {@link Principal}, and in the case of
     * an error attempts to correct the problem by returning a new Principal.
     */
    private Principal checkPrincipalNameAndDefaultGroup(Principal p) {

        if (p == null || p.getName() == null) {
            throw new ApiUsageException("Null principal name.");
        }

        // Null or bad event type values as well as umasks are handled
        // within the SessionManager and EventHandler. It is necessary
        String group = p.getGroup();
        if (group == null) {
            group = "user";
        }

        // ticket:404 -- preventing users from logging into "user" group
        else if (roles.getUserGroupName().equals(p.getGroup())) {
            List<ExperimenterGroup> groups = query.findAllByQuery(
                    "select g from ExperimenterGroup g "
                            + "join g.groupExperimenterMap as m "
                            + "join m.child as u "
                            + "where g.name  != :userGroup and "
                            + "u.omeName = :userName and "
                            + "m.defaultGroupLink = true", new Parameters()
                            .addString("userGroup", roles.getUserGroupName())
                            .addString("userName", p.getName()));

            if (groups.size() != 1) {
                throw new SecurityViolation(
                        String
                                .format(
                                        "User %s attempted to login to user group \"%s\". When "
                                                + "doing so, there must be EXACTLY one default group for "
                                                + "that user and not %d", p
                                                .getName(), roles
                                                .getUserGroupName(), groups
                                                .size()));
            }
            group = groups.get(0).getName();
        }
        return new Principal(p.getName(), group, p.getEventType());
    }

    private void parseAndSetDefaultPermissions(Permissions perms,
            Session session) {
        Permissions _perm = (perms == null) ? Permissions.DEFAULT : perms;
        parseAndSetDefaultPermissions(_perm.toString(), session);
    }

    private void parseAndSetDefaultPermissions(String perms, Session session) {
        String _perm = (perms == null) ? Permissions.DEFAULT.toString() : perms
                .toString();
        session.setDefaultPermissions(_perm);
    }

    private void parseAndSetDefaultType(String type, Session session) {
        String _type = (type == null) ? "User" : type;
        session.setDefaultEventType(_type);
    }

    protected void copy(Session source, Session target) {
        if (source == null || target == null) {
            throw new ApiUsageException("Source and target may not be null.");
        }

        target.setId(source.getId());
        target.setClosed(source.getClosed());
        target.setDefaultEventType(source.getDefaultEventType());
        target.setDefaultPermissions(source.getDefaultPermissions());
        target.getDetails().shallowCopy(source.getDetails());
        target.setStarted(source.getStarted());
        target.setUserAgent(source.getUserAgent());
        target.setUuid(source.getUuid());
    }

    // StaleCacheListener
    // =========================================================================

    /**
     * Will be called in a synchronized block by {@link SessionCache} in order
     * to allow for an update.
     */
    public boolean attemptCacheUpdate() {
        for (String key : cache.getIds()) {
            SessionContext ctx = cache.getSessionContext(key);
            if (ctx != null) {
                cache.removeSession(key);
            }
            Session s = new Session();
            s.setUuid(ctx.getSession().getUuid());
            this.update(s);
        }
        return true;
    }

}
