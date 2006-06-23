package ome.client.itests;

import org.testng.annotations.*;
import java.util.Date;

import junit.framework.TestCase;

import ome.api.IUpdate;
import ome.model.containers.Project;
import ome.model.core.Pixels;
import ome.model.display.RenderingDef;
import ome.model.meta.Experimenter;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;
import omeis.providers.re.RenderingEngine;

@Test( 
	groups = {"client","integration", "renderingengine"} 
)
public class RenderingEngineTest extends TestCase
{

    ServiceFactory sf = new ServiceFactory();
    RenderingEngine re = sf.createRenderingEngine();
    IUpdate iUpdate = sf.getUpdateService();
    
    @Test
    public void test_connectionClosed() throws Exception
    {
        try {
            sf.getQueryService().get(Experimenter.class,0L);
        } catch (Exception e) {
            //e.printStackTrace();
            //ok. http://bugs.openmicroscopy.org.uk/show_bug.cgi?id=649
        }
        
        Pixels pix = ObjectFactory.createPixelGraph(null);
        RenderingDef def = ObjectFactory.createRenderingDef();
        pix.addToSettings( def );
        
        pix = (Pixels) iUpdate.saveAndReturnObject( pix );
        
        re.lookupPixels( pix.getId() );
        re.lookupRenderingDef( pix.getId() );
        re.load();
        
    }
    
}
