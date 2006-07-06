package ome.client.itests.sec;

import javax.ejb.EJBException;
import org.testng.annotations.*;


import ome.model.meta.Experimenter;
import ome.system.Login;
import ome.system.ServiceFactory;

@Test( 
	groups = {"client","integration","security",
			  "ticket:181","ticket:199", 
			  "password"} 
)
public class PasswordTest extends AbstractAccountTest
{

    // design:
    // 1. who : root or user
    // 2. state : password filled, empty, missing
    // 3. action : change own, change other
    
    // ~ ROOT WITH FILLED PASSWORD
	// =========================================================================
    
    @Test
    public void testRootCanChangePassword() throws Exception {
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
    
}
