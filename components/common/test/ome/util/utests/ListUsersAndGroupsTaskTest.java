/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.utests;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import ome.api.IQuery;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.OmeroContext;
import ome.system.ServiceFactory;
import ome.util.tasks.admin.ListUsersAndGroupsTask;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 */
public class ListUsersAndGroupsTaskTest extends MockObjectTestCase {

    Mock mockQuery;

    ServiceFactory sf;

    List<Experimenter> users;

    List<ExperimenterGroup> groups;

    static String USERS = ListUsersAndGroupsTask.Keys.users.toString();

    static String GROUPS = ListUsersAndGroupsTask.Keys.groups.toString();

    static Properties users_out = new Properties(),
            users_err = new Properties(), groups_out = new Properties(),
            groups_err = new Properties();
    static {
        users_out.setProperty(USERS, "out");
        users_err.setProperty(USERS, "err");
        groups_out.setProperty(GROUPS, "out");
        groups_err.setProperty(GROUPS, "err");
    }

    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();

        data();
        mockQuery = mock(IQuery.class);
        final IQuery proxy = (IQuery) mockQuery.proxy();
        sf = new ServiceFactory((OmeroContext) null) {
            @Override
            public IQuery getQueryService() {
                return proxy;
            }
        };

    };

    @Test
    public void testNoProperties() throws Exception {
        ListUsersAndGroupsTask t = new ListUsersAndGroupsTask(sf, null);
        t.run();
    }

    @Test
    public void testJustUsersStdOut() throws Exception {
        checksUsers();
        Properties p = getProps(users_out);
        ListUsersAndGroupsTask t = new ListUsersAndGroupsTask(sf, p);
        t.run();
    }

    @Test
    public void testJustUsersStdErr() throws Exception {
        checksUsers();
        Properties p = getProps(users_err);
        ListUsersAndGroupsTask t = new ListUsersAndGroupsTask(sf, p);
        t.run();
    }

    @Test
    public void testJustGroupsStdOut() throws Exception {
        checksGroups();
        Properties p = getProps(groups_out);
        ListUsersAndGroupsTask t = new ListUsersAndGroupsTask(sf, p);
        t.run();
    }

    @Test
    public void testJustGroupsStdErr() throws Exception {
        checksGroups();
        Properties p = getProps(groups_err);
        ListUsersAndGroupsTask t = new ListUsersAndGroupsTask(sf, p);
        t.run();
    }

    @Test
    public void testSameTmpFileForBoth() throws Exception {
        checksGroups();
        checksUsers();
        File tmp = File.createTempFile("omero", ".tmp");
        Properties p = getProps();
        p.setProperty(USERS, tmp.getAbsolutePath());
        p.setProperty(GROUPS, tmp.getAbsolutePath());
        ListUsersAndGroupsTask t = new ListUsersAndGroupsTask(sf, p);
        t.run();
    }

    // ~ Helpers
    // =========================================================================

    void checksUsers() {
        mockQuery.expects(once()).method("findAllByQuery").will(
                returnValue(users));
    }

    void checksGroups() {
        mockQuery.expects(once()).method("findAllByQuery").will(
                returnValue(groups));
    }

    Properties getProps(Properties... props) {
        Properties p = new Properties();
        for (Properties properties : props) {
            p.putAll(properties);
        }
        return p;
    }

    void data() {
        Experimenter[] es = new Experimenter[3];
        for (int i = 0; i < es.length; i++) {
            es[i] = new Experimenter();
            if (i == 0) {
                continue; // testing null handling
            }
            es[i].setId(Long.valueOf(i));
            es[i].setOmeName("name" + i);
            es[i].setFirstName("first" + i);
            es[i].setLastName("last" + i);
        }
        users = Arrays.asList(es);

        ExperimenterGroup[] gs = new ExperimenterGroup[3];
        for (int i = 0; i < gs.length; i++) {
            gs[i] = new ExperimenterGroup();
            if (i == 0) {
                continue; // testing null handling
            }
            gs[i].setId(Long.valueOf(i));
            gs[i].setName("name" + i);
            gs[i].setDescription("desc" + i);
            gs[i].getDetails().setOwner(new Experimenter());
            gs[i].getDetails().getOwner().setId(-1L * i);
            gs[i].getDetails().getOwner().setOmeName("owner" + i);
        }
        groups = Arrays.asList(gs);

    }

}
