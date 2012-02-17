/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.fire;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import ome.logic.HardWiredInterceptor;
import ome.security.SecuritySystem;
import ome.services.blitz.impl.ServiceFactoryI;
import ome.services.blitz.util.ConvertToBlitzExceptionMessage;
import ome.services.blitz.util.RegisterServantMessage;
import ome.services.blitz.util.UnregisterServantMessage;
import ome.services.messages.DestroySessionMessage;
import ome.services.sessions.SessionManager;
import ome.services.sessions.events.ChangeSecurityContextEvent;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.Roles;
import ome.util.messages.MessageException;
import omero.ApiUsageException;
import omero.WrappedCreateSessionException;
import omero.api.ClientCallbackPrxHelper;
import omero.api._ServiceFactoryTie;
import omero.constants.EVENT;
import omero.constants.GROUP;
import omero.constants.topics.HEARTBEAT;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import Glacier2.CannotCreateSessionException;
import Glacier2.StringSetPrx;

/**
 * Central login logic for all OMERO.blitz clients. It is required to create a
 * {@link Glacier2.Session} via the {@link Glacier2.SessionManager} in order to
 * get through the firewall. The {@link Glacier2.Session} (here a
 * {@link ServiceFactoryI} instance) also manages all servants created by the
 * client.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public final class SessionManagerI extends Glacier2._SessionManagerDisp
        implements ApplicationContextAware, ApplicationListener {

    /**
     * "ome.security.basic.BasicSecurityWiring" <em>may</em> be replaced by
     * another value at compile time (see blitz/build.xml), but a default value
     * is necessary here fore testing.
     */
    private final static List<HardWiredInterceptor> CPTORS = HardWiredInterceptor
            .parse(new String[] { "ome.security.basic.BasicSecurityWiring" });

    private final static Log log = LogFactory.getLog(SessionManagerI.class);

    protected OmeroContext context;

    protected final Ice.ObjectAdapter adapter;

    protected final SecuritySystem securitySystem;

    protected final SessionManager sessionManager;

    protected final Executor executor;

    protected final Ring ring;

    protected final Registry registry;
    
    protected final TopicManager topicManager;
    
    protected final AtomicBoolean loaded = new AtomicBoolean(false);

    /**
     * An internal mapping to all {@link ServiceFactoryI} instances for a given
     * session since there is no method on {@link Ice.ObjectAdapter} to retrieve
     * all servants.
     */
    protected final Map<String, Set<String>> sessionToClientIds = new ConcurrentHashMap<String, Set<String>>();

    public SessionManagerI(Ring ring, Ice.ObjectAdapter adapter,
            SecuritySystem secSys, SessionManager sessionManager,
            Executor executor, TopicManager topicManager, Registry reg) {
        this.ring = ring;
        this.registry = reg;
        this.adapter = adapter;
        this.executor = executor;
        this.securitySystem = secSys;
        this.topicManager = topicManager;
        this.sessionManager = sessionManager;
    }

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.context = (OmeroContext) applicationContext;
        HardWiredInterceptor.configure(CPTORS, context);
        loaded.set(true);
    }

    public Glacier2.SessionPrx create(String userId,
            Glacier2.SessionControlPrx control, Ice.Current current)
            throws CannotCreateSessionException {

        if (!loaded.get()) {
            WrappedCreateSessionException wrapped = new WrappedCreateSessionException();
            wrapped.backOff = 1000L;
            wrapped.concurrency = true;
            wrapped.reason = "Server not fully initialized";
            wrapped.type = "ApiUsageException";
            throw wrapped;
        }
        
        try {
            
            // First thing we do is guarantee that the client is giving us
            // the required information.
            ServiceFactoryI.clientId(current); // throws ApiUsageException

            // Before asking the ring, see if we already have the
            // session locally.
            boolean local = false;
            try {
                Object o = sessionManager.find(userId);
                local = (o != null);
                log.info("Found session locally: " + userId);
            } catch (Exception e) {
                log.debug("Exception while waiting on "
                        + "SessionManager.find " + e);
            }

            // If not, then give the ring a chance to redirect to
            // other instances which may already have it.
            if (!local) {
                Glacier2.SessionPrx sf = ring.getProxyOrNull(userId, control,
                        current);
                if (sf != null) {
                    return sf; // EARLY EXIT
                }
            }

            // Defaults
            Roles roles = securitySystem.getSecurityRoles();

            String group = getGroup(current);
            if (group == null) {
                group = roles.getUserGroupName();
            }
            String event = getEvent(current);
            if (event == null) {
                event = "User"; // FIXME This should be in Roles as well.
            }
            String agent = getAgent(current);

            // Create the session for this ServiceFactory
            Principal p = new Principal(userId, group, event);
            ome.model.meta.Session s = sessionManager.createWithAgent(p, agent);
            Principal sp = new Principal(s.getUuid(), group, event);
            // Event raised to add to Ring

            // Create the ServiceFactory
            ServiceFactoryI session = new ServiceFactoryI(local /* ticket:911 */,
                    current, control, context, sessionManager, executor,
                    sp, CPTORS, topicManager, registry);

            Ice.Identity id = session.sessionId();

            if (control != null) {
                // Not having a control implies that this is an internal
                // call, not coming through Glacier, so we can trust it.
                StringSetPrx cat = control.categories();
                cat.add(new String[]{id.category});
                cat.add(new String[]{id.name});
            }
            
            _ServiceFactoryTie tie = new _ServiceFactoryTie(session);
            Ice.ObjectPrx _prx = current.adapter.add(tie, id); // OK Usage
            _prx = current.adapter.createDirectProxy(id);

            // Logging & sessionToClientIds addition
            if (!sessionToClientIds.containsKey(s.getUuid())) {
                sessionToClientIds.put(s.getUuid(), new HashSet<String>());
                log.info(String.format("Created session %s for user %s (agent=%s)",
                        session, userId, agent));
            } else {
                if (log.isInfoEnabled()) {
                    log.info(String.format("Rejoining session %s (agent=%s)",
                            session, agent));
                }
            }
            sessionToClientIds.get(s.getUuid()).add(session.clientId);
            return Glacier2.SessionPrxHelper.uncheckedCast(_prx);

        } catch (Exception t) {

            // Then we are good to go.
            if (t instanceof CannotCreateSessionException) {
                throw (CannotCreateSessionException) t;
            }

            // These need special handling as well.
            else if (t instanceof ome.conditions.ConcurrencyException
                    || t instanceof omero.ConcurrencyException) {

                // Parse out the back off, then everything is generic.
                long backOff = (t instanceof omero.ConcurrencyException) ? ((omero.ConcurrencyException) t).backOff
                        : ((ome.conditions.ConcurrencyException) t).backOff;

                WrappedCreateSessionException wrapped = new WrappedCreateSessionException();
                wrapped.backOff = backOff;
                wrapped.type = t.getClass().getName();
                wrapped.concurrency = true;
                wrapped.reason = "ConcurrencyException: " + t.getMessage()
                        + "\nPlease retry in " + backOff + "ms. Cause: "
                        + t.getMessage();
                throw wrapped;

            }

            ConvertToBlitzExceptionMessage convert = new ConvertToBlitzExceptionMessage(
                    this, t);
            try {
                // TODO Possibly provide context.convert(ConversionMsg) methd.
                context.publishMessage(convert);
            } catch (Throwable t2) {
                log.error("Error while converting exception:", t2);
            }

            if (convert.to instanceof CannotCreateSessionException) {
                throw (CannotCreateSessionException) convert.to;
            }

            // We make an exception for some more or less "expected" exception
            // types. Everything else gets logged as an error which we need
            // to review.
            if (!(t instanceof omero.ApiUsageException
                    || t instanceof ome.conditions.ApiUsageException || t instanceof ome.conditions.SecurityViolation)) {
                log.error("Error while creating ServiceFactoryI", t);
            }

            WrappedCreateSessionException wrapped = new WrappedCreateSessionException();
            wrapped.backOff = -1;
            wrapped.concurrency = false;
            wrapped.reason = t.getMessage();
            wrapped.type = t.getClass().getName();
            wrapped.setStackTrace(t.getStackTrace());
            throw wrapped;
        }
    }

    // Listener
    // =========================================================================

    public void onApplicationEvent(ApplicationEvent event) {
        try {
            if (event instanceof UnregisterServantMessage) {
                UnregisterServantMessage msg = (UnregisterServantMessage) event;
                Ice.Current curr = msg.getCurrent();

                // And unregister the service if possible
                Ice.Identity id = getServiceFactoryIdentity(curr);
                ServiceFactoryI sf = getServiceFactory(id);
                if (sf != null) {
                    sf.unregisterServant(curr.id);
                }
            } else if (event instanceof RegisterServantMessage) {
                RegisterServantMessage msg = (RegisterServantMessage) event;
                Ice.Current curr = msg.getCurrent();
                Ice.Identity id = getServiceFactoryIdentity(curr);
                ServiceFactoryI sf = getServiceFactory(id);
                if (sf != null) {
                    Ice.Identity newId = new Ice.Identity(UUID.randomUUID().toString(), id.name);
                    msg.setProxy(sf.registerServant(newId, msg.getServant()));
                }
            } else if (event instanceof DestroySessionMessage) {
                DestroySessionMessage msg = (DestroySessionMessage) event;
                reapSession(msg.getSessionId());
            } else if (event instanceof ChangeSecurityContextEvent) {
                ChangeSecurityContextEvent csce = (ChangeSecurityContextEvent) event;
                checkStatefulServices(csce);
            }
        } catch (Throwable t) {
            throw new MessageException("SessionManagerI.onApplicationEvent", t);
        }
    }

    /**
     * Checks that there are no stateful services active for the session.
     */
    void checkStatefulServices(ChangeSecurityContextEvent csce) {
        String uuid = csce.getUuid();
        Set<String> clientIds = sessionToClientIds.get(uuid);
        if (clientIds == null) {
            return; // nothing to be done. should only happen during testing.
        }
        clientIds = new HashSet<String>(clientIds);
        for (String clientId : clientIds) {
            try {
                ServiceFactoryI sf = getServiceFactory(clientId, uuid);
                if (sf != null) {
                    String servants = sf.getStatefulServiceCount();
                    if (servants.length() > 0) {
                        csce.cancel("Client " + clientId +
                                " has active stateful services:\n" + servants);
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * {@link ServiceFactoryI#doDestroy() Destroys} all the
     * {@link ServiceFactoryI} instances based on the given sessionId. Multiple
     * clients can be attached to the same session, each with its own
     * {@link ServiceFactoryI}
     */
    public void requestHeartBeats() {
        log.info("Performing requestHeartbeats");
        this.context.publishEvent(new TopicManager.TopicMessage(this,
                HEARTBEAT.value, new ClientCallbackPrxHelper(),
                "requestHeartbeat"));
    }

    /**
     * {@link ServiceFactoryI#doDestroy() Destroys} all the
     * {@link ServiceFactoryI} instances based on the given sessionId. Multiple
     * clients can be attached to the same session, each with its own
     * {@link ServiceFactoryI}
     */
    public void reapSession(String sessionId) {
        Set<String> clientIds = sessionToClientIds.get(sessionId);
        if (clientIds != null) {
            if (clientIds.size() > 0) {
                log.info("Reaping " + clientIds.size() + " clients for " + sessionId);
            }
            for (String clientId : clientIds) {
                try {
                    ServiceFactoryI sf = getServiceFactory(clientId, sessionId);
                    if (sf != null) {
                        sf.doDestroy();
                        Ice.Identity id = sf.sessionId();
                        log.info("Removing " + sf);
                        adapter.remove(id); // OK ADAPTER USAGE
                    }
                } catch (Ice.ObjectAdapterDeactivatedException oade) {
                    log.warn("Cannot reap session " + sessionId
                            + " from client " + clientId
                            + " since adapter is deactivated.");
                } catch (Exception e) {
                    log.error("Error reaping session " + sessionId
                            + " from client " + clientId, e);
                }
            }
        }
        sessionToClientIds.remove(sessionId);
    }

    // Helpers
    // =========================================================================

    protected ServiceFactoryI getServiceFactory(String clientId, String sessionId) {
        Ice.Identity iid = ServiceFactoryI.sessionId(clientId,
                sessionId);
        return getServiceFactory(iid);
    }

    protected ServiceFactoryI getServiceFactory(Ice.Identity iid) {
        Ice.Object obj = adapter.find(iid);
        if (obj == null) {
            log.debug(Ice.Util.identityToString(iid)
                    + " already removed.");
            return null;
        }

        if (obj instanceof _ServiceFactoryTie) {
            _ServiceFactoryTie tie = (_ServiceFactoryTie) obj;
            ServiceFactoryI sf = (ServiceFactoryI) tie.ice_delegate();
            return sf;
        } else {
            log.warn("Not a ServiceFactory: " + obj);
            return null;
        }

    }

    protected Ice.Identity getServiceFactoryIdentity(Ice.Current curr) {
        Ice.Identity id;
        try {
            String clientId = ServiceFactoryI.clientId(curr);
            id = ServiceFactoryI.sessionId(clientId, curr.id.category);
        } catch (ApiUsageException e) {
            throw new RuntimeException(
                    "Cannot create session id for servant:"
                    + String.format("\nInfo:\n\tId:%s\n\tOp:%s\n\tCtx:%s",
                            Ice.Util.identityToString(curr.id),
                            curr.operation, curr.ctx),
                            e);
        }
        return id;
    }

    protected String getGroup(Ice.Current current) {
        if (current.ctx == null) {
            return null;
        }
        return current.ctx.get(GROUP.value);
    }

    protected String getAgent(Ice.Current current) {
        if (current.ctx == null) {
            return null;
        }
        return current.ctx.get("omero.agent");
    }

    protected String getEvent(Ice.Current current) {
        if (current.ctx == null) {
            return null;
        }
        return current.ctx.get(EVENT.value);
    }

}
