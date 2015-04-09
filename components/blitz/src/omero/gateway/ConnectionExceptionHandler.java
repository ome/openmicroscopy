/*
 * org.openmicroscopy.shoola.env.data.ConnectionExceptionHandler 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omero.gateway;

//Java imports
import java.net.UnknownHostException;

//Application-internal dependencies
import ome.conditions.SessionTimeoutException;
import omero.DatabaseBusyException;
//Third-party libraries
import Glacier2.CannotCreateSessionException;
import Ice.CommunicatorDestroyedException;
import Ice.ConnectionLostException;
import Ice.ConnectionRefusedException;
import Ice.ConnectionTimeoutException;
import Ice.DNSException;
import Ice.ObjectNotExistException;
import Ice.SocketException;
import Ice.TimeoutException;
import Ice.UnknownException;

/**
 * Handles the connection exceptions
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class ConnectionExceptionHandler {
    /** String identifying the connection refused exception. */
    private static final String REFUSED = "Ice::ConnectionRefusedException";

    /** String identifying the connection lost exception. */
    private static final String LOST = "Ice::ConnectionLostException";

    /**
     * Handles the <code>Ice.UnknownException</code>. Returns the index
     * depending on the unknown message.
     * 
     * @param e
     *            The exception to handle.
     * @return See above.
     */
    private ConnectionStatus handleIceUnknownException(Throwable e) {
        ConnectionStatus index = ConnectionStatus.UNKNOWN;
        UnknownException ex = (UnknownException) e;
        if (ex.unknown.contains(REFUSED))
            index = ConnectionStatus.SERVER_OUT_OF_SERVICE;
        else if (ex.unknown.contains(LOST))
            index = ConnectionStatus.LOST_CONNECTION;
        return index;
    }

    /**
     * Returns one of the constants defined by this class or <code>-1</code>.
     * 
     * @param e
     *            The exception to handle.
     * @return See above.
     */
    public ConnectionStatus handleConnectionException(Throwable e) {
        ConnectionStatus index = ConnectionStatus.UNKNOWN;
        Throwable cause = e.getCause();
        if (cause instanceof ConnectionLostException
                || e instanceof ConnectionLostException
                || cause instanceof SessionTimeoutException
                || e instanceof SessionTimeoutException
                || cause instanceof TimeoutException
                || e instanceof TimeoutException
                || cause instanceof ObjectNotExistException
                || e instanceof ObjectNotExistException
                || cause instanceof DNSException || e instanceof DNSException)
            index = ConnectionStatus.LOST_CONNECTION;
        else if (cause instanceof CommunicatorDestroyedException
                || e instanceof CommunicatorDestroyedException)
            index = ConnectionStatus.DESTROYED_CONNECTION;
        else if (cause instanceof SocketException
                || e instanceof SocketException
                || e instanceof UnknownHostException)
            index = ConnectionStatus.NETWORK;
        else if (cause instanceof ConnectionRefusedException
                || e instanceof ConnectionRefusedException
                || cause instanceof ConnectionTimeoutException
                || e instanceof ConnectionTimeoutException
                || cause instanceof DatabaseBusyException
                || e instanceof DatabaseBusyException
                || e instanceof CannotCreateSessionException
                || cause instanceof CannotCreateSessionException)
            index = ConnectionStatus.SERVER_OUT_OF_SERVICE;
        else if (cause instanceof UnknownException)
            index = handleIceUnknownException(cause);
        else if (e instanceof UnknownException)
            index = handleIceUnknownException(e);
        return index;
    }

}
