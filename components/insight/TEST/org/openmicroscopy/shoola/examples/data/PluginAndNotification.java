/*
 * org.openmicroscopy.shoola.examples.startup.PluginAndNotification 
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
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.ConnectedEvent;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;

/** 
 * Login and list to connection event sent.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class PluginAndNotification 
	implements AgentEventListener
{

	PluginAndNotification()
	{
		
		String home = "";
		//Login in with splash screen
		Container c = Container.startupInPluginMode(home, null, 1);
		//If we arrive here the user clicks on Login/Quit.
		//Check if connected
		Registry reg = c.getRegistry();
		if (!reg.getAdminService().isConnected()) {
			System.err.println("not connected");
			return;
		}
		//Register to connected event to know when we log off.
		reg.getEventBus().register(this, ConnectedEvent.class);
		
	}
	
	public static void main(String[] args)
	{
		new PluginAndNotification();
	}

	public void eventFired(AgentEvent e) {
		//we are now disconnected
		if (e instanceof ConnectedEvent) {
			System.err.println(e);
		}
		
	}
	
}
