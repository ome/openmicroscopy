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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.login.LoginService;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;


/** 
 * Connect to OMERO w/o splash-screen. The credentials might have already 
 * been stored locally.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
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
			container.activateUI();
		}
	}
	
	public static void main(String[] args)
	{
		new LoginHeadless();
	}
	
}
