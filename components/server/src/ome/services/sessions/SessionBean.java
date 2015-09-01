/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import ome.annotations.Hidden;
import ome.annotations.NotNull;
import ome.annotations.RolesAllowed;
import ome.api.ISession;
import ome.api.ServiceInterface;
import ome.conditions.AuthenticationException;
import ome.conditions.RootException;
import ome.conditions.SecurityViolation;
import ome.conditions.SessionException;
import ome.model.meta.Session;
import ome.security.SecuritySystem;
import ome.security.basic.CurrentDetails;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link ISession}. Is merely a wrapper around the
 * {@link SessionManager} Spring-singleton.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
@Transactional
public class SessionBean implements ISession {

    private final static Logger log = LoggerFactory.getLogger(SessionBean.class);

    final private SessionManager mgr;
    
    final private Executor ex;

    final private CurrentDetails cd;

    public SessionBean(SessionManager mgr, Executor ex, CurrentDetails cd) {
        this.mgr = mgr;
        this.ex = ex;
        this.cd = cd;
    }
    
    // ~ Injectors
    // =========================================================================

    public Class<? extends ServiceInterface> getServiceInterface() {
        return ISession.class;
    }

    // ~ Session lifecycle
    // =========================================================================

    @RolesAllowed({"user", "HasPassword"})
    public Session createUserSession(final long timeToLiveMs,
            final long timeToIdleMs,
            String defaultGroup) {

        final String user = currentUser();
        if (user == null) {
            throw new SecurityViolation("No current user");
        }
        
        try {
            final Principal principal = principal(defaultGroup, user);
            Future<Session> future = ex.submit(new Callable<Session>(){
                public Session call() throws Exception {
                    Session session = mgr.createWithAgent(principal, "createSession", null);
                    session.setTimeToIdle(timeToIdleMs);
                    session.setTimeToLive(timeToLiveMs);
                    return mgr.update(session, false);
                }});
            return ex.get(future);
        } catch (Exception e) {
            throw creationExceptionHandler(e);
        }

    }

    @RolesAllowed("user" /* group owner */)
    public Session createSessionWithTimeout(@NotNull final Principal principal,
            final long milliseconds) {
        return createSessionWithTimeouts(principal, milliseconds, 0L);
    }

    @RolesAllowed("user" /*group owner*/)
    public Session createSessionWithTimeouts(@NotNull final Principal principal,
            final long timeToLiveMilliseconds, final long timeToIdleMilliseconds) {

        final EventContext context = currentContext();
        final List<Long> groupsLed = context.isCurrentUserAdmin() ? null :
            context.getLeaderOfGroupsList();

        try {
            Future<Session> future = ex.submit(new Callable<Session>(){
                public Session call() throws Exception {
                    SessionManager.CreationRequest req = new SessionManager.CreationRequest();
                    req.principal = principal;
                    req.agent = "createSession";
                    req.groupsLed = groupsLed;
                    req.timeToIdle = timeToIdleMilliseconds;
                    req.timeToLive = timeToLiveMilliseconds;
                    return mgr.createFromRequest(req);
                }});
            return ex.get(future);
        } catch (Exception e) {
            throw creationExceptionHandler(e);
        }

    }

    @RolesAllowed( { "user", "guest" })
    public Session createSession(@NotNull Principal principal,
            @Hidden String credentials) {

        Session session = null;
        try {
            session = mgr.createWithAgent(principal, credentials, "createSession", null);
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
    public Session updateSession(@NotNull final Session session) {
        Future<Session> future = ex.submit(new Callable<Session>(){
            public Session call() throws Exception {
                return mgr.update(session);
            }});
        return ex.get(future);
    }

    @RolesAllowed( { "user", "guest" })
    public int closeSession(@NotNull final Session session) {
        Future<Integer> future = ex.submit(new Callable<Integer>(){
            public Integer call() throws Exception {
                return mgr.close(session.getUuid());
            }});
        return ex.get(future);
    }

    @RolesAllowed("user")
    public java.util.List<Session> getMyOpenSessions() {
        final String user = currentUser();
        Future<List<Session>> future = ex.submit(new Callable<List<Session>>(){
            public List<Session> call() throws Exception {
                return mgr.findByUser(user);
            }});
        return ex.get(future);
    }

    @RolesAllowed("user")
    public java.util.List<Session> getMyOpenAgentSessions(final String agent) {
        final String user = currentUser();
        Future<List<Session>> future = ex.submit(new Callable<List<Session>>(){
            public List<Session> call() throws Exception {
                return mgr.findByUserAndAgent(user, agent);
            }});
        return ex.get(future);
    }

    @RolesAllowed("user")
    public java.util.List<Session> getMyOpenClientSessions() {
        final String user = currentUser();
        Future<List<Session>> future = ex.submit(new Callable<List<Session>>(){
            public List<Session> call() throws Exception {
                return mgr.findByUserAndAgent(user, "OMERO.insight",
                        "OMERO.web", "OMERO.importer");
            }});
        return ex.get(future);
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

    @RolesAllowed( { "user", "guest" })
    public Map<String, Object> getInputs(String session) {
        return mgr.inputEnvironment(session);
    }

    @RolesAllowed( { "user", "guest" })
    public Map<String, Object> getOutputs(String session) {
        return mgr.outputEnvironment(session);
    }

    // ~ Helpers
    // =========================================================================

    String currentUser() {
        return currentContext().getCurrentUserName();
    }

    EventContext currentContext() {
        String user = cd.getLast().getName();
        return mgr.getEventContext(new Principal(user));
    }

    private Principal principal(String defaultGroup, final String user) {
        Principal p;
        if (defaultGroup != null) {
            p = new Principal(user, defaultGroup, "User");
        } else {
            p = new Principal(user);
        }
        return p;
    }

    RuntimeException creationExceptionHandler(Exception e) {
        log.info("Handling session exception: ", e);
        if (e instanceof SessionException) {
            return (SessionException) e;
        } else if (e instanceof SecurityViolation) {
            return (SecurityViolation) e;
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
