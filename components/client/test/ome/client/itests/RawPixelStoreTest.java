package ome.client.itests;

import org.testng.annotations.*;

import junit.framework.TestCase;

import ome.api.IUpdate;
import ome.api.RawPixelsStore;
import ome.model.core.Pixels;
import ome.model.meta.Experimenter;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;

@Test( 
	groups = {"client","integration", "binary"} 
)
public class RawPixelStoreTest extends TestCase
{

    ServiceFactory sf = new ServiceFactory();
    RawPixelsStore raw = sf.createRawPixelsStore();
    IUpdate iUpdate = sf.getUpdateService();
    
    @Test
    public void test_simpleDigest() throws Exception
    {
        try {
            sf.getQueryService().get(Experimenter.class,0L);
        } catch (Exception e) {
            //e.printStackTrace();
            //ok. http://bugs.openmicroscopy.org.uk/show_bug.cgi?id=649
        }
        
        Pixels pix = ObjectFactory.createPixelGraph(null);
        pix.setSizeX(16);
        pix.setSizeY(16);
        pix.setSizeZ(1);
        pix.setSizeT(1);
        pix.setSizeC(1);
        pix = (Pixels) iUpdate.saveAndReturnObject( pix );
        
        raw.setPixelsId( pix.getId() );
        raw.calculateMessageDigest();
        
    }
    
}
