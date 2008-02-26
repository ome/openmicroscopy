/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions;

import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import ome.annotations.Hidden;
import ome.annotations.NotNull;
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.api.ISession;
import ome.api.ServiceInterface;
import ome.conditions.AuthenticationException;
import ome.conditions.RootException;
import ome.conditions.SessionException;
import ome.logic.SimpleLifecycle;
import ome.model.meta.Session;
import ome.services.util.BeanHelper;
import ome.services.util.OmeroAroundInvoke;
import ome.system.Principal;
import ome.system.SelfConfigurableService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link ISession}. Is merely a wrapper around the
 * {@link SessionManager} Spring-singleton.
 * 
 * Note: unlike all other services, {@link SessionBean} is <em>not</em>
 * intercepted via {@link OmeroAroundInvoke} as the starting point for all Omero
 * interactions.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional
@RevisionDate("$Date: 2007-06-05 15:59:33 +0200 (Tue, 05 Jun 2007) $")
@RevisionNumber("$Revision: 1593 $")
@Stateless
@Remote(ISession.class)
@RemoteBinding(jndiBinding = "omero/remote/ome.api.ISession")
@Local(ISession.class)
@LocalBinding(jndiBinding = "omero/local/ome.api.ISession")
@Interceptors( { SimpleLifecycle.class })
public class SessionBean implements ISession, SelfConfigurableService {

    private BeanHelper helper = new BeanHelper(SessionBean.class);

    // Injected
    SessionManager mgr;

    // ~ Injectors
    // =========================================================================

    BeanHelper getHelper() {
        if (helper == null) {
            helper = new BeanHelper(SessionBean.class);
        }
        return helper;
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

    @RolesAllowed("system")
    public Session createSessionWithTimeout(@NotNull
    Principal principal, long seconds) {

        Session session = null;
        try {
            session = mgr.create(principal);
            session.setTimeToIdle(0L);
            session.setTimeToLive(seconds);
            return mgr.update(session);
        } catch (Exception e) {
            throw creationExceptionHandler(e);
        }

    }

    @RolesAllowed( { "user", "guest" })
    public Session createSession(@NotNull
    Principal principal, @Hidden
    String credentials) {

        Session session = null;
        try {
            session = mgr.create(principal, credentials);
        } catch (Exception e) {
            throw creationExceptionHandler(e);
        }
        return session;
    }

    @RolesAllowed( { "user", "guest" })
    public Session getSession(@NotNull
    String sessionUuid) {
        return mgr.find(sessionUuid);
    }

    @RolesAllowed( { "user", "guest" })
    public Session updateSession(@NotNull
    Session session) {
        return mgr.update(session);
    }

    @RolesAllowed( { "user", "guest" })
    public void closeSession(@NotNull
    Session session) {
        mgr.close(session.getUuid());
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
