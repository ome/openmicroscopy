/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ome.api.IAdmin;
import ome.api.IShare;
import ome.api.local.LocalAdmin;
import ome.logic.HardWiredInterceptor;
import ome.services.blitz.fire.AopContextInitializer;
import ome.services.blitz.fire.Registry;
import ome.services.blitz.fire.TopicManager;
import ome.services.blitz.util.ServiceFactoryAware;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import omero.ApiUsageException;
import omero.InternalException;
import omero.SecurityViolation;
import omero.ServerError;
import omero.api.ClientCallbackPrx;
import omero.api.ExporterPrx;
import omero.api.ExporterPrxHelper;
import omero.api.IAdminPrx;
import omero.api.IAdminPrxHelper;
import omero.api.IConfigPrx;
import omero.api.IConfigPrxHelper;
import omero.api.IContainerPrx;
import omero.api.IContainerPrxHelper;
import omero.api.ILdapPrx;
import omero.api.ILdapPrxHelper;
import omero.api.IMetadataPrx;
import omero.api.IMetadataPrxHelper;
import omero.api.IPixelsPrx;
import omero.api.IPixelsPrxHelper;
import omero.api.IProjectionPrx;
import omero.api.IProjectionPrxHelper;
import omero.api.IQueryPrx;
import omero.api.IQueryPrxHelper;
import omero.api.IRenderingSettingsPrx;
import omero.api.IRenderingSettingsPrxHelper;
import omero.api.IRepositoryInfoPrx;
import omero.api.IRepositoryInfoPrxHelper;
import omero.api.IRoiPrx;
import omero.api.IRoiPrxHelper;
import omero.api.IScriptPrx;
import omero.api.IScriptPrxHelper;
import omero.api.ISessionPrx;
import omero.api.ISessionPrxHelper;
import omero.api.ISharePrx;
import omero.api.ISharePrxHelper;
import omero.api.ITimelinePrx;
import omero.api.ITimelinePrxHelper;
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
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceFactoryPrxHelper;
import omero.api.ServiceInterfacePrx;
import omero.api.ServiceInterfacePrxHelper;
import omero.api.StatefulServiceInterfacePrx;
import omero.api.StatefulServiceInterfacePrxHelper;
import omero.api.ThumbnailStorePrx;
import omero.api.ThumbnailStorePrxHelper;
import omero.api._ServiceFactoryOperations;
import omero.api._StatefulServiceInterfaceOperations;
import omero.constants.ADMINSERVICE;
import omero.constants.CONFIGSERVICE;
import omero.constants.CONTAINERSERVICE;
import omero.constants.EXPORTERSERVICE;
import omero.constants.JOBHANDLE;
import omero.constants.LDAPSERVICE;
import omero.constants.METADATASERVICE;
import omero.constants.PIXELSSERVICE;
import omero.constants.PROJECTIONSERVICE;
import omero.constants.QUERYSERVICE;
import omero.constants.RAWFILESTORE;
import omero.constants.RAWPIXELSSTORE;
import omero.constants.RENDERINGENGINE;
import omero.constants.RENDERINGSETTINGS;
import omero.constants.REPOSITORYINFO;
import omero.constants.ROISERVICE;
import omero.constants.SCRIPTSERVICE;
import omero.constants.SEARCH;
import omero.constants.SESSIONSERVICE;
import omero.constants.SHAREDRESOURCES;
import omero.constants.SHARESERVICE;
import omero.constants.THUMBNAILSTORE;
import omero.constants.TIMELINESERVICE;
import omero.constants.TYPESSERVICE;
import omero.constants.UPDATESERVICE;
import omero.constants.topics.HEARTBEAT;
import omero.grid.SharedResourcesPrx;
import omero.grid.SharedResourcesPrxHelper;
import omero.model.IObject;
import omero.util.IceMapper;
import omero.util.ServantHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;
import Ice.ObjectPrx;

/**
 * Responsible for maintaining all servants for a single session.
 *
 * In general, try to reduce access to the {@link Ice.Current} and
 * {@link Ice.Util} objects.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public final class ServiceFactoryI extends omero.cmd.SessionI implements _ServiceFactoryOperations {

    // STATIC
    // ===========

    private final static Logger log = LoggerFactory.getLogger(ServiceFactoryI.class);

    // SHARED STATE
    // ===================
    // The following elements will all be the same or at least equivalent
    // in different instances of SF attached to the same session.

    final TopicManager topicManager;

    final Registry registry;

    final List<HardWiredInterceptor> cptors;

    final AopContextInitializer initializer;

    // ~ Initialization and context methods
    // =========================================================================

    public ServiceFactoryI(Ice.Current current,
            ServantHolder holder,
            Glacier2.SessionControlPrx control, OmeroContext context,
            SessionManager manager, Executor executor, Principal p,
            List<HardWiredInterceptor> interceptors,
            TopicManager topicManager, Registry registry)
            throws ApiUsageException {
        this(false, current, holder, control, context, manager, executor, p,
                interceptors, topicManager, registry, null);
    }

    public ServiceFactoryI(boolean reusedSession,
            Ice.Current current,
            ServantHolder holder,
            Glacier2.SessionControlPrx control, OmeroContext context,
            SessionManager manager, Executor executor, Principal p,
            List<HardWiredInterceptor> interceptors,
            TopicManager topicManager, Registry registry, String token)
            throws ApiUsageException {
        super(reusedSession, current, holder, control, context, manager, executor, p, token);
        this.cptors = interceptors;
        this.initializer = new AopContextInitializer(new ServiceFactory(
                this.context), this.principal, this.reusedSession);
        this.topicManager = topicManager;
        this.registry = registry;
    }

    public ServiceFactoryPrx proxy() {
        return ServiceFactoryPrxHelper.uncheckedCast(adapter
                .createDirectProxy(sessionId()));
    }

    // ~ Security Context
    // =========================================================================

    @SuppressWarnings("unchecked")
    public List<IObject> getSecurityContexts(Current __current)
            throws ServerError {

        final EventContext ec = getEventContext();
        List<?> objs = (List) executor.execute(principal,
                new Executor.SimpleWork(this, "getSecurityContext") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {

                        final IAdmin admin = sf.getAdminService();
                        final IShare share = sf.getShareService();
                        final List<ome.model.IObject> objs = new ArrayList<ome.model.IObject>();

                        // Groups
                        final Set<Long> added = new HashSet<Long>();
                        for (Long id : ec.getMemberOfGroupsList()) {
                            objs.add(admin.getGroup(id));
                            added.add(id);
                        }
                        for (Long id : ec.getLeaderOfGroupsList()) {
                            if (!added.contains(id)) {
                                objs.add(admin.getGroup(id));
                            }
                        }

                        // Shares
                        objs.addAll(share.getMemberShares(true));
                        objs.addAll(share.getOwnShares(true));

                        return objs;
                    }
                });
        IceMapper mapper = new IceMapper();
        return (List<IObject>) mapper.map(objs);
    }

    public IObject setSecurityContext(IObject obj, Current __current)
            throws ServerError {

        IceMapper mapper = new IceMapper();
        try {
            ome.model.IObject iobj = (ome.model.IObject) mapper.reverse(obj);
            ome.model.IObject old = sessionManager.setSecurityContext(
                    principal, iobj);
            return (IObject) mapper.map(old);
        } catch (Exception e) {
            throw handleException(e);
        }

    }

    public void setSecurityPassword(final String password, Current __current)
            throws ServerError {

        final EventContext ec = getEventContext();
        final String name = ec.getCurrentUserName();
        final boolean ok = sessionManager.executePasswordCheck(name, password);
        if (!ok) {
            final String msg = "Bad password for " + name;
            log.info("setSecurityPassword: " + msg);
            throw new SecurityViolation(null, null, "Bad password for " + name);
        } else {
            this.reusedSession.set(false);
        }
    }

    protected omero.ServerError handleException(Throwable t) {
        IceMapper mapper = new IceMapper();
        Ice.UserException iue = mapper.handleException(t, context);
        if (iue instanceof ServerError) {
            return (ServerError) iue;
        } else { // This may not be necessary
            InternalException iu = new InternalException();
            iu.initCause(t);
            IceMapper.fillServerError(iu, t);
            return iu;
        }
    }

    // ~ Stateless
    // =========================================================================

    public IAdminPrx getAdminService(Ice.Current current) throws ServerError {
        return IAdminPrxHelper.uncheckedCast(getByName(ADMINSERVICE.value,
                current));
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

    public IContainerPrx getContainerService(Ice.Current current)
            throws ServerError {
        return IContainerPrxHelper.uncheckedCast(getByName(
                CONTAINERSERVICE.value, current));
    }

    public IProjectionPrx getProjectionService(Ice.Current current)
            throws ServerError {
        return IProjectionPrxHelper.uncheckedCast(getByName(
                PROJECTIONSERVICE.value, current));
    }

    public IQueryPrx getQueryService(Ice.Current current) throws ServerError {
        return IQueryPrxHelper.uncheckedCast(getByName(QUERYSERVICE.value,
                current));
    }

    public IRoiPrx getRoiService(Ice.Current current) throws ServerError {
        Ice.ObjectPrx prx = getByName(ROISERVICE.value, current);
        return IRoiPrxHelper.uncheckedCast(prx);
    }

    public IScriptPrx getScriptService(Ice.Current current) throws ServerError {
        Ice.ObjectPrx prx = getByName(SCRIPTSERVICE.value, current);
        return IScriptPrxHelper.uncheckedCast(prx);
    }

    public ISessionPrx getSessionService(Current current) throws ServerError {
        return ISessionPrxHelper.uncheckedCast(getByName(SESSIONSERVICE.value,
                current));
    }

    public ISharePrx getShareService(Current current) throws ServerError {
        return ISharePrxHelper.uncheckedCast(getByName(SHARESERVICE.value,
                current));
    }

    public ITimelinePrx getTimelineService(Ice.Current current)
            throws ServerError {
        return ITimelinePrxHelper.uncheckedCast(getByName(
                TIMELINESERVICE.value, current));
    }

    public ITypesPrx getTypesService(Ice.Current current) throws ServerError {
        return ITypesPrxHelper.uncheckedCast(getByName(TYPESSERVICE.value,
                current));
    }

    public IUpdatePrx getUpdateService(Ice.Current current) throws ServerError {
        Ice.ObjectPrx prx = getByName(UPDATESERVICE.value, current);
        return IUpdatePrxHelper.uncheckedCast(prx);

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

    public IMetadataPrx getMetadataService(Ice.Current current)
            throws ServerError {
        return IMetadataPrxHelper.uncheckedCast(getByName(
                METADATASERVICE.value, current));
    }

    // ~ Stateful
    // =========================================================================

    public ExporterPrx createExporter(Current current) throws ServerError {
        return ExporterPrxHelper.uncheckedCast(createByName(
                EXPORTERSERVICE.value, current));
    }

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

    public SharedResourcesPrx sharedResources(Current current)
            throws ServerError {
        return SharedResourcesPrxHelper.uncheckedCast(getByName(
                SHAREDRESOURCES.value, current));
    }

    public Ice.TieBase getTie(Ice.Identity id) {
        return (Ice.TieBase) holder.get(id);
    }

    public Object getServant(Ice.Identity id) {
        return holder.getUntied(id);
    }

    public ServiceInterfacePrx getByName(String blankname, Current dontUse)
            throws ServerError {

        // First try to get the blankname as is in case a value from
        // activeServices is being passed back in.
        Ice.Identity immediateId = holder.getIdentity(blankname);
        if (holder.get(immediateId) != null) {
            return ServiceInterfacePrxHelper.uncheckedCast(adapter
                    .createDirectProxy(immediateId));
        }

        // ticket:911 - in order to use a different initializer
        // for each stateless service, we need to attach modify the id.
        // idName is just the value id.name not Ice.Util.identityToString(id)
        String idName = clientId + blankname;
        Ice.Identity id = holder.getIdentity(idName);

        holder.acquireLock(idName);
        try {
            Ice.ObjectPrx prx;
            Ice.Object servant = holder.get(id);
            if (servant == null) {
                servant = createServantDelegate(blankname);
                // Previously we checked for stateful services here,
                // however the logic is the same so it shouldn't
                // cause any issues.
                prx = registerServant(id, servant);
            } else {
                prx = adapter.createDirectProxy(id);
            }
            return ServiceInterfacePrxHelper.uncheckedCast(prx);
        } finally {
            holder.releaseLock(idName);
        }
    }

    public StatefulServiceInterfacePrx createByName(String name, Current current)
            throws ServerError {

        Ice.Identity id = holder.getIdentity(UUID.randomUUID().toString() + name);
        if (null != adapter.find(id)) {
            omero.InternalException ie = new omero.InternalException();
            ie.message = name + " already registered for this adapter.";
        }

        Ice.Object servant = createServantDelegate(name);
        Ice.ObjectPrx prx = registerServant(id, servant);
        return StatefulServiceInterfacePrxHelper.uncheckedCast(prx);
    }

    public void subscribe(String topicName, ObjectPrx prx, Current __current)
            throws ServerError {

        if (topicName == null || !topicName.startsWith("/public/")) {
            throw new omero.ApiUsageException(null, null,
                    "Currently only \"/public/\" topics allowed.");
        }
        topicManager.register(topicName, prx, false);
        log.info("Registered " + prx + " for " + topicName);
    }

    public void setCallback(ClientCallbackPrx callback, Ice.Current current)
            throws ServerError {
        if (false) { // ticket:2558, disabling because of long logins. See also
                     // #2485
            this.callback = callback;
            log.info(Ice.Util.identityToString(this.sessionId())
                    + " set callback to " + this.callback);
            try {
                subscribe(HEARTBEAT.value, callback, current);
                // ticket:2485. Ignoring any errors on registration
                // of callbacks to permit login. Other client
                // callbacks may want to force an exception with ice_isA
                // or similar.
            } catch (RuntimeException e) {
                log.warn("Failed to subscribe " + callback, e);
                // throw e; ticket:2485
            } catch (ServerError e) {
                log.warn("Failed to subscribe " + callback, e);
                // throw e; ticket:2485
            } catch (Exception e) {
                log.warn("Failed to subscribe " + callback, e);
                // throw new RuntimeException(e); ticket:2485
            }
        }
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
     * NB: Much of the logic here is similar to {@link #doClose} and should be
     * pushed down.
     */
    public String getStatefulServiceCount() {
        return holder.getStatefulServiceCount();
    }

    public List<String> activeServices(Current __current) {
        return holder.getServantList();
    }

    /** Doesn't take current into account */
    public EventContext getEventContext() {
        return sessionManager.getEventContext(this.principal);
    }

    /** Takes current into account */
    public EventContext getEventContext(final Ice.Current current) {
        return (EventContext) executor.execute(current.ctx, this.principal,
                new Executor.SimpleWork(this, "getEventContext") {
                    @Transactional(readOnly=true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        return ((LocalAdmin) sf.getAdminService()).getEventContextQuiet();
                    }
                });
    }

    public long keepAllAlive(ServiceInterfacePrx[] proxies, Current __current)
            throws ServerError {

        try {
            // First take measures to keep the session alive
            getEventContext();
            if (log.isDebugEnabled()) {
                log.debug("Keep all alive: " + this);
            }

            if (proxies == null || proxies.length == 0) {
                return -1; // All set to 1
            }

            long retVal = 0;
            for (int i = 0; i < proxies.length; i++) {
                ServiceInterfacePrx prx = proxies[i];
                if (prx == null) {
                    continue;
                }
                Ice.Identity id = prx.ice_getIdentity();
                if (null == holder.get(id)) {
                    retVal |= 1 << i;
                }
            }
            return retVal;
        } catch (Throwable t) {
            throw handleException(t);
        }
    }

    /**
     * Currently ignoring the individual proxies
     */
    public boolean keepAlive(ServiceInterfacePrx proxy, Current __current)
            throws ServerError {

        try {
            // First take measures to keep the session alive
            getEventContext();
            if (log.isDebugEnabled()) {
                log.debug("Keep alive: " + this);
            }

            if (proxy == null) {
                return false;
            }
            Ice.Identity id = proxy.ice_getIdentity();
            return null != holder.get(id);
        } catch (Throwable t) {
            throw handleException(t);
        }
    }

    // ~ Helpers
    // =========================================================================

        @Override
    protected void internalServantConfig(Object obj) throws ServerError {
        super.internalServantConfig(obj);
        if (obj instanceof ServiceFactoryAware) {
            ((ServiceFactoryAware) obj).setServiceFactory(this);
        }
        if (obj instanceof AbstractAmdServant) {
            AbstractAmdServant amd = (AbstractAmdServant) obj;
            amd.applyHardWiredInterceptors(cptors, initializer);
            amd.setHolder(holder);
            // TODO: amd.setApplicationContext(context);
        }

    }

}
