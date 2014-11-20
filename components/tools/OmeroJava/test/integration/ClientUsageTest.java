/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import static omero.rtypes.rbool;
import static omero.rtypes.rstring;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

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
        e.setOmeName(rstring(uuid));
        e.setFirstName(rstring("integration"));
        e.setLastName(rstring("tester"));
        e.setLdap(rbool(false));
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(rstring(uuid));
        g.setLdap(rbool(false));
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
        e.setOmeName(rstring(uuid));
        e.setFirstName(rstring("integration"));
        e.setLastName(rstring("tester"));
        e.setLdap(rbool(false));
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(rstring(uuid));
        g.setLdap(rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rw----"));
        g = svc.getGroup(svc.createGroup(g));
        long uid = newUserInGroupWithPassword(e, g, uuid);
        svc.setDefaultGroup(svc.getExperimenter(uid), g);
        client = new omero.client();
        client.createSession(uuid, uuid);

        assertEquals(0, client.getInputKeys().size());
        client.setInput("a", rstring("b"));
        assertEquals(1, client.getInputKeys().size());
        assertTrue(client.getInputKeys().contains("a"));
        assertEquals("b", ((RString) client.getInput("a")).getValue());

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
        e.setOmeName(rstring(uuid));
        e.setFirstName(rstring("integration"));
        e.setLastName(rstring("tester"));
        e.setLdap(rbool(false));
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(rstring(uuid));
        g.setLdap(rbool(false));
        g.getDetails().setPermissions(new PermissionsI("rw----"));
        g = svc.getGroup(svc.createGroup(g));
        long uid = newUserInGroupWithPassword(e, g, uuid);
        svc.setDefaultGroup(svc.getExperimenter(uid), g);
        client secure = new omero.client();
        ServiceFactoryPrx factory = secure.createSession(uuid, uuid);
        assertTrue(secure.isSecure());
        try {
            factory.getAdminService().getEventContext();
            omero.client insecure = secure.createClient(false);
            try {
                insecure.getSession().getAdminService().getEventContext();
                assertFalse(insecure.isSecure());
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
        assertEquals(1, srvs.size());
        try {
            sf.setSecurityContext(new omero.model.ExperimenterGroupI(1L, false));
            fail("Should not be allowed");
        } catch (omero.SecurityViolation sv) {
            // good
        }
        srvs.get(0).close();
        srvs = root.getStatefulServices();
        assertEquals(0, srvs.size());
        sf.setSecurityContext(new omero.model.ExperimenterGroupI(1L, false));
    }

}
