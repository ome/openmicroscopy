/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sec;

import static ome.model.internal.Permissions.Right.READ;
import static ome.model.internal.Permissions.Role.GROUP;
import static ome.model.internal.Permissions.Role.USER;
import static ome.model.internal.Permissions.Role.WORLD;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import ome.model.IObject;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.security.AdminAction;
import ome.server.itests.AbstractManagedContextTest;
import ome.server.itests.Wrap;
import ome.server.itests.Wrap.Backdoor;
import ome.services.query.Definitions;
import ome.services.query.Query;
import ome.services.query.QueryParameterDef;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

@Test(groups = { "ticket:117", "security", "filter" })
public class SecurityFilterTest extends AbstractManagedContextTest {

    static String ticket117 = "ticket:117";

    Permissions userReadableOnly = new Permissions().revoke(GROUP, READ)
            .revoke(WORLD, READ);

    Permissions unreadable = new Permissions(userReadableOnly).revoke(USER,
            READ);

    Permissions groupReadable = new Permissions().revoke(WORLD, READ);

    List<Experimenter> users = new ArrayList<Experimenter>();

    List<String> names = new ArrayList<String>();

    ome.api.local.LocalQuery wrappedQuery;

    @Configuration(beforeTestClass = true)
    public void createData() throws Exception {
        setUp();
        wrappedQuery = this.iQuery;

        String gname = uuid();
        ExperimenterGroup g = new ExperimenterGroup();
        g.setName(gname);
        iAdmin.createGroup(g);

        for (int i = 0; i < 3; i++) {
            String name = UUID.randomUUID().toString();
            Experimenter e2 = new Experimenter();
            e2.setOmeName(name);
            e2.setFirstName("security");
            e2.setLastName("filter too");
            users.add(new Experimenter(factory.getAdminService().createUser(e2,
                    gname), false));
            names.add(name);

        }
        tearDown();
    }

    /**
     * Below we are resetting the iQuery value for use with {@link Backdoor},
     * so we will need to make sure this is valid for all other tests.
     */
    @BeforeMethod
    public void fixQuery() throws Exception {
        this.iQuery = wrappedQuery;
    }

    @Test
    public void testFilterDisallowsRead() throws Exception {

        Image i;

        loginUser(names.get(0));
        i = createImage(userReadableOnly);

        loginUser(names.get(1));
        assertCannotReadImage(i);

        loginUser(names.get(0));
        assertCanReadImage(i);
    }

    @Test
    public void testRootCanReadAll() throws Exception {

        Image i;

        loginUser(names.get(0));
        i = createImage(unreadable);
        assertCannotReadImage(i);

        loginUser(names.get(1));
        assertCannotReadImage(i);

        loginRoot();
        assertCanReadImage(i);
    }

    @Test(groups = "broken")
    // Must add the xml-rpc jar for this to work
    public void testRunAsAdminCanReadAll() throws Exception {

        final Image i;
        final Dataset d;

        loginUser(names.get(0));
        d = createDataset(userReadableOnly);
        i = createImage(userReadableOnly);

        loginUser(names.get(1));
        assertCannotReadImage(d, i);

        final SecurityFilterTest test = this;
        new Wrap(names.get(1), new Wrap.QueryBackdoor() {
            @RolesAllowed("user")
            public void run() {
                test.iQuery = this; // Use this (unwrapped) instance for call
                securitySystem.runAsAdmin(new AdminAction() {
                    public void runAsAdmin() {
                        assertCanReadImage(d, i);
                    }
                });
            }
        });
        this.iQuery = wrappedQuery;

        loginUser(names.get(0));
        assertCanReadImage(d, i);
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

        loginUser(names.get(0));
        i = createImage(groupReadable);
        factory.getAdminService().changeGroup(i, group.getName());
        assertCanReadImage(i);

        loginUser(names.get(1));
        assertCanReadImage(i);

        loginUser(names.get(2));
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
        loginUser(names.get(0));
        i = createImage(userReadableOnly);
        factory.getAdminService().changeGroup(i, group.getName());
        assertCanReadImage(i);

        // others in group can't read
        loginUser(names.get(1));
        assertCannotReadImage(i);

        // but PI can
        loginUser(names.get(2));
        assertCanReadImage(i);
    }

    @Test
    public void testUserCanHideFromSelf() throws Exception {
        Image i;

        // create an image with no permissions
        loginUser(names.get(0));
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

    private Image createImage(Permissions p) {
        Image img = new Image();
        img.setName(ticket117 + ":" + UUID.randomUUID().toString());
        return createObject(img, p);
    }

    private Dataset createDataset(Permissions p) {
        Dataset ds = new Dataset();
        ds.setName(ticket117 + ":" + UUID.randomUUID().toString());
        return createObject(ds, p);
    }

    private <T extends IObject> T createObject(T obj, Permissions p) {
        obj.getDetails().setPermissions(p);
        obj = factory.getUpdateService().saveAndReturnObject(obj);
        return obj;
    }

    private <T extends IObject> void assertCannotReadImage(T... ts) {

        T test;
        for (T t : ts) {
            test = getAsString(t);
            assertNull(t + "==null", test);

            test = getByCriteria(t);
            assertNull(t + "==null", test);
        }

    }

    private <T extends IObject> void assertCanReadImage(T... ts) {

        T test;
        for (T t : ts) {
            test = getAsString(t);
            assertNotNull(t + "!=null", test);

            test = getByCriteria(t);
            assertNotNull(t + "!=null", test);
        }

    }

    private <T extends IObject> T getAsString(T obj) {
        return (T) iQuery.findByString(obj.getClass(), "name", ByNameQuery
                .name(obj));
    }

    private <T extends IObject> T getByCriteria(T obj) {
        return (T) iQuery.execute(new ByNameQuery(obj));
    }
}

class ByNameQuery extends Query {

    static Definitions defs = new Definitions(new QueryParameterDef("name",
            String.class, false));

    Object obj;

    public <T extends IObject> ByNameQuery(T obj) {
        super(defs, new Parameters(new Filter().unique()).addString("name",
                name(obj)));
        this.obj = obj;
    }

    @Override
    protected void buildQuery(Session session) throws HibernateException,
            SQLException {
        Criteria c = session.createCriteria(obj.getClass());
        c.add(Restrictions.eq("name", value("name")));
        setCriteria(c);
    }

    static String name(Object object) {
        try {
            Field f = object.getClass().getDeclaredField("name");
            f.setAccessible(true);
            return (String) f.get(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
