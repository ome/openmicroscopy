/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import ome.conditions.SessionException;
import ome.logic.HardWiredInterceptor;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import ome.system.Principal;
import omero.ApiUsageException;
import omero.InternalException;
import omero.ServerError;
import omero.ShutdownInProgress;
import omero.api.ClientCallbackPrx;
import omero.api.ClientCallbackPrxHelper;
import omero.api._StatefulServiceInterfaceOperations;
import omero.constants.CLIENTUUID;
import omero.util.CloseableServant;
import omero.util.IceMapper;
import omero.util.ServantHolder;
import omero.util.TieAware;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import Ice.ConnectTimeoutException;
import Ice.ConnectionLostException;
import Ice.ConnectionRefusedException;
import Ice.Current;

/**
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.3.2
 */
public class SessionI implements _SessionOperations {

    // STATIC
    // ===========

    private final static Logger log = LoggerFactory.getLogger(SessionI.class);

    // PRIVATE STATE
    // =================
    // These fields are special for this instance of SF alone. It represents
    // a single clients use of a session.

    public final String clientId;

    public final Glacier2.SessionControlPrx control;

    protected final AtomicBoolean reusedSession; // See #3202, modifiable as of 4.3

    protected boolean doClose = true;

    protected ClientCallbackPrx callback;

    // SHARED STATE
    // ===================
    // The following elements will all be the same or at least equivalent
    // in different instances of SF attached to the same session.

    public final ServantHolder holder;

    public final SessionManager sessionManager;

    /**
     * {@link Executor} to be used by servant implementations which do not
     * delegate to the server package where all instances are wrapped with AOP
     * for dealing with Hibernate.
     */
    public final Executor executor;

    public final Principal principal;

    public final OmeroContext context;

    public final Ice.ObjectAdapter adapter;

    /** Token in the form of a UUID for securing method invocations. */
    public final String token;

    public SessionI(boolean reusedSession, Ice.Current current,
            ServantHolder holder, Glacier2.SessionControlPrx control,
            OmeroContext context, SessionManager sessionManager,
            Executor executor, Principal principal, String token) throws ApiUsageException {

        this.clientId = clientId(current);
        this.adapter = current.adapter;

        this.control = control;
        this.sessionManager = sessionManager;
        this.context = context;
        this.executor = executor;
        this.principal = principal;
        this.token = token;
        this.reusedSession = new AtomicBoolean(reusedSession);

        // Setting up in memory store.
        Ehcache cache = sessionManager.inMemoryCache(principal.getName());
        String key = "servantHolder";
        this.holder = holder;
        if (!cache.isKeyInCache(key)) {
            cache.put(new Element(key, holder));
        } else {
            // Check that SessionManagerI has given us the same instance.
            ServantHolder oldHolder = (ServantHolder) cache.get(key).getObjectValue();
            assert oldHolder == holder;
        }
    }

    public Ice.ObjectAdapter getAdapter() {
        return this.adapter;
    }

    public Principal getPrincipal() {
        return this.principal;
    }

    public Executor getExecutor() {
        return this.executor;
    }

    // ~ Command API
    // =========================================================================

    public void submit_async(AMD_Session_submit __cb, omero.cmd.Request req,
            Ice.Current current) {
        try {

            if (req == null || !IRequest.class.isAssignableFrom(req.getClass())) {
                log.info("Non-IRequest found:" + req);
                __cb.ice_response(null);
                return; // EARLY EXIT

            }

            Ice.Object servant = null;
            for (String key : Arrays.asList(req.ice_id(),
                    _HandleTie.ice_staticId())) {
                try {
                    servant = createServantDelegate(key);
                    if (servant != null && servant instanceof IHandle) {
                        break;
                    }
                } catch (Exception e) {
                    log.debug(e.getClass().getName() + " on lookup of " + key);
                }
            }

            IHandle handle = null;
            if (servant != null) {
                if (servant instanceof Ice.TieBase) {
                    Ice.TieBase tie = (Ice.TieBase) servant;
                    Object delegate = tie.ice_delegate();
                    if (IHandle.class.isAssignableFrom(delegate.getClass())) {
                        handle = (IHandle) delegate;
                    }
                } else if (servant instanceof IHandle) {
                    handle = (IHandle) servant;
                }
            }

            if (handle == null) {
                log.info("No handle found for " + req);
                InternalException ie = new InternalException();
                ie.message = "No handle found for " + req;
                __cb.ice_exception(ie);
                return; // EARLY EXIT
            }

            // ID
            Ice.Identity id = holder.getIdentity("IHandle" + UUID.randomUUID().toString());

            // Tie
            _HandleTie tie = (_HandleTie) servant;
            HandlePrx prx = HandlePrxHelper.checkedCast(registerServant(id, tie));

            // Init
            try {
                handle.initialize(id, (IRequest) req, current.ctx);
                executor.submit(current.ctx, Executors.callable(handle));
                __cb.ice_response(prx);
            } catch (Throwable e) {
                log.error("Exception on startup; removing handle " + id, e);
                unregisterServant(id);
                throw e;
            }
        } catch (Exception e) {
            log.error("Exception on " + req);
            __cb.ice_exception(e);
        } catch (Throwable t) {
            log.error("Throwable on " + req);
            RuntimeException rt = new RuntimeException("Throwable raised on "
                    + req);
            rt.initCause(t);
            throw rt;
        }
    }

    // ~ Glacier2 API
    // =========================================================================

    /**
     * Destruction simply decrements the reference count for a session to allow
     * reconnecting to it. This means that the Glacier timeout property is
     * fairly unimportant. If a Glacier connection times out or is otherwise
     * destroyed, a client can attempt to reconnect.
     *
     * However, in the case of only one reference to the session, if the
     * Glacier2 timeout is greater than the session timeout, exceptions can be
     * thrown when this method tries to clean up the session. Therefore all
     * session access must be guarded by a try/finally block.
     */
    public void destroy(Ice.Current current) {

        Ice.Identity sessionId = sessionId();
        log.debug("destroy(" + this + ")");

        // Remove this instance from the adapter: 1) to prevent
        // further remote calls on it, and 2) to reduce the number
        // of calls to destroy() from SessionManagerI.reapSession()
        // If an exception if thrown, there's not much we can do,
        // and it's important to continue cleaning up resources!
        try {
            adapter.remove(sessionId); // OK ADAPTER USAGE
            // If not in adapter, then SessionManagerI won't be able to find it.
            holder.removeClientId(clientId);
        } catch (Ice.NotRegisteredException nre) {
            // It's possible that another thread tried to remove
            // this session first. Logging the fact, but we will
            // continue with the closing which should be safe
            // to call multiple times.
            log.warn("NotRegisteredException: "
                    + Ice.Util.identityToString(sessionId()));
        } catch (Ice.ObjectAdapterDeactivatedException oade) {
            log.warn("Adapter already deactivated. Cannot remove: "
                    + sessionId());
        } catch (Throwable t) {
            log.error("Can't remove service factory", t);
        }

        int ref;
        try {
            // First detach and get the reference count.
            ref = sessionManager.detach(this.principal.getName());
        } catch (SessionException rse) {
            // If the session has already been removed or has timed out,
            // then we should do everything we can to clean up.
            log.info("Session already removed. Cleaning up blitz state.");
            ref = 0;
            doClose = true;
        }

        // If we are supposed to close, do only so if the ref count
        // is < 1.
        if (doClose && ref < 1) {

            // First call back to the client to prevent any further access
            // We do so one way though to prevent hanging this method. We
            // also take steps to not fall into a recursive loop.
            ClientCallbackPrx copy = callback;
            callback = null;
            if (copy != null) {
                try {
                    Ice.ObjectPrx prx = copy.ice_oneway();
                    ClientCallbackPrx oneway = ClientCallbackPrxHelper
                            .uncheckedCast(prx);
                    oneway.sessionClosed();
                } catch (Exception e) {
                    handleCallbackException(e);
                }
            }

            // Must check all session access in this method too.
            doDestroy();

            try {
                ref = sessionManager.close(this.principal.getName());
            } catch (SessionException se) {
                // An exception could still theoretically be thrown here
                // if the timeout/removal happened since the last call.
                // Therefore, we'll just let another exception be thrown
                // since the time for shutdown is not overly critical.
            }

        } else {
            cleanupSelf();
        }

    }

    /**
     * Reusable method for logging an exception that occurs on one-way
     * communication with clients during callback notification.
     */
    public void handleCallbackException(Exception e) {
        if (e instanceof Ice.NotRegisteredException) {
            log.warn(clientId + "'s callback not registered -"
                    + " perhaps wrong proxy?");
        } else if (e instanceof ConnectionRefusedException) {
            log.warn(clientId + "'s callback refused connection -"
                + " did the client die?");
        } else if (e instanceof ConnectionLostException) {
            log.debug(clientId + "'s connection lost as expected");
        } else if (e instanceof ConnectTimeoutException) {
            log.warn("ConnectTimeoutException on callback:" + clientId);
        } else if (e instanceof Ice.SocketException ) {
            log.warn("SocketException on callback: " + clientId);
        } else {
            log.error(
                "Unknown error on callback method for client: " + clientId, e);
        }
    }

    /**
     * Called if this isn't a kill-everything event.
     * @see ticket 9330
     */
    public void cleanupSelf() {
        if (log.isInfoEnabled()) {
            log.info(String.format("cleanupSelf(%s).", this));
        }
        cleanServants(false);
    }

    /**
     * Performs the actual cleanup operation on all the resources shared between
     * this and other {@link ServiceFactoryI} instances in the same
     * {@link Session}. Since {@link #destroy()} is called regardless by the
     * router, even when a client has just died, we have this internal method
     * for handling the actual closing of resources.
     *
     * This method must take precautions to not throw a {@link SessionException}
     * . See {@link #destroy(Current)} for more information.
     *
     * When {@link #destroy(Current)} is called but it isn't time to close down
     * the entire instance, then we should still close down the stateless
     * services for this instance as well as remove ourselves from the adapter.
     *
     * @see ticket 9330
     */
    public void doDestroy() {

        if (log.isInfoEnabled()) {
            log.info(String.format("doDestroy(%s)", this));
        }
        cleanServants(true);
    }

    /**
     * Use {@link #cleanServants(boolean, String, ServantHolder, Ice.ObjectAdapter)}
     * to clean up.
     *
     * @param all
     */
    public void cleanServants(boolean all) {
        cleanServants(all, clientId, holder, adapter);
    }

    /**
     * Clean all servants that are in the current holder and remove them from
     * the adapter. If all is true, then all servants that are non-null will be
     * processed. Otherwise, only servants that a) belong to this clientId and
     * b) are not stateful (since stateful services may be used by other clients).
     *
     * @param all
     * @param clientId Only used if all is false.
     * @param holder {@link ServantHolder} to be cleaned.
     * @param adapter from which the servants should be removed.
     */
    public static void cleanServants(boolean all, String clientId,
        ServantHolder holder, Ice.ObjectAdapter adapter) {

        // Cleaning up resources
        // =================================================
        holder.acquireLock("*"); // Protects all the servants on destruction
        try {
            List<String> servants = holder.getServantList();
            for (final String idName : servants) {
                final Ice.Identity id = holder.getIdentity(idName);
                final Object servant = holder.getUntied(id);

                if (servant == null) {
                    log.warn("Servant already removed: " + idName);
                    // But calling unregister just in case
                    unregisterServant(id, adapter, holder);
                    continue; // LOOP.
                }

                if (!all) {
                    if (!idName.contains(clientId)) {
                        // If this doesn't belong to the current client, then
                        // do nothing.
                        continue; // LOOP
                    } else if (servant instanceof _StatefulServiceInterfaceOperations) {
                        // Allowing stateful sessions to persist even after this
                        // particular client is shutdown. This allows another client
                        // to re-attach.
                        log.info("Leaving StatefulService alive:" + idName);
                        continue; // LOOP
                    }
                }

                // All errors are ignored within the loop.
                try {

                    // Now that we have the servant instance, we do what we can
                    // to clean it up. Our AmdServants must use a message
                    // to have the servant removed. InteractiveProcessors must
                    // be stopped and unregistered. Stateless must only be
                    // unregistered.
                    //
                    if (servant instanceof CloseableServant) {
                        CloseableServant cs = (CloseableServant) servant;
                        cs.close(newCurrent(id, "close", adapter, clientId));
                    }
                    // Now ignoring all non-CloseableServants, since
                    // that is *the* interface that should be used
                    // for any cleanup. See #11378
                } catch (Exception e) {
                    log.error("Error destroying servant: " + idName + "="
                            + servant, e);
                } finally {
                    // Now we will again try to remove the servant, which may
                    // have already been done, after the method call, though, it
                    // is guaranteed to no longer be active.
                    unregisterServant(id, adapter, holder);
                    log.info("Removed servant from adapter: " + idName);
                }
            }
        } finally {
            holder.releaseLock("*");
        }
    }

    // ~ Helpers
    // =========================================================================

    protected void internalServantConfig(Object obj) throws ServerError {
        if (obj instanceof SessionAware) {
            ((SessionAware) obj).setSession(this);
        }
    }

    public static Ice.Current newCurrent(Ice.Identity id, String method,
            Ice.ObjectAdapter adapter, String clientId) {
        final Ice.Current __curr = new Ice.Current();
        __curr.id = id;
        __curr.adapter = adapter;
        __curr.operation = method;
        __curr.ctx = new HashMap<String, String>();
        __curr.ctx.put(CLIENTUUID.value, clientId);
        return __curr;
    }

    public Ice.Current newCurrent(Ice.Identity id, String method) {
        return newCurrent(id, method, adapter, clientId);
    }

    public void allow(Ice.ObjectPrx prx) {
        if (prx != null && control != null) {
            control.identities().add(
                    new Ice.Identity[] { prx.ice_getIdentity() });
        }
    }

    /**
     * Creates a proxy according to the {@link ServantDefinition} for the given
     * name. Injects the {@link #helper} instance for this session so that all
     * services are linked to a single session.
     *
     * Creates an ome.api.* service (mostly managed by Spring), wraps it with
     * the {@link HardWiredInterceptor interceptors} which are in effect, and
     * stores the instance away in the cache.
     *
     * Note: Since {@link HardWiredInterceptor} implements
     * {@link MethodInterceptor}, all the {@link Advice} instances will be
     * wrapped in {@link Advisor} instances and will be returned by
     * {@link Advised#getAdvisors()}.
     */
    protected Ice.Object createServantDelegate(String name) throws ServerError {

        Ice.Object servant = null;
        try {

            servant = (Ice.Object) context.getBean(name);
            configureServant(servant);
            return servant;
        } catch (ClassCastException cce) {
            InternalException ie = new InternalException();
            IceMapper.fillServerError(ie, cce);
            ie.message = "Could not cast to Ice.Object:[" + name + "]";
            throw ie;
        } catch (NoSuchBeanDefinitionException nosuch) {
            ApiUsageException aue = new ApiUsageException();
            aue.message = name
                    + " is an unknown service. Please check Constants.ice or the documentation for valid strings.";
            throw aue;
        } catch (Exception e) {
            log.warn("Uncaught exception in createServantDelegate. ", e);
            throw new InternalException(null, e.getClass().getName(),
                    e.getMessage());
        }
    }

    /**
     * Apply proper AOP and call setters for any known injection properties.
     * @param servant
     * @throws ServerError
     */
    public void configureServant(Ice. Object servant) throws ServerError {
        // Now setup the servant
        // ---------------------------------------------------------------------
        internalServantConfig(servant);
        Object real = servant;

        if (servant instanceof Ice.TieBase) {
            Ice.TieBase tie = (Ice.TieBase) servant;
            real = tie.ice_delegate();
            internalServantConfig(real);

            if (real instanceof TieAware) {
                ((TieAware) real).setTie(tie);
            }
        }
    }

    /**
     * Registers the servant with the adapter (or throws an exception if one is
     * already registered) as well as configures the servant in any post-Spring
     * way necessary, based on the type of the servant.
     */
    public Ice.ObjectPrx registerServant(Ice.Identity id, Ice.Object servant)
            throws ServerError {

        Ice.ObjectPrx prx = null;
        try {
            servant = callContextWrapper(servant);
            Ice.Object already = adapter.find(id);
            if (null == already) {
                adapter.add(servant, id); // OK ADAPTER USAGE
                prx = adapter.createDirectProxy(id);
                if (log.isInfoEnabled()) {
                    log.info("Added servant to adapter: "
                            + servantString(id, servant));
                }
            } else {
                throw new omero.InternalException(null, null,
                        "Servant already registered: "
                                + servantString(id, servant));
            }
        } catch (Exception e) {
            if (e instanceof omero.InternalException) {
                throw (omero.InternalException) e;
            } else if (e instanceof Ice.ObjectAdapterDeactivatedException) {
                // ticket:1251
                ShutdownInProgress sip = new ShutdownInProgress(null, null,
                        "ObjectAdapter deactivated");
                IceMapper.fillServerError(sip, e);
                throw sip;
            } else {
                omero.InternalException ie = new omero.InternalException();
                IceMapper.fillServerError(ie, e);
                throw ie;
            }
        }

        // Alright to register this servant now.
        // Using just the name because the category essentially == this
        // holder
        holder.put(id, servant);

        return prx;

    }


    protected Ice.Object callContextWrapper(Ice.Object servant) {
        // If this isn't a tie, then we can't do any wrapping.
        if (!(Ice.TieBase.class.isAssignableFrom(servant.getClass()))) {
            return servant;
        }

        Ice.TieBase tie = (Ice.TieBase) servant;
        Object delegate = tie.ice_delegate();

        ProxyFactory wrapper = new ProxyFactory(delegate);
        wrapper.addAdvice(0, new CallContext(context, token));
        tie.ice_delegate(wrapper.getProxy());
        return servant;
    }

    /**
     * Calls {@link #unregisterServant(Ice.Identity, Ice.ObjectAdapter, ServantHolder)}
     */
    public void unregisterServant(Ice.Identity id) {
        unregisterServant(id, adapter, holder);
    }

    /**
     * Reverts all the additions made by
     * {@link #registerServant(ServantInterface, Ice.Current, Ice.Identity)}
     *
     * Now called by {@link ome.services.blitz.fire.SessionManagerI} in response
     * to an {@link UnregisterServantMessage}
     */
    public static void unregisterServant(Ice.Identity id, Ice.ObjectAdapter adapter,
        ServantHolder holder) {

        // If this is not found ignore.
        if (null == adapter.find(id)) {
            return; // EARLY EXIT!
        }

        // Here we assume that if the "close()" call is required, that it has
        // already been made, either by a user or by the SF.close() method in
        // which case unregisterServant() is being closed via
        // onApplicationEvent().
        // Otherwise, it is being called directly by SF.close().
        final Ice.Object obj = adapter.remove(id); // OK ADAPTER USAGE
        final String str = servantString(id, obj);

        if (holder == null) {
            log.warn("Holder is null for " + str);
        } else {
            Object removed = holder.remove(id);
            if (removed == null) {
                log.error("Adapter and active servants out of sync.");
            }
        }
        if (log.isInfoEnabled()) {
            log.info("Unregistered servant:" + str);
        }
    }

    private static String servantString(Ice.Identity id, Object obj) {
        StringBuilder sb = new StringBuilder(Ice.Util.identityToString(id));
        sb.append("(");
        sb.append(obj);
        sb.append(")");
        return sb.toString();
    }

    // Id Helpers
    // =========================================================================
    // Used for naming service factory instances and creating Ice.Identities
    // from Ice.Currents, etc.

    public Ice.Identity getIdentity(String name) {
        return holder.getIdentity(name);
    }

    /**
     * Definition of session ids: "session-<CLIENTID>/<UUID>"
     */
    public static Ice.Identity sessionId(String clientId, String uuid) {
        Ice.Identity id = new Ice.Identity();
        id.category = "session-" + clientId;
        id.name = uuid;
        return id;
    }

    /**
     * Returns the {@link Ice.Identity} for this instance as defined by
     * {@link #sessionId(String, String)}
     *
     * @return
     */
    public Ice.Identity sessionId() {
        return sessionId(clientId, principal.getName());
    }

    /**
     * Helpers method to extract the {@link CLIENTUUID} out of the given
     * Ice.Current. Throws an {@link ApiUsageException} if none is present,
     * since it is each client's responsibility to set this value.
     *
     * (Typically done in our SDKs)
     */
    public static String clientId(Ice.Current current) throws ApiUsageException {
        String clientId = null;
        if (current.ctx != null) {
            clientId = current.ctx.get(omero.constants.CLIENTUUID.value);
        }
        if (clientId == null) {
            throw new ApiUsageException(null, null, "No "
                    + omero.constants.CLIENTUUID.value
                    + " key provided in context.");
        }
        return clientId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("(");
        sb.append(Ice.Util.identityToString(sessionId()));
        sb.append(")");
        return sb.toString();
    }
}
