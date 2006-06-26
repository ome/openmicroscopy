package ome.client.itests.rois;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.testng.annotations.*;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.sql.DataSource;

import junit.framework.TestCase;

import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.core.Pixels;
import ome.model.enums.OverlayType;
import ome.model.enums.RegionType;
import ome.model.meta.Experimenter;
import ome.model.uroi.BoundingBox;
import ome.model.uroi.Overlay;
import ome.model.uroi.Region;
import ome.model.uroi.URoi;
import ome.model.uroi.UShape;
import ome.model.uroi.USlice;
import ome.model.uroi.USquare;
import ome.model.uroi.XY;
import ome.model.uroi.XYZ;
import ome.model.uroi.XYZCT;
import ome.model.uroi.XYZT;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;

@Test(
        // "ignored" because it should only be run manually
        groups = {"ignore","client","integration","roi","proposal"} 
)
public class URoiTest extends TestCase
{

    private static Log TESTLOG = LogFactory.getLog("TEST-"+URoiTest.class.getName());
    
    static int Xmax = 1024, Ymax = 1024, Zmax = 24, Tmax = 120, Cmax = 3;

    ServiceFactory sf;
    IQuery iQuery;
    IUpdate up;
    Random rnd;
    
    DataSource ds;
    SimpleJdbcTemplate jdbc;
    
    @Configuration( beforeTestClass = true )
    public void config()
    {
        TESTLOG.info("INIT");
        sf = new ServiceFactory("ome.client.test");
        iQuery = sf.getQueryService();
        up = sf.getUpdateService();
        rnd = new Random();

        ds = (DataSource) sf.getContext().getBean("dataSource");
        jdbc = new SimpleJdbcTemplate(ds);
        
        TESTLOG.info("PSQL/bug649");        
        try {
            iQuery.get(Experimenter.class,0L);
        } catch (Exception e ) {
            // ok. http://bugs.openmicroscopy.org.uk/show_bug.cgi?id=649
        }
        
    }
    
    @Test
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
      
      for (int i = 0; i < 5; i++)
      {
          int j = rnd.nextInt(4);
          TESTLOG.info("Creating region with:"+j);
          switch (j)
          {
            case 0: l.add( singlePlane( pix ));break;
            case 1: l.add( continuousPlanes( pix ));break;
            case 2: l.add( randomPlanes( pix ));break;
            case 3: l.add( cellLineage( pix ));break;
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

        XYZCT bb = new XYZCT();
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

        // Bottom to top (right)

        URoi roi = singleRoi(x, y, z, t, c);

        // link and send

        Region region = new Region();
        region.setPixels(pix);
        region.setType(type);
        region.addBoundingBox(bb);
        region.addSpecification(roi);

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
        slice.addUShape(square);

        URoi roi = new URoi();
        roi.addUSlice(slice);
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

        XYZCT bb = new XYZCT();
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
                    slice.addUShape(square);
                    roi.addUSlice(slice);
                }
            }
        }

        // link and send

        Region region = new Region();
        region.setPixels(pix);
        region.setType(type);
        region.addBoundingBox(bb);
        region.addSpecification(roi);

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
            region.addSpecification(roi);
        }

        int[] 
            x = new int[]{-1,-1},
            y = new int[]{-1,-1},
            z = new int[]{-1,-1},
            c = new int[]{-1,-1},
            t = new int[]{-1,-1};
        
        for (URoi roi : (List<URoi>) region.collectSpecs(null))
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

        XYZCT bb = new XYZCT();
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
        region.addBoundingBox(bb);

        return region;

    }

    /**
     * data for this method must be manually added via:
     * --------------------------------------------------------
       create table CellLineage2 
       (type varchar, slice int, time int, 
           x1 real, y1 real, x2 real, y2 real, 
           text varchar, color char(6), filled boolean, grouped varchar, 
           Notes varchar);
       copy CellLineage2 from '/tmp/cell2' with null as 'N/A';
     * --------------------------------------------------------       
     */
    protected Region cellLineage(final Pixels pix)
    {
        RegionType regionType = new RegionType();
        regionType.setValue("cellLineage");

        Region region = new Region();
        region.setPixels(pix);
        region.setType(regionType);

        XYZT topBox = new XYZT();
        region.addBoundingBox( topBox );

        ParameterizedRowMapper<Overlay> prm = 
        new ParameterizedRowMapper<Overlay>(){
            public Overlay mapRow(java.sql.ResultSet rs, int row)
            throws java.sql.SQLException {
                String type = rs.getString("type");
                int slice   = rs.getInt("slice");
                int time    = rs.getInt("time");
                float x1    = rs.getFloat("x1");
                float y1    = rs.getFloat("y1");
                float x2    = rs.getFloat("x2");
                float y2    = rs.getFloat("y2");
                String text = rs.getString("text");
                String color= rs.getString("color");
                boolean fill= rs.getBoolean("filled");
                String group= rs.getString("grouped");
                String notes= rs.getString("notes");

                XYZ volume = new XYZ();
                volume.setT( time );
                
                XY plane = new XY();
                plane.setX1( (int) x1 );
                plane.setY1( (int) y1 );
                plane.setX2( (int) x2 );
                plane.setY2( (int) y2 );
                plane.setZ( slice );
                plane.linkXYZ( volume );
                
                OverlayType overlayType = new OverlayType();
                overlayType.setValue(type);
                
                Overlay overlay = new Overlay();
                overlay.setColor(color);
                overlay.setText(text);
                overlay.setType(overlayType);
                overlay.setPlane(plane);

                return overlay;
            };   
        };
        
        List<Overlay> list = jdbc.query(
                "select * from CellLineage2 where time = 1",
                prm);
        Map<Integer,XYZ> timePoints = new HashMap<Integer,XYZ>();

        for (Overlay overlay : list)
        {
            XY plane = overlay.getPlane();
            XYZ vol = (XYZ) plane.linkedXYZList().get(0);
            Integer time = vol.getT();
            assertNotNull( time );
            
            if ( ! timePoints.containsKey( time ))
            {
                timePoints.put( time, vol );
            } else {
                XYZ extant = timePoints.get( vol.getT() );
                extant.linkXY( (XY) vol.linkedXYList().get(0) );
                vol.clearXYLinks();
            }
        }
        //assertions!
        
        for (XYZ volume : timePoints.values())
        {
            topBox.linkXYZ( volume );
        }
        
        return region;

    }
    
}
