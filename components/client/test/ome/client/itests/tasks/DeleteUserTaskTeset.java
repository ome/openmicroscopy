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
import ome.util.tasks.admin.*;

public class DeleteUserTaskTeset extends AbstractAdminTaskTest {

    @Test
    public void testSimple() throws Exception {
        String group = makeGroup();
        String user = makeUser(group);
        Properties p = new Properties();
        p.setProperty("user",user);
        new DeleteUserTask(root, p).run();
    }

}
