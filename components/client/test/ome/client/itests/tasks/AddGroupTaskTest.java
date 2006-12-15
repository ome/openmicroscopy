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

import ome.system.Login;
import ome.system.ServiceFactory;
import ome.util.tasks.Run;
import ome.util.tasks.admin.AddGroupTask;
import ome.util.tasks.admin.AddUserTask;

import junit.framework.TestCase;

public class AddGroupTaskTest extends AbstractAdminTaskTest {

	@Test
	public void testSimple() throws Exception {
		Properties p = new Properties();
		p.setProperty("name",UUID.randomUUID().toString());
		p.setProperty("description", "task");
		p.setProperty("leader", "root");
		new AddGroupTask(root,p).run();
	}
	
	@Test
	public void testViaCommandLine() throws Exception {
		Run.main(join(rootString, 
				new String[]{
				"task=admin.AddGroupTask",
				"description=task",
				"leader=root",
				"name="+UUID.randomUUID().toString()}));
	}

}
