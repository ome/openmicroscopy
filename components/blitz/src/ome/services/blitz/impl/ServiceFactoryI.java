/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import ome.api.JobHandle;
import ome.api.ServiceInterface;
import ome.logic.HardWiredInterceptor;
import ome.services.blitz.fire.AopContextInitializer;
import ome.services.blitz.util.ServantDefinition;
import ome.services.blitz.util.ServantHelper;
import ome.services.blitz.util.UnregisterServantMessage;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import omero.ApiUsageException;
import omero.ServerError;
import omero.api.IAdminPrx;
import omero.api.IAdminPrxHelper;
import omero.api.IConfigPrx;
import omero.api.IConfigPrxHelper;
import omero.api.ILdapPrx;
import omero.api.ILdapPrxHelper;
import omero.api.IPixelsPrx;
import omero.api.IPixelsPrxHelper;
import omero.api.IPojosPrx;
import omero.api.IPojosPrxHelper;
import omero.api.IQueryPrx;
import omero.api.IQueryPrxHelper;
import omero.api.IRenderingSettingsPrx;
import omero.api.IRenderingSettingsPrxHelper;
import omero.api.IRepositoryInfoPrx;
import omero.api.IRepositoryInfoPrxHelper;
import omero.api.IScriptPrx;
import omero.api.IScriptPrxHelper;
import omero.api.ISessionPrx;
import omero.api.ISessionPrxHelper;
import omero.api.ISharePrx;
import omero.api.ISharePrxHelper;
import omero.api.ITypesPrx;
import omero.api.ITypesPrxHelper;
import omero.api.IUpdatePrx;
import omero.api.IUpdatePrxHelper;
import omero.api.JobHandlePrx;
import omero.api.JobHandlePrxHelper;
import omero.api.RawPixelsStorePrx;
import omero.api.RawPixelsStorePrxHelper;
import omero.api.RenderingEnginePrx;
import omero.api.RenderingEnginePrxHelper;
import omero.api.SearchPrx;
import omero.api.SearchPrxHelper;
import omero.api.ServiceInterfacePrx;
import omero.api.ServiceInterfacePrxHelper;
import omero.api.SimpleCallbackPrx;
import omero.api.StatefulServiceInterface;
import omero.api.StatefulServiceInterfacePrx;
import omero.api.StatefulServiceInterfacePrxHelper;
import omero.api.ThumbnailStorePrx;
import omero.api.ThumbnailStorePrxHelper;
import omero.api._ServiceFactoryDisp;
import omero.constants.ADMINSERVICE;
import omero.constants.CONFIGSERVICE;
import omero.constants.JOBHANDLE;
import omero.constants.LDAPSERVICE;
import omero.constants.PIXELSSERVICE;
import omero.constants.POJOSSERVICE;
import omero.constants.QUERYSERVICE;
import omero.constants.RAWFILESTORE;
import omero.constants.RAWPIXELSSTORE;
import omero.constants.RENDERINGENGINE;
import omero.constants.RENDERINGSETTINGS;
import omero.constants.REPOSITORYINFO;
import omero.constants.SCRIPTSERVICE;
import omero.constants.SEARCH;
import omero.constants.SESSIONSERVICE;
import omero.constants.SHARESERVICE;
import omero.constants.THUMBNAILSTORE;
import omero.constants.TYPESSERVICE;
import omero.constants.UPDATESERVICE;
import omero.grid.InteractiveProcessorI;
import omero.grid.InteractiveProcessorPrx;
import omero.grid.InteractiveProcessorPrxHelper;
import omero.grid.ProcessorPrx;
import omero.grid.ProcessorPrxHelper;
import omero.model.Job;
import omero.model.JobStatusI;
import omero.util.IceMapper;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.TransactionStatus;

import Ice.Current;

/**
 * Responsible for maintaining all servants for a single session.
 * 
 * In general, this implementation stores all services (ome.api.*) under the
 * {@link String} representation of the {@link Ice.Identity} and the actual
 * servants are only maintained by the {@link Ice.ObjectAdapter}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public final class ServiceFactoryI extends _ServiceFactoryDisp {

    // STATIC

    private final static Log log = LogFactory.getLog(ServiceFactoryI.class);

    // PRIVATE STATE

    boolean doClose = false;

    public final String clientId;

    // SHARED STATE

    final SessionManager sessionManager;

    /**
     * {@link Executor} to be used by servant implementations which do not
     * delegate to the server package where all instances are wrapped with AOP
     * for dealing with Hibernate.
     */
    final Executor executor;

    /**
     * Stored by {@link String} since {@link Ice.Identity} does not behave
     * properly as a key.
     */
    final Map<String, Ice.Object> activeServants;

    final Map<String, Ice.Object> interactiveSlots;

    final Principal principal;

    final List<HardWiredInterceptor> cptors;

    final OmeroContext context;

    final ServantHelper helper;

    final AopContextInitializer initializer;

    // ~ Initialization and context methods
    // =========================================================================

    public ServiceFactoryI(Ice.Current current, OmeroContext context,
            SessionManager manager, Executor executor, Principal p,
            List<HardWiredInterceptor> interceptors) throws ApiUsageException {
        this.clientId = clientId(current);
        this.context = context;
        this.sessionManager = manager;
        this.executor = executor;
        this.helper = new ServantHelper();
        this.principal = p;
        this.cptors = interceptors;
        initializer = new AopContextInitializer(
                new ServiceFactory(this.context), this.principal);

        // Setting up in memory store.
        Ehcache cache = manager.inMemoryCache(p.getName());
        // TODO the getMap() method could be put in our own Cache class
        activeServants = getMap(ACTIVE_SERVANTS, cache);
        interactiveSlots = getMap(INTERACTIVE_SLOTS, cache);
    }

    static String ACTIVE_SERVANTS = "activeServants";
    static String INTERACTIVE_SLOTS = "interactiveSlots";

    @SuppressWarnings("unchecked")
    private <T> Map<String, T> getMap(String key, Ehcache cache) {
        Map<String, T> ids;
        if (!cache.isKeyInCache(key)) {
            ids = new ConcurrentHashMap<String, T>();
            cache.put(new Element(key, ids));
        } else {
            ids = (Map<String, T>) cache.get(key).getObjectValue();
        }
        return ids;
    }

    // ~ Stateless
    // =========================================================================

    public IAdminPrx getAdminService(Ice.Current current) throws ServerError {
        return IAdminPrxHelper.uncheckedCast(getByName(ADMINSERVICE.value,
                current));
    }

    public IScriptPrx getScriptService(Ice.Current current) throws ServerError {
        Ice.Identity id = getIdentity(current, SCRIPTSERVICE.value);
        Ice.Object servant = current.adapter.find(id);
        if (servant == null) {
            servant = new ScriptI(this);
            registerServant(current, id, servant);
        }
        Ice.ObjectPrx prx = current.adapter.createProxy(id);
        return IScriptPrxHelper.uncheckedCast(prx);
    }

    public IConfigPrx getConfigService(Ice.Current current) throws ServerError {
        return IConfigPrxHelper.uncheckedCast(getByName(CONFIGSERVICE.value,
                current));
    }

    public ILdapPrx getLdapService(Ice.Current current) throws ServerError {
        return ILdapPrxHelper.uncheckedCast(getByName(LDAPSERVICE.value,
                current));
    }

    public IPixelsPrx getPixelsService(Ice.Current current) throws ServerError {
        return IPixelsPrxHelper.uncheckedCast(getByName(PIXELSSERVICE.value,
                current));
    }

    public IPojosPrx getPojosService(Ice.Current current) throws ServerError {
        return IPojosPrxHelper.uncheckedCast(getByName(POJOSSERVICE.value,
                current));
    }

    public IQueryPrx getQueryService(Ice.Current current) throws ServerError {
        return IQueryPrxHelper.uncheckedCast(getByName(QUERYSERVICE.value,
                current));
    }

    public ISessionPrx getSessionService(Current current) throws ServerError {
        return ISessionPrxHelper.uncheckedCast(getByName(SESSIONSERVICE.value,
                current));
    }

    public ISharePrx getShareService(Current current) throws ServerError {
        return ISharePrxHelper.uncheckedCast(getByName(SHARESERVICE.value,
                current));
    }

    public ITypesPrx getTypesService(Ice.Current current) throws ServerError {
        return ITypesPrxHelper.uncheckedCast(getByName(TYPESSERVICE.value,
                current));
    }

    public IUpdatePrx getUpdateService(Ice.Current current) throws ServerError {
        return IUpdatePrxHelper.uncheckedCast(getByName(UPDATESERVICE.value,
                current));
    }

    public IRenderingSettingsPrx getRenderingSettingsService(Ice.Current current)
            throws ServerError {
        return IRenderingSettingsPrxHelper.uncheckedCast(getByName(
                RENDERINGSETTINGS.value, current));
    }

    public IRepositoryInfoPrx getRepositoryInfoService(Ice.Current current)
            throws ServerError {
        return IRepositoryInfoPrxHelper.uncheckedCast(getByName(
                REPOSITORYINFO.value, current));
    }

    // ~ Stateful
    // =========================================================================

    public JobHandlePrx createJobHandle(Ice.Current current) throws ServerError {
        return JobHandlePrxHelper.uncheckedCast(createByName(JOBHANDLE.value,
                current));
    }

    public RenderingEnginePrx createRenderingEngine(Ice.Current current)
            throws ServerError {
        return RenderingEnginePrxHelper.uncheckedCast(createByName(
                RENDERINGENGINE.value, current));
    }

    public omero.api.RawFileStorePrx createRawFileStore(Ice.Current current)
            throws ServerError {
        return omero.api.RawFileStorePrxHelper.uncheckedCast(createByName(
                RAWFILESTORE.value, current));
    }

    public RawPixelsStorePrx createRawPixelsStore(Ice.Current current)
            throws ServerError {
        return RawPixelsStorePrxHelper.uncheckedCast(createByName(
                RAWPIXELSSTORE.value, current));
    }

    public SearchPrx createSearchService(Ice.Current current)
            throws ServerError {
        return SearchPrxHelper
                .uncheckedCast(createByName(SEARCH.value, current));
    }

    public ThumbnailStorePrx createThumbnailStore(Ice.Current current)
            throws ServerError {
        return ThumbnailStorePrxHelper.uncheckedCast(createByName(
                THUMBNAILSTORE.value, current));
    }

    // ~ Other interface methods
    // =========================================================================

    public ServiceInterfacePrx getByName(String name, Current current)
            throws ServerError {

        Ice.Identity id = getIdentity(current, name);

        synchronized (activeServants) {
            Ice.Object servant = activeServants.get(id);
            if (servant == null) {
                servant = createServantDelegate(name);

                // Here we disallow stateful services
                if (StatefulServiceInterface.class.isAssignableFrom(servant
                        .getClass())) {
                    ApiUsageException aue = new ApiUsageException();
                    aue.message = name
                            + " is a stateful service. Please use createByName() instead.";
                    throw aue;
                }

                registerServant(current, id, servant);
            }
        }

        Ice.ObjectPrx prx = current.adapter.createProxy(id);
        return ServiceInterfacePrxHelper.uncheckedCast(prx);
    }

    public StatefulServiceInterfacePrx createByName(String name, Current current)
            throws ServerError {

        Ice.Identity id = getIdentity(current, Ice.Util.generateUUID() + name);

        if (null != current.adapter.find(id)) {
            omero.InternalException ie = new omero.InternalException();
            ie.message = name + " already registered for this adapter.";
        }

        Ice.Object servant = createServantDelegate(name);
        registerServant(current, id, servant);
        Ice.ObjectPrx prx = current.adapter.createProxy(id);
        return StatefulServiceInterfacePrxHelper.uncheckedCast(prx);
    }

    public InteractiveProcessorPrx acquireProcessor(final Job submittedJob,
            int seconds, Current current) throws ServerError {

        if (seconds > (3 * 60)) {
            ApiUsageException aue = new ApiUsageException();
            aue.message = "Delay is too long. Maximum = 3 minutes.";
        }

        final IceMapper mapper = new IceMapper();

        // First create the job with a status of WAITING.
        // The InteractiveProcessor will be responsible for its
        // further lifetime.
        final ome.model.jobs.Job savedJob = (ome.model.jobs.Job) this.executor
                .execute(this.principal, new Executor.Work() {

                    public ome.model.jobs.Job doWork(TransactionStatus status,
                            Session session, ServiceFactory sf) {

                        final JobHandle handle = sf.createJobHandle();
                        try {
                            submittedJob.status = new JobStatusI();
                            submittedJob.status.value = new omero.RString(
                                    JobHandle.WAITING);
                            submittedJob.message = new omero.RString(
                                    "Interactive job. Waiting.");

                            handle.submit((ome.model.jobs.Job) mapper
                                    .reverse(submittedJob));
                            return handle.getJob();
                        } catch (ApiUsageException e) {
                            return null;
                        } finally {
                            if (handle != null) {
                                handle.close();
                            }
                        }
                    }
                });

        if (savedJob == null) {
            throw new ApiUsageException(null, null, "Could not submit job. ");
        }

        // Unloading job to prevent lazy-initialization exceptions.
        Job unloadedJob = (Job) mapper.map(savedJob);
        unloadedJob.unload();

        // Lookup processor
        // Create wrapper (InteractiveProcessor)
        // Create session (with session)
        // Setup environment
        // Send off to processor
        long start = System.currentTimeMillis();
        long stop = seconds < 0 ? start : (start + (seconds * 1000));
        do {

            Ice.ObjectPrx objectPrx = current.adapter.getCommunicator()
                    .stringToProxy("IceGrid/Query");
            IceGrid.QueryPrx query = IceGrid.QueryPrxHelper
                    .checkedCast(objectPrx);
            Ice.ObjectPrx[] candidates = query
                    .findAllObjectsByType("::omero::grid::Processor");

            // //current.con

            for (Ice.ObjectPrx op : candidates) {
                ProcessorPrx p;
                try {
                    p = ProcessorPrxHelper.checkedCast(op);
                    if (p != null) {
                        // p.login()
                    }
                } catch (Exception e) {
                    // continue
                }
            }

            Ice.ObjectPrx prx = current.adapter.getCommunicator()
                    .stringToProxy("Processor");
            if (prx != null) {
                ProcessorPrx processor;
                try {
                    processor = ProcessorPrxHelper.checkedCast(prx);
                    if (processor != null) {
                        long timeout = System.currentTimeMillis() + 60 * 60 * 1000L;
                        InteractiveProcessorI ip = new InteractiveProcessorI(
                                this.principal, this.sessionManager,
                                this.executor, processor, unloadedJob, timeout);
                        Ice.Identity id = new Ice.Identity();
                        id.category = current.id.name;
                        id.name = Ice.Util.generateUUID();
                        Ice.ObjectPrx rv = current.adapter.add(ip, id);
                        interactiveSlots.put(Ice.Util.identityToString(id), ip);
                        return InteractiveProcessorPrxHelper.uncheckedCast(rv);
                    }
                    try {
                        Thread.sleep((stop - start) / 10);
                    } catch (InterruptedException ie) {
                        // ok.
                    }
                } catch (Ice.NoEndpointException nee) {
                    // This means that there probably is none.
                    // Wait a little longer
                    try {
                        Thread.sleep((stop - start) / 3);
                    } catch (InterruptedException ie) {
                        // ok.
                    }
                }
            }
        } while (stop < System.currentTimeMillis());
        return null;
    }

    public void setCallback(SimpleCallbackPrx callback, Ice.Current current) {
        throw new UnsupportedOperationException();
    }

    public void detachOnDestroy(Ice.Current current) {
        doClose = false;
    }

    @Deprecated
    public void close(Ice.Current current) {
        doClose = false;
    }

    public void closeOnDestroy(Ice.Current current) {
        doClose = true;
    }

    /**
     * Destruction simply decrements the reference count for a session to allow
     * reconnecting to it. This means that the Glacier timeout property is
     * fairly unimportant. If a Glacier connection times out or is otherwise
     * destroyed, a client can attempt to reconnect
     */
    public void destroy(Ice.Current current) {

        // First detach and get the reference count.
        int ref = sessionManager.detach(this.principal.getName());

        // If we are supposed to close, do only so if the ref count
        // is < 1.
        if (doClose && ref < 1) {

            ref = sessionManager.close(this.principal.getName());

            // If someone has attached since we called detach() above, do not
            // destroy this session.
            if (ref > 0) {
                return;
            }

            doDestroy(current.adapter);

        }

        // All resources cleaned up or not based on the reference count.
        // Now we can remove the current session. If an exception if thrown,
        // there's not much we can do.
        try {
            current.adapter.remove(sessionId(principal.getName()));
        } catch (Throwable t) {
            // FIXME
            log.error("Possible memory leak: can't remove service factory", t);
        }

    }

    /**
     * Performs the actual cleanup operation on all the resources shared between
     * this and other {@link ServiceFactoryI} instances in the same
     * {@link Session}. Since {@link #destroy()} is called regardless by the
     * router, even when a client has just died, we have this internal method
     * for handling the actual closing of resources.
     */
    public void doDestroy(Ice.ObjectAdapter adapter) {

        if (log.isInfoEnabled()) {
            log.info(String.format("Closing %s session", this));
        }

        // Cleaning up resources

        // INTERACTIVE
        Set<String> ipIds = interactiveSlots.keySet();
        for (String id : ipIds) {
            InteractiveProcessorI ip = null;
            try {
                ip = (InteractiveProcessorI) interactiveSlots.get(id);
                ip.stop();
            } catch (Exception e) {
                log.error("Error stopping interactive processor: " + ip);
            } finally {
                adapter.remove(Ice.Util.stringToIdentity(id));
                interactiveSlots.remove(id);
            }
        }

        // SERVANTS
        // Here we call the "close()" method on all methods which require
        // that
        // logic allowing the IceMethodInvoker to raise the
        // UnregisterServantEvent, otherwise there is a recursive call back
        // to close.
        List<String> ids = activeServices();
        for (String idString : ids) {
            Ice.Identity id = Ice.Util.stringToIdentity(idString);
            Ice.Object obj = adapter.find(id);
            try {
                if (obj instanceof StatefulServiceInterface) {
                    Method m = obj.getClass().getMethod("close",
                            Ice.Current.class);
                    Ice.Current __curr = new Ice.Current();
                    __curr.id = id;
                    __curr.adapter = adapter;
                    __curr.operation = "close";
                    m.invoke(obj, __curr);
                } else {
                    unregisterServant(id, adapter);
                }
            } catch (Exception e) {
                log.error("Failure to close: " + idString + "=" + obj, e);
            }
        }

    }

    public List<String> activeServices(Current __current) {
        List<String> active = new ArrayList<String>();
        for (String id : activeServants.keySet()) {
            active.add(id);
        }
        return active;
    }

    public long keepAllAlive(ServiceInterfacePrx[] proxies, Current __current) {
        if (proxies == null || proxies.length == 0) {
            return -1; // All set to 1
        }

        long retVal = 0;
        for (int i = 0; i < proxies.length; i++) {
            ServiceInterfacePrx prx = proxies[i];
            if (!keepAlive(prx, __current)) {
                retVal |= 1 << i;
            }
        }
        return retVal;
    }

    /**
     * Currently ignoring the individual proxies
     */
    public boolean keepAlive(ServiceInterfacePrx proxy, Current __current) {
        if (proxy == null) {
            return false;
        }
        Ice.Identity id = proxy.ice_getIdentity();
        // This will keep the session alive.
        sessionManager.getEventContext(this.principal);
        return null != activeServants.get(Ice.Util.identityToString(id));
    }

    // ~ Helpers
    // =========================================================================

    /**
     * Constructs an {@link Ice.Identity} from the name of this
     * {@link ServiceFactoryI} and from the given {@link String} which for
     * stateless services are defined by the instance fields {@link #adminKey},
     * {@link #configKey}, etc. and for stateful services are UUIDs.
     */
    protected Ice.Identity getIdentity(Ice.Current curr, String key) {
        Ice.Identity id = new Ice.Identity();
        id.category = curr.id.name;
        id.name = key;
        return id;
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
    protected <O extends ServiceInterface> Ice.Object createServantDelegate(
            String name) throws ApiUsageException {

        ServantDefinition sd;
        try {
            sd = (ServantDefinition) context.getBean(name);
        } catch (Exception e) {
            ApiUsageException aue = new ApiUsageException();
            aue.message = name
                    + " is an unknown service. Please check Constants.ice or the documentation for valid strings.";
            throw aue;
        }

        Object srv = null;
        try {
            srv = context.getBean("managed-" + sd.getServiceClass().getName());
        } catch (Exception e) {
            ApiUsageException aue = new ApiUsageException();
            aue.message = "No managed service of given type found:"
                    + sd.getServiceClass().getName();
            throw aue;
        }

        ProxyFactory managedService = new ProxyFactory();
        managedService.setInterfaces(new Class[] { sd.getServiceClass() });

        List<HardWiredInterceptor> reversed = new ArrayList<HardWiredInterceptor>(
                cptors);
        Collections.reverse(reversed);
        for (HardWiredInterceptor hwi : reversed) {
            managedService.addAdvice(0, hwi);
        }
        managedService.addAdvice(0, initializer);
        managedService.setTarget(srv);
        ServiceInterface srvIface = (ServiceInterface) managedService
                .getProxy();

        ProxyFactory factory = new ProxyFactory();
        factory.setInterfaces(new Class[] { sd.getOperationsClass() });
        factory.addAdvice(new Interceptor(sd.getServiceClass(), srvIface,
                helper, context));
        Object ops = factory.getProxy();

        Ice.Object servant = null;
        try {
            Constructor<Ice.Object> ctor = sd.getTieClass().getConstructor(
                    sd.getOperationsClass());
            servant = ctor.newInstance(ops);
        } catch (Exception e) {
            omero.InternalException ie = new omero.InternalException();
            ie.message = "Failed while trying to create servant.";
            ie.serverExceptionClass = e.getClass().getName();
        }
        return servant;
    }

    protected void registerServant(Current current, Ice.Identity id,
            Ice.Object servant) throws omero.InternalException {

        activeServants.put(Ice.Util.identityToString(id), servant);
        try {
            Ice.Object already = current.adapter.find(id);
            if (null == already) {
                current.adapter.add(servant, id);
                if (log.isInfoEnabled()) {
                    log.info("Created servant:" + servantString(id, servant));
                }
            } else {
                current.adapter.remove(id);
                current.adapter.add(servant, id);
                if (already.hashCode() != servant.hashCode()) {
                    log.info(String.format("Replacing %s with %s",
                            servantString(id, already), servantString(id,
                                    servant)));
                }
            }
        } catch (Exception e) {
            // FIXME
            omero.InternalException ie = new omero.InternalException();
            ie.message = e.getMessage();
            throw ie;
        }
    }

    /**
     * Reverts all the additions made by
     * {@link #registerServant(ServantInterface, Ice.Current, Ice.Identity)}
     * 
     * Now called by {@link ome.services.blitz.fire.SessionManagerI} in response
     * to an {@link UnregisterServantMessage}
     */
    public void unregisterServant(Ice.Identity id, Ice.ObjectAdapter adapter) {

        // Here we assume that if the "close()" call is required, that it has
        // already been made, either by a user or by the SF.close() method in
        // which case unregisterServant() is being closed via
        // onApplicationEvent().
        // Otherwise, it is being called directly by SF.close().
        Ice.Object obj = adapter.remove(id);
        Object removed = activeServants.remove(Ice.Util.identityToString(id));
        if (removed == null) {
            log.error("Adapter and active servants out of sync.");
        }
        if (log.isInfoEnabled()) {
            log.info("Unregistered servant:" + servantString(id, obj));
        }
    }

    private String servantString(Ice.Identity id, Object obj) {
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

    public static Ice.Identity sessionId(Ice.Current current, String uuid)
            throws ApiUsageException {
        String clientId = clientId(current);
        return sessionId(clientId, uuid);
    }

    public static Ice.Identity sessionId(String clientId, String uuid) {
        Ice.Identity id = new Ice.Identity();
        id.category = "session-" + clientId;
        id.name = uuid;
        return id;

    }

    public Ice.Identity sessionId(String uuid) {
        return sessionId(clientId, uuid);
    }

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

}
