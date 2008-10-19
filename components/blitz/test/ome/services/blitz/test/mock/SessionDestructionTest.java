/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.mock;

import ome.model.meta.Session;
import ome.services.messages.DestroySessionMessage;
import omero.api.ServiceFactoryPrx;

import org.jmock.MockObjectTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * Creates sessions and tests the various ways they can be destroyed. Initially
 * (Oct. 2008) this was used to manually inspect in a profiler whether or not
 * all related instances were being properly cleaned up.
 */
public class SessionDestructionTest extends MockObjectTestCase {

    MockFixture fixture;

    @AfterMethod
    public void shutdownFixture() {
        fixture.tearDown();
    }

    @Test
    public void testOneSessionGlacierDestruction() throws Exception {
        fixture = new MockFixture(this);
        ServiceFactoryPrx sf = fixture.createServiceFactory();
        sf.closeOnDestroy();
        sf.getAdminService();

        fixture.mock("sessionsMock").expects(once()).method("detach").will(
                returnValue(0));
        fixture.mock("sessionsMock").expects(once()).method("close").will(
                returnValue(-2));
        sf.destroy();

    }

    @Test
    public void testOneSessionNotificationDestruction() throws Exception {
        fixture = new MockFixture(this);
        ServiceFactoryPrx sf = fixture.createServiceFactory();
        sf.closeOnDestroy();
        sf.getAdminService();

        fixture.mock("sessionsMock").expects(once()).method("detach").will(
                returnValue(0));
        fixture.mock("sessionsMock").expects(once()).method("close").will(
                returnValue(-2));
        fixture.getSessionManager().onApplicationEvent(
                new DestroySessionMessage(this, "my-session-uuid"));

    }

    @Test
    public void testOneSessionDetachNotificationDestruction() throws Exception {
        fixture = new MockFixture(this);
        ServiceFactoryPrx sf = fixture.createServiceFactory();
        sf.detachOnDestroy();
        sf.getAdminService();

        fixture.mock("sessionsMock").expects(once()).method("detach").will(
                returnValue(0));
        fixture.mock("sessionsMock").expects(once()).method("close").will(
                returnValue(-2));
        fixture.getSessionManager().onApplicationEvent(
                new DestroySessionMessage(this, "my-session-uuid"));

    }

    @Test
    public void testTwoClientsSessionGlacierDestruction() throws Exception {

        fixture = new MockFixture(this);
        Session s = fixture.session();
        net.sf.ehcache.Cache c = fixture.cache();
        ServiceFactoryPrx sf1 = fixture.createServiceFactory(s, c, "sf1");
        ServiceFactoryPrx sf2 = fixture.createServiceFactory(s, c, "sf2");
        sf1.closeOnDestroy();
        sf2.closeOnDestroy();
        sf1.getAdminService();
        sf2.getAdminService();

        fixture.mock("sessionsMock").expects(once()).method("detach").will(
                returnValue(1));
        sf1.destroy();
        fixture.mock("sessionsMock").expects(once()).method("detach").will(
                returnValue(0));
        fixture.mock("sessionsMock").expects(once()).method("close").will(
                returnValue(-2));
        sf2.destroy();
    }

    @Test
    public void testTwoClientsSessionNotificationDestruction() throws Exception {

        fixture = new MockFixture(this);
        Session s = fixture.session();
        net.sf.ehcache.Cache c = fixture.cache();
        ServiceFactoryPrx sf1 = fixture.createServiceFactory(s, c, "sf1");
        ServiceFactoryPrx sf2 = fixture.createServiceFactory(s, c, "sf2");
        sf1.closeOnDestroy();
        sf2.closeOnDestroy();
        sf1.getAdminService();
        sf2.getAdminService();

        fixture.getSessionManager().onApplicationEvent(
                new DestroySessionMessage(this, "my-session-uuid"));
    }

    @Test
    public void testTwoClientsSessionDetachedNotificationDestruction()
            throws Exception {

        fixture = new MockFixture(this);
        Session s = fixture.session();
        net.sf.ehcache.Cache c = fixture.cache();
        ServiceFactoryPrx sf1 = fixture.createServiceFactory(s, c, "sf1");
        ServiceFactoryPrx sf2 = fixture.createServiceFactory(s, c, "sf2");
        sf1.detachOnDestroy();
        sf2.detachOnDestroy();
        sf1.getAdminService();
        sf2.getAdminService();

        fixture.getSessionManager().onApplicationEvent(
                new DestroySessionMessage(this, "my-session-uuid"));
    }

}
