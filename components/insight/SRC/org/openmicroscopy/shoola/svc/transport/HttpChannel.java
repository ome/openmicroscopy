/*
 * org.openmicroscopy.shoola.svc.transport.HttpChannel 
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
import java.io.IOException;


//Third-party libraries
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;

//Application-internal dependencies
import org.openmicroscopy.shoola.svc.proxy.Reply;
import org.openmicroscopy.shoola.svc.proxy.Request;

/** 
 * Top-channel that all channels using <code>HTTP</code> communication should 
 * extend.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public abstract class HttpChannel
{

    /** The default channel type. */
    public static final int DEFAULT = 0;

    /** The type identifying that one channel is created per request. */
    public static final int CONNECTION_PER_REQUEST = 1;

    /** Default property key for proxy host. */
    static final String PROXY_HOST = "http.proxyHost";

    /** Default property key for proxy port. */
    static final String PROXY_PORT = "http.proxyPort";

    /** 
     * Returns the channel corresponding to the passed type.
     * 
     * @return See above.
     * @throws TransportException If an error occurred while creating the
     *                            client.
     */ 
    protected abstract CloseableHttpClient getCommunicationLink()
            throws TransportException;

    /**
     * Returns the path derived from the server's URL.
     * 
     * @return See above.
     */
    protected abstract String getRequestPath();

    /**
     * Posts the request and catches the reply.
     * 
     * @param out The request to post.
     * @param in The reply to fill.
     * @throws TransportException If an error occurred while transferring data.
     * @throws IOException	If an error occurred while unmarshalling the method.
     */
    public void exchange(Request out, Reply in) 
            throws TransportException, IOException
    {
        //Sanity checks.
        if (out == null) throw new NullPointerException("No request.");
        if (in == null) throw new NullPointerException("No reply.");

        //Build HTTP request, send it, and wait for response.
        //Then read the response into the Reply object.
        CloseableHttpClient comLink = getCommunicationLink();
        HttpUriRequest method = null;
        try {
            method = out.marshal(getRequestPath());
            CloseableHttpResponse response = comLink.execute(method);
            in.unmarshal(response, this);
        } finally {
            if (comLink != null) comLink.close();
        }
    }

}
