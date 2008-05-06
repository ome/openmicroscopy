/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.system;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Contacts a given URL which should be an OME server which will return either
 * an empty String or a URL which points to a needed upgrade.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2.3
 */
public class UpgradeCheck implements Runnable {

    private final static Log log = LogFactory.getLog(UpgradeCheck.class);

    /**
     * Default timeout is 10 seconds.
     */
    public final static int DEFAULT_TIMEOUT = 10 * 1000;

    final String url;
    final String version;
    final int timeout;
    final String agent;

    String upgradeUrl = null;
    int status = 0;
    Exception exc = null;

    /**
     * Calls {@link UpgradeCheck#UpgradeCheck(String, String, String, int)}
     * using {@link #DEFAULT_TIMEOUT}
     */
    public UpgradeCheck(String url, String version, String agent) {
        this(url, version, agent, DEFAULT_TIMEOUT);
    }

    /**
     * Main constructor.
     * 
     * @param url
     *            Null or empty value disables check.
     * @param version
     *            Current version as specified in the omero.properties file
     *            under the "omero.version" property. This can be accessed via
     *            <code>
     *            ResourceBundle.getBundle("omero").getString("omero.version");
     *            </code>
     * @param agent
     *            Name of the agent which is accessing the registry. This will
     *            be appended to "OMERO." in order to adhere to the registry
     *            API.
     * @param timeout
     *            How long to wait for a
     */
    public UpgradeCheck(String url, String version, String agent, int timeout) {
        this.url = url;
        this.version = version;
        this.agent = "OMERO." + agent;
        this.timeout = timeout;
    }

    public boolean isUpgradeNeeded() {
        return upgradeUrl != null;
    }

    public String getUpgradeUrl() {
        return upgradeUrl;
    }

    public boolean isExceptionThrown() {
        return exc != null;
    }

    public Exception getExceptionThrown() {
        return exc;
    }

    private void set(String results, Exception e) {
        this.upgradeUrl = results;
        this.exc = e;
    }

    /**
     * If the {@link #url} has been set to null or the empty string, then no
     * upgrade check will be performed (silently). If however the string is an
     * invalid URL, a warning will be printed.
     * 
     * This method should <em>never</em> throw an exception.
     */
    public void run() {

        // If null or empty, the upgrade check is disabled.
        if (url == null || url.length() == 0) {
            return; // EARLY EXIT!
        }

        StringBuilder query = new StringBuilder();
        try {
            query.append(url);
            query.append("?version=");
            query.append(URLEncoder.encode(version, "UTF-8"));
            query.append(";os.name=");
            query.append(URLEncoder.encode(System.getProperty("os.name"),
                    "UTF-8"));
            query.append(";os.arch=");
            query.append(URLEncoder.encode(System.getProperty("os.arch"),
                    "UTF-8"));
            query.append(";os.version=");
            query.append(URLEncoder.encode(System.getProperty("os.version"),
                    "UTF-8"));
            query.append(";java.runtime.version=");
            query.append(URLEncoder.encode(System
                    .getProperty("java.runtime.version"), "UTF-8"));
            query.append(";java.vm.vendor=");
            query.append(URLEncoder.encode(
                    System.getProperty("java.vm.vendor"), "UTF-8"));
        } catch (UnsupportedEncodingException uee) {
            // Internal issue
            set(null, uee);
            return;
        }

        URL _url;
        try {
            _url = new URL(query.toString());
        } catch (Exception e) {
            set(null, e);
            log.error("Invalid URL: " + query.toString());
            return;
        }

        try {
            URLConnection conn = _url.openConnection();
            conn.setUseCaches(false);
            conn.addRequestProperty("User-Agent", agent);
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            conn.connect();

            log.debug("Attempting to connect to " + query);

            InputStream in = conn.getInputStream();
            BufferedInputStream bufIn = new BufferedInputStream(in);

            StringBuilder sb = new StringBuilder();
            while (true) {
                int data = bufIn.read();
                if (data == -1) {
                    break;
                } else {
                    sb.append((char) data);
                }
            }
            String result = sb.toString();
            if (result.length() == 0) {
                log.info("no update needed");
                set(null, null);
            } else {
                log.warn("UPGRADE AVAILABLE:" + result);
                set(result, null);
            }
        } catch (UnknownHostException uhe) {
            log.error("Unknown host:" + url);
            set(null, uhe);
        } catch (IOException ioe) {
            log.error(String.format("Error reading from url: %s \"%s\"", query,
                    ioe.getMessage()));
            set(null, ioe);
        } catch (Exception ex) {
            log.error("Unknown exception thrown on UpgradeCheck", ex);
            set(null, ex);
        }
    }
}
