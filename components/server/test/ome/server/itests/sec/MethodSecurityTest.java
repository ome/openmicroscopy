/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.itests.sec;

import java.util.List;

import ome.security.auth.PasswordUtil;
import ome.security.basic.BasicMethodSecurity;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.ServiceFactory;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

@Test(groups = "integration")
public class MethodSecurityTest extends AbstractManagedContextTest {

    BasicMethodSecurity msec;

    @Test(groups = "ticket:645")
    public void testUserRoles() throws Exception {

        SessionManager mgr = (SessionManager) this.applicationContext
                .getBean("sessionManager");

        msec = new BasicMethodSecurity();
        msec.setSessionManager(mgr);

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>)
        executor.execute(this.loginAop.p, new Executor.SimpleWork(this, "getRoles") {
            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                return new PasswordUtil(getSqlAction()).userGroups("root");
            }
        });

        assertTrue(roles.size() >= 2);
        boolean found = false;
        for (int i = 0; i < roles.size(); i++) {
            if (roles.get(i).equals("system")) {
                found = true;
            }
        }
        assertTrue(found);
    }

}
