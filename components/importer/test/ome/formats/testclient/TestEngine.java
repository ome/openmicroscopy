package ome.formats.testclient;

import ome.formats.OMEROMetadataStore;
import ome.formats.importer.OMEROWrapper;
import ome.formats.testclient.TestServiceFactory;
import ome.system.ServiceFactory;

public class TestEngine
{   
    private TestEngine() throws Exception
    {
        OMEROWrapper wrapper = new OMEROWrapper();
        ServiceFactory sf = new TestServiceFactory();
        OMEROMetadataStore store = new OMEROMetadataStore(sf);
        wrapper.setMetadataStore(store);
        wrapper.setId("/Users/TheBrain/Desktop/Images/FRAP-23.8.05/IAGFP-Noc02_R3D.dv");
        //wrapper.setId("/workspace/Test Images/Zeiss (.lsm)/C1.lsm");
        System.err.println("DONE BITCHES!");
    }
    
    public static void main(String[] args) throws Exception
    {
        new TestEngine();
    }

}
