package ome.client.itests.sec;

import ome.model.IObject;
import ome.model.core.Pixels;
import ome.model.internal.Permissions;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;

import org.testng.annotations.Test;


@Test(groups = { "ticket:TODO", "security", "integration" })
public class UmaskSecurityTest extends AbstractPermissionsTest 
{
	
	@Override
	public void testSingleProject_R() throws Exception {
		r.setUmask(RW_xx_xx);
		createProject(r, null, system_group);
		assertUmaskWorks(r, prj, RW_xx_xx);
	}

	@Override
	public void testSingleProject_U() throws Exception {
		u.setUmask(RW_xx_xx);
		createProject(u, null, user_other_group);
		assertUmaskWorks(u, prj, RW_xx_xx);
	}

	@Override
	public void testSingleProject_W() throws Exception {
		w.setUmask(RW_xx_xx);
		createProject(w, null, common_group);
		assertUmaskWorks(w, prj, RW_xx_xx);
	}

	@Override
	public void test_O_Pixels_And_U_Thumbnails() throws Exception {}
	@Override
	public void test_U_Image_U_Pixels() throws Exception {}
	@Override
	public void test_U_Instrument_And_U_Microscope() throws Exception {}
	@Override
	public void test_U_Pixels_And_O_Thumbnails() throws Exception {}
	@Override
	public void test_U_Pixels_And_R_Thumbnails() throws Exception {}
	@Override
	public void test_U_Pixels_And_U_Thumbnails() throws Exception {}
	@Override
	public void test_U_Projects_U_Datasets_U_Link() throws Exception {}

	// ~ Helpers
	// =========================================================================
	
	private void assertUmaskWorks(ServiceFactory sf, IObject _i, Permissions perms) {
		sf.setUmask(perms);
		IObject t = sf.getQueryService().get(_i.getClass(), _i.getId());
		assertTrue(t+"!="+perms,
				t.getDetails().getPermissions().sameRights(perms));
	}

}
