package ome.ejb.itests;

import ome.ro.ejb.RenderingBean;

import omeis.providers.re.data.PlaneDef;

import junit.framework.TestCase;


public class ExampleUsageTest extends TestCase
{

    RenderingBean re;
    
    @Override
    protected void setUp() throws Exception
    {
        re = new RenderingBean();
        re.selfConfigure();
    }
        
    
    public void testLookupConstruction() throws Exception
    {
        re.lookupPixels(1);
        re.lookupRenderingDef(1);
        re.load();
        { // now it's ready
            PlaneDef pd = new PlaneDef(PlaneDef.XY,1);
            re.render(pd);
        }
        re.destroy();
        
    }
    
}
