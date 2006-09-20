package ome.server.itests;

import java.util.UUID;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import ome.api.IAdmin;
import ome.api.IAnalysis;
import ome.api.IConfig;
import ome.api.IPixels;
import ome.api.IPojos;
import ome.api.local.LocalAdmin;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.model.meta.Experimenter;
import ome.security.SecuritySystem;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.Roles;
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
    
    protected IConfig iConfig;
    
    protected IAnalysis iAnalysis;
    
    protected IPojos iPojos;
    
    protected IPixels iPixels;
    
    protected OMEData data;
    
    protected JdbcTemplate jdbcTemplate;
    
    protected HibernateTemplate hibernateTemplate;
    
    protected SecuritySystem securitySystem;
    
    protected Roles roles;
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    protected void onSetUp() throws Exception {
    	factory = new ServiceFactory( (OmeroContext) applicationContext );
        iQuery = (LocalQuery) factory.getQueryService();
        iUpdate = (LocalUpdate) factory.getUpdateService();
        iAdmin = (LocalAdmin) factory.getAdminService();
        iAnalysis = factory.getAnalysisService();
        iConfig = factory.getConfigService();
        iPojos = factory.getPojosService();
        iPixels = factory.getPixelsService();

        DataSource dataSource = (DataSource) applicationContext.getBean("dataSource");
        jdbcTemplate = (JdbcTemplate) applicationContext.getBean("jdbcTemplate");
        
        data = new OMEData();
        data.setDataSource( dataSource );
        
        hibernateTemplate = (HibernateTemplate) applicationContext.getBean("hibernateTemplate");
        
        securitySystem = (SecuritySystem) applicationContext.getBean("securitySystem");
        roles = securitySystem.getSecurityRoles();
        loginRoot();
        
    }
    
    protected void loginRoot()
    {
    	
        login(roles.getRootName(),roles.getSystemGroupName(),"Test");
    }

    protected void loginNewUser()
    {
    	loginRoot();
    	String uuid = uuid();
    	Experimenter e = new Experimenter();
    	e.setFirstName("New");
    	e.setLastName("User");
    	e.setOmeName(uuid);
    	iAdmin.createUser(e);
    	loginUser(uuid);
    }
    
    protected void loginUser( String omeName )
    {
        login(omeName,roles.getUserGroupName(),"Test");
    }

    
    protected String[] getConfigLocations() { return new String[]{}; }
    protected ConfigurableApplicationContext getContext(Object key)
    {
        return OmeroContext.getManagedServerContext();
    }
    
    protected void login(String userName, String groupName, String eventType)
    {
        securitySystem.login( 
                new Principal( userName, groupName, eventType ));
    }
    
    protected String uuid()
    {
    	return UUID.randomUUID().toString();
    }

}
