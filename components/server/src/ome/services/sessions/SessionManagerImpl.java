/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions;

import java.sql.Timestamp;
import java.util.List;

import ome.api.local.LocalAdmin;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.SecurityViolation;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.parameters.Parameters;
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
public class SessionManagerImpl implements SessionManager {

    

    public final static Permissions INVISIBLE_MASK;
    static {
        Permissions p = new Permissions(Permissions.PUBLIC);
        p.revoke(Permissions.Role.USER, Permissions.Right.READ);
        INVISIBLE_MASK = Permissions.immutable(p);
    }

    // Injected
    LocalAdmin admin;
    LocalQuery query;
    LocalUpdate update;
    SessionCache sessions;
    Roles roles;
    
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
        sessions = sessionCache;
    }

    public void setRoles(Roles securityRoles) { 
        roles = securityRoles;
    }

    // ~ Session management
    // =========================================================================

    /*
     * (non-Javadoc)
     * 
     * @see ome.server.utests.sessions.SessionManager#create(ome.system.Principal)
     */
    public Session create(Principal principal) {
        
        principal = checkPrincipal(principal);

        // Do lookups 
        final Experimenter exp = admin.userProxy(principal.getName());
        final ExperimenterGroup grp = admin.groupProxy(principal.getGroup());
        final List<Long> memberOfGroupsIds = admin.getMemberOfGroupIds(exp);
        final List<Long> leaderOfGroupsIds = admin.getLeaderOfGroupIds(exp);

        // Set values on sessions 
        Session session = new Session();
        session.getDetails().getPermissions().revokeAll(INVISIBLE_MASK);
        session.getDetails().setOwner(exp);
        session.getDetails().setGroup(grp);
        
        session.setStarted(new Timestamp(System.currentTimeMillis()));
        session = update.saveAndReturnObject(session);

        SessionContext sessionContext = new SessionContextImpl(copy(session), 
                leaderOfGroupsIds, memberOfGroupsIds);
        sessions.put(sessionContext);
        return session;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.server.utests.sessions.SessionManager#copy(Session)
     */
    public Session copy(Session session) {
        if (session == null)
            return null; // EARLY EXIT.
        
        Session copy = new Session();
        copy.setId(session.getId());
        copy.setClosed(session.getClosed());
        copy.setDefaultEventType(session.getDefaultEventType());
        copy.setDefaultPermissions(session.getDefaultPermissions());
        Details d = session.getDetails();
        copy.setDetails(d == null ? null : d.shallowCopy());
        copy.setStarted(session.getStarted());
        copy.setUserAgent(session.getUserAgent());
        copy.setUuid(session.getUserAgent());
        return copy;
    }

    public Session update(Session session) {
        throw new UnsupportedOperationException();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ome.server.utests.sessions.SessionManager#getSession(java.lang.String)
     */
    public Session find(String uuid) {
        SessionContext sessionContext = sessions.get(uuid);
        return checkTimeout(sessionContext.getSession());
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ome.server.utests.sessions.SessionManager#getSession(java.lang.String)
     */
    public Session find(long id) {
        SessionContext sessionContext = sessions.get(id);
        return checkTimeout(sessionContext.getSession());
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.server.utests.sessions.SessionManager#closeSession(java.lang.String)
     */
    public void close(String sessionId) {
        throw new UnsupportedOperationException();
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
        if (session == null) return null; // EARLY EXIT.
throw new UnsupportedOperationException("CHECK FOR NULL; NYI");
        //return sessions.get(uuid);
        //null // we must check here if the Principal matches and update the group/event
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
        // if is a session event or admin event (user change, etc.) act.
        // send to all notifications.
    }

    // ~ Misc
    // =========================================================================

    protected long lastAccess(String sessionId) {
        return 0L;
    }

    /**
     * If the session is timed out, this method removes the session from the 
     * collection and returns null.
     * 
     * @param session
     */
    protected Session checkTimeout(Session session) {
        return null;
    }

    protected String lookup(long id) {
        return null; // TODO
    }


    // ~ Callbacks (Registering session-based components)
    // =========================================================================

    public void addCallback(String sessionId, SessionCallback cb) {

    }

    public Object getCallbackObject(String sessionId, String name) {
        return null;
    }
    
    // ~ Helpers
    // =========================================================================
    
    /**
     * Checks the validity of the given {@link Principal}, and in the case of 
     * an error attempts to correct the problem by returning a new Principal.
     */
    Principal checkPrincipal(Principal p) {

        if (p == null || p.getName() == null) {
            throw new ApiUsageException("Null principal name.");
        }
        
        String type = p.getEventType();
        if (type == null) {
            type = "User";
        }

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
                                    + "m.defaultGroupLink = true",
                            new Parameters().addString("userGroup",
                                    roles.getUserGroupName()).addString(
                                    "userName", p.getName()));

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
        return new Principal(p.getName(), group, type);
    }
}
