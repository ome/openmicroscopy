/*
 * ome.system.Server
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.system;

// Java imports
import java.util.Properties;

// Third-party libraries

// Application-internal dependencies
import ome.conditions.ApiUsageException;

/**
 * Provides simplified handling of server properties when creating a
 * {@link ome.system.ServiceFactory}. For more complicated uses,
 * {@link java.util.Properties} can also be used. In which case, the constant
 * {@link java.lang.String strings} provided in this class can be used as the
 * keys to the {@link java.util.Properties properties instance} passed to
 * {@link ome.system.ServiceFactory#ServiceFactory(Properties)}.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * @see ome.system.ServiceFactory
 * @since 1.0
 */
public class Server {

    /**
     * Java property name for use in configuration of the client connection.
     */
    public final static String OMERO_HOST = "server.host";

    /**
     * Java property name for use in configuration of the client connection.
     */
    public final static String OMERO_PORT = "server.port";

    public final static int DEFAULT_PORT = 1099;

    private String _server, _port;

    // Need at least user and password
    private Server() {
    }

    /**
     * standard constructor which users {@link #DEFAULT_PORT}.
     * 
     * @param serverHost
     *            Not null.
     */
    public Server(String serverHost) {
        if (serverHost == null) {
            throw new ApiUsageException("serverHost argument "
                    + "to Server constructor cannot be null");
        }
        _server = serverHost;
        _port = Integer.toString(DEFAULT_PORT);
    }

    /**
     * extended constructor. As with {@link #Server(String)}, serverHost may
     * not be null.
     * 
     * @param serverHost
     *            Not null.
     * @param port
     */
    public Server(String serverHost, int port) {
        this(serverHost);
        if (port < 0) {
            throw new ApiUsageException("serverPort may not be null.");
        }
        _port = Integer.toString(port);
    }

    // ~ Views
    // =========================================================================

    /**
     * produces a copy of the internal fields as a {@link java.util.Properties}
     * instance. Only those keys are present for which a field is non-null.
     * 
     * @return Properties. Not null.
     */
    public Properties asProperties() {
        Properties p = new Properties();
        p.setProperty(OMERO_HOST, _server);
        p.setProperty(OMERO_PORT, _port);
        return p;
    }

    /**
     * simple getter for the server host passed into the constructor
     * 
     * @return host name Not null.
     */
    public String getHost() {
        return _server;
    }

    /**
     * simple getter for the port passed into the constructor or the default
     * port if none.
     */
    public int getPort() {
        return Integer.valueOf(_port);
    }

}
