/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.client.itests.sec;

import junit.framework.TestCase;
import ome.api.ISession;
import ome.model.meta.Session;
import ome.system.Login;
import ome.system.Principal;
import ome.system.ServiceFactory;

import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class SessionTest extends TestCase {

    @Test
    public void testServiceFactoryWithNormalUsageAcquiresSession() {
        ServiceFactory sf = new ServiceFactory();
    }

    @Test
    public void AndIfSessionIsLostReacquires() {
        ServiceFactory sf = new ServiceFactory();

        fail("Is this the right thing to do?");
    }

    @Test
    public void testSimpleCreate() throws Exception {
        ServiceFactory sf = new ServiceFactory();
        ISession service = sf.getServiceByClass(ISession.class);
        Session s = sf.getSession();
        service.closeSession(s);
    }

    @Test
    public void testCreationByRoot() throws Exception {
        ServiceFactory sf = new ServiceFactory("ome.client.test");
        String name = sf.getAdminService().getEventContext()
                .getCurrentUserName();
        Login rootLogin = (Login) sf.getContext().getBean("rootLogin");
        ServiceFactory root = new ServiceFactory(rootLogin);
        ISession sessions = root.getServiceByClass(ISession.class);
        Principal p = new Principal(name, "user", "Test");
        Session s = sessions.createSessionWithTimeout(p, 10L);
        ServiceFactory sessionedSf = new ServiceFactory();
        sessionedSf.setSession(s);
        sessionedSf.getConfigService().getServerTime();
    }

}
