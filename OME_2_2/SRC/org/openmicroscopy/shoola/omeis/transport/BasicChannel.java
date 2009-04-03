/*
 * org.openmicroscopy.shoola.omeis.transport.BasicChannel
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

//Third-party libraries
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

//Application-internal dependencies

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
class BasicChannel
    extends HttpChannel
{

    static final int    DEF_CONN_TIMEOUT = 10000;
    static final int    DEF_BLOCK_SIZE = 64*1024;
    
    
    private final URI       omeisURL;
    private final String    requestPath;
    private final int       connTimeout;
    private final int       blockSize;
    
    
    BasicChannel(String url, int connTimeout, int blockSize) 
        throws IllegalArgumentException
    {
        try {
            omeisURL = new URI(url);
            requestPath = omeisURL.getPath();
        } catch (URIException e) {
            throw new IllegalArgumentException(
                    "Invalid URL to OMEIS: "+url+".");
        }
        this.connTimeout = (connTimeout < 0 ? DEF_CONN_TIMEOUT : connTimeout);
        this.blockSize = (blockSize <= 0 ? DEF_BLOCK_SIZE : blockSize);
    }
    
    /* (non-Javadoc)
     * @see HttpChannel#getCommunicationLink()
     */
    protected HttpClient getCommunicationLink()
    {
        HostConfiguration cfg = new HostConfiguration();
        cfg.setHost(omeisURL);
        HttpClient channel = new HttpClient();
        channel.setHostConfiguration(cfg);
        channel.setConnectionTimeout(connTimeout);
        return channel;
    }
    /* NB: In order to enforce a connection per request model, the same channel
     * may not be shared by concurrent invocations of the exchange method.  For
     * this reason we create a new HttpClient everytime -- using a synch object
     * would imply serializing all requests, thus defeating any potential
     * benefit of concurrency.  Note that HttpClient can be configured with a 
     * thread-safe connection pool (see docs), but in this case connections are
     * recycled and possibly waited on, so we wouldn't have a connection per 
     * request if we were to use the HttpClient built-in capabilities. 
     */

    /* (non-Javadoc)
     * @see HttpChannel#getRequestPath()
     */
    protected String getRequestPath()
    {
        return requestPath;
    }
    
    public int getBlockSize()
    {
        return blockSize;
    }

}
