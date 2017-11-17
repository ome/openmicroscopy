/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015-2017 University of Dundee. All rights reserved.
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

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

    /** The PropertyChangeSupport */
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
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
     *             If the {@link Facility} can't be retrieved or instantiated
     */
    public static <T extends Facility> T getFacility(final Class<T> type,
            final Gateway gateway) throws ExecutionException {

        if (AutoCloseable.class.isAssignableFrom(type)) {
            // Don't cache closeable (~ stateful) Facilities,
            // just create a new instance and return it.
            try {
                Facility facility = type.getDeclaredConstructor(Gateway.class)
                        .newInstance(gateway);
                for (PropertyChangeListener l : gateway
                        .getPropertyChangeListeners()) {
                    facility.addPropertyChangeListener(l);
                    facility.pcs
                            .firePropertyChange(Gateway.PROP_FACILITY_CREATED,
                                    null, type.getName());
                }
                return (T) facility;
            } catch (Exception e) {
                throw new ExecutionException("Can't instantiate "
                        + type.getSimpleName(), e);
            }
        }
        
        return (T) cache.get(type.getSimpleName(), new Callable<Facility>() {

            @Override
            public Facility call() throws Exception {
                gateway.getLogger().debug(this,
                        "Created new " + type.getSimpleName());
                Facility facility = type.getDeclaredConstructor(Gateway.class).newInstance(
                        gateway);
                for (PropertyChangeListener l : gateway
                        .getPropertyChangeListeners()) {
                    facility.addPropertyChangeListener(l);
                    facility.pcs.firePropertyChange(
                            Gateway.PROP_FACILITY_CREATED, null, type.getName());
                }
                return facility;
            }

        });
    }
    
    /**
     * Clears the Facility object cache
     */
    public static void clear() {
        Facility.cache.invalidateAll();
    }

    /**
     * Adds a {@link PropertyChangeListener}
     * @param listener The listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    /**
     * Removes a {@link PropertyChangeListener} 
     * (Pass <code>null</code> to remove all {@link PropertyChangeListener}s)
     * 
     * @param listener
     *            The listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listener == null) {
            for (PropertyChangeListener l : this.pcs
                    .getPropertyChangeListeners())
                this.pcs.removePropertyChangeListener(l);
        }
        this.pcs.removePropertyChangeListener(listener);
    }
    
    /**
     * Fires a {@link PropertyChangeEvent}
     * 
     * @param event
     *            The PropertyChangeEvent
     */
    public void firePropertyChanged(PropertyChangeEvent event) {
        this.pcs.firePropertyChange(event);
    }

    /**
     * Fires a {@link PropertyChangeEvent}
     * 
     * @param propertyName
     *            The property name
     * @param oldValue
     *            The old value
     * @param newValue
     *            The new value
     * 
     */
    public void firePropertyChanged(String propertyName, Object oldValue,
            Object newValue) {
        this.pcs.firePropertyChange(propertyName, oldValue, newValue);
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
        if (t != null)
            gateway.getLogger().debug(originator, new LogMessage(msg, t));
        else
            gateway.getLogger().debug(originator, msg);
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
        if (t != null)
            gateway.getLogger().info(originator, new LogMessage(msg, t));
        else
            gateway.getLogger().info(originator, msg);
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
        if (t != null)
            gateway.getLogger().warn(originator, new LogMessage(msg, t));
        else
            gateway.getLogger().warn(originator, msg);
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
        if (t != null)
            gateway.getLogger().error(originator, new LogMessage(msg, t));
        else
            gateway.getLogger().error(originator, msg);
    }

    /**
     * Helper method to handle exceptions thrown by the connection library.
     * Methods in this class are required to fill in a meaningful context
     * message. This method is not supposed to be used in this class'
     * constructor or in the login/logout methods.
     * 
     * @param originator
     *            The originator
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
            throw new DSOutOfServiceException("Connection lost.", t);
        if (!gateway.isConnected())
            throw new DSOutOfServiceException("Gateway is disconnected.", t);
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
        if (e instanceof DSOutOfServiceException) {
            DSOutOfServiceException dso = (DSOutOfServiceException) e;
            if (dso.getConnectionStatus() != null)
                return dso.getConnectionStatus();
        }
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
