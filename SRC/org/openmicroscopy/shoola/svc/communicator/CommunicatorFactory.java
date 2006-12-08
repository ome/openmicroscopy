/*
 * org.openmicroscopy.shoola.svc.communicator.CommunicatorFactory 
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.svc.communicator;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.svc.SvcActivationException;
import org.openmicroscopy.shoola.svc.SvcActivator;
import org.openmicroscopy.shoola.svc.SvcDescriptor;
import org.openmicroscopy.shoola.svc.proxy.CommunicatorProxy;
import org.openmicroscopy.shoola.svc.transport.ChannelFactory;
import org.openmicroscopy.shoola.svc.transport.HttpChannel;

/** 
 * Component Factory for the {@link Communicator}.
 * It creates an object implementing the {@link Communicator} interface.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class CommunicatorFactory 
	implements SvcActivator
{

    /**
     * Implemented as specified by the {@link SvcActivator} interface. 
     * @see SvcActivator#activate(SvcDescriptor)
     */
	public Object activate(SvcDescriptor desc) 
		throws Exception
	{
		CommunicatorDescriptor d = (CommunicatorDescriptor) desc;
		Communicator service = null;
		try {
			HttpChannel channel = ChannelFactory.getChannel(
									d.getChannelType(), 
									d.getURL(), d.getConnexionTimOut());
			service = new CommunicatorProxy(channel);
		} catch (Exception e) {
			throw new SvcActivationException("Couldn't activate Communicator.", 
                    e);
		}
		System.out.println("service: "+service);
		return service;
	}

    /**
     * Implemented as specified by the {@link SvcActivator} interface. 
     * @see SvcActivator#deactivate()
     */
	public void deactivate()
	{
		
	}

}
