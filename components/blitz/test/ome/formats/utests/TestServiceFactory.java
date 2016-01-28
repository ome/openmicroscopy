package ome.formats.utests;

import omero.ServerError;
import omero.api.MetadataStorePrx;
import omero.api.ServiceInterfacePrx;
import omero.api.ServiceFactoryPrx;
import omero.constants.METADATASTORE;

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
