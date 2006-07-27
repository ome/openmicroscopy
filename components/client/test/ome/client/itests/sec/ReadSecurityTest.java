package ome.client.itests.sec;

import java.util.UUID;

import ome.api.IUpdate;
import ome.conditions.SecurityViolation;
import ome.model.acquisition.ImagingEnvironment;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.Thumbnail;
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
public class ReadSecurityTest extends AbstractSecurityTest {

	static Permissions RWU_RWU_RWU = new Permissions(),
			RWU_RWU_xxx = new Permissions()
					.revoke(WORLD, READ, WRITE, USE),
			RWU_xxx_xxx = new Permissions()
					.revoke(WORLD, READ, WRITE, USE)
					.revoke(GROUP, READ, WRITE, USE),
			xxx_xxx_xxx = new Permissions()
					.revoke(WORLD, READ, WRITE, USE)
					.revoke(GROUP, READ, WRITE, USE)
					.revoke(USER,  READ, WRITE, USE);

	/*
	 * factors 1. graph type: single, one-many, many-one, many-many (links),
	 * special* 2. ownership : top user then other, top other then user, all
	 * user, all other // NOTE: groups and world too! 3. different permissions
	 * 4. which hibernate api (load/get/createQuery/createCriteria/lazy-loading
	 * 5. second level cache, etc. template 1. create graph 2. verify ownerships
	 * 3. run HQL and check returns
	 */

	ExperimenterGroup user_other_group = new ExperimenterGroup();

	Experimenter user = new Experimenter();

	Experimenter other = new Experimenter();

	Experimenter world = new Experimenter();

	ServiceFactory u, o, w;

	@Configuration(beforeTestClass = true)
	public void createUsersAndGroups() throws Exception {
		
		init();

		user_other_group.setName(UUID.randomUUID().toString());
		user_other_group = new ExperimenterGroup(rootAdmin
				.createGroup(user_other_group), false);

		user.setOmeName(UUID.randomUUID().toString());
		user.setFirstName("read");
		user.setLastName("security");
		user = rootAdmin.getExperimenter(rootAdmin.createUser(user));
		rootAdmin.addGroups(user, user_other_group);

		Login userLogin = new Login(user.getOmeName(), "empty");
		u = new ServiceFactory(userLogin);

		other.setOmeName(UUID.randomUUID().toString());
		other.setFirstName("read");
		other.setLastName("security2");
		other = rootAdmin.getExperimenter(rootAdmin.createUser(other));
		rootAdmin.addGroups(other, user_other_group);

		Login otherLogin = new Login(other.getOmeName(), "empty");
		o = new ServiceFactory(otherLogin);

		world.setOmeName(UUID.randomUUID().toString());
		world.setFirstName("read");
		world.setLastName("Security -- not in their group");
		world = rootAdmin.getExperimenter(rootAdmin.createUser(world));
		// not in same group

		Login worldLogin = new Login(world.getOmeName(), "empty");
		w = new ServiceFactory(worldLogin);
	}

	// ~ single
	// =========================================================================

	public void testSingleProject() throws Exception {
		// create
		Project p = new Project();
		p.setName("single");
		p.getDetails().setPermissions(RWU_xxx_xxx);
		p.getDetails().setGroup(user_other_group);
		p = u.getUpdateService().saveAndReturnObject(p);

		// verify
		Project v = rootQuery.get(Project.class, p.getId());
		assertEquals(v.getDetails().getOwner().getId(), user.getId());

		// should not be readable by anyone but me.
		Project t = u.getQueryService().find(Project.class, p.getId());
		assertNotNull(t);

		try { o.getQueryService().find(Project.class, p.getId()); fail("secvio!"); } 
		catch (SecurityViolation sv) {}

		try { w.getQueryService().find(Project.class, p.getId()); fail("secvio!");} 
		catch (SecurityViolation sv) {}

		// now let's up the readability
		p.getDetails().setPermissions(RWU_RWU_xxx);
		p = u.getUpdateService().saveAndReturnObject(p);

		// test again
		t = o.getQueryService().find(Project.class, p.getId());
		assertNotNull(t);

		try { w.getQueryService().find(Project.class, p.getId()); fail("secvio!"); } 
		catch (SecurityViolation sv) {}

		// now let's up the readability one more time
		p.getDetails().setPermissions(RWU_RWU_RWU);
		p = u.getUpdateService().saveAndReturnObject(p);

		// test again
		t = o.getQueryService().find(Project.class, p.getId());
		assertNotNull(t);

		t = w.getQueryService().find(Project.class, p.getId());
		assertNotNull(t);

		// and if we make it invisible
		p.getDetails().setPermissions(xxx_xxx_xxx);
		p = u.getUpdateService().saveAndReturnObject(p);

		try {
			u.getQueryService().find(Project.class, p.getId());
			fail("secvio!");
		} catch (SecurityViolation sv) {
		}

	}

	// ~ one-to-many
	// =========================================================================
	public void testMyPixelsAndMyThumbnails() throws Exception {
		Pixels p = ObjectFactory.createPixelGraph(null);
		p.getDetails().setPermissions(RWU_RWU_RWU);
		p.getDetails().setGroup(user_other_group);

		Thumbnail t = ObjectFactory.createThumbnails(p);
		t.getDetails().setPermissions(RWU_RWU_RWU);
		p.getDetails().setGroup(user_other_group);

		t = u.getUpdateService().saveAndReturnObject(t);
		p = t.getPixels();

		// verify
		assertEquals(t.getDetails().getOwner().getId(), user.getId());
		assertEquals(p.getDetails().getOwner().getId(), user.getId());

		String outerJoin = "select p from Pixels p left outer join fetch p.thumbnails where p.id = :id";
		String innerJoin = "select p from Pixels p join fetch p.thumbnails where p.id = :id";
		Parameters params = new Parameters().addId(p.getId());

		// readable by all
		Pixels test = o.getQueryService().findByQuery(outerJoin, params);
		assertNotNull(test);
		assertTrue(test.sizeOfThumbnails() > 0);

		test = o.getQueryService().findByQuery(innerJoin, params);
		assertNotNull(test);
		assertTrue(test.sizeOfThumbnails() > 0);

		test = w.getQueryService().findByQuery(outerJoin, params);
		assertNotNull(test);
		assertTrue(test.sizeOfThumbnails() > 0);

		test = w.getQueryService().findByQuery(innerJoin, params);
		assertNotNull(test);
		assertTrue(test.sizeOfThumbnails() > 0);

		// now lets lower visibility
		u.getAdminService().changePermissions(t, RWU_RWU_xxx);

		// thumbnail readable by other but not by world
		test = o.getQueryService().findByQuery(outerJoin, params);
		assertNotNull(test);
		assertTrue(test.sizeOfThumbnails() > 0);

		test = o.getQueryService().findByQuery(innerJoin, params);
		assertNotNull(test);
		assertTrue(test.sizeOfThumbnails() > 0);

		test = w.getQueryService().findByQuery(outerJoin, params);
		assertNotNull(test);
		assertFalse(test.sizeOfThumbnails() > 0);

		test = w.getQueryService().findByQuery(innerJoin, params);
		assertNotNull(test);
		assertFalse(test.sizeOfThumbnails() > 0);

		// and one more time
		u.getAdminService().changePermissions(t, RWU_xxx_xxx);

		// neither should get the thumbnails back
		test = o.getQueryService().findByQuery(innerJoin, params);
		assertNotNull(test);
		assertFalse(test.sizeOfThumbnails() > 0);

		test = w.getQueryService().findByQuery(innerJoin, params);
		assertNotNull(test);
		assertFalse(test.sizeOfThumbnails() > 0);

		// and one more time
		u.getAdminService().changePermissions(p, RWU_xxx_xxx);

		// neither should get anything back
		test = o.getQueryService().findByQuery(innerJoin, params);
		assertNull(test);

		test = w.getQueryService().findByQuery(innerJoin, params);
		assertNull(test);

	}

	// ~ many-to-one
	// =========================================================================
	public void testMyThumbnailsAndMyPixels() throws Exception {
		Pixels p = ObjectFactory.createPixelGraph(null);
		p.getDetails().setPermissions(RWU_RWU_RWU);
		p.getDetails().setGroup(user_other_group);

		Thumbnail t = ObjectFactory.createThumbnails(p);
		t.getDetails().setPermissions(RWU_RWU_RWU);
		p.getDetails().setGroup(user_other_group);

		t = u.getUpdateService().saveAndReturnObject(t);
		p = t.getPixels();

		// verify
		assertEquals(t.getDetails().getOwner().getId(), user.getId());
		assertEquals(p.getDetails().getOwner().getId(), user.getId());

		String outerJoin = "select t from Thumbnail t left outer join fetch t.pixels where t.id = :id";
		String innerJoin = "select t from Thumbnail t join fetch t.pixels where t.id = :id";
		Parameters params = new Parameters().addId(t.getId());

		// readable by all
		Thumbnail test = o.getQueryService().findByQuery(outerJoin, params);
		assertNotNull(test);
		assertNotNull(test.getPixels());

		test = w.getQueryService().findByQuery(outerJoin, params);
		assertNotNull(test);
		assertNotNull(test.getPixels());

		// lower visibility of thumbnail
		u.getAdminService().changePermissions(t, RWU_RWU_xxx);

		test = o.getQueryService().findByQuery(outerJoin, params);
		assertNotNull(test);
		assertNotNull(test.getPixels());

		test = w.getQueryService().findByQuery(outerJoin, params);
		assertNull(test);

	}

	// ~ many-to-many
	// =========================================================================

	public void testProjectsAndDatasets() throws Exception {
		// create
		Project p = new Project();
		p.setName("links");
		p.getDetails().setPermissions(RWU_RWU_RWU);

		Dataset d = new Dataset();
		d.setName("links");
		d.getDetails().setPermissions(RWU_RWU_RWU);

		// what happens if we manipulate the link permissions?

	}

	// ~ Special: "tag" (e.g. Image/Pixels)
	// =========================================================================
}
