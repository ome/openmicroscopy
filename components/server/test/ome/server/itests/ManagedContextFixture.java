/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests;

import java.util.UUID;

import ome.api.IAdmin;
import ome.model.meta.Experimenter;
import ome.model.meta.Session;
import ome.security.SecuritySystem;
import ome.security.basic.PrincipalHolder;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.testing.InterceptingServiceFactory;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class ManagedContextFixture {

    public OmeroContext ctx = OmeroContext.getManagedServerContext();
    public SessionManager mgr = (SessionManager) ctx.getBean("sessionManager");
    public Executor ex = (Executor) ctx.getBean("executor");
    public ServiceFactory sf = new ServiceFactory(ctx);
    public SecuritySystem security;
    public PrincipalHolder holder;
    public LoginInterceptor login;

    public ManagedContextFixture() {
        security = (SecuritySystem) ctx.getBean("securitySystem");
        holder = (PrincipalHolder) ctx.getBean("principalHolder");
        login = new LoginInterceptor(holder);
        sf = new InterceptingServiceFactory(sf, login);
        setCurrentUser("root");
        String user = newUser();
        setCurrentUser(user);
    }

    protected void tearDown() throws Exception {
        sf = null;
        ctx.close();
    }

    public String uuid() {
        return UUID.randomUUID().toString();
    }

    public String newUser() {
        Experimenter e = new Experimenter();
        String uuid = uuid();
        e.setOmeName(uuid);
        e.setFirstName("managed");
        e.setMiddleName("context");
        e.setLastName("test");
        IAdmin admin = sf.getAdminService();
        admin.createUser(e, "default");
        return uuid;
    }

    public String getCurrentUser() {
        return sf.getAdminService().getEventContext().getCurrentUserName();
    }

    public void setCurrentUser(String user) {
        Principal p = new Principal(user, "user", "Test");
        Session s = mgr.create(p);
        p = new Principal(s.getUuid(), "user", "Test");
        login.p = p;
    }
}
