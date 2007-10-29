/*
 * ome.client.itests.tasks.AddGroupTaskTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.client.itests.tasks;

import java.util.Properties;

import ome.util.tasks.admin.ListUsersAndGroupsTask;

import org.testng.annotations.Test;

public class ListUsersAndGroupsTaskTest extends AbstractAdminTaskTest {

    @Test(groups = { "broken", "ticket:823" })
    public void testOut() throws Exception {
        Properties p = new Properties();
        p.setProperty("groups", "out");
        p.setProperty("users", "out");
        new ListUsersAndGroupsTask(root, p).run();
    }

}
