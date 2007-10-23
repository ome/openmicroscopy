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
import ome.security.SecuritySystem;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.tools.spring.ManagedServiceFactory;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class ManagedContextFixture {

    public OmeroContext ctx = OmeroContext.getManagedServerContext();
    public ManagedServiceFactory sf = new ManagedServiceFactory();
    public SecuritySystem sec;

    public ManagedContextFixture() {
        sf.setApplicationContext(ctx);
        sec = (SecuritySystem) sf.getContext().getBean("securitySystem");
        setCurrentUser("root");
        String user = newUser();
        setCurrentUser(user);
    }

    protected void tearDown() throws Exception {
        sf = null;
        sec.logout();
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
        sec.login(new Principal(user, "user", "Test"));
    }
}
