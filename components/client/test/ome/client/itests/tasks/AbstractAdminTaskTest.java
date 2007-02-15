/*
 * ome.client.itests.tasks.AbstractAdminTaskTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.client.itests.tasks;

import java.util.UUID;

import org.testng.annotations.*;

import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.Login;
import ome.system.ServiceFactory;
import junit.framework.TestCase;

@Test(groups = { "client", "integration" })
public abstract class AbstractAdminTaskTest extends TestCase {

    ServiceFactory sf, root;

    String[] rootString;

    @org.testng.annotations.Configuration(beforeTestClass = true)
    public void setup() {
        sf = new ServiceFactory("ome.client.test");
        Login rootLogin = (Login) sf.getContext().getBean("rootLogin");
        root = new ServiceFactory(rootLogin);
        rootString = new String[] { "omero.user=" + rootLogin.getName(),
                "omero.group=" + rootLogin.getGroup(),
                "omero.type=" + rootLogin.getEvent(),
                "omero.pass=" + rootLogin.getPassword() };
    }

    // ~ Helpers
    // =========================================================================

    protected String[] join(String[] arr1, String[] arr2) {
        String[] arr = new String[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, arr, 0, arr1.length);
        System.arraycopy(arr2, 0, arr, arr1.length, arr2.length);
        return arr;
    }

    protected String makeGroup() {
        String uuid = UUID.randomUUID().toString();
        ExperimenterGroup group = new ExperimenterGroup();
        group.setName(uuid);
        root.getAdminService().createGroup(group);
        return uuid;
    }
    
    protected String makeUser(String group) {
        String uuid = UUID.randomUUID().toString();
        Experimenter user = new Experimenter();
        user.setOmeName(uuid);
        user.setFirstName("Task");
        user.setLastName("Test");
        root.getAdminService().createUser(user, group);
        return uuid;
    }
}
