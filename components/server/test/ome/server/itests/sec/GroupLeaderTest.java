/*
 *   $Id$
 *
 *   Copyright 2006-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sec;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.testng.annotations.Test;

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

        String gid = uuid();
        ExperimenterGroup g = new ExperimenterGroup();
        g.setName(gid);
        g.setLdap(false);
        g = new ExperimenterGroup(iAdmin.createGroup(g), false);

        Experimenter e = new Experimenter();
        e.setOmeName(UUID.randomUUID().toString());
        e.setFirstName("group leader");
        e.setLastName("GroupLeaderTest");
        e.setLdap(false);
        e = iAdmin.getExperimenter(iAdmin.createUser(e, gid));

        iAdmin.setGroupOwner(g, e);

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

        String gid = uuid();
        ExperimenterGroup g = new ExperimenterGroup();
        g.setName(gid);
        g.setLdap(false);
        g = new ExperimenterGroup(iAdmin.createGroup(g), false);

        Experimenter e = new Experimenter();
        e.setOmeName(UUID.randomUUID().toString());
        e.setFirstName("group leader");
        e.setLastName("GroupLeaderTest");
        e.setLdap(false);
        e = iAdmin.getExperimenter(iAdmin.createUser(e, gid));

        iAdmin.setGroupOwner(g, e);

        List<ExperimenterGroup> groups = iQuery
                .findAllByQuery(
                        "select g from ExperimenterGroup g where g.details.owner.id = :id",
                        new Parameters().addId(e.getId()));

        assertNotNull(groups);
        assertTrue(groups.size() > 0);

        final Experimenter exp = e;
        List<Long> groupIds = iQuery.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                Query q = session
                        .createQuery("select g.id from ExperimenterGroup g where g.details.owner.id = :id");
                q.setParameter("id", exp.getId());
                return q.list();
            }
        });

        assertNotNull(groupIds);
        assertTrue(groupIds.size() > 0);

    }

}
