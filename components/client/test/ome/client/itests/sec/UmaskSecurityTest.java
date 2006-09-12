package ome.client.itests.sec;

import ome.model.core.Pixels;
import ome.model.internal.Permissions;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;

import org.testng.annotations.Test;


@Test(groups = { "ticket:200", "security", "integration" })
public class UmaskSecurityTest extends AbstractPermissionsTest 
{

	Pixels p = ObjectFactory.createPixelGraph(null);
	Pixels t;
	
	@Test
	public void testU() throws Exception {
		assertUmaskWorks(u,RW_RW_RW);
		assertUmaskWorks(u,RW_xx_xx);
	}
	
	@Test
	public void testR() throws Exception {
		assertUmaskWorks(r,RW_RW_RW);
		assertUmaskWorks(r,RW_xx_xx);
	}

	private void assertUmaskWorks(ServiceFactory sf, Permissions perms) {
		sf.setUmask(perms);
		t = sf.getUpdateService().saveAndReturnObject(p);
		assertTrue(t+"!="+perms,
				t.getDetails().getPermissions().identical(perms));
	}

}
