/*
 * org.openmicroscopy.shoola.svc.proxy.MessengerReply
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
package org.openmicroscopy.shoola.svc.proxy;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.apache.http.client.methods.CloseableHttpResponse;
import org.openmicroscopy.shoola.svc.transport.HttpChannel;
import org.openmicroscopy.shoola.svc.transport.TransportException;

/** 
 * Reply to a messenger request.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class MessengerReply
    extends Reply
{

    /** The reply to send to the user. */
    private StringBuilder reply;

    /**
     * Creates a new instance.
     * 
     * @param reply The reply to send to user.
     */
    MessengerReply(StringBuilder reply)
    {
        this.reply = reply;
    }

    /**
     * Checks the HTTP status.
     * @see Reply#unmarshal(HttpMethod, HttpChannel)
     */
    public void unmarshal(CloseableHttpResponse response, HttpChannel context)
            throws TransportException
    {
        if (reply != null)
            reply.append(checkStatusCode(response));
    }

}
