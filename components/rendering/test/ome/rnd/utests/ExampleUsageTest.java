package ome.rnd.utests;

import ome.model.core.Pixels;
import ome.model.display.RenderingDef;

import omeis.providers.re.RenderingEngineImpl;
import omeis.providers.re.data.PlaneDef;

import junit.framework.TestCase;


public class ExampleUsageTest extends TestCase
{

    public void testSpringConstruction() throws Exception
    {
                
        RenderingEngineImpl re = new RenderingEngineImpl();
        re.acquireDependencies();
        re.loadFromIds(1l,1l);
        { // now it's ready
            PlaneDef pd = new PlaneDef(PlaneDef.XY,1);
            re.render(pd);
        }
        // re.release(); not yet needed
        
    }
    
    public void testCallbackConstruction() throws Exception
    {
        RenderingEngineImpl re = new RenderingEngineImpl();
        re.setCallback(new Object());// not yet implemented 
        re.loadFromIds(1l,1l);
        { // now it's ready
            PlaneDef pd = new PlaneDef(PlaneDef.XY,1);
            re.render(pd);
        }
        // re.release(); not yet needed
        
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
        RenderingEngineImpl re = new RenderingEngineImpl();
        re.setCallback(new Object());
        re.setPixels(p);
        re.setRenderDefintion(def);
        re.load();
        { // now it's ready
            PlaneDef pd = new PlaneDef(PlaneDef.XY,1);
            re.render(pd);
        }
        // re.release(); not yet needed
        
    }
    
}
