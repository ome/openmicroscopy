/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.client.itests;

import java.util.Date;

import junit.framework.TestCase;
import ome.api.IUpdate;
import ome.model.containers.Project;
import ome.system.ServiceFactory;

import org.testng.annotations.Test;

@Test(groups = { "client", "integration", "manual" })
public class EjbTest extends TestCase {

    ServiceFactory sf = new ServiceFactory();

    @Test
    public void test_withLogin() throws Exception {
        IUpdate iUpdate = sf.getUpdateService();
        Project p = new Project();
        p.setName("ejb test:" + new Date());
        iUpdate.saveObject(p);
    }

    @Test(groups = "ticket:818")
    public void testMemoryLeak() throws Exception {
        sf.getAdminService().getEventContext();
        Thread t = null;
        for (int i = 0; i < 100; i++) {
            t = new Thread() {
                @Override
                public void run() {
                    sf.getConfigService().getServerTime();
                };
            };
            t.run();
        }
        t.join();
    }

}
