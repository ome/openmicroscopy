package ome.server.itests;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import ome.api.IAdmin;
import ome.api.IAnalysis;
import ome.api.IPixels;
import ome.api.IPojos;
import ome.api.local.LocalAdmin;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.testing.OMEData;

@Test(
        groups = {"integration"}
)
        
public class AbstractManagedContextTest
        extends AbstractDependencyInjectionSpringContextTests
{
    
    // ~ Testng Adapter
    // =========================================================================
    @Configuration(beforeTestMethod = true)
    public void adaptSetUp() throws Exception{setUp();}
    @Configuration(afterTestMethod = true)
    public void adaptTearDown() throws Exception{tearDown();}
    // =========================================================================
    
    protected ServiceFactory factory;
    
    protected LocalQuery iQuery;

    protected LocalUpdate iUpdate;
    
    protected LocalAdmin iAdmin;
    
    protected IAnalysis iAnalysis;
    
    protected IPojos iPojos;
    
    protected IPixels iPixels;
    
    protected EventContext eContext;
    
    protected OMEData data;
    
    protected JdbcTemplate jdbcTemplate;
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    protected void onSetUp() throws Exception {
    	factory = new ServiceFactory( (OmeroContext) applicationContext );
        iQuery = (LocalQuery) factory.getQueryService();
        iUpdate = (LocalUpdate) factory.getUpdateService();
        iAdmin = (LocalAdmin) factory.getAdminService();
        iAnalysis = factory.getAnalysisService();
        iPojos = factory.getPojosService();
        iPixels = factory.getPixelsService();
        eContext = (EventContext) applicationContext.getBean("eventContext");
        loginRoot();
        
        data = new OMEData();
        data.setDataSource((DataSource) applicationContext.getBean("dataSource"));
    
        DataSource dataSource = (DataSource) applicationContext.getBean("dataSource");
        jdbcTemplate = (JdbcTemplate) applicationContext.getBean("jdbcTemplate");
    
    }
    
    protected void loginRoot()
    {
        login("root","system","Test");
    }

    protected void loginUser( String omeName )
    {
        login(omeName,"user","Test");
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
