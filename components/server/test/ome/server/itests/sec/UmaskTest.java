/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sec;

import org.testng.annotations.Test;

import ome.model.containers.Project;
import ome.model.internal.Permissions.Flag;
import ome.server.itests.AbstractManagedContextTest;

@Test(groups = { "ticket:182", "ticket:307" })
public class UmaskTest extends AbstractManagedContextTest {

    // ~ Tickets
    // =========================================================================

    public void testSoftIsRemovedOnTransientDetails() throws Exception {
        Project p = new Project();
        p.setName("ticket:307");
        assertNull(p.getDetails().getPermissions());
        p = iUpdate.saveAndReturnObject(p);
        assertNotNull(p.getDetails().getPermissions());
        assertFalse(p.getDetails().getPermissions().isSet(Flag.SOFT));
    }

}
