package ome.server.itests;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import ome.api.IAnalysis;
import ome.api.IPixels;
import ome.api.IPojos;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.system.Principal;


public class AbstractManagedContextTest
        extends AbstractDependencyInjectionSpringContextTests
{
    protected LocalQuery iQuery;

    protected LocalUpdate iUpdate;
    
    protected IAnalysis iAnalysis;
    
    protected IPojos iPojos;
    
    protected IPixels iPixels;
    
    protected EventContext eContext;
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    protected void onSetUp() throws Exception {
        iQuery = (LocalQuery) applicationContext.getBean("queryService");
        iUpdate = (LocalUpdate) applicationContext.getBean("updateService");
        iAnalysis = (IAnalysis) applicationContext.getBean("analysisService");
        iPojos = (IPojos) applicationContext.getBean("pojosService");
        iPixels = (IPixels) applicationContext.getBean("pixelsService");
        eContext = (EventContext) applicationContext.getBean("eventContext");
        
        login("root","system","Test");
    }
    
    protected String[] getConfigLocations() { return new String[]{}; }
    protected ConfigurableApplicationContext getContext(Object key)
    {
        return OmeroContext.getManagedServerContext();
    }
    
    protected void login(String userName, String groupName, String eventType)
    {
        eContext.setPrincipal( 
                new Principal( userName, groupName, eventType ));
    }

}
