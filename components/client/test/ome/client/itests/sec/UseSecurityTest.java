package ome.client.itests.sec;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

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
import ome.model.internal.Permissions.Flag;
import ome.parameters.Parameters;
import ome.system.Login;
import ome.system.ServiceFactory;
import ome.util.ShallowCopy;

import static ome.model.internal.Permissions.Right.*;
import static ome.model.internal.Permissions.Role.*;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import junit.framework.TestCase;

@Test(groups = { "ticket:200", "security", "integration" })
public class UseSecurityTest extends AbstractPermissionsTest 
{
	
	// single plays no role in USE
	
	
	// ~ one-to-many
	// =========================================================================
	
	public void test_U_Pixels_And_U_Thumbnails() throws Exception {
		ownsfA = u;
		ownerA = user;
		groupA = user_other_group;
		
		ownsfB = u;
		ownerB = user;
		groupB = user_other_group;
		
		// RW_RW_RW / RW_RW_RW
		permsA = RW_RW_RW;
		permsB = RW_RW_RW;
		oneToMany(u, true);
		oneToMany(o, true);
		oneToMany(w, true);
		oneToMany(p, true);
		oneToMany(r, true);
		
		// RW_RW_RW / RW_RW_Rx 
		permsA = RW_RW_RW;
		permsB = RW_RW_Rx;
		oneToMany(u, true);
		oneToMany(o, true);
		oneToMany(w, true);
		oneToMany(p, true);
		oneToMany(r, true);
		
		// RW_RW_RW / RW_RW_xxx 
		permsA = RW_RW_RW;
		permsB = RW_RW_xx;
		oneToMany(u, true);
		oneToMany(o, true);
		oneToMany(w, true);
		oneToMany(p, true);
		oneToMany(r, true);

		// RW_RW_RW / RW_Rx_Rx
		permsA = RW_RW_RW;
		permsB = RW_Rx_Rx;
		oneToMany(u, true);
		oneToMany(o, true);
		oneToMany(w, true);
		oneToMany(p, true);
		oneToMany(r, true);
		
		// RW_RW_RW / RW_xxx_xxx		
		permsA = RW_RW_RW;
		permsB = RW_xx_xx;
		oneToMany(u, true);
		oneToMany(o, true);
		oneToMany(w, true);
		oneToMany(p, true);
		oneToMany(r, true);

		// RW_RW_Rx / RW_RW_Rx
		permsA = RW_RW_Rx;
		permsB = RW_RW_Rx;
		oneToMany(u, true);
		oneToMany(o, true);
		oneToMany(w, false);
		oneToMany(p, true);
		oneToMany(r, true);
		
		// RW_RW_xxx / RW_RW_xxx
		permsA = RW_RW_xx;
		permsB = RW_RW_xx;
		oneToMany(u, true);
		oneToMany(o, true);
		oneToMany(w, false);
		oneToMany(p, true);
		oneToMany(r, true);

		// RW_Rx_Rx / RW_Rx_Rx
		permsA = RW_Rx_Rx;
		permsB = RW_Rx_Rx;
		oneToMany(u, true);
		oneToMany(o, false);
		oneToMany(w, false);
		oneToMany(p, true);
		oneToMany(r, true);
		
		// RW_xxx_xxx / RW_xxx_xxx
		permsA = RW_xx_xx;
		permsB = RW_xx_xx;
		oneToMany(u, true);
		oneToMany(o, false);
		oneToMany(w, false);
		oneToMany(p, true);
		oneToMany(r, true);

		// Rx_Rx_Rx / Rx_Rx_Rx
		permsA = Rx_Rx_Rx;
		permsB = Rx_Rx_Rx;
		oneToMany(u, false);
		oneToMany(o, false);
		oneToMany(w, false);
		oneToMany(p, true);
		oneToMany(r, true);
		
		// xxx_xxx_xxx / xxx_xxx_xxx
		permsA = xx_xx_xx;
		permsB = xx_xx_xx;
		oneToMany(u, false);
		oneToMany(o, false);
		oneToMany(w, false);
		oneToMany(p, true);
		oneToMany(r, true);
		
	}

	public void test_O_Pixels_And_U_Thumbnails() throws Exception {
		ownsfA = o;
		ownerA = other;
		groupA = user_other_group;
		
		ownsfB = u;
		ownerB = user;
		groupB = user_other_group;

		// RW_RW_RW / RW_RW_RW
		permsA = RW_RW_RW;
		permsB = RW_RW_RW;
		oneToMany(u, true);
		oneToMany(o, true);
		oneToMany(w, true);
		oneToMany(p, true);
		oneToMany(r, true);

		// RW_RW_RW / RW_RW_Rx 
		permsA = RW_RW_RW;
		permsB = RW_RW_Rx;
		oneToMany(u, true);
		oneToMany(o, true);
		oneToMany(w, true);
		oneToMany(p, true);
		oneToMany(r, true);
		
		// RW_RW_RW / RW_RW_xxx 
		permsA = RW_RW_RW;
		permsB = RW_RW_xx;
		oneToMany(u, false); // see NOTE below (U_pix_R_tb)
		oneToMany(o, false);
		oneToMany(w, false);
		oneToMany(p, true);
		oneToMany(r, true);
		
		// RW_RW_RW / RW_xxx_xxx
		permsA = RW_RW_RW;
		permsB = RW_xx_xx;
		oneToMany(u, true);
		oneToMany(o, true);
		oneToMany(w, true);
		oneToMany(p, true);
		oneToMany(r, true);

		// RW_RW_RW / xxx_xxx_xxx		
		permsA = RW_RW_RW;
		permsB = xx_xx_xx;
		oneToMany(u, true);
		oneToMany(o, true);
		oneToMany(w, true);
		oneToMany(p, true);
		oneToMany(r, true);
		
		// RW_xxx_xxx / RW_xxx_xxx
		permsA = RW_xx_xx;
		permsB = RW_xx_xx;
		oneToMany(u, false);
		oneToMany(o, true);
		oneToMany(w, false);
		oneToMany(p, true);
		oneToMany(r, true);

		// xxx_xxx_xxx / xxx_xxx_xxx
		permsA = xx_xx_xx;
		permsB = xx_xx_xx;
		oneToMany(u, false);
		oneToMany(o, false);
		oneToMany(w, false);
		oneToMany(p, true);
		oneToMany(r, true);
		
	}
	
	public void test_U_Pixels_And_R_Thumbnails() throws Exception {
		ownsfA = u;
		ownerA = user;
		groupA = user_other_group;
		
		ownsfB = r;
		ownerB = root;
		groupB = system_group;
		
		// RW_RW_RW / RW_RW_RW
		permsA = RW_RW_RW;
		permsB = RW_RW_RW;
		oneToMany(u, true);
		oneToMany(o, true);
		oneToMany(w, true);
		oneToMany(p, true);
		oneToMany(r, true);
		
		// RW_RW_RW / RW_RW_Rx 
		permsA = RW_RW_RW;
		permsB = RW_RW_Rx;
		oneToMany(u, true);
		oneToMany(o, true);
		oneToMany(w, true);
		oneToMany(p, true);
		oneToMany(r, true);
		
		// RW_RW_RW / RW_RW_xxx 
		permsA = RW_RW_RW;
		permsB = RW_RW_xx;
		oneToMany(u, false); // NOTE: this fails on update(pix) because
		oneToMany(o, false); // the thumbnail (detached) isn't loadable.
		oneToMany(w, false); // a similar problem to trying to filter 
		oneToMany(p, false); // many-to-ones.
		oneToMany(r, true);
		
		// RW_RW_RW / RW_xxx_xxx
		permsA = RW_RW_RW;
		permsB = RW_xx_xx;
		oneToMany(u, true);
		oneToMany(o, true);
		oneToMany(w, true);
		oneToMany(p, true);
		oneToMany(r, true);
				
		// RW_xxx_xxx / RW_xxx_xxx
		permsA = RW_xx_xx;
		permsB = RW_xx_xx;
		oneToMany(u, true);
		oneToMany(o, false);
		oneToMany(w, false);
		oneToMany(p, true);
		oneToMany(r, true);

		// xxx_xxx_xxx / xxx_xxx_xxx
		permsA = xx_xx_xx;
		permsB = xx_xx_xx;
		oneToMany(u, false);
		oneToMany(o, false);
		oneToMany(w, false);
		oneToMany(p, true);
		oneToMany(r, true);
		
	}
	
	/** first tries to change both the pixel and the thumbnail and the tries
	 * to delete them
	 */
	protected void oneToMany(ServiceFactory sf, 
			boolean link_ok)
	{
		
		createPixels(ownsfA,groupA,permsA);
		verifyDetails(pix, ownerA, groupA, permsA);

		try {
			createThumbnail(ownsfB,groupB,permsB,pix);
			if (!link_ok) fail("secvio!");
		} catch (SecurityViolation sv) {
			if (link_ok) throw sv;
		}

		// ok the linking was ok, then pixels should be locked.
		verifyDetails(tb,  ownerB,  groupB,  permsB); // and locked??
		Pixels test = tb.getPixels();
		Details test_d = test.getDetails();
		assertTrue( test.getDetails().getPermissions().isSet( Flag.LOCKED ));

		Details tmp;

		// and so userA shouldn't be able to change anything
		try {
			tmp = new Details( test_d ); 
			tmp.setPermissions( xx_xx_xx );
			test.setDetails( tmp );
			ownsfA.getUpdateService().saveObject( test );
			fail("secvio!");
		} catch (SecurityViolation sv) {
			// ok
		}
		
		// TODO group, etc...

	}
	
	// ~ many-to-one
	// =========================================================================
	public void test_U_Thumbnails_And_U_Pixels() throws Exception {
		ownsfA = ownsfB = u;
		ownerA = ownerB = user;
		groupA = groupB = user_other_group;
		
		// RW_RW_RW / RW_RW_RW
		permsA = RW_RW_RW;
		permsB = RW_RW_RW;
		manyToOne(u, true, true);
		manyToOne(o, true, true);
		manyToOne(w, true, true);
		manyToOne(p, true, true);
		manyToOne(r, true, true);
		
		// RW_RW_xxx / RW_RW_RW
		permsA = RW_RW_xx;
		permsB = RW_RW_RW;
		manyToOne(u, true, true);
		manyToOne(o, true, true);
		manyToOne(w, false, false);
		manyToOne(p, true, true);
		manyToOne(r, true, true);

		// RW_RW_RW / Rx_Rx_Rx
		permsA = RW_RW_RW;
		permsB = Rx_Rx_Rx;
		manyToOne(u, true, false);
		manyToOne(o, true, false);
		manyToOne(w, true, false);
		manyToOne(p, true, true);
		manyToOne(r, true, true);

		// RW_RW_RW / xxx_xxx_xxx
		permsA = RW_RW_RW;
		permsB = xx_xx_xx;
		manyToOne(u, false, false); // see NOTE (U_pix_R_tb)
		manyToOne(o, false, false);
		manyToOne(w, false, false);
		manyToOne(p, true, true);
		manyToOne(r, true, true);

	}
	
	/** performs various types of Omero lookups for a particular user 
	 * proxied by a {@link ServiceFactory}. If pix_ok is true, then the lookups
	 * should succeed for the top-level pixel, and if tb_ok is true, then that
	 * pixel should contain a single thumbnail.
	 */
	protected void manyToOne(ServiceFactory sf, 
			boolean tb_ok, 
			boolean pix_ok)
	{
		
		createPixels(ownsfB,groupB,permsB);
		verifyDetails(pix, ownerB, groupB, permsB);
		
		createThumbnail(ownsfA,groupA,permsA,pix);
		verifyDetails(tb,  ownerA,  groupA,  permsA);
		
		// almost identical copy of oneToMany starting here. refactor.
		
		Thumbnail test = null;
		tb.setPixels(pix);
		
		try {
			test = sf.getUpdateService().saveAndReturnObject(tb);
			if (!tb_ok) fail("secvio!");
			assertNotNull( test.getPixels() );
		} catch (SecurityViolation sv) {
			if (tb_ok) throw sv;
		}
						
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
		
		// RW_RW_RW / RW_RW_xxx / RW_RW_RW
		permsA = RW_RW_RW;
		permsB = RW_RW_xx;
		permsC = RW_RW_RW;
		manyToMany(u,true,true,true);
		manyToMany(o,true,true,true);
		manyToMany(w,true,false,true);
		manyToMany(p,true,true,true);
		manyToMany(r,true,true,true);
		
		// RW_RW_RW / RW_xxx_xxx / RW_RW_RW
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
		
		try 
		{
			sf.getUpdateService().deleteObject(link);
			if (!link_ok) fail("secvio!");
		} catch (SecurityViolation sv) { 
			if (link_ok) throw sv;
		} finally {
			prj.clearDatasetLinks(); // done for later recursive delete.
			ds.clearProjectLinks();  // ditto;
		}
		
		
		try 
		{
			deleteRecurisvely(sf, ds);
			if (!ds_ok) fail("secvio!");
		} catch (SecurityViolation sv) { 
			if (ds_ok) throw sv;
		} 
		
		try 
		{
			deleteRecurisvely( sf, prj );
			if (!prj_ok) fail("secvio!");
		} catch (SecurityViolation sv) { 
			if (prj_ok) throw sv;
		}
		
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
		
		// RW_RW_RW / RW_RW_xxx
		u.getAdminService().changePermissions(pix, RW_RW_xx);
		verifyDetails(img,user,user_other_group,RW_RW_RW);
		verifyDetails(pix,user,user_other_group,RW_RW_xx);
		imagePixels(u,true,true);
		imagePixels(o,true,true);
		imagePixels(w,true,false);
		imagePixels(p,true,true);
		imagePixels(r,true,true);
		
		// RW_RW_RW / RW_xxx_xxx
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
	
	// will need to eventually use meta-data to get the one-to-manys and 
	// many-to-ones right
	protected void deleteRecurisvely( ServiceFactory sf, IObject target )
	{
		// Deleting all links to target
		Set<String> fields = target.fields();
		for (String field : fields) {
			Object obj = target.retrieve(field);
			if (obj instanceof IObject && ! (obj instanceof IEnum)) {
				
				IObject iobj = (IObject) obj;
				//deleteRecurisvely(sf, iobj);
			} else if (obj instanceof Collection	) {
				Collection coll = (Collection) obj;
				for (Object object : coll) {
					if (object instanceof IObject) {
						IObject iobj = (IObject) object;
						deleteRecurisvely(sf, iobj);
					}
				}
			}
		}
		// Now actually delete target
		sf.getUpdateService().deleteObject(target);

	}
	
	private String makeModifiedMessage() {
		return "user can modify:"+UUID.randomUUID();
	}
}
