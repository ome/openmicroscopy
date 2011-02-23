/*
 *   $Id: AdminTest.java 4203 2009-04-03 13:02:36Z ola $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sec.test;

import static omero.rtypes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import omero.ServerError;
import omero.api.IAdminPrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Experimenter;
import omero.model.ExperimenterI;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.GroupExperimenterMap;
import omero.model.Permissions;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.PermissionsI;
import omero.sys.Roles;

import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.system.Login;

import org.testng.annotations.Test;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

@Test(enabled = false, groups="broken")
public class AdminTest extends AbstractAccountTest {

    // ~ chown / chgrp / chmod
    // =========================================================================

    @Test(enabled=false)
    public void testChownThroughIUpdateActuallyWorks() throws Exception {
        
        ServiceFactoryPrx u = createUser();

        // target user
        Experimenter target = createNewUser(rootAdmin);

        // new image
        Image i = new ImageI();
        i.setName(rstring("test"));
        i = (Image) u.getUpdateService().saveAndReturnObject(i);

        // change owner
        Image test = (Image) rootQuery.get("Image", i.getId().getValue());
        test.getDetails().setOwner(target);
        rootUpdate.saveObject(test);
        test = (Image) rootQuery.get("Image", i.getId().getValue());
        assertEquals(test.getDetails().getOwner().getId(), target.getId());
    }

    @Test(enabled=false, groups = "ticket:397")
    public void testChangePermissionsCantMisuseAdminAction() throws Exception {

        ServiceFactoryPrx u = createUser();

        // make an image
        Image i = new ImageI();
        i.setName(rstring("adminactiontest"));
        i = (Image) u.getUpdateService().saveAndReturnObject(i);

        // use changePerms to change the permissions
        // but try to pass in a trojan horse
        Permissions perms = new PermissionsI();
        perms.setWorldRead(true);
        perms.setWorldWrite(true);  //.grant(Role.WORLD, Right.WRITE);
        i.getDetails().setOwner(new ExperimenterI(0L, false));
        u.getAdminService().changePermissions(i, perms);
        i = (Image) u.getQueryService().get(i.getClass().getName(), i.getId().getValue());
        assertFalse(i.getDetails().getOwner().getId().equals(0L));

    }

    // ~ updating users
    // =========================================================================

    /**
     * Here the only change made was to allow all users to get the default group
     * for another user. Since this is visible anyway, there's no major concern.
     */
    @Test(enabled=false, groups = "ticket:688")
    public void testGetDefaultGroup() throws Exception {
        ServiceFactoryPrx u = createUser();
        IAdminPrx uAdmin = u.getAdminService();
        long uid = uAdmin.getEventContext().userId;
        rootAdmin.getDefaultGroup(uid);
        uAdmin.getDefaultGroup(uid);
    }

    /**
     * Setting the default group, however, is more critical. If a user is not
     * the admin, then we must be careful to not allow them to change other
     * user's groups, nor to elevate their privileges
     */
    @Test(enabled=false, groups = "ticket:688")
    public void testSetDefaultGroup() throws Exception {

        Roles roles = rootAdmin.getSecurityRoles();

        // Creating our target user and group
        ExperimenterGroup newgrp = new ExperimenterGroupI();
        List<ExperimenterGroup> newgrps = new ArrayList<ExperimenterGroup>();
        newgrps.add(newgrp);
        newgrp.setName(rstring(java.util.UUID.randomUUID().toString()));
        long gid = rootAdmin.createGroup(newgrp);
        newgrp.setId(rlong(gid));

        Experimenter user = createNewUser(rootAdmin); // in default group
        Login ul = new Login(user.getOmeName().getValue(), "");
        ServiceFactoryPrx usf = c.createSession(ul.getName(), ul.getPassword());
        IAdminPrx ua = usf.getAdminService();

        ExperimenterGroup oldgrp = rootAdmin.getDefaultGroup(user.getId().getValue());
        rootAdmin.addGroups(user, newgrps);

        // Let's make sure this still works properly
        Experimenter admin = createNewSystemUser(rootAdmin);
        Login al = new Login(admin.getOmeName().getValue(), "");
        ServiceFactoryPrx asf =c.createSession(al.getName(), al.getPassword());
        IAdminPrx aa = asf.getAdminService();
        ExperimenterGroup currgrp = aa.getDefaultGroup(user.getId().getValue());
        assertEquals(oldgrp.getName(), currgrp.getName());
        aa.setDefaultGroup(user, newgrp);

        // And now let's see what a user can do
        try {
            ExperimenterGroup sysGrp = ua.lookupGroup(roles.systemGroupName);
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

    @Test(enabled=false, groups = "ticket:688")
    public void testUpdateUser() throws Exception {

        // A new user
        ServiceFactoryPrx u = createUser();
        IAdminPrx ua = u.getAdminService();
        String name = ua.getEventContext().userName;
        Experimenter self = ua.lookupExperimenter(name);

        // A new group which the user can attempt to add
        ExperimenterGroup grp = new ExperimenterGroupI();
        grp.setName(rstring(java.util.UUID.randomUUID().toString()));
        long gid = rootAdmin.createGroup(grp);
        ExperimenterGroup grpPrx = new ExperimenterGroupI(gid, false);

        // Groups (non-changeable)
        ExperimenterGroup dfault = ua.getDefaultGroup(self.getId().getValue());
        List<ExperimenterGroup> groups = ua.containedGroups(self.getId().getValue());
        java.util.Set<Long> s = new java.util.HashSet<Long>();
        for (ExperimenterGroup g : groups) {
            s.add(g.getId().getValue());
        }

        // Fields (changeable)
        Long id;
        String on, fn, mn, ln, em, in, uuid;
        id = self.getId().getValue();
        on = self.getOmeName().getValue();
        fn = self.getFirstName().getValue();
        mn = self.getMiddleName().getValue();
        ln = self.getLastName().getValue();
        em = self.getEmail().getValue();
        in = self.getInstitution().getValue();

        uuid = java.util.UUID.randomUUID().toString();

        self.setId(rlong(-1L));
        self.setOmeName(rstring(uuid));
        self.setFirstName(rstring(uuid));
        self.setMiddleName(rstring(uuid));
        self.setLastName(rstring(uuid));
        self.setEmail(rstring(uuid));
        self.setInstitution(rstring(uuid));

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
        List<ExperimenterGroup> check = rootAdmin.containedGroups(id);
        java.util.Set<Long> s2 = new java.util.HashSet<Long>();
        for (ExperimenterGroup g : check) {
            s2.add(g.getId().getValue());
        }
        assertEquals(s.size(), s2.size());
        assertEquals(dfault.getId(), rootAdmin.getDefaultGroup(id).getId());
    }
    
    @Test(enabled=false, groups = "ticket:1104")
    public void testCreateAndUpdateUserWithPassword() throws Exception {
        Roles roles = rootAdmin.getSecurityRoles();

        ExperimenterGroup userGrp = new ExperimenterGroupI(1L, false);

        Experimenter e = new ExperimenterI();
        e.setOmeName(rstring(UUID.randomUUID().toString()));
        e.setFirstName(rstring("ticket:1104"));
        e.setLastName(rstring("ticket:1104"));
        long eid = rootAdmin.createExperimenterWithPassword(e, rstring("password"),
                userGrp, null);
        
        Login ul = new Login(UUID.randomUUID().toString(), "password");
        ServiceFactoryPrx usf = c.createSession(ul.getName(), ul.getPassword());
        usf.getAdminService().getEventContext();
        
        rootAdmin.updateExperimenterWithPassword(e, rstring("password2"));
        
        Login ul2 = new Login(UUID.randomUUID().toString(), "password2");
        ServiceFactoryPrx usf2 = c.createSession(ul2.getName(), ul2.getPassword());
        usf2.getAdminService().getEventContext();
    }

    // ~ utilities
    // =========================================================================

    private ServiceFactoryPrx createUser() {

        try
        {        
            Experimenter e = createNewUser(rootAdmin);
            Login l = new Login(e.getOmeName().getValue(), "");
            ServiceFactoryPrx u;
            u = c.createSession(l.getName(), l.getPassword());
            return u;
        } catch (CannotCreateSessionException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (PermissionDeniedException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (ServerError e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
    }

}
