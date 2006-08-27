package ome.client.itests.sec;

import java.util.UUID;

import ome.api.IUpdate;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.acquisition.ImagingEnvironment;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.Thumbnail;
import ome.model.meta.Experimenter;
import ome.parameters.Parameters;
import ome.system.Login;
import ome.system.ServiceFactory;

import static ome.model.internal.Permissions.Right.*;
import static ome.model.internal.Permissions.Role.*;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import junit.framework.TestCase;

@Test(groups = { "ticket:200", "security", "integration" })
public class ReadSecurityTest extends AbstractPermissionsTest 
{

	// ~ single
	// =========================================================================
	
	public void testSingleProject_U() throws Exception {
		createProject(u,RWU_xxx_xxx,user_other_group);

		// RWU_xxx_xxx : should not be readable by anyone but user.
		verifyDetails(prj,user,user_other_group,RWU_xxx_xxx);
		single(u,true);
		single(o,false);
		single(w,false);
		single(p,true);
		single(r,true);
		
		// RWU_RWU_xxx : now let's up the readability
		prj.getDetails().setPermissions(RWU_RWU_xxx);
		prj = u.getUpdateService().saveAndReturnObject(prj);
		verifyDetails(prj, user, user_other_group, RWU_RWU_xxx);
		single(u,true);
		single(o,true);
		single(w,false);
		single(p,true);
		single(r,true);
		
		
		// RWU_RWU_RWU : now let's up the readability one more time
		prj.getDetails().setPermissions(RWU_RWU_RWU);
		prj = u.getUpdateService().saveAndReturnObject(prj);
		verifyDetails(prj,user,user_other_group,RWU_RWU_RWU);
		single(u,true);
		single(o,true);
		single(w,true);
		single(p,true);
		single(r,true);
		
		// xxx_xxx_xxx : and if we make it invisible
		prj.getDetails().setPermissions(xxx_xxx_xxx);
		prj = u.getUpdateService().saveAndReturnObject(prj);
		verifyDetails(prj, user, user_other_group, xxx_xxx_xxx);
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,true);
		single(r,true);
	}
	
	// don't need to test for OTHER because OTHER and USER are symmetric.
	
	public void testSingleProject_W() throws Exception {
		createProject(w,RWU_xxx_xxx,common_group);
		
		// RWU_xxx_xxx : should not be readable by anyone but world.
		verifyDetails(prj,world,common_group,RWU_xxx_xxx);
		single(u,false);
		single(o,false);
		single(w,true);
		single(p,false);
		single(r,true);

		// RWU_RWU_xxx : now let's up the readability
		prj.getDetails().setPermissions(RWU_RWU_xxx);
		prj = w.getUpdateService().saveAndReturnObject(prj);
		verifyDetails(prj, world, common_group, RWU_RWU_xxx);
		single(u,true);
		single(o,true);
		single(w,true);
		single(p,true);
		single(r,true);

		// RWU_RWU_RWU : now let's up the readability one more time
		prj.getDetails().setPermissions(RWU_RWU_RWU);
		prj = w.getUpdateService().saveAndReturnObject(prj);
		verifyDetails(prj,world,common_group,RWU_RWU_RWU);
		single(u,true);
		single(o,true);
		single(w,true);
		single(p,true);
		single(r,true);

		// xxx_xxx_xxx : and if we make it invisible
		prj.getDetails().setPermissions(xxx_xxx_xxx);
		prj = w.getUpdateService().saveAndReturnObject(prj);
		verifyDetails(prj, world, common_group, xxx_xxx_xxx);
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,false);
		single(r,true);

	}

	// don't need to test PI because acts just like a group member
	
	public void testSingleProject_R() throws Exception {
		createProject(r,RWU_xxx_xxx,system_group);
		
		// RWU_xxx_xxx : should not be readable by anyone but world.
		verifyDetails(prj,root,system_group,RWU_xxx_xxx);
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,false);
		single(r,true);

		// RWU_RWU_xxx : now let's up the readability
		prj.getDetails().setPermissions(RWU_RWU_xxx);
		prj = r.getUpdateService().saveAndReturnObject(prj);
		verifyDetails(prj,root,system_group,RWU_RWU_xxx);
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,false);
		single(r,true);

		// RWU_RWU_RWU : now let's up the readability one more time
		prj.getDetails().setPermissions(RWU_RWU_RWU);
		prj = r.getUpdateService().saveAndReturnObject(prj);
		verifyDetails(prj,root,system_group,RWU_RWU_RWU);
		single(u,true);
		single(o,true);
		single(w,true);
		single(p,true);
		single(r,true);

		// xxx_xxx_xxx : and if we make it invisible
		prj.getDetails().setPermissions(xxx_xxx_xxx);
		prj = w.getUpdateService().saveAndReturnObject(prj);
		verifyDetails(prj,root,system_group,xxx_xxx_xxx);
		single(u,false);
		single(o,false);
		single(w,false);
		single(p,false);
		single(r,true);

	}
	
	/** performs various types of Omero lookups for a particular user 
	 * proxied by a {@link ServiceFactory}. If ok is true, then the lookups
	 * should succeed.
	 */
	protected void single(ServiceFactory sf, boolean ok)
	{
		Project t;
		try {
			t = sf.getQueryService().find(Project.class, prj.getId());
			assertNotNull(t);
		} catch (SecurityViolation e) {
			if (ok) throw e;
		}
		
		String q = "select p from Project p where p.id = :id";
		Parameters param = new Parameters().addId(prj.getId());
		
		t = sf.getQueryService().findByQuery(q, param);
		if (ok)
		{
			assertNotNull(t);
		} else {
			assertNull(t);
		}
		
	}
	
	// ~ one-to-many
	// =========================================================================
	
	public void test_U_Pixels_And_U_Thumbnails() throws Exception {
		createPixels(u,user_other_group,RWU_RWU_RWU);
		createThumbnail(u,user_other_group,RWU_RWU_RWU,pix);
		pix = tb.getPixels();

		// RWU_RWU_RWU / RWU_RWU_RWU
		verifyDetails(tb, user, user_other_group, RWU_RWU_RWU);
		verifyDetails(pix, user, user_other_group, RWU_RWU_RWU);
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, true, true);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RWU_RWU_RWU / RWU_RWU_xxx : now lets lower visibility
		// thumbnail readable by other but not by world
		u.getAdminService().changePermissions(tb, RWU_RWU_xxx);
		verifyDetails(tb, user, user_other_group, RWU_RWU_xxx);
		verifyDetails(pix, user, user_other_group, RWU_RWU_RWU);
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RWU_RWU_RWU / RWU_xxx_xxx
		u.getAdminService().changePermissions(tb, RWU_xxx_xxx);
		verifyDetails(tb, user, user_other_group, RWU_xxx_xxx);
		verifyDetails(pix, user, user_other_group, RWU_RWU_RWU);
		oneToMany(u, true, true);
		oneToMany(o, true, false);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// RWU_RWU_RWU / RWU_xxx_xxx		
		u.getAdminService().changePermissions(tb, xxx_xxx_xxx);
		verifyDetails(tb, user, user_other_group, xxx_xxx_xxx);
		verifyDetails(pix, user, user_other_group, RWU_RWU_RWU);
		oneToMany(u, true, false);
		oneToMany(o, true, false);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// RWU_RWU_xxx / RWU_RWU_xxx
		u.getAdminService().changePermissions(tb, RWU_RWU_xxx);
		u.getAdminService().changePermissions(pix, RWU_RWU_xxx);
		verifyDetails(tb, user, user_other_group, RWU_RWU_xxx);
		verifyDetails(pix, user, user_other_group, RWU_RWU_xxx);
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RWU_xxx_xxx / RWU_xxx_xxx
		u.getAdminService().changePermissions(tb, RWU_xxx_xxx);
		u.getAdminService().changePermissions(pix, RWU_xxx_xxx);
		verifyDetails(tb, user, user_other_group, RWU_xxx_xxx);
		verifyDetails(pix, user, user_other_group, RWU_xxx_xxx);
		oneToMany(u, true, true);
		oneToMany(o, false, false);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// xxx_xxx_xxx / xxx_xxx_xxx
		u.getAdminService().changePermissions(tb, xxx_xxx_xxx);
		u.getAdminService().changePermissions(pix, xxx_xxx_xxx);
		verifyDetails(tb, user, user_other_group, xxx_xxx_xxx);
		verifyDetails(pix, user, user_other_group, xxx_xxx_xxx);
		oneToMany(u, false, false);
		oneToMany(o, false, false);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
	}



	public void test_O_Pixels_And_U_Thumbnails() throws Exception {
		createPixels(o,user_other_group,RWU_RWU_RWU);
		createThumbnail(u,user_other_group,RWU_RWU_RWU, pix);
		pix = tb.getPixels();

		// RWU_RWU_RWU / RWU_RWU_RWU
		verifyDetails(tb, user, user_other_group, RWU_RWU_RWU);
		verifyDetails(pix, other, user_other_group, RWU_RWU_RWU);
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, true, true);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RWU_RWU_RWU / RWU_RWU_xxx : now lets lower visibility
		// thumbnail readable by other but not by world
		u.getAdminService().changePermissions(tb, RWU_RWU_xxx);
		verifyDetails(tb, user, user_other_group, RWU_RWU_xxx);
		verifyDetails(pix, other, user_other_group, RWU_RWU_RWU);
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RWU_RWU_RWU / RWU_xxx_xxx
		u.getAdminService().changePermissions(tb, RWU_xxx_xxx);
		verifyDetails(tb, user, user_other_group, RWU_xxx_xxx);
		verifyDetails(pix, other, user_other_group, RWU_RWU_RWU);
		oneToMany(u, true, true);
		oneToMany(o, true, false);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// RWU_RWU_RWU / xxx_xxx_xxx		
		u.getAdminService().changePermissions(tb, xxx_xxx_xxx);
		verifyDetails(tb, user, user_other_group, xxx_xxx_xxx);
		verifyDetails(pix, other, user_other_group, RWU_RWU_RWU);
		oneToMany(u, true, false);
		oneToMany(o, true, false);
		oneToMany(w, true, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RWU_xxx_xxx / RWU_xxx_xxx
		o.getAdminService().changePermissions(pix, RWU_xxx_xxx);
		u.getAdminService().changePermissions(tb, RWU_xxx_xxx);
		verifyDetails(tb, user, user_other_group, RWU_xxx_xxx);
		verifyDetails(pix, other, user_other_group, RWU_xxx_xxx);
		oneToMany(u, false, false);
		oneToMany(o, true, false);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);

		// xxx_xxx_xxx / xxx_xxx_xxx
		o.getAdminService().changePermissions(pix, xxx_xxx_xxx);
		u.getAdminService().changePermissions(tb, xxx_xxx_xxx);
		verifyDetails(tb, user, user_other_group, xxx_xxx_xxx);
		verifyDetails(pix, other, user_other_group, xxx_xxx_xxx);
		oneToMany(u, false, false);
		oneToMany(o, false, false);
		oneToMany(w, false, false);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
	}
	
	public void test_U_Pixels_And_R_Thumbnails() throws Exception {
		createPixels(u,user_other_group,RWU_RWU_RWU);
		createThumbnail(r,system_group,RWU_RWU_RWU, pix);
		pix = tb.getPixels();

		// RWU_RWU_RWU / RWU_RWU_RWU
		verifyDetails(pix, user, user_other_group, RWU_RWU_RWU);
		verifyDetails(tb,  root, system_group, RWU_RWU_RWU);
		oneToMany(u, true, true);
		oneToMany(o, true, true);
		oneToMany(w, true, true);
		oneToMany(p, true, true);
		oneToMany(r, true, true);
		
		// RWU_RWU_RWU / RWU_RWU_xxx 
		r.getAdminService().changePermissions(tb, RWU_RWU_xxx);
		verifyDetails(pix, user, user_other_group, RWU_RWU_RWU);
		verifyDetails(tb,  root, system_group, RWU_RWU_xxx);
		oneToMany(u, true, false);
		oneToMany(o, true, false);
		oneToMany(w, true, false);
		oneToMany(p, true, false);
		oneToMany(r, true, true);
		
		// RWU_RWU_RWU / RWU_xxx_xxx
		r.getAdminService().changePermissions(tb, RWU_xxx_xxx);
		verifyDetails(tb,  root, system_group, RWU_xxx_xxx);
		oneToMany(u, true, false);
		oneToMany(o, true, false);
		oneToMany(w, true, false);
		oneToMany(p, true, false);
		oneToMany(r, true, true);
				
		// RWU_xxx_xxx / RWU_xxx_xxx
		u.getAdminService().changePermissions(pix, RWU_xxx_xxx);
		r.getAdminService().changePermissions(tb, RWU_xxx_xxx);
		verifyDetails(pix, user, user_other_group, RWU_xxx_xxx);
		verifyDetails(tb,  root, system_group, RWU_xxx_xxx);
		oneToMany(u, true, false);
		oneToMany(o, false, false);
		oneToMany(w, false, false);
		oneToMany(p, true, false);
		oneToMany(r, true, true);

		// xxx_xxx_xxx / xxx_xxx_xxx
		r.getAdminService().changePermissions(tb, xxx_xxx_xxx);
		u.getAdminService().changePermissions(pix, xxx_xxx_xxx);
		verifyDetails(pix, user, user_other_group, xxx_xxx_xxx);
		verifyDetails(tb,  root, system_group, xxx_xxx_xxx);
		oneToMany(u, false, false);
		oneToMany(o, false, false);
		oneToMany(w, false, false);
		oneToMany(p, true, false);
		oneToMany(r, true, true);
		
	}
	
	/** performs various types of Omero lookups for a particular user 
	 * proxied by a {@link ServiceFactory}. If pix_ok is true, then the lookups
	 * should succeed for the top-level pixel, and if tb_ok is true, then that
	 * pixel should contain a single thumbnail.
	 */
	protected void oneToMany(ServiceFactory sf, 
			boolean pix_ok, 
			boolean tb_ok)
	{
		String outerJoin = "select p from Pixels p left outer join fetch p.thumbnails where p.id = :id";
		String innerJoin = "select p from Pixels p join fetch p.thumbnails where p.id = :id";
		Parameters params = new Parameters().addId(pix.getId());

		Pixels test = sf.getQueryService().findByQuery(outerJoin, params);
		if (pix_ok)
		{		
			assertNotNull(test);
			if (tb_ok)
			{
				assertTrue(test.sizeOfThumbnails() > 0);
			} else {
				assertTrue(test.sizeOfThumbnails() == 0); // TODO should it be null?
			}
			
		} else {
			assertNull(test);
		}
		
	}
	
	// ~ many-to-one
	// =========================================================================
	public void test_U_Thumbnails_And_U_Pixels() throws Exception {
		createPixels(u,user_other_group,RWU_RWU_RWU);
		createThumbnail(u,user_other_group,RWU_RWU_RWU,pix);
		pix = tb.getPixels();

		// RWU_RWU_RWU / RWU_RWU_RWU : readable by all
		verifyDetails(tb, user, user_other_group, RWU_RWU_RWU);
		verifyDetails(pix, user, user_other_group, RWU_RWU_RWU);
		manyToOne(u, true, true);
		manyToOne(o, true, true);
		manyToOne(w, true, true);
		manyToOne(p, true, true);
		manyToOne(r, true, true);
		
		// RWU_RWU_xxx / RWU_RWU_RWU
		// lower visibility of thumbnail
		u.getAdminService().changePermissions(tb, RWU_RWU_xxx);
		verifyDetails(tb, user, user_other_group, RWU_RWU_xxx);
		verifyDetails(pix, user, user_other_group, RWU_RWU_RWU);
		manyToOne(u, true, true);
		manyToOne(o, true, true);
		manyToOne(w, false, false);
		manyToOne(p, true, true);
		manyToOne(r, true, true);
		
		// RWU_RWU_RWU / xxx_xxx_xxx
		// try to make the pixels disappear ... 
		u.getAdminService().changePermissions(pix, xxx_xxx_xxx);
		u.getAdminService().changePermissions(tb, RWU_RWU_RWU);
		verifyDetails(pix, user, user_other_group, xxx_xxx_xxx);
		verifyDetails(tb, user, user_other_group, RWU_RWU_RWU);
		manyToOne(u, true, false);
		manyToOne(o, true, false);
		manyToOne(w, true, false);
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
		String outerJoin = "select t from Thumbnail t left outer join fetch t.pixels where t.id = :id";
		String innerJoin = "select t from Thumbnail t join fetch t.pixels where t.id = :id";
		Parameters params = new Parameters().addId(tb.getId());
		
		try 
		{
			Thumbnail test = sf.getQueryService().findByQuery(outerJoin, params);
			if (tb_ok)
			{		
				assertNotNull(test);
				if (pix_ok)
				{
					assertNotNull(test.getPixels());
				} else {
					assertNull(test.getPixels()); // TODO should it be null?
				}
				
			} else {
				assertNull(test);
			}
		} catch (SecurityViolation sv) {
			if (tb_ok && pix_ok) throw sv;
		}
		
	}

	// ~ many-to-many
	// =========================================================================

	public void test_U_Projects_And_U_Datasets() throws Exception {
		// create
		prj = new Project();
		prj.setName("links");
		prj.getDetails().setPermissions(RWU_RWU_RWU);
		prj.getDetails().setGroup(user_other_group);

		Dataset ds = new Dataset();
		ds.setName("links");
		ds.getDetails().setPermissions(RWU_RWU_RWU);
		ds.getDetails().setGroup(user_other_group);
		
		prj = u.getUpdateService().saveAndReturnObject(prj);
		ds  = u.getUpdateService().saveAndReturnObject(ds);
		ProjectDatasetLink link = new ProjectDatasetLink();
		link.link(prj, ds);
		u.getUpdateService().saveObject(link);
		
		// RWU_RWU_RWU / RWU_RWU_RWU
		verifyDetails(prj, user, user_other_group, RWU_RWU_RWU);
		verifyDetails(ds, user, user_other_group, RWU_RWU_RWU);
		manyToMany(u,true,true);
		manyToMany(o,true,true);
		manyToMany(w,true,true);
		manyToMany(p,true,true);
		manyToMany(r,true,true);
		
		// RWU_RWU_RWU / RWU_RWU_xxx
		u.getAdminService().changePermissions(ds, RWU_RWU_xxx);
		verifyDetails(prj, user, user_other_group, RWU_RWU_RWU);
		verifyDetails(ds, user, user_other_group, RWU_RWU_xxx);
		manyToMany(u,true,true);
		manyToMany(o,true,true);
		manyToMany(w,true,false);
		manyToMany(p,true,true);
		manyToMany(r,true,true);
		
		// RWU_RWU_RWU / RWU_xxx_xxx
		u.getAdminService().changePermissions(ds, RWU_xxx_xxx);
		verifyDetails(prj, user, user_other_group, RWU_RWU_RWU);
		verifyDetails(ds, user, user_other_group, RWU_xxx_xxx);
		manyToMany(u,true,true);
		manyToMany(o,true,false);
		manyToMany(w,true,false);
		manyToMany(p,true,true);
		manyToMany(r,true,true);
		
		
	}

	/** performs various types of Omero lookups for a particular user 
	 * proxied by a {@link ServiceFactory}. If prj_ok is true, then the lookups
	 * should succeed for the top-level project, and if ds_ok is true, then that
	 * project should contain a single linked dataset.
	 */
	protected void manyToMany(ServiceFactory sf, 
			boolean prj_ok, 
			boolean ds_ok)
	{
		String outerJoin = "select p from Project p " +
				" left outer join fetch p.datasetLinks pdl " +
				" left outer join fetch pdl.child ds " +
				" where p.id = :id";
		Parameters params = new Parameters().addId(prj.getId());
		
		try {
			Project test = sf.getQueryService().findByQuery(outerJoin, params);
			if (prj_ok)
			{		
				assertNotNull(test);
				if (ds_ok)
				{
					assertNotNull(test.linkedDatasetList().size() == 1);
				} else {
					assertTrue(test.linkedDatasetList().size() == 0); // TODO should it be null?
				}
				
			} else {
				assertNull(test);
			}
		} catch (SecurityViolation sv) {
			if (prj_ok && ds_ok) throw sv;
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
	
}
