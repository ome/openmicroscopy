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
package org.openmicroscopy.shoola.env.data;


//Java imports
import java.net.UnknownHostException;

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

//Application-internal dependencies
import ome.conditions.SessionTimeoutException;
import omero.DatabaseBusyException;
import omero.gateway.exception.ConnectionStatus;
import omero.gateway.exception.DSOutOfServiceException;

/**
 * Handles the connection exceptions
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class ConnectionExceptionHandler
{

    /** Indicates that the connection has been lost. */
    public static final int LOST_CONNECTION = 0;

    /** Indicates that the server is out of service. */
    public static final int SERVER_OUT_OF_SERVICE = 1;

    /** Indicates that the server is out of service. */
    public static final int DESTROYED_CONNECTION = 2;

    /** Indicates that the network is down. */
    public static final int NETWORK = 3;

    /** String identifying the connection refused exception.*/
    private static final String REFUSED = "Ice::ConnectionRefusedException";

    /** String identifying the connection lost exception.*/
    private static final String LOST = "Ice::ConnectionLostException";

    /**
     * Handles the <code>Ice.UnknownException</code>.
     * Returns the index depending on the unknown message.
     * 
     * @param e The exception to handle.
     * @return See above.
     */
    private int handleIceUnknownException(Throwable e)
    {
        int index = -1;
        UnknownException ex = (UnknownException) e;
        if (ex.unknown.contains(REFUSED))
            index = SERVER_OUT_OF_SERVICE;
        else if (ex.unknown.contains(LOST))
            index = LOST_CONNECTION;
        return index;
    }

    /**
     * Returns one of the constants defined by this class or <code>-1</code>.
     * 
     * @param e The exception to handle.
     * @return See above.
     */
    public int handleConnectionException(Throwable e)
    {
        int index = -1;
        Throwable cause = e.getCause();
        ConnectionStatus cs = null;
        if (e instanceof DSOutOfServiceException) {
            DSOutOfServiceException dso = (DSOutOfServiceException)e;
            cs = dso.getConnectionStatus();
        }
        if (cause instanceof ConnectionLostException ||
                e instanceof ConnectionLostException ||
                cause instanceof SessionTimeoutException ||
                e instanceof SessionTimeoutException ||
                cause instanceof TimeoutException ||
                e instanceof TimeoutException ||
                cause instanceof ObjectNotExistException ||
                e instanceof ObjectNotExistException ||
                cause instanceof DNSException ||
                e instanceof DNSException ||
                cs == ConnectionStatus.LOST_CONNECTION)
            index = LOST_CONNECTION;
        else if (cause instanceof CommunicatorDestroyedException ||
                e instanceof CommunicatorDestroyedException)
            index = DESTROYED_CONNECTION;
        else if (cause instanceof SocketException ||
                e instanceof SocketException ||
                e instanceof UnknownHostException ||
                cs == ConnectionStatus.NETWORK)
            index = NETWORK;
        else if (cause instanceof ConnectionRefusedException ||
                e instanceof ConnectionRefusedException ||
                cause instanceof ConnectionTimeoutException ||
                e instanceof ConnectionTimeoutException ||
                cause instanceof DatabaseBusyException ||
                e instanceof DatabaseBusyException ||
                e instanceof CannotCreateSessionException ||
                cause instanceof CannotCreateSessionException)
            index = SERVER_OUT_OF_SERVICE;
        else if (cause instanceof UnknownException)
            index = handleIceUnknownException(cause);
        else if (e instanceof UnknownException)
            index = handleIceUnknownException(e);
        return index;
    }

}
