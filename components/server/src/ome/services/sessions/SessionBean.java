/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions;

import java.util.Set;

import ome.annotations.Hidden;
import ome.annotations.NotNull;
import ome.annotations.PermitAll;
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.annotations.RolesAllowed;
import ome.api.ISession;
import ome.api.ServiceInterface;
import ome.conditions.AuthenticationException;
import ome.conditions.RootException;
import ome.conditions.SecurityViolation;
import ome.conditions.SessionException;
import ome.model.internal.Permissions;
import ome.model.meta.Session;
import ome.security.basic.CurrentDetails;
import ome.services.util.BeanHelper;
import ome.system.Principal;
import ome.system.SelfConfigurableService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link ISession}. Is merely a wrapper around the
 * {@link SessionManager} Spring-singleton.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
@Transactional
@RevisionDate("$Date: 2007-06-05 15:59:33 +0200 (Tue, 05 Jun 2007) $")
@RevisionNumber("$Revision: 1593 $")
public class SessionBean implements ISession, SelfConfigurableService {

    private final static Log log = LogFactory.getLog(SessionBean.class);

    private BeanHelper helper = new BeanHelper(SessionBean.class);

    // Injected
    SessionManager mgr;

    CurrentDetails currentUser;

    // ~ Injectors
    // =========================================================================

    BeanHelper getHelper() {
        if (helper == null) {
            helper = new BeanHelper(SessionBean.class);
        }
        return helper;
    }

    public void setCurrentDetails(CurrentDetails cd) {
        getHelper().throwIfAlreadySet(currentUser, cd);
        this.currentUser = cd;
    }

    public void setSessionManager(SessionManager sessionManager) {
        getHelper().throwIfAlreadySet(mgr, sessionManager);
        this.mgr = sessionManager;
    }

    public Class<? extends ServiceInterface> getServiceInterface() {
        return ISession.class;
    }

    public void selfConfigure() {
        getHelper().configure(this);
    }

    // ~ Session lifecycle
    // =========================================================================

    @RolesAllowed("user")
    public Session createUserSession(long timeToLiveMs, long timeToIdleMs,
            String defaultGroup, Permissions umask) {

        final String user = currentUser.getCurrentEventContext().getCurrentUserName();
        if (user == null) {
            throw new SecurityViolation("No current user");
        }
        
        Session session = null;
        try {
            Principal principal = null;
            if (defaultGroup != null) {
                principal = new Principal(user, defaultGroup, "User");
            } else {
                principal = new Principal(user);
            }
            session = mgr.create(principal);
            session.setTimeToIdle(timeToIdleMs);
            session.setTimeToLive(timeToLiveMs);
            return mgr.update(session, false);
        } catch (Exception e) {
            throw creationExceptionHandler(e);
        }

    }

    @RolesAllowed("system")
    public Session createSessionWithTimeout(@NotNull Principal principal,
            long milliseconds) {

        Session session = null;
        try {
            session = mgr.create(principal);
            session.setTimeToIdle(0L);
            session.setTimeToLive(milliseconds);
            return mgr.update(session, true);
        } catch (Exception e) {
            throw creationExceptionHandler(e);
        }

    }

    @RolesAllowed("system")
    public Session createSessionWithTimeouts(@NotNull Principal principal,
            long timeToLiveMilliseconds, long timeToIdleMilliseconds) {

        Session session = null;
        try {
            session = mgr.create(principal);
            session.setTimeToIdle(timeToIdleMilliseconds);
            session.setTimeToLive(timeToLiveMilliseconds);
            return mgr.update(session, true);
        } catch (Exception e) {
            throw creationExceptionHandler(e);
        }

    }

    @RolesAllowed( { "user", "guest" })
    public Session createSession(@NotNull Principal principal,
            @Hidden String credentials) {

        Session session = null;
        try {
            session = mgr.create(principal, credentials);
        } catch (Exception e) {
            throw creationExceptionHandler(e);
        }
        return session;
    }

    @RolesAllowed( { "user", "guest" })
    public Session getSession(@NotNull String sessionUuid) {
        return mgr.find(sessionUuid);
    }
    
    @RolesAllowed( { "user", "guest" })
    public int getReferenceCount(@NotNull String sessionUuid) {
        return mgr.getReferenceCount(sessionUuid);
    }

    @RolesAllowed( { "user", "guest" })
    public Session updateSession(@NotNull Session session) {
        return mgr.update(session);
    }

    @RolesAllowed( { "user", "guest" })
    public int closeSession(@NotNull Session session) {
        return mgr.close(session.getUuid());
    }

    // ~ Environment
    // =========================================================================

    @RolesAllowed( { "user", "guest" })
    public Object getInput(String session, String key) {
        return mgr.getInput(session, key);
    }

    @RolesAllowed( { "user", "guest" })
    public Object getOutput(String session, String key) {
        return mgr.getOutput(session, key);
    }

    @RolesAllowed( { "user", "guest" })
    public void setInput(String session, String key, Object object) {
        mgr.setInput(session, key, object);
    }

    @RolesAllowed( { "user", "guest" })
    public void setOutput(String session, String key, Object object) {
        mgr.setOutput(session, key, object);
    }

    @RolesAllowed( { "user", "guest" })
    public Set<String> getInputKeys(String session) {
        return mgr.inputEnvironment(session).keySet();
    }

    @RolesAllowed( { "user", "guest" })
    public Set<String> getOutputKeys(String session) {
        return mgr.outputEnvironment(session).keySet();
    }

    // ~ Helpers
    // =========================================================================

    RuntimeException creationExceptionHandler(Exception e) {
        log.info("Handling session exception: ", e);
        if (e instanceof SessionException) {
            return (SessionException) e;
        } else if (e instanceof RootException) {
            // This may should be more specific or need to use an event-based
            // conversion routine like in blitz, to allow exceptions like
            // NoAvailableLicenseException to be propagated to the client.
            return (AuthenticationException) new AuthenticationException(
                    "Error creating session.").initCause(e);
        } else {
            return new AuthenticationException("Unknown error ("
                    + e.getClass().getName() + "):" + e.getMessage());
        }
    }

}
