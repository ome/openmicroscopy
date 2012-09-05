/*
 * org.openmicroscopy.shoola.examples.data.LoginHeadless 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.examples.data;


//Java imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.AdminService;
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.events.ActivateAgents;
import org.openmicroscopy.shoola.env.data.login.LoginService;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;

import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;


/** 
 * Connect to OMERO w/o splash-screen. The credentials might have already 
 * been stored locally.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class LoginHeadless {

	LoginHeadless()
	{
		String homeDir = "";
		Container container = Container.startupInHeadlessMode(homeDir, null, 1);
		Registry reg = container.getRegistry();
		LoginService svc = (LoginService) reg.lookup(LookupNames.LOGIN);
		UserCredentials uc = new UserCredentials("root", "omero",
				"localhost", UserCredentials.HIGH);
		int value = svc.login(uc);
		if (value == LoginService.CONNECTED) {
			System.err.println("connected");
			//For testing purpose. Now start the UI if required.
			container.getRegistry().getEventBus().post(new ActivateAgents());
			
			//List
			AdminService adminSvc = container.getRegistry().getAdminService();
			//If you need information about the user.
			ExperimenterData exp = adminSvc.getUserDetails();
			//All the groups the user is member of 
			Collection<GroupData> groups = adminSvc.getAvailableUserGroups();
			//The user is logged into his/her default group. 
			//Security Context is mainly for now only using the group
			// idea is to support multi-server.
			SecurityContext ctx = new SecurityContext(
					exp.getDefaultGroup().getId());
			List<Long> imageIds = new ArrayList<Long>();
			
			//To be modified.
			imageIds.add(122L);
			imageIds.add(151L);
			OmeroDataService dataSvc = container.getRegistry().getDataService();
			try {
				Set images = dataSvc.getImages(ctx, ImageData.class, imageIds,
						-1);
				System.err.println(images.size());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public static void main(String[] args)
	{
		new LoginHeadless();
	}
	
}
