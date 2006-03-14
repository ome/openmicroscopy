package ome.server.itests;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import ome.api.IAnalysis;
import ome.api.IPixels;
import ome.api.IPojos;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.system.OmeroContext;


public class AbstractManagedContextTest
        extends AbstractDependencyInjectionSpringContextTests
{
    protected LocalQuery iQuery;

    protected LocalUpdate iUpdate;
    
    protected IAnalysis iAnalysis;
    
    protected IPojos iPojos;
    
    protected IPixels iPixels;
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    protected void onSetUp() throws Exception {
        iQuery = (LocalQuery) applicationContext.getBean("queryService");
        iUpdate = (LocalUpdate) applicationContext.getBean("updateService");
        iAnalysis = (IAnalysis) applicationContext.getBean("analysisService");
        iPojos = (IPojos) applicationContext.getBean("pojosService");
        iPixels = (IPixels) applicationContext.getBean("pixelsService");
        login("root","system","Test");
    }
    
    protected String[] getConfigLocations() { return new String[]{}; }
    protected ConfigurableApplicationContext getContext(Object key)
    {
        return OmeroContext.getManagedServerContext();
    }
    
    /* FIXME */
    protected void login(String userName, String groupName, String eventType)
    {
        System.getProperties().setProperty("omero.username",userName);
        System.getProperties().setProperty("omero.groupname",groupName);
        System.getProperties().setProperty("omero.eventtype",eventType);
    }

}
