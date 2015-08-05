/*
 * org.openmicroscopy.shoola.env.data.NetworkChecker
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee & Open Microscopy Environment.
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
package omero.gateway.util;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import omero.log.Logger;

/**
 * Checks if the network is still up.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class NetworkChecker {

    private final AtomicLong lastCheck = new AtomicLong(
            System.currentTimeMillis());

    private final AtomicBoolean lastValue = new AtomicBoolean(true);

    /**
     * The IP Address of the server the client is connected to or
     * <code>null</code>.
     */
    private InetAddress ipAddress;

    /** The address of the server to reach. */
    private final String address;

    /** Reference to the logger. */
    private Logger logger;

    /** The list of interfaces when the network checker is initialized. */
    private long interfacesCount;

    /**
     * Creates a new instance.
     *
     * @param address
     *            The address of the server the client is connected to or
     *            <code>null</code>.
     * @param logger Reference to the logger.
     */
    public NetworkChecker(String address, Logger logger) {
        this.address = address;
        this.logger = logger;
        if (ipAddress != null) {
            try {
                this.ipAddress = InetAddress.getByName(address);
            } catch (UnknownHostException e) {
                // Ignored
            }
        }
        interfacesCount = 0;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface
                    .getNetworkInterfaces();
            if (interfaces != null) {
                NetworkInterface ni;
                while (interfaces.hasMoreElements()) {
                    ni = interfaces.nextElement();
                    if (ni.isLoopback() || !ni.isUp())
                        continue;
                    interfacesCount++;
                }
            }
        } catch (Exception e) {
            // Ignored
        }
    }

    /**
     * Returns <code>true</code> if the network is still up, otherwise throws an
     * <code>UnknownHostException</code>. This tests if the adapter is ready.
     *
     * @param useCachedValue Pass <code>true</code> if we use the cached value,
     *                       <code>false</code> otherwise.
     * @return See above.
     * @throws Exception
     *             Thrown if the network is down.
     */
    public boolean isNetworkup(boolean useCachedValue) throws Exception {
        if (useCachedValue) {
            long elapsed = System.currentTimeMillis() - lastCheck.get();
            if (elapsed <= 5000) {
                return lastValue.get();
            }
        }
        boolean newValue = _isNetworkup();
        long stop = System.currentTimeMillis();
        lastValue.set(newValue);
        lastCheck.set(stop);
        return newValue;
    }

    /**
     * Checks the network is available or not.
     *
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred if we cannot reach.
     */
    public boolean isAvailable() throws Exception {
        if (ipAddress != null && ipAddress.isLoopbackAddress()) {
            return true;
        }
        if (address != null) {
            try {
                URL url = new URL("http://" + address);
                HttpURLConnection urlConnect = (HttpURLConnection) url
                        .openConnection();
                urlConnect.setConnectTimeout(1000);
                urlConnect.getContent();
            } catch (Exception e) {
                log("Not available %s", e);
                return false;
            }
        }
        return true;
    }

    /**
     * Returns <code>true</code> if the network is still up, otherwise throws an
     * <code>UnknownHostException</code>.
     *
     * @return See above.
     * @throws Exception
     *             Thrown if the network is down.
     */
    private boolean _isNetworkup() throws Exception {
        if (ipAddress != null && ipAddress.isLoopbackAddress()) {
            return true;
        }
        boolean networkup = false;
        Enumeration<NetworkInterface> interfaces = NetworkInterface
                .getNetworkInterfaces();
        long count = 0;
        if (interfaces != null && !networkup) {
            NetworkInterface ni;
            while (interfaces.hasMoreElements()) {
                ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp())
                    continue;
                count++;
            }
        }
        if (count >= interfacesCount) {
            networkup = true;
        } else {
            networkup = isAvailable();
            if (networkup) { // one interface was dropped e.g. wireless
                interfacesCount = count;
            }
        }
        if (!networkup) {
            throw new UnknownHostException("Network is down.");
        }
        return networkup;
    }

    /**
     * Logs the error.
     *
     * @param msg
     *            The message to log
     * @param objs
     *            The objects to add to the message.
     */
    void log(String msg, Object... objs) {
        if (logger != null) {
            logger.debug(this, String.format(msg, objs));
        }
    }

}
