package ome.ejb.itests;

import ome.api.IPixels;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;
import ome.model.display.RenderingDef;
import ome.ro.ejb.QueryBean;
import ome.ro.ejb.RenderingBean;
import ome.ro.ejb.UpdateBean;

import omeis.providers.re.data.PlaneDef;

import junit.framework.TestCase;


public class ExampleUsageTest extends TestCase
{

    public void testSpringConstruction() throws Exception
    {
                
        RenderingBean re = new RenderingBean();
        re.lookupPixels(1);
        re.lookupRenderingDef(1);
        re.load();
        { // now it's ready
            PlaneDef pd = new PlaneDef(PlaneDef.XY,1);
            re.render(pd);
        }
        re.destroy();
        
    }
    
    public void testConstructorConstruction() throws Exception
    {
        IPixels metaSrv = null;
        PixelsService pixelsSrv = null;
        
        RenderingBean re = new RenderingBean(metaSrv,pixelsSrv);
        re.lookupPixels(1);
        re.lookupRenderingDef(1);
        re.load();
        { // now it's ready
            PlaneDef pd = new PlaneDef(PlaneDef.XY,1);
            re.render(pd);
        }
        re.destroy();
        
    }
    
    public void testDirectConstruction() throws Exception
    {
        Pixels p = new Pixels(1l);
        p.setSizeX(new Integer(64));
        p.setSizeY(new Integer(64));
        p.setSizeZ(new Integer(64));
        p.setSizeC(new Integer(64));
        p.setSizeT(new Integer(64));
        
        RenderingDef def = new RenderingDef(1l);
        def.setDefaultT(new Integer(1));
        // etc
        RenderingBean re = new RenderingBean();
        re.usePixels(p);
        re.useRenderDefintion(def);
        re.load();
        { // now it's ready
            PlaneDef pd = new PlaneDef(PlaneDef.XY,1);
            re.render(pd);
        }
        // re.release(); not yet needed
        
    }
    
}
