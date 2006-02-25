package ome.server.itests;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import ome.api.local.LocalQuery;
import ome.system.OmeroContext;


public class AbstractManagedContextTest
        extends AbstractDependencyInjectionSpringContextTests
{
    protected LocalQuery iQuery;

    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    protected void onSetUp() throws Exception {
        iQuery = (LocalQuery) applicationContext.getBean("queryService");
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
