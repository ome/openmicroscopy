package ome.formats.importer.util;


import ome.formats.OMEROMetadataStoreClient;
import omero.client;

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
            client.logout();
        }
    }

    
    public OMEROMetadataStoreClient getClient()
    {
        synchronized (client) {
            return client;
        }
    }

    
    public void setClient(OMEROMetadataStoreClient client)
    {
        synchronized (client) {
            this.client = client;
        }
    }
}