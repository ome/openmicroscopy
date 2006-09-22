package ome.client.itests.sec;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
import ome.parameters.Parameters;
import ome.system.Login;
import ome.system.ServiceFactory;
import ome.util.ShallowCopy;

import static ome.model.internal.Permissions.Right.*;
import static ome.model.internal.Permissions.Role.*;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import junit.framework.TestCase;

@Test(groups = { "ticket:236", "security", "integration" })
public class WriteSecurityTest extends AbstractPermissionsTest
{

	/* notes:
	 * ------
	 * currently delete and write follow the same logic so both are included
	 * here. if we develop separate logic (see allow{Update|Delete} in 
	 * BasicSecuritySystem) then we'll need to separate these out.
	 * 
	 * TODO this could all be moved to an excel table!
	 */
	
	public void testSingleProject_U() throws Exception {
		ownsfA = u;
		ownerA = user;
		groupA = user_other_group;

		// RW_RW_RW
		permsA = RW_RW_RW;
		single(u,true);
		single(o,true);
		single(w,true);
		single(p,true);
		single(r,true);

		// RW_RW_Rxx
		permsA = RW_RW_Rx;
		single(u,true);
		single(o,true);
		single(w,false);
		single(p,true);
		single(r,true);
		
		// RW_RW_xx
		permsA = RW_RW_xx;
		single(u,true);
		single(o,true);
		single(w,false);
		single(p,true);
		single(r,true);
		
		// RW_Rxx_Rxx
		permsA = RW_Rx_Rx;
		single(u,true);
		single(o,false);
		single(w,false);
		single(p,true);
		single(r,true);

		// RW_xx_xx
		permsA = RW_xx_xx;
		single(u,true);
		single(o,false);
		single(w,false);
		single(p,true);
		single(r,true);

		// Rxx_Rxx_Rxx
		permsA = Rx_Rx_Rx;
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,true);
		single(r,true);
		
		// xx_xx_xx
		permsA = xx_xx_xx;
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,true);
		single(r,true);
		
	}
	
	// don't need to test for OTHER because OTHER and USER are symmetric.
	
	public void testSingleProject_W() throws Exception {
		ownsfA = w;
		ownerA = world;
		groupA = common_group;
		
		// RW_RW_RW
		permsA = RW_RW_RW;
		single(u,true);
		single(o,true);
		single(w,true);
		single(p,true);
		single(r,true);

		// RW_RW_Rxx 
		permsA = RW_RW_Rx;
		single(u,true);
		single(o,true);
		single(w,true);
		single(p,true);
		single(r,true);
		
		// RW_RW_xx 
		permsA = RW_RW_xx;
		single(u,true);
		single(o,true);
		single(w,true);
		single(p,true);
		single(r,true);

		// RW_Rxx_Rxx 
		permsA = RW_Rx_Rx;
		single(u,false);
		single(o,false);
		single(w,true);
		single(p,false);
		single(r,true);

		// RW_xx_xx 
		permsA = RW_xx_xx;
		single(u,false);
		single(o,false);
		single(w,true);
		single(p,false);
		single(r,true);

		// Rxx_Rxx_Rxx 
		permsA = Rx_Rx_Rx;
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,false);
		single(r,true);

		// xx_xx_xx 
		permsA = xx_xx_xx;
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,false);
		single(r,true);

	}

	// don't need to test PI because acts just like a group member
	
	public void testSingleProject_R() throws Exception {
		ownsfA = r;
		ownerA = root;
		groupA = system_group;
		
		// RW_RW_RW
		permsA = RW_RW_RW;
		single(u,true);
		single(o,true);
		single(w,true);
		single(p,true);
		single(r,true);

		// RW_RW_Rxx 
		permsA = RW_RW_Rx;
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,false);
		single(r,true);
		
		// RW_RW_xx 
		permsA = RW_RW_xx;
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,false);
		single(r,true);

		// Rxx_Rxx_Rxx
		permsA = Rx_Rx_Rx;
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,false);
		single(r,true);
		
		// xx_xx_xx
		permsA = xx_xx_xx;
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,false);
		single(r,true);

	}
	
	/** attempts to change the prj instance at all cost.
	 */
	protected void single(ServiceFactory sf, boolean ok)
	{
		createProject(ownsfA,permsA,groupA);
		verifyDetails(prj,ownerA,groupA,permsA);
		
		Project t = null;
		String MSG = makeModifiedMessage();
		prj.setName(MSG);

		try {
			t = sf.getUpdateService().saveAndReturnObject(prj);
			if (!ok) fail("Secvio!");
			assertTrue( MSG.equals( t.getName() ));
		} catch (SecurityViolation sv) {
			if (ok) throw sv;
		}
		
		try 
		{
			sf.getUpdateService().deleteObject(prj);
			if (!ok) fail("secvio!");
		} catch (SecurityViolation sv) { 
			if (ok) throw sv;
		}
		
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
		
		// RW_RW_RW / RW_RW_RW
		permsA = RW_RW_RW;
		permsB = RW_RW_RW;
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, true, true);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RW_RW_RW / RW_RW_Rxx 
		permsA = RW_RW_RW;
		permsB = RW_RW_Rx;
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RW_RW_RW / RW_RW_xx 
		permsA = RW_RW_RW;
		permsB = RW_RW_xx;
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// RW_RW_RW / RW_Rxx_Rxx
		permsA = RW_RW_RW;
		permsB = RW_Rx_Rx;
		oneToMany(u, true, true);
		oneToMany(o, true, false);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RW_RW_RW / RW_xx_xx		
		permsA = RW_RW_RW;
		permsB = RW_xx_xx;
		oneToMany(u, true, true);
		oneToMany(o, true, false);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// RW_RW_Rxx / RW_RW_Rxx
		permsA = RW_RW_Rx;
		permsB = RW_RW_Rx;
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RW_RW_xx / RW_RW_xx
		permsA = RW_RW_xx;
		permsB = RW_RW_xx;
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// RW_Rxx_Rxx / RW_Rxx_Rxx
		permsA = RW_Rx_Rx;
		permsB = RW_Rx_Rx;
		oneToMany(u, true, true);
		oneToMany(o, false, false);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RW_xx_xx / RW_xx_xx
		permsA = RW_xx_xx;
		permsB = RW_xx_xx;
		oneToMany(u, true, true);
		oneToMany(o, false, false);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// Rxx_Rxx_Rxx / Rxx_Rxx_Rxx
		permsA = Rx_Rx_Rx;
		permsB = Rx_Rx_Rx;
		oneToMany(u, false, false);
		oneToMany(o, false, false);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// Rx_xx_xx / xx_xx_xx
		permsA = Rx_xx_xx;
		permsB = xx_xx_xx;
		oneToMany(u, false, false);
		oneToMany(o, false, false);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
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
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, true, true);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// RW_RW_RW / RW_RW_Rxx 
		permsA = RW_RW_RW;
		permsB = RW_RW_Rx;
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RW_RW_RW / RW_RW_xx 
		permsA = RW_RW_RW;
		permsB = RW_RW_xx;
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RW_RW_RW / RW_xx_xx
		permsA = RW_RW_RW;
		permsB = RW_xx_xx;
		oneToMany(u, true, true);
		oneToMany(o, true, false);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// RW_RW_RW / xx_xx_xx		
		permsA = RW_RW_RW;
		permsB = xx_xx_xx;
		oneToMany(u, true, false);
		oneToMany(o, true, false);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RW_xx_xx / RW_xx_xx
		permsA = RW_Rx_Rx;
		permsB = RW_xx_xx;
		oneToMany(u, false, true);
		oneToMany(o, true, false);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// xx_xx_xx / xx_xx_xx
		permsA = Rx_Rx_Rx;
		permsB = xx_xx_xx;
		oneToMany(u, false, false);
		oneToMany(o, false, false);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
	}
	
	@Override
	public void test_U_Pixels_And_O_Thumbnails() throws Exception {
		ownsfA = u;
		ownerA = user;
		groupA = user_other_group;
		
		ownsfB = o;
		ownerB = other;
		groupB = user_other_group;

		// RW_RW_RW / RW_RW_RW
		permsA = RW_RW_RW;
		permsB = RW_RW_RW;
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, true, true);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// RW_RW_RW / RW_RW_Rxx 
		permsA = RW_RW_RW;
		permsB = RW_RW_Rx;
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RW_RW_RW / RW_RW_xx 
		permsA = RW_RW_RW;
		permsB = RW_RW_xx;
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RW_RW_RW / RW_xx_xx
		permsA = RW_RW_RW;
		permsB = RW_xx_xx;
		oneToMany(u, true, false);
		oneToMany(o, true, true);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// RW_RW_RW / xx_xx_xx		
		permsA = RW_RW_RW;
		permsB = xx_xx_xx;
		oneToMany(u, true, false);
		oneToMany(o, true, false);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RW_xx_xx / RW_xx_xx
		permsA = RW_Rx_Rx;
		permsB = RW_xx_xx;
		oneToMany(u, true, false);
		oneToMany(o, true, true);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// xx_xx_xx / xx_xx_xx
		permsA = Rx_Rx_Rx;
		permsB = xx_xx_xx;
		oneToMany(u, false, false);
		oneToMany(o, false, false);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
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
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, true, true);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RW_RW_RW / RW_RW_Rxx 
		permsA = RW_RW_RW;
		permsB = RW_RW_Rx;
		oneToMany(u, true, false);
		oneToMany(o, true, false);
		oneToMany(w, true, false);
		oneToMany(p, true, false);
		oneToMany(r, true, true);
		
		// RW_RW_RW / RW_RW_xx 
		permsA = RW_RW_RW;
		permsB = RW_RW_xx;
		oneToMany(u, true, false); 
		oneToMany(o, true, false); 
		oneToMany(w, true, false); 
		oneToMany(p, true, false);
		oneToMany(r, true, true);
		
		// RW_RW_RW / RW_xx_xx
		permsA = RW_RW_RW;
		permsB = RW_xx_xx;
		oneToMany(u, true, false);
		oneToMany(o, true, false);
		oneToMany(w, true, false);
		oneToMany(p, true, false);
		oneToMany(r, true, true);
				
		// RW_xx_xx / RW_xx_xx
		permsA = RW_xx_xx;
		permsB = RW_xx_xx;
		oneToMany(u, true, false);
		oneToMany(o, false, false);
		oneToMany(w, false, false);
		oneToMany(p, true, false);
		oneToMany(r, true, true);

		// xx_xx_xx / xx_xx_xx
		permsA = xx_xx_xx;
		permsB = xx_xx_xx;
		oneToMany(u, false, false);
		oneToMany(o, false, false);
		oneToMany(w, false, false);
		oneToMany(p, true, false);
		oneToMany(r, true, true);
		
	}
	
	/** first tries to change both the pixel and the thumbnail and the tries
	 * to delete them
	 */
	protected void oneToMany(ServiceFactory sf, 
			boolean pix_ok, 
			boolean tb_ok)
	{
		
		createPixels(ownsfA,groupA,permsA);
		createThumbnail(ownsfB,groupB,permsB, pix);
		pix = tb.getPixels();
		verifyDetails(pix, ownerA, groupA, permsA);
		verifyDetails(tb,  ownerB,groupB,permsB);
		
		Pixels t = null;
		Thumbnail tB = null;
		String MSG = makeModifiedMessage();
		String oldMsg = pix.getSha1();
		
		pix.setSha1(MSG);
		
		try {
			pix.putAt( Pixels.THUMBNAILS,  
					Collections.singleton( new Thumbnail( tb.getId(), false ) ));
			t = sf.getUpdateService().saveAndReturnObject(pix);
			t.addThumbnail(tb); // used to update the pixel version in tb 
			if (!pix_ok) fail("secvio!");
			assertTrue( MSG.equals( t.getSha1() ));
		} catch (SecurityViolation sv) {
			// rollback
			pix.setSha1( oldMsg );
			if (pix_ok) throw sv;
		}
		
		tb.setRef(MSG);
		
		try {
			tB = sf.getUpdateService().saveAndReturnObject(tb);
			if (!tb_ok) fail("secvio!");
			assertTrue( MSG.equals( tB.getRef() ));
		} catch (SecurityViolation sv) {
			if (tb_ok) throw sv;
		}
		
		try 
		{
			sf.getUpdateService().deleteObject(tb);
			if (!tb_ok) fail("secvio!");
			
			// done for later recursive delete.
			pix.putAt( Pixels.THUMBNAILS, null );
			// must be internal to try/catch otherwise, explodes
			// since tb still references the pix.
			deleteRecurisvely( sf, pix );
			if (!pix_ok) fail("secvio!");
			
		} catch (SecurityViolation sv) { 
			if (tb_ok&&pix_ok) throw sv;
		} 
				
	}
	
	// ~ unidirectional many-to-one
	// =========================================================================
	@Override
	public void test_U_Instrument_And_U_Microscope() throws Exception {
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
			ds.putAt( Dataset.PROJECTLINKS, null );
			testB = sf.getUpdateService().saveAndReturnObject(ds);
			if (!ds_ok) fail("secvio!");
			assertTrue( MSG.equals( testB.getName() ));
		} catch (SecurityViolation sv) {
			if (ds_ok) throw sv;
		}
		
		prj.setName(MSG);
		
		try {
			prj.putAt( Project.DATASETLINKS, null );
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
			sf.getUpdateService().deleteObject( // proxy needed
					new ProjectDatasetLink( link.getId(), false));
			if (!link_ok) fail("secvio!");
			prj.putAt( Project.DATASETLINKS, null );
			ds.putAt( Dataset.PROJECTLINKS, null );

		} catch (SecurityViolation sv) { 
			if (link_ok) throw sv;
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
		ownsfA = ownsfB = u;
		ownerA = ownerB = user;
		groupA = groupB = user_other_group;
		
		// RW_RW_RW / RW_RW_RW
		permsA = permsB = RW_RW_RW;
		imagePixels(u,true,true);
		imagePixels(o,true,true);
		imagePixels(w,true,true);
		imagePixels(p,true,true);
		imagePixels(r,true,true);
		
		// RW_RW_RW / RW_RW_xx
		permsA = RW_RW_RW;
		permsB = RW_RW_xx;
		imagePixels(u,true,true);
		imagePixels(o,true,true);
		imagePixels(w,true,false);
		imagePixels(p,true,true);
		imagePixels(r,true,true);
		
		// RW_RW_RW / RW_xx_xx
		permsA = RW_RW_RW;
		permsB = RW_xx_xx;
		imagePixels(u,true,true);
		imagePixels(o,true,false);
		imagePixels(w,true,false);
		imagePixels(p,true,true);
		imagePixels(r,true,true);
		
	}
	
	@Test
	public void test_ImagesAndDefaultPixels() throws Exception 
	{
		
		fail("	see UpdateTest(server).test_experimenters_groups ");
	}
	
	protected void imagePixels(ServiceFactory sf, 
			boolean img_ok, 
			boolean pix_ok)
	{
		createPixels(     ownsfA, groupA, permsA);
		verifyDetails(pix,ownerA, groupA, permsA);

		createImage(      ownsfB, groupB, permsB, pix);
		verifyDetails(img,ownerB, groupB ,permsB);
		
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
}
