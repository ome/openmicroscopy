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
    Random rnd = new Random();

    static int Xmax = 1024, Ymax = 1024, Zmax = 24, Tmax = 120, Cmax = 3;

    public void test_createRoisOnNewPixels() throws Exception
    {

      // Edge types.
      Pixels pix = ObjectFactory.createPixelGraph(null);
      pix.setSizeX( Xmax );
      pix.setSizeY( Ymax );
      pix.setSizeZ( Zmax );
      pix.setSizeT( Tmax );
      pix.setSizeC( Cmax );
      List<Region> l = new ArrayList<Region>();
      
      for (int i = 0; i < 50; i++)
      {
          int j = rnd.nextInt(3);
          switch (j)
        {
            case 0: l.add( singlePlane( pix ));break;
            case 1: l.add( continuousPlanes( pix ));break;
            case 2: l.add( randomPlanes( pix ));break;
            default: throw new RuntimeException();

        }
      }
      
      up.saveArray( (Region[]) l.toArray(new Region[l.size()]));
      
    }
  
    // ~ Helpers 
    // =========================================================================
    
    protected int pickSingle(int max)
    {
        return rnd.nextInt( max );
    }
    
    protected int[] pickRange( int max )
    {
        int[] retVal = new int[2];
        retVal[0] = rnd.nextInt( max-1 );
        retVal[1] = retVal[0] + 1 + rnd.nextInt( max-retVal[0] );
        return retVal;
    }
    
    protected Region singlePlane(Pixels pix)
    {
        RegionType type = new RegionType();
        type.setValue("single");

        int[] x = pickRange( pix.getSizeX() );
        int[] y = pickRange( pix.getSizeY() );
        int z = pickSingle( pix.getSizeZ() );
        int t = pickSingle( pix.getSizeT() );
        int c = pickSingle( pix.getSizeC() );
        
        // Bottom to top (left)

        BoundingBox bb = new XYZCT();
        bb.setX1( x[0] );
        bb.setX2( x[1] );
        bb.setY1( y[0] );
        bb.setY2( y[1] );
        bb.setZ1( z );
        bb.setZ2( z );
        bb.setT1( t );
        bb.setT2( t );
        bb.setC1( c );
        bb.setC2( c );
        // This is a mistake.
        bb.setZ(new Integer(-1));
        bb.setC(new Integer(-1));
        bb.setT(new Integer(-1));

        // Bottom to top (right)

        URoi roi = singleRoi(x, y, z, t, c);

        // link and send

        Region region = new Region();
        region.setPixels(pix);
        region.setType(type);
        region.addToBoxes(bb);
        region.addToUrois(roi);

        return region;

    }

    private URoi singleRoi(int[] x, int[] y, int z, int t, int c)
    {
        USquare square = new USquare();
        square.setUpperLeftX(x[0]);
        square.setLowerRightX(x[1]);
        square.setUpperLeftY(y[0]);
        square.setLowerRightY(y[1]);

        USlice slice = new USlice();
        slice.setC(c);
        slice.setT(t);
        slice.setZ(z);
        slice.addToShapes(square);

        URoi roi = new URoi();
        roi.addToSlices(slice);
        return roi;
    }

    protected Region continuousPlanes(Pixels pix)
    {
        RegionType type = new RegionType();
        type.setValue("continuous");

        int[] x = pickRange( pix.getSizeX() );
        int[] y = pickRange( pix.getSizeY() );
        int[] z = pickRange( pix.getSizeZ() );
        int[] t = pickRange( pix.getSizeT() );
        int[] c = pickRange( pix.getSizeC() );
        
        // Bottom to top (left)

        BoundingBox bb = new XYZCT();
        bb.setX1( x[0] );
        bb.setX2( x[1] );
        bb.setY1( y[0] );
        bb.setY2( y[1] );
        bb.setZ1( z[0] );
        bb.setZ2( z[1] );
        bb.setT1( t[0] );
        bb.setT2( t[1] );
        bb.setC1( c[0] );
        bb.setC2( c[1] );
        // This is a mistake.
        bb.setZ(new Integer(-1));
        bb.setC(new Integer(-1));
        bb.setT(new Integer(-1));

        // Bottom to top (right)

        USquare square = new USquare();
        square.setUpperLeftX(x[0]);
        square.setLowerRightX(x[1]);
        square.setUpperLeftY(y[0]);
        square.setLowerRightY(y[1]);

        URoi roi = new URoi();
        for (int C = 0; C < c.length; C++)
        {
            for (int T = 0; T < t.length; T++)
            {
                for (int Z = 0; Z < z.length; Z++)
                {
                    USlice slice = new USlice();
                    slice.setC(C);
                    slice.setT(T);
                    slice.setZ(Z);
                    slice.addToShapes(square);
                    roi.addToSlices(slice);
                }
            }
        }

        // link and send

        Region region = new Region();
        region.setPixels(pix);
        region.setType(type);
        region.addToBoxes(bb);
        region.addToUrois(roi);

        return region;

    }

    protected Region randomPlanes(Pixels pix)
    {
        RegionType type = new RegionType();
        type.setValue("random");

        // link and send

        Region region = new Region();
        region.setPixels(pix);
        region.setType(type);
       
        // Bottom to top (right)

        for (int n = 0; n < 10; n++)
        {
            int[] x = pickRange( pix.getSizeX() );
            int[] y = pickRange( pix.getSizeY() );
            int z = pickSingle( pix.getSizeZ() );
            int t = pickSingle( pix.getSizeT() );
            int c = pickSingle( pix.getSizeC() );
            URoi roi = singleRoi(x,y,z,t,c);
            region.addToUrois(roi);
        }

        int[] 
            x = new int[]{-1,-1},
            y = new int[]{-1,-1},
            z = new int[]{-1,-1},
            c = new int[]{-1,-1},
            t = new int[]{-1,-1};
        
        for (URoi roi : (List<URoi>) region.collectUrois(null))
        {
            for(USlice slice : (List<USlice>) roi.collectSlices(null))
            {
                for (USquare square : (List<USquare>) slice.collectShapes(null))
                {
                    if ( x[0] < 0 || square.getUpperLeftX() < x[0] )
                    {
                        x[0] = square.getUpperLeftX();
                    }
                    
                    if ( x[1] < 0 || square.getLowerRightX() > x[1] )
                    {
                        x[1] = square.getLowerRightX();
                    }
                    
                    if ( y[0] < 0 || square.getLowerRightY() < y[0] )
                    {
                        y[0] = square.getLowerRightY();
                    }
                    
                    if ( y[1] < 0 || square.getUpperLeftY() > y[1] )
                    {
                        y[1] = square.getUpperLeftY();
                    }

                    if ( z[0] < 0 || slice.getZ() < z[0] )
                    {
                        z[0] = slice.getZ();
                    }

                    if ( z[1] < 0 || slice.getZ() > z[1] )
                    {
                        z[1] = slice.getZ();
                    }
                    
                    if ( c[0] < 0 || slice.getC() < c[0] )
                    {
                        c[0] = slice.getC();
                    }

                    if ( c[1] < 0 || slice.getC() > c[1] )
                    {
                        c[1] = slice.getC();
                    }

                    if ( t[0] < 0 || slice.getT() < t[0] )
                    {
                        t[0] = slice.getT();
                    }

                    if ( t[1] < 0 || slice.getT() > t[1] )
                    {
                        t[1] = slice.getT();
                    }
                    
                }
            }
        }
        
        // Bottom to top (left)

        BoundingBox bb = new XYZCT();
        bb.setX1( x[0] );
        bb.setX2( x[1] );
        bb.setY1( y[0] );
        bb.setY2( y[1] );
        bb.setZ1( z[0] );
        bb.setZ2( z[1] );
        bb.setT1( t[0] );
        bb.setT2( t[1] );
        bb.setC1( c[0] );
        bb.setC2( c[1] );
        // This is a mistake.
        bb.setZ(new Integer(-1));
        bb.setC(new Integer(-1));
        bb.setT(new Integer(-1));
        region.addToBoxes(bb);

        return region;

    }

    
}
