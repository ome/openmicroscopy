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
import ome.model.internal.Permissions;
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

@Test(groups = { "ticket:200", "security", "integration" })
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
	
	/* these are needed because writing without reading throws a load exception
	 * rather than a write exception. */
	final static protected Permissions 
		RWU_RWU_Rxx = new Permissions()
			.revoke(WORLD, WRITE, USE),
		RWU_Rxx_Rxx = new Permissions()
			.revoke(WORLD, WRITE, USE)
			.revoke(GROUP, WRITE, USE),
		Rxx_Rxx_Rxx = new Permissions()
			.revoke(WORLD, WRITE, USE)
			.revoke(GROUP, WRITE, USE)
			.revoke(USER,  WRITE, USE);
	
	ServiceFactory 		ownsf, ownsfB, ownsfC;
	Permissions 		perms, permsB, permsC;
	Experimenter 		owner, ownerB, ownerC;
	ExperimenterGroup 	group, groupB, groupC;
	
	// ~ single
	// =========================================================================
	
	public void testSingleProject_U() throws Exception {
		ownsf = u;
		owner = user;
		group = user_other_group;

		// RWU_RWU_RWU
		perms = RWU_RWU_RWU;
		single(u,true);
		single(o,true);
		single(w,true);
		single(p,true);
		single(r,true);

		// RWU_RWU_Rxx
		perms = RWU_RWU_Rxx;
		single(u,true);
		single(o,true);
		single(w,false);
		single(p,true);
		single(r,true);
		
		// RWU_RWU_xxx
		perms = RWU_RWU_xxx;
		single(u,true);
		single(o,true);
		single(w,false);
		single(p,true);
		single(r,true);
		
		// RWU_Rxx_Rxx
		perms = RWU_Rxx_Rxx;
		single(u,true);
		single(o,false);
		single(w,false);
		single(p,true);
		single(r,true);

		// RWU_xxx_xxx
		perms = RWU_xxx_xxx;
		single(u,true);
		single(o,false);
		single(w,false);
		single(p,true);
		single(r,true);

		// Rxx_Rxx_Rxx
		perms = Rxx_Rxx_Rxx;
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,true);
		single(r,true);
		
		// xxx_xxx_xxx
		perms = xxx_xxx_xxx;
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,true);
		single(r,true);
		
	}
	
	// don't need to test for OTHER because OTHER and USER are symmetric.
	
	public void testSingleProject_W() throws Exception {
		ownsf = w;
		owner = world;
		group = common_group;
		
		// RWU_RWU_RWU
		perms = RWU_RWU_RWU;
		single(u,true);
		single(o,true);
		single(w,true);
		single(p,true);
		single(r,true);

		// RWU_RWU_Rxx 
		perms = RWU_RWU_Rxx;
		single(u,true);
		single(o,true);
		single(w,true);
		single(p,true);
		single(r,true);
		
		// RWU_RWU_xxx 
		perms = RWU_RWU_xxx;
		single(u,true);
		single(o,true);
		single(w,true);
		single(p,true);
		single(r,true);

		// RWU_Rxx_Rxx 
		perms = RWU_Rxx_Rxx;
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,false);
		single(r,true);

		// RWU_xxx_xxx 
		perms = RWU_xxx_xxx;
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,false);
		single(r,true);

		// Rxx_Rxx_Rxx 
		perms = Rxx_Rxx_Rxx;
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,false);
		single(r,true);

		// xxx_xxx_xxx 
		perms = xxx_xxx_xxx;
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,false);
		single(r,true);

	}

	// don't need to test PI because acts just like a group member
	
	public void testSingleProject_R() throws Exception {
		ownsf = r;
		owner = root;
		group = system_group;
		
		// RWU_RWU_RWU
		perms = RWU_RWU_RWU;
		single(u,true);
		single(o,true);
		single(w,true);
		single(p,true);
		single(r,true);

		// RWU_RWU_Rxx 
		perms = RWU_RWU_Rxx;
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,false);
		single(r,true);
		
		// RWU_RWU_xxx 
		perms = RWU_RWU_xxx;
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,false);
		single(r,true);

		// Rxx_Rxx_Rxx
		perms = Rxx_Rxx_Rxx;
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,false);
		single(r,true);
		
		// xxx_xxx_xxx
		perms = xxx_xxx_xxx;
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
		createProject(ownsf,perms,group);
		verifyDetails(prj,owner,group,perms);
		
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
		ownsf = u;
		owner = user;
		group = user_other_group;
		
		ownsfB = u;
		ownerB = user;
		groupB = user_other_group;
		
		// RWU_RWU_RWU / RWU_RWU_RWU
		perms = RWU_RWU_RWU;
		permsB = RWU_RWU_RWU;
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, true, true);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RWU_RWU_RWU / RWU_RWU_Rxx 
		perms = RWU_RWU_RWU;
		permsB = RWU_RWU_Rxx;
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RWU_RWU_RWU / RWU_RWU_xxx 
		perms = RWU_RWU_RWU;
		permsB = RWU_RWU_xxx;
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// RWU_RWU_RWU / RWU_Rxx_Rxx
		perms = RWU_RWU_RWU;
		permsB = RWU_Rxx_Rxx;
		oneToMany(u, true, true);
		oneToMany(o, true, false);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RWU_RWU_RWU / RWU_xxx_xxx		
		perms = RWU_RWU_RWU;
		permsB = RWU_xxx_xxx;
		oneToMany(u, true, false);
		oneToMany(o, true, false);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// RWU_RWU_Rxx / RWU_RWU_Rxx
		perms = RWU_RWU_Rxx;
		permsB = RWU_RWU_Rxx;
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RWU_RWU_xxx / RWU_RWU_xxx
		perms = RWU_RWU_xxx;
		permsB = RWU_RWU_xxx;
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// RWU_Rxx_Rxx / RWU_Rxx_Rxx
		perms = RWU_Rxx_Rxx;
		permsB = RWU_Rxx_Rxx;
		oneToMany(u, true, true);
		oneToMany(o, false, false);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RWU_xxx_xxx / RWU_xxx_xxx
		perms = RWU_xxx_xxx;
		permsB = RWU_xxx_xxx;
		oneToMany(u, true, true);
		oneToMany(o, false, false);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// Rxx_Rxx_Rxx / Rxx_Rxx_Rxx
		perms = Rxx_Rxx_Rxx;
		permsB = Rxx_Rxx_Rxx;
		oneToMany(u, false, false);
		oneToMany(o, false, false);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// xxx_xxx_xxx / xxx_xxx_xxx
		perms = xxx_xxx_xxx;
		permsB = xxx_xxx_xxx;
		oneToMany(u, false, false);
		oneToMany(o, false, false);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
	}

	public void test_O_Pixels_And_U_Thumbnails() throws Exception {
		ownsf = o;
		owner = other;
		group = user_other_group;
		
		ownsfB = u;
		ownerB = user;
		groupB = user_other_group;

		// RWU_RWU_RWU / RWU_RWU_RWU
		perms = RWU_RWU_RWU;
		permsB = RWU_RWU_RWU;
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, true, true);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// RWU_RWU_RWU / RWU_RWU_Rxx 
		perms = RWU_RWU_RWU;
		permsB = RWU_RWU_Rxx;
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RWU_RWU_RWU / RWU_RWU_xxx 
		perms = RWU_RWU_RWU;
		permsB = RWU_RWU_xxx;
		oneToMany(u, false, false); // see NOTE below (U_pix_R_tb)
		oneToMany(o, false, false);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RWU_RWU_RWU / RWU_xxx_xxx
		perms = RWU_RWU_RWU;
		permsB = RWU_xxx_xxx;
		oneToMany(u, true, true);
		oneToMany(o, true, false);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// RWU_RWU_RWU / xxx_xxx_xxx		
		perms = RWU_RWU_RWU;
		permsB = xxx_xxx_xxx;
		oneToMany(u, true, false);
		oneToMany(o, true, false);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RWU_xxx_xxx / RWU_xxx_xxx
		perms = RWU_xxx_xxx;
		permsB = RWU_xxx_xxx;
		oneToMany(u, false, false);
		oneToMany(o, true, false);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// xxx_xxx_xxx / xxx_xxx_xxx
		perms = xxx_xxx_xxx;
		permsB = xxx_xxx_xxx;
		oneToMany(u, false, false);
		oneToMany(o, false, false);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
	}
	
	public void test_U_Pixels_And_R_Thumbnails() throws Exception {
		ownsf = u;
		owner = user;
		group = user_other_group;
		
		ownsfB = r;
		ownerB = root;
		groupB = system_group;
		
		// RWU_RWU_RWU / RWU_RWU_RWU
		perms = RWU_RWU_RWU;
		permsB = RWU_RWU_RWU;
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, true, true);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RWU_RWU_RWU / RWU_RWU_Rxx 
		perms = RWU_RWU_RWU;
		permsB = RWU_RWU_Rxx;
		oneToMany(u, true, false);
		oneToMany(o, true, false);
		oneToMany(w, true, false);
		oneToMany(p, true, false);
		oneToMany(r, true, true);
		
		// RWU_RWU_RWU / RWU_RWU_xxx 
		perms = RWU_RWU_RWU;
		permsB = RWU_RWU_xxx;
		oneToMany(u, false, false); // NOTE: this fails on update(pix) because
		oneToMany(o, false, false); // the thumbnail (detached) isn't loadable.
		oneToMany(w, false, false); // a similar problem to trying to filter 
		oneToMany(p, false, false); // many-to-ones.
		oneToMany(r, true, true);
		
		// RWU_RWU_RWU / RWU_xxx_xxx
		perms = RWU_RWU_RWU;
		permsB = RWU_xxx_xxx;
		oneToMany(u, true, false);
		oneToMany(o, true, false);
		oneToMany(w, true, false);
		oneToMany(p, true, false);
		oneToMany(r, true, true);
				
		// RWU_xxx_xxx / RWU_xxx_xxx
		perms = RWU_xxx_xxx;
		permsB = RWU_xxx_xxx;
		oneToMany(u, true, false);
		oneToMany(o, false, false);
		oneToMany(w, false, false);
		oneToMany(p, true, false);
		oneToMany(r, true, true);

		// xxx_xxx_xxx / xxx_xxx_xxx
		perms = xxx_xxx_xxx;
		permsB = xxx_xxx_xxx;
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
		
		createPixels(ownsf,group,perms);
		createThumbnail(ownsfB,groupB,permsB, pix);
		pix = tb.getPixels();
		verifyDetails(pix, owner, group, perms);
		verifyDetails(tb,  ownerB,groupB,permsB);
		
		Pixels t = null;
		Thumbnail tB = null;
		String MSG = makeModifiedMessage();
		pix.setSha1(MSG);
		
		try {
			t = sf.getUpdateService().saveAndReturnObject(pix);
			if (!pix_ok) fail("secvio!");
			assertTrue( MSG.equals( t.getSha1() ));
		} catch (SecurityViolation sv) {
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
		} catch (SecurityViolation sv) { 
			if (tb_ok) throw sv;
		} finally {
			pix.clearThumbnails(); // done for later recursive delete.
		}
		
		try 
		{
			deleteRecurisvely( sf, pix );
			if (!pix_ok) fail("secvio!");
		} catch (SecurityViolation sv) { 
			if (pix_ok) throw sv;
		}
		
	}
	
	// ~ many-to-one
	// =========================================================================
	public void test_U_Thumbnails_And_U_Pixels() throws Exception {
		ownsf = ownsfB = u;
		owner = ownerB = user;
		group = groupB = user_other_group;
		
		// RWU_RWU_RWU / RWU_RWU_RWU
		perms = RWU_RWU_RWU;
		permsB = RWU_RWU_RWU;
		manyToOne(u, true, true);
		manyToOne(o, true, true);
		manyToOne(w, true, true);
		manyToOne(p, true, true);
		manyToOne(r, true, true);
		
		// RWU_RWU_xxx / RWU_RWU_RWU
		perms = RWU_RWU_xxx;
		permsB = RWU_RWU_RWU;
		manyToOne(u, true, true);
		manyToOne(o, true, true);
		manyToOne(w, false, false);
		manyToOne(p, true, true);
		manyToOne(r, true, true);

		// RWU_RWU_RWU / Rxx_Rxx_Rxx
		perms = RWU_RWU_RWU;
		permsB = Rxx_Rxx_Rxx;
		manyToOne(u, true, false);
		manyToOne(o, true, false);
		manyToOne(w, true, false);
		manyToOne(p, true, true);
		manyToOne(r, true, true);

		// RWU_RWU_RWU / xxx_xxx_xxx
		perms = RWU_RWU_RWU;
		permsB = xxx_xxx_xxx;
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
		createThumbnail(ownsf,group,perms, pix);
		pix = tb.getPixels();
		verifyDetails(pix, ownerB, groupB, permsB);
		verifyDetails(tb,  owner,  group,  perms);
		
		// almost identical copy of oneToMany starting here. refactor.
		
		Pixels testB = null;
		Thumbnail test = null;
		String MSG = makeModifiedMessage();
		pix.setSha1(MSG);
		
		try {
			testB = sf.getUpdateService().saveAndReturnObject(pix);
			if (!pix_ok) fail("secvio!");
			assertTrue( MSG.equals( testB.getSha1() ));
		} catch (SecurityViolation sv) {
			if (pix_ok) throw sv;
		}
		
		tb.setRef(MSG);
		
		try {
			test = sf.getUpdateService().saveAndReturnObject(tb);
			if (!tb_ok) fail("secvio!");
			assertTrue( MSG.equals( test.getRef() ));
		} catch (SecurityViolation sv) {
			if (tb_ok) throw sv;
		}
		
		try 
		{
			sf.getUpdateService().deleteObject(tb);
			if (!tb_ok) fail("secvio!");
		} catch (SecurityViolation sv) { 
			if (tb_ok) throw sv;
		} finally {
			pix.clearThumbnails(); // done for later recursive delete.
		}
		
		try 
		{
			deleteRecurisvely( sf, pix );
			if (!pix_ok) fail("secvio!");
		} catch (SecurityViolation sv) { 
			if (pix_ok) throw sv;
		}
						
	}

	// ~ many-to-many
	// =========================================================================

	public void test_U_Projects_U_Datasets_U_Link() throws Exception {
		ownsf = ownsfB = ownsfC = u;
		owner = ownerB = ownerC = user;
		group = groupB = groupC = user_other_group;
		
		// RWU_RWU_RWU / RWU_RWU_RWU / RWU_RWU_RWU
		perms = permsB = permsC = RWU_RWU_RWU;
		manyToMany(u,true,true,true);
		manyToMany(o,true,true,true);
		manyToMany(w,true,true,true);
		manyToMany(p,true,true,true);
		manyToMany(r,true,true,true);
		
		// RWU_RWU_RWU / RWU_RWU_xxx / RWU_RWU_RWU
		perms = RWU_RWU_RWU;
		permsB = RWU_RWU_xxx;
		permsC = RWU_RWU_RWU;
		manyToMany(u,true,true,true);
		manyToMany(o,true,true,true);
		manyToMany(w,true,false,true);
		manyToMany(p,true,true,true);
		manyToMany(r,true,true,true);
		
		// RWU_RWU_RWU / RWU_xxx_xxx / RWU_RWU_RWU
		perms = RWU_RWU_RWU;
		permsB = RWU_xxx_xxx;
		permsC = RWU_RWU_RWU;
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
		
		createProject(ownsf, perms, group);
		createDataset(ownsfB, permsB, groupB);
		createPDLink(ownsfC, permsC, groupC); 

		verifyDetails(prj, owner, group, perms);
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
		createPixels(u, user_other_group, RWU_RWU_RWU);
		createImage(u, user_other_group, RWU_RWU_RWU, pix);
		
		// RWU_RWU_RWU / RWU_RWU_RWU
		verifyDetails(img,user,user_other_group,RWU_RWU_RWU);
		verifyDetails(pix,user,user_other_group,RWU_RWU_RWU);
		imagePixels(u,true,true);
		imagePixels(o,true,true);
		imagePixels(w,true,true);
		imagePixels(p,true,true);
		imagePixels(r,true,true);
		
		// RWU_RWU_RWU / RWU_RWU_xxx
		u.getAdminService().changePermissions(pix, RWU_RWU_xxx);
		verifyDetails(img,user,user_other_group,RWU_RWU_RWU);
		verifyDetails(pix,user,user_other_group,RWU_RWU_xxx);
		imagePixels(u,true,true);
		imagePixels(o,true,true);
		imagePixels(w,true,false);
		imagePixels(p,true,true);
		imagePixels(r,true,true);
		
		// RWU_RWU_RWU / RWU_xxx_xxx
		u.getAdminService().changePermissions(pix, RWU_xxx_xxx);
		verifyDetails(img,user,user_other_group,RWU_RWU_RWU);
		verifyDetails(pix,user,user_other_group,RWU_xxx_xxx);
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
