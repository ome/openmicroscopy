/*
 * ome.client.itests.tasks.AbstractAdminTaskTest
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

package ome.client.itests.tasks;

import org.testng.annotations.*;

import java.util.Properties;
import java.util.UUID;

import ome.system.Login;
import ome.system.ServiceFactory;
import ome.util.tasks.Run;
import ome.util.tasks.admin.AddUserTask;

import junit.framework.TestCase;

@Test( groups = { "client", "integration" } )
public abstract class AbstractAdminTaskTest extends TestCase {

	ServiceFactory sf, root;
	String[] rootString;

	@org.testng.annotations.Configuration( beforeTestClass = true)
	public void setup(){
		sf = new ServiceFactory("ome.client.test");
		Login rootLogin = (Login) sf.getContext().getBean("rootLogin");
		root = new ServiceFactory(rootLogin);
		rootString = new String[]{
				"user="+rootLogin.getName(),
				"group="+rootLogin.getGroup(),
				"type="+rootLogin.getEvent(),
				"pass="+rootLogin.getPassword()
		};
	}

	// ~ Helpers
	// =========================================================================
	
	protected String[] join(String[] arr1, String[] arr2) {
		String[] arr = new String[arr1.length+arr2.length];
		System.arraycopy(arr1, 0, arr, 0, arr1.length);
		System.arraycopy(arr2, 0, arr, arr1.length, arr2.length);
		return arr;
	}
}
