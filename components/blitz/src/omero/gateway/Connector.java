/*
 * org.openmicroscopy.shoola.env.data.Connector 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omero.gateway;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import ome.formats.OMEROMetadataStoreClient;
import omero.ServerError;
import omero.client;
import omero.api.ExporterPrx;
import omero.api.ExporterPrxHelper;
import omero.api.IAdminPrx;
import omero.api.IAdminPrxHelper;
import omero.api.IConfigPrx;
import omero.api.IConfigPrxHelper;
import omero.api.IContainerPrx;
import omero.api.IContainerPrxHelper;
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
import omero.api.IUpdatePrx;
import omero.api.IUpdatePrxHelper;
import omero.api.RawFileStorePrx;
import omero.api.RawFileStorePrxHelper;
import omero.api.RawPixelsStorePrx;
import omero.api.RawPixelsStorePrxHelper;
import omero.api.RenderingEnginePrx;
import omero.api.SearchPrx;
import omero.api.SearchPrxHelper;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceInterfacePrx;
import omero.api.StatefulServiceInterfacePrx;
import omero.api.ThumbnailStorePrx;
import omero.api.ThumbnailStorePrxHelper;
import omero.cmd.CmdCallbackI;
import omero.cmd.DoAll;
import omero.cmd.Request;
import omero.grid.SharedResourcesPrx;
import omero.grid.SharedResourcesPrxHelper;
import omero.log.Logger;
import omero.model.ExperimenterGroup;
import omero.model.Session;
import omero.sys.Principal;
import omero.util.CommonsLangUtils;


/** 
 * Manages the various services and entry points.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
class Connector
{

    /**
     * The elapsed time before checking if the services need to be
     * kept alive.
     */
    private final int ELAPSED_TIME = 30000;

    /** Keeps track of the last keep alive action.*/
    private final AtomicLong lastKeepAlive = new AtomicLong(System.currentTimeMillis());

    /** 
     * The Blitz client object, this is the entry point to the
     * OMERO Server using a secure connection.
     */
    private final client secureClient;

    /** 
     * The client object, this is the entry point to the
     * OMERO Server using non secure data transfer
     */
    private client unsecureClient;

    /**
     * The entry point provided by the connection library to access the various
     * <i>OMERO</i> services.
     */
    private ServiceFactoryPrx entryEncrypted;

    /**
     * The entry point provided by the connection library to access the various
     * <i>OMERO</i> services.
     */
    private ServiceFactoryPrx entryUnencrypted;

    /** Collection of stateless services to prevent re-lookup */
    private final Map<String, ServiceInterfacePrx> statelessServices;

    /** Collection of stateful services to prevent re-lookup.
     * {@link RenderingEnginePrx} and {@link OMEROMetadataStoreClient}
     * instances are stored separately */
    private final Multimap<String, StatefulServiceInterfacePrx> statefulServices;

    private final Map<OMEROMetadataStoreClient, String> importStores;

    /** Collection of services to keep alive. */
    private final Multimap<Long, RenderingEnginePrx> reServices;

    /** The security context for that connector.*/
    private final SecurityContext context;

    /**
     * The map of derived connector. This will be only used when
     * performing action for other users e.g. import as.
     *
     * TODO: this should be reviewed, since if getConnector(String) is used
     * outside of the import process there could be a race condition.
     */
    private final Cache<String, Connector> derived;

    /** The name of the group. To be removed when we can use groupId.*/
    private String groupName;

    /** Reference to the logger.*/
    private final Logger logger;

    /**
     * Logs the error.
     */
    private void log(String msg)
    {
        logger.debug(this, msg);
    }

    Connector(SecurityContext context, client client,
            ServiceFactoryPrx entryEncrypted, boolean encrypted, Logger logger)
                    throws Exception
    {
        if (context == null)
            throw new IllegalArgumentException("No Security context.");
        if (client == null)
            throw new IllegalArgumentException("No Server entry point.");
        if (entryEncrypted == null)
            throw new IllegalArgumentException("No Services entry point.");
        if (!encrypted) {
            unsecureClient = client.createClient(false);
            entryUnencrypted = unsecureClient.getSession();
        } else {
            unsecureClient = null;
            entryUnencrypted = null;
        }
        this.logger = logger;
        this.secureClient = client;
        this.entryEncrypted = entryEncrypted;
        this.context = context;
        final MapMaker mapMaker = new MapMaker();
        statelessServices = mapMaker.makeMap();
        importStores = mapMaker.makeMap();
        statefulServices = Multimaps.<String, StatefulServiceInterfacePrx>
        synchronizedMultimap(
                HashMultimap.<String, StatefulServiceInterfacePrx>create());
        reServices = Multimaps.<Long, RenderingEnginePrx>
        synchronizedMultimap(
                HashMultimap.<Long, RenderingEnginePrx>create());
        derived = CacheBuilder.newBuilder().build();
    }

    //
    // Regular service lookups
    //

    /**
     * Returns the {@link SharedResourcesPrx} service.
     * 
     * @return See above.
     * @throws Throwable Thrown if the service cannot be initialized.
     */
    SharedResourcesPrx getSharedResources()
            throws DSOutOfServiceException
    {
        return SharedResourcesPrxHelper.uncheckedCast(
                get(omero.constants.SHAREDRESOURCES.value, true));
    }

    /**
     * Returns the {@link IRenderingSettingsPrx} service.
     * 
     * @return See above.
     * @throws Throwable Thrown if the service cannot be initialized.
     */
    IRenderingSettingsPrx getRenderingSettingsService()
            throws DSOutOfServiceException
    {
        return IRenderingSettingsPrxHelper.uncheckedCast(
                get(omero.constants.RENDERINGSETTINGS.value,
                        unsecureClient == null));
    }

    /**
     * Returns the {@link IRepositoryInfoPrx} service.
     * 
     * @return See above.
     * @throws Throwable Thrown if the service cannot be initialized.
     */
    IRepositoryInfoPrx getRepositoryService()
            throws DSOutOfServiceException
    {
        return IRepositoryInfoPrxHelper.uncheckedCast(
                get(omero.constants.REPOSITORYINFO.value,
                        unsecureClient == null));
    }

    /**
     * Returns the {@link IScriptPrx} service.
     * 
     * @return See above.
     * @throws Throwable Thrown if the service cannot be initialized.
     */
    IScriptPrx getScriptService()
            throws DSOutOfServiceException
    {
        return IScriptPrxHelper.uncheckedCast(
                get(omero.constants.SCRIPTSERVICE.value,
                        unsecureClient == null));
    }

    /**
     * Returns the {@link IContainerPrx} service.
     * 
     * @return See above.
     * @throws Throwable Thrown if the service cannot be initialized.
     */
    IContainerPrx getPojosService()
            throws DSOutOfServiceException
    {
        return IContainerPrxHelper.uncheckedCast(
                get(omero.constants.CONTAINERSERVICE.value,
                        unsecureClient == null));
    }

    /**
     * Returns the {@link IQueryPrx} service.
     * 
     * @return See above.
     * @throws Throwable Thrown if the service cannot be initialized.
     */
    IQueryPrx getQueryService()
            throws DSOutOfServiceException
    {
        return IQueryPrxHelper.uncheckedCast(
                get(omero.constants.QUERYSERVICE.value,
                        unsecureClient == null));
    }

    /**
     * Returns the {@link IUpdatePrx} service.
     *  
     * @return See above.
     * @throws Throwable Thrown if the service cannot be initialized.
     */
    IUpdatePrx getUpdateService()
            throws DSOutOfServiceException
    {
        return IUpdatePrxHelper.uncheckedCast(
                get(omero.constants.UPDATESERVICE.value,
                        unsecureClient == null));
    }

    /**
     * Returns the {@link IMetadataPrx} service.
     *  
     * @return See above.
     * @throws Throwable Thrown if the service cannot be initialized.
     */
    IMetadataPrx getMetadataService()
            throws DSOutOfServiceException
    {
        return IMetadataPrxHelper.uncheckedCast(
                get(omero.constants.METADATASERVICE.value,
                        unsecureClient == null));
    }

    /**
     * Returns the {@link IRoiPrx} service.
     *  
     * @return See above.
     * @throws Throwable Thrown if the service cannot be initialized.
     */
     IRoiPrx getROIService()
            throws DSOutOfServiceException
    {
        return IRoiPrxHelper.uncheckedCast(
                get(omero.constants.ROISERVICE.value, unsecureClient == null));
    }

    /**
     * Returns the {@link IConfigPrx} service.
     * 
     * @return See above.
     * @throws Throwable Thrown if the service cannot be initialized.
     */
     IConfigPrx getConfigService()
            throws DSOutOfServiceException
    {
        return IConfigPrxHelper.uncheckedCast(
                get(omero.constants.CONFIGSERVICE.value,
                        unsecureClient == null));
    }

    /**
     * Returns the {@link ThumbnailStorePrx} service.
     *
     * @return See above.
     * @throws Throwable Thrown if the service cannot be initialized.
     */
     ThumbnailStorePrx getThumbnailService()
            throws DSOutOfServiceException
    {
        return ThumbnailStorePrxHelper.uncheckedCast(
                create(omero.constants.THUMBNAILSTORE.value,
                        unsecureClient == null));
    }

    /**
     * Returns the {@link ExporterPrx} service.
     *   
     * @return See above.
     * @throws @throws Throwable Thrown if the service cannot be initialized.
     */
     ExporterPrx getExporterService()
            throws DSOutOfServiceException
    {
        return ExporterPrxHelper.uncheckedCast(
                create(omero.constants.EXPORTERSERVICE.value,
                        unsecureClient == null));
    }

    /**
     * Returns the {@link RawFileStorePrx} service.
     *  
     * @return See above.
     * @throws @throws Throwable Thrown if the service cannot be initialized.
     */
     RawFileStorePrx getRawFileService()
            throws DSOutOfServiceException
    {
        return RawFileStorePrxHelper.uncheckedCast(
                create(omero.constants.RAWFILESTORE.value,
                        unsecureClient == null));
    }

    /**
     * Returns the {@link RawPixelsStorePrx} service.
     * 
     * @return See above.
     * @throws Throwable Thrown if the service cannot be initialized.
     */
     RawPixelsStorePrx getPixelsStore()
            throws DSOutOfServiceException
    {
        return RawPixelsStorePrxHelper.uncheckedCast(
                create(omero.constants.RAWPIXELSSTORE.value,
                        unsecureClient == null));
    }

    /**
     * Returns the {@link IPixelsPrx} service.
     * 
     * @return See above.
     * @throws Throwable Thrown if the service cannot be initialized.
     */
     IPixelsPrx getPixelsService()
            throws DSOutOfServiceException
    {
        return IPixelsPrxHelper.uncheckedCast(
                get(omero.constants.PIXELSSERVICE.value,
                        unsecureClient == null));
    }

    /**
     * Returns the {@link SearchPrx} service.
     * 
     * @return See above.
     * @throws Throwable Thrown if the service cannot be initialized.
     */
     SearchPrx getSearchService()
            throws DSOutOfServiceException
    {
        return SearchPrxHelper.uncheckedCast(
                create(omero.constants.SEARCH.value, unsecureClient == null));
    }

    /**
     * Returns the {@link IProjectionPrx} service.
     * 
     * @return See above.
     * @throws Throwable Thrown if the service cannot be initialized.
     */
     IProjectionPrx getProjectionService()
            throws DSOutOfServiceException
    {
        return IProjectionPrxHelper.uncheckedCast(
                get(omero.constants.PROJECTIONSERVICE.value,
                        unsecureClient == null));
    }

    /**
     * Returns the {@link IAdminPrx} service.
     * 
     * @return See above.
     * @throws Throwable Thrown if the service cannot be initialized.
     */
     IAdminPrx getAdminService()
            throws DSOutOfServiceException
    {
        return getAdminService(unsecureClient == null);
    }


    /**
     * Returns the {@link IAdminPrx} service.
     *
     * @param secure Pass <code>true</code> to have a secure admin service,
     *               <code>false</code> otherwise.
     * @return See above.
     * @throws Throwable Thrown if the service cannot be initialized.
     */
     IAdminPrx getAdminService(boolean secure)
            throws DSOutOfServiceException
    {
        return IAdminPrxHelper.uncheckedCast(
                get(omero.constants.ADMINSERVICE.value, secure));
    }

    //
    // Irregular service lookups
    //

    /**
     * Creates or recycles the import store.
     * 
     * @return See above.
     * @throws Throwable Thrown if the service cannot be initialized.
     */
     OMEROMetadataStoreClient getImportStore()
            throws DSOutOfServiceException
    {
        OMEROMetadataStoreClient importStore = new OMEROMetadataStoreClient();
        try {
            if (entryUnencrypted != null) {
                // Note: this is a change. Now permit unencrypted import!
                importStore.initialize(entryUnencrypted);
            } else {
                importStore.initialize(entryEncrypted);
            }
            importStores.put(importStore, "");
            return importStore;
        } catch (Exception e) {
            throw new DSOutOfServiceException("Failed to create import store", e);
        }
     }

    /**
     * Returns the {@link RenderingEnginePrx Rendering service}.
     * 
     * @return See above.
     * @throws Throwable Thrown if the service cannot be initialized.
     */
     RenderingEnginePrx getRenderingService(long pixelsID, CompressionQuality compression)
            throws DSOutOfServiceException, ServerError
    {
        RenderingEnginePrx prx = null;
        try {
            if (entryUnencrypted != null) {
                prx = entryUnencrypted.createRenderingEngine();
            } else {
                prx = entryEncrypted.createRenderingEngine();
            }
        } catch (Exception e) {
            throw new DSOutOfServiceException("Could not get rendering engine", e);
        }

        // TODO: get compression value!
        float c = 1;
        prx.setCompressionLevel(c);
        reServices.put(pixelsID, prx);
        return prx;
    }

    /**
     * Rejoins the session.
     * 
     * @throws Throwable Thrown if an error occurred while rejoining the session.
     */
     void joinSession()
            throws Throwable
    {
        String uuid = secureClient.getSessionId();
        statelessServices.clear();
        reServices.clear();
        statefulServices.clear();
        secureClient.closeSession();
        if (unsecureClient != null) {
            unsecureClient.closeSession();
        }
        entryEncrypted = secureClient.joinSession(uuid);
        if (unsecureClient != null) { //we are in unsecured mode
            unsecureClient = null;
            entryUnencrypted = null;
            unsecureClient = secureClient.createClient(false);
            entryUnencrypted = unsecureClient.getSession();
        }
        
    }

    /**
     * Helper returning the group id, to be used after reconnect.
     * 
     * @return See above.
     */
     long getGroupID() { return context.getGroupID(); }

    //
    // Cleanup
    //

    /**
     * Closes the session.
     * 
     * @param networkup Pass <code>true</code> if the network is up,
     * <code>false</code> otherwise.
     */
     void close(boolean networkup)
            throws Throwable
    {
        secureClient.setFastShutdown(!networkup);
        if (unsecureClient != null) 
            unsecureClient.setFastShutdown(!networkup);
        if (networkup) {
            shutDownServices(true);
        }
        secureClient.__del__(); // Won't throw.
        if (unsecureClient != null) unsecureClient.__del__();
        closeDerived(networkup);
    }

    /**
     * Closes the services initialized by the importer
     * TODO: along with the TODO on derived, this will need to be reviewed
     * for race conditions.
     **/
     void closeImport()
    {
        shutdownImports();
        try {
            closeDerived(false);
        } catch (Throwable e) {
            log("Exception on closeDerived: " + e);
        }
    }

    /**
     * Closes the connectors associated to the master connector.
     * 
     * @param networkup Pass <code>true</code> if the network is up,
     * <code>false</code> otherwise.
     */
     void closeDerived(boolean networkup)
            throws Throwable
    {
        for (final Connector c : derived.asMap().values()) {
            try {
                c.close(networkup);
            } catch (Throwable e) {
                log(String.format("Failed to close(%s) service: %s",
                        networkup, c));
            }
        }
        derived.invalidateAll();
    }

    /** 
     * Shuts downs the stateful services.
     * 
     * @param rendering Pass <code>true</code> to shut down the rendering 
     * 					services, <code>false</code> otherwise.
     */
     void shutDownServices(boolean rendering)
    {
        shutdownStateful();
        shutdownImports();
        if (!rendering) return;
        Set<Long> tmp = new HashSet<Long>(reServices.keySet());
        for (Long pixelsId : tmp) {
            shutDownRenderingEngine(pixelsId);
        }
    }

    /**
     * Prints the stack trace and returns it as a string.
     * 
     * @return See above.
     */
     String getErrorMessage(Exception e)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
    
    /**
     * Keeps the services alive.
     * Returns <code>true</code> if success, <code>false</code> otherwise.
     * 
     * @return See above.
     */
     boolean keepSessionAlive()
    {
        boolean success = true;
        try {
            entryEncrypted.keepAllAlive(null);
        } catch (Exception e) {
            success = false;
            log("Failed encrypted keep alive: " + getErrorMessage(e));
        }
        try {
            if (entryUnencrypted != null && success)
                entryUnencrypted.keepAllAlive(null);
        } catch (Exception e) {
            success = false;
            log("failed unencrypted keep alive: " + getErrorMessage(e));
        }

        if (success) {
            lastKeepAlive.set(System.currentTimeMillis());
        }
        return success;
    }

    /**
     * Closes the specified proxy.
     * 
     * @param proxy The proxy to close.
     */
     void close(StatefulServiceInterfacePrx proxy)
    {
        if (proxy == null) {
            return;
        }

        try {
            proxy.close();
        } catch (Ice.ObjectNotExistException e) {
            // ignore
        } catch (Exception e) {
            log("Failed to close " + proxy + "(" + getErrorMessage(e) + ")");
        } finally {
            if (proxy instanceof RenderingEnginePrx) {
                Set<Long> keys = reServices.keySet();
                keys = Sets.newHashSet(keys);
                for (Long key : keys) {
                    reServices.remove(key, proxy);
                }
            } else {
                Set<String> keys = statefulServices.keySet();
                keys = Sets.newHashSet(keys);
                for (String key : keys) {
                    statefulServices.remove(key, proxy);
                }
            }
        }
    }

    /**
     * Shuts downs the rendering engine.
     * 
     * @param pixelsId The id of the pixels set.
     */
     void shutDownRenderingEngine(long pixelsId)
    {
        Collection<RenderingEnginePrx> proxies = reServices.removeAll(pixelsId);
        for (RenderingEnginePrx prx : proxies) {
            close(prx);
        }
    }

    /** Shuts down the import services.*/
     void shutdownImports() {
        Collection<OMEROMetadataStoreClient> imports = null;
        synchronized (importStores) {
            imports = importStores.keySet();
            importStores.clear();
        }

        for (OMEROMetadataStoreClient store : imports) {
            try {
                store.closeServices();
            } catch (Exception e) {
                log("Failed to close import store:" + getErrorMessage(e));
            }
        }
    }

    /** Shuts down the stateful services.*/
     void shutdownStateful() {
        Collection<StatefulServiceInterfacePrx> proxies = null;
        synchronized (statefulServices) {
            proxies = statefulServices.values();
            statefulServices.clear();
        }
        for (StatefulServiceInterfacePrx prx : proxies) {
            close(prx);
        }
    }

    /**
     * Returns the unsecured client if not <code>null</code> otherwise
     * returns the secured client.
     * 
     * @return See above.
     */
     client getClient()
    {
        if (unsecureClient != null) return unsecureClient;
        return secureClient;
    }

    /**
     * Executes the commands.
     * 
     * @param commands The commands to execute.
     * @param target The target context is any.
     * @return See above.
     */
     CmdCallbackI submit(List<Request> commands, SecurityContext target)
            throws Throwable
    {
        if (CollectionUtils.isEmpty(commands)) return null;
        DoAll all = new DoAll();
        all.requests = commands;
        Map<String, String> callContext = new HashMap<String, String>();
        if (target != null) {
            callContext.put("omero.group", ""+target.getGroupID());
        }
        if (entryUnencrypted != null) {
            return new CmdCallbackI(getClient(),
                    entryUnencrypted.submit(all, callContext));
        }
        return new CmdCallbackI(getClient(),
                entryEncrypted.submit(all, callContext));
    }

    /**
     * Returns the rendering engines that are currently active.
     * 
     * @return See above.
     */
     Map<SecurityContext, Set<Long>> getRenderingEngines()
    {
        Map<SecurityContext, Set<Long>>
        map = new HashMap<SecurityContext, Set<Long>>();
        Set<Long> list = new HashSet<Long>();
        Iterator<Long> i = reServices.keySet().iterator();
        while (i.hasNext())
            list.add(i.next());

        map.put(context, list);
        return map;
    }

    /**
     * Returns the connector associated to the specified user. If none exists,
     * creates a connector.
     * 
     * @param userName
     *            The name of the user. To be replaced by user's id.
     * @return See above.
     */
     Connector getConnector(final String userName)
            throws Throwable
    {
        if (CommonsLangUtils.isBlank(userName)) 
            return this;
        return derived.get(userName, new Callable<Connector>() {
            @Override
            public Connector call() throws Exception {
                if (groupName == null) {
                    ExperimenterGroup g = getAdminService().getGroup(
                            context.getGroupID());
                    groupName = g.getName().getValue();
                }
                // Create a connector.
                Principal p = new Principal();
                p.group = groupName;
                p.name = userName;
                p.eventType = "Sessions";
                ISessionPrx prx = entryEncrypted.getSessionService();
                Session session = prx.createSessionWithTimeout(p, 0L);
                // Create the userSession
                omero.client client = new omero.client(context
                        .getServerInformation().getHostname(), context
                        .getServerInformation().getPort());
                ServiceFactoryPrx userSession = client.createSession(session
                        .getUuid().getValue(), session.getUuid().getValue());
                final Connector c = new Connector(context.copy(), client,
                        userSession, unsecureClient == null, logger);
                log("Created derived connector: " + userName);
                return c;
            }
        });
    }
    
    //
    // HELPERS
    //

    /**
     * Returns <code>true</code> if the services need to be kept alive,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
     boolean needsKeepAlive()
    {
        long last = lastKeepAlive.get();
        long elapsed = System.currentTimeMillis() - last;
        return elapsed > ELAPSED_TIME;
    }

    /**
     * Recycles the specified service.
     * 
     * @param name The name of the service to create or recycle.
     * @param secure Pass <code>true</code> to create a secure object,
     * <code>false</code> otherwise.
     * @return See above.
     * @throws DSOutOfServiceException Thrown if an error occurred.
     */
    private ServiceInterfacePrx get(String name, boolean secure)
            throws DSOutOfServiceException {
        try {
            ServiceInterfacePrx prx = statelessServices.get(name);
            if (!secure && prx != null) { // Reload if secure is true.
                return prx;
            }
            if (!secure && entryUnencrypted != null) {
                prx = entryUnencrypted.getByName(name);
            } else {
                prx = entryEncrypted.getByName(name);
            }
            statelessServices.put(name, prx);
            return prx;
        } catch (Exception e) {
            throw new DSOutOfServiceException("Could not load " + name, e);
        }
    }

    /**
     * Creates the specified service.
     * 
     * @param name The name of the service to create or recycle.
     * @param secure Pass <code>true</code> to create a secure object,
     * <code>false</code> otherwise.
     * @return See above.
     * @throws DSOutOfServiceException Thrown if an error occurred.
     */
    private StatefulServiceInterfacePrx create(String name, boolean secure)
            throws DSOutOfServiceException {
        try {
            StatefulServiceInterfacePrx prx = null;
            if (!secure && entryUnencrypted != null) {
                prx = entryUnencrypted.createByName(name);
            } else {
                prx = entryEncrypted.createByName(name);
            }
            statefulServices.put(name, prx);
            return prx;
        } catch (Exception e) {
            throw new DSOutOfServiceException("Could not create " + name, e);
        }
    }

}
