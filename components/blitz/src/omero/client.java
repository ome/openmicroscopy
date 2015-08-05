/*
 * Copyright (C) 2008-2013 Glencoe Software, Inc. All rights reserved.
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

package omero;

import static omero.rtypes.rlong;
import static omero.rtypes.rstring;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import ome.util.Utils;
import ome.util.checksum.ChecksumProviderFactory;
import ome.util.checksum.ChecksumProviderFactoryImpl;
import ome.util.checksum.ChecksumType;
import omero.api.ClientCallback;
import omero.api.ClientCallbackPrxHelper;
import omero.api.ISessionPrx;
import omero.api.IUpdatePrx;
import omero.api.RawFileStorePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceFactoryPrxHelper;
import omero.api.ServiceInterfacePrx;
import omero.api.StatefulServiceInterface;
import omero.api.StatefulServiceInterfacePrx;
import omero.api._ClientCallbackDisp;
import omero.constants.AGENT;
import omero.constants.IP;
import omero.model.ChecksumAlgorithm;
import omero.model.ChecksumAlgorithmI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.enums.ChecksumAlgorithmSHA1160;
import omero.util.ModelObjectFactoryRegistry;
import omero.util.Resources;
import omero.util.Resources.Entry;
import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;
import Ice.Current;

/**
 * Central client-side blitz entry point. This class uses solely Ice
 * functionality to provide access to blitz (as opposed to also using Spring)
 * and should be in sync with the OmeroPy omero.client class as well as the
 * OmeroCpp omero::client class.
 *
 * In order to more closely map the destructors in Python and C++, this class
 * keeps a {@link #CLIENTS collection} of {@link omero.client} instances, which
 * are destroyed on program termination.
 *
 * Typical usage: <code>
 *   omero.client client = new omero.client();           // Uses ICE_CONFIG
 *   omero.client client = new omero.client(host);       // Defines "omero.host"
 *   omero.client client = new omero.client(host, port); // Defines "omero.host" and "omero.port"
 *</code>
 *
 * More more information, see <a
 * href="http://trac.openmicroscopy.org.uk/ome/wiki/ClientDesign">
 * http://trac.openmicroscopy.org.uk/ome/wiki/ClientDesign </a>
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class client {

    /**
     * A {@link java.util.Collection} of all the {@link omero.client} instances
     * created so that we can guarantee that we at least <i>attempt</i> to shut
     * them down before exiting.
     */
    private final static Set<client> CLIENTS = Collections
            .synchronizedSet(new HashSet<client>());
    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Set<client> clients = new HashSet<client>(CLIENTS);
                for (client client : clients) {
                    try {
                        client.__del__();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    /**
     * See {@link #setAgent(String)}
     */
    private volatile String __agent = "OMERO.java";

    /**
     * See {@link #setIP(String)}
     */
    private volatile String __ip;


    /**
     * Identifier for this client instance. Multiple client uuids may be
     * attached to a single session uuid.
     */
    private volatile String __uuid;

    /**
     * {@link Ice.InitializationData} from the last communicator used to create
     * the {@link #__ic} if nulled after {@link #closeSession()}.
     */
    private volatile Ice.InitializationData __previous;

    /**
     * {@link Ice.ObjectAdapter} containing the {@link ClientCallback} for this
     * instance.
     */
    private volatile Ice.ObjectAdapter __oa;

    /**
     * Single communicator for this {@link omero.client}. Nullness is used as a
     * test of what state the client is in, therefore all access is sychronized
     * by {@link #lock}
     */
    private volatile Ice.Communicator __ic;

    /**
     * Single session for this {@link omero.client}. Nullness is used as a test
     * of what state the client is in, like {@link #__ic}, therefore all access
     * is synchronized by {@link #lock}
     *
     */
    private volatile ServiceFactoryPrx __sf;

    /**
     * Callback object which is linked to this router session.
     */
    private volatile CallbackI __cb;

    /**
     * If non-null, then has access to this client instance and will
     * periodically call
     * {@link ServiceFactoryPrx#keepAlive(omero.api.ServiceInterfacePrx)} in
     * order to keep any session alive. This can be enabled either via the
     * omero.keep_alive configuration property, or by calling the
     * {@link #enableKeepAlive(int)} method. Once enabled, the period cannot be
     * adjusted during a single session.
     */
    private volatile Resources __resources;

    /**
     * Whether or not remote calls are allowed during shutdown.
     * If false (the default), then the instance will try to
     * connect to the server and free any resources. Otherwise,
     * a fastShutdown will take place. The most common reason
     * to perform a fast shutdown is the loss of network
     * connection. Calling "waitForShutdown" on the Ice stack
     * without the proper connection will hang. (See #9673)
     */
    private AtomicBoolean fastShutdown = new AtomicBoolean(false);

    /**
     * @see #isSecure()
     */
    private final boolean insecure;

    // Creation
    // =========================================================================

    private static Properties defaultRouter(String host, int port) {
        Properties p = new Properties();
        p.setProperty("omero.host", host);
        p.setProperty("omero.port", Integer.toString(port));
        return p;
    }

    /**
     * Calls {@link #client(Ice.InitializationData)} with a new
     * {@link Ice.InitializationData}
     */
    public client() {
        this(new Ice.InitializationData());
    }

    /**
     * Creates an {@link Ice.Communicator} from a {@link Ice.InitializationData}
     * instance. Cannot be null.
     */
    public client(Ice.InitializationData id) {
        insecure = false;
        init(id);
    }

    /**
     * Creates an {@link Ice.Communicator} pointing at the given server using
     * the {@link #DEFAULT_PORT}.
     */
    public client(String host) {
        this(defaultRouter(host, omero.constants.GLACIER2PORT.value));
    }

    /**
     * Creates an {@link Ice.Communicator} pointing at the given server with the
     * non-standard port.
     */
    public client(String host, int port) {
        this(defaultRouter(host, port));
    }

    /**
     * Calls {@link #client(String[], Ice.InitializationData)} with a new
     * {@link Ice.InitializationData}
     */
    public client(String[] args) {
        this(args, new Ice.InitializationData());
    }

    /**
     * Creates an {@link Ice.Communicator} from command-line arguments. These
     * are parsed via Ice.Properties.parseIceCommandLineOptions(args) and
     * Ice.Properties.parseCommandLineOptions("omero", args)
     */
    public client(String[] args, Ice.InitializationData id) {
        insecure = false;
        if (id.properties == null) {
            id.properties = Ice.Util.createProperties(args);
        }
        args = id.properties.parseIceCommandLineOptions(args);
        args = id.properties.parseCommandLineOptions("omero", args);
        init(id);
    }

    /**
     * Creates an {@link Ice.Communicator} from multiple files.
     */
    public client(File... files) {
        insecure = false;
        Ice.InitializationData id = new Ice.InitializationData();
        id.properties = Ice.Util.createProperties(new String[] {});
        for (File file : files) {
            id.properties.load(file.getAbsolutePath());
        }
        init(id);
    }

    /**
     * Creates an {@link Ice.Communicator} from a {@link Map} instance. The
     * {@link String} representation of each member is added to the
     * {@link Ice.Properties} under the {@link String} representation of the
     * key.
     */
    public client(Map p) {
        this(p, true);
    }

    private client(Map p, boolean secure) {
        this.insecure = !secure;
        Ice.InitializationData id = new Ice.InitializationData();
        id.properties = Ice.Util.createProperties(new String[] {});
        if (p != null) {
            for (Object key : p.keySet()) {
                id.properties
                        .setProperty(key.toString(), p.get(key).toString());
            }
        }
        init(id);
    }

    private void optionallySetProperty(Ice.InitializationData id, String key, String def) {
        String val = id.properties.getProperty(key);
        if (val == null || val.length() == 0) {
            val = def;
        }
        id.properties.setProperty(key, val);
    }

    /**
     * Initializes the current client via an {@link Ice.InitializationData}
     * instance. This is called by all of the constructors, but may also be
     * called on {@link #createSession(String, String)} if a previous call to
     * {@link #closeSession()} has nulled the {@link Ice.Communicator}
     */
    private void init(Ice.InitializationData id) {

        if (id == null) {
            throw new ClientError("No initialization data provided.");
        }

        if (id.properties == null) {
            id.properties = Ice.Util.createProperties(new String[] {});
        }

        // Strictly necessary for this class to work
        optionallySetProperty(id, "Ice.ImplicitContext", "Shared");
        optionallySetProperty(id, "Ice.ACM.Client", "0");
        optionallySetProperty(id, "Ice.CacheMessageBuffers", "0");
        optionallySetProperty(id, "Ice.RetryIntervals", "-1");
        optionallySetProperty(id, "Ice.Default.EndpointSelection", "Ordered");
        optionallySetProperty(id, "Ice.Default.PreferSecure", "1");
        optionallySetProperty(id, "Ice.Plugin.IceSSL", "IceSSL.PluginFactory");
        optionallySetProperty(id, "IceSSL.Protocols", "tls1");
        optionallySetProperty(id, "IceSSL.Ciphers", "NONE (DH_anon)");
        optionallySetProperty(id, "IceSSL.VerifyPeer", "0");
        optionallySetProperty(id, "omero.block_size", Integer
            .toString(omero.constants.DEFAULTBLOCKSIZE.value));

        // Set the default encoding if this is Ice 3.5 or later
        // and none is set.
        if (Ice.Util.intVersion() >= 30500) {
            optionallySetProperty(id, "Ice.Default.EncodingVersion", "1.0");
        }

        // Setting MessageSizeMax
        optionallySetProperty(id, "Ice.MessageSizeMax", Integer
            .toString(omero.constants.MESSAGESIZEMAX.value));

        // Setting ConnectTimeout
        parseAndSetInt(id, "Ice.Override.ConnectTimeout",
                omero.constants.CONNECTTIMEOUT.value);

        // Set large thread pool max values for all communicators
        for (String x : Arrays.asList("Client", "Server")) {
            String sizemax = id.properties.getProperty(String.format("Ice.ThreadPool.%s.SizeMax", x));
            if (sizemax == null || sizemax.length() == 0) {
                id.properties.setProperty(String.format("Ice.ThreadPool.%s.SizeMax", x), "50");
            }
        }

        // Port, setting to default if not present
        String port = parseAndSetInt(id, "omero.port",
                omero.constants.GLACIER2PORT.value);

        // Default Router, set a default and then replace
        String router = id.properties.getProperty("Ice.Default.Router");
        if (router == null || router.length() == 0) {
            router = omero.constants.DEFAULTROUTER.value;
        }
        String host = id.properties.getPropertyWithDefault("omero.host",
                "<\"omero.host\" not set>");
        router = router.replaceAll("@omero.port@", port);
        router = router.replaceAll("@omero.host@", host);
        id.properties.setProperty("Ice.Default.Router", router);

        // Dump properties
        String dump = id.properties.getProperty("omero.dump");
        if (dump != null && dump.length() > 0) {
            Map<String, String> propertyMap = getPropertyMap(id.properties);
            for (String key : propertyMap.keySet()) {
                System.out.println(String.format("%s=%s", key,
                        propertyMap.get(key)));
            }
        }

        if (__ic != null) {
            throw new ClientError("Client already initialized.");
        }

        try {
            __ic = Ice.Util.initialize(id);
        } catch (Ice.EndpointParseException epe) {
            throw new ClientError("No host specified. " +
                "Use omero.client(HOSTNAME), ICE_CONFIG, or similar.");
        }


        if (__ic == null) {
            throw new ClientError("Improper initialization");
        }

        // Register Object Factories
        new ModelObjectFactoryRegistry().setIceCommunicator(__ic, this);
        new rtypes.RTypeObjectFactoryRegistry().setIceCommunicator(__ic);

        // Define our unique identifer (used during close/detach)
        __uuid = UUID.randomUUID().toString();
        Ice.ImplicitContext ctx = __ic.getImplicitContext();
        if (ctx == null) {
            throw new ClientError("Ice.ImplicitContext not set to Shared");
        }
        ctx.put(omero.constants.CLIENTUUID.value, __uuid);

        // ticket:2951 - sending user group
        String group = id.properties.getPropertyWithDefault("omero.group", "");
        if (group.length() > 0) {
            ctx.put("omero.group", group);
        }

        // Store this instance for cleanup on shutdown.
        CLIENTS.add(this);

    }

    /**
     * Sets the {@link #fastShutdown} flag. By setting this
     * to true, you will prevent proper clean up. This should
     * only be used in the case of network loss (or similar).
     */
    public boolean setFastShutdown(boolean fastShutdown) {
        return this.fastShutdown.getAndSet(fastShutdown);
    }

    /**
     * Sets the {@link omero.model.Session#getUserAgent() user agent} string for
     * this client. Every session creation will be passed this argument. Finding
     * open sesssions with the same agent can be done via
     * {@link omero.api.ISessionPrx#getMyOpenAgentSessions(String)}.
     */
    public void setAgent(String agent) {
        __agent = agent;
    }

    /**
     * Sets the {@link omero.model.Session#getUserIP() user ip} string for
     * this client. Every session creation will be passed this argument. Finding
     * open sesssions with the same agent can be done via
     * {@link omero.api.ISessionPrx#getMyOpenIPSessions(String)}.
     */
    public void setIP(String ip) {
        __ip = ip;
    }

    /**
     * Specifies whether or not this client was created via a call to
     * {@link #createClient(boolean)} with a boolean of false. If insecure, then
     * all remote calls will use the insecure connection defined by the server.
     */
    public boolean isSecure() {
        return !insecure;
    }

    /**
     * Creates a possibly insecure {@link omero.client} instance and calls
     * {@link #joinSession(String)} using the current {@link #getSessionId()
     * session id}. If secure is false, then first the "omero.router.insecure"
     * configuration property is retrieved from the server and used as the value
     * of "Ice.Default.Router" for the new client. Any exception thrown during
     * creation is passed on to the caller.
     *
     * Note: detachOnDestroy has NOT been called on the session in the returned client.
     * Clients are responsible for doing this immediately if such desired.
     */
    public omero.client createClient(boolean secure) throws ServerError,
            CannotCreateSessionException, PermissionDeniedException {

        Map<String, String> props = getPropertyMap();
        if (!secure) {
            String insecure = getSession().getConfigService().getConfigValue(
                    "omero.router.insecure");
            if (insecure != null && insecure.length() != 0) {
                props.put("Ice.Default.Router", insecure);
            } else {
                getCommunicator().getLogger().warning("Could not retrieve \"omero.router.insecure\"");
            }
        }

        omero.client nClient = new omero.client(props, secure);
        nClient.setAgent(__agent + ";secure=" + secure);
        nClient.joinSession(getSessionId());
        return nClient;
    }

    // Destruction
    // =========================================================================

    /**
     * Calls closeSession() and ignores any exceptions.
     *
     * Equivalent to OmeroPy's __del__ or OmeroCpp's omero::client::~client()
     */
    public void __del__() {
        try {
            closeSession();
        } catch (Exception e) {
            System.out.println("Ignoring error in client.__del__()");
            e.printStackTrace();
        }
    }

    /**
     * Returns the {@link Ice.Communicator} for this instance or throws an
     * exception if null.
     */
    public Ice.Communicator getCommunicator() {
        Ice.Communicator ic = __ic;
        if (ic == null) {
            throw new ClientError(
                    "No Ice.Communicator active; call createSession() or create a new client instance.");
        }
        return ic;
    }

    public Ice.ObjectAdapter getAdapter() {
        Ice.ObjectAdapter oa = __oa;
        if (oa == null) {
            throw new ClientError("No ObjectAdapter; call createSession()");
        }
        return oa;
    }

    /**
     * Returns the current active session or throws an exception if none has
     * been {@link #createSession(String, String) created} since the last
     * {@link #closeSession()}
     */
    public ServiceFactoryPrx getSession() {
        ServiceFactoryPrx sf = __sf;
        if (__sf == null) {
            throw new ClientError("Call createSession() to login.");
        }
        return sf;
    }

    /**
     * Returns the UUID for the current session without making a remote call.
     * Uses {@link #getSession()} internally and will throw an exception if
     * no session is active.
     */
    public String getSessionId() {
        return getSession().ice_getIdentity().name;
    }

    /**
     * Returns the category which should be used for all callbacks
     * passed to the server.
     */
    public String getCategory() {
        return getRouter(getCommunicator()).getCategoryForClient();
    }

    /**
     * @see #getSession()
     * @deprecated
     */
    @Deprecated
    public ServiceFactoryPrx getServiceFactory() {
        return getSession();
    }

    /**
     * Returns the {@link Ice.ImplicitContext} which defines what properties
     * will be sent on every method invocation.
     */
    public Ice.ImplicitContext getImplicitContext() {
        return getCommunicator().getImplicitContext();
    }

    /**
     * Returns the {@link Ice.Properties active properties} for this instance.
     */
    public Ice.Properties getProperties() {
            return getCommunicator().getProperties();
    }

    /**
     * Returns the property value for this key or the empty string if none.
     */
    public String getProperty(String key) {
        return getProperties().getProperty(key);
    }

    /**
     * Returns all properties which are prefixed with "omero." or "Ice."
     * using the Properties from {@link #getProperties()}.
     */
    public Map<String, String> getPropertyMap() {
        return getPropertyMap(getProperties());
    }

    /**
     * Returns all properties which are prefixed with "omero." or "Ice."
     */
    public Map<String, String> getPropertyMap(Ice.Properties properties) {
        Map<String, String> rv = new HashMap<String, String>();
        for (String prefix : Arrays.asList("omero", "Ice")) {
            Map<String, String> prefixed = properties
                    .getPropertiesForPrefix(prefix);
            rv.putAll(prefixed);
        }
        return rv;
    }

    /**
     * Returns the user configured setting for "omero.block_size"
     * or {@link omero.constants.DEFAULTBLOCKSIZE} if none is set.
     */
    public int getDefaultBlockSize() {
        try {
            return Integer.valueOf(getProperty("omero.block_size"));
        } catch (Exception e) {
            return omero.constants.DEFAULTBLOCKSIZE.value;
        }
    }

    // Session management
    // =========================================================================

    /**
     * Uses the given session uuid as name and password to rejoin a running
     * session.
     *
     * @throws PermissionDeniedException
     * @throws CannotCreateSessionException
     */
    public ServiceFactoryPrx joinSession(String session)
            throws CannotCreateSessionException, PermissionDeniedException,
            ServerError {
        return createSession(session, session);
    }

    /**
     * Calls {@link #createSession(String, String)} with null values
     */
    public ServiceFactoryPrx createSession()
            throws CannotCreateSessionException, PermissionDeniedException,
            ServerError {
        return createSession(null, null);
    }

    /**
     * Performs the actual logic of logging in, which is done via the
     * {@link #getRouter()}. Disallows an extant {@link ServiceFactoryPrx}, and
     * tries to re-create a null {@link Ice.Communicator}. A null or empty
     * username will throw an exception, but an empty password is allowed.
     *
     * @param username
     * @param password
     * @return
     * @throws CannotCreateSessionException
     * @throws PermissionDeniedException
     */
    public ServiceFactoryPrx createSession(String username, String password)
            throws CannotCreateSessionException, PermissionDeniedException,
            ServerError {

        // Checking state

        if (__sf != null) {
            throw new ClientError(
                    "Session already active. Create a new omero.client or closeSession()");
        }

        if (__ic == null) {
            if (__previous == null) {
                throw new ClientError(
                        "No previous data to recreate communicator.");
            }
            init(__previous);
            __previous = null;
        }

        // Check the required properties
        if (username == null) {
            username = getProperty("omero.user");
            if (username == null || "".equals(username)) {
                throw new ClientError("No username specified");
            }
        }
        if (password == null) {
            password = getProperty("omero.pass");
            if (password == null) {
                throw new ClientError("No password specified");
            }
        }

        // Acquire router and get the proxy
        Glacier2.SessionPrx prx = null;
        int retries = 0;
        while (retries < 3) {
            String reason = null;
            if (retries > 0) {
                __ic.getLogger().warning(
                        reason + " - createSession retry: " + retries);
            }
            try {
                Map<String, String> ctx = new HashMap<String, String>(getImplicitContext().getContext());
                ctx.put(AGENT.value, __agent);
                ctx.put(IP.value, __ip);
                Glacier2.RouterPrx rtr = getRouter(__ic);
                prx = rtr.createSession(username, password, ctx);

                // Create the adapter.
                __oa = __ic.createObjectAdapterWithRouter("omero.ClientCallback", rtr);
                __oa.activate();

                Ice.Identity id = new Ice.Identity();
                id.name = __uuid;
                id.category = rtr.getCategoryForClient();

                __cb = new CallbackI(id, this.__ic, this.__oa);
                __oa.add(__cb, id);
                break;
            } catch (omero.WrappedCreateSessionException wrapped) {
                if (!wrapped.concurrency) {
                    throw wrapped; // We only retry concurrency issues.
                }
                reason = wrapped.type + ":" + wrapped.reason;
                retries++;
            } catch (Ice.ConnectTimeoutException cte) {
                reason = "Ice.ConnectTimeoutException:" + cte.getMessage();
                retries++;
            }
        }

        if (null == prx) {
            throw new ClientError("Obtained null object proxy");
        }

        // Check type
        __sf = ServiceFactoryPrxHelper.uncheckedCast(prx);
        if (__sf == null) {
            throw new ClientError(
                    "Obtained object proxy is not a ServiceFactory");
        }

        // Configure keep alive
        String keep_alive = __ic.getProperties().getPropertyWithDefault(
                "omero.keep_alive", "-1");
        try {
            int i = Integer.valueOf(keep_alive);
            enableKeepAlive(i);
        } catch (NumberFormatException nfe) {
            // ignore
        }

        // Set the client callback on the session
        // and pass it to icestorm
        try {
            Ice.ObjectPrx raw = __oa.createProxy(__cb.id);
            __sf.setCallback(ClientCallbackPrxHelper.uncheckedCast(raw));
        } catch (RuntimeException e) {
            __del__();
            throw e;
        } catch (Exception e) {
            __del__();
            throw new RuntimeException(e);
        }

        // Set the session uuid in the implicit context
        getImplicitContext().put(omero.constants.SESSIONUUID.value, getSessionId());
        return this.__sf;

    }

    /**
     * Acquires the {@link Ice.Communicator#getDefaultRouter default router},
     * and throws an exception if it is not of type {Glacier2.RouterPrx}. Also
     * sets the {@link Ice.ImplicitContext} on the router proxy.
     */
    public static Glacier2.RouterPrx getRouter(Ice.Communicator comm) {
        Ice.RouterPrx prx = comm.getDefaultRouter();
        if (prx == null) {
            throw new ClientError("No default router found.");
        }

        Glacier2.RouterPrx router = Glacier2.RouterPrxHelper.checkedCast(prx);
        if (router == null) {
            throw new ClientError("Error obtaining Glacier2 router");
        }

        // For whatever reason, we have to set the context
        // on the router context here as well
        router = Glacier2.RouterPrxHelper.uncheckedCast(router.ice_context(comm
                .getImplicitContext().getContext()));
        return router;
    }

    /**
     * Resets the "omero.keep_alive" property on the current
     * {@link Ice.Communicator} which is used on initialization to determine the
     * time-period between {@link Resources.Entry#check() checks}. If no
     * {@link #__resources} is available currently, one is also created.
     */
    public void enableKeepAlive(int seconds) {

        // A communicator must be configured!
        Ice.Communicator ic = getCommunicator();
        // Setting this here guarantees that after closeSession(), the
        // next createSession() will use the new value despite what was
        // in the configuration file.
        ic.getProperties().setProperty("omero.keep_alive", "" + seconds);

        // If it's not null, then there's already an entry for keeping
        // any existing session alive.
        if (__resources == null && seconds > 0) {
            __resources = new Resources(seconds);
            __resources.add(new Entry() {
                // Return true unless prx.keepAlive() throws an exception.
                public boolean check() {
                    ServiceFactoryPrx prx = __sf;
                    Ice.Communicator ic = __ic;
                    if (prx != null) {
                        try {
                            prx.keepAlive(null);
                        } catch (Exception e) {
                            if (ic != null) {
                                ic.getLogger().warning(
                                        "Proxy keep alive failed.");
                            }
                        }
                    }
                    return true;
                }

                public void cleanup() {
                    // Nothing to do.
                }
            });
        }
    }

    /**
     * Returns all active {@link StatefulServiceInterface} proxies. This can
     * be used to call close before calling setSecurityContext.
     */
    public List<StatefulServiceInterfacePrx> getStatefulServices() throws ServerError {
        List<StatefulServiceInterfacePrx> rv = new ArrayList<StatefulServiceInterfacePrx>();
        ServiceFactoryPrx sf = getSession();
        List<String> services = sf.activeServices();
        for (String srv : services) {
            try {
                ServiceInterfacePrx prx = sf.getByName(srv);
                StatefulServiceInterfacePrx sPrx =
                    omero.api.StatefulServiceInterfacePrxHelper.checkedCast(prx);
                if (sPrx != null) {
                    rv.add(sPrx);
                }
            } catch (Exception e) {
                getCommunicator().getLogger().warning(
                        "Error looking up proxy: " + srv);
            }
        }
        return rv;
    }

    /**
     * Closes the session and nulls out the communicator. This is required by an
     * Ice bug.
     *
     * @see <a
     *      href="http://www.zeroc.com/forums/help-center/2370-ice_ping-error-right-after-createsession-succeed.html">2370
     *      Ice Ping Error</a>
     */
    public void closeSession() {

        ServiceFactoryPrx oldSf = this.__sf;
        this.__sf = null;

        Ice.ObjectAdapter oldOa = this.__oa;
        this.__oa = null;

        Ice.Communicator oldIc = this.__ic;
        this.__ic = null;

        // Only possible if improperly configured
        if (oldIc == null) {
            return; // EARLY EXIT !
        }

        if (oldOa != null) {
            try {
                oldOa.deactivate();
            } catch (Exception e) {
                oldIc.getLogger().warning(
                        "While deactivating adapter: " + e.getMessage());
            }
        }

        __previous = new Ice.InitializationData();
        __previous.properties = oldIc.getProperties()._clone();
        __previous.logger = oldIc.getLogger();
        // ThreadHook is not support since not available from ic

        // Shutdown keep alive
        Resources oldR = __resources;
        __resources = null;
        if (oldR != null) {
            try {
                oldR.cleanup();
            } catch (Exception e) {
                oldIc.getLogger().warning(
                        "While cleaning up resources: " + e.getMessage());
            }
        }

        final boolean fast = this.fastShutdown.get();
        try {
            if (oldSf != null && !fast) {
                oldSf = ServiceFactoryPrxHelper.uncheckedCast(oldSf.ice_oneway());
            }
        } catch (Ice.ConnectionLostException cle) {
            // ok. Exception will always be thrown
        } catch (Ice.ConnectionRefusedException cle) {
            // ok. Server probably went down
        } catch (Ice.ConnectTimeoutException cte) {
            // ok. Server probably went down
        } catch (Ice.DNSException dns) {
            // ok. client is having network issues
        } catch (Ice.SocketException se) {
            // ok. client is having network issues
        } finally {
            try {
                if (oldIc != null && !fast) {
                    oldIc.destroy();
                }
            } finally {
                CLIENTS.remove(this);
            }
        }
    }

    /**
     * Calls ISession.closeSession(omero.model.Session) until
     * the returned reference count is greater than zero. The
     * number of invocations is returned. If ISession.closeSession()
     * cannot be called, -1 is returned.
     */
    public int killSession() {

        ServiceFactoryPrx sf = getSession();
        Ice.Logger __logger = getCommunicator().getLogger();

        omero.model.Session s = new omero.model.SessionI();
        s.setUuid(omero.rtypes.rstring(getSessionId()));

        omero.api.ISessionPrx prx = null;
        try {
            prx = sf.getSessionService();
        } catch (Exception e) {
            __logger.warning("Cannot get session service for killSession. Using closeSession: " + e);
            closeSession();
            return -1;
        }

        int count = 0;
        try {
            int r = 1;
            while (r > 0) {
                count++;
                r = prx.closeSession(s);
            }
        } catch (omero.RemovedSessionException rse) {
            // ignore
        } catch (Exception e) {
            __logger.warning("Unknown exception while closing all references:" + e);
        }

        // Now the server-side session is dead, call closeSession()
        closeSession();
        return count;
    }
    // File handling
    // =========================================================================

    /**
     * Calculates the local sha1 for a file.
     */
    public String sha1(File file) {
        ChecksumProviderFactory cpf = new ChecksumProviderFactoryImpl();
        return cpf.getProvider(ChecksumType.SHA1).putFile(
                        file.getAbsolutePath()).checksumAsString();
    }

    public OriginalFile upload(File file) throws ServerError, IOException {
        return upload(file, null);
    }

    public OriginalFile upload(File file, OriginalFile fileObject) throws ServerError, IOException {
        return upload(file, fileObject, 262144);
    }

    /**
     * Utility method to upload a file to the server
     *
     * @param file
     *            Cannot be null.
     * @param fileObject
     *            Can be null.
     * @param blockSize
     *            Can be null.
     * @throws IOException
     */
    public OriginalFile upload(File file, OriginalFile fileObject, Integer blockSize) throws ServerError, IOException {
        ServiceFactoryPrx sf = getSession();
        if (file == null) {
            throw new ClientError("Non-null file must be provided");
        }

        if (!file.exists() || ! file.canRead()) {
            throw new ClientError("File does not exist or is not readable: " + file.getAbsolutePath());
        }

        if (blockSize == null) {
            blockSize = 262144;
        }

        long size = file.length();
        if (blockSize > size) {
            blockSize = (int) size;
        }

        if (fileObject == null) {
            fileObject = new OriginalFileI();
        }

        fileObject.setSize(rlong(size));

        final ChecksumAlgorithm hasher = new ChecksumAlgorithmI();
        hasher.setValue(rstring(ChecksumAlgorithmSHA1160.value));
        fileObject.setHasher(hasher);
        fileObject.setHash(rstring(sha1(file)));

        if (fileObject.getName() == null) {
            fileObject.setName(rstring(file.getName()));
        }

        if (fileObject.getPath() == null) {
            String path = file.getParent() == null ?
                    File.separator : (file.getParent() + File.separator);
            fileObject.setPath(rstring(path));
        }

        if (fileObject.getMimetype() == null) {
            fileObject.setMimetype(rstring("application/octet-stream"));
        }

        IUpdatePrx up = sf.getUpdateService();
        fileObject = (OriginalFile) up.saveAndReturnObject(fileObject);

        byte[] buf = new byte[blockSize];
        RawFileStorePrx rfs = sf.createRawFileStore();
        FileInputStream stream = null;
        try {
            rfs.setFileId(fileObject.getId().getValue());
            stream = new FileInputStream(file);
            long pos = 0;
            int rlen;
            ByteBuffer bbuf;
            while ((rlen = stream.read(buf)) > 0) {
                rfs.write(buf, pos, rlen);
                pos += rlen;
                bbuf = ByteBuffer.wrap(buf);
                bbuf.limit(rlen);
            }
            return rfs.save();
        } finally {
            Utils.closeQuietly(stream);
            if (rfs != null) {
                rfs.close();
            }
        }
    }

    public void download(long fileId, File file) throws ServerError, IOException {
        download(fileId, file, 262144);
    }

    public void download(long fileId, File file, int blockSize) throws ServerError, IOException  {
        final ServiceFactoryPrx sf = getSession();
        final OriginalFile obj = (OriginalFile) sf.getQueryService().get("OriginalFile", fileId);
        final RawFileStorePrx store = sf.createRawFileStore();
        final FileOutputStream stream = new FileOutputStream(file);

        final long size = obj.getSize().getValue();

        long offset = 0;

        store.setFileId(fileId);
        try {
            for (offset = 0; (offset+blockSize) < size;) {
                stream.write(store.read(offset, blockSize));
                offset += blockSize;
            }
            stream.write(store.read(offset, (int) (size-offset)));
        } finally {
            Utils.closeQuietly(stream);
            store.close();
        }

    }

    // Environment methods
    // =========================================================================

    /**
     * Retrieves an item from the "input" shared (session) memory.
     */
    public RType getInput(String key) throws ServerError {
        return env().getInput(getSessionId(), key);
    }

    /**
     * Retrieves an item from the "output" shared (session) memory.
     */
    public RType getOutput(String key) throws ServerError {
        return env().getOutput(getSessionId(), key);
    }

    /**
     * Sets an item in the "input" shared (session) memory under the given name.
     */
    public void setInput(String key, RType value) throws ServerError {
        env().setInput(getSessionId(), key, value);
    }

    /**
     * Sets an item in the "output" shared (session) memory under the given
     * name.
     */
    public void setOutput(String key, RType value) throws ServerError {
        env().setOutput(getSessionId(), key, value);
    }

    /**
     * Returns a list of keys for all items in the "input" shared (session)
     * memory
     */
    public List<String> getInputKeys() throws ServerError {
        return env().getInputKeys(getSessionId());
    }

    /**
     * Returns a list of keys for all items in the "output" shared (session)
     * memory
     */
    public List<String> getOutputKeys() throws ServerError {
        return env().getOutputKeys(getSessionId());
    }

    // Helpers
    // =========================================================================

    protected String parseAndSetInt(Ice.InitializationData data, String key,
            int newValue) {
        String currentValue = data.properties.getProperty(key);
        if (currentValue == null || currentValue.length() == 0) {
            String newStr = Integer.toString(newValue);
            data.properties.setProperty(key, newStr);
            currentValue = newStr;
        }
        return currentValue;
    }

    protected static String filesToString(File... files) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < files.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(files[i].getAbsolutePath());
        }
        return sb.toString();
    }

    /**
     * Helper method to access session environment
     */
    protected ISessionPrx env() throws ServerError {
        ISessionPrx s = getSession().getSessionService();
        return s;
    }

    // Callback
    // =========================================================================

    public void onHeartbeat(Runnable runnable) {
        __cb.onHeartbeat = runnable;
    }

    public void onSessionClosed(Runnable runnable) {
        __cb.onSessionClosed = runnable;
    }

    public void onShutdown(Runnable runnable) {
        __cb.onShutdown = runnable;
    }

    /**
     * Implementation of {@link ClientCallback} which will be added to any
     * {@link ServiceFactoryPrx} which this instance creates. Note: this client
     * should avoid all interaction with the {@link client#lock} since it can
     * lead to deadlocks during shutdown. See: ticket:1210
     */
    private static class CallbackI extends _ClientCallbackDisp {

        private final Ice.Identity id;

        private final Ice.Communicator ic;

        private final Ice.ObjectAdapter oa;

        private Runnable _noop = new Runnable() {
            public void run() {
                // ok
            }
        };

        private Runnable _closeSession = new Runnable() {

            public void run() {
                try {
                    oa.deactivate();
                } catch (Exception e) {
                    System.err.println("On session closed: " + e.getMessage());
                }
            }

        };

        private Runnable onHeartbeat = _noop;
        private Runnable onSessionClosed = _noop;
        private Runnable onShutdown = _noop;

        public CallbackI(Ice.Identity id, Ice.Communicator ic, Ice.ObjectAdapter oa) {
            this.id = id;
            this.ic = ic;
            this.oa = oa;
        }

        public void requestHeartbeat(Current __current) {
            execute(onHeartbeat, "heartbeat");
        }

        public void shutdownIn(long milliseconds, Current __current) {
            execute(onShutdown, "shutdown");
        }

        public void sessionClosed(Current __current) {
            execute(onSessionClosed, "sessionClosed");
        }

        protected void execute(Runnable runnable, String action) {
            try {
                runnable.run();
                // ic.getLogger().trace("ClientCallback", action + " run");
            } catch (Exception e) {
                try {
                    ic.getLogger().error(
                            "Error performing " + action + ": "
                                    + e.getMessage());
                } catch (Exception e2) {
                    // This could be a null pointer exception or any number
                    // of things. But it's important for us to know that a
                    // heartbeat could not be performed, for example.
                    System.err.println("Error performing " + action + " :"
                            + e.getMessage());
                    System.err.println("(Stderr due to: " + e2.getMessage()
                            + ")");
                }
            }
        }

    }
}
