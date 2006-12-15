/*
 * ome.client.itests.tasks.AbstractAdminTaskTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.client.itests.tasks;

import org.testng.annotations.*;

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
        rootString = new String[] { "user=" + rootLogin.getName(),
                "group=" + rootLogin.getGroup(),
                "type=" + rootLogin.getEvent(),
                "pass=" + rootLogin.getPassword() };
    }

    // ~ Helpers
    // =========================================================================

    protected String[] join(String[] arr1, String[] arr2) {
        String[] arr = new String[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, arr, 0, arr1.length);
        System.arraycopy(arr2, 0, arr, arr1.length, arr2.length);
        return arr;
    }
}
