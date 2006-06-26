package ome.client.itests;

import org.testng.annotations.*;
import java.util.Date;

import junit.framework.TestCase;

import ome.api.IUpdate;
import ome.model.acquisition.AcquisitionContext;
import ome.model.containers.Project;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import ome.model.display.RenderingDef;
import ome.model.enums.AcquisitionMode;
import ome.model.enums.DimensionOrder;
import ome.model.enums.PhotometricInterpretation;
import ome.model.enums.PixelsType;
import ome.model.meta.Experimenter;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;
import omeis.providers.re.RenderingEngine;

@Test( 
	groups = {"client","integration", "renderingengine"} 
)
public class RenderingEngineTest extends TestCase
{

    /* 
    Pixels p = new Pixels();
    AcquisitionContext ac = new AcquisitionContext();
    PhotometricInterpretation pi = new PhotometricInterpretation();
    AcquisitionMode mode = new AcquisitionMode();
    PixelsType pt = new PixelsType();
    DimensionOrder dO = new DimensionOrder();
    PixelsDimensions pd = new PixelsDimensions();
    Image i = new Image();
    Channel c = new Channel();
    */

    ServiceFactory sf = new ServiceFactory();
    RenderingEngine re = sf.createRenderingEngine();
    RenderingEngine re2 = sf.createRenderingEngine();
    IUpdate iUpdate = sf.getUpdateService();

    @Test
    public void test_simpleUsage() throws Exception
    {
        try {
            sf.getQueryService().get(Experimenter.class,0L);
        } catch (Exception e) {
            //e.printStackTrace();
            //ok. http://bugs.openmicroscopy.org.uk/show_bug.cgi?id=649
        }

        Pixels pix = ObjectFactory.createPixelGraph(null);
        pix = (Pixels) iUpdate.saveAndReturnObject( pix );
        
        re.lookupPixels( pix.getId() );
        re.load();
        
    }
    
    @Test
    public void test_simpleUsageWithRenderingDef() throws Exception
    {

        Pixels pix = ObjectFactory.createPixelGraph(null);
        RenderingDef def = ObjectFactory.createRenderingDef();
        pix.addRenderingDef( def );
        
        pix = (Pixels) iUpdate.saveAndReturnObject( pix );
        
        re.lookupPixels( pix.getId() );
        re.lookupRenderingDef( pix.getId() );
        re.load();
        
    }

    @Test
    public void test_differentReferences() throws Exception
    {
        assertTrue( re != re2 );
    }

    
}
