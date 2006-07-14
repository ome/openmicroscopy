package ome.client.itests.sec;

import org.jboss.util.id.GUID;
import org.testng.annotations.*;

import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.Login;
import ome.system.ServiceFactory;

@Test( 
	groups = {"client","integration","security",
			  "ticket:181","ticket:199", 
			  "password"} 
)
public class AccountCreationTest extends AbstractAccountTest
{

    @Test
    public void testSudoCreatesAccountThroughIUpdate() throws Exception {
    	Experimenter e = createNewExperimenter( getSudoUpdate("ome"), userGrp );
    	assertNull( getPasswordFromDb(e));
    	assertCannotLogin(e.getOmeName(),"ome");
    	assertCannotLogin(e.getOmeName(),"");
    	
    	doesNotHaveSystemPrivileges(e);
    	
    	getSudoAdmin("ome").changeUserPassword(e.getOmeName(),"test");
    	assertCanLogin(e.getOmeName(),"test");
    }
    
    @Test
    public void testSudoCreatesUserAccountThroughIAdmin() throws Exception {
    	Experimenter e = new Experimenter();
    	e.setOmeName(new GUID().asString());
    	e.setFirstName("ticket:181");
    	e.setLastName("ticket:199");
    	e = getSudoAdmin("ome").createUser(e);
    	assertCanLogin(e.getOmeName(),"");
    	assertCanLogin(e.getOmeName(),"ome");
    	assertCanLogin(e.getOmeName(),"bob");
    	
    	doesNotHaveSystemPrivileges(e);
    }
    
    @Test
    public void testSudoCreatesSystemAccountThroughIAdmin() throws Exception {
		Experimenter e = new Experimenter();
		e.setOmeName(new GUID().asString());
		e.setFirstName("ticket:181");
		e.setLastName("ticket:199");
		e = getSudoAdmin("ome").createSystemUser(e);
		assertCanLogin(e.getOmeName(),"");
    	assertCanLogin(e.getOmeName(),"ome");
    	assertCanLogin(e.getOmeName(),"bob");
    	
		hasSystemPrivileges(e);
		
		getSudoAdmin("ome").changeUserPassword(e.getOmeName(), "bob");
		
		assertCannotLogin(e.getOmeName(),"");
    	assertCannotLogin(e.getOmeName(),"ome");
    	assertCanLogin(e.getOmeName(),"bob");
    	
	}

    @Test
    public void testSudoCreatesAccountThroughIAdmin() throws Exception {
    	Experimenter e = new Experimenter();
    	e.setOmeName(new GUID().asString());
    	e.setFirstName("ticket:181");
    	e.setLastName("ticket:199");
    	e = getSudoAdmin("ome").createExperimenter(e, userGrp,null);
    	assertCanLogin(e.getOmeName(),"");
    	assertCanLogin(e.getOmeName(),"ome");
    	assertCanLogin(e.getOmeName(),"bob");
    
    	doesNotHaveSystemPrivileges(e);
    	
    	getSudoAdmin("ome").changeUserPassword(e.getOmeName(), "bob");
		
		assertCannotLogin(e.getOmeName(),"");
    	assertCannotLogin(e.getOmeName(),"ome");
    	assertCanLogin(e.getOmeName(),"bob");
    	
    }

    @Test
    public void testSudoSysCreatesAccountThroughIAdmin() throws Exception {
    	Experimenter e = new Experimenter();
    	e.setOmeName(new GUID().asString());
    	e.setFirstName("ticket:181");
    	e.setLastName("ticket:199");
    	e = getSudoAdmin("ome").createExperimenter(e, userGrp,
    			new ExperimenterGroup[]{ sysGrp });
    	assertCanLogin(e.getOmeName(),"");
    	assertCanLogin(e.getOmeName(),"ome");
    	assertCanLogin(e.getOmeName(),"bob");
    
    	hasSystemPrivileges(e);
    	
    	getSudoAdmin("ome").changeUserPassword(e.getOmeName(), "bob");
		
		assertCannotLogin(e.getOmeName(),"");
    	assertCannotLogin(e.getOmeName(),"ome");
    	assertCanLogin(e.getOmeName(),"bob");
    	
    }
    
    // ~ Helpers
	// =========================================================================


	private void hasSystemPrivileges(Experimenter e) {
		new ServiceFactory( new Login(e.getOmeName(),"")).getAdminService().
		synchronizeLoginCache();
	}


	private void doesNotHaveSystemPrivileges(Experimenter e) {
		try {
    	hasSystemPrivileges(e);
    	fail("Should be security violation");
    	} catch (Exception ex) {
    		//ok.
    	}
	}
	
}
