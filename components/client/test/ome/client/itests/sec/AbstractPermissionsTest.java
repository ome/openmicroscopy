package ome.client.itests.sec;

import java.util.UUID;

import ome.api.IUpdate;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.acquisition.ImagingEnvironment;
import ome.model.acquisition.Instrument;
import ome.model.acquisition.Microscope;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.Thumbnail;
import ome.model.enums.MicroscopeType;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.parameters.Parameters;
import ome.system.Login;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;

import static ome.model.internal.Permissions.Right.*;
import static ome.model.internal.Permissions.Role.*;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import junit.framework.TestCase;

/**
 * The subclasses of {@link AbstractPermissionsTest} define the proper working
 * and the completeness of the security system. 
 *
 */
@Test(groups = { "security", "integration" })
public abstract class AbstractPermissionsTest extends AbstractSecurityTest {

	/*
	 * factors: 
	 * -------------------------------------------------------------------------
	 * 	SEPARATE TESTS
	 * 		1. graph type: single, one-many, many-one, many-many (links),
	 * 			special* 
	 * 		2. ownership : top user then other, top other then user, all
	 * 			user, all other // NOTE: groups and world too! 
	 * 	WITHIN TEST
	 * 		3. different permissions
	 *  PER-GRAPHTYPE method (single,oneToMany,...)
	 * 		4. which hibernate api (load/get/createQuery/createCriteria/lazy-loading
	 *   TBD
	 * 		5. second level cache, etc. 
	 * 
	 * template: 
	 * -------------------------------------------------------------------------
	 * 		a. create graph 
	 * 		b. set permissions
	 * 		c. verify ownerships
	 * 		d. run queries and check returns
	 * 		e. goto b.
	 */
	
	final static protected Permissions 
			RW_RW_RW = new Permissions(),
			RW_RW_xx = new Permissions()
					.revoke(WORLD, READ, WRITE),
			RW_xx_xx = new Permissions()
					.revoke(WORLD, READ, WRITE)
					.revoke(GROUP, READ, WRITE),
			xx_xx_xx = new Permissions()
					.revoke(WORLD, READ, WRITE)
					.revoke(GROUP, READ, WRITE)
					.revoke(USER,  READ, WRITE),
			RW_RW_Rx = new Permissions()
					.revoke(WORLD, WRITE),
			RW_Rx_Rx = new Permissions()
					.revoke(WORLD, WRITE)
					.revoke(GROUP, WRITE),
			Rx_Rx_Rx = new Permissions()
					.revoke(WORLD, WRITE)
					.revoke(GROUP, WRITE)
					.revoke(USER,  WRITE),
			Rx_Rx_xx = new Permissions()
					.revoke(WORLD, READ, WRITE)
					.revoke(GROUP, WRITE)
					.revoke(USER,  WRITE),
			Rx_xx_xx = new Permissions()
					.revoke(WORLD, READ, WRITE)
					.revoke(GROUP, READ, WRITE)
					.revoke(USER,  WRITE);

	protected ExperimenterGroup 
		system_group = new ExperimenterGroup(0L,false),
		common_group = new ExperimenterGroup(1L,false),
		user_other_group = new ExperimenterGroup();
	
	protected Experimenter 
		root = new Experimenter(0L,false),
		pi   = new Experimenter(),
		user = new Experimenter(),
		other = new Experimenter(),
		world = new Experimenter();

	protected String gname;
	
	protected ServiceFactory u, o, w, p, r;

	protected Project prj;
	
	protected Dataset ds;

	protected ProjectDatasetLink link;
	
	protected Pixels pix;

	protected Thumbnail tb;

	protected Image img;
	
	protected Microscope micro;
	
	protected Instrument instr;

	protected ServiceFactory 		ownsfA,ownsfB,ownsfC;
	protected Permissions 			permsA,permsB,permsC;
	protected Experimenter 			ownerA,ownerB,ownerC;
	protected ExperimenterGroup 	groupA,groupB,groupC;

	@Configuration(beforeTestClass = true)
	public void createUsersAndGroups() throws Exception {
		
		init();
		
		gname = UUID.randomUUID().toString();

		// shortcut for root service factory, created in super class
		r = rootServices;
		
		// create the PI for a new group
		Login piLogin = new Login(UUID.randomUUID().toString(),"empty",gname,"Test");
		p = new ServiceFactory(piLogin);
		pi.setOmeName(piLogin.getName());
		pi.setFirstName("read");
		pi.setLastName("security -- leader of user_other_group");
		pi = new Experimenter(rootAdmin.createUser(pi), false);
				
		// create the new group with the PI as leader
		user_other_group.setName(gname);
		user_other_group.getDetails().setOwner(pi);
		user_other_group = new ExperimenterGroup(rootAdmin
				.createGroup(user_other_group), false);

		// also add the PI to that group
		rootAdmin.addGroups(pi, user_other_group);
		
		// create a new user in that group
		Login userLogin = new Login(UUID.randomUUID().toString(), "empty",gname,"Test");
		u = new ServiceFactory(userLogin);
		user.setOmeName(userLogin.getName());
		user.setFirstName("read");
		user.setLastName("security");
		user = new Experimenter(rootAdmin.createUser(user), false);
		rootAdmin.addGroups(user, user_other_group);

		// create another user in that group
		Login otherLogin = new Login(UUID.randomUUID().toString(), "empty",gname,"Test");
		o = new ServiceFactory(otherLogin);
		other.setOmeName(otherLogin.getName());
		other.setFirstName("read");
		other.setLastName("security2");
		other = new Experimenter(rootAdmin.createUser(other), false);
		rootAdmin.addGroups(other, user_other_group);
		
		// create a third regular user not in that group
		Login worldLogin = new Login(UUID.randomUUID().toString(), "empty" /* not gname!*/);
		w = new ServiceFactory(worldLogin);
		world.setOmeName(worldLogin.getName());
		world.setFirstName("read");
		world.setLastName("Security -- not in their group");
		world = new Experimenter(rootAdmin.createUser(world), false);
		// not in same group

	}
	
	// ~ Tests
	// =========================================================================
	// single
	public abstract void testSingleProject_U() throws Exception;
	public abstract void testSingleProject_W() throws Exception;
	public abstract void testSingleProject_R() throws Exception;
	// bidirectional one-to-many
	public abstract void test_U_Pixels_And_U_Thumbnails() throws Exception;
	public abstract void test_O_Pixels_And_U_Thumbnails() throws Exception;
	public abstract void test_U_Pixels_And_O_Thumbnails() throws Exception;
	public abstract void test_U_Pixels_And_R_Thumbnails() throws Exception;
	// unidirectional many-to-one
	public abstract void test_U_Instrument_And_U_Microscope() throws Exception;
	// many-to-many with a mapping table
	public abstract void test_U_Projects_U_Datasets_U_Link() throws Exception;
	// special
	public abstract void test_U_Image_U_Pixels() throws Exception;
	// ~ Helpers
	// ========================================================================
	
	protected void verifyDetails(IObject _i, 
			Experimenter _user,
			ExperimenterGroup _group,
			Permissions _perms) {

		IObject v = rootQuery.get(_i.getClass(), _i.getId());
		Details d = v.getDetails();
		assertEquals(d.getOwner().getId(), _user.getId());
		assertEquals(d.getGroup().getId(), _group.getId());
		assertTrue(_perms.sameRights(v.getDetails().getPermissions()));	
	}

	protected void createProject(ServiceFactory sf, Permissions perms, ExperimenterGroup group) {
		prj = new Project();
		prj.setName("single");
		prj.getDetails().setPermissions(perms);
		prj.getDetails().setGroup(group);
		prj = sf.getUpdateService().saveAndReturnObject(prj);
	}
	
	protected void createDataset(ServiceFactory sf, Permissions perms, ExperimenterGroup group) {
		ds = new Dataset();
		ds.setName("single");
		ds.getDetails().setPermissions(perms);
		ds.getDetails().setGroup(group);
		ds = sf.getUpdateService().saveAndReturnObject(ds);
	}

	protected void createPDLink(ServiceFactory sf, Permissions perms, ExperimenterGroup group) {		
		link = new ProjectDatasetLink();
		link.link(prj, ds);
		link.getDetails().setPermissions(perms);
		link.getDetails().setGroup(group);
		link = sf.getUpdateService().saveAndReturnObject(link);
		ds = link.child();
		prj = link.parent();
	}

	protected void createPixels(ServiceFactory sf, ExperimenterGroup group, Permissions perms) {
		pix = ObjectFactory.createPixelGraph(null);
		pix.getDetails().setGroup(group);
		// pix.getDetails().setPermissions(perms); must be done for whole graph
		sf.setUmask( perms );
		pix = sf.getUpdateService().saveAndReturnObject(pix);
		sf.setUmask( null );
	}

	protected void createThumbnail(ServiceFactory sf, ExperimenterGroup group, Permissions perms, Pixels _p) {
		tb = ObjectFactory.createThumbnails(_p);
		tb.getDetails().setPermissions(perms);
		tb.getDetails().setGroup(group);
		tb = sf.getUpdateService().saveAndReturnObject(tb);
	}

	protected void createImage(ServiceFactory sf, ExperimenterGroup group, Permissions perms, Pixels p) {
		img = new Image();
		img.setName("special");
		Details d = img.getDetails();
		d.setGroup(group);
		d.setPermissions(perms);
		p.setDefaultPixels(Boolean.TRUE);
		img.addPixels(p);
		img = sf.getUpdateService().saveAndReturnObject(img);
	}

	protected void createMicroscope(ServiceFactory sf, ExperimenterGroup group, Permissions perms)
	{
		MicroscopeType type = new MicroscopeType();
		type.setValue("Upright");
		micro = new Microscope();
		micro.setManufacturer("test");
		micro.setModel("model");
		micro.setSerialNumber("123456789");
		micro.setType( type );
		Details d = micro.getDetails();
		d.setGroup(group);
		d.setPermissions(perms);
		micro = sf.getUpdateService().saveAndReturnObject(micro);
	}
	
	protected void createInstrument(ServiceFactory sf, ExperimenterGroup group, Permissions perms, Microscope m)
	{
		instr = new Instrument();
		instr.setMicroscope(m);
		Details d = instr.getDetails();
		d.setGroup(group);
		d.setPermissions(perms);
		instr = sf.getUpdateService().saveAndReturnObject(instr);
	}
	
	protected String makeModifiedMessage() {
		return "user can modify:"+UUID.randomUUID();
	}
	
}
