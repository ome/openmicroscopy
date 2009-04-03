/*
 * org.openmicroscopy.shoola.omeis.transport.HttpChannel
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

package org.openmicroscopy.shoola.omeis.transport;


//Java imports
import java.io.IOException;

//Third-party libraries
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;

//Application-internal dependencies
import org.openmicroscopy.shoola.omeis.proxy.Reply;
import org.openmicroscopy.shoola.omeis.proxy.Request;
import org.openmicroscopy.shoola.omeis.services.ImageServiceException;

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
public abstract class HttpChannel
{

    public static final int DEFAULT = 0;
    public static final int CONNECTION_PER_REQUEST = 1;
    
    
    protected abstract HttpClient getCommunicationLink();
    protected abstract String getRequestPath();
    public abstract int getBlockSize();
    
    public void exchange(Request out, Reply in) 
        throws ImageServiceException, IOException
    {
        //Sanity checks.
        if (out == null) throw new NullPointerException("No request.");
        if (in == null) throw new NullPointerException("No reply.");
        
        //Build HTTP request, send it, and wait for response.
        //Then read the response into the Reply object.
        HttpClient comLink = getCommunicationLink();
        HttpMethodBase method = null;
        try {
            method = out.marshal();
            method.setPath(getRequestPath());
            comLink.executeMethod(method);
            in.unmarshal(method, this);
        } finally {
            //Required by Http Client library.
            if (method != null) method.releaseConnection();
        }
    }
    
}
