/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package coverage;

import static omero.rtypes.*;
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
        prx.changeUserPassword(e.getOmeName().getValue(), rstring("foo"));
    }

    // ~ Helpers
    // =========================================================================

    private long newUser(IAdminPrx prx) throws ServerError {
        Experimenter e = new ExperimenterI();
        e.setFirstName(rstring("admin"));
        e.setLastName(rstring("test"));
        e.setOmeName(rstring(Ice.Util.generateUUID()));
        long id = prx.createUser(e, "default");
        return id;
    }
}
