/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.system.EventContext;
import ome.system.Principal;

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

    // Injected
    LocalQuery query;
    LocalUpdate update;
    Cache sessions;
    
    // ~ Injectors
    // =========================================================================
    
    public void setQueryService(LocalQuery queryService) {
        query = queryService;
    }
    
    public void setUpdateService(LocalUpdate updateService) {
        update = updateService;
    }

    public void setCache(Cache sessionCache) { 
        sessions = sessionCache;
    }

    // ~ Session management
    // =========================================================================

    /*
	 * Is given trustable values by the {@link SessionBean}
     */
    public Session create(Experimenter exp, ExperimenterGroup grp, 
    		List<Long>leaderOfGroupsIds, List<Long> memberOfGroupsIds, List<String> roles,
    		String type, Permissions perms) {

        // Set values on sessions 
        Session session = new Session();
        session.setUuid(UUID.randomUUID().toString());
        session.getDetails().setPermissions( new Permissions(Permissions.USER_PRIVATE) );
        session.getDetails().setOwner(exp);
        session.getDetails().setGroup(grp);
        
        // if defaults are null
        parseAndSetDefaultType(type, session);
        parseAndSetDefaultPermissions(perms, session);
        
        session.setStarted(new Timestamp(System.currentTimeMillis()));
        session = update.saveAndReturnObject(session);

        Session copy = new Session();
        copy(session,copy);
        SessionContext sessionContext = new SessionContextImpl
        	(copy,leaderOfGroupsIds, memberOfGroupsIds, roles);
        putSession(session.getUuid(), sessionContext);
        return session;
    }

    public void copy(Session source, Session target) {
        if (source == null || target ==null)
        	throw new ApiUsageException("Source and target may not be null.");
        
        target.setId(source.getId());
        target.setClosed(source.getClosed());
        target.setDefaultEventType(source.getDefaultEventType());
        target.setDefaultPermissions(source.getDefaultPermissions());
        Details d = source.getDetails();
        target.setDetails(d == null ? null : d.shallowCopy());
        target.setStarted(source.getStarted());
        target.setUserAgent(source.getUserAgent());
        target.setUuid(source.getUuid());
    }

    public Session update(Session session) {
    	if (session == null) return null;
    	if (session.getUuid() == null) return null;
    	
    	SessionContext ctx = getSessionContext(session.getUuid());
    	Session orig = ctx.getSession();
    	
    	// Conditiablly settable
    	
    	
    	// Unconditionally settable
    	parseAndSetDefaultType(session.getDefaultEventType(), orig);
    	parseAndSetDefaultPermissions(session.getDefaultPermissions(), orig);
    	orig.setUserAgent(session.getUserAgent());
    	// Need to handle notifications
    	
    	throw new UnsupportedOperationException();
    	
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ome.server.utests.sessions.SessionManager#getSession(java.lang.String)
     */
    public Session find(String uuid) {
        SessionContext sessionContext = getSessionContext(uuid);
        return (sessionContext == null) ? null : sessionContext.getSession();
    }

    /*
     */
    public void close(String uuid) {
    	
    	SessionContext ctx = getSessionContext(uuid);
    	if (ctx == null) return;
    	
    	// TODO this is not safe
    	Session s = ctx.getSession();
    	s.setClosed(new Timestamp(System.currentTimeMillis()));
    	update(s);
    	sessions.remove(uuid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.services.sessions.SessionManager#getUserRoles(String)
     */
    public List<String> getUserRoles(String uuid) {
    	SessionContext ctx = getSessionContext(uuid);
    	if (ctx == null) return Collections.emptyList();
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

    // ~ Callbacks (Registering session-based components)
    // =========================================================================

    public void addCallback(String sessionId, SessionCallback cb) {

    }

    public Object getCallbackObject(String sessionId, String name) {
        return null;
    }
    
    // ~ Misc
    // =========================================================================
    
	private void putSession(String uuid, SessionContext sessionContext) {
		sessions.put(new Element(uuid,sessionContext));
	}
    
	private SessionContext getSessionContext(String uuid) {
		Element elt = sessions.get(uuid);
		if (elt == null) return null;
		return (SessionContext) elt.getObjectValue(); 
	}

	private void parseAndSetDefaultPermissions(Permissions perms, Session session) {
		Permissions _perm = (perms == null) ? Permissions.DEFAULT : perms;
        parseAndSetDefaultPermissions(_perm.toString(), session);
	}
	
	private void parseAndSetDefaultPermissions(String perms, Session session) {
		String _perm = (perms == null) ? Permissions.DEFAULT.toString() : perms.toString();
        session.setDefaultPermissions(_perm);
	}

	private void parseAndSetDefaultType(String type, Session session) {
		String _type = (type == null) ? "User" : type;
        session.setDefaultEventType(_type);
	}
    
}
