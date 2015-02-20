/*
 * org.openmicroscopy.shoola.svc.transport.BasicChannel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.svc.transport;

import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

/** 
 * Creates a basic <code>HttpChannel</code>.
 * In order to enforce a connection per request model, the same channel
 * may not be shared by concurrent invocations of the exchange method.  For
 * this reason we create a new HttpClient every time -- using a synch object
 * would imply serializing all requests, thus defeating any potential
 * benefit of concurrency.  Note that HttpClient can be configured with a
 * thread-safe connection pool (see documents), but in this case connections are
 * recycled and possibly waited on, so we wouldn't have a connection per 
 * request if we were to use the HttpClient built-in capabilities.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class BasicChannel
    extends HttpChannel
{

    /** The default value for the time out. */
    static final int DEF_CONN_TIMEOUT = 10000;

    /** The requested path. */
    private final String requestPath;

    /** The time before being disconnected. */
    private final int connTimeout;

    /**
     * Creates a connection.
     * 
     * @return See above
     * @throws TransportException Thrown if an error occurred while creating the
     *                            SSL context.
     */
    private SSLConnectionSocketFactory createSSLConnection()
        throws TransportException
    {
        SSLContext sslcontext = SSLContexts.createSystemDefault();
        final TrustManager trustEverything = new X509TrustManager() {
            private final X509Certificate[] 
                    acceptedIssuers = new X509Certificate[0];

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {}

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {}

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return this.acceptedIssuers;
            }
        };
        
        TrustManager[] managers = {trustEverything};
        try {
            sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, managers, null);
        } catch (Exception e) {
            new TransportException("Cannot create security context", e);
        }
        return new SSLConnectionSocketFactory(sslcontext,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param url The server's URL.
     * @param connTimeout The time before being disconnected.
     * @throws IllegalArgumentException If the specified URL is not valid.
     */
    BasicChannel(String url, int connTimeout)
            throws IllegalArgumentException
    {
        requestPath = url;
        this.connTimeout = (connTimeout < 0 ? DEF_CONN_TIMEOUT : connTimeout);
    }

    /**
     * Creates a <code>HttpClient</code> to communicate.
     * @see HttpChannel#getCommunicationLink()
     */
    protected CloseableHttpClient getCommunicationLink()
            throws TransportException
    {
        //Default connection configuration
        RequestConfig.Builder builder = RequestConfig.custom();
        builder.setCookieSpec(CookieSpecs.BEST_MATCH)
                .setExpectContinueEnabled(true)
                .setStaleConnectionCheckEnabled(true)
                .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM,
                        AuthSchemes.DIGEST))
                .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC));
        builder.setConnectTimeout(connTimeout);
        String proxyHost = System.getProperty(HttpChannel.PROXY_HOST);
        String proxyPort = System.getProperty(HttpChannel.PROXY_PORT);
        if (CommonsLangUtils.isNotBlank(proxyHost) &&
                CommonsLangUtils.isNotBlank(proxyPort)) {
            builder.setProxy(new HttpHost(proxyHost,
                    Integer.parseInt(proxyPort)));
        }
        HttpClientBuilder httpBuilder = HttpClients.custom();
        httpBuilder.setDefaultRequestConfig(builder.build());
        String value = requestPath;
        if (value.toLowerCase().startsWith("https"))
            httpBuilder.setSSLSocketFactory(createSSLConnection());
        return httpBuilder.build();
    }

    /**
     * Returns the requested path.
     * @see HttpChannel#getRequestPath()
     */
    protected String getRequestPath() { return requestPath; }

}
