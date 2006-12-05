/*
 * ome.util.tasks.admin.AddUserTask
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

package ome.util.tasks.admin;

//Java imports
import java.util.Properties;

//Third-party libraries

//Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.api.IAdmin;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.ServiceFactory;
import ome.util.tasks.Configuration;
import ome.util.tasks.SimpleTask;

import static ome.util.tasks.admin.AddGroupTask.Keys.leader;
import static ome.util.tasks.admin.AddUserTask.Keys.*;

/** 
 * {@link SimpleTask} which creates a {@link Experimenter} with the given
 * login name, first name, and last name, and optionally with the given 
 * email, middle name, institution, and email.
 * 
 * Understands the parameters:
 * <ul>
 * <li>omename</li>
 * <li>firstname</li>
 * <li>lastname</li>
 * <li>middlename</li>
 * <li>institution</li>
 * <li>email</li>
 * <li>group</li>
 * </ul>
 * 
 * Must be logged in as an administrator. 
 * See {@link Configuration} on how to do this.
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see     SimpleTask
 * @since   3.0-M4
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class AddUserTask extends SimpleTask
{

	/**
	 * Enumeration of the string values which will be used directly by {@link AddUserTask}.
	 */
	public enum Keys {
		omename, firstname, lastname, middlename, institution, email, group
	}
	
	/** Delegates to super */
	public AddUserTask(ServiceFactory sf, Properties p) 
	{
		super(sf,p);
	}
	
	// TODO if we want to use this directly in AdminImpl we'll need to override
	// the slow property lookups.
	
	/** Performs the actual {@link Experimenter} creation.
	 */
	@Override
	public void doTask() {
		super.doTask(); // logs
		final IAdmin admin = getServiceFactory().getAdminService();
		final String groupName = enumValue(group);
		Experimenter e = new Experimenter();
		e.setOmeName(enumValue(omename));
		e.setFirstName(enumValue(firstname));
		e.setMiddleName(enumValue(middlename));
		e.setLastName(enumValue(lastname));
		e.setInstitution(enumValue(institution));
		e.setEmail(enumValue(email));
		long uid = admin.createUser(e,groupName);
		getLogger().info(String.format(
				"Added user %s with id %d",e.getOmeName(),uid));
	}
		
}
