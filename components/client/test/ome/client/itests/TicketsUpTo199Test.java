package ome.client.itests;

import org.testng.annotations.*;
import java.util.Date;

import junit.framework.TestCase;

import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.containers.Project;
import ome.model.core.Pixels;
import ome.model.core.PlaneInfo;
import ome.parameters.Parameters;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;

@Test( 
	groups = {"client","integration"} 
)
public class TicketsUpTo199Test extends TestCase
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
        pixels.addToPlaneInfo(planeInfo);
        pixels = (Pixels) iUpdate.saveAndReturnObject(pixels);
        PlaneInfo test = (PlaneInfo)
        iQuery.findByQuery( "select pi from PlaneInfo pi " +
                "where pi.pixels.id = :id",new Parameters().addId(pixels.getId()));
        assertNotNull( test );
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
