/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sec;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;

import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Flag;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.PasswordUtil;
import ome.security.basic.BasicMethodSecurity;
import ome.server.itests.AbstractManagedContextTest;
import ome.system.Roles;
import ome.util.IdBlock;

@Test( groups = "integration")
public class MethodSecurityTest extends AbstractManagedContextTest {

    BasicMethodSecurity msec;

    @Test(groups = "ticket:645")
    public void testUserRoles() throws Exception {

        SimpleJdbcTemplate jdbc =         (SimpleJdbcTemplate)
        this.applicationContext.getBean("simpleJdbcTemplate");

        msec = new BasicMethodSecurity();
        msec.setSimpleJdbcOperations(jdbc);
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
