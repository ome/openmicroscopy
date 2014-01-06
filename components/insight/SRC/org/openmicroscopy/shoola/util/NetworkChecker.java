/*
 * org.openmicroscopy.shoola.env.data.NetworkChecker
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
package org.openmicroscopy.shoola.util;

//Java imports
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

//Third-party libraries

//Application-internal dependencies


/**
 * Checks if the network is still up.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
@SuppressWarnings("unchecked")
public class NetworkChecker {

    private final AtomicLong lastCheck = new AtomicLong(System.currentTimeMillis());

    private final AtomicBoolean lastValue = new AtomicBoolean(true);

    /**
     * The IP Address of the server the client is connected to
     * or <code>null</code>.
     */
    private InetAddress ipAddress;

    /** The list of interfaces when the network checker is initialized.*/
    private long interfacesCount;
    
    /** Creates a new instance.*/
    public NetworkChecker()
    {
        this(null);
    }

    /**
     * Creates a new instance.
     *
     * @param ipAddress The IP address of the server the client is connected to
     * or <code>null</code>.
     */
    public NetworkChecker(String ipAddress)
    {
        if (ipAddress != null) {
            try {
                this.ipAddress = InetAddress.getByName(ipAddress);
            } catch (UnknownHostException e) {
                // Ignored
            }
        }
        interfacesCount = 0;
        try {
            Enumeration<NetworkInterface> interfaces =
                    NetworkInterface.getNetworkInterfaces();
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
     * Returns <code>true</code> if the network is still up, otherwise
     * throws an <code>UnknownHostException</code>.
     *
     * @return See above.
     * @throws Exception Thrown if the network is down.
     */
    public boolean isNetworkup(boolean useCachedValue)
            throws Exception
    {
        if (useCachedValue) {
            long elapsed = System.currentTimeMillis() - lastCheck.get();
            if (elapsed <= 5000) {
                boolean last = lastValue.get();
                log("Cached networkup: %s", last);
                return last;
            }
        }
        long start = System.currentTimeMillis();
        boolean newValue = _isNetworkup();
        long stop = System.currentTimeMillis();
        long elapsed = stop - start;
        log("Network status: %s (in %s ms.) use cached value: %s", newValue,
                elapsed, useCachedValue);
        lastValue.set(newValue);
        lastCheck.set(stop);
        return newValue;
     }

    /**
     * Returns <code>true</code> if the network is still up, otherwise
     * throws an <code>UnknownHostException</code>.
     *
     * @return See above.
     * @throws Exception Thrown if the network is down.
     */
    private boolean _isNetworkup()
            throws Exception
    {
        if (ipAddress != null && ipAddress.isLoopbackAddress()) {
            return true;
        }
        boolean networkup = false;
        Enumeration<NetworkInterface> interfaces =
                NetworkInterface.getNetworkInterfaces();
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
        log("Count: %s  %s", count, interfacesCount);
        if (count >= interfacesCount) {
            networkup = true;
        }
        if (!networkup) {
            throw new UnknownHostException("Network is down.");
        }
        return networkup;
    }

    static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(NetworkChecker.class.getName());

    /**
     * Logs the error.
     * 
     * @param msg The message to log
     * @param objs The objects to add to the message.
     */
    void log(String msg, Object...objs) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format(msg, objs));
        }
    }

    /**
     * Testing main that allows for the usage of the NetworkChecker
     * without running the entire software stack.
     */
    public static void main(String[] args) throws Exception {
        String address = args.length == 1? args[0] : "null";
        runTest(new NetworkChecker(address), address);
    }

    /**
     * Runs the tests.
     *
     * @param nc The network checker.
     * @param address The address to check.
     */
    private static void runTest(NetworkChecker nc, String address)
    {
        try {
            int i = 0;
            while (i < 1) {
                System.err.println("Using explicit server address: " + address);
                System.err.println("Java version: " + System.getProperty("java.version"));
                System.err.println("isNetworkup()?: " + nc.isNetworkup(false));
                try {
                    Thread.sleep(5L*1000L);
                } catch(Exception ex) {}
                i++;
            }
        } catch (Exception e) {
            System.err.println("isNetworkup()?: " + false);
        }
    }

}
