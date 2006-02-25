package ome.server.itests;

import java.util.Arrays;

import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.BeanFactoryUtils;

import junit.framework.TestCase;

import ome.api.IPixels;
import ome.api.IQuery;
import ome.system.OmeroContext;
import omeis.providers.re.RenderingEngineImpl;

public class ContextTest extends TestCase
{

    protected RE re;
    
    static class RE  extends RenderingEngineImpl {
        public boolean pdCalled = false, pmCalled = false;
        public void setPixelsData(ome.io.nio.PixelsService arg0) {
            pdCalled = true;
        };
        public void setPixelsMetadata(IPixels arg0)
        {
            pmCalled = true;
        }
    }
    
    @Override
    protected void setUp() throws Exception
    {
        re = new RE();
    }

    public void testListBeans() throws Exception
    {
        
        System.out.println(Arrays.asList(
            BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
                    OmeroContext.getManagedServerContext().getBeanFactory(), 
                    Object.class, true, true)));
    }
    
    public void testManagedContext() throws Exception
    {
        OmeroContext ctx = OmeroContext.getManagedServerContext();
        onContext(ctx);
    }
    
    public void testInternalContext() throws Exception
    {
        OmeroContext ctx = OmeroContext.getInternalServerContext();
        onContext(ctx);
    }
    
    protected void onContext(OmeroContext ctx) {
        IQuery q = (IQuery) ctx.getBean("queryService");
        Advised aop = (Advised) q;
        System.out.println(Arrays.asList(aop.getAdvisors()));
    }
    
    public void testConfigureBean() throws Exception
    {
        
        OmeroContext ctx = OmeroContext.getInternalServerContext();
        ctx.applyBeanPropertyValues(re,"omeis.providers.re.RenderingEngine");
        assertTrue(re.pdCalled);
        assertTrue(re.pmCalled);
    }
    
    public void testSelfConfigureBean() throws Exception
    {
        re.selfConfigure();
        assertTrue(re.pdCalled);
        assertTrue(re.pmCalled);
    }
    
    public void testReferentialIntegrity() throws Exception
    {
        OmeroContext mCtx = OmeroContext.getManagedServerContext();
        OmeroContext iCtx = OmeroContext.getInternalServerContext();
        assertTrue(mCtx.getParent() == iCtx);
    }
}
