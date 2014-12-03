/*
 * org.openmicroscopy.shoola.svc.communicator.CommunitatorDescriptor 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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
import org.openmicroscopy.shoola.svc.SvcDescriptor;

/** 
 * Service descriptor for the {@link Communicator} service.
 * Use an instance of this class to retrieve the {@link Communicator} from
 * {@link org.openmicroscopy.shoola.svc.SvcRegistry}
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class CommunicatorDescriptor
    implements SvcDescriptor
{

    /** Human readable name of the service identified by this descriptor. */
    private static final String SVC_NAME = "Communication Service";

    /** The server's URL. */
    private String url;

    /** The time before being disconnected. */
    private int connTimeout;

    /** 
     * The type of channel. One of the constants defined by 
     * {@link org.openmicroscopy.shoola.svc.transport.HttpChannel}.
     */
    private int channelType;

    /**
     * Creates a new instance.
     * 
     * @param channelType The type of channel.
     * @param url The server's URL.
     * @param connTimeout The time before being disconnected.
     */
    public CommunicatorDescriptor(int channelType, String url, int connTimeout)
    {
        this.url = url;
        this.connTimeout = connTimeout;
        this.channelType = channelType;
    }

    /**
     * Returns the server's URL.
     * 
     * @return See above.
     */
    public String getURL() { return url; }

    /**
     * Returns the time before being disconnected.
     * 
     * @return See above.
     */
    public int getConnexionTimeout() { return connTimeout; }

    /**
     * Returns the type of channel.
     * 
     * @return See above.
     */
    public int getChannelType() { return channelType; }

    /**
     * Implemented as specified by the {@link SvcDescriptor} interface.
     * @see SvcDescriptor#getSvcName()
     */
    public String getSvcName() { return SVC_NAME; }

}
