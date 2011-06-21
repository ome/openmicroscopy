/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sec.test;

import junit.framework.TestCase;
import ome.api.IConfig;
import ome.api.ISession;
import ome.conditions.RemovedSessionException;
import ome.model.meta.Experimenter;
import ome.model.meta.Session;
import ome.system.Login;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.ServiceFactory;

import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
@Test(enabled=false, groups = { "broken" })
public class SessionTest extends TestCase {

    Login rootLogin = null; // (Login) OmeroContext.getInstance("ome.client.test") .getBean("rootLogin");
    Principal rootPrincipal = null; // new Principal(rootLogin.getName(), "system", "Test");

    @Test(enabled=false)
    public void testServiceFactoryWithNormalUsageAcquiresSession() {
        ServiceFactory sf = new ServiceFactory(rootLogin);
        sf.getQueryService().get(Experimenter.class, 0L);
    }

    @Test(enabled=false)
    public void AndIfSessionIsLostReacquires() {
        ServiceFactory sf = new ServiceFactory();
        IConfig c1 = sf.getConfigService(), c2;
        sf.closeSession();
        try {
            c1.getServerTime();
            fail("should fail since session closed");
        } catch (Exception e) {
            // ok
        }
        // A new proxy should work
        c2 = sf.getConfigService();
        c2.getServerTime();

        // Just calling close session
        sf.getSessionService().closeSession(sf.getSession());
        try {
            sf.getQueryService().get(Experimenter.class, 0L);
            fail("Shouldn't be logged in");
        } catch (Exception e) {
            // ok
        }

    }

    @Test(enabled=false)
    public void testSimpleCreate() throws Exception {
        ServiceFactory sf = new ServiceFactory();
        ISession service = sf.getServiceByClass(ISession.class);

        Session s = service.createSession(rootPrincipal, rootLogin
                .getPassword());
        sf.setSession(s);
        Session s2 = sf.getSession();
        assertEquals(s, s2);
        service.closeSession(s);
    }

    @Test(enabled=false)
    public void testCreationByRoot() throws Exception {
        ServiceFactory sf = new ServiceFactory("ome.client.test");
        String name = sf.getAdminService().getEventContext()
                .getCurrentUserName();
        ServiceFactory root = new ServiceFactory(rootLogin);
        ISession sessions = root.getServiceByClass(ISession.class);
        Principal p = new Principal(name, "user", "Test");
        Session s = sessions.createSessionWithTimeout(p, 10 * 1000L);
        ServiceFactory sessionedSf = new ServiceFactory();
        sessionedSf.setSession(s);
        sessionedSf.getConfigService().getServerTime();
    }

    @Test(enabled=false, expectedExceptions = RemovedSessionException.class)
    public void testOthersCanKillASession() {
        ServiceFactory sf1 = new ServiceFactory(rootLogin), sf2 = new ServiceFactory(
                rootLogin);

        Session session = sf1.getSession();
        sf2.getSessionService().closeSession(session);
        sf1.getConfigService().getServerTime();
    }

}
