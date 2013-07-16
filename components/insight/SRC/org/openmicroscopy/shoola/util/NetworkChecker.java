/*
 * org.openmicroscopy.shoola.env.data.NetworkChecker
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
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
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
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

	@SuppressWarnings("rawtypes")
	static private Class NetworkInterfaceClass = null;
	static private Method getNetworkInterfacesMethod = null;
	static private Method isUpMethod = null;
	static private Method isLoopbackMethod = null;
	static private boolean useReflectiveCheck = false;

	static {
		//
		// Perform static lookup via reflection of the methods
		// needed to run Java 6 network checks.
		//
		try {
			NetworkInterfaceClass = Class.forName("java.net.NetworkInterface");
			getNetworkInterfacesMethod = NetworkInterfaceClass.getMethod("getNetworkInterfaces");
			isUpMethod = NetworkInterfaceClass.getMethod("isUp");
			isLoopbackMethod = NetworkInterfaceClass.getMethod("isLoopback");
			useReflectiveCheck = true;
		} catch (ClassNotFoundException e) {
			// Knowingly using System.err since 1) this will be primarily used on
			// Linux in the first instance and 2) we don't have access to a logger.
			System.err.println("NetworkInterface class not found: assuming Java 1.5");
		} catch (SecurityException e) {
			// This should not happen. Logging (at ERROR)
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// This should not happen. Logging (at ERROR)
			e.printStackTrace();
		}
	}

    //
    // WARNING: these would usually not be static fields, but due to
    // the number of created NetworkCheckers, sharing the state among
    // all of them will be substantially quicker than letting each
    // make its own call, even if cached.
    //

    private static final AtomicLong lastCheck = new AtomicLong(0);

    private static final AtomicBoolean lastValue = new AtomicBoolean(true);

	/**
	 * The IP Address of the server the client is connected to
	 * or <code>null</code>.
	 */
	private InetAddress ipAddress;
	
	/** Creates a new instance.
	 */
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
	}

	/**
	 * Run the standard Java 1.6 check using reflection. If this is not 1.6 or later, then
	 * exit successfully, printing this assumption to System.err. If any odd reflection error
	 * occurs, then act as if the network is up, even though we don't know. Finally, if the
	 * reflection code works properly, throw an {@link UnknownHostException} if no up, non-loopback
	 * network is found.
	 *
	 * @throws UnknownHostException
	 */
	@SuppressWarnings({ "rawtypes"})
	public boolean reflectiveCheck() throws UnknownHostException {

		if (!useReflectiveCheck) {
			return true;
		}

		try {
			Enumeration interfaces = (Enumeration) getNetworkInterfacesMethod.invoke(null);
			if (interfaces != null) {
				while (interfaces.hasMoreElements()) {
					Object ni = interfaces.nextElement();
					Boolean isUp = (Boolean) isUpMethod.invoke(ni);
					Boolean isLoopback = (Boolean) isLoopbackMethod.invoke(ni);
					if (isUp != null && isLoopback != null && (isUp && !isLoopback)) {
						// TODO: add logging here for the successful check.
						return true;
					}
				}
			}
		} catch (Exception e) {
			// This should not happen. It likely implies that something has happened in
			// our reflection code, since no checked exceptions are thrown from the 1.6 code.
			System.err.println("Failed during reflection. Assuming network is up.");
			e.printStackTrace();
			return true;
		}

		// If we reach here and no exception has been thrown then we assume that
		// there is no network.
		return false;
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
        log("Network status: %s (in %s ms.)", newValue, elapsed);
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
    public boolean isNetworkup()
        throws Exception
    {
        return isNetworkup(true);
    }

	public boolean _isNetworkup()
		throws Exception
	{

		boolean networkup = false;
		List<String> ips = new ArrayList<String>();
		if (useReflectiveCheck) {
			// On Java 1.6+, reflectiveCheck will perform a proper check.
			networkup = reflectiveCheck();
		} else {
			Enumeration<NetworkInterface> interfaces =
					NetworkInterface.getNetworkInterfaces();
			if (interfaces != null && !networkup) {
				NetworkInterface ni;
				InetAddress ia;
				while (interfaces.hasMoreElements()) {
					ni = interfaces.nextElement();
					Enumeration<InetAddress> e = ni.getInetAddresses();
					if (!ni.getDisplayName().startsWith("lo")) {
						while (e.hasMoreElements()) {
							ia = (InetAddress) e.nextElement();
							if (!ia.isAnyLocalAddress() &&
									!ia.isLoopbackAddress()) {
								if (!ia.getHostName().equals(
										ia.getHostAddress())) {
									networkup = true;
									break;
								}
							}
						}
						if (networkup) break;
					}
				}
			}
		}
		if (!networkup) {
			if (ipAddress != null && ipAddress.isLoopbackAddress()) {
				return true;
			}
			throw new UnknownHostException("Network is down.");
		}
		return networkup;
	}

	static org.apache.log4j.Logger logger = 
	        org.apache.log4j.Logger.getLogger(NetworkChecker.class.getName());

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
		String address = args.length == 1? args[0] : null;
		NetworkChecker nc = new NetworkChecker(address);
		System.err.println("Using explicit server address: " + address);
		System.err.println("Java version: " + System.getProperty("java.version"));
		System.err.println("isNetworkup()?: " + nc.isNetworkup());
	}
}
