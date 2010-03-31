/*
 * ome.formats.importer.gui.History
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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
package ome.formats.importer.util;

import ome.formats.OMEROMetadataStoreClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A {@link Runnable} which keeps a {@link Connector}'s server side resources
 * from timing out. <b>NOTE:</b> Upon catching an exception, the 
 * <code>Connector</code> is logged out.
 * 
 * @author Chris Allan <callan@glencoesoftware.com>
 *
 */
public class ClientKeepAlive implements Runnable
{
    /** Logger for this class. */
    private static Log log = LogFactory.getLog(ClientKeepAlive.class);
    
    /** The connector we're trying to keep alive. */
    private OMEROMetadataStoreClient client;
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        try
        {
            synchronized (client) {
                if (client != null)
                {
                    client.ping();
                }
            }
        }
        catch (Throwable t)
        {
            log.error(
                "Exception while executing ping(), logging Connector out: ", t);
            try {
                client.logout();
            } catch (Exception e) {
                log.error("Nested error on client.logout() " +
                		"while handling exception from ping()", e);
            }
        }
    }

    
    /**
     * @return OMEROMetadataStoreClient
     */
    public OMEROMetadataStoreClient getClient()
    {
        synchronized (client) {
            return client;
        }
    }

    
    /**
     * @param client - OMEROMetadataStoreClient to set
     */
    public void setClient(OMEROMetadataStoreClient client)
    {
        synchronized (client) {
            this.client = client;
        }
    }
}