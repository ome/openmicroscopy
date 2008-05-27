/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.client.itests;

import java.util.Properties;
import java.util.ResourceBundle;
import java.util.UUID;

import junit.framework.TestCase;
import ome.api.IQuery;
import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.model.meta.Experimenter;
import ome.system.Login;
import ome.system.ServiceFactory;

import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;

@Test(groups = { "client", "integration" })
public class LoginTest extends TestCase {

    static ResourceBundle locals = ResourceBundle.getBundle("local");
    static String rootpass = locals.getString("omero.rootpass");

    @Test
    public void test_withPropertiesNull() throws Exception {
        Properties p = new Properties();
        ServiceFactory factory = new ServiceFactory(p);
        IQuery iQuery = factory.getQueryService();
        iQuery.get(Experimenter.class, 0L);
    }

    @Test
    @ExpectedExceptions(ome.conditions.ApiUsageException.class)
    public void test_withLoginNull() throws Exception {
        Login login = new Login(null, "b");
        ServiceFactory factory = new ServiceFactory(login);
        IQuery iQuery = factory.getQueryService();
        iQuery.get(Experimenter.class, 0L);
    }

    @Test
    public void test_withProps() throws Exception {
        Properties p = new Properties();
        p.setProperty("omero.user", "root");
        p.setProperty("omero.pass", rootpass);
        ServiceFactory factory = new ServiceFactory(p);
        IQuery iQuery = factory.getQueryService();
        iQuery.get(Experimenter.class, 0L);
    }

    @Test
    public void test_withLogin() throws Exception {
        Login login = new Login("root", rootpass);
        ServiceFactory factory = new ServiceFactory(login);
        IQuery iQuery = factory.getQueryService();
        iQuery.get(Experimenter.class, 0L);
    }

    @Test(groups = { "ticket:182" })
    public void testLoginWithUmask() throws Exception {
        Login login = new Login("root", rootpass);
        ServiceFactory factory = new ServiceFactory(login);
        factory.setUmask(Permissions.READ_ONLY);
        Image i = new Image();
        i.setName(UUID.randomUUID().toString());
        Image test = factory.getUpdateService().saveAndReturnObject(i);
        assertFalse(test.getDetails().getPermissions().isGranted(Role.USER,
                Right.WRITE));
    }

    @Test(groups = { "ticket:297", "broken" })
    public void testLoginWithGUEST() throws Exception {
        ServiceFactory factory = new ServiceFactory(Login.GUEST);
        factory.getQueryService().findAll(Image.class, null);
    }

}
