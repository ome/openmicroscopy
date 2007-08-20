/*
 *    $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Wrapper around a call to 'glacier2router' to permit Java control over the
 * process.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class Router {

	private final static String LOCALHOST = "127.0.0.1";

	private final static Log log = LogFactory.getLog("OMERO.router");

	Process p = null;

	private final Map<String, String> map = new HashMap<String, String>();
	{
		map.put("Glacier2.InstanceName", "OMEROGlacier2");
		setClientEndpoints(LOCALHOST, 9998);
		setSessionManager(LOCALHOST, 9999);
		setPermissionsVerifier(LOCALHOST, 9999);
		setTimeout(600);
	}

	public void setClientEndpoints(String host, int port) {
		map.put("Glacier2.Client.Endpoints", "tcp -p " + port + " -h " + host);
	}

	public void setServerEndpoints(String host, int port) {
		map.put("Glacier2.Server.Endpoints", "tcp -h " + host + " -p " + port);
	}

	public void setSessionManager(String host, int port) {
		map.put("Glacier2.SessionManager", "Manager:tcp -h " + host + " -p "
				+ port);
	}

	public void setPermissionsVerifier(String host, int port) {
		map.put("Glacier2.PermissionsVerifier", "Verifier:tcp -h " + host
				+ " -p " + port);
	}

	public void setTimeout(int timeout) {
		map.put("Glacier2.SessionTimeout", "" + timeout);
	}

	public void tryStart(int n) {

		if (n == 0) {
			throw new RuntimeException("No tries left.");
		}

		start();
		if (!running()) {
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				// ok.
			}
			log.info(n + " more tries.");
			tryStart(--n);
		}
	}

	public void start() {
		List<String> list = new ArrayList<String>();
		list.add(getBashPath());
		for (String string : map.keySet()) {
			list.add("--" + string + "=" + map.get(string));
		}
		log.info(list);
		ProcessBuilder pb = new ProcessBuilder((String[]) list
				.toArray(new String[list.size()]));
		try {
			p = pb.start();
		} catch (Exception e) {
			log.info("Failed to start", e);
		}
	}

	public boolean running() {
		if (p == null)
			return false;
		try {
			p.exitValue();
		} catch (IllegalThreadStateException e) {
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the {@link Process} was active, and therefore
	 * an attempt was made to call {@link Process#destroy()}.
	 * @return
	 */
	public boolean stop() {
		boolean active = running();
		if (active) {
			p.destroy();
		}
		return active;
	}

	String getBashPath() {
		ProcessBuilder pb = new ProcessBuilder("bash", "-l","-c", 
				"which glacier2router");
		String path;
		try {
			Process p = pb.start();
			StringBuilder sb = new StringBuilder();
			InputStream is = p.getInputStream();
			int c;
			while ((c = is.read()) != -1) {
				sb.append((char) c);
			}
			path = sb.toString().trim();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return path;
	}

}
