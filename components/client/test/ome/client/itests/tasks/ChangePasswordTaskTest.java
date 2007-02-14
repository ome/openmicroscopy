/*
 * ome.client.itests.tasks.AddUserTaskTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.client.itests.tasks;

import org.testng.annotations.*;

import java.util.Properties;
import java.util.UUID;

import ome.util.tasks.Run;
import ome.util.tasks.admin.AddUserTask;
import ome.util.tasks.admin.ChangePasswordTask;

public class ChangePasswordTaskTest extends AbstractAdminTaskTest {

    @Test
    public void testSimple() throws Exception {
        String group = makeGroup();
        Properties p = new Properties();
        p.setProperty("omename", UUID.randomUUID().toString());
        p.setProperty("firstname", "task");
        p.setProperty("lastname", "test");
        p.setProperty("group", group);
        new AddUserTask(root, p).run();
        
        p.setProperty("password", "bob");
        new ChangePasswordTask(root,p).run();
    }

    @Test
    public void testWithNoPassword() throws Exception {
        String group = makeGroup();
        Properties p = new Properties();
        p.setProperty("omename", UUID.randomUUID().toString());
        p.setProperty("firstname", "task");
        p.setProperty("lastname", "test");
        p.setProperty("group", group);
        new AddUserTask(root, p).run();

        // Didn't sett "password" here. Shouldn't be removed.
        new ChangePasswordTask(root,p).run();
    }

}
