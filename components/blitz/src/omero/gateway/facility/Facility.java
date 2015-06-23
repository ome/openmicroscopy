/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
package omero.gateway.facility;

import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.media.jai.operator.LogDescriptor;

import ome.conditions.SessionTimeoutException;
import omero.AuthenticationException;
import omero.DatabaseBusyException;
import omero.ResourceError;
import omero.SecurityViolation;
import omero.SessionException;
import omero.gateway.Gateway;
import omero.gateway.exception.ConnectionStatus;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.log.LogMessage;
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

//Java imports

/**
 * A Facility encapsulates a certain set of functionality for dealing with an
 * OMERO server
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public abstract class Facility {
    /** Holds references to the different facilities so that they can be reused */
    private static final Cache<String, Facility> cache = CacheBuilder
            .newBuilder().build();

    /** Reference to the {@link Gateway} */
    final Gateway gateway;

    /**
     * Creates a new instance
     * 
     * @param gateway
     *            Reference to the {@link Gateway}
     */
    Facility(Gateway gateway) {
        this.gateway = gateway;
    }

    /**
     * Get a reference to a certain Facility
     * 
     * @param type
     *            The type of the Facility
     * @param gateway
     *            Reference to the {@link Gateway}
     * @return See above
     * @throws ExecutionException
     */
    @SuppressWarnings("unchecked")
    public static <T extends Facility> T getFacility(final Class<T> type,
            final Gateway gateway) throws ExecutionException {

        return (T) cache.get(type.getSimpleName(), new Callable<Facility>() {

            @Override
            public Facility call() throws Exception {
                gateway.getLogger().debug(null, "Created new "+type.getSimpleName());
                return type.getDeclaredConstructor(Gateway.class).newInstance(
                        gateway);
            }

        });
    }

    /**
     * Helper method to simplify logging
     * 
     * @param originator
     *            The source of the log message
     * @param msg
     *            The message
     * @param t
     *            The exception
     */
    public void logDebug(Object originator, String msg, Throwable t) {
        gateway.getLogger().debug(originator, new LogMessage(msg, t));
    }

    /**
     * Helper method to simplify logging
     * 
     * @param originator
     *            The source of the log message
     * @param msg
     *            The message
     * @param t
     *            The exception
     */
    public void logInfo(Object originator, String msg, Throwable t) {
        gateway.getLogger().info(originator, new LogMessage(msg, t));
    }

    /**
     * Helper method to simplify logging
     * 
     * @param originator
     *            The source of the log message
     * @param msg
     *            The message
     * @param t
     *            The exception
     */
    public void logWarn(Object originator, String msg, Throwable t) {
        gateway.getLogger().warn(originator, new LogMessage(msg, t));
    }

    /**
     * Helper method to simplify logging
     * 
     * @param originator
     *            The source of the log message
     * @param msg
     *            The message
     * @param t
     *            The exception
     */
    public void logError(Object originator, String msg, Throwable t) {
        gateway.getLogger().error(originator, new LogMessage(msg, t));
    }

    /**
     * Helper method to handle exceptions thrown by the connection library.
     * Methods in this class are required to fill in a meaningful context
     * message. This method is not supposed to be used in this class'
     * constructor or in the login/logout methods.
     *
     * @param t
     *            The exception.
     * @param message
     *            The context message.
     * @throws DSOutOfServiceException
     *             A connection problem.
     * @throws DSAccessException
     *             A server-side error.
     */
    void handleException(Object originator, Throwable t, String message)
            throws DSOutOfServiceException, DSAccessException {
        logError(originator, message, t);

        ConnectionStatus b = getConnectionStatus(t);
        if (b != ConnectionStatus.OK)
            return;
        if (!gateway.isConnected())
            return;
        Throwable cause = t.getCause();
        if (cause instanceof SecurityViolation) {
            String s = "For security reasons, cannot access data. \n";
            throw new DSAccessException(s + message, cause);
        } else if (cause instanceof SessionException) {
            String s = "Session is not valid. \n";
            throw new DSOutOfServiceException(s + message, cause);
        } else if (cause instanceof AuthenticationException) {
            String s = "Cannot initialize the session. \n";
            throw new DSOutOfServiceException(s + message, cause);
        } else if (cause instanceof ResourceError) {
            String s = "Fatal error. Please contact the administrator. \n";
            throw new DSOutOfServiceException(s + message, t);
        }
        throw new DSAccessException("Cannot access data. \n" + message, t);
    }

    /**
     * Returns one of the constants defined by this class or <code>-1</code>.
     * 
     * @param e
     *            The exception to handle.
     * @return See above.
     */
    private ConnectionStatus getConnectionStatus(Throwable e) {
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
            return ConnectionStatus.LOST_CONNECTION;
        else if (cause instanceof CommunicatorDestroyedException
                || e instanceof CommunicatorDestroyedException)
            return ConnectionStatus.DESTROYED_CONNECTION;
        else if (cause instanceof SocketException
                || e instanceof SocketException
                || e instanceof UnknownHostException)
            return ConnectionStatus.NETWORK;
        else if (cause instanceof ConnectionRefusedException
                || e instanceof ConnectionRefusedException
                || cause instanceof ConnectionTimeoutException
                || e instanceof ConnectionTimeoutException
                || cause instanceof DatabaseBusyException
                || e instanceof DatabaseBusyException
                || e instanceof CannotCreateSessionException
                || cause instanceof CannotCreateSessionException)
            return ConnectionStatus.SERVER_OUT_OF_SERVICE;
        else if (cause instanceof UnknownException)
            return handleIceUnknownException(cause);
        else if (e instanceof UnknownException)
            return handleIceUnknownException(e);
        return ConnectionStatus.OK;
    }

    /**
     * Handles the <code>Ice.UnknownException</code>. Returns the index
     * depending on the unknown message.
     * 
     * @param e
     *            The exception to handle.
     * @return See above.
     */
    private ConnectionStatus handleIceUnknownException(Throwable e) {
        UnknownException ex = (UnknownException) e;
        if (ex.unknown.contains("Ice::ConnectionRefusedException"))
            return ConnectionStatus.SERVER_OUT_OF_SERVICE;
        else if (ex.unknown.contains("Ice::ConnectionLostException"))
            return ConnectionStatus.LOST_CONNECTION;
        return ConnectionStatus.OK;
    }
}
