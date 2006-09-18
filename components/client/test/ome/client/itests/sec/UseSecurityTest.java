package ome.client.itests.sec;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import ome.api.IUpdate;
import ome.conditions.SecurityViolation;
import ome.model.IEnum;
import ome.model.IObject;
import ome.model.acquisition.ImagingEnvironment;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.Thumbnail;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Flag;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.parameters.Parameters;
import ome.system.Login;
import ome.system.ServiceFactory;
import ome.util.ShallowCopy;

import static ome.model.internal.Permissions.Right.*;
import static ome.model.internal.Permissions.Role.*;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import junit.framework.TestCase;

@Test(groups = { "ticket:337", "security", "integration" })
public class UseSecurityTest extends AbstractPermissionsTest
{
	
//  unlike READ and WRITE tests, we don't need to repeat the tests for each user
//	create and the the test is what the user is trying to change.
//	what's allowed when locked. then it makes sense to also test root 
//	too see if it will fail. don't need to test the unlinkable configurations.	
	
	boolean will_lock;
	
	// ~ single
	// =========================================================================
	
	// single plays little role in USE, but verify not locked
	public void testSingleProject_U() throws Exception 
	{
		createProject(u, RW_Rx_Rx, user_other_group);
		verifyDetails(prj, user, user_other_group, RW_Rx_Rx);
		verifyLockStatus(prj, false);
		verifyLocked(u, prj, d(prj,RW_xx_xx), true);
		verifyLocked(u, prj, d(prj,common_group), true);
	}
	public void testSingleProject_W() throws Exception
	{
		createProject(w, RW_Rx_Rx, common_group);
		verifyDetails(prj, world, common_group, RW_Rx_Rx);
		verifyLockStatus(prj, false);
		verifyLocked(w, prj, d(prj,RW_xx_xx), true); // no other group
	}

	public void testSingleProject_R() throws Exception
	{
		createProject(r, RW_Rx_Rx, system_group);
		verifyDetails(prj, root, system_group, RW_Rx_Rx);
		verifyLockStatus(prj, false);
		verifyLocked(r, prj, d(prj,RW_xx_xx), true);
		verifyLocked(r, prj, d(prj,common_group), true);
		verifyLocked(r, prj, d(prj,world), true);
	}

	
	// ~ one-to-many
	// =========================================================================
	
	public void test_U_Pixels_And_U_Thumbnails() throws Exception {
		ownsfA = u;
		ownerA = user;
		groupA = user_other_group;
		
		ownsfB = u;
		ownerB = user;
		groupB = user_other_group;
		
		will_lock = false;
				
		// RW_RW_RW / RW_RW_RW
		permsA = RW_RW_RW;
		permsB = RW_RW_RW;
		oneToMany(ownsfA,true,RW_xx_xx,common_group);
		
		// RW_RW_RW / RW_RW_Rx 
		permsA = RW_RW_RW;
		permsB = RW_RW_Rx;
		oneToMany(ownsfA,true,RW_xx_xx,common_group);
		
		// RW_RW_RW / RW_RW_xx 
		permsA = RW_RW_RW;
		permsB = RW_RW_xx;
		oneToMany(ownsfA,true,RW_xx_xx,common_group);

		// RW_RW_RW / RW_Rx_Rx
		permsA = RW_RW_RW;
		permsB = RW_Rx_Rx;
		oneToMany(ownsfA,true,RW_xx_xx,common_group);
		
		// RW_RW_RW / RW_xx_xx		
		permsA = RW_RW_RW;
		permsB = RW_xx_xx;
		oneToMany(ownsfA,true,RW_xx_xx,common_group);

		// RW_RW_Rx / RW_RW_Rx
		permsA = RW_RW_Rx;
		permsB = RW_RW_Rx;
		oneToMany(ownsfA,true,RW_xx_xx,common_group);

		// RW_RW_xx / RW_RW_xx
		permsA = RW_RW_xx;
		permsB = RW_RW_xx;
		oneToMany(ownsfA,true,RW_xx_xx,common_group);

		// RW_Rx_Rx / RW_Rx_Rx
		permsA = RW_Rx_Rx;
		permsB = RW_Rx_Rx;
		oneToMany(ownsfA,true,RW_xx_xx,common_group);
		
		// RW_xx_xx / RW_xx_xx
		permsA = RW_xx_xx;
		permsB = RW_xx_xx;
		oneToMany(ownsfA,true,RW_xx_xx,common_group);

		// Rx_Rx_Rx / Rx_Rx_Rx
		permsA = Rx_Rx_Rx;
		permsB = Rx_Rx_Rx;
		oneToMany(ownsfA,true,RW_xx_xx,common_group);
		
		// xx_xx_xx / xx_xx_xx
		permsA = xx_xx_xx;
		permsB = xx_xx_xx;
		oneToMany(ownsfA,true,RW_xx_xx,common_group);
		
	}

	public void test_O_Pixels_And_U_Thumbnails() throws Exception {
		ownsfA = o;
		ownerA = other;
		groupA = user_other_group;
		
		ownsfB = u;
		ownerB = user;
		groupB = user_other_group;
		
		will_lock = true;

		// RW_RW_RW / RW_RW_RW
		permsA = RW_RW_RW;
		permsB = RW_RW_RW;
		oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
		oneToMany(r, true, user);

		// RW_RW_RW / RW_RW_Rx 
		permsA = RW_RW_RW;
		permsB = RW_RW_Rx;
		oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
		oneToMany(r, true, user);
		
		// RW_RW_RW / RW_RW_xx 
		permsA = RW_RW_RW;
		permsB = RW_RW_xx;
		oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
		oneToMany(r, true, user);
		
		// RW_RW_RW / RW_xx_xx
		permsA = RW_RW_RW;
		permsB = RW_xx_xx;
		oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
		oneToMany(r, true, user);

		// RW_RW_RW / xx_xx_xx		
		permsA = RW_RW_RW;
		permsB = xx_xx_xx;
		oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
		oneToMany(r, true, user);
		
		// RW_RW_xx / RW_xx_xx
		permsA = RW_RW_xx;
		permsB = RW_xx_xx;
		oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
		oneToMany(r, false, user);
		
	}
	
	/** */ @Override
	public void test_U_Pixels_And_O_Thumbnails() throws Exception {
		fail("implement");
	}
	
	public void test_U_Pixels_And_R_Thumbnails() throws Exception {
		ownsfA = u;
		ownerA = user;
		groupA = user_other_group;
		
		ownsfB = r;
		ownerB = root;
		groupB = system_group;

		will_lock = true;
		
		// root can read everything and so can lock everything.
		
		// RW_RW_RW / RW_RW_RW
		permsA = RW_RW_RW;
		permsB = RW_RW_RW;
		oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
		oneToMany(r, true, user);

		// RW_RW_RW / RW_RW_Rx 
		permsA = RW_RW_RW;
		permsB = RW_RW_Rx;
		oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
		oneToMany(r, true, user);
		
		// RW_RW_RW / RW_RW_xx 
		permsA = RW_RW_RW;
		permsB = RW_RW_xx;
		oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
		oneToMany(r, true, user);
		
		// RW_RW_RW / RW_xx_xx
		permsA = RW_RW_RW;
		permsB = RW_xx_xx;
		oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
		oneToMany(r, true, user);

		// RW_RW_RW / xx_xx_xx		
		permsA = RW_RW_RW;
		permsB = xx_xx_xx;
		oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
		oneToMany(r, true, user);
		
		// RW_xx_xx / RW_xx_xx
		permsA = RW_xx_xx;
		permsB = RW_xx_xx;
		oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
		oneToMany(r, false, user);

		// xx_xx_xx / xx_xx_xx
		permsA = xx_xx_xx;
		permsB = xx_xx_xx;
		oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
		oneToMany(r, false, user);
		
	}
	
	protected void oneToMany(ServiceFactory sf, boolean can_change, Object...details_changed)
	{

//		link no read
//		OR link then locked
//		try to unlock (reload & check) ...only root works
		
		// whether or not this is valid is handled in the ReadSecurityTest.
		// an exception here means something went wrong elsewhere.
		createPixels(ownsfA,groupA,permsA);
		verifyDetails(pix, ownerA, groupA, permsA);
		createThumbnail(ownsfB,groupB,permsB,pix);
		verifyDetails(tb,  ownerB,  groupB,  permsB); 

		verifyLockStatus(pix,will_lock);
		for (Object object : details_changed) {
			verifyLocked(sf, pix, d(pix,object), can_change);
		}
			
	}

	// ~ unidirectional many-to-one
	// =========================================================================
	
	/** */ @Override
	public void test_U_Instrument_And_U_Microscope() throws Exception 
	{
		fail("implement");
	}
	
	// ~ many-to-many
	// =========================================================================

	public void test_U_Projects_U_Datasets_U_Link() throws Exception {
		ownsfA = ownsfB = ownsfC = u;
		ownerA = ownerB = ownerC = user;
		groupA = groupB = groupC = user_other_group;
		
		// RW_RW_RW / RW_RW_RW / RW_RW_RW
		permsA = permsB = permsC = RW_RW_RW;
		manyToMany(u,true,true,true);
		manyToMany(o,true,true,true);
		manyToMany(w,true,true,true);
		manyToMany(p,true,true,true);
		manyToMany(r,true,true,true);
		
		// RW_RW_RW / RW_RW_xx / RW_RW_RW
		permsA = RW_RW_RW;
		permsB = RW_RW_xx;
		permsC = RW_RW_RW;
		manyToMany(u,true,true,true);
		manyToMany(o,true,true,true);
		manyToMany(w,true,false,true);
		manyToMany(p,true,true,true);
		manyToMany(r,true,true,true);
		
		// RW_RW_RW / RW_xx_xx / RW_RW_RW
		permsA = RW_RW_RW;
		permsB = RW_xx_xx;
		permsC = RW_RW_RW;
		manyToMany(u,true,true,true);
		manyToMany(o,true,false,true);
		manyToMany(w,true,false,true);
		manyToMany(p,true,true,true);
		manyToMany(r,true,true,true);
		
	}

	/** performs various write operations on linked projects and datasets.
	 */
	protected void manyToMany(ServiceFactory sf, 
			boolean prj_ok, 
			boolean ds_ok,
			boolean link_ok)
	{
		
		createProject(ownsfA, permsA, groupA);
		createDataset(ownsfB, permsB, groupB);
		createPDLink(ownsfC, permsC, groupC); 

		verifyDetails(prj, ownerA, groupA, permsA);
		verifyDetails(ds,  ownerB,groupB,permsB);
		verifyDetails(link,ownerC,groupC,permsC);
		
		ProjectDatasetLink testC = null;
		Dataset testB = null;
		Project test = null;
		String MSG = makeModifiedMessage();
		
		ds.setName(MSG);
		try {
			testB = sf.getUpdateService().saveAndReturnObject(ds);
			if (!ds_ok) fail("secvio!");
			assertTrue( MSG.equals( testB.getName() ));
		} catch (SecurityViolation sv) {
			if (ds_ok) throw sv;
		}
		
		prj.setName(MSG);
		
		try {
			test = sf.getUpdateService().saveAndReturnObject(prj);
			if (!prj_ok) fail("secvio!");
			assertTrue( MSG.equals( test.getName() ));
		} catch (SecurityViolation sv) {
			if (prj_ok) throw sv;
		}
		
		// try to change the link to point to something different. 
		// should be immutable!!!
		
//		try 
//		{
//			sf.getUpdateService().deleteObject(link);
//			if (!link_ok) fail("secvio!");
//		} catch (SecurityViolation sv) { 
//			if (link_ok) throw sv;
//		} finally {
//			prj.clearDatasetLinks(); // done for later recursive delete.
//			ds.clearProjectLinks();  // ditto;
//		}
//		
//		
//		try 
//		{
//			deleteRecurisvely(sf, ds);
//			if (!ds_ok) fail("secvio!");
//		} catch (SecurityViolation sv) { 
//			if (ds_ok) throw sv;
//		} 
//		
//		try 
//		{
//			deleteRecurisvely( sf, prj );
//			if (!prj_ok) fail("secvio!");
//		} catch (SecurityViolation sv) { 
//			if (prj_ok) throw sv;
//		}
		
	}
	
	// ~ Special: "tag" (e.g. Image/Pixels)
	// =========================================================================

	@Test
	public void test_U_Image_U_Pixels() throws Exception {
		createPixels(u, user_other_group, RW_RW_RW);
		createImage(u, user_other_group, RW_RW_RW, pix);
		
		// RW_RW_RW / RW_RW_RW
		verifyDetails(img,user,user_other_group,RW_RW_RW);
		verifyDetails(pix,user,user_other_group,RW_RW_RW);
		imagePixels(u,true,true);
		imagePixels(o,true,true);
		imagePixels(w,true,true);
		imagePixels(p,true,true);
		imagePixels(r,true,true);
		
		// RW_RW_RW / RW_RW_xx
		u.getAdminService().changePermissions(pix, RW_RW_xx);
		verifyDetails(img,user,user_other_group,RW_RW_RW);
		verifyDetails(pix,user,user_other_group,RW_RW_xx);
		imagePixels(u,true,true);
		imagePixels(o,true,true);
		imagePixels(w,true,false);
		imagePixels(p,true,true);
		imagePixels(r,true,true);
		
		// RW_RW_RW / RW_xx_xx
		u.getAdminService().changePermissions(pix, RW_xx_xx);
		verifyDetails(img,user,user_other_group,RW_RW_RW);
		verifyDetails(pix,user,user_other_group,RW_xx_xx);
		imagePixels(u,true,true);
		imagePixels(o,true,false);
		imagePixels(w,true,false);
		imagePixels(p,true,true);
		imagePixels(r,true,true);
		
	}
	
	@Test
	public void test_ImagesAndDefaultPixels() throws Exception {
		fail("	see UpdateTest(server).test_experimenters_groups ");
	}
	
	protected void imagePixels(ServiceFactory sf, 
			boolean img_ok, 
			boolean pix_ok)
	{
		String outerJoin = "select i from Image i " +
				"left outer join fetch i.defaultPixels " +
				"left outer join fetch i.pixels " +
				"where i.id = :id";
		Parameters params = new Parameters().addId(img.getId());

		Image test = sf.getQueryService().findByQuery(outerJoin, params);
		if (img_ok)
		{		
			assertNotNull(test);
			if (pix_ok)
			{
				assertNotNull(test.getDefaultPixels());
				assertTrue(test.sizeOfPixels() > 0);
			} else {
				assertNull(test.getDefaultPixels());
				assertTrue(test.sizeOfPixels() == 0); // TODO should it be null?
			}
			
		} else {
			assertNull(test);
		}
		
	}
	
	// ~ Other
	// ========================================================================

	@Test
	public void testNoLoadOnNonReadableProxy() throws Exception {
		fail("A user should not be able to pass in an unreadable, unloaded" +
				" proxy and have it linked. This would allow RW to suffice" +
				" for RW*U*");
	}
	
	@Test
	public void testRootCanRemove() throws Exception {fail("implement!");}
	@Test
	public void testAttachedHasPermissionsChanged() throws Exception {fail("implement!");}
	@Test
	public void testCantChangePermissionsForMe() throws Exception {fail("implement!");}
	// tough because I don't know who's attached?!?! Perhaps GROUP_LOCKED, WORLD_LOCKED?
	// but then if a user leaves the group. (garbage collected)
	
	// ~ Helpers
	// =========================================================================
	
	protected void verifyLockStatus(IObject _i,boolean was_locked) {
		IObject v = rootQuery.get(_i.getClass(), _i.getId());
		Details d = v.getDetails();
		assertEquals( was_locked, d.getPermissions().isSet( Flag.LOCKED ));	
	}
	
	protected void verifyLocked(ServiceFactory sf, IObject _i, Details d, 
			boolean can_change) {

		// shouldn't be able to remove read
		try {
			_i.setDetails( d );
			sf.getUpdateService().saveObject( _i );
			if (!can_change) fail("secvio!");
		} catch (SecurityViolation sv) {
			if (can_change) throw sv;
		}
	}
	
	protected Details d(IObject _i, Object _o)
	{
		Details retVal = new Details( _i.getDetails() );
		if (_o instanceof Experimenter) {
			Experimenter _e = (Experimenter) _o;
			retVal.setOwner(_e);
		} else if (_o instanceof ExperimenterGroup) {
			ExperimenterGroup _g = (ExperimenterGroup) _o;
			retVal.setGroup(_g);
		} else if (_o instanceof Permissions) {
			Permissions _p = (Permissions) _o;
			retVal.setPermissions(_p);
		} else {
			throw new IllegalArgumentException("Not user/group/permissions:"+_o);
		}
		return retVal;
	}
}
