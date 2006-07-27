package ome.client.itests.sec;

import javax.sql.DataSource;

import ome.api.IAdmin;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.meta.Experimenter;
import ome.system.Login;
import ome.system.ServiceFactory;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import junit.framework.TestCase;

@Test( groups = {"client","integration","security"} )
public class AbstractSecurityTest extends TestCase {
	
    protected ServiceFactory tmp = new ServiceFactory( "ome.client.test" );
    protected DataSource dataSource = (DataSource) tmp.getContext().getBean("dataSource");
    protected SimpleJdbcTemplate jdbc = new SimpleJdbcTemplate( dataSource );
    protected Login rootLogin = (Login) tmp.getContext().getBean("rootLogin");
  
    protected ServiceFactory rootServices;
    protected IAdmin rootAdmin;
    protected IQuery rootQuery;
    protected IUpdate rootUpdate;
    
    // shouldn't use beforeTestClass here because called by all subclasses
    // in their beforeTestClass i.e. super.setup(); ... 
    protected void init() throws Exception
    {
        rootServices = new ServiceFactory( rootLogin );
        rootAdmin = rootServices.getAdminService();
        rootQuery = rootServices.getQueryService();
        rootUpdate = rootServices.getUpdateService();
        try 
        {
            rootQuery.get(Experimenter.class,0l);
        } catch (Throwable t){
            // TODO no, no, really. This is ok. (And temporary) 
        }
    }
}
