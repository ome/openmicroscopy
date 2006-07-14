package ome.server.itests;

import org.testng.annotations.*;

import java.util.Arrays;

import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.BeanFactoryUtils;

import junit.framework.TestCase;

import ome.api.IPixels;
import ome.api.IQuery;
import ome.system.OmeroContext;
import ome.system.ServiceFactory;
import ome.tools.spring.InternalServiceFactory;
import omeis.providers.re.RenderingEngine;
import omeis.providers.re.RenderingEngineImpl;

@Test(
        groups = "integration"
)
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
  @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception
    {
        re = new RE();
    }

  @Test
    public void testListBeans() throws Exception
    {
        
        assertTrue(0 < Arrays.asList(
            BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
                    OmeroContext.getManagedServerContext().getBeanFactory(), 
                    Object.class, true, true)).size());
    }
    
  @Test
    public void testManagedContext() throws Exception
    {
        OmeroContext ctx = OmeroContext.getManagedServerContext();
        onContext(ctx);
    }
    
  @Test
    public void testInternalContext() throws Exception
    {
        OmeroContext ctx = OmeroContext.getInternalServerContext();
        onContext(ctx);
    }
  
   protected void onContext(OmeroContext ctx)
   {
        ServiceFactory sf = new ServiceFactory( ctx );
        IQuery q = sf.getQueryService();
        assertTrue( Advised.class.isAssignableFrom( q.getClass() ));
    }
    
  @Test
    public void testConfigureBean() throws Exception
    {
        
        OmeroContext ctx = OmeroContext.getInternalServerContext();
        ctx.applyBeanPropertyValues(re,RenderingEngine.class);
        assertTrue(re.pdCalled);
        assertTrue(re.pmCalled);
    }
    
  @Test
    public void testSelfConfigureBean() throws Exception
    {
        re.selfConfigure();
        assertTrue(re.pdCalled);
        assertTrue(re.pmCalled);
    }
    
  @Test
    public void testReferentialIntegrity() throws Exception
    {
        OmeroContext mCtx = OmeroContext.getManagedServerContext();
        OmeroContext iCtx = OmeroContext.getInternalServerContext();
        assertTrue(mCtx.getParent() == iCtx);
    }
}
