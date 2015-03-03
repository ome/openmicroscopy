/*
 * Copyright 2012 Glencoe Software, Inc. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.sec;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import ome.model.core.Image;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.security.SystemTypes;
import ome.security.basic.BasicACLVoter;
import ome.security.basic.BasicEventContext;
import ome.security.basic.CurrentDetails;
import ome.security.basic.TokenHolder;
import ome.security.policy.DefaultPolicyService;
import ome.services.sessions.SessionContext;
import ome.services.sessions.SessionContextImpl;
import ome.services.sessions.state.SessionCache;
import ome.services.sessions.stats.NullSessionStats;
import ome.system.Principal;

import org.jmock.MockObjectTestCase;
import org.testng.annotations.Test;

/**
 * Intended to test the "write-ability" granted to users based on the current
 * context and the object in question. These permissions should be passed
 * back via the "disallowAnnotate" and "disallowEdit" flags.
 *
 * @since 4.4.0
 * @see ticket 8277
 */
@Test(groups = { "unit", "permissions", "ticket:8277" })
public class WritePermissionsTest extends MockObjectTestCase {

    final static Long ROOT = 0L;

    final static Long THE_GROUP = 2L;

    final static Long THE_OWNER = 2L;

    final static Long GROUP_MEMBER = 3L;

    final SessionCache cache = new SessionCache();

    final CurrentDetails cd = new CurrentDetails(cache);

    final BasicACLVoter voter = new BasicACLVoter(cd, new SystemTypes(),
            new TokenHolder(), null, new DefaultPolicyService());

    protected Session login(String perms, long user, boolean leader) {

        Session s = sess(perms, user, THE_GROUP);
        SessionContext sc = context(s, leader);
        cache.putSession(s.getUuid(), sc);
        BasicEventContext bec = new BasicEventContext(new Principal(s.getUuid()),
                new NullSessionStats(), sc);
        ExperimenterGroup g = s.getDetails().getGroup();
        bec.setGroup(g, g.getDetails().getPermissions());
        bec.setOwner(s.getDetails().getOwner());
        cd.login(bec);
        return s;
    }

    protected Details objectBelongingTo(Session session, long user) {
        return objectBelongingTo(session, user,
                session.getDetails().getPermissions());
    }

    protected Details objectBelongingTo(Session session, long user, String s) {
        return objectBelongingTo(session, user, Permissions.parseString(s));
    }

    /**
     * Creates an object which is in the group given by the {@link Session}
     * object, but which belongs to the given {@link Experimenter} and has
     * the given {@link Permissions}
     *
     * @param session
     *            Session which the object was created during.
     * @param user
     *            User who owns this object.
     * @param p
     *            Permissions to set on the object details.
     */
    protected Details objectBelongingTo(Session session, long user, Permissions p) {
        Image i = new Image();
        Details d = i.getDetails();
        d.setOwner(new Experimenter(user, true));
        d.setGroup(session.getDetails().getGroup());
        d.setPermissions(p);
        voter.postProcess(i);
        return d;
    }

    // object setting differs from group
    // =========================================================================
    // Since in 4.4, it's possible for permissions settings of an object to
    // differ from those of the group, we need to make sure that post-processing
    // properly maps to the group permissions.

    public void testDifferentPerms() {
        Session s = login("rwr---", THE_OWNER, false);
        Details d = objectBelongingTo(s, THE_OWNER, "r-r-r-");
        assertCanAnnotate(d);
        assertCanEdit(d);
        assertEquals("rwr---", d.getPermissions().toString());
    }

    // rwr, non-system owner
    // =========================================================================

    class Data {
        final String name;
        final String perms;
        final Long user;
        final boolean leader;
        final Long owner;
        boolean annotate, delete, edit, link;
        Data(String name, String perms, Long user, boolean leader, Long owner,
                boolean annotate, boolean delete, boolean edit, boolean link) {
            this.name = name;
            this.perms = perms;
            this.user = user;
            this.leader = leader;
            this.owner = owner;
            this.annotate = annotate;
            this.delete = delete;
            this.edit = edit;
            this.link = link;
        }

        void run() {
            Session s = login(perms, user, leader);
            Details d = objectBelongingTo(s, owner);
            Permissions p = d.getPermissions();
            assertPerms("Annotate", annotate, !p.isDisallowAnnotate());
            assertPerms("Delete", delete, !p.isDisallowDelete());
            assertPerms("Edit", edit, !p.isDisallowEdit());
            assertPerms("Link", link, !p.isDisallowLink());
        }

        void assertPerms(String type, boolean expected, boolean found) {
            String msg = String.format("%s: allow%s broken!", name, type);
            assertEquals(msg, expected, found);
        }
    }

    final Data[] data = new Data[] {
            // rw
            new Data("rw: owner can all", "rw----", THE_OWNER, false, THE_OWNER,
                    true, true, true, true),
            new Data("rw: admin cannot link", "rw----", ROOT, false, THE_OWNER,
                    false, true, true, false),
            new Data("rw: member can do nothing", "rw----", GROUP_MEMBER, false, THE_OWNER,
                    false, false, false, false),
            new Data("rw: leader cannot link", "rw----", GROUP_MEMBER, true, THE_OWNER,
                    false, true, true, false),

            // rwr
            new Data("rwr: owner can all", "rwr---", THE_OWNER, false, THE_OWNER,
                    true, true, true, true),
            new Data("rwr: admin can all", "rwr---", ROOT, false, THE_OWNER,
                    true, true, true, true),
            new Data("rwr: member cannot all", "rwr---", GROUP_MEMBER, false, THE_OWNER,
                    false, false, false, false),
            new Data("rwr: leader can all", "rwr---", GROUP_MEMBER, true, THE_OWNER,
                    true, true, true, true)
    };

    public void testData() {
        for (Data entry : data) {
            entry.run();
        }
    }

    // Helpers
    // =========================================================================

    void assertCanAnnotate(Details d) {
        assertFalse(d.getPermissions().isDisallowAnnotate());
    }

    void assertCanEdit(Details d) {
        assertFalse(d.getPermissions().isDisallowEdit());
    }

    void assertCanLink(Details d) {
        assertFalse(d.getPermissions().isDisallowLink());
    }

    void assertCanDelete(Details d) {
        assertFalse(d.getPermissions().isDisallowDelete());
    }

    void assertCannotAnnotate(Details d) {
        assertTrue(d.getPermissions().isDisallowAnnotate());
    }

    void assertCannotEdit(Details d) {
        assertTrue(d.getPermissions().isDisallowEdit());
    }

    void assertCannotDelete(Details d) {
        assertTrue(d.getPermissions().isDisallowDelete());
    }

    void assertCannotLink(Details d) {
        assertTrue(d.getPermissions().isDisallowLink());
    }

    Session sess(String perms, long user, long group) {
        Permissions p = Permissions.parseString(perms);
        Session s = new Session();
        s.setStarted(new Timestamp(System.currentTimeMillis()));
        s.setTimeToIdle(0L);
        s.setTimeToLive(0L);
        s.setUuid(UUID.randomUUID().toString());
        s.getDetails().setPermissions(p);
        // group
        ExperimenterGroup g = new ExperimenterGroup(group, true);
        g.getDetails().setPermissions(Permissions.parseString(perms));
        s.getDetails().setGroup(g);
        // user
        Experimenter e = new Experimenter(user, true);
        s.getDetails().setOwner(e);
        return s;
    }

    SessionContext context(Session s, boolean leader) {

        final Long user = s.getDetails().getOwner().getId();
        final Long group = s.getDetails().getGroup().getId();
        List<String> roles = new ArrayList<String>();
        List<Long> memberOf = new ArrayList<Long>();
        List<Long> leaderOf = new ArrayList<Long>();

        roles.add("user");
        memberOf.add(1L);
        memberOf.add(group);

        if (user.equals(0L)) { // use "root" as proxy for "admin"
            memberOf.add(0L); // system
            roles.add("system");
        }

        if (leader) {
            leaderOf = Arrays.asList(group);
        }

        return new SessionContextImpl(s, leaderOf, memberOf, roles,
                new NullSessionStats(), null);
    }
}
