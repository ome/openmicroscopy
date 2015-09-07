/*
 *   Copyright 2006-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.itests.sec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.server.itests.AbstractManagedContextTest;
import ome.system.Login;
import ome.system.Roles;
import ome.system.ServiceFactory;
import ome.util.IdBlock;

import org.springframework.mail.MailSender;
import org.testng.annotations.Test;

public class AdminTest extends AbstractManagedContextTest {

    @Test
    public void testGetEventContext() throws Exception {
        iAdmin.getEventContext();
    }
    
    // ~ IAdmin.createUser
    // =========================================================================

    @Test(expectedExceptions = ApiUsageException.class)
    public void testUserAccountCreationWithNull() throws Exception {
        iAdmin.createUser(null, null);
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testUserAccountCreationWithEmpty() throws Exception {
        Experimenter e = new Experimenter();
        iAdmin.createUser(e, null);
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testUserAccountCreationWithUnknownGroup() throws Exception {
        Experimenter e = new Experimenter();
        iAdmin.createUser(e, uuid()); // uuid won't exist
    }

    @Test
    public void testUserAccountCreation() throws Exception {
        ExperimenterGroup g = testGroup();
        iAdmin.createGroup(g);
        Experimenter e = testExperimenter();
        e = iAdmin.getExperimenter(iAdmin.createUser(e, g.getName()));
        assertNotNull(e.getEmail());
        assertNotNull(e.getOmeName());
        assertNotNull(e.getFirstName());
        assertNotNull(e.getLastName());
        int size = e.sizeOfGroupExperimenterMap();
        assertTrue(String.format("%d not 2", size), size == 2);
    }

    // ~ IAdmin.createSystemUser
    // =========================================================================

    @Test(expectedExceptions = ApiUsageException.class)
    public void testSysUserAccountCreationWithNull() throws Exception {
        iAdmin.createUser(null, null);
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testSysUserAccountCreationWithEmpty() throws Exception {
        Experimenter e = new Experimenter();
        iAdmin.createSystemUser(e);
    }

    @Test
    public void testSysUserAccountCreation() throws Exception {
        Experimenter e = testExperimenter();
        e = iAdmin.getExperimenter(iAdmin.createSystemUser(e));
        assertNotNull(e.getEmail());
        assertNotNull(e.getOmeName());
        assertNotNull(e.getFirstName());
        assertNotNull(e.getLastName());
        assertEquals(2, iAdmin.containedGroups(e.getId()).length);
        assertEquals(2, e.sizeOfGroupExperimenterMap());
    }

    // ~ IAdmin.createExperimenter
    // =========================================================================

    @Test(expectedExceptions = ApiUsageException.class)
    public void testExperimenterAccountCreationWithAllNulls() throws Exception {
        iAdmin.createExperimenter(null, null, (ome.model.meta.ExperimenterGroup[])null);
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testExperimenterAccountCreationWithEmpty() throws Exception {
        Experimenter e = new Experimenter();
        iAdmin.createExperimenter(e, null, (ome.model.meta.ExperimenterGroup[])null);
    }

    @Test
    public void testExperimenterAccountCreation() throws Exception {
        Experimenter e = testExperimenter();
        e = iAdmin.getExperimenter(iAdmin.createExperimenter(e,
                new ExperimenterGroup(0L, false)));
        assertNotNull(e.getEmail());
        assertNotNull(e.getOmeName());
        assertNotNull(e.getFirstName());
        assertNotNull(e.getLastName());
        assertFalse(e.getLdap());
        assertTrue(e.sizeOfGroupExperimenterMap() == 1);
    }
    
    @Test
    public void testExperimenterAccountCreationAndUpdateWithPassword() throws Exception {
        Experimenter e = testExperimenter();
        e = iAdmin.getExperimenter(iAdmin.createExperimenterWithPassword(e, "password", 
                new ExperimenterGroup(0L, false)));
        assertNotNull(e.getEmail());
        assertNotNull(e.getOmeName());
        assertNotNull(e.getFirstName());
        assertNotNull(e.getLastName());
        assertFalse(e.getLdap());
        assertTrue(e.sizeOfGroupExperimenterMap() == 1);
        
        Login ul = new Login(e.getOmeName(), "password");
        ServiceFactory usf = new ServiceFactory(ul);
        usf.getAdminService().getEventContext();
           
        iAdmin.updateExperimenterWithPassword(e, "password2");
        
        Login ul2 = new Login(e.getOmeName(), "password2");
        ServiceFactory usf2 = new ServiceFactory(ul2);
        usf2.getAdminService().getEventContext();
    }

    @Test(groups = "ticket:1021")
    public void testDefaultGroupNotAddedTwice() throws Exception {
        List<ExperimenterGroup> groups = iAdmin.lookupGroups();
        ExperimenterGroup def = null, nonDef = null;
        for (ExperimenterGroup group : groups) {
            if (group.getName().equals("user")
                    || group.getName().equals("system")) {
                continue;
            }
            if (def == null) {
                def = group;
            } else if (nonDef == null && ! def.getId().equals(group.getId())) {
                nonDef = group;
            } else {
                break;
            }
        }
        assertNotNull(def);
        assertNotNull(nonDef);
        assertFalse(def.getId().equals(nonDef.getId()));

        Experimenter e = testExperimenter();
        long id = iAdmin.createExperimenter(e, def, nonDef);
        assertEquals(2, iAdmin.containedGroups(id).length);
        e = iAdmin.lookupExperimenter(e.getOmeName());
        assertEquals("should be 2", 2, e.sizeOfGroupExperimenterMap());

        e = testExperimenter();
        id = iAdmin.createExperimenter(e, def, nonDef, def);
        e = iAdmin.lookupExperimenter(e.getOmeName());
        assertEquals("should still be 2", 2, e.sizeOfGroupExperimenterMap());

    }

    private ExperimenterGroup testGroup() {
        ExperimenterGroup g = new ExperimenterGroup();
        g.setName(uuid());
        g.setLdap(false);
        return g;
    }

    private Experimenter testExperimenter() {
        Experimenter e = new Experimenter();
        e.setEmail("blah");
        e.setFirstName("foo");
        e.setLastName("bar");
        e.setOmeName(UUID.randomUUID().toString());
        e.setLdap(false);
        return e;
    }

    // ~ Groups
    // =========================================================================
    @Test(groups = "ticket:293")
    public void testUserCanOnlySetDetailsOnOwnObject() throws Exception {

        Experimenter e1 = loginNewUser();

        Image i = new Image();
        i.setName("test");
        i = iUpdate.saveAndReturnObject(i);

        // this user should not be able to change things
        Experimenter e2 = loginNewUserInOtherUsersGroup(e1);

        try {
            iAdmin.changeOwner(i, e2.getOmeName());
            fail("secvio!");
        } catch (SecurityViolation sv) {
        }
        try {
            iAdmin.changeGroup(i, "system");
            fail("secvio!");
        } catch (SecurityViolation sv) {
        }
        try {
            iAdmin.changePermissions(i, Permissions.EMPTY);
            fail("secvio!");
        } catch (SecurityViolation sv) {
        }

        // guarantee that the client-side check for ticket:293 still holds.
        // see: TicketsUpTo500Test
        loginUser(e1.getOmeName());
        iAdmin.changePermissions(i, Permissions.EMPTY);
        // iAdmin.changePermissions(i, Permissions.DEFAULT);
        loginRoot();
        iAdmin.changePermissions(i, Permissions.EMPTY);
        // iAdmin.changePermissions(i, Permissions.DEFAULT);
        fail("review post-ticket:1434");

    }

    @Test
    public void testUserCanOnlySetDetailsToOwnGroup() throws Exception {
        ExperimenterGroup g = testGroup();
        iAdmin.createGroup(g);

        Experimenter e1 = testExperimenter();
        e1.setId(iAdmin.createUser(e1, g.getName()));

        ExperimenterGroup g1 = new ExperimenterGroup(), g2 = new ExperimenterGroup();

        g1.setName(uuid());
        g2.setName(uuid());
        g1.setLdap(false);
        g2.setLdap(false);

        g1.setId(iAdmin.createGroup(g1));
        g2.setId(iAdmin.createGroup(g2));

        login(e1.getOmeName(), g.getName(), "Test");

        Image i = new Image();
        i.setName("test");
        i = iUpdate.saveAndReturnObject(i);

        try {
            iAdmin.changeGroup(i, g2.getName());
            fail("secvio!");
        } catch (SecurityViolation sv) {
            // ok
        }

        // add the user to these groups and try again.
        iAdmin.addGroups(e1, g1, g2);

        // should now work.
        iAdmin.changeGroup(i, g2.getName());

    }

    @Test(groups = {"ticket:343", "ticket:1434"})
    public void testSetGroupOwner() throws Exception {
        ExperimenterGroup g = testGroup();
        iAdmin.createGroup(g);

        Experimenter e1 = testExperimenter();
        e1.setId(iAdmin.createUser(e1, g.getName()));

        ExperimenterGroup g1 = new ExperimenterGroup();
        g1.setName(uuid());
        g1.setLdap(false);
        g1.setId(iAdmin.createGroup(g1));

        loginRoot();

        iAdmin.setGroupOwner(g1, e1);

        ExperimenterGroup test = iQuery
                .get(ExperimenterGroup.class, g1.getId());
        boolean found = false;
        Iterator<GroupExperimenterMap> maps = test.iterateGroupExperimenterMap();
        while (maps.hasNext()) {
            GroupExperimenterMap map = maps.next();
            if (map.child().getId().equals(e1.getId())) {
                if (map.getOwner()) {
                    found = true;
                }
            }

        }
        assertTrue(found);

    }

    // ~ chgrp
    // =========================================================================
    @Test
    public void testUserUsesChgrpThroughAdmin() throws Exception {

        Experimenter e = loginNewUser();

        // a second group
        loginRoot();
        ExperimenterGroup g = new ExperimenterGroup();
        g.setName(UUID.randomUUID().toString());
        g.setLdap(false);
        g = iAdmin.getGroup(iAdmin.createGroup(g));
        iAdmin.addGroups(e, g);
        loginUser(e.getOmeName());

        // create a new image
        Image i = new Image();
        i.setName(UUID.randomUUID().toString());
        i = factory.getUpdateService().saveAndReturnObject(i);

        // it should be in some other group
        Long group = i.getDetails().getGroup().getId();
        assertFalse(group.equals(g.getId()));

        // now let's try to change that group
        factory.getAdminService().changeGroup(i, g.getName());
        Image copy = factory.getQueryService().get(Image.class, i.getId());
        Long test = copy.getDetails().getGroup().getId();

        assertFalse(test.equals(group));
        assertTrue(test.equals(g.getId()));

    }

    // ~ IAdmin.setDefaultGroup
    // =========================================================================
    @Test
    public void testSetDefaultGroup() throws Exception {
        loginRoot();

        // test group
        String gid = uuid();
        ExperimenterGroup g = new ExperimenterGroup();
        g.setName(gid);
        g.setLdap(false);
        g = iAdmin.getGroup(iAdmin.createGroup(g));

        // create a new user for the test
        Experimenter e = new Experimenter();
        e.setFirstName("user admin setters");
        e.setLastName("test");
        e.setOmeName(UUID.randomUUID().toString());
        e.setLdap(false);
        e = iAdmin.getExperimenter(iAdmin.createUser(e, gid));

        // check current default group
        ExperimenterGroup def = iAdmin.getDefaultGroup(e.getId());
        assertEquals(def.getId(), g.getId());

        // new test group
        String gid2 = uuid();
        ExperimenterGroup g2 = new ExperimenterGroup();
        g2.setName(gid2);
        g2.setLdap(false);
        g2 = iAdmin.getGroup(iAdmin.createGroup(g2));

        // now change
        iAdmin.addGroups(e, g2);
        iAdmin.setDefaultGroup(e, g2);

        // test
        def = iAdmin.getDefaultGroup(e.getId());
        assertEquals(def.getId(), g2.getId());

    }
    
    @Test(groups = "ticket:1109")
    public void testSetDefaultGroup2() throws Exception {

        Experimenter e = loginNewUser();
        
        e = assertGetDefaultGroupAndContainedExperimenters(e);
        
        // new test group
        String gid2 = uuid();
        ExperimenterGroup g2 = new ExperimenterGroup();
        g2.setName(gid2);
        g2.setLdap(false);
        g2 = iAdmin.getGroup(iAdmin.createGroup(g2));

        // now change
        iAdmin.addGroups(e, g2);
        iAdmin.setDefaultGroup(e, g2);
        assertEquals(g2.getId(), iAdmin.getDefaultGroup(e.getId()).getId());

        e = assertGetDefaultGroupAndContainedExperimenters(e);

    }

    private Experimenter assertGetDefaultGroupAndContainedExperimenters(
            Experimenter e) {
        
        ExperimenterGroup g1 = iAdmin.getDefaultGroup(e.getId());
        Experimenter[] members = iAdmin.containedExperimenters(g1.getId());
        boolean found = false;
        for (int i = 0; i < members.length; i++) {
            if (members[i].getId().longValue() == e.getId().longValue()) {
                e = members[i];
                found = true;
                break;
            }
        }
        assertTrue(found);
        assertEquals(g1.getId(), e.getGroupExperimenterMap(0).parent().getId());
        return e;
    }

    // ~ IAdmin.addGroups & .removeGroups
    // =========================================================================
    @Test
    public void testPlusAndMinusGroups() throws Exception {
        loginRoot();

        ExperimenterGroup g = testGroup();
        iAdmin.createGroup(g);

        // create a new user for the test
        Experimenter e = new Experimenter();
        e.setFirstName("user admin setters");
        e.setLastName("test");
        e.setOmeName(UUID.randomUUID().toString());
        e.setLdap(false);
        e = iAdmin.getExperimenter(iAdmin.createUser(e, g.getName()));

        int size = e.sizeOfGroupExperimenterMap();
        assertTrue(String.format("%d not 2", size), size == 2);

        // two new test groups
        ExperimenterGroup g1 = new ExperimenterGroup();
        g1.setName(UUID.randomUUID().toString());
        g1.setLdap(false);
        g1 = iAdmin.getGroup(iAdmin.createGroup(g1));
        ExperimenterGroup g2 = new ExperimenterGroup();
        g2.setName(UUID.randomUUID().toString());
        g2.setLdap(false);
        g2 = iAdmin.getGroup(iAdmin.createGroup(g2));

        iAdmin.addGroups(e, g1, g2);

        // test
        e = iAdmin.lookupExperimenter(e.getOmeName());
        assertTrue(e.linkedExperimenterGroupList().size() == 4);

        iAdmin.removeGroups(e, g1);
        e = iAdmin.lookupExperimenter(e.getOmeName());
        assertTrue(e.linkedExperimenterGroupList().size() == 3);
    }

    // ~ IAdmin.contained*
    // =========================================================================
    @Test
    public void testContainedUsersAndGroups() throws Exception {
        loginRoot();

        ExperimenterGroup g = testGroup();
        iAdmin.createGroup(g);

        // create a new user for the test
        Experimenter e = new Experimenter();
        e.setFirstName("user admin setters");
        e.setLastName("test");
        e.setOmeName(UUID.randomUUID().toString());
        e.setLdap(false);
        e = iAdmin.getExperimenter(iAdmin.createUser(e, g.getName()));

        // two new test groups
        ExperimenterGroup g1 = new ExperimenterGroup();
        g1.setName(UUID.randomUUID().toString());
        g1.setLdap(false);
        g1 = iAdmin.getGroup(iAdmin.createGroup(g1));
        ExperimenterGroup g2 = new ExperimenterGroup();
        g2.setName(UUID.randomUUID().toString());
        g2.setLdap(false);
        g2 = iAdmin.getGroup(iAdmin.createGroup(g2));

        // add them all together
        iAdmin.addGroups(e, g1, g2);

        // test
        Experimenter[] es = iAdmin.containedExperimenters(g1.getId());
        assertEquals(1, es.length);
        assertTrue(es[0].getId().equals(e.getId()));

        ExperimenterGroup[] gs = iAdmin.containedGroups(e.getId());
        assertEquals(4, gs.length);
        List<Long> ids = new ArrayList<Long>();
        for (ExperimenterGroup group : gs) {
            ids.add(group.getId());
        }
        assertTrue(ids.contains(1L));
        assertTrue(ids.contains(g1.getId()));
        assertTrue(ids.contains(g2.getId()));
    }

    // ~ IAdmin.lookup* & .get*
    // =========================================================================
    @Test
    public void testLookupAndGet() throws Exception {
        loginRoot();
        // create a new user for the test
        Experimenter e = new Experimenter();
        e.setFirstName("user admin setters");
        e.setLastName("test");
        e.setOmeName(UUID.randomUUID().toString());
        e.setLdap(false);
        e = iAdmin.getExperimenter(iAdmin.createSystemUser(e));

        loginUser(e.getOmeName());

        Experimenter test_e = iAdmin.lookupExperimenter(e.getOmeName());
        ExperimenterGroup test_g = iAdmin.getGroup(0L);

        assertTrue(test_e.linkedExperimenterGroupList().size() == 2);
        assertTrue(test_g.eachLinkedExperimenter(new IdBlock()).contains(
                e.getId()));
    }

    @Test(groups = "ticket:910")
    public void testLookupGroupsReturnsExperimentersWithGroupsLoaded() {
        loginRoot();

        List<ExperimenterGroup> list = iAdmin.lookupGroups();
        ExperimenterGroup group = list.get(0);
        Experimenter exp = group.linkedExperimenterList().get(0);
        assertNotNull(exp.getPrimaryGroupExperimenterMap());
    }

    // ~ Passwords
    // =========================================================================

    /**
     * using this test to visually inspect the log output for changeUserPassword
     * it will fail and so there should be no side-effects.
     */
    // SECURITY CHECKS AREN'T DONE FROM WITHIN. NEED TO HANDLE THIS!!!
    @Test(groups = { "ticket:209", "security", "broken" })
    public void testUnallowedPasswordChange() throws Exception {
        loginRoot();

        // and a new group
        ExperimenterGroup g = new ExperimenterGroup();
        g.setName(UUID.randomUUID().toString());
        g.setLdap(false);
        iAdmin.createGroup(g);

        // create a new user for the test
        Experimenter e = new Experimenter();
        e.setFirstName("user admin setters");
        e.setLastName("test");
        e.setOmeName(UUID.randomUUID().toString());
        e.setLdap(false);
        iAdmin.createUser(e, g.getName());

        loginUser(e.getOmeName());
        try {
            iAdmin.changeUserPassword("root", "THIS SHOULD NOT BE VISIBLE.");
            fail("secvio!");
        } catch (SecurityViolation ex) {
            // ok.
        }

    }

    // ~ Security context
    // =========================================================================

    @Test(groups = "ticket:328")
    public void testRoles() throws Exception {
        loginRoot();

        Roles r = iAdmin.getSecurityRoles();
        assertNotNull(r.getRootName());
        assertNotNull(r.getSystemGroupName());
        assertNotNull(r.getUserGroupName());
    }

    // ~ Deletion
    // =========================================================================

    public void testDeleteGroup() {
        ExperimenterGroup g = testGroup();
        long gid = iAdmin.createGroup(g);

        Experimenter e1 = testExperimenter();
        iAdmin.createUser(e1, g.getName());

        iAdmin.deleteGroup(new ExperimenterGroup(gid, false));
    }

    public void testDeleteUser() {
        ExperimenterGroup g = testGroup();
        iAdmin.createGroup(g);

        Experimenter e1 = testExperimenter();
        long uid = iAdmin.createUser(e1, g.getName());

        iAdmin.deleteExperimenter(new Experimenter(uid, false));
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testDeleteUserWithObject() {
        ExperimenterGroup g = testGroup();
        iAdmin.createGroup(g);

        Experimenter e1 = testExperimenter();
        long uid = iAdmin.createUser(e1, g.getName());

        // Now make something
        loginUser(e1.getOmeName());
        iUpdate.saveObject(new Image("name"));

        loginRoot();
        iAdmin.deleteExperimenter(new Experimenter(uid, false));
    }

    // Non private creation (#1204)
    // =========================================================================
    
    @Test(groups = "ticket:1204")
    public void testUserCreate() {
        
        Experimenter e = loginNewUser();
        // This creates all the types of interest: user, group, link

        loginRoot();

        List<ExperimenterGroup> groups = iQuery.findAll(ExperimenterGroup.class, null);
        assertWorldReadable(groups);
        groups = null;
        
        List<GroupExperimenterMap> maps = iQuery.findAll(GroupExperimenterMap.class, null);
        assertWorldReadable(maps);
        maps = null;

        List<Experimenter> users = iQuery.findAll(Experimenter.class, null);
        assertWorldReadable(users);
        users = null;
    }
    
    // ~ Bugs
    // =========================================================================

    public void testSetDefaultGroupCanNotUpateRows() throws Exception {

        Experimenter e1 = loginNewUser();
        Experimenter e2 = loginNewUser();
        loginRoot();

        ExperimenterGroup eg = iAdmin.getDefaultGroup(e1.getId());
        iAdmin.addGroups(e2, eg);
        iAdmin.setDefaultGroup(e2, eg);

    }

    public void testLookupExperimentersOnlyReturnsEachUserOnce() throws Exception {
        
        Experimenter e = loginNewUser();
        
        loginRoot();
        ExperimenterGroup g = new ExperimenterGroup(uuid(), false);
        g = new ExperimenterGroup( iAdmin.createGroup(g), false);
        iAdmin.addGroups(e, g);
        
        loginUser(e.getOmeName());
     
        Set<Long> seen = new HashSet<Long>();
        List<Experimenter> list = iAdmin.lookupExperimenters();
        for (Experimenter user : list) {
            assertFalse(String.format("Already saw %s in %s", user, list),
                    seen.contains(user.getId()));
            seen.add(user.getId());
        }
    }

    public void testReportForgottenPassword() throws Exception {

        MailSender old = null;
        try {
            old = setNoopMailSender();
            Experimenter e = loginNewUser();
            loginRoot();
            e = iQuery.get(Experimenter.class, e.getId());
            e.setEmail("test@localhost");
            iAdmin.updateExperimenter(e);

            loginUser(e.getOmeName());
            iAdmin.reportForgottenPassword(e.getOmeName(), e.getEmail());
        } finally {
            setMailSender(old);
        }
    }

}
