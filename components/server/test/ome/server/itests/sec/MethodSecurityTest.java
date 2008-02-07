/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sec;

import java.util.List;

import ome.security.PasswordUtil;
import ome.security.basic.BasicMethodSecurity;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.sessions.SessionManager;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.testng.annotations.Test;

@Test( groups = "integration")
public class MethodSecurityTest extends AbstractManagedContextTest {

    BasicMethodSecurity msec;

    @Test(groups = "ticket:645")
    public void testUserRoles() throws Exception {

        SimpleJdbcTemplate jdbc =         (SimpleJdbcTemplate)
        this.applicationContext.getBean("simpleJdbcTemplate");

        SessionManager mgr = (SessionManager)
        this.applicationContext.getBean("sessionManager");

        msec = new BasicMethodSecurity();
        msec.setSessionManager(mgr);
        List<String> roles = PasswordUtil.userGroups(jdbc,"root");
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
