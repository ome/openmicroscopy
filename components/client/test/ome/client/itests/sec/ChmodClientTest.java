package ome.client.itests.sec;


import javax.ejb.EJBException;

import org.testng.annotations.*;

import junit.framework.TestCase;

import ome.api.IQuery;
import ome.api.IUpdate;
import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.system.Login;
import ome.system.ServiceFactory;

@Test( 
	groups = {"client","integration","security","ticket:365", "chmod" }
)
public class ChmodClientTest extends AbstractChangeDetailClientTest
{

	// TODO : This series of tests (AbstractChangeDetailClientTest) 
	// should be unified with AbstractPermissionsTest (both setup 
	// UserOtherWorldPiRoot etc.)
	
    // design parameters:
    //  1. various permissions (belonging to root, user, or other)
    //  2. as user or root
    //  3. changing to various permissions 
	
    @Test
    public void test_user_RWRW_user_PUBLIC() throws Exception
    {
    	newUserImagePermissionsAsUserToPermissions(true,
    			asUser, Permissions.GROUP_PRIVATE, asUser, Permissions.PUBLIC);
    }

    @Test
    public void test_user_RWRW_other_PUBLIC() throws Exception
    {
    	newUserImagePermissionsAsUserToPermissions(false,
    			asUser, Permissions.GROUP_PRIVATE, asOther, Permissions.PUBLIC);
    }

    @Test
    public void test_user_RWRW_world_PUBLIC() throws Exception
    {
    	newUserImagePermissionsAsUserToPermissions(false,
    			asUser, Permissions.GROUP_PRIVATE, asWorld, Permissions.PUBLIC);
    }
    
    @Test
    public void test_user_RWRW_pi_PUBLIC() throws Exception
    {
    	newUserImagePermissionsAsUserToPermissions(true,
    			asUser, Permissions.GROUP_PRIVATE, asPI, Permissions.PUBLIC);
    }

    @Test
    public void test_user_RWRW_root_PUBLIC() throws Exception
    {
    	newUserImagePermissionsAsUserToPermissions(true,
    			asUser, Permissions.GROUP_PRIVATE, asRoot, Permissions.PUBLIC);
    }

    @Test( groups = {"ticket:397","broken"} )
    public void testCheckInitialParameters() throws Exception {
		fail("USER CAN CURRENTLY JUST PASS IN WHATEVER OWNER THEY WANT.");
		// UNTAINT
	}
    
    // ~ Helpers
    // =========================================================================
    
    protected void newUserImagePermissionsAsUserToPermissions(boolean ok,
    		Login owner, Permissions orig, Login changer, Permissions target) 
    throws ValidationException
    {
		ServiceFactory factory = new ServiceFactory(owner);
		ServiceFactory factory2 = new ServiceFactory(changer);

		Image i;
		
		// via IAdmin
		try {
			i = new Image();
			i.getDetails().setPermissions(orig);
			i.setName("test");
			i = factory.getUpdateService().saveAndReturnObject(i);
	 
			factory2.getAdminService().changePermissions(i, target);
			if (!ok) fail("secvio!");
		} catch (SecurityViolation sv) {
			if (ok) throw sv;
		}
		
		
		// via Details
		try {
			i = new Image();
			i.getDetails().setPermissions(orig);
			i.setName("test");
			i = factory.getUpdateService().saveAndReturnObject(i);

			i.getDetails().setPermissions(target);
			factory2.getUpdateService().saveObject(i);
			if (!ok) fail("secvio!");
		} catch (SecurityViolation sv) {
			if (ok) throw sv;
		}
    }
    
}
