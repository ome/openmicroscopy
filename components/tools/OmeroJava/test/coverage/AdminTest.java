/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests.coverage;

import ome.icy.model.itests.IceTest;
import omero.JString;
import omero.ServerError;
import omero.api.IAdminPrx;
import omero.model.Experimenter;
import omero.model.ExperimenterI;

import org.testng.annotations.Test;

public class AdminTest extends IceTest {

    @Test
    public void testNewUser() throws Exception {
        IAdminPrx prx = root.getServiceFactory().getAdminService();
        assertNotNull(prx);
        newUser(prx);
    }

    @Test
    public void testChangeUserPassword() throws Exception {
        IAdminPrx prx = root.getServiceFactory().getAdminService();
        assertNotNull(prx);
        long id = newUser(prx);
        Experimenter e = prx.getExperimenter(id);
        prx.changeUserPassword(e.getOmeName().val, new JString("foo"));
    }

    // ~ Helpers
    // =========================================================================

    private long newUser(IAdminPrx prx) throws ServerError {
        Experimenter e = new ExperimenterI();
        e.setFirstName(new JString("admin"));
        e.setLastName(new JString("test"));
        e.setOmeName(new JString(Ice.Util.generateUUID()));
        long id = prx.createUser(e, "default");
        return id;
    }
}
