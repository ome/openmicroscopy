package ome.client.itests.sec;

import javax.ejb.EJBException;
import javax.sql.DataSource;

import org.jboss.util.id.GUID;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.testng.annotations.*;

import junit.framework.TestCase;

import ome.api.IAdmin;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.system.Login;
import ome.system.ServiceFactory;

@Test( 
	groups = {"client","integration","security",
			  "ticket:181","ticket:199", 
			  "password"} 
)
public class AbstractAccountTest extends TestCase
{

    protected static final String OME_HASH = "vvFwuczAmpyoRC0Nsv8FCw==";

	protected Experimenter root, sudo;

    protected ServiceFactory tmp = new ServiceFactory( "ome.client.test" );
    protected DataSource dataSource = (DataSource) tmp.getContext().getBean("dataSource");
    protected SimpleJdbcTemplate jdbc = new SimpleJdbcTemplate( dataSource );
    
    // ~ Testng Adapter
    // =========================================================================
    @Configuration( afterTestMethod = true, alwaysRun = true )
    public void resetRootLoginTo_ome() throws Exception
    {
    	resetPasswordTo_ome(root);
    	assertCanLogin("root","ome");
    }
    
    @Configuration(beforeTestClass = true)
    public void rootCanLoginWith_ome() throws Exception
    {
        super.setUp();

        IQuery rootQuery = new ServiceFactory( new Login("root","ome")).getQueryService();
        try 
        {
            rootQuery.get(Experimenter.class,0l);
        } catch (Throwable t){
            // TODO no, no, really. This is ok. (And temporary) 
        }
        
        root = rootQuery.get( Experimenter.class, 0L );
        sudo = createNewExperimenter(getRootUpdate("ome"),
        		new ExperimenterGroup(0L,false),
        		new ExperimenterGroup(1L,false));
        resetPasswordTo_ome(sudo);
        assertCanLogin(sudo.getOmeName(),"ome");
    }
      
    // ~ Helpers
    // =========================================================================

	protected Experimenter createNewExperimenter() {
		return createNewExperimenter( getUpdateBackdoor(), 
				new ExperimenterGroup( 1L, false  ));
	}

	protected Experimenter createNewExperimenter(IUpdate iUpdate, 
			ExperimenterGroup...groups) {
		
		Experimenter e = new Experimenter();
    	e.setOmeName(new GUID().asString());
    	e.setFirstName("ticket:181");
    	e.setLastName("ticket:181");
    	for (ExperimenterGroup group : groups) {
    		GroupExperimenterMap map = new GroupExperimenterMap();
    		map.link(group,e);
			e.addGroupExperimenterMap(map, false);
		}
    	e = iUpdate.saveAndReturnObject(e);
		return e;
	}
	
	protected String getPasswordFromDb(Experimenter e) throws Exception {
		try {
			return jdbc.queryForObject("select hash from password " +
				"where experimenter_id = ?",
				String.class, e.getId());
		} catch (EmptyResultDataAccessException ex){
			return null;
		}
	}
	
	protected void resetPasswordTo_ome(Experimenter e) throws Exception {
		int count = jdbc.update("update password set hash = ? where experimenter_id = ?",
    			AbstractAccountTest.OME_HASH,
    			e.getId());
    	
		if ( count < 1 )
		{
		
			count = jdbc.update("insert into password values (?,?)",
    			e.getId(),
    			AbstractAccountTest.OME_HASH);
			assertTrue( count == 1 );
		}
		dataSource.getConnection().commit();
    	getAdminBackdoor().synchronizeLoginCache();
	}

	protected int setPasswordtoEmptyString(Experimenter e) throws Exception {
		int count = jdbc.update("update password set hash = ? where experimenter_id = ?",
    			"",
    			e.getId());
		if ( count < 1 )
		{
			count = jdbc.update("insert into password values (?,?)",
					e.getId(),
					"");
		}
		dataSource.getConnection().commit();
		getAdminBackdoor().synchronizeLoginCache();
		return count;
	}
	
	protected void removePasswordEntry(Experimenter e) throws Exception {
		int count = jdbc.update("delete from password where experimenter_id = ?",
    			e.getId());
		dataSource.getConnection().commit();
		getAdminBackdoor().synchronizeLoginCache();
	}
	
	protected void nullPasswordEntry(Experimenter e) throws Exception {
		int count = jdbc.update("update password set hash = null where experimenter_id = ?",
    			e.getId());
		if ( count < 1 )
		{
			count = jdbc.update("insert into password values (?,null)",
					e.getId());
		}
		dataSource.getConnection().commit();
		getAdminBackdoor().synchronizeLoginCache();
	}

	protected void assertCanLogin(String name, String password)
	{
		assertLogin(name,password,true);
	}
	
	protected void assertCannotLogin(String name, String password)
	{
		assertLogin(name,password,false);
	}
	
    protected void assertLogin(String name, String password, boolean works)
    {
    	try {
    		new ServiceFactory( new Login(name,password) ).getQueryService().
    		get(Experimenter.class,0L); 
    		if (!works) fail("Login should not have succeeded:"+name+":"+password);
    	} catch (Exception e) {
    		if (works) throw new RuntimeException(e);
    	}
    	
    }
  
    protected IAdmin getRootAdmin( String password )
    {
    	return new ServiceFactory( new Login( root.getOmeName(),password))
		.getAdminService();
    }
    
    protected IQuery getRootQuery( String password )
    {
    	return new ServiceFactory( new Login( root.getOmeName(),password))
		.getQueryService();
    }
    
    protected IUpdate getRootUpdate( String password )
    {
    	return new ServiceFactory( new Login( root.getOmeName(),password))
		.getUpdateService();
    }
    
    protected IAdmin getAdminBackdoor()
    {
    	return new ServiceFactory( new Login( sudo.getOmeName(),"ome"))
    		.getAdminService();
    }

    protected IUpdate getUpdateBackdoor()
    {
    	return new ServiceFactory( new Login( sudo.getOmeName(),"ome"))
    		.getUpdateService();
    }
    
}
