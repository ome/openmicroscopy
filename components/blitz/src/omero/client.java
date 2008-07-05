/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omero;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import omero.api.IAdminPrx;
import omero.api.ISessionPrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceFactoryPrxHelper;
import omero.util.ObjectFactoryRegistrar;
import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

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
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class client {

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

    Ice.Communicator ic;

    ServiceFactoryPrx sf;

    boolean closed = false;

    // Creation
    // =========================================================================

    public client(String[] args) {
        ic = Ice.Util.initialize(args);
        if (null == ic) {
            throw new ClientError("Improper initialization");
        }
        init();
    }

    public client(String iceConfig) {
        ic = Ice.Util.initialize(new String[] { "--Ice.Config=" + iceConfig });
        init();
    }

    public client(File... files) {
        this(filesToString(files));
    }

    public client(Properties p) {
        Ice.InitializationData id = new Ice.InitializationData();
        id.properties = Ice.Util.createProperties();
        if (p != null) {
            for (Object key : p.keySet()) {
                id.properties
                        .setProperty(key.toString(), p.get(key).toString());
            }
        }
        ic = Ice.Util.initialize(id);
        init();
    }

    public client(String name, String password) {
        final Ice.InitializationData id = new Ice.InitializationData();
        id.properties.setProperty(omero.constants.USERNAME.value, name);
        id.properties.setProperty(omero.constants.PASSWORD.value, password);
        this.ic = Ice.Util.initialize(id);
        init();
    }

    private void init() {
        if (ic == null) {
            throw new ClientError("No communicator.");
        }
        // Register Object Factory
        ObjectFactoryRegistrar.registerObjectFactory(ic,
                ObjectFactoryRegistrar.INSTANCE);
        // Define our unique identifer (used during close/detach)
        ic.getImplicitContext().put(omero.constants.CLIENTUUID.value,
                UUID.randomUUID().toString());
        CLIENTS.add(this);
    }

    // Destruction
    // =========================================================================

    /**
     * Equivalent to OmeroPy's __del__ or OmeroCpp's omero::client::~client()
     */
    public void close() {
        try {
            closeSession();
        } catch (Exception e) {
            System.out.println("Ignoring error in client.close()");
            e.printStackTrace();
        } finally {
            closed = true;
        }
    }

    public Ice.Communicator getCommunicator() {
        return ic;
    }

    public ServiceFactoryPrx getServiceFactory() {
        if (sf == null) {
            throw new ClientError("Call createSession() to login.");
        }
        return sf;
    }

    public Ice.Properties getProperties() {
        return this.ic.getProperties();
    }

    public String getProperty(String key) {
        return getProperties().getProperty(key);
    }

    public ServiceFactoryPrx createSession()
            throws CannotCreateSessionException, PermissionDeniedException {
        return createSession(null, null);
    }

    public ServiceFactoryPrx createSession(String username, String password)
            throws CannotCreateSessionException, PermissionDeniedException {

        // Check the required properties
        if (username == null) {
            username = getProperty("omero.user");
            if (username == null) {
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
        // For whatever reason, we have to se the context
        // on the router context here as well
        Glacier2.SessionPrx prx = getRouter().createSession(username, password);
        if (null == prx) {
            throw new ClientError("Obtained null object proxy");
        }
        prx = Glacier2.SessionPrxHelper.uncheckedCast(prx.ice_context(ic
                .getImplicitContext().getContext()));
        sf = ServiceFactoryPrxHelper.checkedCast(prx);
        if (sf == null) {
            throw new ClientError(
                    "Obtained object proxy is not a ServiceFactory");
        }

        return this.sf;
    }

    public Glacier2.RouterPrx getRouter() {
        Ice.RouterPrx prx = ic.getDefaultRouter();
        if (prx == null) {
            throw new ClientError("No default router found.");
        }

        Glacier2.RouterPrx router = Glacier2.RouterPrxHelper.checkedCast(prx);
        if (router == null) {
            throw new ClientError("Error obtaining Glacier2 router");
        }
        return router;
    }

    public void closeSession() {

        ServiceFactoryPrx old = this.sf;
        if (this.sf != null) {
            this.sf = null;
        }

        if (ic == null && sf != null) {
            ic = sf.ice_getCommunicator();
        }

        if (ic == null) {
            return; // EARLY EXIT!
        }

        try {
            getRouter().destroySession();
        } catch (Glacier2.SessionNotExistException snee) {
            // ok. We don't want it to exist
        } catch (Ice.ConnectionLostException cle) {
            // ok. Exception will always be thrown
        } finally {
            ic = null;
        }
    }

    // Environment methods
    // =========================================================================

    public RType getInput(String key) throws ServerError {
        return env().getInput(sess(), key);
    }

    public RType getOutput(String key) throws ServerError {
        return env().getOutput(sess(), key);
    }

    public void setInput(String key, RType value) throws ServerError {
        env().setInput(sess(), key, value);
    }

    public void setOutput(String key, RType value) throws ServerError {
        env().setOutput(sess(), key, value);
    }

    public List<String> getInputKeys() throws ServerError {
        return env().getInputKeys(sess());
    }

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
        if (sf == null) {
            throw new ClientError("No session active");
        }
        ISessionPrx s = sf.getSessionService();
        return s;
    }

    /**
     * Helper method to access session id
     */
    protected String sess() throws ServerError {
        if (sf == null) {
            throw new ClientError("No session active");
        }
        IAdminPrx a = sf.getAdminService();
        String u = a.getEventContext().sessionUuid;
        return u;
    }
}
