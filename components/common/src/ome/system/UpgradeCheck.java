/*
 *   $Id$
 *
 *   Copyright 2007-2013 Glencoe Software, Inc. All rights reserved.
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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import ome.util.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contacts a given URL which should be an OME server which will return either
 * an empty String or a URL which points to a needed upgrade.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2.3
 */
public class UpgradeCheck implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(UpgradeCheck.class);

    /**
     * Default timeout is 10 seconds.
     */
    public final static int DEFAULT_TIMEOUT = 10 * 1000;

    private static final HostnameVerifier ACCEPT_ANY_HOSTNAME = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }};

    private static final SSLSocketFactory TRUSTING_SSL_SOCKET_FACTORY;

    static {
        final TrustManager trustEverything = new X509TrustManager() {
            private final X509Certificate[] acceptedIssuers = new X509Certificate[0];

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) { }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) { }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return this.acceptedIssuers;
            }
        };

        SSLContext trustingContext = null;

        try {
            trustingContext = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException e) {
            // http://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#SSLContext
        }

        try {
            trustingContext.init(null, new TrustManager[]{trustEverything}, null);
        } catch (KeyManagementException e) {
            // uses standard key manager
        }

        TRUSTING_SSL_SOCKET_FACTORY = trustingContext.getSocketFactory();
    }

    final String url;
    final String version;
    final int timeout;
    final String agent;

    String upgradeUrl = null;
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
     *            <code>IConfig.getVersion() // 4.0.0</code>
     *            or
     *            <code>IConfig.getConfigValue("omero.version") // [optional-]4.0.0[-optional]</code>
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

        BufferedInputStream bufIn = null;
        try {
            URLConnection conn = _url.openConnection();
            conn.setUseCaches(false);
            conn.addRequestProperty("User-Agent", agent);
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            if (conn instanceof HttpsURLConnection) {
                final HttpsURLConnection httpsConnection = (HttpsURLConnection) conn;
                httpsConnection.setHostnameVerifier(ACCEPT_ANY_HOSTNAME);
                httpsConnection.setSSLSocketFactory(TRUSTING_SSL_SOCKET_FACTORY);
            }
            conn.connect();

            log.debug("Attempting to connect to " + query);

            InputStream in = conn.getInputStream();
            bufIn = new BufferedInputStream(in);

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
        } finally {
            Utils.closeQuietly(bufIn);
        }
    }
}
