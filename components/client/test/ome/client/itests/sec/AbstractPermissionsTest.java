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
import ome.model.internal.Details;
import ome.model.internal.Permissions;
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

@Test(groups = { "ticket:200", "security", "integration" })
public class AbstractPermissionsTest extends AbstractSecurityTest {

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
			RWU_RWU_RWU = new Permissions(),
			RWU_RWU_xxx = new Permissions()
					.revoke(WORLD, READ, WRITE, USE),
			RWU_xxx_xxx = new Permissions()
					.revoke(WORLD, READ, WRITE, USE)
					.revoke(GROUP, READ, WRITE, USE),
			xxx_xxx_xxx = new Permissions()
					.revoke(WORLD, READ, WRITE, USE)
					.revoke(GROUP, READ, WRITE, USE)
					.revoke(USER,  READ, WRITE, USE);

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

	protected ServiceFactory u, o, w, p, r;

	protected Project prj;
	
	protected Dataset ds;

	protected ProjectDatasetLink link;
	
	protected Pixels pix;

	protected Thumbnail tb;

	protected Image img;

	@Configuration(beforeTestClass = true)
	public void createUsersAndGroups() throws Exception {
		
		init();

		// shortcut for root service factory, created in super class
		r = rootServices;
		
		// create the PI for a new group
		Login piLogin = new Login(UUID.randomUUID().toString(),"empty");
		p = new ServiceFactory(piLogin);
		pi.setOmeName(piLogin.getName());
		pi.setFirstName("read");
		pi.setLastName("security -- leader of user_other_group");
		pi = new Experimenter(rootAdmin.createUser(pi), false);
				
		// create the new group with the PI as leader
		user_other_group.setName(UUID.randomUUID().toString());
		user_other_group.getDetails().setOwner(pi);
		user_other_group = new ExperimenterGroup(rootAdmin
				.createGroup(user_other_group), false);

		// also add the PI to that group
		rootAdmin.addGroups(pi, user_other_group);
		
		// create a new user in that group
		Login userLogin = new Login(UUID.randomUUID().toString(), "empty");
		u = new ServiceFactory(userLogin);
		user.setOmeName(userLogin.getName());
		user.setFirstName("read");
		user.setLastName("security");
		user = new Experimenter(rootAdmin.createUser(user), false);
		rootAdmin.addGroups(user, user_other_group);

		// create another user in that group
		Login otherLogin = new Login(UUID.randomUUID().toString(), "empty");
		o = new ServiceFactory(otherLogin);
		other.setOmeName(otherLogin.getName());
		other.setFirstName("read");
		other.setLastName("security2");
		other = new Experimenter(rootAdmin.createUser(other), false);
		rootAdmin.addGroups(other, user_other_group);
		
		// create a third regular user not in that group
		Login worldLogin = new Login(UUID.randomUUID().toString(), "empty");
		w = new ServiceFactory(worldLogin);
		world.setOmeName(worldLogin.getName());
		world.setFirstName("read");
		world.setLastName("Security -- not in their group");
		world = new Experimenter(rootAdmin.createUser(world), false);
		// not in same group

	}
	
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
		assertTrue(v.getDetails().getPermissions().identical(_perms));
	
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
		pix.getDetails().setPermissions(perms);
		pix.getDetails().setGroup(group);
		pix = sf.getUpdateService().saveAndReturnObject(pix);
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
	
}
