/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.client.itests;

import org.testng.annotations.*;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import junit.framework.TestCase;

import ome.api.IQuery;
import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.model.meta.Experimenter;
import ome.system.Login;
import ome.system.Server;
import ome.system.ServiceFactory;

@Test(groups = { "client", "integration", "ignore" })
public class LoginTest extends TestCase {

    public void mageHasTooManyExperimenters() throws Exception {
        Login l = new Login("root","ome");
        Server s = new Server("warlock.openmicroscopy.org.uk",1099);
        ServiceFactory sf = new ServiceFactory(s,l);
        List<Experimenter> list = sf.getQueryService().findAll(Experimenter.class, null);
        System.out.println(list);
        Set<Experimenter> set = new HashSet<Experimenter>(list);
        System.out.println(set);
        
        sf.getQueryService().get(Experimenter.class, 15l);
    }

}
