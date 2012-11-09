/*
 * org.openmicroscopy.shoola.env.data.Connector 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
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
package org.openmicroscopy.shoola.env.data;


//Java imports
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.formats.OMEROMetadataStoreClient;
import omero.client;
import omero.api.ExporterPrx;
import omero.api.IAdminPrx;
import omero.api.IConfigPrx;
import omero.api.IContainerPrx;
import omero.api.IDeletePrx;
import omero.api.IMetadataPrx;
import omero.api.IPixelsPrx;
import omero.api.IProjectionPrx;
import omero.api.IQueryPrx;
import omero.api.IRenderingSettingsPrx;
import omero.api.IRepositoryInfoPrx;
import omero.api.IRoiPrx;
import omero.api.IScriptPrx;
import omero.api.IUpdatePrx;
import omero.api.RawFileStorePrx;
import omero.api.RawPixelsStorePrx;
import omero.api.RenderingEnginePrx;
import omero.api.SearchPrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceInterfacePrx;
import omero.api.StatefulServiceInterfacePrx;
import omero.api.ThumbnailStorePrx;
import omero.cmd.DoAll;
import omero.cmd.Request;
import omero.grid.SharedResourcesPrx;

import org.openmicroscopy.shoola.env.data.util.SecurityContext;


/** 
 * Manages the various services and entry points.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
class Connector
{
	
	/** The thumbnail service. */
	private ThumbnailStorePrx thumbnailService;

	/** The raw file store. */
	private RawFileStorePrx fileStore;
	
	/** The raw pixels store. */
	private RawPixelsStorePrx pixelsStore;

	/** The projection service. */
	private IProjectionPrx projService;
	
	/** The query service. */
	private IQueryPrx queryService;
	
	/** The rendering settings service. */
	private IRenderingSettingsPrx rndSettingsService;
	
	/** The repository service. */
	private IRepositoryInfoPrx repInfoService;
	
	/** The delete service. */
	private IDeletePrx deleteService;
	
	/** The pixels service. */
	private IPixelsPrx pixelsService;
	
	/** The container service. */
	private IContainerPrx pojosService;
	
	/** The update service. */
	private IUpdatePrx updateService;
	
	/** The metadata service. */
	private IMetadataPrx metadataService;
	
	/** The scripting service. */
	private IScriptPrx scriptService;
	
	/** The ROI (Region of Interest) service. */
	private IRoiPrx roiService;
	
	/** The Admin service. */
	private IAdminPrx adminService;
	
	/** The shared resources. */
	private SharedResourcesPrx sharedResources;
	
	/** The service to import files. */
	private OMEROMetadataStoreClient importStore;
	
	/** The search service.*/
	private SearchPrx searchService;
	
	/** The exporter service.*/
	private ExporterPrx exportService;
	
	/** 
	 * The Blitz client object, this is the entry point to the 
	 * OMERO Server using a secure connection. 
	 */
	private client secureClient;

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
	
	/** Collection of services to keep alive. */
	private Set<ServiceInterfacePrx> services;
	
	/** Collection of services to keep alive. */
	private Map<Long, StatefulServiceInterfacePrx> reServices;
	
	/**
	 * The number of thumbnails already retrieved. Resets to <code>0</code>
	 * when the value equals {@link OMEROGateway#MAX_RETRIEVAL}.
	 */
	private int thumbRetrieval;
	
	/** The security context for that connector.*/
	private SecurityContext context;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param context The context hosting information about the user.
	 * @param secureClient The entry point to server.
	 * @param entryEncrypted The entry point to access the various services.
	 * @param encrypted The entry point to access the various services.
	 * @throws Throwable Thrown if entry points cannot be initialized.
	 */
	Connector(SecurityContext context, client secureClient,
			ServiceFactoryPrx entryEncrypted, boolean encrypted)
			throws Throwable
	{
		if (context == null)
			throw new IllegalArgumentException("No Security context.");
		if (secureClient == null)
			throw new IllegalArgumentException("No Server entry point.");
		if (entryEncrypted == null)
			throw new IllegalArgumentException("No Services entry point.");
		if (!encrypted) {
			unsecureClient = secureClient.createClient(false);
			entryUnencrypted = unsecureClient.getSession();
		}
		this.secureClient = secureClient;
		this.entryEncrypted = entryEncrypted;
		this.context = context;
		thumbRetrieval = 0;
		services = new HashSet<ServiceInterfacePrx>();
		reServices = new HashMap<Long, StatefulServiceInterfacePrx>();
	}

	/**
	 * Returns the {@link SharedResourcesPrx} service.
	 * 
	 * @return See above.
	 * @throws Throwable Thrown if the service cannot be initialized.
	 */
	SharedResourcesPrx getSharedResources()
		throws Throwable
	{
		if (sharedResources == null)
			sharedResources = entryEncrypted.sharedResources();
		return sharedResources;
	}
	
	/**
	 * Returns the {@link IRenderingSettingsPrx} service.
	 * 
	 * @return See above.
	 * @throws Throwable Thrown if the service cannot be initialized.
	 */
	IRenderingSettingsPrx getRenderingSettingsService()
		throws Throwable
	{
		if (rndSettingsService == null) {
			if (entryUnencrypted != null)
				rndSettingsService = 
					entryUnencrypted.getRenderingSettingsService();
			else 
				rndSettingsService = 
					entryEncrypted.getRenderingSettingsService();
			
			if (rndSettingsService != null)
				services.add(rndSettingsService);
		}
		return rndSettingsService;
	}

	/**
	 * Creates or recycles the import store.
	 * 
	 * @return See above.
	 * @throws Throwable Thrown if the service cannot be initialized.
	 */
	OMEROMetadataStoreClient getImportStore()
		throws Throwable
	{
		if (importStore == null) {
			importStore = new OMEROMetadataStoreClient();
			importStore.initialize(entryEncrypted);
		}
		return importStore;
	}
	
	/**
	 * Returns the {@link IRepositoryInfoPrx} service.
	 * 
	 * @return See above.
	 * @throws Throwable Thrown if the service cannot be initialized.
	 */
	IRepositoryInfoPrx getRepositoryService()
		throws Throwable
	{
		if (repInfoService == null) {
			if (entryUnencrypted != null)
				repInfoService = 
					entryUnencrypted.getRepositoryInfoService();
			else repInfoService = 
					entryEncrypted.getRepositoryInfoService();
			if (repInfoService != null)
				services.add(repInfoService);
		}
		return repInfoService;
	}

	/**
	 * Returns the {@link IScriptPrx} service.
	 * 
	 * @return See above.
	 * @throws Throwable Thrown if the service cannot be initialized.
	 */
	IScriptPrx getScriptService()
		throws Throwable
	{ 
		if (scriptService == null) {
			if (entryUnencrypted != null)
				scriptService = entryUnencrypted.getScriptService();
			else scriptService = entryEncrypted.getScriptService();
			if (scriptService != null)
				services.add(scriptService);
		}
		return scriptService; 
	}
	
	/**
	 * Returns the {@link IContainerPrx} service.
	 * 
	 * @return See above.
	 * @throws Throwable Thrown if the service cannot be initialized.
	 */
	IContainerPrx getPojosService()
		throws Throwable
	{
		if (pojosService == null) {
			if (entryUnencrypted != null)
				pojosService = entryUnencrypted.getContainerService();
			else pojosService = entryEncrypted.getContainerService();
			if (pojosService != null)
				services.add(pojosService);
		}
		return pojosService;
	}

	/**
	 * Returns the {@link IQueryPrx} service.
	 *  
	 * @return See above.
	 * @throws Throwable Thrown if the service cannot be initialized.
	 */
	IQueryPrx getQueryService()
		throws Throwable
	{ 
		if (queryService == null) {
			if (entryUnencrypted != null)
				queryService = entryUnencrypted.getQueryService();
			else queryService = entryEncrypted.getQueryService();
			if (queryService != null)
				services.add(queryService);
		}
		return queryService;
	}
	
	/**
	 * Returns the {@link IUpdatePrx} service.
	 *  
	 * @return See above.
	 * @throws Throwable Thrown if the service cannot be initialized.
	 */
	IUpdatePrx getUpdateService()
		throws Throwable
	{ 
		if (updateService == null) {
			if (entryUnencrypted != null)
				updateService = entryUnencrypted.getUpdateService();
			else updateService = entryEncrypted.getUpdateService();
			if (updateService != null)
				services.add(updateService);
		}
		return updateService;
	}

	/**
	 * Returns the {@link IMetadataPrx} service.
	 *  
	 * @return See above.
	 * @throws Throwable Thrown if the service cannot be initialized.
	 */
	IMetadataPrx getMetadataService()
		throws Throwable
	{ 
		if (metadataService == null) {
			if (entryUnencrypted != null)
				metadataService = entryUnencrypted.getMetadataService();
			else metadataService = entryEncrypted.getMetadataService();
			if (metadataService != null)
				services.add(metadataService);
		}
		return metadataService;
	}

	/**
	 * Returns the {@link IRoiPrx} service.
	 *  
	 * @return See above.
	 * @throws Throwable Thrown if the service cannot be initialized.
	 */
	IRoiPrx getROIService()
		throws Throwable
	{ 
		if (roiService == null) {
			if (entryUnencrypted != null)
				roiService = entryUnencrypted.getRoiService();
			else roiService = entryEncrypted.getRoiService();
			if (roiService != null) services.add(roiService);
		}
		return roiService; 
	}

	/**
	 * Returns the {@link IConfigPrx} service.
	 * 
	 * @return See above.
	 * @throws Throwable Thrown if the service cannot be initialized.
	 */
	IConfigPrx getConfigService()
		throws Throwable
	{
		if (entryUnencrypted != null)
			return entryUnencrypted.getConfigService();
		return entryEncrypted.getConfigService();
	}
	
	/**
	 * Returns the {@link IDeletePrx} service.
	 * 
	 * @return See above.
	 * @throws Throwable Thrown if the service cannot be initialized.
	 */
	IDeletePrx getDeleteService()
		throws Throwable
	{ 
		if (deleteService == null) {
			if (entryUnencrypted != null)
				deleteService = entryUnencrypted.getDeleteService();
			else 
				deleteService = entryEncrypted.getDeleteService();
			if (deleteService != null)
				services.add(deleteService);
		}
		return deleteService;
	}
	
	/**
	 * Returns the {@link ThumbnailStorePrx} service.
	 *
	 * @param value The number of elements to retrieve.
	 * @return See above.
	 * @throws Throwable Thrown if the service cannot be initialized.
	 */
	ThumbnailStorePrx getThumbnailService(int value)
		throws Throwable
	{ 
		thumbRetrieval += value;
		if (thumbRetrieval >= OMEROGateway.MAX_RETRIEVAL) {
			thumbRetrieval = 0;
			//to be on the save side
			if (thumbnailService != null) thumbnailService.close();
			services.remove(thumbnailService);
			thumbnailService = null;
		}
		if (thumbnailService == null) {
			if (entryUnencrypted != null)
				thumbnailService = entryUnencrypted.createThumbnailStore();
			else 
				thumbnailService = entryEncrypted.createThumbnailStore();
			if (thumbnailService != null)
				services.add(thumbnailService);
		}
		return thumbnailService;
	}

	/**
	 * Returns the {@link ExporterPrx} service.
	 *   
	 * @return See above.
	 * @throws @throws Throwable Thrown if the service cannot be initialized.
	 */
	ExporterPrx getExporterService()
		throws Throwable
	{ 
		if (exportService != null) return exportService;
		if (entryUnencrypted != null)
			exportService = entryUnencrypted.createExporter();
		else exportService = entryEncrypted.createExporter();
		services.add(exportService);
		return exportService;
	}
	
	/**
	 * Returns the {@link RawFileStorePrx} service.
	 *  
	 * @return See above.
	 * @throws @throws Throwable Thrown if the service cannot be initialized.
	 */
	RawFileStorePrx getRawFileService()
		throws Throwable
	{
		if (entryUnencrypted != null)
			return entryUnencrypted.createRawFileStore();
		return entryEncrypted.createRawFileStore();
	}

	/**
	 * Returns the {@link RenderingEnginePrx Rendering service}.
	 * 
	 * @return See above.
	 * @throws Throwable Thrown if the service cannot be initialized.
	 */
	RenderingEnginePrx getRenderingService(long pixelsID)
		throws Throwable
	{
		RenderingEnginePrx prx;
		if (entryUnencrypted != null)
			prx = entryUnencrypted.createRenderingEngine();
		else prx = entryEncrypted.createRenderingEngine();
		prx.setCompressionLevel(context.getCompression());
		reServices.put(pixelsID, prx);
		return prx;
	}

	/**
	 * Returns the {@link RawPixelsStorePrx} service.
	 * 
	 * @return See above.
	 * @throws Throwable Thrown if the service cannot be initialized.
	 */
	RawPixelsStorePrx getPixelsStore()
		throws Throwable
	{
		if (entryUnencrypted != null)
			return entryUnencrypted.createRawPixelsStore();
		return entryEncrypted.createRawPixelsStore();
	}

	/**
	 * Returns the {@link IPixelsPrx} service.
	 * 
	 * @return See above.
	 * @throws Throwable Thrown if the service cannot be initialized.
	 */
	IPixelsPrx getPixelsService()
		throws Throwable
	{ 
		if (pixelsService == null) {
			if (entryUnencrypted != null)
				pixelsService = entryUnencrypted.getPixelsService();
			else 
				pixelsService = entryEncrypted.getPixelsService();
			if (pixelsService == null)
				throw new DSOutOfServiceException(
						"Cannot access the Pixels service.");
			services.add(pixelsService);
		}
		return pixelsService;
	}
	
	/**
	 * Returns the {@link SearchPrx} service.
	 * 
	 * @return See above.
	 * @throws Throwable Thrown if the service cannot be initialized.
	 */
	SearchPrx getSearchService()
		throws Throwable
	{
		if (searchService != null) return searchService;
		if (entryUnencrypted != null)
			searchService = entryUnencrypted.createSearchService();
		else searchService = entryEncrypted.createSearchService();
		services.add(searchService);
		return searchService;
	}
	
	/**
	 * Returns the {@link IProjectionPrx} service.
	 * 
	 * @return See above.
	 * @throws Throwable Thrown if the service cannot be initialized.
	 */
	IProjectionPrx getProjectionService()
		throws Throwable
	{
		if (projService == null) {
			if (entryUnencrypted != null)
				projService = entryUnencrypted.getProjectionService();
			else projService = entryEncrypted.getProjectionService();
			if (projService != null)
				services.add(projService);
		}
		return projService;
	}

	/**
	 * Returns the {@link IAdminPrx} service.
	 * 
	 * @return See above.
	 * @throws Throwable Thrown if the service cannot be initialized.
	 */
	IAdminPrx getAdminService()
		throws Throwable
	{ 
		if (adminService == null) {
			adminService = entryEncrypted.getAdminService();
			if (adminService != null)
				services.add(adminService);
		}
		return adminService; 
	}
	
	/** Clears the data. */
	void clear()
	{
		services.clear();
		reServices.clear();
		adminService = null;
		thumbnailService = null;
		fileStore = null;
		metadataService = null;
		pojosService = null;
		projService = null;
		queryService = null;
		rndSettingsService = null;
		repInfoService = null;
		deleteService = null;
		pixelsService = null;
		roiService = null;
		pixelsStore = null;
		updateService = null;
		scriptService = null;
		sharedResources = null;
		importStore = null;
	}
	
	/** 
	 * Closes the session.
	 * 
	 * @param networkup Pass <code>true</code> if the network is up,
	 * <code>false</code> otherwise.
	 */
	void close(boolean networkup)
		throws Throwable
	{
		if (networkup) {
			shutDownServices(true);
			secureClient.closeSession();
		}
	}
	
	/** 
	 * Tries to reconnect to the server. Returns <code>true</code>
	 * if it was possible to reconnect, <code>false</code>
	 * otherwise.
	 * 
	 * @param userName The user name to be used for login.
	 * @param password The password to be used for login.
	 * @return See above.
	 */
	void reconnect(String name, String password)
		throws Throwable
	{
		entryEncrypted =  secureClient.createSession(name, password);
		if (entryUnencrypted != null) {
			unsecureClient = secureClient.createClient(false);
			entryUnencrypted = unsecureClient.getSession();
		}
	}
	
	/** Closes the services initialized by the importer.*/
	void closeImport()
	{
		if (importStore != null) {
			importStore.closeServices();
			importStore = null;
		}
	}
	
	/** 
	 * Shuts downs the stateful services. 
	 * 
	 * @param rendering Pass <code>true</code> to shut down the rendering 
	 * 					services, <code>false</code> otherwise.
	 */
	void shutDownServices(boolean rendering)
	{
		if (thumbnailService != null) close(thumbnailService);
		if (pixelsStore != null) close(pixelsStore);
		if (fileStore != null) close(fileStore);
		if (searchService != null) close(searchService);
		if (importStore != null) {
			importStore.closeServices();
			importStore = null;
		}
		
		Collection<StatefulServiceInterfacePrx> l = reServices.values();
		if (l != null && rendering) {
			Iterator<StatefulServiceInterfacePrx> i = l.iterator();
			while (i.hasNext()) {
				close(i.next());
			}
			reServices.clear();
		}
	}
	
	/** Keeps services alive.*/
	void ping()
		throws Exception
	{
		entryEncrypted.keepAlive(null);
	}
	
	/** Keeps the services alive. */
	void keepSessionAlive()
	{
		try {
			entryEncrypted.keepAllAlive(null);
		} catch (Exception e) {}
		try {
			if (entryUnencrypted != null)
				entryUnencrypted.keepAllAlive(null);
		} catch (Exception e) {}
	}

	/**
	 * Closes the specified proxy.
	 * 
	 * @param proxy The proxy to close.
	 */
	void close(StatefulServiceInterfacePrx proxy)
	{
		try {
			proxy.close();
		} catch (Exception e) {} //ignore.
		if (proxy == thumbnailService) {
			services.remove(thumbnailService);
			thumbnailService = null;
		}
		if (proxy == pixelsStore) {
			services.remove(pixelsStore);
			pixelsStore = null;
		}
		if (proxy == fileStore) {
			services.remove(fileStore);
			fileStore = null;
		}
		if (proxy == searchService) {
			services.remove(searchService);
			searchService = null;
		}
		if (proxy == exportService) {
			services.remove(exportService);
			exportService = null;
		}
	}
	
	/**
	 * Shuts downs the rendering engine.
	 * 
	 * @param pixelsId The id of the pixels set.
	 */
	void shutDownRenderingEngine(long pixelsId)
	{
		try {
			StatefulServiceInterfacePrx proxy = reServices.get(pixelsId);
			if (proxy != null) proxy.close();
			reServices.remove(pixelsId);
		} catch (Exception e) {
		}
	}
	
	/**
	 * Returns <code>true</code> if it is the connector corresponding to the
	 * passed context, <code>false</code> otherwise.
	 * 
	 * @param ctx The security context.
	 * @return See above.
	 */
	boolean isSame(SecurityContext ctx)
	{
		if (ctx == null) return false;
		//ignore server for now.
		return ctx.getGroupID() == context.getGroupID();
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
	RequestCallback submit(List<Request> commands, SecurityContext target)
		throws Throwable
	{
		if (commands == null || commands.size() == 0)
			return null;
		DoAll all = new DoAll();
		all.requests = commands;
		Map<String, String> callContext = new HashMap<String, String>();
		if (target != null) {
			callContext.put("omero.group", ""+target.getGroupID());
		}
		if (entryUnencrypted != null) {
			return new RequestCallback(getClient(), 
					entryUnencrypted.submit(all, callContext));
		}
		return new RequestCallback(getClient(), 
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
	
}