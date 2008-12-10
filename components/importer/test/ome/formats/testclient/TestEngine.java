package ome.formats.testclient;

import ome.formats.OMEROMetadataStore;
import ome.formats.enums.EnumerationProvider;
import ome.formats.importer.OMEROWrapper;
import ome.formats.testclient.TestServiceFactory;
import ome.system.ServiceFactory;

public class TestEngine
{   
    private TestEngine() throws Exception
    {
        OMEROWrapper wrapper = new OMEROWrapper();
        ServiceFactory sf = new TestServiceFactory();
        EnumerationProvider ep = new TestEnumerationProvider();
        OMEROMetadataStore store = new OMEROMetadataStore(sf);
        
        store.setEnumerationProvider(ep);
        wrapper.setMetadataStore(store);
        //wrapper.setId("/Users/TheBrain/test_images/dv/IAGFP-Noc01_R3D.dv");
        
        wrapper.setId("/Users/TheBrain/test_images/lsm/C1.lsm");
        System.err.println("DONE BITCHES!");
    }
    
    public static void main(String[] args) throws Exception
    {
        new TestEngine();
    }

}
