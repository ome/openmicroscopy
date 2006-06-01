package ome.client.itests.rois;

import org.testng.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import ome.api.IUpdate;
import ome.model.core.Pixels;
import ome.model.enums.RegionType;
import ome.model.uroi.BoundingBox;
import ome.model.uroi.Region;
import ome.model.uroi.ULink;
import ome.model.uroi.URoi;
import ome.model.uroi.USlice;
import ome.model.uroi.USquare;
import ome.model.uroi.XYZCT;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;

@Test( 
	groups = {"client","integration","roi","proposal"} 
)
public class URoiTest extends TestCase
{

    ServiceFactory sf = new ServiceFactory();
    IUpdate up = sf.getUpdateService();

    public void test_bug() throws Exception
    {
        RegionType[] rt = new RegionType[3];
        RegionType r = new RegionType();
        r.setValue("test");
        rt[0] = r;
        rt[1] = r;
        rt[2] = r;
//        rt[2] = r;
//        rt[3] = r;
        up.saveArray( rt );

    }
    
  @Test( invocationCount = 5, enabled = false )
    public void test_createRoisOnNewPixels() throws Exception
    {

      // Edge types.
      Pixels pix = ObjectFactory.createPixelGraph(null);
      Random rnd = new Random();
      List<Region> l = new ArrayList<Region>();
      
      for (int i = 0; i < 20; i++)
      {
          int j = rnd.nextInt(4);
          switch (j)
        {
            case 0: l.add( simpleRegion( pix ));break;
            case 1: l.add( simpleRegion( pix ));break;
            case 2: l.add( simpleRegion( pix ));break;
            case 3: l.add( simpleRegion( pix ));break;
            default: throw new RuntimeException();

        }
      }
      
      up.saveArray( (Region[]) l.toArray(new Region[l.size()]));
      
    }
  
  protected Region simpleRegion(Pixels pix)
  {
      RegionType type = new RegionType( );
      type.setValue( "simple" );

      // Bottom to top (left)
      
      BoundingBox bb = new XYZCT();
      bb.setX1( new Integer(1) );
      bb.setX2( new Integer(1) );
      bb.setY1( new Integer(1) );
      bb.setY2( new Integer(1) );
      bb.setZ1( new Integer(1) );
      bb.setZ2( new Integer(1) );
      bb.setC1( new Integer(1) );
      bb.setC2( new Integer(1) );
      bb.setT1( new Integer(1) );
      bb.setT2( new Integer(1) );
      // This is a mistake.
      bb.setZ( new Integer(1) );
      bb.setC( new Integer(1) );
      bb.setT( new Integer(1) );
      
      // Bottom to top (right)
      
      USquare square = new USquare();
      square.setUpperLeftX( new Integer(1) );
      square.setLowerRightX( new Integer(1) );
      square.setUpperLeftY( new Integer(1) );
      square.setLowerRightY( new Integer(1) );

      USlice slice = new USlice();
      slice.setC( new Integer(1) );
      slice.setT( new Integer(1) );
      slice.setZ( new Integer(1) );
      slice.addToShapes( square );
      
      URoi roi = new URoi();
      roi.addToSlices( slice );

      // link and send
      
      ULink link = new ULink();
      link.link( bb, roi );

      Region region = new Region();
      region.setPixels( pix );
      region.setType( type );
      region.addToBoxes( bb );
      region.addToUrois( roi );
      
      return region;
      
    }
    
}
