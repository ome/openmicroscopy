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

import ome.conditions.SecurityViolation;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;
import ome.system.EventContext;
import ome.system.Principal;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.testng.annotations.Test;

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

        ExperimenterGroup g = createGroup();

        Experimenter e = createUser(g);

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

        ExperimenterGroup g = createGroup();

        Experimenter e = createUser(g);

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

    // ~ ISession.createUserSession
    // =========================================================================

    public void testGroupLeaderCanSudoInOwnGroup() throws Exception {
        loginRoot();
        ExperimenterGroup ownGroup = createGroup();
        Experimenter leader = createUser(ownGroup);
        Experimenter member = createUser(ownGroup);
        iAdmin.setGroupOwner(ownGroup, leader);
        loginUser(leader.getOmeName(), ownGroup.getName());

        Principal p = new Principal(member.getOmeName());
        iSession.createSessionWithTimeouts(p, 10000, 0);
    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void testGroupLeaderCannotSudoInOtherGroup() throws Exception {
        loginRoot();
        ExperimenterGroup ownGroup = createGroup();
        ExperimenterGroup otherGroup = createGroup();
        Experimenter leader = createUser(ownGroup);
        Experimenter member = createUser(otherGroup);
        iAdmin.setGroupOwner(ownGroup, leader);
        loginUser(leader.getOmeName(), ownGroup.getName());

        Principal p = new Principal(member.getOmeName());
        iSession.createSessionWithTimeouts(p, 10000, 0);
    }

    public void testButRootCanSudoInOtherGroup() throws Exception {
        loginRoot();
        ExperimenterGroup otherGroup = createGroup();
        Experimenter member = createUser(otherGroup);

        Principal p = new Principal(member.getOmeName());
        iSession.createSessionWithTimeouts(p, 10000, 0);
    }

    /**
     * leader creates a session for memebr in ownGroup, but then tries to
     * call setSecurityContext for anotherGroup which should fail.
     */
    @Test(expectedExceptions = SecurityViolation.class)
    public void testGroupLeaderCantChgrpWithSudo() throws Exception {
        loginRoot();
        ExperimenterGroup ownGroup = createGroup();
        ExperimenterGroup anotherGroup = createGroup();
        Experimenter leader = createUser(ownGroup);
        Experimenter member = createUser(ownGroup);
        iAdmin.setGroupOwner(ownGroup, leader);
        iAdmin.addGroups(member, anotherGroup);
        loginUser(leader.getOmeName(), ownGroup.getName());

        Principal p = new Principal(member.getOmeName());
        ome.model.meta.Session s = iSession.createSessionWithTimeouts(p, 10000, 0);
        p = login(s.getUuid(), "user", "Test");
        EventContext ec = iAdmin.getEventContext();
        assertEquals(member.getOmeName(), ec.getCurrentUserName());
        assertEquals(ownGroup.getId(), ec.getCurrentGroupId());
        sessionManager.setSecurityContext(p, anotherGroup);
    }

    // ~ Helpers
    // =========================================================================

    protected Experimenter createUser(ExperimenterGroup g) {
        Experimenter e = new Experimenter();
        e.setOmeName(UUID.randomUUID().toString());
        e.setFirstName("group leader");
        e.setLastName("GroupLeaderTest");
        e.setLdap(false);
        e = iAdmin.getExperimenter(iAdmin.createUser(e, g.getName()));
        return e;
    }

    protected ExperimenterGroup createGroup() {
        String gid = uuid();
        ExperimenterGroup g = new ExperimenterGroup();
        g.setName(gid);
        g.setLdap(false);
        return iAdmin.getGroup(iAdmin.createGroup(g));
    }

}
