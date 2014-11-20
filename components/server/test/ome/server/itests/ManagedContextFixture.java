/*
 *   $Id$
 *
 *   Copyright 2007-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests;

import java.util.UUID;

import ome.api.IAdmin;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.security.SecuritySystem;
import ome.security.basic.CurrentDetails;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.testing.InterceptingServiceFactory;
import ome.tools.spring.InternalServiceFactory;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class ManagedContextFixture {

    public OmeroContext ctx;
    public SessionManager mgr;
    public Executor ex;
    public ServiceFactory managedSf;
    public ServiceFactory internalSf;
    public SecuritySystem security;
    public CurrentDetails holder;
    public LoginInterceptor login;

    public ManagedContextFixture() {
        this(OmeroContext.getManagedServerContext());
    }

    public void close() {
        ctx.closeAll();
    }

    public ManagedContextFixture(OmeroContext ctx) {
        this.ctx = ctx;
        mgr = (SessionManager) ctx.getBean("sessionManager");
        ex = (Executor) ctx.getBean("executor");
        security = (SecuritySystem) ctx.getBean("securitySystem");
        holder = (CurrentDetails) ctx.getBean("principalHolder");
        login = new LoginInterceptor(holder);
        managedSf = new ServiceFactory(ctx);
        managedSf = new InterceptingServiceFactory(managedSf, login);
        internalSf = new InternalServiceFactory(ctx);
        setCurrentUser("root");
        String user = newUser();
        setCurrentUser(user);
    }

    public void tearDown() throws Exception {
        managedSf = null;
        ctx.close();
    }

    // UTILITIES
    // =========================================================================

    public String uuid() {
        return UUID.randomUUID().toString();
    }

    // LOGIN / PERMISSIONS
    // =========================================================================

    /**
     * Create a new user in a new group.
     */
    public String newUser() {
        String uuid = uuid();
        IAdmin admin = managedSf.getAdminService();
        ExperimenterGroup group = new ExperimenterGroup();
        group.setName(uuid);
        group.setLdap(false);
        admin.createGroup(group);
        return newUser(uuid);
    }

    /**
     * Create a new user in the given group
     */
    public String newUser(String group) {
        IAdmin admin = managedSf.getAdminService();
        Experimenter e = new Experimenter();
        String uuid = uuid();
        e.setOmeName(uuid);
        e.setFirstName("managed");
        e.setMiddleName("context");
        e.setLastName("test");
        e.setLdap(false);
        String oldusr = admin.getEventContext().getCurrentUserName();
        String oldgrp = admin.getEventContext().getCurrentGroupName();
        setCurrentUser("root");
        admin.createUser(e, group);
        setCurrentUserAndGroup(oldusr, oldgrp);
        return uuid;
    }

    /**
     * Create a new user in the "default" group and login.
     */
    public String loginNewUserDefaultGroup() {
        String user = newUser();
        setCurrentUser(user);
        return user;
    }

    /**
     * Login a new user into a new group and return
     * 
     * @return
     */
    public String loginNewUserNewGroup() {
        IAdmin admin = managedSf.getAdminService();
        String groupName = uuid();
        ExperimenterGroup g = new ExperimenterGroup();
        g.setName(groupName);
        g.setLdap(false);
        admin.createGroup(g);
        String name = newUser(groupName);
        setCurrentUser(name);
        return name;
    }

    public String getCurrentUser() {
        return managedSf.getAdminService().getEventContext()
                .getCurrentUserName();
    }

    public void setCurrentUser(String user) {
        setCurrentUserAndGroup(user, "user");
    }

    public void setCurrentUserAndGroup(String user, String group) {
        Principal p = new Principal(user, group, "Test");
        Session s = mgr.createWithAgent(p, "ManagedContextFixture", "127.0.0.1");
        p = new Principal(s.getUuid(), group, "Test");
        login.p = p;
    }

    public Principal getPrincipal() {
        return login.p;
    }
}
