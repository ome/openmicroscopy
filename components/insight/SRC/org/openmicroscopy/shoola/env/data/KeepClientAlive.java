/*
 * org.openmicroscopy.shoola.env.data.KeepClientAlive 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.data.events.HeartbeatEvent;
import omero.log.LogMessage;


/** 
 * Keeps the services alive.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class KeepClientAlive 
	implements Runnable
{

	/** Reference to the gateway. */
	private final OMEROGateway gateway;
	
	/** Reference to the container. */
	private Container container;

	/** Runs. */
	public void run()
	{
		
		try {
            gateway.keepSessionAlive();
        } catch (Throwable t) {
        	LogMessage message = new LogMessage();
        	message.append("Exception while keeping the services alive.\n");
        	message.print(t);
        	container.getRegistry().getLogger().error(this, message);
        	gateway.handleConnectionException(t);
        }
		container.getRegistry().getEventBus().post(new HeartbeatEvent());
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param container Reference to the container. 
	 * 					Mustn't be <code>null</code>.
	 * @param gateway   Reference to the gateway. Mustn't be <code>null</code>.
	 */
	KeepClientAlive(Container container, OMEROGateway gateway)
	{
		if (container == null)
			throw new IllegalArgumentException("No container specified.");
		if (gateway == null)
			throw new IllegalArgumentException("No gateway specified.");
		this.gateway = gateway;
		this.container = container;
	}
	
}
