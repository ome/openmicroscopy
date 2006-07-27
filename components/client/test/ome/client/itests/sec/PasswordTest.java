package ome.client.itests.sec;

import javax.ejb.EJBException;
import org.testng.annotations.*;


import ome.api.IAdmin;
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
    // 1. who : sudo or user (doing sudo because playing with root is a pain)
    // 2. state : password filled, empty, missing
    // 3. action : change own, change other
    
    // ~ SUDO WITH FILLED PASSWORD
	// =========================================================================
    
    @Test
    public void testSudoCanChangePassword() throws Exception {
    	try {
    		IAdmin sudoAdmin = getSudoAdmin("ome");
	    	sudoAdmin.changePassword("testing...");    		
			assertCanLogin(sudo_name,"testing...");
			try {
				sudoAdmin.synchronizeLoginCache();
				// TODO original still works 
				// fail("Old services should be unusable.");
			} catch (Exception ex) {
				// ok
			}
			assertCannotLogin(sudo_name,"ome");			
    	} finally {
    		// return to normal.
    		getSudoAdmin("testing...").changePassword("ome");
    	}
    }
    
    @Test
    public void testSudoCanChangeOthersPassword() throws Exception {

    	Experimenter e = createNewUser( rootAdmin );
    	resetPasswordTo_ome(e);
    	assertCanLogin(e.getOmeName(),"ome");

    	getSudoAdmin("ome").changeUserPassword(e.getOmeName(), "foo");
    	assertCanLogin(e.getOmeName(),"foo");
    	assertCannotLogin(e.getOmeName(),"bar");
    	assertCannotLogin(e.getOmeName(),"");
    	
    	getSudoAdmin("ome").changeUserPassword(e.getOmeName(), "");
    	assertCanLogin(e.getOmeName(),"");
    	assertCanLogin(e.getOmeName(),"NOTCORRECT");

    }
    
    // ~ USER WITH FILLED PASSWORD
	// =========================================================================
    
    @Test
    public void testUserCanChangeOwnPassword() throws Exception {
    	Experimenter e = createNewUser( rootAdmin );
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
    	Experimenter e = createNewUser( getSudoAdmin("ome") );
    	resetPasswordTo_ome(e);
    	assertCanLogin(e.getOmeName(),"ome");
    	
    	Experimenter target = createNewUser( getSudoAdmin("ome") );
    	resetPasswordTo_ome(target);
    	assertCanLogin(target.getOmeName(),"ome");
    	
    	ServiceFactory userServices = new ServiceFactory( new Login(e.getOmeName(),"ome"));
    	userServices.getAdminService().changeUserPassword(target.getOmeName(),"test");
    	
	}
    
    // ~ EMPTY PASSWORD
	// =========================================================================
    
    @Test
    public void testAnyOneCanLoginWithEmptyPassword() throws Exception {
		
    	Experimenter e = createNewUser( rootAdmin );
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
		
    	setPasswordtoEmptyString(sudo);
		assertCanLogin(sudo_name,"bob");
		assertCanLogin(sudo_name,"");
		assertCanLogin(sudo_name,"ome");
	
		getSudoAdmin("blah").changePassword("ome");
	
		assertCannotLogin(sudo_name,"bob");
		assertCannotLogin(sudo_name,"");
		assertCanLogin(sudo_name,"ome");	
	
    }
    
    // ~ MISSING PASSWORD (Locked account)
	// =========================================================================

    @Test
    public void testNoOneCanLoginWithMissingPassword() throws Exception {
		
    	Experimenter e = createNewUser( rootAdmin );
    	removePasswordEntry(e);
    	
    	assertCannotLogin(e.getOmeName(),"bob");
		assertCannotLogin(e.getOmeName(),"");
		assertCannotLogin(e.getOmeName(),"ome");    	

		resetPasswordTo_ome(e);

		assertCannotLogin(e.getOmeName(),"bob");
		assertCannotLogin(e.getOmeName(),"");
		assertCanLogin(e.getOmeName(),"ome");   
    
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		
    	removePasswordEntry(sudo);
    	
		assertCannotLogin(sudo_name,"bob");
		assertCannotLogin(sudo_name,"");
		assertCannotLogin(sudo_name,"ome");
		
		resetPasswordTo_ome(sudo);
		
		assertCannotLogin(sudo_name,"bob");
		assertCannotLogin(sudo_name,"");
		assertCanLogin(sudo_name,"ome");
		
    }
    
    @Test
    public void testNoOneCanLoginWithNullPassword() throws Exception {
		
    	Experimenter e = createNewUser( rootAdmin );
    	nullPasswordEntry(e);
    	
    	assertCannotLogin(e.getOmeName(),"bob");
		assertCannotLogin(e.getOmeName(),"");
		assertCannotLogin(e.getOmeName(),"ome");    	

		resetPasswordTo_ome(e);

		assertCannotLogin(e.getOmeName(),"bob");
		assertCannotLogin(e.getOmeName(),"");
		assertCanLogin(e.getOmeName(),"ome");   
    
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		
    	nullPasswordEntry(sudo);
    	
		assertCannotLogin(sudo_name,"bob");
		assertCannotLogin(sudo_name,"");
		assertCannotLogin(sudo_name,"ome");
		
		resetPasswordTo_ome(sudo);
		
		assertCannotLogin(sudo_name,"bob");
		assertCannotLogin(sudo_name,"");
		assertCanLogin(sudo_name,"ome");

	
    }
    
    @Test( groups = "special")
    public void testSpecialCaseOfSudosOldPassword() throws Exception {
		resetPasswordTo_ome(sudo);
		assertTrue( OME_HASH.equals( getPasswordFromDb(sudo) ));
		
		assertCanLogin(sudo_name,"ome");
		assertCannotLogin(sudo_name,"bob");
		assertCannotLogin(sudo_name,"");
		
		assertTrue( OME_HASH.equals( getPasswordFromDb(sudo) ));
		
		removePasswordEntry(sudo);
		assertNull( getPasswordFromDb(sudo) );
		
		assertCannotLogin(sudo_name,"");
		assertCannotLogin(sudo_name,"bob");
		
		assertNull( getPasswordFromDb(sudo) );
		
		assertCannotLogin(sudo_name,"ome");
		
		assertNull( getPasswordFromDb(sudo) );
		
	}
    
}
