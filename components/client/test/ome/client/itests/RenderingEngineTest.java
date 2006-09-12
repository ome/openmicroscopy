package ome.client.itests;

import org.testng.annotations.*;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import ome.api.IPixels;
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
import ome.model.enums.Family;
import ome.model.enums.PhotometricInterpretation;
import ome.model.enums.PixelsType;
import ome.model.enums.RenderingModel;
import ome.model.meta.Experimenter;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;
import omeis.providers.re.RenderingEngine;

@Test( 
	groups = {"client", "integration", "renderingengine", "broken"} 
	// Needs an ImporterFixture
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

    private final static Long MAGE_PIXELS_54 = 54L;
    
    @Test( groups = {"magedb","ticket:194"} )
    public void testUnloadedDetails() throws Exception
    {
        re.lookupPixels(MAGE_PIXELS_54);
        re.lookupRenderingDef(MAGE_PIXELS_54);
        re.load();
        
        RenderingModel m = re.getModel();
        List families = re.getAvailableFamilies();
        Pixels pix = re.getPixels();
        test( m, families, pix );
    }
    
    @Test( groups = {"magedb","ticket:194"} )
    public void testUnloadedDetailsWithGetBean() throws Exception
    {
        RenderingEngine gotBean = sf.createRenderingEngine();
        
        gotBean.lookupPixels(MAGE_PIXELS_54);
        gotBean.lookupRenderingDef(MAGE_PIXELS_54);
        gotBean.load();
        
        RenderingModel m = gotBean.getModel();
        List families = gotBean.getAvailableFamilies();
        Pixels pix = gotBean.getPixels();
        test( m, families, pix );
    }
    
    @Test( groups = {"manualimport","ticket:258"})
    public void testQuantumDefTransientObjectException() throws Exception {
		RenderingEngine re = sf.createRenderingEngine();
		re.lookupPixels(1L);
		re.lookupRenderingDef(1L);
		re.load();
		
		re.getModel();
		re.setQuantumStrategy(1);
	}
    
    // copied from server test
    @Test( groups = "ticket:330" )
    public void testPixelsIsFilled() throws Exception {
    	Pixels p = ObjectFactory.createPixelGraph(null);
    	p = sf.getUpdateService().saveAndReturnObject( p );
    	
    	IPixels pix = sf.getPixelsService();
    	Pixels t = pix.retrievePixDescription(p.getId());
    	testPixelsFilled(t);
    	
    	RenderingEngine re = sf.createRenderingEngine();
    	re.lookupPixels(p.getId());
    	t = re.getPixels();
    	testPixelsFilled(t);
    }

	private void testPixelsFilled(Pixels t) {
		//assertTrue( t.sizeOfPlaneInfo() >= 0 );
    	
    	PixelsDimensions pd = t.getPixelsDimensions();
    	assertNotNull( pd );
    	assertNotNull( pd.getSizeX() );
    	
    	List c = t.getChannels();
    	assertNotNull( c );
    	assertTrue( c.size() > 0 );
    	
    	for (Object object : c) {
			Channel ch = (Channel) object;
			assertNotNull( ch.getLogicalChannel() );
		}
	}
    
    // ~ Helpers
	// =========================================================================
    
    private void test( RenderingModel m, List<Family> families, Pixels pix)
    {
        assertNotNull(m);
        assertNotNull(pix);
        assertNotNull(families);
        assertTrue(families.size()>0);
        
    }
}
