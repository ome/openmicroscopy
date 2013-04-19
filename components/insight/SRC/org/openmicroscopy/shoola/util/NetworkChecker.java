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
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;

//Third-party libraries
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;


/** 
 * Checks if the network is still up.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class NetworkChecker {

    private final static Logger log = LoggerFactory.getLogger(NetworkChecker.class);

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
		//Code work for 1.6. only. Tmp solution for now
		/*
		boolean networkup = false;
		Enumeration<NetworkInterface> interfaces =
				NetworkInterface.getNetworkInterfaces();
		NetworkInterface ni;
		if (interfaces != null) {
			while (interfaces.hasMoreElements()) {
				ni = interfaces.nextElement();
				if (ni.isUp() && !ni.isLoopback()) {
					networkup = true;
					break;
				}
			}
		}
		if (!networkup) {
			throw new UnknownHostException("Network is down.");
		}
		return networkup;
		*/
		//tmp code
		boolean networkup = false;
		if (UIUtilities.isLinuxOS()) {
			if (log.isDebugEnabled()) {
				log.debug("LinuxOS - checking network connection [HTTP]");
			}

			try {
				// use HTTP URL instead of plain socket connection to avoid
				// network checks timeouts for clients behind a web proxy
				// (requires adequate system property in startup script)
				URL url = new URL("http://www.openmicroscopy.org.uk");
				InputStream is = url.openStream();
				is.close();
				networkup = true;
			} catch (Exception e) {}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Win/MacOS - checking network connection [NIC]");
			}

			Enumeration<NetworkInterface> interfaces =
					NetworkInterface.getNetworkInterfaces();
			if (interfaces != null) {
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
								//if (!ia.isSiteLocalAddress()) {
								if (!ia.getHostName().equals(
										ia.getHostAddress())) {
									networkup = true;
									break;
								}
								//}
							}
						}
						if (networkup) break;
					}
				}
			}
		}
		if (!networkup) {
			throw new UnknownHostException("Network is down.");
		}
		return networkup;
	}
}
