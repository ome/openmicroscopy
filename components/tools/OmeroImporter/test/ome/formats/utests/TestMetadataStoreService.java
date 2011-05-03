package ome.formats.utests;

import java.util.List;
import java.util.Map;

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
import omero.grid.InteractiveProcessorPrx;
import omero.metadatastore.IObjectContainer;
import omero.model.Pixels;
import omero.sys.EventContext;

import omero.api.AMI_MetadataStore_createRoot;
import omero.api.AMI_MetadataStore_populateMinMax;
import omero.api.AMI_MetadataStore_postProcess;
import omero.api.AMI_MetadataStore_saveToDB;
import omero.api.AMI_MetadataStore_setPixelsParams;
import omero.api.AMI_MetadataStore_updateObjects;
import omero.api.AMI_MetadataStore_updateReferences;
import omero.api.AMI_StatefulServiceInterface_activate;
import omero.api.AMI_StatefulServiceInterface_close;
import omero.api.AMI_StatefulServiceInterface_getCurrentEventContext;
import omero.api.AMI_StatefulServiceInterface_passivate;
import omero.api.MetadataStorePrx;


public class TestMetadataStoreService implements MetadataStorePrx
{

	public void createRoot() throws ServerError {
		// TODO Auto-generated method stub
		
	}

	public void createRoot(Map<String, String> arg0) throws ServerError {
		// TODO Auto-generated method stub
		
	}

	public boolean createRoot_async(AMI_MetadataStore_createRoot arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean createRoot_async(AMI_MetadataStore_createRoot arg0,
			Map<String, String> arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public void populateMinMax(double[][][] arg0) throws ServerError {
		// TODO Auto-generated method stub
		
	}

	public void populateMinMax(double[][][] arg0, Map<String, String> arg1)
			throws ServerError {
		// TODO Auto-generated method stub
		
	}

	public boolean populateMinMax_async(AMI_MetadataStore_populateMinMax arg0,
			double[][][] arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean populateMinMax_async(AMI_MetadataStore_populateMinMax arg0,
			double[][][] arg1, Map<String, String> arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	public List<Pixels> saveToDB() throws ServerError {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Pixels> saveToDB(Map<String, String> arg0) throws ServerError {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean saveToDB_async(AMI_MetadataStore_saveToDB arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean saveToDB_async(AMI_MetadataStore_saveToDB arg0,
			Map<String, String> arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public void updateObjects(IObjectContainer[] arg0) throws ServerError {
		// TODO Auto-generated method stub
		
	}

	public void updateObjects(IObjectContainer[] arg0, Map<String, String> arg1)
			throws ServerError {
		// TODO Auto-generated method stub
		
	}

	public boolean updateObjects_async(AMI_MetadataStore_updateObjects arg0,
			IObjectContainer[] arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean updateObjects_async(AMI_MetadataStore_updateObjects arg0,
			IObjectContainer[] arg1, Map<String, String> arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	public void updateReferences(Map<String, String[]> arg0) throws ServerError {
		// TODO Auto-generated method stub
		
	}

	public void updateReferences(Map<String, String[]> arg0,
			Map<String, String> arg1) throws ServerError {
		// TODO Auto-generated method stub
		
	}

	public boolean updateReferences_async(
			AMI_MetadataStore_updateReferences arg0, Map<String, String[]> arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean updateReferences_async(
			AMI_MetadataStore_updateReferences arg0,
			Map<String, String[]> arg1, Map<String, String> arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	public void activate() throws ServerError {
		// TODO Auto-generated method stub
		
	}

	public void activate(Map<String, String> arg0) throws ServerError {
		// TODO Auto-generated method stub
		
	}

	public boolean activate_async(AMI_StatefulServiceInterface_activate arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean activate_async(AMI_StatefulServiceInterface_activate arg0,
			Map<String, String> arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public void close() throws ServerError {
		// TODO Auto-generated method stub
		
	}

	public void close(Map<String, String> arg0) throws ServerError {
		// TODO Auto-generated method stub
		
	}

	public boolean close_async(AMI_StatefulServiceInterface_close arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean close_async(AMI_StatefulServiceInterface_close arg0,
			Map<String, String> arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public EventContext getCurrentEventContext() throws ServerError {
		// TODO Auto-generated method stub
		return null;
	}

	public EventContext getCurrentEventContext(Map<String, String> arg0)
			throws ServerError {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getCurrentEventContext_async(
			AMI_StatefulServiceInterface_getCurrentEventContext arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean getCurrentEventContext_async(
			AMI_StatefulServiceInterface_getCurrentEventContext arg0,
			Map<String, String> arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public void passivate() throws ServerError {
		// TODO Auto-generated method stub
		
	}

	public void passivate(Map<String, String> arg0) throws ServerError {
		// TODO Auto-generated method stub
		
	}

	public boolean passivate_async(AMI_StatefulServiceInterface_passivate arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean passivate_async(AMI_StatefulServiceInterface_passivate arg0,
			Map<String, String> arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public ObjectPrx ice_adapterId(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectPrx ice_batchDatagram() {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectPrx ice_batchOneway() {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectPrx ice_collocationOptimized(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectPrx ice_compress(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectPrx ice_connectionCached(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectPrx ice_connectionId(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectPrx ice_context(Map<String, String> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectPrx ice_datagram() {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectPrx ice_defaultContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectPrx ice_endpointSelection(EndpointSelectionType arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectPrx ice_endpoints(Endpoint[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectPrx ice_facet(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void ice_flushBatchRequests() {
		// TODO Auto-generated method stub
		
	}

	public boolean ice_flushBatchRequests_async(
			AMI_Object_ice_flushBatchRequests arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public String ice_getAdapterId() {
		// TODO Auto-generated method stub
		return null;
	}

	public Connection ice_getCachedConnection() {
		// TODO Auto-generated method stub
		return null;
	}

	public Communicator ice_getCommunicator() {
		// TODO Auto-generated method stub
		return null;
	}

	public Connection ice_getConnection() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> ice_getContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public EndpointSelectionType ice_getEndpointSelection() {
		// TODO Auto-generated method stub
		return null;
	}

	public Endpoint[] ice_getEndpoints() {
		// TODO Auto-generated method stub
		return null;
	}

	public String ice_getFacet() {
		// TODO Auto-generated method stub
		return null;
	}

	public int ice_getHash() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Identity ice_getIdentity() {
		// TODO Auto-generated method stub
		return null;
	}

	public LocatorPrx ice_getLocator() {
		// TODO Auto-generated method stub
		return null;
	}

	public int ice_getLocatorCacheTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	public RouterPrx ice_getRouter() {
		// TODO Auto-generated method stub
		return null;
	}

	public String ice_id() {
		// TODO Auto-generated method stub
		return null;
	}

	public String ice_id(Map<String, String> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectPrx ice_identity(Identity arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] ice_ids() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] ice_ids(Map<String, String> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean ice_invoke(String arg0, OperationMode arg1, byte[] arg2,
			ByteSeqHolder arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean ice_invoke(String arg0, OperationMode arg1, byte[] arg2,
			ByteSeqHolder arg3, Map<String, String> arg4) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean ice_invoke_async(AMI_Object_ice_invoke arg0, String arg1,
			OperationMode arg2, byte[] arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean ice_invoke_async(AMI_Object_ice_invoke arg0, String arg1,
			OperationMode arg2, byte[] arg3, Map<String, String> arg4) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean ice_isA(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean ice_isA(String arg0, Map<String, String> arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean ice_isBatchDatagram() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean ice_isBatchOneway() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean ice_isCollocationOptimized() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean ice_isConnectionCached() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean ice_isDatagram() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean ice_isOneway() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean ice_isPreferSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean ice_isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean ice_isTwoway() {
		// TODO Auto-generated method stub
		return false;
	}

	public ObjectPrx ice_locator(LocatorPrx arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectPrx ice_locatorCacheTimeout(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectPrx ice_oneway() {
		// TODO Auto-generated method stub
		return null;
	}

	public void ice_ping() {
		// TODO Auto-generated method stub
		
	}

	public void ice_ping(Map<String, String> arg0) {
		// TODO Auto-generated method stub
		
	}

	public ObjectPrx ice_preferSecure(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectPrx ice_router(RouterPrx arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectPrx ice_secure(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectPrx ice_timeout(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String ice_toString() {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectPrx ice_twoway() {
		// TODO Auto-generated method stub
		return null;
	}

    public List<InteractiveProcessorPrx> postProcess() throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public List<InteractiveProcessorPrx> postProcess(Map<String, String> __ctx)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean postProcess_async(AMI_MetadataStore_postProcess __cb) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean postProcess_async(AMI_MetadataStore_postProcess __cb,
            Map<String, String> __ctx) {
        // TODO Auto-generated method stub
        return false;
    }

    public void setPixelsParams(long pixelsId, boolean useOriginalFile,
            Map<String, String> params) throws ServerError
    {
        // TODO Auto-generated method stub
    }

    public void setPixelsParams(long pixelsId, boolean useOriginalFile,
            Map<String, String> params, Map<String, String> ctx)
        throws ServerError
    {
        // TODO Auto-generated method stub
    }

    public boolean setPixelsParams_async(AMI_MetadataStore_setPixelsParams cb,
            long pixelsId, boolean useOriginalFile, Map<String, String> params)
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean setPixelsParams_async(
            AMI_MetadataStore_setPixelsParams cb, long pixelsId,
            boolean useOriginalFile, Map<String, String> params,
            Map<String, String> ctx)
    {
        // TODO Auto-generated method stub
        return false;
    }

}
