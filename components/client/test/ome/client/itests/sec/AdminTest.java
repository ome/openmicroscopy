package ome.client.itests.sec;

import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
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
	
	@Test( groups = "ticket:397" )
	public void testChangePermissionsCantMisuseAdminAction() throws Exception {
		
		Experimenter e = createNewUser(rootAdmin);
		Login l = new Login(e.getOmeName(),"");
		ServiceFactory u = new ServiceFactory(l);
		
		// make an image
		Image i = new Image();
		i.setName("adminactiontest");
		i = u.getUpdateService().saveAndReturnObject(i);
		
		// use changePerms to change the permissions
		// but try to pass in a trojan horse
		Permissions perms = new Permissions().grant(Role.WORLD, Right.WRITE);
		i.getDetails().setOwner( new Experimenter( 0L, false ));
		u.getAdminService().changePermissions(i, perms);
		i = u.getQueryService().get(i.getClass(), i.getId());
		assertFalse( i.getDetails().getOwner().getId().equals( 0L ));
		
	}
	
}
