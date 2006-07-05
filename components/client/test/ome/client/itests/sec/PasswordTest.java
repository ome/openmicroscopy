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
public class PasswordTest extends TestCase
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
    
    // design:
    // 1. who : root or user
    // 2. state : password filled, empty, missing
    // 3. action : change own, change other
    
    // ~ ROOT WITH FILLED PASSWORD
	// =========================================================================
    
    @Test
    public void testRootCanChangePassword() throws Exception {
    	boolean changed = false;
    	try {
	    	getRootAdmin("ome").changePassword("testing...");    		
			assertCanLogin("root","testing...");
			// original still works rootQuery.get(Experimenter.class, 0L);
			assertCannotLogin("root","ome");			
    	} finally {
    		// return to normal.
    		getRootAdmin("testing...").changePassword("ome");
    	}
    }
    
    @Test
    public void testRootCanChangeOthersPassword() throws Exception {

    	Experimenter e = createNewExperimenter();
    	resetPasswordTo_ome(e);
    	assertCanLogin(e.getOmeName(),"ome");

    	getRootAdmin("ome").changeUserPassword(e.getOmeName(), "foo");
    	assertCanLogin(e.getOmeName(),"foo");
    	assertCannotLogin(e.getOmeName(),"bar");
    	assertCannotLogin(e.getOmeName(),"");
    	
    	getRootAdmin("ome").changeUserPassword(e.getOmeName(), "");
    	assertCanLogin(e.getOmeName(),"");
    	assertCanLogin(e.getOmeName(),"NOTCORRECT");

    }
    
    // ~ USER WITH FILLED PASSWORD
	// =========================================================================
    
    @Test
    public void testUserCanChangeOwnPassword() throws Exception {
    	Experimenter e = createNewExperimenter();
    	resetPasswordTo_ome(e);
    	assertCanLogin(e.getOmeName(),"ome");
    	
    	ServiceFactory userServices = new ServiceFactory( new Login(e.getOmeName(),"ome"));
    	userServices.getAdminService().changePassword("test");
    	assertCanLogin(e.getOmeName(),"test");
    	assertCannotLogin(e.getOmeName(),"ome");
    	
	}
    
    @Test
    @ExpectedExceptions( EJBException.class )
    public void testUserCantChangeOthersPassword() throws Exception {
    	Experimenter e = createNewExperimenter();
    	resetPasswordTo_ome(e);
    	assertCanLogin(e.getOmeName(),"ome");
    	
    	Experimenter target = createNewExperimenter();
    	resetPasswordTo_ome(target);
    	assertCanLogin(target.getOmeName(),"ome");
    	
    	ServiceFactory userServices = new ServiceFactory( new Login(e.getOmeName(),"ome"));
    	userServices.getAdminService().changeUserPassword(target.getOmeName(),"test");
    	
	}
    
    // ~ EMPTY PASSWORD
	// =========================================================================
    
    @Test
    public void testAnyOneCanLoginWithEmptyPassword() throws Exception {
		
		Experimenter e = createNewExperimenter();
    	setPasswordtoEmptyString(e);
    	assertCanLogin(e.getOmeName(),"bob");
		assertCanLogin(e.getOmeName(),"");
		assertCanLogin(e.getOmeName(),"ome");    	
	
		new ServiceFactory( new Login(e.getOmeName(),"blah")).getAdminService().
		changePassword("ome");
    
		assertCannotLogin(e.getOmeName(),"bob");
		assertCannotLogin(e.getOmeName(),"");
		assertCanLogin(e.getOmeName(),"ome");    

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		
    	setPasswordtoEmptyString(root);
		assertCanLogin("root","bob");
		assertCanLogin("root","");
		assertCanLogin("root","ome");
		
		new ServiceFactory( new Login("root","blah")).getAdminService().
		changePassword("ome");
	
		assertCannotLogin("root","bob");
		assertCannotLogin("root","");
		assertCanLogin("root","ome");	
	
    }
    
    // ~ MISSING PASSWORD (Locked account)
	// =========================================================================

    @Test
    public void testNoOneCanLoginWithMissingPassword() throws Exception {
		
		Experimenter e = createNewExperimenter();
    	removePasswordEntry(e);
    	
    	assertCannotLogin(e.getOmeName(),"bob");
		assertCannotLogin(e.getOmeName(),"");
		assertCannotLogin(e.getOmeName(),"ome");    	

		resetPasswordTo_ome(e);

		assertCannotLogin(e.getOmeName(),"bob");
		assertCannotLogin(e.getOmeName(),"");
		assertCanLogin(e.getOmeName(),"ome");   
    
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		
    	removePasswordEntry(root);
    	
		assertCannotLogin("root","bob");
		assertCannotLogin("root","");
		assertCannotLogin("root","ome");
		
		resetPasswordTo_ome(root);
		
		assertCannotLogin("root","bob");
		assertCannotLogin("root","");
		assertCanLogin("root","ome");
		
    }
    
    @Test
    public void testNoOneCanLoginWithNullPassword() throws Exception {
		
		Experimenter e = createNewExperimenter();
    	nullPasswordEntry(e);
    	
    	assertCannotLogin(e.getOmeName(),"bob");
		assertCannotLogin(e.getOmeName(),"");
		assertCannotLogin(e.getOmeName(),"ome");    	

		resetPasswordTo_ome(e);

		assertCannotLogin(e.getOmeName(),"bob");
		assertCannotLogin(e.getOmeName(),"");
		assertCanLogin(e.getOmeName(),"ome");   
    
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		
    	nullPasswordEntry(root);
    	
		assertCannotLogin("root","bob");
		assertCannotLogin("root","");
		assertCannotLogin("root","ome");
		
		resetPasswordTo_ome(root);
		
		assertCannotLogin("root","bob");
		assertCannotLogin("root","");
		assertCanLogin("root","ome");

	
    }
    
    @Test( groups = "special")
    public void testSpecialCaseOfRootsOldPassword() throws Exception {
		resetPasswordTo_ome(root);
		assertTrue( OME_HASH.equals( getPasswordFromDb(root) ));
		
		assertCanLogin("root","ome");
		assertCannotLogin("root","bob");
		assertCannotLogin("root","");
		
		assertTrue( OME_HASH.equals( getPasswordFromDb(root) ));
		
		removePasswordEntry(root);
		assertNull( getPasswordFromDb(root) );
		
		assertCannotLogin("root","");
		assertCannotLogin("root","bob");
		
		assertNull( getPasswordFromDb(root) );
		
		assertCannotLogin("root","ome");
		
		assertNull( getPasswordFromDb(root) );
		
	}
    
    
    // ~ Helpers
    // =========================================================================

	private Experimenter createNewExperimenter() {
		return createNewExperimenter( getUpdateBackdoor(), 
				new ExperimenterGroup( 1L, false  ));
	}

	private Experimenter createNewExperimenter(IUpdate iUpdate, 
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
	
	private String getPasswordFromDb(Experimenter e) throws Exception {
		try {
			return jdbc.queryForObject("select hash from password " +
				"where experimenter_id = ?",
				String.class, e.getId());
		} catch (EmptyResultDataAccessException ex){
			return null;
		}
	}
	
	private void resetPasswordTo_ome(Experimenter e) throws Exception {
		int count = jdbc.update("update password set hash = ? where experimenter_id = ?",
    			PasswordTest.OME_HASH,
    			e.getId());
    	
		if ( count < 1 )
		{
		
			count = jdbc.update("insert into password values (?,?)",
    			e.getId(),
    			PasswordTest.OME_HASH);
			assertTrue( count == 1 );
		}
		dataSource.getConnection().commit();
    	getAdminBackdoor().synchronizeLoginCache();
	}

	private int setPasswordtoEmptyString(Experimenter e) throws Exception {
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
	
	private void removePasswordEntry(Experimenter e) throws Exception {
		int count = jdbc.update("delete from password where experimenter_id = ?",
    			e.getId());
		dataSource.getConnection().commit();
		getAdminBackdoor().synchronizeLoginCache();
	}
	
	private void nullPasswordEntry(Experimenter e) throws Exception {
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
