/*
 * org.openmicroscopy.shoola.svc.communicator.CommunicatorFactory 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
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
                    d.getURL(), d.getConnexionTimeout());
            service = new CommunicatorProxy(channel);
        } catch (Exception e) {
            throw new SvcActivationException("Couldn't activate Communicator.",
                    e);
        }
        return service;
    }

    /**
     * Implemented as specified by the {@link SvcActivator} interface.
     * @see SvcActivator#deactivate()
     */
    public void deactivate() {}

}
