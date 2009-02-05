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
import java.util.concurrent.ConcurrentHashMap;

import ome.conditions.InternalException;
import ome.conditions.SecurityViolation;
import ome.logic.HardWiredInterceptor;
import ome.security.SecuritySystem;
import ome.services.blitz.impl.ServiceFactoryI;
import ome.services.blitz.util.ConvertToBlitzExceptionMessage;
import ome.services.blitz.util.UnregisterServantMessage;
import ome.services.messages.DestroySessionMessage;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.Roles;
import omero.ApiUsageException;
import omero.api.ClientCallbackPrx;
import omero.api.ClientCallbackPrxHelper;
import omero.constants.EVENT;
import omero.constants.GROUP;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import Glacier2.CannotCreateSessionException;

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

    /**
     * An internal mapping to all {@link ServiceFactoryI} instances for a given
     * session since there is no method on {@link Ice.ObjectAdapter} to retrieve
     * all servants.
     */
    protected final Map<String, Set<String>> sessionToClientIds = new ConcurrentHashMap<String, Set<String>>();

    public SessionManagerI(Ring ring, Ice.ObjectAdapter adapter,
            SecuritySystem secSys, SessionManager sessionManager,
            Executor executor) {
        this.ring = ring;
        this.adapter = adapter;
        this.executor = executor;
        this.securitySystem = secSys;
        this.sessionManager = sessionManager;
    }

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.context = (OmeroContext) applicationContext;
        HardWiredInterceptor.configure(CPTORS, context);
    }

    public Glacier2.SessionPrx create(String userId,
            Glacier2.SessionControlPrx control, Ice.Current current)
            throws CannotCreateSessionException {

        try {

            Glacier2.SessionPrx sf = ring.getProxyOrNull(userId, control,
                    current);
            if (sf != null) {
                return sf; // EARLY EXIT
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

            // Create the session for this ServiceFactory
            Principal p = new Principal(userId, group, event);
            ome.model.meta.Session s = sessionManager.create(p);
            Principal sp = new Principal(s.getUuid(), group, event);
            // Event raised to add to Ring

            // Create the ServiceFactory
            ServiceFactoryI session = new ServiceFactoryI(current, context,
                    sessionManager, executor, sp, CPTORS);

            Ice.Identity id = session.sessionId();
            Ice.ObjectPrx _prx = current.adapter.add(session, id);
            _prx = current.adapter.createDirectProxy(id);

            // Logging & sessionToClientIds addition
            if (!sessionToClientIds.containsKey(s.getUuid())) {
                sessionToClientIds.put(s.getUuid(), new HashSet<String>());
                log.info(String.format("Created session %s for user %s",
                        id.name, userId));
            } else {
                if (log.isInfoEnabled()) {
                    log.info(String.format("Rejoining session %s", id.name));
                }
            }
            sessionToClientIds.get(s.getUuid()).add(session.clientId);
            return Glacier2.SessionPrxHelper.uncheckedCast(_prx);

        } catch (Exception t) {

            if (t instanceof CannotCreateSessionException) {
                throw (CannotCreateSessionException) t;
            } else if (t instanceof ome.conditions.ConcurrencyException) {
                ome.conditions.ConcurrencyException ce = (ome.conditions.ConcurrencyException) t;
                throw new CannotCreateSessionException(
                        "ConcurrencyException: Please retry in " + ce.backOff
                                + "ms. Cause: " + ce.getMessage());
            } else if (t instanceof ome.conditions.ApiUsageException) {
                ome.conditions.ApiUsageException aue = (ome.conditions.ApiUsageException) t;
                throw new CannotCreateSessionException(aue.getMessage());
            } else if (t instanceof ApiUsageException) {
                ApiUsageException aue = (ApiUsageException) t;
                throw new CannotCreateSessionException(aue.message);
            } else if (t instanceof SecurityViolation) {
                SecurityViolation sv = (SecurityViolation) t;
                throw new CannotCreateSessionException(sv.getMessage());
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

            // FIXME this copying should be a part of ome.conditions.*
            log.error("Error while creating ServiceFactoryI", t);
            InternalException ie = new InternalException(t.getMessage());
            ie.setStackTrace(t.getStackTrace());
            throw ie;
        }
    }

    // Listener
    // =========================================================================

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof UnregisterServantMessage) {
            UnregisterServantMessage msg = (UnregisterServantMessage) event;
            String key = msg.getServiceKey();
            Ice.Current curr = msg.getCurrent();

            // And unregister the service if possible
            Ice.Identity id;
            try {
                String clientId = ServiceFactoryI.clientId(curr);
                id = ServiceFactoryI.sessionId(clientId, curr.id.category);
            } catch (ApiUsageException e) {
                throw new RuntimeException(
                        "Could not unregister servant: could not create session id");
            }
            Ice.Object obj = curr.adapter.find(id);
            if (obj instanceof ServiceFactoryI) {
                ServiceFactoryI sf = (ServiceFactoryI) obj;
                sf.unregisterServant(Ice.Util.stringToIdentity(key));
            } else {
                log.warn("Not a ServiceFactory: " + obj);
            }
        } else if (event instanceof DestroySessionMessage) {
            DestroySessionMessage msg = (DestroySessionMessage) event;
            reapSession(msg.getSessionId());
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
                "/public/HeartBeat", new ClientCallbackPrxHelper(),
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
            for (String clientId : clientIds) {
                try {
                    Ice.Identity iid = ServiceFactoryI.sessionId(clientId,
                            sessionId);
                    Ice.Object obj = adapter.find(iid);
                    if (obj == null) {
                        log.debug(Ice.Util.identityToString(iid)
                                + " already removed.");
                    } else {
                        ServiceFactoryI sf = (ServiceFactoryI) obj;
                        sf.doDestroy();
                        adapter.remove(sf.sessionId());
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

    protected String getGroup(Ice.Current current) {
        if (current.ctx == null) {
            return null;
        }
        return current.ctx.get(GROUP.value);
    }

    protected String getEvent(Ice.Current current) {
        if (current.ctx == null) {
            return null;
        }
        return current.ctx.get(EVENT.value);
    }

}
