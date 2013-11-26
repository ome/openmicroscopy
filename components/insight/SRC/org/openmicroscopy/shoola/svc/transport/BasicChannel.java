/*
 * org.openmicroscopy.shoola.svc.transport.BasicChannel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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

//Java imports
import java.util.Arrays;

//Third-party libraries
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

//Application-internal dependencies

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
        if (StringUtils.isNotBlank(proxyHost) &&
                StringUtils.isNotBlank(proxyPort)) {
            builder.setProxy(new HttpHost(proxyHost,
                    Integer.parseInt(proxyPort)));
        }
        CloseableHttpClient httpclient = HttpClients.custom()
            .setDefaultRequestConfig(builder.build())
            .build();
        return httpclient;
    }

    /**
     * Returns the requested path.
     * @see HttpChannel#getRequestPath()
     */
    protected String getRequestPath() { return requestPath; }

}
