/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.client.itests.sec;

import ome.api.IAdmin;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.system.Login;
import ome.system.Roles;
import ome.system.ServiceFactory;

import org.testng.annotations.Test;

public class AdminTest extends AbstractAccountTest {

    // ~ chown / chgrp / chmod
    // =========================================================================

    @Test
    public void testChownThroughIUpdateActuallyWorks() throws Exception {
        ServiceFactory u = createUser();

        // target user
        Experimenter target = createNewUser(rootAdmin);

        // new image
        Image i = new Image();
        i.setName("test");
        i = u.getUpdateService().saveAndReturnObject(i);

        // change owner
        Image test = rootQuery.get(Image.class, i.getId());
        test.getDetails().setOwner(target);
        rootUpdate.saveObject(test);
        test = rootQuery.get(Image.class, i.getId());
        assertEquals(test.getDetails().getOwner().getId(), target.getId());
    }

    @Test(groups = "ticket:397")
    public void testChangePermissionsCantMisuseAdminAction() throws Exception {

        ServiceFactory u = createUser();

        // make an image
        Image i = new Image();
        i.setName("adminactiontest");
        i = u.getUpdateService().saveAndReturnObject(i);

        // use changePerms to change the permissions
        // but try to pass in a trojan horse
        Permissions perms = new Permissions().grant(Role.WORLD, Right.WRITE);
        i.getDetails().setOwner(new Experimenter(0L, false));
        u.getAdminService().changePermissions(i, perms);
        i = u.getQueryService().get(i.getClass(), i.getId());
        assertFalse(i.getDetails().getOwner().getId().equals(0L));

    }

    // ~ updating users
    // =========================================================================

    /**
     * Here the only change made was to allow all users to get the default group
     * for another user. Since this is visible anyway, there's no major concern.
     */
    @Test(groups = "ticket:688")
    public void testGetDefaultGroup() throws Exception {
        ServiceFactory u = createUser();
        ome.api.IAdmin uAdmin = u.getAdminService();
        long uid = uAdmin.getEventContext().getCurrentUserId();
        rootAdmin.getDefaultGroup(uid);
        uAdmin.getDefaultGroup(uid);
    }

    /**
     * Setting the default group, however, is more critical. If a user is not
     * the admin, then we must be careful to not allow them to change other
     * user's groups, nor to elevate their privileges
     */
    @Test(groups = "ticket:688")
    public void testSetDefaultGroup() throws Exception {

        Roles roles = rootAdmin.getSecurityRoles();

        // Creating our target user and group
        ExperimenterGroup newgrp = new ExperimenterGroup();
        newgrp.setName(java.util.UUID.randomUUID().toString());
        long gid = rootAdmin.createGroup(newgrp);
        newgrp.setId(gid);

        Experimenter user = createNewUser(rootAdmin); // in default group
        Login ul = new Login(user.getOmeName(), "");
        ServiceFactory usf = new ServiceFactory(ul);
        IAdmin ua = usf.getAdminService();

        ExperimenterGroup oldgrp = rootAdmin.getDefaultGroup(user.getId());
        rootAdmin.addGroups(user, newgrp);

        // Let's make sure this still works properly
        Experimenter admin = createNewSystemUser(rootAdmin);
        Login al = new Login(admin.getOmeName(), "");
        ServiceFactory asf = new ServiceFactory(al);
        IAdmin aa = asf.getAdminService();
        ExperimenterGroup currgrp = aa.getDefaultGroup(user.getId());
        assertEquals(oldgrp.getName(), currgrp.getName());
        aa.setDefaultGroup(user, newgrp);

        // And now let's see what a user can do
        try {
            ExperimenterGroup sysGrp = ua.lookupGroup(roles
                    .getSystemGroupName());
            ua.setDefaultGroup(user, sysGrp);
        } catch (ApiUsageException aue) {
            // good!
        }

        try {
            ua.setDefaultGroup(admin, newgrp);
        } catch (SecurityViolation sv) {
            // good!
        }

        // Resetting; should work.
        ua.setDefaultGroup(user, oldgrp);

    }

    @Test(groups = "ticket:688")
    public void testUpdateUser() throws Exception {

        // A new user
        ServiceFactory u = createUser();
        IAdmin ua = u.getAdminService();
        String name = ua.getEventContext().getCurrentUserName();
        Experimenter self = ua.lookupExperimenter(name);

        // A new group which the user can attempt to add
        ExperimenterGroup grp = new ExperimenterGroup();
        grp.setName(java.util.UUID.randomUUID().toString());
        long gid = rootAdmin.createGroup(grp);
        ExperimenterGroup grpPrx = new ExperimenterGroup(gid, false);

        // Groups (non-changeable)
        ExperimenterGroup dfault = ua.getDefaultGroup(self.getId());
        ExperimenterGroup groups[] = ua.containedGroups(self.getId());
        java.util.Set<Long> s = new java.util.HashSet<Long>();
        for (ExperimenterGroup g : groups) {
            s.add(g.getId());
        }

        // Fields (changeable)
        Long id;
        String on, fn, mn, ln, em, in, uuid;
        id = self.getId();
        on = self.getOmeName();
        fn = self.getFirstName();
        mn = self.getMiddleName();
        ln = self.getLastName();
        em = self.getEmail();
        in = self.getInstitution();

        uuid = java.util.UUID.randomUUID().toString();

        self.setId(-1L);
        self.setOmeName(uuid);
        self.setFirstName(uuid);
        self.setMiddleName(uuid);
        self.setLastName(uuid);
        self.setEmail(uuid);
        self.setInstitution(uuid);

        GroupExperimenterMap map = self.linkExperimenterGroup(grpPrx);
        self.setPrimaryGroupExperimenterMap(map);

        // Update and reacquire
        ua.updateSelf(self);
        self = ua.getExperimenter(id);

        // Should be changed
        assertEquals(id, self.getId());
        assertEquals(name, self.getOmeName());
        assertFalse(fn.equals(self.getFirstName()));
        assertNull(mn);
        assertNotNull(self.getMiddleName());
        assertFalse(ln.equals(self.getLastName()));
        assertNull(em);
        assertNotNull(self.getEmail());
        assertNull(in);
        assertNotNull(self.getInstitution());

        // Should not be changed
        ExperimenterGroup check[] = rootAdmin.containedGroups(id);
        java.util.Set<Long> s2 = new java.util.HashSet<Long>();
        for (ExperimenterGroup g : check) {
            s2.add(g.getId());
        }
        assertEquals(s.size(), s2.size());
        assertEquals(dfault.getId(), rootAdmin.getDefaultGroup(id).getId());
    }

    // ~ utilities
    // =========================================================================

    private ServiceFactory createUser() {
        Experimenter e = createNewUser(rootAdmin);
        Login l = new Login(e.getOmeName(), "");
        ServiceFactory u = new ServiceFactory(l);
        return u;
    }

}
