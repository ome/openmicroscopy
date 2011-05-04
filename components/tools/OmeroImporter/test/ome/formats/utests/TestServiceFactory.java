package ome.formats.utests;

import java.util.List;
import java.util.Map;

import Glacier2.AMI_Session_destroy;
import Ice.AMI_Object_ice_flushBatchRequests;
import Ice.AMI_Object_ice_invoke;
import Ice.ByteSeqHolder;
import Ice.Communicator;
import Ice.Connection;
import Ice.Endpoint;
import Ice.EndpointSelectionType;
import Ice.Identity;
import Ice.LocatorPrx;
import Ice.ObjectPrx;
import Ice.OperationMode;
import Ice.RouterPrx;
import omero.ServerError;
import omero.api.ClientCallbackPrx;
import omero.api.GatewayPrx;
import omero.api.IAdminPrx;
import omero.api.IConfigPrx;
import omero.api.IContainerPrx;
import omero.api.IDeletePrx;
import omero.api.ILdapPrx;
import omero.api.IMetadataPrx;
import omero.api.IPixelsPrx;
import omero.api.IProjectionPrx;
import omero.api.IQueryPrx;
import omero.api.IRenderingSettingsPrx;
import omero.api.IRepositoryInfoPrx;
import omero.api.IRoiPrx;
import omero.api.IScriptPrx;
import omero.api.ISessionPrx;
import omero.api.ISharePrx;
import omero.api.ITimelinePrx;
import omero.api.ITypesPrx;
import omero.api.IUpdatePrx;
import omero.api.JobHandlePrx;
import omero.api.RawFileStorePrx;
import omero.api.RawPixelsStorePrx;
import omero.api.SearchPrx;
import omero.api.ServiceInterfacePrx;
import omero.api.StatefulServiceInterfacePrx;
import omero.api.ThumbnailStorePrx;
//import omero.system.OmeroContext;
import omero.api.ServiceFactoryPrx;
import omero.api.RenderingEnginePrx;
import omero.constants.METADATASTORE;
import omero.grid.InteractiveProcessorPrx;
import omero.model.IObject;
import omero.model.Job;


public class TestServiceFactory implements ServiceFactoryPrx
{

    public TestServiceFactory()
    {
    }

    public InteractiveProcessorPrx acquireProcessor(Job arg0, int arg1)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public InteractiveProcessorPrx acquireProcessor(Job arg0, int arg1,
            Map<String, String> arg2) throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public List<String> activeServices()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public List<String> activeServices(Map<String, String> arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public void close()
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void close(Map<String, String> arg0)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void closeOnDestroy()
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void closeOnDestroy(Map<String, String> arg0)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public StatefulServiceInterfacePrx createByName(String arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public StatefulServiceInterfacePrx createByName(String arg0,
            Map<String, String> arg1) throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public GatewayPrx createGateway() throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public GatewayPrx createGateway(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public JobHandlePrx createJobHandle() throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public JobHandlePrx createJobHandle(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public RawFileStorePrx createRawFileStore(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public RawPixelsStorePrx createRawPixelsStore(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public RenderingEnginePrx createRenderingEngine() throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public RenderingEnginePrx createRenderingEngine(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public SearchPrx createSearchService() throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public SearchPrx createSearchService(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ThumbnailStorePrx createThumbnailStore() throws ServerError
    {
        return null;
    }

    public ThumbnailStorePrx createThumbnailStore(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public void detachOnDestroy()
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void detachOnDestroy(Map<String, String> arg0)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public IAdminPrx getAdminService() throws ServerError
    {
        return null;
    }

    public IAdminPrx getAdminService(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ServiceInterfacePrx getByName(String arg0) throws ServerError
    {
    	if (arg0.equals(METADATASTORE.value))
    	{
    		return new TestMetadataStoreService();
    	}
        return null;
    }

    public ServiceInterfacePrx getByName(String arg0, Map<String, String> arg1)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public IConfigPrx getConfigService() throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public IConfigPrx getConfigService(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public IDeletePrx getDeleteService() throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public IDeletePrx getDeleteService(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ILdapPrx getLdapService() throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ILdapPrx getLdapService(Map<String, String> arg0) throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public IPixelsPrx getPixelsService() throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public IPixelsPrx getPixelsService(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public IContainerPrx getContainerService() throws ServerError
    {
        return null;
    }

    public IContainerPrx getContainerService(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public IProjectionPrx getProjectionService() throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public IProjectionPrx getProjectionService(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public IMetadataPrx getMetadataService() throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public IMetadataPrx getMetadataService(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    
    public IQueryPrx getQueryService() throws ServerError
    {
        return null;
    }

    public IQueryPrx getQueryService(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public IRenderingSettingsPrx getRenderingSettingsService()
            throws ServerError
    {
        return null;
    }

    public IRenderingSettingsPrx getRenderingSettingsService(
            Map<String, String> arg0) throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public IRepositoryInfoPrx getRepositoryInfoService() throws ServerError
    {
        return null;
    }

    public IRepositoryInfoPrx getRepositoryInfoService(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public IScriptPrx getScriptService() throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public IScriptPrx getScriptService(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ISessionPrx getSessionService() throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ISessionPrx getSessionService(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ISharePrx getShareService() throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ISharePrx getShareService(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ITimelinePrx getTimelineService() throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ITimelinePrx getTimelineService(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ITypesPrx getTypesService() throws ServerError
    {
    	return null;
    }

    public ITypesPrx getTypesService(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public IUpdatePrx getUpdateService() throws ServerError
    {
        return null;
    }

    public IUpdatePrx getUpdateService(Map<String, String> arg0)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean keepAlive(ServiceInterfacePrx arg0)
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean keepAlive(ServiceInterfacePrx arg0, Map<String, String> arg1)
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public long keepAllAlive(ServiceInterfacePrx[] arg0)
    {
		return arg0.length;
    }

    public long keepAllAlive(ServiceInterfacePrx[] arg0,
            Map<String, String> arg1)
    {
        // TODO Auto-generated method stub
        //return 0;
        throw new RuntimeException("Not implemented yet.");
    }

    public void setCallback(ClientCallbackPrx arg0)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void setCallback(ClientCallbackPrx arg0, Map<String, String> arg1)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void subscribe(String arg0, ObjectPrx arg1) throws ServerError
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void subscribe(String arg0, ObjectPrx arg1, Map<String, String> arg2)
            throws ServerError
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void destroy()
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void destroy(Map<String, String> arg0)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean destroy_async(AMI_Session_destroy arg0)
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean destroy_async(AMI_Session_destroy arg0,
            Map<String, String> arg1)
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_adapterId(String arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_batchDatagram()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_batchOneway()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_collocationOptimized(boolean arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_compress(boolean arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_connectionCached(boolean arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_connectionId(String arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_context(Map<String, String> arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_datagram()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_defaultContext()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_endpointSelection(EndpointSelectionType arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_endpoints(Endpoint[] arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_facet(String arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public void ice_flushBatchRequests()
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean ice_flushBatchRequests_async(
            AMI_Object_ice_flushBatchRequests arg0)
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public String ice_getAdapterId()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public Connection ice_getCachedConnection()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public Communicator ice_getCommunicator()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public Connection ice_getConnection()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public Map<String, String> ice_getContext()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public EndpointSelectionType ice_getEndpointSelection()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public Endpoint[] ice_getEndpoints()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public String ice_getFacet()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public int ice_getHash()
    {
        // TODO Auto-generated method stub
        //return 0;
        throw new RuntimeException("Not implemented yet.");
    }

    public Identity ice_getIdentity()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public LocatorPrx ice_getLocator()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public int ice_getLocatorCacheTimeout()
    {
        // TODO Auto-generated method stub
        //return 0;
        throw new RuntimeException("Not implemented yet.");
    }

    public RouterPrx ice_getRouter()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public String ice_id()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public String ice_id(Map<String, String> arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_identity(Identity arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public String[] ice_ids()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public String[] ice_ids(Map<String, String> arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean ice_invoke(String arg0, OperationMode arg1, byte[] arg2,
            ByteSeqHolder arg3)
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean ice_invoke(String arg0, OperationMode arg1, byte[] arg2,
            ByteSeqHolder arg3, Map<String, String> arg4)
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean ice_invoke_async(AMI_Object_ice_invoke arg0, String arg1,
            OperationMode arg2, byte[] arg3)
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean ice_invoke_async(AMI_Object_ice_invoke arg0, String arg1,
            OperationMode arg2, byte[] arg3, Map<String, String> arg4)
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean ice_isA(String arg0)
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean ice_isA(String arg0, Map<String, String> arg1)
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean ice_isBatchDatagram()
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean ice_isBatchOneway()
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean ice_isCollocationOptimized()
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean ice_isConnectionCached()
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean ice_isDatagram()
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean ice_isOneway()
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean ice_isPreferSecure()
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean ice_isSecure()
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean ice_isTwoway()
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_locator(LocatorPrx arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_locatorCacheTimeout(int arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_oneway()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public void ice_ping()
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void ice_ping(Map<String, String> arg0)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_preferSecure(boolean arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_router(RouterPrx arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_secure(boolean arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_timeout(int arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public String ice_toString()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public ObjectPrx ice_twoway()
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public RawFileStorePrx createRawFileStore() throws ServerError
    {
        return null;
    }

    public RawPixelsStorePrx createRawPixelsStore() throws ServerError
    {
        return null;
    }

    public IRoiPrx getRoiService() throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public IRoiPrx getRoiService(Map<String, String> __ctx) throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }
    
    public omero.api.ExporterPrx createExporter() throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public omero.api.ExporterPrx createExporter(Map<String, String> __ctx) throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }
    
    public omero.grid.SharedResourcesPrx sharedResources() {
    	return null;
    }

    public omero.grid.SharedResourcesPrx sharedResources(java.util.Map<java.lang.String,java.lang.String> arg0) {
    	return null;
    }

    public List<IObject> getSecurityContexts() throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public List<IObject> getSecurityContexts(Map<String, String> __ctx)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public IObject setSecurityContext(IObject obj) throws ServerError {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public void setSecurityPassword(String password) throws ServerError {
        // TODO Auto-generated method stub
        throw new RuntimeException("Not implemented yet.");
    }

    public void setSecurityPassword(String password, Map<String, String> __ctx)
            throws ServerError {
        // TODO Auto-generated method stub
        throw new RuntimeException("Not implemented yet.");
    }

    public IObject setSecurityContext(IObject obj, Map<String, String> __ctx)
            throws ServerError {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }
}
