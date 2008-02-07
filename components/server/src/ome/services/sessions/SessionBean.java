/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions;

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
import ome.api.local.LocalAdmin;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
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
 * Implementation of {@link ISession}.
 * 
 * Note: unlike all other services, {@link SessionBean} is <em>not</em>
 * intercepted via {@link OmeroAroundInvoke} as the starting point for all
 * Omero interactions.
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

    SessionManager mgr;

    LocalAdmin rawAdmin;

    // ~ Injectors
    // =========================================================================

    BeanHelper getHelper() {
        if (helper == null) {
            helper = new BeanHelper(SessionBean.class);
        }
        return helper;
    }

    public void setManager(SessionManager sessionManager) {
        getHelper().throwIfAlreadySet(mgr, sessionManager);
        this.mgr = sessionManager;
    }

    public void setLocalAdmin(LocalAdmin admin) {
        getHelper().throwIfAlreadySet(rawAdmin, admin);
        this.rawAdmin = admin;
    }

    public Class<? extends ServiceInterface> getServiceInterface() {
        return ISession.class;
    }

    public void selfConfigure() {
        getHelper().configure(this);
    }

    // ~ Session lifecycle
    // =========================================================================

    public Session createSession(@NotNull
    Principal principal, @Hidden String credentials) {

        if (principal == null) {
            throw new ApiUsageException("Principal cannot be null");
        }

        if (!rawAdmin.checkPassword(principal.getName(), credentials)) {
            throw new SecurityViolation("Authentication exception.");
        }

        Session session = null;
        session = mgr.create(principal);
        if (session == null) {
            throw new SessionException("Session creation failed.");
        }
        return session;
    }

    public Session updateSession(@NotNull
    Session session) {
        return mgr.update(session);
    }

    public void closeSession(@NotNull
    Session session) {
        mgr.close(session.getUuid());
    }

}
