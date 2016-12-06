/*
 *   Copyright 2006-2016 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import omero.RString;
import omero.ServerError;
import omero.ValidationException;
import omero.api.IAdminPrx;
import omero.api.IQueryPrx;
import omero.api.ServiceFactoryPrx;
import omero.cmd.Request;
import omero.gateway.util.Requests;
import omero.model.AdminPrivilege;
import omero.model.AdminPrivilegeI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.GroupExperimenterMap;
import omero.model.IObject;
import omero.model.NamedValue;
import omero.model.Permissions;
import omero.model.PermissionsI;
import omero.model.enums.AdminPrivilegeModifyUser;
import omero.model.enums.AdminPrivilegeReadSession;
import omero.model.enums.AdminPrivilegeSudo;
import omero.model.enums.AdminPrivilegeWriteOwned;
import omero.sys.EventContext;
import omero.sys.ParametersI;
import omero.sys.Roles;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Sets;

import org.testng.Assert;
import org.testng.annotations.Test;

import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

/**
 * Collections of tests for the <code>IAdmin</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @author m.t.b.carroll@dundee.ac.uk
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class AdminServiceTest extends AbstractServerTest {

    /** Identifies the model object class for groups. */
    public static final String REF_GROUP = "ExperimenterGroup";

    /** The password of the user. */
    private String PASSWORD = "password";

    /** The password of the user. */
    private String PASSWORD_MODIFIED = "passwordModified";

    /**
     * Return a user's {@link Experimenter#getConfig()} property value as a multimap.
     * @param user a user
     * @return the user's configuration properties, never {@code null}
     */
    private static ImmutableMultimap<String, String> getUserConfig(Experimenter user) {
        final List<NamedValue> config = user.getConfig();
        if (config == null || config.isEmpty()) {
            return ImmutableMultimap.of();
        }
        final ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
        for (final NamedValue namedValue : config) {
            builder.put(namedValue.name, namedValue.value);
        }
        return builder.build();
    }

    /**
     * Tests the <code>lookupGroup</code> method. Retrieves a specified group.
     * Controls if the following groups exist: <code>system, user</code> and
     * converts the group into the corresponding POJO.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLookupGroup() throws Exception {
        IAdminPrx svc = root.getSession().getAdminService();
        ExperimenterGroup group = svc.lookupGroup(roles.userGroupName);
        Assert.assertNotNull(group);
        Assert.assertEquals(roles.userGroupName, group.getName().getValue());
        GroupData data = new GroupData(group);
        Assert.assertEquals(data.getId(), group.getId().getValue());
        Assert.assertEquals(data.getName(), group.getName().getValue());
        group = svc.lookupGroup(roles.systemGroupName);
        Assert.assertNotNull(group);
        Assert.assertEquals(roles.systemGroupName, group.getName().getValue());
        // Test the conversion into the corresponding POJO
        data = new GroupData(group);
        Assert.assertEquals(data.getId(), group.getId().getValue());
        Assert.assertEquals(data.getName(), group.getName().getValue());
    }

    /**
     * Tests the <code>lookupExperimenter</code> method. Retrieves an
     * experimenter and converts the group into the corresponding POJO.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLookupExperimenter() throws Exception {
        IAdminPrx svc = root.getSession().getAdminService();
        Experimenter exp = svc.lookupExperimenter("root");
        Assert.assertNotNull(exp);

        // Test the conversion into the corresponding POJO
        ExperimenterData data = new ExperimenterData(exp);
        Assert.assertEquals(data.getId(), exp.getId().getValue());
        Assert.assertEquals(data.getUserName(), exp.getOmeName().getValue());
    }

    /**
     * Tests the <code>lookupGroups</code> method. This method should return 2
     * or more groups.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLookupGroups() throws Exception {
        IAdminPrx svc = root.getSession().getAdminService();
        List<ExperimenterGroup> groups = svc.lookupGroups();
        Assert.assertNotNull(groups);
        Assert.assertTrue(groups.size() >= 2);
    }

    /**
     * Tests the creation of a group with permission <code>rw----</code>.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateGroupRW() throws Exception {
        String uuid = UUID.randomUUID().toString();
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rw----"));
        IAdminPrx svc = root.getSession().getAdminService();
        long id = svc.createGroup(g);
        // Load the group
        IQueryPrx query = root.getSession().getQueryService();
        ParametersI p = new ParametersI();
        p.addId(id);
        ExperimenterGroup eg = (ExperimenterGroup) query.findByQuery(
                "select distinct g from ExperimenterGroup g where g.id = :id",
                p);
        Assert.assertNotNull(eg);
        Assert.assertEquals(eg.getName().getValue(), uuid);
        // test permissions
        Permissions perms = eg.getDetails().getPermissions();
        Assert.assertTrue(perms.isUserRead());
        Assert.assertTrue(perms.isUserWrite());
        Assert.assertFalse(perms.isGroupRead());
        Assert.assertFalse(perms.isGroupWrite());
        Assert.assertFalse(perms.isWorldRead());
        Assert.assertFalse(perms.isWorldWrite());
    }

    /**
     * Tests the creation of a group with permission <code>rwr---</code>.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateGroupRWR() throws Exception {
        String uuid = UUID.randomUUID().toString();
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rwr---"));
        IAdminPrx svc = root.getSession().getAdminService();
        long id = svc.createGroup(g);
        // Load the group
        IQueryPrx query = root.getSession().getQueryService();
        ParametersI p = new ParametersI();
        p.addId(id);
        ExperimenterGroup eg = (ExperimenterGroup) query.findByQuery(
                "select distinct g from ExperimenterGroup g where g.id = :id",
                p);
        Assert.assertNotNull(eg);
        Assert.assertEquals(eg.getName().getValue(), uuid);
        // test permissions
        Permissions perms = eg.getDetails().getPermissions();
        Assert.assertTrue(perms.isUserRead());
        Assert.assertTrue(perms.isUserWrite());
        Assert.assertTrue(perms.isGroupRead());
        Assert.assertFalse(perms.isGroupWrite());
        Assert.assertFalse(perms.isWorldRead());
        Assert.assertFalse(perms.isWorldWrite());
    }

    /**
     * Tests the creation of a group with permission <code>rwrw--</code>.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateGroupRWRW() throws Exception {
        String uuid = UUID.randomUUID().toString();
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rwrw--"));
        IAdminPrx svc = root.getSession().getAdminService();
        long id = svc.createGroup(g);
        // Load the group
        IQueryPrx query = root.getSession().getQueryService();
        ParametersI p = new ParametersI();
        p.addId(id);
        ExperimenterGroup eg = (ExperimenterGroup) query.findByQuery(
                "select distinct g from ExperimenterGroup g where g.id = :id",
                p);
        Assert.assertNotNull(eg);
        Assert.assertEquals(eg.getName().getValue(), uuid);
        // test permissions
        Permissions perms = eg.getDetails().getPermissions();
        Assert.assertTrue(perms.isUserRead());
        Assert.assertTrue(perms.isUserWrite());
        Assert.assertTrue(perms.isGroupRead());
        Assert.assertTrue(perms.isGroupWrite());
        Assert.assertFalse(perms.isWorldRead());
        Assert.assertFalse(perms.isWorldWrite());
    }

    /**
     * Tests the creation of a user w/o setting a password and adds the user to
     * the newly created group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateUserWithoutPassword() throws Exception {
        // First create a group.
        String uuid = UUID.randomUUID().toString();
        Experimenter e = createExperimenterI(uuid, "user", "user");

        // Create a group and add the experimenter to that group
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rw----"));
        IAdminPrx svc = root.getSession().getAdminService();
        long id = svc.createGroup(g);
        // Load the group
        IQueryPrx query = root.getSession().getQueryService();
        ParametersI p = new ParametersI();
        p.addId(id);
        ExperimenterGroup eg = (ExperimenterGroup) query.findByQuery(
                "select distinct g from ExperimenterGroup g where g.id = :id",
                p);
        Assert.assertNotNull(eg);
        long groupId = id;
        List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
        // method tested elsewhere
        ExperimenterGroup userGroup = svc.lookupGroup(roles.userGroupName);
        groups.add(eg);
        groups.add(userGroup);
        id = svc.createExperimenter(e, eg, groups);
        // Check if we have a user
        p = new ParametersI();
        p.addId(id);
        e = (Experimenter) query.findByQuery(
                "select distinct e from Experimenter e where e.id = :id", p);
        Assert.assertNotNull(e);
        Assert.assertEquals(e.getOmeName().getValue(), uuid);
        // now check if the user is in correct groups.
        List<Long> ids = new ArrayList<Long>();
        ids.add(groupId);
        ids.add(userGroup.getId().getValue());
        p = new ParametersI();
        p.addLongs("gids", ids);
        List<IObject> list = query.findAllByQuery("select m "
                + "from GroupExperimenterMap as m "
                + "left outer join fetch m.child "
                + "left outer join fetch m.parent"
                + " where m.parent.id in (:gids)", p);
        Assert.assertNotNull(list);
        Iterator<IObject> i = list.iterator();
        GroupExperimenterMap geMap;
        int count = 0;
        while (i.hasNext()) {
            geMap = (GroupExperimenterMap) i.next();
            if (geMap.getChild().getId().getValue() == id)
                count++;
        }
        Assert.assertEquals(count, 2);
    }

    /**
     * Tests the creation of a user with a password and adds the user to the
     * newly created group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateUserWithPassword() throws Exception {
        // First create a group.
        String uuid = UUID.randomUUID().toString();
        Experimenter e = createExperimenterI(uuid, "user", "user");

        // Create a group and add the experimenter to that group
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rw----"));
        IAdminPrx svc = root.getSession().getAdminService();
        long id = svc.createGroup(g);
        // Load the group
        IQueryPrx query = root.getSession().getQueryService();
        ParametersI p = new ParametersI();
        p.addId(id);
        ExperimenterGroup eg = (ExperimenterGroup) query.findByQuery(
                "select distinct g from ExperimenterGroup g where g.id = :id",
                p);
        Assert.assertNotNull(eg);
        long groupId = id;
        List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
        // method tested elsewhere
        ExperimenterGroup userGroup = svc.lookupGroup(roles.userGroupName);
        groups.add(eg);
        groups.add(userGroup);
        id = svc.createExperimenterWithPassword(e, omero.rtypes.rstring(PASSWORD), eg,
                groups);
        // Check if we have a user
        p = new ParametersI();
        p.addId(id);
        e = (Experimenter) query.findByQuery(
                "select distinct e from Experimenter e where e.id = :id", p);
        Assert.assertNotNull(e);
        Assert.assertEquals(e.getOmeName().getValue(), uuid);
        // now check if the user is in correct groups.
        List<Long> ids = new ArrayList<Long>();
        ids.add(groupId);
        ids.add(userGroup.getId().getValue());
        p = new ParametersI();
        p.addLongs("gids", ids);
        List<IObject> list = query.findAllByQuery("select m "
                + "from GroupExperimenterMap as m "
                + "left outer join fetch m.child "
                + "left outer join fetch m.parent"
                + " where m.parent.id in (:gids)", p);
        Assert.assertNotNull(list);
        Iterator<IObject> i = list.iterator();
        GroupExperimenterMap geMap;
        int count = 0;
        while (i.hasNext()) {
            geMap = (GroupExperimenterMap) i.next();
            if (geMap.getChild().getId().getValue() == id)
                count++;
        }
        Assert.assertEquals(count, 2);
    }

    /**
     * Tests the creation of a user using the <code>createUser</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateUser() throws Exception {
        String uuid = UUID.randomUUID().toString();
        Experimenter e = createExperimenterI(uuid, "user", "user");
        IAdminPrx svc = root.getSession().getAdminService();

        // already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rw----"));
        long groupId = svc.createGroup(g);

        long id = svc.createUser(e, uuid);
        IQueryPrx query = root.getSession().getQueryService();

        // Check if we have a user
        ParametersI p = new ParametersI();
        p.addId(id);
        e = (Experimenter) query.findByQuery(
                "select distinct e from Experimenter e where e.id = :id", p);
        Assert.assertNotNull(e);
        Assert.assertEquals(e.getOmeName().getValue(), uuid);
        // check if we are in the correct group i.e. user and uuid
        // now check if the user is in correct groups.
        ExperimenterGroup userGroup = svc.lookupGroup(roles.userGroupName);
        List<Long> ids = new ArrayList<Long>();
        ids.add(groupId);
        ids.add(userGroup.getId().getValue());
        p = new ParametersI();
        p.addLongs("gids", ids);
        List<IObject> list = query.findAllByQuery("select m "
                + "from GroupExperimenterMap as m "
                + "left outer join fetch m.child "
                + "left outer join fetch m.parent"
                + " where m.parent.id in (:gids)", p);
        Assert.assertNotNull(list);
        Iterator<IObject> i = list.iterator();
        GroupExperimenterMap geMap;
        int count = 0;
        while (i.hasNext()) {
            geMap = (GroupExperimenterMap) i.next();
            if (geMap.getChild().getId().getValue() == id)
                count++;
        }
        Assert.assertEquals(count, 2);
    }

    /**
     * Tests the creation of a user using the <code>createUser</code> method. A
     * group not created is specified as the default group, in that case, an
     * exception should be thrown.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateUserNoGroup() throws Exception {
        String uuid = UUID.randomUUID().toString();
        Experimenter e = createExperimenterI(uuid, "user", "user");
        IAdminPrx svc = root.getSession().getAdminService();

        try {
            svc.createUser(e, uuid);
            Assert.fail("The user should not have been created. No group specified.");
        } catch (Exception ex) {
        }
    }

    /**
     * Tests the update an existing experimenter.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testUpdateExperimenterByAdmin() throws Exception {
        // First create a new user.
        String uuid = UUID.randomUUID().toString();
        Experimenter e = createExperimenterI(uuid, "user", "user");
        IAdminPrx svc = root.getSession().getAdminService();

        // already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rw----"));

        // create group.
        svc.createGroup(g);

        long id = svc.createUser(e, uuid);
        IQueryPrx query = root.getSession().getQueryService();
        ParametersI p = new ParametersI();
        p.addId(id);
        e = (Experimenter) query.findByQuery(
                "select distinct e from Experimenter e where e.id = :id", p);
        Assert.assertNotNull(e);

        String name = "userModified";
        uuid = UUID.randomUUID().toString();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring(name));
        e.setLastName(omero.rtypes.rstring(name));
        e.setLdap(omero.rtypes.rbool(false));
        svc.updateExperimenter(e);
        e = (Experimenter) query.findByQuery(
                "select distinct e from Experimenter e where e.id = :id", p);
        Assert.assertNotNull(e);
        Assert.assertEquals(e.getOmeName().getValue(), uuid);
        Assert.assertEquals(e.getFirstName().getValue(), name);
        Assert.assertEquals(e.getLastName().getValue(), name);
    }

    /**
     * Tests the update of the details of the user currently logged in using the
     * <code>updateSelf</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testUpdateExperimenterByUserUsingUpdateSelf() throws Exception {
        // First create a new user.
        String uuid = UUID.randomUUID().toString();
        Experimenter e = createExperimenterI(uuid, "user", "user");
        IAdminPrx svc = root.getSession().getAdminService();

        // already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rw----"));

        // create group.
        g = svc.getGroup(svc.createGroup(g));

        long id = newUserInGroupWithPassword(e, g, uuid);
        svc.setDefaultGroup(svc.getExperimenter(id), g);
        IQueryPrx query = root.getSession().getQueryService();
        ParametersI p = new ParametersI();
        p.addId(id);
        e = (Experimenter) query.findByQuery(
                "select distinct e from Experimenter e where e.id = :id", p);
        Assert.assertNotNull(e);

        String name = "userModified";
        // uuid = UUID.randomUUID().toString();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring(name));
        e.setLastName(omero.rtypes.rstring(name));
        e.setLdap(omero.rtypes.rbool(false));

        // owner logs in.
        omero.client client = newOmeroClient();
        client.createSession(uuid, uuid);
        init(client);
        iAdmin.updateSelf(e);
        e = (Experimenter) query.findByQuery(
                "select distinct e from Experimenter e where e.id = :id", p);
        Assert.assertNotNull(e);
        Assert.assertEquals(e.getOmeName().getValue(), uuid);
        Assert.assertEquals(e.getFirstName().getValue(), name);
        Assert.assertEquals(e.getLastName().getValue(), name);
    }

    /**
     * Tests the update of the details of the user currently logged in using the
     * <code>updateExperimenter</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testUpdateExperimenterByUserUsingUpdateExperimenter()
            throws Exception {
        // First create a new user.
        String uuid = UUID.randomUUID().toString();
        Experimenter e = createExperimenterI(uuid, "user", "user");
        IAdminPrx svc = root.getSession().getAdminService();

        // already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rw----"));

        // create group.
        g = svc.getGroup(svc.createGroup(g));

        long id = newUserInGroupWithPassword(e, g, uuid);
        svc.setDefaultGroup(svc.getExperimenter(id), g);
        IQueryPrx query = root.getSession().getQueryService();
        ParametersI p = new ParametersI();
        p.addId(id);
        e = (Experimenter) query.findByQuery(
                "select distinct e from Experimenter e where e.id = :id", p);
        Assert.assertNotNull(e);

        String name = "userModified";
        // uuid = UUID.randomUUID().toString();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring(name));
        e.setLastName(omero.rtypes.rstring(name));
        e.setLdap(omero.rtypes.rbool(false));

        // owner logs in.
        omero.client client = newOmeroClient();
        client.createSession(uuid, uuid);
        init(client);
        iAdmin.updateExperimenter(e);
        e = (Experimenter) query.findByQuery(
                "select distinct e from Experimenter e where e.id = :id", p);
        Assert.assertNotNull(e);
        Assert.assertEquals(e.getOmeName().getValue(), uuid);
        Assert.assertEquals(e.getFirstName().getValue(), name);
        Assert.assertEquals(e.getLastName().getValue(), name);
    }

    /**
     * Tests the update of the user password by the administrator
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testChangePasswordByAdmin() throws Exception {
        // First create a new user.
        String uuid = UUID.randomUUID().toString();
        Experimenter e = createExperimenterI(uuid, "user", "user");
        IAdminPrx svc = root.getSession().getAdminService();

        // already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rw----"));

        // create group.
        svc.createGroup(g);
        svc.createUser(e, uuid);
        e = svc.lookupExperimenter(uuid);
        try {
            svc.changeUserPassword(uuid, omero.rtypes.rstring(PASSWORD_MODIFIED));
        } catch (Exception ex) {
            Assert.fail("Not possible to modify the experimenter's password.");
        }
    }

    /**
     * Tests the default group of an experimenter.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testChangeDefaultGroup() throws Exception {
        // Create 2 groups and add a user
        String uuid1 = UUID.randomUUID().toString();
        ExperimenterGroup g1 = new ExperimenterGroupI();
        g1.setName(omero.rtypes.rstring(uuid1));
        g1.setLdap(omero.rtypes.rbool(false));
        g1.getDetails().setPermissions(new PermissionsI("rw----"));

        String uuid2 = UUID.randomUUID().toString();
        ExperimenterGroup g2 = new ExperimenterGroupI();
        g2.setName(omero.rtypes.rstring(uuid2));
        g2.setLdap(omero.rtypes.rbool(false));
        g2.getDetails().setPermissions(new PermissionsI("rw----"));

        IAdminPrx svc = root.getSession().getAdminService();
        IQueryPrx query = root.getSession().getQueryService();
        long id1 = svc.createGroup(g1);
        long id2 = svc.createGroup(g2);

        ParametersI p = new ParametersI();
        p.addId(id1);

        ExperimenterGroup eg1 = (ExperimenterGroup) query.findByQuery(
                "select distinct g from ExperimenterGroup g where g.id = :id",
                p);
        p = new ParametersI();
        p.addId(id2);

        ExperimenterGroup eg2 = (ExperimenterGroup) query.findByQuery(
                "select distinct g from ExperimenterGroup g where g.id = :id",
                p);
        Experimenter e = createExperimenterI(uuid1, "user", "user");

        List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
        // method tested elsewhere
        ExperimenterGroup userGroup = svc.lookupGroup(roles.userGroupName);
        groups.add(eg1);
        groups.add(eg2);
        groups.add(userGroup);

        long id = svc.createExperimenter(e, eg1, groups);
        e = svc.lookupExperimenter(uuid1);
        List<GroupExperimenterMap> links = e.copyGroupExperimenterMap();
        Assert.assertEquals(groups.get(0).getId().getValue(), eg1.getId().getValue());
        svc.setDefaultGroup(e, eg2);

        e = svc.lookupExperimenter(uuid1);
        links = e.copyGroupExperimenterMap();
        groups = new ArrayList<ExperimenterGroup>();
        for (GroupExperimenterMap link : links) {
            groups.add(link.getParent());
        }
        Assert.assertEquals(groups.get(0).getId().getValue(), eg2.getId().getValue());
    }

    /**
     * Tests the deletion of an experimenter.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteExperimenter() throws Exception {
        // First create a new user.
        String uuid = UUID.randomUUID().toString();
        Experimenter e = createExperimenterI(uuid, "user", "user");
        IAdminPrx svc = root.getSession().getAdminService();

        // already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rw----"));

        // create group.
        svc.createGroup(g);
        long id = svc.createUser(e, uuid);
        e = svc.lookupExperimenter(uuid);
        svc.deleteExperimenter(e);
        ParametersI p = new ParametersI();
        p.addId(id);
        IQueryPrx query = root.getSession().getQueryService();
        e = (Experimenter) query.findByQuery(
                "select distinct e from Experimenter e where e.id = :id", p);
        Assert.assertNull(e);
    }

    /**
     * Tests the deletion of a group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteGroup() throws Exception {
        // First create a new user.
        String uuid = UUID.randomUUID().toString();
        IAdminPrx svc = root.getSession().getAdminService();

        // already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rw----"));

        // create group.
        long id = svc.createGroup(g);
        g = svc.lookupGroup(uuid);
        svc.deleteGroup(g);
        ParametersI p = new ParametersI();
        p.addId(id);
        IQueryPrx query = root.getSession().getQueryService();
        g = (ExperimenterGroup) query.findByQuery(
                "select distinct g from ExperimenterGroup g where g.id = :id",
                p);
        Assert.assertNull(g);
    }

    /**
     * Tests to make a user the owner of a group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSetOwner() throws Exception {
        // First create a new user.
        String uuid = UUID.randomUUID().toString();
        Experimenter e = createExperimenterI(uuid, "user", "user");
        IAdminPrx svc = root.getSession().getAdminService();

        // already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rw----"));

        // create group.
        long groupId = svc.createGroup(g);
        g = svc.lookupGroup(uuid);
        // create the user.
        long expId = svc.createUser(e, uuid);
        e = svc.lookupExperimenter(uuid);
        // set the user as the group owner.
        svc.setGroupOwner(g, e);
        IQueryPrx query = root.getSession().getQueryService();
        String sql = "select m from GroupExperimenterMap as m ";
        sql += "left outer join fetch m.child as c ";
        sql += "left outer join fetch m.parent as p ";
        sql += "where ";
        sql += "c.id = :expId ";
        sql += " and p.id = :groupId";
        ParametersI p = new ParametersI();
        p.addLong("expId", expId);
        p.addLong("groupId", groupId);
        List<IObject> l = query.findAllByQuery(sql, p);
        Iterator<IObject> i = l.iterator();
        GroupExperimenterMap map;
        while (i.hasNext()) {
            map = (GroupExperimenterMap) i.next();
            Assert.assertTrue(map.getOwner().getValue());
        }
    }

    /**
     * Tests the upload of the user picture.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testUploadMyUserPhoto() throws Exception {
        IAdminPrx svc = root.getSession().getAdminService();
        // First create a new user.
        String uuid = UUID.randomUUID().toString();
        Experimenter e = createExperimenterI(uuid, "user", "user");

        // already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rw----"));

        // create group.
        g = svc.getGroup(svc.createGroup(g));
        // create the user.
        long uid = newUserInGroupWithPassword(e, g, uuid);
        svc.setDefaultGroup(svc.getExperimenter(uid), g);
        omero.client client = new omero.client(root.getPropertyMap());
        try {
            client.createSession(uuid, uuid);
            IAdminPrx prx = client.getSession().getAdminService();
            long id = prx.uploadMyUserPhoto("/tmp/foto.jpg", "image/jpeg",
                    new byte[] { 1 });
            Assert.assertTrue(id >= 0);
        } finally {
            client.closeSession();
        }
    }

    /**
     * Tests the adding and modifying of a user photo, specify all the exception
     * thrown by ticket:1791
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testUserPhoto() throws Exception {
        IAdminPrx prx = root.getSession().getAdminService();
        String uuid = UUID.randomUUID().toString();
        // First create a user in two groups, one rwrw-- and one rwr---
        ExperimenterGroup rwr = new ExperimenterGroupI();
        rwr.setName(omero.rtypes.rstring(uuid));
        rwr.setLdap(omero.rtypes.rbool(false));
        rwr.getDetails().setPermissions(new PermissionsI("rwr---"));
        long rwrID = prx.createGroup(rwr);
        rwr = prx.getGroup(rwrID);

        ExperimenterGroup rwrw = new ExperimenterGroupI();
        rwrw.setName(omero.rtypes.rstring(UUID.randomUUID().toString()));
        rwrw.setLdap(omero.rtypes.rbool(false));
        rwr.getDetails().setPermissions(new PermissionsI("rwrw--"));
        long rwrwID = prx.createGroup(rwrw);
        rwrw = prx.getGroup(rwrwID);

        Experimenter e = createExperimenterI(uuid, "user", "user");
        long userID = newUserInGroupWithPassword(e, rwrw, uuid);
        e = prx.getExperimenter(userID);

        prx.addGroups(e, Arrays.asList(rwr));

        omero.client client = new omero.client(root.getPropertyMap());
        try {
            client.createSession(uuid, uuid);
            prx = client.getSession().getAdminService();
            prx.uploadMyUserPhoto("/tmp/foto.jpg", "image/jpeg",
                    new byte[] { 1 });
            client.getSession().setSecurityContext(
                    new ExperimenterGroupI(rwrID, false));
            prx.uploadMyUserPhoto("/tmp/foto2.jpg", "image/jpeg",
                    new byte[] { 2 });
        } finally {
            client.closeSession();
        }
    }

    /**
     * Tests the modification of the password by the user currently logged in.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testChangePasswordByUser() throws Exception {
        // current user change the password.
        iAdmin.changePassword(omero.rtypes.rstring(PASSWORD_MODIFIED));
    }

    /**
     * Tests the attempt to modify the password by another user (non admin) than
     * the one currently logged in.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testChangePasswordByOtherUser() throws Exception {
        IAdminPrx prx = root.getSession().getAdminService();
        // add a new user
        String groupName = iAdmin.getEventContext().groupName;
        String userName = iAdmin.getEventContext().userName;
        String uuid = UUID.randomUUID().toString();
        Experimenter e = createExperimenterI(uuid, "user", "user");
        // create the user.
        long userID = prx.createUser(e, groupName);
        // now the new user is going to try to modify the password
        omero.client client = new omero.client(root.getPropertyMap());
        try {
            client.createSession(uuid, groupName);
            client.getSession().getAdminService()
                    .changeUserPassword(userName, omero.rtypes.rstring(PASSWORD_MODIFIED));
            Assert.fail("The user should not have been able to modify the password.");
        } catch (Exception ex) {

        }
        client.closeSession();
    }

    /**
     * Test the experimenter renaming prohibitions of {@link ome.api.IAdmin#updateExperimenter(ome.model.meta.Experimenter)}.
     * @throws Exception unexpected
     */
    @Test
    public void testExperimenterRenameValidation() throws Exception {
        final IAdminPrx proxy = root.getSession().getAdminService();

        final Roles roles = proxy.getSecurityRoles();
        final Experimenter rootExperimenter  = proxy.getExperimenter(roles.rootId);
        final Experimenter guestExperimenter = proxy.getExperimenter(roles.guestId);

        final String userName = UUID.randomUUID().toString();
        Experimenter normalExperimenter = createExperimenterI(userName, "a", "user");
        normalExperimenter = proxy.getExperimenter(proxy.createUser(normalExperimenter, roles.userGroupName));

        final RString newName = omero.rtypes.rstring(UUID.randomUUID().toString());

        for (final Experimenter specialExperimenter : ImmutableList.of(rootExperimenter, guestExperimenter)) {
            try {
                /* test that special users cannot be renamed */
                specialExperimenter.setOmeName(newName);
                proxy.updateExperimenter(specialExperimenter);
                Assert.fail("Should not be able to rename special users.");
            } catch (ValidationException e) { }
        }
        /* test that normal users can be renamed */
        normalExperimenter.setOmeName(newName);
        proxy.updateExperimenter(normalExperimenter);
    }

    /**
     * Test the group renaming prohibitions of {@link ome.api.IAdmin#updateGroup(ome.model.meta.ExperimenterGroup)}.
     * @throws Exception unexpected
     */
    @Test
    public void testGroupRenameValidation() throws Exception {
        final IAdminPrx proxy = root.getSession().getAdminService();

        final Roles roles = proxy.getSecurityRoles();
        final ExperimenterGroup userGroup   = proxy.getGroup(roles.userGroupId);
        final ExperimenterGroup systemGroup = proxy.getGroup(roles.systemGroupId);
        final ExperimenterGroup guestGroup  = proxy.getGroup(roles.guestGroupId);

        final String groupName = UUID.randomUUID().toString();
        ExperimenterGroup normalGroup = new ExperimenterGroupI();
        normalGroup.setName(omero.rtypes.rstring(groupName));
        normalGroup.setLdap(omero.rtypes.rbool(false));
        normalGroup.getDetails().setPermissions(new PermissionsI("rw----"));
        normalGroup = proxy.getGroup(proxy.createGroup(normalGroup));

        final RString newName = omero.rtypes.rstring(UUID.randomUUID().toString());

        for (final ExperimenterGroup specialGroup : ImmutableList.of(userGroup, systemGroup, guestGroup)) {
            try {
                /* test that special groups cannot be renamed */
                specialGroup.setName(newName);
                proxy.updateGroup(specialGroup);
                Assert.fail("Should not be able to rename special groups.");
            } catch (ValidationException e) { }
        }
        /* test that normal groups can be renamed */
        normalGroup.setName(newName);
        proxy.updateGroup(normalGroup);
    }

    /**
     * Test that a user cannot rename the group that is currently their context.
     * @throws Exception unexpected
     */
    @Test
    public void testCurrentGroupRenameProhibition() throws Exception {
        IAdminPrx proxy;
        proxy = root.getSession().getAdminService();

        final Roles roles = proxy.getSecurityRoles();
        ExperimenterGroup userGroup   = proxy.getGroup(roles.userGroupId);
        ExperimenterGroup systemGroup = proxy.getGroup(roles.systemGroupId);

        final String normalGroupName1α = UUID.randomUUID().toString();
        final String normalGroupName1β = UUID.randomUUID().toString();
        ExperimenterGroup normalGroup1 = new ExperimenterGroupI();
        normalGroup1.setName(omero.rtypes.rstring(normalGroupName1α));
        normalGroup1.setLdap(omero.rtypes.rbool(false));
        normalGroup1 = proxy.getGroup(proxy.createGroup(normalGroup1));

        final String normalGroupName2 = UUID.randomUUID().toString();
        ExperimenterGroup normalGroup2 = new ExperimenterGroupI();
        normalGroup2.setName(omero.rtypes.rstring(normalGroupName2));
        normalGroup2.setLdap(omero.rtypes.rbool(false));
        normalGroup2 = proxy.getGroup(proxy.createGroup(normalGroup2));

        final String userName1 = UUID.randomUUID().toString();
        Experimenter experimenter1 = createExperimenterI(userName1, "1", "user");
        final long experimenterId1 = newUserInGroupWithPassword(experimenter1,
                normalGroup1, userName1);
        experimenter1 = proxy.getExperimenter(experimenterId1);
        proxy.addGroups(experimenter1, ImmutableList.of(userGroup, systemGroup, normalGroup1, normalGroup2));
        experimenter1 = proxy.getExperimenter(experimenterId1);

        final String userName2 = UUID.randomUUID().toString();
        Experimenter experimenter2 = createExperimenterI(userName2, "2", "user");
        final long experimenterId2 = newUserInGroupWithPassword(experimenter2,
                normalGroup1, userName2);
        experimenter2 = proxy.getExperimenter(experimenterId2);
        proxy.addGroups(experimenter2, ImmutableList.of(userGroup, normalGroup1, normalGroup2));
        experimenter2 = proxy.getExperimenter(experimenterId2);

        final omero.client client1 = newOmeroClient();
        final omero.client client2 = newOmeroClient();

        client1.createSession(userName1, userName1);
        client2.createSession(userName2, userName2);

        proxy = client1.getSession().getAdminService();
        try {
            /* test that the current group cannot be renamed */
            normalGroup1.setName(omero.rtypes.rstring(normalGroupName1β));
            proxy.updateGroup(normalGroup1);
            Assert.fail("the current group may not be renamed");
        } catch (ValidationException e) { }
        /* switch current group */
        proxy.setDefaultGroup(experimenter1, normalGroup2);
        client1.closeSession();
        client1.createSession(userName1, userName1);
        proxy = client1.getSession().getAdminService();
        /* test that the same group can be renamed if no longer current */
        proxy.updateGroup(normalGroup1);
        /* test the viability of another user still logged in with the renamed group as current */
        proxy = client2.getSession().getAdminService();
        proxy.setDefaultGroup(experimenter2, normalGroup2);

        client1.closeSession();
        client2.closeSession();
    }

    /**
     * Test the group removal prohibitions of
     * {@link ome.api.IAdmin#removeGroups(ome.model.meta.Experimenter, ome.model.meta.ExperimenterGroup...)}.
     * Specifically, test this claim from the Javadoc:
     * <q>The root experimenter is required to be in both the user and system groups.</q>
     * @throws Exception unexpected
     */
    @Test
    public void testGroupRemovalValidationClaim1() throws Exception {
        IAdminPrx proxy;
        proxy = root.getSession().getAdminService();

        final Roles roles = proxy.getSecurityRoles();
        Experimenter rootExperimenter = proxy.getExperimenter(roles.rootId);
        ExperimenterGroup userGroup   = proxy.getGroup(roles.userGroupId);
        ExperimenterGroup systemGroup = proxy.getGroup(roles.systemGroupId);

        final String normalGroupName = UUID.randomUUID().toString();
        ExperimenterGroup normalGroup = new ExperimenterGroupI();
        normalGroup.setName(omero.rtypes.rstring(normalGroupName));
        normalGroup.setLdap(omero.rtypes.rbool(false));
        normalGroup = proxy.getGroup(proxy.createGroup(normalGroup));

        proxy.addGroups(rootExperimenter, ImmutableList.of(normalGroup));
        rootExperimenter = proxy.getExperimenter(roles.rootId);

        final String userName1 = UUID.randomUUID().toString();
        Experimenter experimenter1 = createExperimenterI(userName1, "1", "user");
        final long experimenterId1 = newUserInGroupWithPassword(experimenter1,
                normalGroup, userName1);
        experimenter1 = proxy.getExperimenter(experimenterId1);
        proxy.addGroups(experimenter1, ImmutableList.of(userGroup, systemGroup, normalGroup));
        experimenter1 = proxy.getExperimenter(experimenterId1);

        final String userName2 = UUID.randomUUID().toString();
        Experimenter experimenter2 = createExperimenterI(userName2, "2", "user");
        final long experimenterId2 = newUserInGroupWithPassword(experimenter2,
                normalGroup, userName2);
        experimenter2 = proxy.getExperimenter(experimenterId2);
        proxy.addGroups(experimenter2, ImmutableList.of(userGroup, systemGroup, normalGroup));
        experimenter2 = proxy.getExperimenter(experimenterId2);

        /* unload experimenters and groups */
        userGroup = new ExperimenterGroupI(userGroup.getId(), false);
        systemGroup = new ExperimenterGroupI(systemGroup.getId(), false);
        normalGroup = new ExperimenterGroupI(normalGroup.getId(), false);
        rootExperimenter = new ExperimenterI(rootExperimenter.getId(), false);
        experimenter1 = new ExperimenterI(experimenter1.getId(), false);
        experimenter2 = new ExperimenterI(experimenter2.getId(), false);

        final omero.client client = newOmeroClient();

        client.createSession(userName1, userName1);
        proxy = client.getSession().getAdminService();

        try {
            /* test that the system group cannot be removed from root */
            proxy.removeGroups(rootExperimenter, ImmutableList.of(systemGroup));
            Assert.fail("the root experimenter may not be removed from the system group");
        } catch (ValidationException e) { }
        try {
            /* test that the user group cannot be removed from root */
            proxy.removeGroups(rootExperimenter, ImmutableList.of(userGroup));
            Assert.fail("the root experimenter may not be removed from the user group");
        } catch (ValidationException e) { }
        /* test that a non-system group can be removed from root */
        proxy.removeGroups(rootExperimenter, ImmutableList.of(normalGroup));
        /* test that a user can remove a non-root user from the system group */
        proxy.removeGroups(experimenter2, ImmutableList.of(systemGroup));
        /* test that a user can remove a non-root user from the user group */
        proxy.removeGroups(experimenter2, ImmutableList.of(userGroup));

        client.closeSession();
    }

    /**
     * Test the group removal prohibitions of
     * {@link ome.api.IAdmin#removeGroups(ome.model.meta.Experimenter, ome.model.meta.ExperimenterGroup...)}.
     * Specifically, test this claim from the Javadoc:
     * <q>An experimenter may not remove himself/herself from the user or system group.</q>
     * @throws Exception unexpected
     */
    @Test
    public void testGroupRemovalValidationClaim2() throws Exception {
        IAdminPrx proxy;
        proxy = root.getSession().getAdminService();

        final Roles roles = proxy.getSecurityRoles();
        ExperimenterGroup userGroup   = proxy.getGroup(roles.userGroupId);
        ExperimenterGroup systemGroup = proxy.getGroup(roles.systemGroupId);

        final String normalGroupName = UUID.randomUUID().toString();
        ExperimenterGroup normalGroup = new ExperimenterGroupI();
        normalGroup.setName(omero.rtypes.rstring(normalGroupName));
        normalGroup.setLdap(omero.rtypes.rbool(false));
        normalGroup = proxy.getGroup(proxy.createGroup(normalGroup));

        final String userName1 = UUID.randomUUID().toString();
        Experimenter experimenter1 = createExperimenterI(userName1, "1", "user");
        final long experimenterId1 = newUserInGroupWithPassword(experimenter1,
                normalGroup, userName1);
        experimenter1 = proxy.getExperimenter(experimenterId1);
        proxy.addGroups(experimenter1, ImmutableList.of(userGroup, systemGroup, normalGroup));
        experimenter1 = proxy.getExperimenter(experimenterId1);

        final String userName2 = UUID.randomUUID().toString();
        Experimenter experimenter2 = createExperimenterI(userName2, "2", "user");
        final long experimenterId2 = newUserInGroupWithPassword(experimenter2,
                normalGroup, userName2);
        experimenter2 = proxy.getExperimenter(experimenterId2);
        proxy.addGroups(experimenter2, ImmutableList.of(userGroup, systemGroup, normalGroup));
        experimenter2 = proxy.getExperimenter(experimenterId2);

        /* unload experimenters and groups */
        userGroup = new ExperimenterGroupI(userGroup.getId(), false);
        systemGroup = new ExperimenterGroupI(systemGroup.getId(), false);
        normalGroup = new ExperimenterGroupI(normalGroup.getId(), false);
        experimenter1 = new ExperimenterI(experimenter1.getId(), false);
        experimenter2 = new ExperimenterI(experimenter2.getId(), false);

        final omero.client client = newOmeroClient();

        client.createSession(userName1, userName1);
        proxy = client.getSession().getAdminService();

        try {
            /* test that the system group cannot be removed from the current user */
            proxy.removeGroups(experimenter1, ImmutableList.of(systemGroup));
            Assert.fail("an experimenter may not remove themself from the system group");
        } catch (ValidationException e) { }
        try {
            /* test that the user group cannot be removed from the current user */
            proxy.removeGroups(experimenter1, ImmutableList.of(userGroup));
            Assert.fail("an experimenter may not remove themself from the user group");
        } catch (ValidationException e) { }
        /* test that a different group can be removed from the current user */
        proxy.removeGroups(experimenter1, ImmutableList.of(normalGroup));
        /* test that a user can remove a different user from the system group */
        proxy.removeGroups(experimenter2, ImmutableList.of(systemGroup));
        /* test that a user can remove a different user from the user group */
        proxy.removeGroups(experimenter2, ImmutableList.of(userGroup));

        client.closeSession();
    }

    /**
     * Test the group removal prohibitions of
     * {@link ome.api.IAdmin#removeGroups(ome.model.meta.Experimenter, ome.model.meta.ExperimenterGroup...)}.
     * Specifically, test this claim from the Javadoc:
     * <q>An experimenter may not be a member of only the user group,
     * some other group is also required as the default group.</q>
     * @throws Exception unexpected
     */
    @Test
    public void testGroupRemovalValidationClaim3() throws Exception {
        IAdminPrx proxy;
        proxy = root.getSession().getAdminService();

        final Roles roles = proxy.getSecurityRoles();
        ExperimenterGroup userGroup = proxy.getGroup(roles.userGroupId);

        final String normalGroupName = UUID.randomUUID().toString();
        ExperimenterGroup normalGroup = new ExperimenterGroupI();
        normalGroup.setName(omero.rtypes.rstring(normalGroupName));
        normalGroup.setLdap(omero.rtypes.rbool(false));
        normalGroup = proxy.getGroup(proxy.createGroup(normalGroup));

        final String userName1 = UUID.randomUUID().toString();
        Experimenter experimenter1 = createExperimenterI(userName1, "1", "user");
        final long experimenterId1 = proxy.createUser(experimenter1, normalGroupName);
        experimenter1 = proxy.getExperimenter(experimenterId1);
        proxy.addGroups(experimenter1, ImmutableList.of(userGroup, normalGroup));
        experimenter1 = proxy.getExperimenter(experimenterId1);

        final String userName2 = UUID.randomUUID().toString();
        Experimenter experimenter2 = createExperimenterI(userName2, "2", "user");
        final long experimenterId2 = proxy.createUser(experimenter2, normalGroupName);
        experimenter2 = proxy.getExperimenter(experimenterId2);
        proxy.addGroups(experimenter2, ImmutableList.of(userGroup, normalGroup));
        experimenter2 = proxy.getExperimenter(experimenterId2);

        /* unload experimenters and groups */
        userGroup = new ExperimenterGroupI(userGroup.getId(), false);
        normalGroup = new ExperimenterGroupI(normalGroup.getId(), false);
        experimenter1 = new ExperimenterI(experimenter1.getId(), false);
        experimenter2 = new ExperimenterI(experimenter2.getId(), false);

        try {
            /* test that a user cannot be left in only the user group */
            proxy.removeGroups(experimenter1, ImmutableList.of(normalGroup));
            Assert.fail("an experimenter may not be a member of only the user group");
        } catch (ValidationException e) { }
        /* test that the user group can be removed from a user, leaving them in one group */
        proxy.removeGroups(experimenter2, ImmutableList.of(userGroup));
    }

    /**
     * Test the group removal prohibitions of
     * {@link ome.api.IAdmin#removeGroups(ome.model.meta.Experimenter, ome.model.meta.ExperimenterGroup...)}.
     * Specifically, test this claim from the Javadoc:
     * <q>An experimenter must remain a member of some group.</q>
     * @throws Exception unexpected
     */
    @Test
    public void testGroupRemovalValidationClaim4() throws Exception {
        IAdminPrx proxy;
        proxy = root.getSession().getAdminService();

        final Roles roles = proxy.getSecurityRoles();
        ExperimenterGroup userGroup = proxy.getGroup(roles.userGroupId);

        final String normalGroupName1 = UUID.randomUUID().toString();
        ExperimenterGroup normalGroup1 = new ExperimenterGroupI();
        normalGroup1.setName(omero.rtypes.rstring(normalGroupName1));
        normalGroup1.setLdap(omero.rtypes.rbool(false));
        normalGroup1 = proxy.getGroup(proxy.createGroup(normalGroup1));

        final String normalGroupName2 = UUID.randomUUID().toString();
        ExperimenterGroup normalGroup2 = new ExperimenterGroupI();
        normalGroup2.setName(omero.rtypes.rstring(normalGroupName2));
        normalGroup2.setLdap(omero.rtypes.rbool(false));
        normalGroup2 = proxy.getGroup(proxy.createGroup(normalGroup2));

        final String userName1 = UUID.randomUUID().toString();
        Experimenter experimenter1 = createExperimenterI(userName1, "1", "user");
        final long experimenterId1 = proxy.createUser(experimenter1, normalGroupName1);
        experimenter1 = proxy.getExperimenter(experimenterId1);
        proxy.addGroups(experimenter1, ImmutableList.of(userGroup, normalGroup1, normalGroup2));
        experimenter1 = proxy.getExperimenter(experimenterId1);

        /* unload experimenters and groups */
        userGroup = new ExperimenterGroupI(userGroup.getId(), false);
        normalGroup1 = new ExperimenterGroupI(normalGroup1.getId(), false);
        normalGroup2 = new ExperimenterGroupI(normalGroup2.getId(), false);
        experimenter1 = new ExperimenterI(experimenter1.getId(), false);

        try {
            /* test that a user must be a member of some group */
            proxy.removeGroups(experimenter1, ImmutableList.of(userGroup, normalGroup1, normalGroup2));
            Assert.fail("an experimenter must remain a member of some group");
        } catch (ValidationException e) { }
        /* test that a user may be a member of only one group */
        proxy.removeGroups(experimenter1, ImmutableList.of(userGroup, normalGroup1));
    }

    /**
     * Tests to modify the permissions of a group. Creates a <code>rwr---</code>
     * group and increases the permissions to <code>rwrw--</code> then back
     * again to <code>rwr--</code>. This tests the
     * <code>ChangePermissions</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testChangePermissionsRWToRWRW() throws Exception {
        root = newRootOmeroClient();
        IAdminPrx prx = root.getSession().getAdminService();
        String uuid = UUID.randomUUID().toString();

        // First group rwr---
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        String representation = "rwr---";
        g.getDetails().setPermissions(new PermissionsI(representation));
        long id = prx.createGroup(g);
        g = prx.getGroup(id);
        Permissions permissions = g.getDetails().getPermissions();
        Assert.assertTrue(permissions.isGroupRead());
        Assert.assertFalse(permissions.isGroupAnnotate());
        Assert.assertFalse(permissions.isGroupWrite());

        // change permissions
        representation = "rwrw--";

        Request mod = Requests.chmod().target(g).toPerms(representation).build();
        doChange(root, root.getSession(), mod, true);
        g = prx.getGroup(id);
        permissions = g.getDetails().getPermissions();
        Assert.assertTrue(permissions.isGroupRead());
        Assert.assertTrue(permissions.isGroupAnnotate());
        Assert.assertTrue(permissions.isGroupWrite());
    }

    /**
     * Tests to modify the permissions of a group. Creates a <code>rwr---</code>
     * group and increases the permissions to <code>rwrw--</code> then back
     * again to <code>rwr--</code>. This tests the
     * <code>ChangePermissions</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testChangePermissionsRWRToRWRA() throws Exception {
        root = newRootOmeroClient();
        IAdminPrx prx = root.getSession().getAdminService();
        String uuid = UUID.randomUUID().toString();

        // First group rwr---
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        String representation = "rwr---";
        g.getDetails().setPermissions(new PermissionsI(representation));
        long id = prx.createGroup(g);
        g = prx.getGroup(id);
        Permissions permissions = g.getDetails().getPermissions();
        Assert.assertTrue(permissions.isGroupRead());
        Assert.assertFalse(permissions.isGroupWrite());
        Assert.assertFalse(permissions.isGroupAnnotate());

        // change permissions
        representation = "rwra--";

        Request mod = Requests.chmod().target(g).toPerms(representation).build();
        doChange(root, root.getSession(), mod, true);
        g = prx.getGroup(id);
        permissions = g.getDetails().getPermissions();
        Assert.assertTrue(permissions.isGroupRead());
        Assert.assertTrue(permissions.isGroupAnnotate());
        Assert.assertFalse(permissions.isGroupWrite());
    }

    /**
     * Tests to modify the permissions of a group. Creates a <code>rwr---</code>
     * group and increases the permissions to <code>rwrw--</code> then back
     * again to <code>rwr--</code>. This tests the
     * <code>ChangePermissions</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testChangePermissionsRWAToRWRW() throws Exception {
        root = newRootOmeroClient();
        IAdminPrx prx = root.getSession().getAdminService();
        String uuid = UUID.randomUUID().toString();

        // First group rwr---
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        String representation = "rwra--";
        g.getDetails().setPermissions(new PermissionsI(representation));
        long id = prx.createGroup(g);
        g = prx.getGroup(id);
        Permissions permissions = g.getDetails().getPermissions();
        Assert.assertTrue(permissions.isGroupRead());
        Assert.assertTrue(permissions.isGroupAnnotate());
        Assert.assertFalse(permissions.isGroupWrite());

        // change permissions
        representation = "rwrw--";

        Request mod = Requests.chmod().target(g).toPerms(representation).build();
        doChange(root, root.getSession(), mod, true);
        g = prx.getGroup(id);
        permissions = g.getDetails().getPermissions();
        Assert.assertTrue(permissions.isGroupRead());
        Assert.assertTrue(permissions.isGroupAnnotate());
        Assert.assertTrue(permissions.isGroupWrite());
    }

    /**
     * Tests to modify the permissions of a group. This tests the
     * <code>ChangePermissions</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testChangePermissionsRWToRWR() throws Exception {
        root = newRootOmeroClient();
        IAdminPrx prx = root.getSession().getAdminService();
        String uuid = UUID.randomUUID().toString();

        // First group rwr---
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        String representation = "rw----";
        g.getDetails().setPermissions(new PermissionsI(representation));
        long id = prx.createGroup(g);
        g = prx.getGroup(id);
        Permissions permissions = g.getDetails().getPermissions();
        Assert.assertFalse(permissions.isGroupRead());
        Assert.assertFalse(permissions.isGroupAnnotate());
        Assert.assertFalse(permissions.isGroupWrite());

        // change permissions
        representation = "rwr---";

        Request mod = Requests.chmod().target(g).toPerms(representation).build();
        doChange(root, root.getSession(), mod, true);
        g = prx.getGroup(id);
        permissions = g.getDetails().getPermissions();
        Assert.assertTrue(permissions.isGroupRead());
        Assert.assertFalse(permissions.isGroupAnnotate());
        Assert.assertFalse(permissions.isGroupWrite());
    }

    /**
     * Tests to promote a group. The permissions of the group are initially
     * <code>rwr---</code> then upgrade to <code>rwrw--</code>. This tests the
     * <code>ChangePermissions</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testPromoteGroup() throws Exception {
        IAdminPrx prx = root.getSession().getAdminService();
        String uuid = UUID.randomUUID().toString();
        // First create a user in two groups, one rwrw-- and one rwr---
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        String representation = "rw----";
        g.getDetails().setPermissions(new PermissionsI(representation));
        long id = prx.createGroup(g);
        g = prx.getGroup(id);
        Permissions permissions = g.getDetails().getPermissions();
        // change permissions and promote the group
        representation = "rwrw--";
        Request mod = Requests.chmod().target(g).toPerms(representation).build();

        doChange(root, root.getSession(), mod, true);
        g = prx.getGroup(id);
        permissions = g.getDetails().getPermissions();
        Assert.assertTrue(permissions.isGroupRead());
        Assert.assertTrue(permissions.isGroupWrite());
    }

    /**
     * Tests to promote a group and try to reduce the permission. The
     * permissions of the group are initially <code>rw---</code> then upgrade to
     * <code>rwr--</code> then back to a <code>rw---</code>. The latest change
     * should return an exception. This tests the <code>ChangePermissions</code>
     * method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testPromoteAndDowngradeGroup() throws Exception {
        IAdminPrx prx = root.getSession().getAdminService();
        String uuid = UUID.randomUUID().toString();
        // First create a user in two groups, one rwrw-- and one rwr---
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        String representation = "rw----";
        g.getDetails().setPermissions(new PermissionsI(representation));
        long id = prx.createGroup(g);
        g = prx.getGroup(id);
        Permissions permissions = g.getDetails().getPermissions();

        // change permissions and promote the group
        representation = "rwr---";
        Request mod = Requests.chmod().target(g).toPerms(representation).build();

        doChange(root, root.getSession(), mod, true);
        g = prx.getGroup(id);
        permissions = g.getDetails().getPermissions();
        Assert.assertTrue(permissions.isGroupRead());
        Assert.assertFalse(permissions.isGroupWrite());
        g = prx.getGroup(id);
        // now try to turn it back to rw----
        representation = "rw----";
        mod = Requests.chmod().target(g).toPerms(representation).build();

        doChange(root, root.getSession(), mod, true);
    }

    /**
     * Tests the addition of existing user by the owner of the group. The owner
     * is NOT an administrator.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testOwnerAddExistingExperimenterToGroup() throws Exception {
        IAdminPrx svc = root.getSession().getAdminService();

        // First create a new user.
        String uuid = UUID.randomUUID().toString();
        Experimenter e = createExperimenterI(uuid, "user", "user");
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rw----"));
        long gId = svc.createGroup(g);
        g = svc.getGroup(gId);
        newUserInGroupWithPassword(e, g, uuid);
        Experimenter owner = svc.lookupExperimenter(uuid);
        svc.setGroupOwner(g, owner);
        svc.setDefaultGroup(owner, g);

        // create another group and user
        String uuid2 = UUID.randomUUID().toString();
        e = createExperimenterI(uuid2, "user", "user");
        ExperimenterGroup g2 = new ExperimenterGroupI();
        g2.setName(omero.rtypes.rstring(uuid2));
        g2.setLdap(omero.rtypes.rbool(false));
        g2.getDetails().setPermissions(new PermissionsI("rw----"));
        long g2Id = svc.createGroup(g2);
        g2 = svc.getGroup(g2Id);
        newUserInGroupWithPassword(e, g2, uuid2);
        e = svc.lookupExperimenter(uuid2);

        omero.client client = newOmeroClient();
        client.createSession(uuid, uuid);
        init(client);

        List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
        groups.add(g);
        iAdmin.addGroups(e, groups);
    }

    /**
     * Tests the removal of a member of the group by the owner of the group. The
     * owner is NOT an administrator.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testOwnerRemoveExperimenterToGroup() throws Exception {
        // First create a new user.
        String uuid = UUID.randomUUID().toString();
        Experimenter e = createExperimenterI(uuid, "user", "user");
        IAdminPrx svc = root.getSession().getAdminService();

        // already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rw----"));

        // needed because e cannot be left only in the user group
        String uuid2 = UUID.randomUUID().toString();
        ExperimenterGroup g2 = new ExperimenterGroupI();
        g2.setName(omero.rtypes.rstring(uuid2));
        g2.setLdap(omero.rtypes.rbool(false));
        g2.getDetails().setPermissions(new PermissionsI("rw----"));

        // create group.
        long groupId = svc.createGroup(g);
        g = svc.getGroup(groupId);
        // create the user.
        long expId = newUserInGroupWithPassword(e, g, uuid);
        Experimenter owner = svc.lookupExperimenter(uuid);
        // set the user as the group owner.
        svc.setGroupOwner(g, owner);
        svc.setDefaultGroup(owner, g);

        // create another group and user
        String uuid3 = UUID.randomUUID().toString();
        e = createExperimenterI(uuid3, "user", "user");
        //expId = svc.createUser(e, uuid);
        expId = newUserInGroupWithPassword(e, g, uuid3);
        e = svc.lookupExperimenter(uuid3);
        g2 = svc.getGroup(svc.createGroup(g2));
        svc.addGroups(e, Collections.singletonList(g2));
        e = svc.getExperimenter(e.getId().getValue());
        // owner logs in.
        omero.client client = newOmeroClient();
        client.createSession(uuid, uuid);
        init(client);
        // iAdmin.
        List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
        groups.add(g);
        iAdmin.removeGroups(e, groups);
        client.closeSession();
    }

    /**
     * Tests the addition of an existing experimenter to a group. This test uses
     * the <code>addGroups</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testAddExistingUserToGroup() throws Exception {
        String uuid = UUID.randomUUID().toString();
        Experimenter e = createExperimenterI(uuid, "user", "user");
        IAdminPrx svc = root.getSession().getAdminService();

        // already tested
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rw----"));
        long groupId = svc.createGroup(g);

        long id = svc.createUser(e, uuid);

        String uuid2 = UUID.randomUUID().toString();
        g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid2));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rw----"));
        long id2 = svc.createGroup(g);
        IQueryPrx query = root.getSession().getQueryService();

        // Check if we have a user
        ParametersI p = new ParametersI();
        p.addId(id);
        e = (Experimenter) query.findByQuery(
                "select distinct e from Experimenter e where e.id = :id", p);

        // load the group
        p = new ParametersI();
        p.addId(id2);
        ExperimenterGroup eg = (ExperimenterGroup) query.findByQuery(
                "select distinct g from ExperimenterGroup g where g.id = :id",
                p);
        Assert.assertNotNull(eg);
        Assert.assertNotNull(e);
        List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
        groups.add(eg);
        svc.addGroups(e, groups);
        // now check that there are linked
        ExperimenterGroup userGroup = svc.lookupGroup(roles.userGroupName);
        List<Long> ids = new ArrayList<Long>();
        ids.add(groupId);
        ids.add(id2);
        ids.add(userGroup.getId().getValue());
        p = new ParametersI();
        p.addLongs("gids", ids);
        List<IObject> list = query.findAllByQuery("select m "
                + "from GroupExperimenterMap as m "
                + "left outer join fetch m.child "
                + "left outer join fetch m.parent"
                + " where m.parent.id in (:gids)", p);
        Assert.assertNotNull(list);
        Iterator<IObject> i = list.iterator();
        GroupExperimenterMap geMap;
        int count = 0;
        while (i.hasNext()) {
            geMap = (GroupExperimenterMap) i.next();
            if (geMap.getChild().getId().getValue() == id)
                count++;
        }
        Assert.assertEquals(3, count);
    }

    /**
     * Test that security role IDs and names are distinct and that no names are <code>null</code>.
     * @throws ServerError unexpected
     */
    @Test
    public void testSecurityRolesDistinct() throws ServerError {
        final Set<Long> userIds = new HashSet<Long>();
        final Set<Long> groupIds = new HashSet<Long>();
        final Set<String> userNames = new HashSet<String>();
        final Set<String> groupNames = new HashSet<String>();
        final Roles roles = root.getSession().getAdminService().getSecurityRoles();
        Assert.assertTrue(userIds.add(roles.rootId));
        Assert.assertTrue(userIds.add(roles.guestId));
        Assert.assertTrue(groupIds.add(roles.systemGroupId));
        Assert.assertTrue(groupIds.add(roles.userGroupId));
        Assert.assertTrue(groupIds.add(roles.guestGroupId));
        Assert.assertTrue(userNames.add(roles.rootName));
        Assert.assertTrue(userNames.add(roles.guestName));
        Assert.assertTrue(groupNames.add(roles.systemGroupName));
        Assert.assertTrue(groupNames.add(roles.userGroupName));
        Assert.assertTrue(groupNames.add(roles.guestGroupName));
        Assert.assertFalse(userNames.contains(null));
        Assert.assertFalse(groupNames.contains(null));
    }

    /**
     * Test that the root experimenter is reported to be a member of both the system and the user groups
     * and that the guest experimenter is reported to be a member of the guest group.
     * @throws ServerError unexpected
     */
    @Test
    public void testGroupMemberships() throws ServerError {
        final IAdminPrx adminSvc = root.getSession().getAdminService();
        final Roles roles = adminSvc.getSecurityRoles();

        final Experimenter root = new ExperimenterI(roles.rootId, false);
        final List<Long> rootGroups = adminSvc.getMemberOfGroupIds(root);
        Assert.assertTrue(rootGroups.contains(roles.systemGroupId));
        Assert.assertTrue(rootGroups.contains(roles.userGroupId));

        final Experimenter guest = new ExperimenterI(roles.guestId, false);
        final List<Long> guestGroups = adminSvc.getMemberOfGroupIds(guest);
        Assert.assertTrue(guestGroups.contains(roles.guestGroupId));
    }

    /**
     * Test getting the set of light administrator privileges for a user.
     * Includes configuration of unknown privileges and non-Boolean values.
     * @throws Exception unexpected
     */
    @Test
    public void testGetAdminPrivileges() throws Exception {
        final ServiceFactoryPrx rootSession = root.getSession();
        /* create a new light administrator */
        final EventContext ctx = newUserAndGroup("rw----");
        Experimenter user = new ExperimenterI(ctx.userId, false);
        final ExperimenterGroup systemGroup = new ExperimenterGroupI(iAdmin.getSecurityRoles().systemGroupId, false);
        rootSession.getAdminService().addGroups(user, Collections.<ExperimenterGroup>singletonList(systemGroup));
        /* set the light administrator's privileges */
        user = (Experimenter) rootSession.getQueryService().get("Experimenter", user.getId().getValue());
        final List<NamedValue> config = new ArrayList<NamedValue>();
        config.add(new NamedValue(AdminPrivilegeModifyUser.value, "true"));
        config.add(new NamedValue(AdminPrivilegeReadSession.value, "nonsense"));
        config.add(new NamedValue("nonsense", "true"));
        user.setConfig(config);
        rootSession.getUpdateService().saveObject(user);
        /* check the privileges */
        final Set<String> actualPrivileges = new HashSet<String>();
        for (final AdminPrivilege privilege : iAdmin.getAdminPrivileges(user)) {
            actualPrivileges.add(privilege.getValue().getValue());
        }
        Assert.assertTrue(actualPrivileges.contains(AdminPrivilegeModifyUser.value));
        Assert.assertFalse(actualPrivileges.contains("nonsense"));
        Assert.assertFalse(actualPrivileges.contains(AdminPrivilegeReadSession.value));
    }

    /**
     * Test setting a user's set of light administrator privileges.
     * Expects the configuration to undergo minimal changes to effect the requirement.
     * @throws Exception unexpected
     */
    @Test
    public void testSetAdminPrivileges() throws Exception {
        final ServiceFactoryPrx rootSession = root.getSession();
        final AdminPrivilege modifyUser = new AdminPrivilegeI();
        final AdminPrivilege readSession = new AdminPrivilegeI();
        modifyUser.setValue(omero.rtypes.rstring(AdminPrivilegeModifyUser.value));
        readSession.setValue(omero.rtypes.rstring(AdminPrivilegeReadSession.value));
        /* create a user */
        final EventContext ctx = newUserAndGroup("rw----");
        /* set the user's original configuration */
        Experimenter user = (Experimenter) rootSession.getQueryService().get("Experimenter", ctx.userId);
        List<NamedValue> config = new ArrayList<NamedValue>();
        config.add(new NamedValue(AdminPrivilegeModifyUser.value, "nonsense"));
        config.add(new NamedValue("nonsense", "true"));
        user.setConfig(config);
        rootSession.getUpdateService().saveObject(user);
        /* now set ModifyUser but no other privileges */
        rootSession.getAdminService().setAdminPrivileges((Experimenter) user, Collections.singletonList(modifyUser));
        /* check if ModifyUser was added to the configuration without affecting the other entries */
        user = (Experimenter) iQuery.get("Experimenter", ctx.userId);
        ImmutableMultimap<String, String> configMap = getUserConfig(user);
        Assert.assertEquals(configMap.get(AdminPrivilegeModifyUser.value), Collections.singleton("true"));
        Assert.assertEquals(configMap.get("nonsense"), Collections.singleton("true"));
        Assert.assertEquals(configMap.get(AdminPrivilegeReadSession.value), Collections.singleton("false"));
        /* now set ReadSession */
        rootSession.getAdminService().setAdminPrivileges((Experimenter) user, Collections.singletonList(readSession));
        /* check if ReadSession was set and ModifyUser was cleared through only minimal changes to the configuration */
        user = (Experimenter) iQuery.get("Experimenter", ctx.userId);
        configMap = getUserConfig(user);
        Assert.assertEquals(configMap.get(AdminPrivilegeModifyUser.value), Collections.singleton("false"));
        Assert.assertEquals(configMap.get("nonsense"), Collections.singleton("true"));
        Assert.assertEquals(configMap.get(AdminPrivilegeReadSession.value), Collections.singleton("true"));
    }

    /**
     * Test retrieving the list of light administrators who have all of a given set of privileges.
     * @throws Exception unexpected
     */
    @Test
    public void testGetAdminsWithPrivileges() throws Exception {
        final ServiceFactoryPrx rootSession = root.getSession();
        final AdminPrivilege sudo = new AdminPrivilegeI();
        final AdminPrivilege writeOwned = new AdminPrivilegeI();
        sudo.setValue(omero.rtypes.rstring(AdminPrivilegeSudo.value));
        writeOwned.setValue(omero.rtypes.rstring(AdminPrivilegeWriteOwned.value));
        /* note IDs of coming users */
        final Set<Long> allUsers = new HashSet<Long>();
        final Set<Long> canSudo = new HashSet<Long>();
        final Set<Long> canWriteOwned = new HashSet<Long>();
        /* set up users with various combinations of privileges */
        final EventContext ctx = newUserAndGroup("rw----");
        final ExperimenterGroup systemGroup = new ExperimenterGroupI(iAdmin.getSecurityRoles().systemGroupId, false);
        final Boolean[] booleanValues = new Boolean[] {null, true, false};
        for (final Boolean isSudo : booleanValues) {
            for (final Boolean isWriteOwned : booleanValues) {
                final Long newUserId = newUserInGroup().userId;
                final Experimenter user = (Experimenter) rootSession.getQueryService().get("Experimenter", newUserId);
                final List<NamedValue> config = new ArrayList<NamedValue>();
                if (isSudo != null) {
                    config.add(new NamedValue(sudo.getValue().getValue(), Boolean.toString(isSudo)));
                }
                if (!Boolean.FALSE.equals(isSudo)) {
                    canSudo.add(newUserId);
                }
                if (isWriteOwned != null) {
                    config.add(new NamedValue(writeOwned.getValue().getValue(), Boolean.toString(isWriteOwned)));
                }
                if (!Boolean.FALSE.equals(isWriteOwned)) {
                    canWriteOwned.add(newUserId);
                }
                allUsers.add(newUserId);
                user.setConfig(config);
                rootSession.getUpdateService().saveObject(user);
                rootSession.getAdminService().addGroups(user, Collections.<ExperimenterGroup>singletonList(systemGroup));
            }
        }
        /* prepare for testing */
        loginUser(ctx);
        Set<Long> expectedUsers, actualUsers;
        /* check that some users can neither sudo nor write data owned by other users */
        Assert.assertFalse(Sets.difference(allUsers, Sets.union(canSudo, canWriteOwned)).isEmpty());
        /* check the list of users who can sudo */
        expectedUsers = canSudo;
        Assert.assertFalse(expectedUsers.isEmpty());
        actualUsers = new HashSet<Long>();
        for (final Experimenter user : iAdmin.getAdminsWithPrivileges(ImmutableList.of(sudo))) {
            actualUsers.add(user.getId().getValue());
        }
        actualUsers.retainAll(allUsers);
        Assert.assertEquals(actualUsers, expectedUsers);
        /* check the list of users who can write data owned by other users */
        expectedUsers = canWriteOwned;
        Assert.assertFalse(expectedUsers.isEmpty());
        actualUsers = new HashSet<Long>();
        for (final Experimenter user : iAdmin.getAdminsWithPrivileges(ImmutableList.of(writeOwned))) {
            actualUsers.add(user.getId().getValue());
        }
        actualUsers.retainAll(allUsers);
        Assert.assertEquals(actualUsers, expectedUsers);
        /* check the list of users who can both sudo and write data owned by other users */
        expectedUsers = Sets.intersection(canSudo, canWriteOwned);
        Assert.assertFalse(expectedUsers.isEmpty());
        actualUsers = new HashSet<Long>();
        for (final Experimenter user : iAdmin.getAdminsWithPrivileges(ImmutableList.of(sudo, writeOwned))) {
            actualUsers.add(user.getId().getValue());
        }
        actualUsers.retainAll(allUsers);
        Assert.assertEquals(actualUsers, expectedUsers);
    }
}
