package ome.client.itests.sec;

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

    protected ExperimenterGroup 
    	userGrp = new ExperimenterGroup( 1L, false ),
		sysGrp = new ExperimenterGroup( 0L, false );

	protected Experimenter root, sudo;
	protected String sudo_name;
	protected String sudo_id;

    protected ServiceFactory tmp = new ServiceFactory( "ome.client.test" );
    protected DataSource dataSource = (DataSource) tmp.getContext().getBean("dataSource");
    protected SimpleJdbcTemplate jdbc = new SimpleJdbcTemplate( dataSource );
    protected Login rootLogin = (Login) tmp.getContext().getBean("rootLogin");
    
    protected ServiceFactory rootServices;
    protected IAdmin rootAdmin;
    protected IQuery rootQuery;
    protected IUpdate rootUpdate;
    
    // ~ Testng Adapter
    // =========================================================================
    @Configuration(beforeTestClass = true)
    public void rootCanLoginWith_ome() throws Exception
    {
        super.setUp();
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
        
        root = rootQuery.get( Experimenter.class, 0L );
        sudo = createNewExperimenter( rootUpdate,
        		new ExperimenterGroup(0L,false),
        		new ExperimenterGroup(1L,false));
        sudo_name = sudo.getOmeName();
        resetPasswordTo_ome(sudo);
        assertCanLogin(sudo_name,"ome");
    }
    
//    @Configuration( afterTestMethod = true, alwaysRun = true )
//    public void deleteSudo() throws Exception
//    {	
//    	rootAdmin.deleteExperimenter( new Experimenter( sudo.getId(), false ) );
//    }
    
    // ~ Helpers
    // =========================================================================

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
				String.class, e.getId()).trim(); // TODO remove trim in sync with JBossLoginModule
		} catch (EmptyResultDataAccessException ex){
			return null;
		}
	}
	
	protected void resetPasswordTo_ome(Experimenter e) throws Exception {
		resetPasswordTo_ome(e.getId());
	}
	
	protected void resetPasswordTo_ome(Long id) throws Exception {
		int count = jdbc.update("update password set hash = ? where experimenter_id = ?",
    			AbstractAccountTest.OME_HASH,
    			id);
    	
		if ( count < 1 )
		{
		
			count = jdbc.update("insert into password values (?,?)",
    			id,
    			AbstractAccountTest.OME_HASH);
			assertTrue( count == 1 );
		}
		dataSource.getConnection().commit();
    	rootAdmin.synchronizeLoginCache();
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
		rootAdmin.synchronizeLoginCache();
		return count;
	}
	
	protected void removePasswordEntry(Experimenter e) throws Exception {
		int count = jdbc.update("delete from password where experimenter_id = ?",
    			e.getId());
		dataSource.getConnection().commit();
		rootAdmin.synchronizeLoginCache();
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
		rootAdmin.synchronizeLoginCache();
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
  
    protected IAdmin getSudoAdmin( String password )
    {
    	return new ServiceFactory( new Login( sudo_name,password))
		.getAdminService();
    }
    
    protected IQuery getSudoQuery( String password )
    {
    	return new ServiceFactory( new Login( sudo_name,password))
		.getQueryService();
    }
    
    protected IUpdate getSudoUpdate( String password )
    {
    	return new ServiceFactory( new Login( sudo_name,password))
		.getUpdateService();
    }

    
}
