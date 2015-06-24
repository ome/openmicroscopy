/*
 * org.openmicroscopy.shoola.svc.transport.ChannelFactory 
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
package org.openmicroscopy.shoola.svc.transport;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Helper class to create communication links.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class ChannelFactory
{

    /**
     * Creates a <code>HttpChannel</code> corresponding to the passed type.
     * 
     * @param type The channel type.
     * @param url The server's URL.
     * @param connTimeout The time before being disconnected.
     * @return See above.
     * @throws IllegalArgumentException If the specified type is not supported.
     */
    public static HttpChannel getChannel(int type, String url, int connTimeout)
            throws IllegalArgumentException
    {
        HttpChannel channel = null;

        //We only have one channel now, this switch is more of a stub for
        //future development.
        switch (type) {
        case HttpChannel.CONNECTION_PER_REQUEST:
            channel = new BasicChannel(url, connTimeout);
            break;
        case HttpChannel.DEFAULT:
            channel = new BasicChannel(url, connTimeout);
            break;
        default:
            throw new IllegalArgumentException(
                    "Unrecognized channel type: "+type+".");
        }
        return channel;
    }

}
