package ome.server.itests.sec;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;

import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.model.core.Image;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;

public class GroupLeaderTest extends AbstractManagedContextTest {

	// ~ IAdmin.createUser
	// =========================================================================

	@Test
	public void testGroupWithOwnerThroughIUpdate() throws Exception {

		loginRoot();

		Experimenter e = new Experimenter();
		e.setOmeName(UUID.randomUUID().toString());
		e.setFirstName("group leader");
		e.setLastName("GroupLeaderTest");
		e = iAdmin.getExperimenter(iAdmin.createUser(e));

		ExperimenterGroup g = new ExperimenterGroup();
		g.setName(UUID.randomUUID().toString());
		g.getDetails().setOwner(e);
		g = iUpdate.saveAndReturnObject(g);

		List<ExperimenterGroup> groups = iQuery
				.findAllByQuery(
						"select g from ExperimenterGroup g where g.details.owner.id = :id",
						new Parameters().addId(e.getId()));

		assertNotNull(groups);
		assertTrue(groups.size() > 0);

	}

	@Test
	public void testGroupWithOwnerThroughIAdmin() throws Exception {

		loginRoot();

		Experimenter e = new Experimenter();
		e.setOmeName(UUID.randomUUID().toString());
		e.setFirstName("group leader");
		e.setLastName("GroupLeaderTest");
		e = iAdmin.getExperimenter(iAdmin.createUser(e));

		ExperimenterGroup g = new ExperimenterGroup();
		g.setName(UUID.randomUUID().toString());
		g.getDetails().setOwner(e);
		g = iAdmin.getGroup(iAdmin.createGroup(g));

		List<ExperimenterGroup> groups = iQuery
				.findAllByQuery(
						"select g from ExperimenterGroup g where g.details.owner.id = :id",
						new Parameters().addId(e.getId()));

		assertNotNull(groups);
		assertTrue(groups.size() > 0);

		final Experimenter exp = e;
		List<Long> groupIds = iQuery.execute(new HibernateCallback(){
        	public Object doInHibernate(Session session) 
        	throws HibernateException, SQLException {
        		Query q = session.createQuery(
        		"select g.id from ExperimenterGroup g where g.details.owner.id = :id");
        		q.setParameter("id", exp.getId());
        		return q.list();
        	}
		});

		assertNotNull(groupIds);
		assertTrue(groupIds.size() > 0);

	}

}
