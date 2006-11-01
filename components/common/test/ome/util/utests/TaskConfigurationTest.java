/*
 * ome.util.utests.TaskConfigurationTest
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.util.utests;

import org.testng.annotations.*;
import java.util.HashMap;
import java.util.Properties;

import ome.util.builders.PojoOptions;
import ome.util.tasks.Run;
import ome.util.tasks.Configuration;
import ome.util.tasks.Task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/** 
 * fairly limited unit tests of configuration. The real work is in
 * {@link Configuration#createServiceFactory()} and 
 * {@link Configuration#createTask()} but these can't be called from common.
 */
public class TaskConfigurationTest extends TestCase {

	@Test
	public void testRequiredElements() throws Exception {
		fails(null);
		fails(new Properties());
		fails(pairs("task","unknown class"));
		fails(pairs("task","admin.AddUserTask","host","forgotport"));
		fails(pairs("task","admin.AddUserTask","port","forgothost"));
		fails(pairs("task","admin.AddUserTask","user","forgotlots"));
		fails(pairs("task","admin.AddUserTask","pass","forgotlots"));
		fails(pairs("task","admin.AddUserTask","type","forgotlots"));
		fails(pairs("task","admin.AddUserTask","group","forgotlots"));
	}
	
	@Test
	public void testGetProperties() throws Exception {
		Properties p = pairs("task","admin.AddUserTask","foo","bar");
		Configuration c = new Configuration(p);
		c.getProperties().containsKey("foo");
	}
	
	@Test
	public void testGetTaskClass() throws Exception {
		Properties p = pairs("task","admin.AddUserTask");
		Configuration c = new Configuration(p);
		Class<Task> k = c.getTaskClass();
		assertTrue(k.getName().endsWith("admin.AddUserTask"));
	}
	
	@Test
	public void testNullServerAndLogin() throws Exception {
		Properties p = pairs("task","admin.AddUserTask");
		Configuration c = new Configuration(p);
		assertNull( c.getServer() );
		assertNull( c.getLogin() );
	}
	
	// ~ Helpers
	// =========================================================================

	protected void fails(Properties p){
		try {
			new Configuration(p);
			fail("Should have failed.");
		} catch (IllegalArgumentException iae) {
			// ok
		}
	}
	
	protected Properties pairs(String...values) {
		Properties p = new Properties();
		for (int i = 0; i < values.length; i+=2) {
			p.setProperty(values[i], values[i+1]);
		}
		return p;
	}
}
