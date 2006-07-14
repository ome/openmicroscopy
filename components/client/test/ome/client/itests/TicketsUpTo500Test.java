package ome.client.itests;

import org.testng.annotations.*;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import junit.framework.TestCase;

import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.core.PlaneInfo;
import ome.parameters.Parameters;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;
import pojos.ImageData;

@Test( 
	groups = {"client","integration"} 
)
public class TicketsUpTo500Test extends TestCase
{

    ServiceFactory sf = new ServiceFactory();
    IUpdate iUpdate = sf.getUpdateService();
    IQuery iQuery = sf.getQueryService();

    // ~ Ticket 168
    // =========================================================================
    
    @Test( groups = "ticket:168")
    public void test_planeInfoSetPixelsSavePixels() throws Exception
    {
        Pixels pixels = ObjectFactory.createPixelGraph(null);
        PlaneInfo planeInfo = createPlaneInfo();
        planeInfo.setPixels(pixels);
        pixels = (Pixels) iUpdate.saveAndReturnObject(pixels);
        PlaneInfo test = (PlaneInfo)
        iQuery.findByQuery( "select pi from PlaneInfo pi " +
                "where pi.pixels.id = :id",new Parameters().addId(pixels.getId()));
        // Null because saving the pixels rather than planeinfo does not work.
        assertNull( test );
    }
    
    @Test( groups = "ticket:168")
    public void test_planeInfoSetPixelsSavePlaneInfo() throws Exception
    {
        Pixels pixels = ObjectFactory.createPixelGraph(null);
        PlaneInfo planeInfo = createPlaneInfo();
        planeInfo.setPixels(pixels);
        planeInfo = (PlaneInfo) iUpdate.saveAndReturnObject(planeInfo);
        Pixels test = (Pixels)
        iQuery.findByQuery( "select p from Pixels p " +
                "where p.planeInfo.id = :id",new Parameters().addId(planeInfo.getId()));
        assertNotNull( test );
    }

    @Test( groups = "ticket:168")
    public void test_pixelsAddToPlaneInfoSavePixels() throws Exception
    {
        IUpdate iUpdate = sf.getUpdateService();
        Pixels pixels = ObjectFactory.createPixelGraph(null);
        PlaneInfo planeInfo = createPlaneInfo();
        pixels.addPlaneInfo(planeInfo);
        pixels = (Pixels) iUpdate.saveAndReturnObject(pixels);
        PlaneInfo test = (PlaneInfo)
        iQuery.findByQuery( "select pi from PlaneInfo pi " +
                "where pi.pixels.id = :id",new Parameters().addId(pixels.getId()));
        assertNotNull( test );
    }
    
    @Test( groups = {"ticket:221"} )
    public void testGetImagesReturnsNoNulls() throws Exception {
  	  	Dataset d = new Dataset();
  	  	d.setName("ticket:221");
  	  	Image i = new Image();
  	  	i.setName("ticket:221");
  	  	Pixels p = ObjectFactory.createPixelGraph(null);
  	  	p.setDefaultPixels( Boolean.TRUE );
  	  	i.addPixels(p);
  	  	d.linkImage(i);
  	  	d = iUpdate.saveAndReturnObject(d);
  	  	
  	  	Set<Image> set = 
  	  	iPojos.getImages(Dataset.class, Collections.singleton(d.getId()), null);
  	  	Image img = set.iterator().next();
  	  	ImageData test = new ImageData( img );
  	  	assertNotNull(test);
  		assertNotNull(test.getDefaultPixels());
  		assertNotNull(test.getDefaultPixels().getPixelSizeX());
  		assertNotNull(test.getDefaultPixels().getPixelSizeY());
  		assertNotNull(test.getDefaultPixels().getPixelSizeZ());
  		assertNotNull(test.getDefaultPixels().getPixelType());
  		assertNotNull(test.getDefaultPixels().getImage());
  		assertNotNull(test.getDefaultPixels().getOwner());
  		assertNotNull(test.getDefaultPixels().getSizeC());
  		assertNotNull(test.getDefaultPixels().getSizeT());
  		assertNotNull(test.getDefaultPixels().getSizeZ());
  		assertNotNull(test.getDefaultPixels().getSizeY());
  		assertNotNull(test.getDefaultPixels().getSizeX());
  	}

    // ~ Helpers
    // =========================================================================
    // TODO refactor to ObjectFactory
    private PlaneInfo createPlaneInfo()
    {
        PlaneInfo planeInfo = new PlaneInfo();
        planeInfo.setTheZ( 1 );
        planeInfo.setTheC( 1 );
        planeInfo.setTheT( 1 );
        planeInfo.setTimestamp( 0F );
        return planeInfo;
    }
        
    
    
  
    
}
