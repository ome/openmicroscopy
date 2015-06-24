/*
 * org.openmicroscopy.shoola.svc.proxy.Reply 
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
import java.io.InputStreamReader;
import java.io.Reader;


//Third-party libraries
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;

//Application-internal dependencies
import org.openmicroscopy.shoola.svc.transport.HttpChannel;
import org.openmicroscopy.shoola.svc.transport.TransportException;

/** 
 * Top-class that each <code>Reply</code> class should extend.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public abstract class Reply
{

    /**
     * Checks the status of the response.
     * 
     * @param response The response to handle.
     * @return The message from server.
     * @throws TransportException If an error occurred while transferring data.
     */
    protected static String checkStatusCode(CloseableHttpResponse response)
            throws TransportException
    {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            Reader reader = null;
            try {
                reader = new InputStreamReader(entity.getContent());
                char[] buf = new char[32678];
                StringBuilder str = new StringBuilder();
                for (int n; (n = reader.read(buf)) != -1;)
                    str.append(buf, 0, n);
                return str.toString();
            } catch (Exception e) {
                throw new TransportException("Couldn't handle request: "+
                        response.getStatusLine()+".");
            } finally {
                try {
                    if (reader != null) reader.close();
                } catch (Exception ex) {}
            }
        }
        return null;
    }

    /**
     * Unmarshals
     * 
     * @param response The response to handle.
     * @param context The communication link.
     * @throws TransportException If an error occurred while transferring data.
     */
    public abstract void unmarshal(CloseableHttpResponse response,
            HttpChannel context)
            throws TransportException;

}
