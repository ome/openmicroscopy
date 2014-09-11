/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
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

    private final String groupByOwner =
            "select g from ExperimenterGroup g " +
            "join g.groupExperimenterMap m " +
            "join m.child as e where e.id = :id and m.owner = true";

    @Test
    public void testGroupWithOwnerThroughIUpdate() throws Exception {

        loginRoot();

        String gid = uuid();
        ExperimenterGroup g = new ExperimenterGroup();
        g.setName(gid);
        g = new ExperimenterGroup(iAdmin.createGroup(g), false);

        Experimenter e = new Experimenter();
        e.setOmeName(UUID.randomUUID().toString());
        e.setFirstName("group leader");
        e.setLastName("GroupLeaderTest");
        e = iAdmin.getExperimenter(iAdmin.createUser(e, gid));

        iAdmin.setGroupOwner(g, e);

        List<ExperimenterGroup> groups = iQuery
                .findAllByQuery(groupByOwner,
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
        g = new ExperimenterGroup(iAdmin.createGroup(g), false);

        Experimenter e = new Experimenter();
        e.setOmeName(UUID.randomUUID().toString());
        e.setFirstName("group leader");
        e.setLastName("GroupLeaderTest");
        e = iAdmin.getExperimenter(iAdmin.createUser(e, gid));

        iAdmin.setGroupOwner(g, e);

        List<ExperimenterGroup> groups = iQuery
                .findAllByQuery(groupByOwner,
                        new Parameters().addId(e.getId()));

        assertNotNull(groups);
        assertTrue(groups.size() > 0);

        final Experimenter exp = e;
        List<Long> groupIds = iQuery.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                Query q = session
                        .createQuery(groupByOwner);
                q.setParameter("id", exp.getId());
                return q.list();
            }
        });

        assertNotNull(groupIds);
        assertTrue(groupIds.size() > 0);

    }

}
