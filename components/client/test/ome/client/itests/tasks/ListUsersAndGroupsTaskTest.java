/*
 * ome.client.itests.tasks.AddGroupTaskTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.client.itests.tasks;

import org.testng.annotations.*;

import java.util.Properties;
import java.util.UUID;

import ome.util.tasks.Run;
import ome.util.tasks.admin.AddGroupTask;
import ome.util.tasks.admin.ListUsersAndGroupsTask;

public class ListUsersAndGroupsTaskTest extends AbstractAdminTaskTest {

    @Test
    public void testOut() throws Exception {
        Properties p = new Properties();
        p.setProperty("groups", "out"); 
        p.setProperty("users", "out");
        new ListUsersAndGroupsTask(root, p).run();
    }

}
