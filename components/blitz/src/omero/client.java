/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omero;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import omero.api.ClientCallbackPrxHelper;
import omero.api.IAdminPrx;
import omero.api.ISessionPrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceFactoryPrxHelper;
import omero.api._ClientCallbackDisp;
import omero.model.DetailsI;
import omero.model.OriginalFile;
import omero.model.PermissionsI;
import omero.util.ObjectFactoryRegistrar;
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
 * href="https://trac.openmicroscopy.org.uk/omero/wiki/ClientDesign">
 * https://trac.openmicroscopy.org.uk/omero/wiki/ClientDesign </a>
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
                        client.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    /**
     * {@link Ice.InitializationData} from the last communicator used to create
     * the {@link #__ic} if nulled after {@link #closeSession()}.
     */
    private Ice.InitializationData __previous;

    /**
     * {@link Ice.ObjectAdapter} containing the {@link ClientCallback} for
     * this instance.
     */
    private Ice.ObjectAdapter __oa;

    /**
     * Single communicator for this {@link omero.client}. Nullness is used as a
     * test of what state the client is in, therefore all access is sychronized
     * by {@link #lock}
     */
    private Ice.Communicator __ic;

    /**
     * Single session for this {@link omero.client}. Nullness is used as a test
     * of what state the client is in, like {@link #__ic}, therefore all access
     * is synchronized by {@link #lock}
     * 
     */
    private ServiceFactoryPrx __sf;

    /**
     * Lock for all access to {@link #__ic} and {@link #__sf}
     */
    private final Object lock = new Object();

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
        if (id.properties == null) {
            id.properties = Ice.Util.createProperties(new String[]{});
        }
        args = id.properties.parseIceCommandLineOptions(args);
        args = id.properties.parseCommandLineOptions("omero", args);
        init(id);
    }

    /**
     * Creates an {@link Ice.Communicator} from multiple files.
     */
    public client(File... files) {
        Ice.InitializationData id = new Ice.InitializationData();
        id.properties = Ice.Util.createProperties(new String[]{});
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
        Ice.InitializationData id = new Ice.InitializationData();
        id.properties = Ice.Util.createProperties(new String[]{});
        if (p != null) {
            for (Object key : p.keySet()) {
                id.properties
                        .setProperty(key.toString(), p.get(key).toString());
            }
        }
        init(id);
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
            id.properties = Ice.Util.createProperties(new String[]{});
        }

        // Strictly necessary for this class to work
        id.properties.setProperty("Ice.ImplicitContext", "Shared");

        // Setting MessageSizeMax
        String messageSize = id.properties.getProperty("Ice.MessageSizeMax");
        if (messageSize == null || messageSize.length() == 0) {
            id.properties.setProperty("Ice.MessageSizeMax", Integer
                    .toString(omero.constants.MESSAGESIZEMAX.value));
        }

        // Endpoints set to tcp if not present
        String endpoints = id.properties.getProperty("omero.ClientCallback.Endpoints");
        if (endpoints == null || endpoints.length() == 0) {
            id.properties.setProperty("omero.ClientCallback.Endpoints", "tcp");
        }

        // Port, setting to default if not present
        String port = id.properties.getProperty("omero.port");
        if (port == null || port.length() == 0) {
            String portStr = Integer
                    .toString(omero.constants.GLACIER2PORT.value);
            id.properties.setProperty("omero.port", portStr);
            port = portStr;
        }
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
            for (String prefix : Arrays.asList("omero", "Ice")) {
                Map<String, String> prefixed = id.properties.getPropertiesForPrefix(prefix);
                for (String key : prefixed.keySet()) {
                    System.out.println(String.format("%s=%s", key, prefixed.get(key)));
                }
            }
        }

        synchronized (lock) {

            if (__ic != null) {
                throw new ClientError("Client already initialized.");
            }

            __ic = Ice.Util.initialize(id);

            if (__ic == null) {
                throw new ClientError("Improper initialization");
            }

            // Register Object Factories
            ObjectFactoryRegistrar.registerObjectFactory(__ic,
                    ObjectFactoryRegistrar.INSTANCE);
            for (rtypes.ObjectFactory of : rtypes.ObjectFactories.values()) {
                of.register(__ic);
            }
            __ic.addObjectFactory(DetailsI.Factory, DetailsI.ice_staticId());
            __ic.addObjectFactory(PermissionsI.Factory, PermissionsI.ice_staticId());

            // Define our unique identifer (used during close/detach)
            Ice.ImplicitContext ctx = __ic.getImplicitContext();
            if (ctx == null) {
                throw new ClientError("Ice.ImplicitContext not set to Shared");
            }
            ctx.put(omero.constants.CLIENTUUID.value, UUID.randomUUID()
                    .toString());

            // Register the default client callback.
            CallbackI cb = new CallbackI(this);
            __oa = __ic.createObjectAdapter("omero.ClientCallback");
            __oa.add(cb, Ice.Util.stringToIdentity("ClientCallback"));
            __oa.activate();

            // Store this instance for cleanup on shutdown.
            CLIENTS.add(this);
        }
    }

    // Destruction
    // =========================================================================

    /**
     * Calls closeSession() and ignores any exceptions.
     * 
     * Equivalent to OmeroPy's __del__ or OmeroCpp's omero::client::~client()
     */
    public void close() {
        try {
            closeSession();
        } catch (Exception e) {
            System.out.println("Ignoring error in client.close()");
            e.printStackTrace();
        }
    }

    /**
     * Returns the {@link Ice.Communicator} for this instance or throws an
     * exception if null.
     */
    public Ice.Communicator getCommunicator() {
        synchronized (lock) {
            if (__ic == null) {
                throw new ClientError(
                        "No Ice.Communicator active; call createSession() or create a new client instance.");
            }
            return __ic;
        }
    }

    /**
     * Returns the current active session or throws an exception if none has
     * been {@link #createSession(String, String) created} since the last
     * {@link #closeSession()}
     */
    public ServiceFactoryPrx getSession() {
        synchronized (lock) {
            if (__sf == null) {
                throw new ClientError("Call createSession() to login.");
            }
            return __sf;
        }
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
        synchronized (lock) {
            return this.__ic.getProperties();
        }
    }

    /**
     * Returns the property value for this key or the empty string if none.
     */
    public String getProperty(String key) {
        return getProperties().getProperty(key);
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
            throws CannotCreateSessionException, PermissionDeniedException {
        return createSession(session, session);
    }

    /**
     * Calls {@link #createSession(String, String)} with null values
     */
    public ServiceFactoryPrx createSession()
            throws CannotCreateSessionException, PermissionDeniedException {
        return createSession(null, null);
    }

    /**
     * Performs the actual logic of logging in, which is done via the
     * {@link #getRouter()}. Disallows an extant {@link ServiceFactoryPrx},
     * and tries to re-create a null {@link Ice.Communicator}. A null or empty
     * username will throw an exception, but an empty password is allowed.
     * 
     * @param username
     * @param password
     * @return
     * @throws CannotCreateSessionException
     * @throws PermissionDeniedException
     */
    public ServiceFactoryPrx createSession(String username, String password)
            throws CannotCreateSessionException, PermissionDeniedException {

        synchronized (lock) {

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
            Glacier2.SessionPrx prx = getRouter(__ic).createSession(username,
                    password);
            if (null == prx) {
                throw new ClientError("Obtained null object proxy");
            }

            // Check type
            __sf = ServiceFactoryPrxHelper.uncheckedCast(prx);
            if (__sf == null) {
                throw new ClientError(
                        "Obtained object proxy is not a ServiceFactory");
            }

            // Set the client callback on the session
            Ice.ObjectPrx raw = __oa.stringToProxy("ClientCallback");
            __sf.setCallback(ClientCallbackPrxHelper.uncheckedCast(raw));
            return this.__sf;

        }

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
        router = Glacier2.RouterPrxHelper.uncheckedCast(router
                .ice_context(comm.getImplicitContext().getContext()));
        return router;
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

        synchronized (lock) {

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
                    log.warn("While deactivating adapter: " + e.getMessage());
                }
            }

            __previous = new Ice.InitializationData();
            __previous.properties = oldIc.getProperties()._clone();

            try {
                getRouter(oldIc).destroySession();
            } catch (Glacier2.SessionNotExistException snee) {
                // ok. We don't want it to exist
            } catch (Ice.ConnectionLostException cle) {
                // ok. Exception will always be thrown
            } finally {
                oldIc.destroy();
            }

        }

    }

    // File handling
    // =========================================================================


    /**
     * Calculates the local sha1 for a file.
     */
    public String sha1(File file) {
        throw new RuntimeException("NYI");
        // return Utils.fileToSha1(file);
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
     */
    public void upload(File file, OriginalFile fileObject, Integer blockSize) {

    }

    
    // Environment methods
    // =========================================================================

    
    /**
     * Retrieves an item from the "input" shared (session) memory.
     */
    public RType getInput(String key) throws ServerError {
        return env().getInput(sess(), key);
    }

    /**
     * Retrieves an item from the "output" shared (session) memory.
     */
    public RType getOutput(String key) throws ServerError {
        return env().getOutput(sess(), key);
    }

    /**
     * Sets an item in the "input" shared (session) memory under the given name.
     */
    public void setInput(String key, RType value) throws ServerError {
        env().setInput(sess(), key, value);
    }

    /**
     * Sets an item in the "output" shared (session) memory under the given
     * name.
     */
    public void setOutput(String key, RType value) throws ServerError {
        env().setOutput(sess(), key, value);
    }

    /**
     * Returns a list of keys for all items in the "input" shared (session)
     * memory
     */
    public List<String> getInputKeys() throws ServerError {
        return env().getInputKeys(sess());
    }

    /**
     * Returns a list of keys for all items in the "output" shared (session)
     * memory
     */
    public List<String> getOutputKeys() throws ServerError {
        return env().getOutputKeys(sess());
    }

    // Helpers
    // =========================================================================

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
        ISessionPrx s = getServiceFactory().getSessionService();
        return s;
    }

    /**
     * Helper method to access session id
     */
    protected String sess() throws ServerError {
        IAdminPrx a = getServiceFactory().getAdminService();
        String u = a.getEventContext().sessionUuid;
        return u;
    }

    // Callback
    // =========================================================================

    private CallbackI _getCb() {
        Ice.Object obj = this.oa.find(Ice.Util.stringToIdentity("ClientCallback"));
        if (!(obj instanceof CallbackI)) {
            throw new ClientError("Cannot find CallbackI in ObjectAdapter");
        }
        return (CallbackI) obj;
    }

    public void onHearbeat(Runnable runnable) {
       _getCb().onHeartbeat = runnable;
    }


    public void onSessionClosed(Runnable runnable) {
       _getCb().onSessionClosed = runnable;
    }

    public void onShutdown(Runnable runnable) {
       _getCb().onShutdown = runnable;
    }

    /**
     * Implementation of {@link ClientCallback} which will be added to
     * any {@link Session} which this instance creates.
     */
    private static class CallbackI extends _ClientCallbackDisp {

        private final omero.client client;

        private Runnable _noop = new Runnable() {
            public void run() {
                // ok
           }
        };

        private Runnable _closeSession = new Runnable() {

            public void run() {
                try {
                    client.closeSession();
                } catch (Exception e) {
                    // Oh well.
                }
            }

        };

        private Runnable onHeartbeat = _noop;
        private Runnable onSessionClosed = _closeSession;
        private Runnable onShutdown = _closeSession;

        public CallbackI(omero.client client) {
            this.client = client;
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
            Ice.Communicator ic = client.getCommunicator();
            try {
                runnable.run();
                ic.getLogger().trace("ClientCallback", action + " run");
            } catch (Exception e) {
                try {
                    ic.getLogger().error(
                            "Error performing "+action+": " + e.getMessage());
                } catch (Exception e2) {
                    // This could be a null pointer exception or any number
                    // of things. But it's important for us to know that a
                    // heartbeat could not be performed, for exampleì
                    System.err.println("Error performing "+action+" :"+
                            e.getMessage());
                    System.err.println("(Stderr due to: " + e2.getMessage()
                            + ")");
                }
            }
        }

    }
}
