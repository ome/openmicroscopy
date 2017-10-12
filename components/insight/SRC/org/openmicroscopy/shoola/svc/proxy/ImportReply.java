package org.openmicroscopy.shoola.svc.proxy;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.openmicroscopy.shoola.svc.transport.HttpChannel;
import org.openmicroscopy.shoola.svc.transport.TransportException;

/**
 * Checks the outcome of an offline import submission, throwing an exception
 * if the server returned an error.
 */
public class ImportReply extends Reply {

    private void error(String reason) throws TransportException {
        throw new TransportException(
                "Failed to submit files for offline import: " + reason);
    }

    private int status(CloseableHttpResponse response)
            throws TransportException {
        try {
            return response.getStatusLine().getStatusCode();
        } catch (NullPointerException e) {
            error("no server response");
            return 500;  // never reached but keeps compiler happy.
        }
    }

    @Override
    public void unmarshal(CloseableHttpResponse response, HttpChannel context)
            throws TransportException {
        if (status(response) >= 300) {
            error("invalid import submission");
        }
    }

}
