package ome.client.itests.sec;

import ome.model.core.Image;
import ome.model.meta.Experimenter;
import ome.system.Login;
import ome.system.ServiceFactory;

import org.testng.annotations.Test;

public class AdminTest extends AbstractAccountTest {

	// ~ chown / chgrp / chmod
	// =========================================================================
	
	@Test
	public void testChownThroughIUpdateActuallyWorks() throws Exception {
		// new user
		Experimenter e = createNewUser(rootAdmin);
		Login l = new Login(e.getOmeName(),"");
		ServiceFactory u = new ServiceFactory(l);
		
		// target user
		Experimenter target = createNewUser(rootAdmin);
		
		// new image
		Image i = new Image();
		i.setName("test");
		i = u.getUpdateService().saveAndReturnObject(i);
		
		// change owner
		Image test = rootQuery.get(Image.class,i.getId());
		test.getDetails().setOwner(target);
		rootUpdate.saveObject(test);
		test = rootQuery.get(Image.class,i.getId());
		assertEquals(test.getDetails().getOwner().getId(),target.getId());
	}
	
	@Test
	public void testChangePermissionsCantMisuseAdminAction() throws Exception {
		fail("implement");
	}
	
}
