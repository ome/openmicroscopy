/*
 * org.openmicroscopy.shoola.omeis.proxy.GetStackReply
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

package org.openmicroscopy.shoola.omeis.proxy;


//Java imports
import java.io.IOException;

//Third-party libraries
import org.apache.commons.httpclient.HttpMethod;

//Application-internal dependencies
import org.openmicroscopy.shoola.omeis.services.ImageServiceException;
import org.openmicroscopy.shoola.omeis.transport.HttpChannel;
import org.openmicroscopy.shoola.util.mem.ByteArray;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class GetStackReply
    extends Reply
{

    private static final String     MIME_TYPE = "application/octet-stream";
    
    
    private ByteArray   buffer;


    GetStackReply(ByteArray buffer)
    {
        if (buffer == null) throw new NullPointerException("No buffer.");
        this.buffer = buffer;
    }
    
    /* (non-Javadoc)
     * @see Reply#unmarshal(org.apache.commons.httpclient.HttpMethodBase)
     */
    public void unmarshal(HttpMethod response, HttpChannel context)
        throws ImageServiceException, IOException
    {
        //Make sure we actually got a binary stream.
        checkStatusCode(response);
        checkContentType(response, MIME_TYPE);
        
        //Read the stream into buffer.
        readResponseStream(response.getResponseBodyAsStream(), buffer,
                            context.getBlockSize());
    }
    
}
