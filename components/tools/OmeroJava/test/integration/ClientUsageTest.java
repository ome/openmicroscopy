/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;


import java.util.List;
import java.util.UUID;

import omero.RString;
import omero.client;
import omero.api.IAdminPrx;
import omero.api.ServiceFactoryPrx;
import omero.api.StatefulServiceInterfacePrx;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.PermissionsI;
import omero.sys.EventContext;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Various uses of the {@link omero.client} object. All configuration comes from
 * the ICE_CONFIG environment variable.
 */
@Test
public class ClientUsageTest extends AbstractServerTest {

    /**
     * Closes automatically the session.
     *
     * @throws Exception
     *             If an error occurred.
     */
    public void testClientClosedAutomatically() throws Exception {
        IAdminPrx svc = root.getSession().getAdminService();
        String uuid = UUID.randomUUID().toString();
        Experimenter e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring("integration"));
        e.setLastName(omero.rtypes.rstring("tester"));
        e.setLdap(omero.rtypes.rbool(false));
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rw----"));
        g = svc.getGroup(svc.createGroup(g));
        long uid = newUserInGroupWithPassword(e, g, uuid);
        svc.setDefaultGroup(svc.getExperimenter(uid), g);
        client = new omero.client();
        client.createSession(uuid, uuid);
        client.closeSession();
    }

    /**
     * Tests the usage of memory.
     *
     * @throws Exception
     *             If an error occurred.
     */
    public void testUseSharedMemory() throws Exception {
        IAdminPrx svc = root.getSession().getAdminService();
        String uuid = UUID.randomUUID().toString();
        Experimenter e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring("integration"));
        e.setLastName(omero.rtypes.rstring("tester"));
        e.setLdap(omero.rtypes.rbool(false));
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rw----"));
        g = svc.getGroup(svc.createGroup(g));
        long uid = newUserInGroupWithPassword(e, g, uuid);
        svc.setDefaultGroup(svc.getExperimenter(uid), g);
        client = new omero.client();
        client.createSession(uuid, uuid);

        Assert.assertEquals(0, client.getInputKeys().size());
        client.setInput("a", omero.rtypes.rstring("b"));
        Assert.assertEquals(1, client.getInputKeys().size());
        Assert.assertTrue(client.getInputKeys().contains("a"));
        Assert.assertEquals("b", ((RString) client.getInput("a")).getValue());

        client.closeSession();
    }

    /**
     * Test the creation of an insecure client.
     *
     * @throws Exception
     *             If an error occurred.
     */
    public void testCreateInsecureClientTicket2099() throws Exception {
        IAdminPrx svc = root.getSession().getAdminService();
        String uuid = UUID.randomUUID().toString();
        Experimenter e = new ExperimenterI();
        e.setOmeName(omero.rtypes.rstring(uuid));
        e.setFirstName(omero.rtypes.rstring("integration"));
        e.setLastName(omero.rtypes.rstring("tester"));
        e.setLdap(omero.rtypes.rbool(false));
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(omero.rtypes.rstring(uuid));
        g.setLdap(omero.rtypes.rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rw----"));
        g = svc.getGroup(svc.createGroup(g));
        long uid = newUserInGroupWithPassword(e, g, uuid);
        svc.setDefaultGroup(svc.getExperimenter(uid), g);
        client secure = new omero.client();
        ServiceFactoryPrx factory = secure.createSession(uuid, uuid);
        Assert.assertTrue(secure.isSecure());
        try {
            factory.getAdminService().getEventContext();
            omero.client insecure = secure.createClient(false);
            try {
                insecure.getSession().getAdminService().getEventContext();
                Assert.assertFalse(insecure.isSecure());
            } finally {
                insecure.closeSession();
            }
        } finally {
            secure.closeSession();
        }
    }

    /**
     * Test the {@link omero.client#getStatefulServices()} method. All stateful
     * services should be returned. Calling close on them should remove them
     * from future calls, which will allow
     * {@link ServiceFactoryPrx#setSecurityContext} to be called.
     *
     * @throws Exception
     *             If an error occurred.
     */
    public void testGetStatefulServices() throws Exception {
        ServiceFactoryPrx sf = root.getSession();
        sf.setSecurityContext(new omero.model.ExperimenterGroupI(0L, false));
        sf.createRenderingEngine();
        List<StatefulServiceInterfacePrx> srvs = root.getStatefulServices();
        Assert.assertEquals(1, srvs.size());
        try {
            sf.setSecurityContext(new omero.model.ExperimenterGroupI(1L, false));
            Assert.fail("Should not be allowed");
        } catch (omero.SecurityViolation sv) {
            // good
        }
        srvs.get(0).close();
        srvs = root.getStatefulServices();
        Assert.assertEquals(0, srvs.size());
        sf.setSecurityContext(new omero.model.ExperimenterGroupI(1L, false));
    }

    public void testJoinSession() throws Exception {
        
        //create a new user.
        EventContext ec = newUserAndGroup("rw----", true);
        String session = ec.sessionUuid;
        //delete the active client
        disconnect();
        client c = new client();
        try {
            c.joinSession(session);
            if (Ice.Util.intVersion() >= 30600) {
                Assert.fail("The session should have been deleted");
            }
        } catch (Exception e) {
            if (Ice.Util.intVersion() < 30600) {
                Assert.fail("Ice 3.5 do not close the session."
                        + "An error should not have been thrown");
            }
        }
    }
}
