/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.client.itests;

import org.testng.annotations.*;
import java.util.Date;

import junit.framework.TestCase;

import ome.api.IUpdate;
import ome.model.containers.Project;
import ome.system.ServiceFactory;

@Test(groups = { "client", "integration" })
public class EjbTest extends TestCase {

    ServiceFactory sf = new ServiceFactory();

    @Test
    public void test_withLogin() throws Exception {
        IUpdate iUpdate = sf.getUpdateService();
        Project p = new Project();
        p.setName("ejb test:" + new Date());
        iUpdate.saveObject(p);
    }

}
