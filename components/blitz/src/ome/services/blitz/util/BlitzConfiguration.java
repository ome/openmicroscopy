/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.util;

import java.net.URL;

import ome.security.SecuritySystem;
import ome.services.blitz.fire.PermissionsVerifierI;
import ome.services.blitz.fire.Registry;
import ome.services.blitz.fire.Ring;
import ome.services.blitz.fire.SessionManagerI;
import ome.services.blitz.fire.TopicManager;
import ome.services.util.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.ResourceUtils;

import Glacier2.PermissionsVerifier;
import Glacier2.SessionManager;
import Ice.Util;

/**
 * Factory bean which creates an {@link Ice.Communicator} instance as well as the
 * proper {@link Ice.ObjectAdapter} and adds initial, well-known servants.
 * 
 * @author Josh Moore
 * @since 3.0-Beta3.1
 */
public class BlitzConfiguration {

    private final static String CONFIG_KEY = "--Ice.Config=";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Ring blitzRing;

    private final Ice.Communicator communicator;

    private final Ice.ObjectAdapter blitzAdapter;

    private final SessionManagerI blitzManager;

    private final PermissionsVerifier blitzVerifier;

    private final Registry registry;
    
    private final TopicManager topicManager;

    private final Ice.InitializationData id;

    private final Ice.ObjectPrx managerDirectProxy;

    private final int servantsPerSession;

    /**
     * Single constructor which builds all Ice instances needed for the server
     * runtime based on arguments provided. Once the constructor is finished,
     * none of the default create* methods can safely be called, since
     * {@link #throwIfInitialized(Object)} is called first.
     * 
     * If any of the methods other than {@link #createCommunicator()} throws an
     * exception, then {@link #destroy()} will be called to properly shut down
     * the {@link Ice.Communicator} instance. Therefore {@link #destroy()}
     * should be careful to check for nulls.
     */
    public BlitzConfiguration(Ring ring,
            ome.services.sessions.SessionManager sessionManager,
            SecuritySystem securitySystem, Executor executor,
            int servantsPerSession)
            throws RuntimeException {
        this(createId(), ring, sessionManager, securitySystem, executor,
            servantsPerSession);
    }

    /**
     * Like
     * {@link #BlitzConfiguration(Ring, ome.services.sessions.SessionManager, SecuritySystem, Executor, int)}
     * but allows properties to be specified via an
     * {@link Ice.InitializationData} instance.
     * 
     * @param id
     * @param ring
     * @param sessionManager
     * @param securitySystem
     * @param executor
     * @param servantsPerSession
     * @throws RuntimeException
     */
    public BlitzConfiguration(Ice.InitializationData id, Ring ring,
            ome.services.sessions.SessionManager sessionManager,
            SecuritySystem securitySystem, Executor executor,
            int servantsPerSession)
            throws RuntimeException {

        logger.info("Initializing Ice.Communicator");

        this.id = id;
        this.blitzRing = ring;
        this.communicator = createCommunicator();
        this.servantsPerSession = servantsPerSession;

        if (communicator == null) {
            throw new RuntimeException("No communicator cannot continue.");
        }

        try {

            // This component is inert, and so can be created early.
            registry = new Registry.Impl(this.communicator);
            topicManager = new TopicManager.Impl(this.communicator);
            blitzAdapter = createAdapter();
            blitzManager = createAndRegisterManager(sessionManager,
                    securitySystem, executor);
            blitzVerifier = createAndRegisterVerifier(sessionManager, executor);
            managerDirectProxy = blitzAdapter.createDirectProxy(managerId());

            blitzAdapter.activate();

            // When using adapter methods from within the ring, it is necessary
            // to start the adapter first.
            blitzRing.setRegistry(registry);
            blitzRing.init(blitzAdapter, communicator
                    .proxyToString(getDirectProxy()));
        } catch (RuntimeException e) {
            try {
                destroy();
            } catch (Exception e2) {
                logger.error("Error destroying configuration after "
                        + "initialization exception. "
                        + "Throwing initialization exception", e2);
            }
            throw e;
        }

    }

    /**
     * If this configuration is finished and {@link #communicator} is not-null,
     * throw a {@link IllegalStateException}
     */
    protected final void throwIfInitialized(Object instance) {
        if (instance != null) {
            throw new IllegalStateException(
                    "Configuration has already taken place.");
        }
    }

    protected Ice.Communicator createCommunicator() {
        throwIfInitialized(communicator);

        Ice.Communicator ic;

        String ICE_CONFIG = System.getProperty("ICE_CONFIG");
        if (ICE_CONFIG != null) {
            // HORRIBLE HACK. Here we are short cutting the logic below
            // since it is complicated and needs to be reduced. This works in
            // tandem with the code in Main.main() which takes command line
            // arguments.
            id.properties.load(ICE_CONFIG);
        }
        ic = Ice.Util.initialize(id);
        return ic;
    }

    protected Ice.Communicator createCommunicator(String configFile,
            String[] arguments) {

        throwIfInitialized(communicator);

        if (configFile == null) {
            throw new IllegalArgumentException("No config file given.");
        }

        configFile = resolveConfigFile(configFile);

        if (logger.isInfoEnabled()) {
            logger.info("Reading config file:" + configFile);
        }

        Ice.Communicator ic = null;
        Ice.InitializationData id = new Ice.InitializationData();

        if (arguments == null) {
            id.properties = Util.createProperties(new String[] {});
        } else {
            for (int i = 0; i < arguments.length; i++) {
                String s = arguments[i];
                if (s != null && s.startsWith(CONFIG_KEY)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format(
                                "Overriding args setting %s with %s", s,
                                configFile));
                    }
                    arguments[i] = CONFIG_KEY + configFile;
                }
            }
            id.properties = Util.createProperties(arguments);
        }

        ic = Util.initialize(id);

        return ic;
    }

    /**
     * Resolve the given config file to a concrete location, possibly throwing
     * an exception if stored in a jar. Null will not be returned, but an
     * exception may be thrown if the path is invalid.
     */
    protected String resolveConfigFile(String configFile) {
        try {
            URL file = ResourceUtils.getURL(configFile);
            if (ResourceUtils.isJarURL(file)) {
                throw new RuntimeException(configFile + " is in a jar: " + file);
            } else {
                configFile = file.getPath();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error resolving config file: "
                    + configFile, e);
        }
        return configFile;
    }

    /**
     * Creates an adapter with the name "BlitzAdapter", which must be properly
     * configured via --Ice.Config or ICE_CONFIG or similar.
     */
    protected Ice.ObjectAdapter createAdapter() {

        throwIfInitialized(blitzAdapter);

        Ice.ObjectAdapter adapter;
        try {
            adapter = communicator.createObjectAdapter("BlitzAdapter");
        } catch (Exception e) {
            // Possible Ice.IPv6 error
            logger.error("Fatal error creating adapter. " +
                "See the troubleshooting page", e);
            throw new FatalBeanException(
                    "Could not find Ice config for object adapter [ BlitzAdapter ]");
        }

        return adapter;
    }

    protected SessionManagerI createAndRegisterManager(
            ome.services.sessions.SessionManager sessionManager,
            SecuritySystem securitySystem, Executor executor) {

        throwIfInitialized(blitzManager);

        SessionManagerI manager = new SessionManagerI(blitzRing, blitzAdapter,
                securitySystem, sessionManager, executor, topicManager, registry,
                servantsPerSession);
        Ice.Identity id = managerId();
        Ice.ObjectPrx prx = this.blitzAdapter.add(manager, id);
        return manager;
    }

    protected PermissionsVerifier createAndRegisterVerifier(
            ome.services.sessions.SessionManager sessionManager,
            Executor executor) {

        throwIfInitialized(blitzVerifier);

        PermissionsVerifierI verifier = new PermissionsVerifierI(blitzRing,
                sessionManager, executor, blitzRing.uuid);
        this.blitzAdapter.add(verifier, Ice.Util
                .stringToIdentity("BlitzVerifier"));
        return verifier;
    }

    public void destroy() {

        if (blitzRing != null) {
            blitzRing.destroy();
        }

        logger.debug(String.format("Destroying Ice.Communicator (%s)",
                communicator));
        logger.info("Shutting down Ice.Communicator");
        if (blitzAdapter != null) {
            logger.debug(String.format("Deactivating BlitzAdapter (%s)",
                    blitzAdapter));
            blitzAdapter.deactivate();
        }
        communicator.destroy();
    }

    // Getters
    // =========================================================================

    public Ring getRing() {
        if (blitzRing == null) {
            throw new IllegalStateException("Ring is null");
        }
        return blitzRing;
    }

    public Ice.Communicator getCommunicator() {
        if (communicator == null) {
            throw new IllegalStateException("Communicator is null");
        }
        return communicator;
    }

    public Ice.ObjectAdapter getBlitzAdapter() {
        if (blitzAdapter == null) {
            throw new IllegalStateException("Adapter is null");
        }
        return blitzAdapter;
    }

    public SessionManager getBlitzManager() {
        if (blitzManager == null) {
            throw new IllegalStateException("Manager is null");
        }
        return blitzManager;
    }

    public PermissionsVerifier getBlitzVerifier() {
        if (blitzVerifier == null) {
            throw new IllegalStateException("Verifier is null");
        }
        return blitzVerifier;
    }

    public Registry getRegistry() {
        if (registry == null) {
            throw new IllegalStateException("Registry is null");
        }
        return registry;
    }
    
    public TopicManager getTopicManager() {
        if (topicManager == null) {
            throw new IllegalStateException("TopicManager is null");
        }
        return topicManager;
    }

    /**
     * Return a direct proxy to the session manager in this object adapter.
     */
    public Ice.ObjectPrx getDirectProxy() {
        if (managerDirectProxy == null) {
            throw new IllegalStateException("Direct proxy is null");
        }
        return managerDirectProxy;
    }

    // Helpers

    private static Ice.InitializationData createId() {
        Ice.InitializationData iData = new Ice.InitializationData();
        iData.properties = Ice.Util.createProperties();
        return iData;
    }

    private Ice.Identity managerId() {
        Ice.Identity id = Ice.Util.stringToIdentity("BlitzManager");
        return id;
    }

}
