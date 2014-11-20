package ome.formats.utests;

import java.util.List;
import java.util.Map;

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
import omero.api.IAdminPrx;
import omero.api.IConfigPrx;
import omero.api.IContainerPrx;
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
import omero.api.MetadataStorePrx;
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

import org.jmock.Mock;
import org.jmock.core.stub.DefaultResultStub;


public class TestServiceFactory
{

    /**
     * Using an instance method to return a proxy.
     * Rather than using the instance itself, we create
     * an actual mock which saves us from needing to
     * implement each method, which breaks fairly
     * often. Only the methods which need overriding
     */
    public ServiceFactoryPrx proxy()
    {
        Mock mock = new Mock(ServiceFactoryPrx.class);
        mock.setDefaultStub(new DefaultResultStub());
        return (ServiceFactoryPrx) mock.proxy();
    }

    public ServiceInterfacePrx getByName(String arg0) throws ServerError
    {
	if (arg0.equals(METADATASTORE.value))
	{
            Mock mock = new Mock(MetadataStorePrx.class);
            mock.setDefaultStub(new DefaultResultStub());
            return (MetadataStorePrx) mock.proxy();
	}
        return null;
    }

}
