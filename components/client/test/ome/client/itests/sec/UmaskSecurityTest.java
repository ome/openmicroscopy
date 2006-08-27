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
		assertUmaskWorks(u,RWU_RWU_RWU);
		assertUmaskWorks(u,RWU_xxx_xxx);
	}
	
	@Test
	public void testR() throws Exception {
		assertUmaskWorks(r,RWU_RWU_RWU);
		assertUmaskWorks(r,RWU_xxx_xxx);
	}

	private void assertUmaskWorks(ServiceFactory sf, Permissions perms) {
		sf.setUmask(perms);
		t = sf.getUpdateService().saveAndReturnObject(p);
		assertTrue(t+"!="+perms,
				t.getDetails().getPermissions().identical(perms));
	}

}
