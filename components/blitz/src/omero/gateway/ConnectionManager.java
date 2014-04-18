/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.gateway;

import static omero.gateway.util.GatewayUtils.printErrorText;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import omero.AuthenticationException;
import omero.ResourceError;
import omero.SecurityViolation;
import omero.SessionException;
import omero.client;
import omero.api.IAdminPrx;
import omero.api.ServiceFactoryPrx;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.exception.VersionMismatchException;
import omero.gateway.model.SecurityContext;
import omero.gateway.model.UserCredentials;
import omero.gateway.util.ConnectionExceptionHandler;
import omero.gateway.util.ConnectionStatus;
import omero.gateway.util.NetworkChecker;
import omero.model.ExperimenterGroupI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.util.PojoMapper;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

/**
 * Manages all the connection related stuff for the Gateway:
 * Session creation, keep alive, etc.
 * 
 * Should not be instantiated directly, therefore abstract.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public abstract class ConnectionManager {

    private Logger log = LoggerFactory.getLogger(ConnectionManager.class
            .getName());

    private NetworkChecker networkChecker;

    private boolean encrypted = true;

    private boolean connected = false;

    private int elapseTime = -1;

    private UserCredentials userCredentials;

    /**
     * Flag used during reconnecting process if a connection failure occurred.
     */
    private final AtomicBoolean reconnecting = new AtomicBoolean(false);

    /** Tells whether or not the network is up. */
    private final AtomicBoolean networkup = new AtomicBoolean(true);

    /** The collection of connectors. */
    private ListMultimap<Long, Connector> groupConnectorMap = Multimaps
            .<Long, Connector> synchronizedListMultimap(LinkedListMultimap
                    .<Long, Connector> create());

    private ScheduledThreadPoolExecutor keepAliveExecutor;

    /**
     * Tries to connect to <i>OMERO</i> and log in by using the supplied
     * credentials.
     * 
     * @param userName
     *            The user name to be used for login.
     * @param password
     *            The password to be used for login.
     * @param hostName
     *            The name of the server.
     * @param compression
     *            The compression level used for images and thumbnails depending
     *            on the connection speed.
     * @param groupID
     *            The id of the group or <code>-1</code>.
     * @param encrypted
     *            Pass <code>true</code> to encrypt data transfer,
     *            <code>false</code> otherwise.
     * @param agentName
     *            The name to register with the server.
     * @param port
     *            The port to use.
     * @return The user's details.
     * @throws DSOutOfServiceException
     *             If the connection can't be established or the credentials are
     *             invalid.
     * @see #getUserDetails(String) TODO: could be refactored to return a
     *      Connector for later use in login()
     */
    private client createSession(UserCredentials uc, String agentName,
            String clientVersion, boolean skipVersionCheck)
            throws DSOutOfServiceException, VersionMismatchException {
        this.encrypted = uc.isEncrypted();
        this.userCredentials = uc;
        client secureClient = null;
        String serverVersion = null;
        try {
            // client must be cleaned up by caller.
            if (uc.getPort() > 0)
                secureClient = new client(uc.getHostName(), uc.getPort());
            else
                secureClient = new client(uc.getHostName());
            secureClient.setAgent(agentName);
            ServiceFactoryPrx entryEncrypted = secureClient.createSession(
                    uc.getUserName(), uc.getPassword());

            serverVersion = entryEncrypted.getConfigService().getVersion();

            String ip = null;
            try {
                ip = InetAddress.getByName(uc.getHostName()).getHostAddress();
            } catch (Exception e) {
                log("Failed to get inet address: " + uc.getHostName());
            }

            networkChecker = new NetworkChecker(ip);
        } catch (Throwable e) {
            if (secureClient != null) {
                secureClient.__del__();
            }
            connected = false;
            String s = "Can't connect to OMERO. OMERO info not valid.\n\n";
            s += printErrorText(e);
            throw new DSOutOfServiceException(s, e);
        }

        if (!skipVersionCheck) {
            checkClientServerCompatibility(serverVersion, clientVersion);
        }

        Runnable r = new Runnable() {
            public void run() {

                try {
                    keepSessionAlive();
                } catch (Throwable t) {
                    String message = "Exception while keeping the services alive.\n";
                    message += printErrorText(t);
                    log.debug(message);
                    handleConnectionException(t);
                }
            }
        };
        keepAliveExecutor = new ScheduledThreadPoolExecutor(1);
        keepAliveExecutor.scheduleWithFixedDelay(r, 60, 60, TimeUnit.SECONDS);

        return secureClient;
    }

    public ExperimenterData connect(UserCredentials uc, String agentName,
            String clientVersion, float compression)
            throws DSOutOfServiceException, VersionMismatchException {
        return connect(uc, agentName, clientVersion, compression, false);
    }

    public ExperimenterData connect(UserCredentials uc, String agentName,
            String clientVersion, float compression, boolean skipVersionCheck)
            throws DSOutOfServiceException, VersionMismatchException {
        client client = createSession(uc, agentName, clientVersion,
                skipVersionCheck);
        return login(client, uc.getUserName(), uc.getHostName(), compression,
                uc.getGroup(), uc.getPort());
    }

    /**
     * Tries to connect to <i>OMERO</i> and log in by using the supplied
     * credentials. The <code>createSession</code> method must be invoked
     * before.
     * 
     * @param userName
     *            The user name to be used for login.
     * @param secureClient
     *            Reference to the client
     * @param hostName
     *            The name of the server.
     * @param compression
     *            The compression level used for images and thumbnails depending
     *            on the connection speed.
     * @param groupID
     *            The id of the group or <code>-1</code>.
     * @param encrypted
     *            Pass <code>true</code> to encrypt data transfer,
     *            <code>false</code> otherwise.
     * @param agentName
     *            The name to register with the server.
     * @param port
     *            The port to use
     * @return The user's details.
     * @throws DSOutOfServiceException
     *             If the connection can't be established or the credentials are
     *             invalid.
     * @see #createSession(String, String, String, long, boolean, String)
     */
    private ExperimenterData login(client secureClient, String userName,
            String hostName, float compression, long groupID, int port)
            throws DSOutOfServiceException {
        Connector connector = null;
        SecurityContext ctx = null;
        try {
            connected = true;
            ServiceFactoryPrx entryEncrypted = secureClient.getSession();
            IAdminPrx prx = entryEncrypted.getAdminService();
            ExperimenterData exp = (ExperimenterData) PojoMapper
                    .asDataObject(prx.lookupExperimenter(userName));
            if (groupID >= 0) {
                long defaultID = -1;
                try {
                    defaultID = exp.getDefaultGroup().getId();
                } catch (Exception e) {
                }
                ctx = new SecurityContext(defaultID);
                ctx.setServerInformation(hostName, port);
                ctx.setCompression(compression);
                connector = new Connector(ctx, secureClient, entryEncrypted,
                        encrypted, elapseTime);
                groupConnectorMap.put(ctx.getGroupID(), connector);
                if (defaultID == groupID)
                    return exp;
                try {
                    changeCurrentGroup(ctx, exp, groupID);
                    ctx = new SecurityContext(groupID);
                    ctx.setServerInformation(hostName, port);
                    ctx.setCompression(compression);
                    connector = new Connector(ctx, secureClient,
                            entryEncrypted, encrypted, elapseTime);
                    exp = getUserDetails(ctx, userName, true);
                    groupConnectorMap.put(ctx.getGroupID(), connector);
                } catch (Exception e) {
                    String msg = "Error while changing group.\n";
                    msg += printErrorText(e);
                    log(msg);
                }
            }
            // Connector now controls the secureClient for closing.
            ctx = new SecurityContext(exp.getDefaultGroup().getId());
            ctx.setServerInformation(hostName, port);
            ctx.setCompression(compression);
            connector = new Connector(ctx, secureClient, entryEncrypted,
                    encrypted, elapseTime);
            groupConnectorMap.put(ctx.getGroupID(), connector);
            return exp;
        } catch (Throwable e) {
            connected = false;
            String s = "Cannot log in. User credentials not valid.\n\n";
            s += printErrorText(e);
            throw new DSOutOfServiceException(s, e);
        }
    }

    /** Logs out. */
    public void disconnect() {
        try {
            isNetworkUp(false); // Force re-check to prevent hang.
        } catch (Exception e) {
            // ignore already registered.
        }
        connected = false;
        List<Connector> connectors = getAllConnectors();
        Iterator<Connector> i = connectors.iterator();
        while (i.hasNext())
            i.next().shutDownServices(true);
        i = connectors.iterator();
        while (i.hasNext()) {
            try {
                i.next().close(networkup.get());
            } catch (Throwable e) {
                log("Cannot close connector: " + printErrorText(e));
            }
        }
        groupConnectorMap.clear();
        keepAliveExecutor.shutdown();
    }

    public boolean joinSession() {
        log("joinSession ");
        try {
            isNetworkUp(false); // Force re-check to prevent hang
        } catch (Exception e) {
            // no need to handle the exception.
        }
        boolean networkup = this.networkup.get(); // our copy
        connected = false;
        if (!networkup) {
            log("Network is down");
            return false;
        }
        List<Connector> connectors = removeAllConnectors();
        Iterator<Connector> i = connectors.iterator();
        Connector c;
        int index = 0;
        while (i.hasNext()) {
            c = i.next();
            try {
                log("joining the session ");
                c.joinSession();
                groupConnectorMap.put(c.getGroupID(), c);
            } catch (Throwable t) {
                log("Failed to join the session " + printErrorText(t));
                // failed to join so we create a new one, first we shut down
                try {
                    c.shutDownServices(true);
                    c.close(networkup);
                } catch (Throwable e) {
                    log("Failed to close the session " + printErrorText(e));
                }
                if (!groupConnectorMap.containsKey(c.getGroupID())) {
                    try {
                        createConnector(new SecurityContext(c.getGroupID()),
                                false);
                    } catch (Exception e) {
                        log("Failed to create connector " + printErrorText(e));
                        index++;
                    }
                }
            }
        }
        connected = index == 0;
        reconnecting.set(true);
        return connected;
    }

    /**
     * Changes the default group of the currently logged in user.
     * 
     * @param ctx
     *            The security context.
     * @param exp
     *            The experimenter to handle
     * @param groupID
     *            The id of the group.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in.
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public void changeCurrentGroup(SecurityContext ctx, ExperimenterData exp,
            long groupID) throws DSOutOfServiceException, DSAccessException {
        List<GroupData> groups = exp.getGroups();
        Iterator<GroupData> i = groups.iterator();
        GroupData group = null;
        boolean in = false;
        while (i.hasNext()) {
            group = i.next();
            if (group.getId() == groupID) {
                in = true;
                break;
            }
        }
        if (in) {
            Connector c = getConnector(ctx, true, false);
            try {
                IAdminPrx svc = c.getAdminService();
                svc.setDefaultGroup(exp.asExperimenter(),
                        new ExperimenterGroupI(groupID, false));
            } catch (Exception e) {
                handleException(e, "Can't modify the current group for user:"
                        + exp.getId());
            }
        }

        String s = "Can't modify the current group.\n\n";
        if (!in) {
            throw new DSOutOfServiceException(s);
        }
    }

    /**
     * Retrieves the details on the current user and maps the result calling
     * {@link PojoMapper#asDataObjects(Map)}.
     * 
     * @param ctx
     *            The security context.
     * @param name
     *            The user's name.
     * @param connectionError
     *            Pass <code>true</code> to handle the connection error,
     *            <code>false</code> otherwise.
     * @return The {@link ExperimenterData} of the current user.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in.
     * @see IPojosPrx#getUserDetails(Set, Map)
     */
    public ExperimenterData getUserDetails(SecurityContext ctx, String name,
            boolean connectionError) throws DSOutOfServiceException,
            DSAccessException {
        Connector c = getConnector(ctx, true, false);
        try {
            IAdminPrx service = c.getAdminService();
            return (ExperimenterData) PojoMapper.asDataObject(service
                    .lookupExperimenter(name));
        } catch (Exception e) {
            if (connectionError)
                handleConnectionException(e);
            throw new DSOutOfServiceException("Cannot retrieve user's data "
                    + printErrorText(e), e);
        }
    }

    Connector getConnector(SecurityContext ctx)
            throws DSOutOfServiceException {
        return getConnector(ctx, false, false);
    }

    /**
     * Returns the connector corresponding to the passed context.
     * 
     * @param ctx
     *            The security context.
     * @param recreate
     *            whether or not to allow the recreation of the
     *            {@link Connector}. A {@link DSOutOfServiceException} is thrown
     *            if this is set to false and no {@link Connector} is available.
     * @param permitNull
     *            whether or not to throw a {@link DSOutOfServiceException} if
     *            no {@link Connector} is available by the end of the execution.
     * @return
     */
    Connector getConnector(SecurityContext ctx, boolean recreate,
            boolean permitNull) throws DSOutOfServiceException {
        try {
            isNetworkUp(true); // Need safe version?
        } catch (Exception e1) {
            if (permitNull) {
                log("Failed to check network. Returning null connector");
                return null;
            }
            throw new DSOutOfServiceException(null, ConnectionStatus.NETWORK);
        }

        if (!networkup.get()) {
            if (permitNull) {
                log("Network down. Returning null connector");
                return null;
            }
            throw new DSOutOfServiceException(null, ConnectionStatus.NETWORK);
        }

        if (ctx == null) {
            if (permitNull) {
                log("Null SecurityContext. Returning null connector");
                return null;
            }
            throw new DSOutOfServiceException("Null SecurityContext");
        }

        Connector c = null;
        List<Connector> clist = groupConnectorMap.get(ctx.getGroupID());
        if (clist.size() > 0) {
            c = clist.get(0);
            if (c.needsKeepAlive()) {
                // Check if network is up before keeping service otherwise
                // we block until timeout.
                try {
                    isNetworkUp(false);
                } catch (Exception e) {
                    throw new DSOutOfServiceException(null,
                            ConnectionStatus.NETWORK);
                }
                if (!c.keepSessionAlive()) {
                    throw new DSOutOfServiceException(null,
                            ConnectionStatus.LOST_CONNECTION);
                }
            }
            return c;
        }

        // We are going to create a connector and activate a session.
        if (!recreate) {
            if (permitNull) {
                log("Cannot re-create. Returning null connector");
                return null;
            }
            throw new DSOutOfServiceException("Not allowed to recreate");
        }
        return createConnector(ctx, permitNull);
    }

    List<Connector> getAllConnectors() {
        synchronized (groupConnectorMap) {
            // This should be the only location which calls values().
            return new ArrayList<Connector>(groupConnectorMap.values());
        }
    }

    List<Connector> removeAllConnectors() {
        synchronized (groupConnectorMap) {
            // This should be the only location which calls values().
            List<Connector> rv = new ArrayList<Connector>(
                    groupConnectorMap.values());
            groupConnectorMap.clear();
            return rv;
        }
    }

    private Connector createConnector(SecurityContext ctx, boolean permitNull)
            throws DSOutOfServiceException {
        Connector c = null;
        try {
            ctx.setServerInformation(userCredentials.getHostName(),
                    userCredentials.getPort());
            // client will be cleaned up by connector
            client client = new client(userCredentials.getHostName(),
                    userCredentials.getPort());
            ServiceFactoryPrx prx = client.createSession(
                    userCredentials.getUserName(),
                    userCredentials.getPassword());
            prx.setSecurityContext(new ExperimenterGroupI(ctx.getGroupID(),
                    false));
            c = new Connector(ctx, client, prx, encrypted, elapseTime);
            groupConnectorMap.put(ctx.getGroupID(), c);
        } catch (Throwable e) {
            // TODO: This previously was via handleException??
            if (!permitNull) {
                throw new DSOutOfServiceException("Failed to create connector",
                        e);
            }
        }
        return c;
    }

    /**
     * Logs the information.
     */
    private void log(String msg) {
        log.debug(msg);
    }

    /**
     * Helper method to handle exceptions thrown by the connection library.
     * Methods in this class are required to fill in a meaningful context
     * message. This method is not supposed to be used in this class'
     * constructor or in the login/logout methods.
     * 
     * @param t
     *            The exception.
     * @param message
     *            The context message.
     * @throws DSOutOfServiceException
     *             A connection problem.
     * @throws DSAccessException
     *             A server-side error.
     */
    void handleException(Throwable t, String message)
            throws DSOutOfServiceException, DSAccessException {
        boolean b = handleConnectionException(t);
        if (!b)
            return;
        if (!connected)
            return;
        Throwable cause = t.getCause();
        if (cause instanceof SecurityViolation) {
            String s = "For security reasons, cannot access data. \n";
            throw new DSAccessException(s + message, cause);
        } else if (cause instanceof SessionException) {
            String s = "Session is not valid. \n";
            throw new DSOutOfServiceException(s + message, cause);
        } else if (cause instanceof AuthenticationException) {
            String s = "Cannot initialize the session. \n";
            throw new DSOutOfServiceException(s + message, cause);
        } else if (cause instanceof ResourceError) {
            String s = "Fatal error. Please contact the administrator. \n";
            throw new DSOutOfServiceException(s + message, t);
        }
        throw new DSAccessException("Cannot access data. \n" + message, t);
    }

    /**
     * Helper method to handle exceptions thrown by the connection library.
     * Depending on the specified exception, the user will be asked to reconnect
     * or to exit the application.
     * 
     * @param e
     *            The exception to handle.
     * @return <code>true</code> to continue handling the error,
     *         <code>false</code> otherwise.
     */
    boolean handleConnectionException(Throwable e) {
        if (reconnecting.get()) {
            reconnecting.set(false);
            return false;
        }
        // if (networkup) return true;
        ConnectionExceptionHandler handler = new ConnectionExceptionHandler();
        ConnectionStatus index = handler.handleConnectionException(e);
        if (index == ConnectionStatus.OK)
            return true;
        return false;
    }

    /** Keeps the services alive. */
    private void keepSessionAlive() throws DSOutOfServiceException {
        // Check if network is up before keeping service otherwise
        // we block until timeout.
        try {
            isNetworkUp(false);
        } catch (Exception e) {
            throw new DSOutOfServiceException(null, ConnectionStatus.NETWORK);
        }
        Iterator<Connector> i = getAllConnectors().iterator();
        Connector c;
        while (i.hasNext()) {
            c = i.next();
            if (c.needsKeepAlive()) {
                if (!c.keepSessionAlive()) {
                    throw new DSOutOfServiceException(null,
                            ConnectionStatus.LOST_CONNECTION);
                }
            }
        }
    }

    /**
     * Checks if the client's version is compatible to the server
     * @param server The server version
     * @param client The client version
     * @throws VersionMismatchException
     */
    private void checkClientServerCompatibility(String server, String client)
            throws VersionMismatchException {
        if (server == null || client == null)
            throw new VersionMismatchException(
                    "No version information provided");

        if (client.startsWith("@"))
            return;

        if (server.contains("-"))
            server = server.split("-")[0];
        if (client.contains("-"))
            client = client.split("-")[0];
        String[] values = server.split("\\.");
        String[] valuesClient = client.split("\\.");
        if (values.length < 2 || valuesClient.length < 2)
            throw new VersionMismatchException(
                    "Invalid version information provided");
        ;
        int s1, s2, c1, c2;
        try {
            s1 = Integer.parseInt(values[0]);
            s2 = Integer.parseInt(values[1]);
            c1 = Integer.parseInt(valuesClient[0]);
            c2 = Integer.parseInt(valuesClient[1]);
        } catch (Exception e) {
            String msg = "Client server compatibility";
            msg += "\n" + printErrorText(e);
            log.debug(msg);
            throw new VersionMismatchException("Could not check versions");
        }

        // TODO: This would allow a 4.1.0 client to connect to a 5.1.0 server, but not a 5.0.0 client to a 5.1.0 server. Is this really intended? 
        if (s1 < c1)
            throw new VersionMismatchException(client, server);
        if (s2 != c2)
            throw new VersionMismatchException(client, server);
    }

    /**
     * Checks if the network is up.
     * 
     * @throws Exception
     *             Throw
     */
    public void isNetworkUp(boolean useCachedValue) throws Exception {
        try {
            if (networkChecker != null)
                networkup.set(networkChecker.isNetworkup(useCachedValue));
        } catch (Throwable t) {
            log("Error on isNetworkUp check:" + t);
            networkup.set(false);
        }
    }
    
    /**
     * Manually set network status
     * 
     * @throws Exception
     *             Throw
     */
    public void setNetworkUp(boolean value) {
          networkup.set(value);
    }

    /**
     * Returns <code>true</code> if the server is running.
     * 
     * @param ctx
     *            The security context.
     * @return See above.
     */
    public boolean isServerRunning(SecurityContext ctx) {
        if (!isConnected())
            return false;
        try {
            return null != getConnector(ctx, true, true);
        } catch (Throwable t) {
            return false;
        }
    }
    
    public boolean isConnected() {
        return this.connected;
    }

    public boolean isAvailable() throws Exception {
        if (networkChecker == null) {
            return false;
        } else {
            return networkChecker.isAvailable();
        }
    }
}
