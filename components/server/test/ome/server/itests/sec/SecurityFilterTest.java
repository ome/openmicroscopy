package ome.server.itests.sec;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.security.SecuritySystem;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.query.Definitions;
import ome.services.query.Query;
import ome.services.query.QueryParameterDef;
import ome.tools.hibernate.SecurityFilter;

import static ome.model.internal.Permissions.Role.*;
import static ome.model.internal.Permissions.Right.*;

@Test(groups = { "ticket:117", "security", "filter" })
public class SecurityFilterTest extends AbstractManagedContextTest {

	static String ticket117 = "ticket:117";

	Permissions userReadableOnly = new Permissions().revoke(GROUP, READ)
			.revoke(WORLD, READ);

	Permissions unreadable = new Permissions( userReadableOnly )
			.revoke(USER, READ);

	Permissions groupReadable = new Permissions().revoke(WORLD, READ);

	List<Experimenter> users = new ArrayList<Experimenter>();

    @Configuration(beforeTestClass = true)
    public void createData() throws Exception{
    	setUp();
		for (int i = 0; i < 3; i++) {
			users.add(createUser());
		}
		tearDown();
	}

	@Test
	public void testFilterDisallowsRead() throws Exception {

		Image i;

		loginUser(users.get(0).getOmeName());
		i = createImage(userReadableOnly);

		loginUser(users.get(1).getOmeName());
		assertCannotReadImage(i);

		loginUser(users.get(0).getOmeName());
		assertCanReadImage(i);
	}

	@Test
	public void testRootCanReadAll() throws Exception {

		Image i;

		loginUser(users.get(0).getOmeName());
		i = createImage(unreadable);
		assertCannotReadImage(i);

		loginUser(users.get(1).getOmeName());
		assertCannotReadImage(i);

		loginRoot();
		assertCanReadImage(i);
	}

	@Test
	public void testGroupReadable() throws Exception {

		Image i;

		loginRoot();
		ExperimenterGroup group = new ExperimenterGroup();
		group.setName(UUID.randomUUID().toString());
		group = factory.getAdminService().getGroup(
				factory.getAdminService().createGroup(group));

		ExperimenterGroup proxy = new ExperimenterGroup(group.getId(), false);
		factory.getAdminService().addGroups(users.get(0), proxy);
		factory.getAdminService().addGroups(users.get(1), proxy);

		loginUser(users.get(0).getOmeName());
		i = createImage(groupReadable);
		factory.getAdminService().changeGroup(i, group.getName());
		assertCanReadImage(i);

		loginUser(users.get(1).getOmeName());
		assertCanReadImage(i);

		loginUser(users.get(2).getOmeName());
		assertCannotReadImage(i);

	}

	@Test
	public void testGroupLeadersCanReadAllInGroup() throws Exception {
		Image i;

		loginRoot();
		// add user(2) as PI of a new group
		ExperimenterGroup group = new ExperimenterGroup();
		group.setName(UUID.randomUUID().toString());
		group.getDetails().setOwner(users.get(2));
		group = factory.getAdminService().getGroup(
				factory.getAdminService().createGroup(group));

		// add all users to that group
		ExperimenterGroup proxy = new ExperimenterGroup(group.getId(), false);
		factory.getAdminService().addGroups(users.get(0), proxy);
		factory.getAdminService().addGroups(users.get(1), proxy);
		factory.getAdminService().addGroups(users.get(2), proxy);

		// as non-PI create an image..
		loginUser(users.get(0).getOmeName());
		i = createImage(userReadableOnly);
		factory.getAdminService().changeGroup(i, group.getName());
		assertCanReadImage(i);

		// others in group can't read
		loginUser(users.get(1).getOmeName());
		assertCannotReadImage(i);

		// but PI can
		loginUser(users.get(2).getOmeName());
		assertCanReadImage(i);
	}
	
	@Test
	public void testUserCanHideFromSelf() throws Exception {
		Image i;

		// create an image with no permissions
		loginUser(users.get(0).getOmeName());
		i = createImage(unreadable);
		assertCannotReadImage(i);

	}
	
	@Test
	public void testFilterDoesntHinderOuterJoins() throws Exception {

	}

	@Test
	public void testWorldReadable() throws Exception {

	}

	@Test
	public void testStatefulServicesFollowSameContract() throws Exception {

	}

	// ~ Helpers
	// =========================================================================

	private Experimenter createUser() {
		Experimenter e2 = new Experimenter();
		e2.setOmeName(UUID.randomUUID().toString());
		e2.setFirstName("security");
		e2.setLastName("filter too");
		e2 = factory.getAdminService().getExperimenter(
				factory.getAdminService().createUser(e2));
		return e2;
	}

	private Image createImage(Permissions p) {
		Image img = new Image();
		img.setName(ticket117 + ":" + UUID.randomUUID().toString());
		img.getDetails().setPermissions(p);
		img = factory.getUpdateService().saveAndReturnObject(img);
		return img;
	}

	private void assertCannotReadImage(Image img) {
		Image image;
		
		image = getImageAsString(img);
		assertNull(image);

		image = getImageByCriteria(img);
		assertNull(image);

	}

	private void assertCanReadImage(Image img) {
		Image image;
		
		image = getImageAsString(img);
		assertNotNull(image);

		image = getImageByCriteria(img);
		assertNotNull(image);
	}

	private Image getImageAsString(Image img) {
		return iQuery.findByString(Image.class, "name", img.getName());
	}
	
	private Image getImageByCriteria(Image img) {
		return iQuery.execute(new ImageQuery(img));
	}
}

class ImageQuery extends Query<Image> {
	
	static Definitions defs = new Definitions(
			new QueryParameterDef("name",String.class,false));
	
	public ImageQuery(Image img)
	{
		super(defs,new Parameters( new Filter().unique() )
			.addString("name",img.getName()));
	}
	
	@Override
	protected void buildQuery(Session session) 
	throws HibernateException, SQLException {
		Criteria c = session.createCriteria(Image.class);
		c.add(Restrictions.eq("name", value("name")));
		setCriteria( c );
	}
}