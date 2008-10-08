/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package coverage;

import java.io.File;

import junit.framework.TestCase;
import omero.api.IUpdatePrx;
import omero.api.IUpdatePrxHelper;
import omero.constants.UPDATESERVICE;
import omero.model.ImageI;

import org.springframework.util.ResourceUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import Ice.RouterPrx;

@Test(groups = { "integration", "blitz", "client" })
public abstract class IceTest extends TestCase {

    protected omero.client ice = null, root = null;
    protected Ice.Communicator ic = null;

    @Override
    @BeforeMethod
    public void setUp() throws Exception {
        File local = ResourceUtils.getFile("classpath:local.properties");
        ice = new omero.client(local);
        ice.createSession();
        root = new omero.client(local);
        root.createSession("root", ice.getProperty("omero.rootpass"));
        ic = ice.getCommunicator();
    }

    @Override
    @AfterMethod
    public void tearDown() throws Exception {
        ice.closeSession();
        root.closeSession();
    }

    // ~ Helpers
    // =========================================================================

    protected Glacier2.RouterPrx getRouter(Ice.Communicator ic)
            throws Exception {
        RouterPrx defaultRouter = ic.getDefaultRouter();
        Glacier2.RouterPrx router = Glacier2.RouterPrxHelper
                .checkedCast(defaultRouter);
        return router;
    }

    protected Glacier2.SessionPrx getSession(Ice.Communicator ic)
            throws Exception {
        Glacier2.SessionPrx sessionPrx = getRouter(ic).createSession("josh",
                "test");
        return sessionPrx;
    }

    protected void closeSession(Ice.Communicator ic) throws Exception {
        getRouter(ic).destroySession();
    }

    protected IUpdatePrx checkUpdateService(Ice.Communicator ic)
            throws Exception {
        Ice.ObjectPrx base = ic.stringToProxy(UPDATESERVICE.value
                + ":default -p 10000");
        if (base == null) {
            throw new RuntimeException("Cannot create proxy");
        }

        IUpdatePrx iUpdate = IUpdatePrxHelper.checkedCast(base);
        if (iUpdate == null) {
            throw new RuntimeException("Invalid proxy");
        }

        iUpdate.saveAndReturnObject(new ImageI());
        return iUpdate;
    }
}
