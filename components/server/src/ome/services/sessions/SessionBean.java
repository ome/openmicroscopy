/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions;

import java.util.List;

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
import ome.api.local.LocalQuery;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.conditions.SessionException;
import ome.logic.SimpleLifecycle;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.parameters.Parameters;
import ome.services.util.BeanHelper;
import ome.services.util.OmeroAroundInvoke;
import ome.system.Principal;
import ome.system.Roles;
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

    // Injected
    Roles roles;
    SessionManager mgr;
    LocalAdmin rawAdmin;
    LocalQuery rawQuery;

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
    
    public void setLocalQuery(LocalQuery query) {
        getHelper().throwIfAlreadySet(rawQuery, query);
        this.rawQuery = query;
    }

    public void setRoles(Roles securityRoles) { 
        roles = securityRoles;
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

        if (principal == null || principal.getName() == null) {
            throw new ApiUsageException("Principal and user name cannot be null");
        }

        if (!rawAdmin.checkPassword(principal.getName(), credentials)) {
            throw new SecurityViolation("Authentication exception.");
        }
        
        principal = checkPrincipal(principal);

        // Do lookups 
        final Experimenter exp = rawAdmin.userProxy(principal.getName());
        final ExperimenterGroup grp = rawAdmin.groupProxy(principal.getGroup());
        final List<Long> memberOfGroupsIds = rawAdmin.getMemberOfGroupIds(exp);
        final List<Long> leaderOfGroupsIds = rawAdmin.getLeaderOfGroupIds(exp);
        final List<String> userRoles = rawAdmin.getUserRoles(exp);
        
        Session session = null;
        session = mgr.create(exp,grp,leaderOfGroupsIds,memberOfGroupsIds,userRoles,principal.getEventType(),principal.getUmask());
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
    
    // ~ Helpers
    // ========================================================================

    /**
     * Checks the validity of the given {@link Principal}, and in the case of 
     * an error attempts to correct the problem by returning a new Principal.
     */
    Principal checkPrincipal(Principal p) {

        if (p == null || p.getName() == null) {
            throw new ApiUsageException("Null principal name.");
        }

        //
        // TODO we may should push this code back into SessionManager
        // since it will also need to be checked on group change. 
        // in which case the previous event type check should occur 
        // here
        //
        
        // Null or bad event type values as well as umasks are handled
        // within the SessionManager and EventHandler. It is necessary
        String group = p.getGroup();
        if (group == null) {
            group = "user";
        }
        

        // ticket:404 -- preventing users from logging into "user" group
        else if (roles.getUserGroupName().equals(p.getGroup())) {
            List<ExperimenterGroup> groups = rawQuery.findAllByQuery(
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
        return new Principal(p.getName(), group, p.getEventType()/*FIXME*/);
    }

}
